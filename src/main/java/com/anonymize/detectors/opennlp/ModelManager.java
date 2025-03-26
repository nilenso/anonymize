package com.anonymize.detectors.opennlp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages downloading, caching, and accessing OpenNLP models. This utility ensures models are
 * available locally before they are needed.
 */
public class ModelManager {
  private static final Logger logger = LoggerFactory.getLogger(ModelManager.class);

  // Default models directory
  private static final String DEFAULT_MODELS_DIR = "models/opennlp";

  // Map of model URLs for downloading
  private static final Map<String, String> MODEL_URLS = new HashMap<>();

  // Flag to track which models have been downloaded
  private static final Map<String, Boolean> downloadedModels = new ConcurrentHashMap<>();

  // Lock to prevent concurrent downloads of the same model
  private static final Map<String, ReentrantLock> modelLocks = new ConcurrentHashMap<>();

  // Initialize with model URLs
  static {
    // Standard English models from Apache OpenNLP project
    MODEL_URLS.put(
        "en-ner-person.bin", "https://opennlp.sourceforge.net/models-1.5/en-ner-person.bin");
    MODEL_URLS.put(
        "en-ner-location.bin", "https://opennlp.sourceforge.net/models-1.5/en-ner-location.bin");
    MODEL_URLS.put(
        "en-ner-organization.bin",
        "https://opennlp.sourceforge.net/models-1.5/en-ner-organization.bin");

    // Initialize locks for each model
    MODEL_URLS.keySet().forEach(model -> modelLocks.put(model, new ReentrantLock()));

    // Create default models directory if it doesn't exist
    try {
      File defaultDir = new File(getDefaultModelsDirectory());
      if (!defaultDir.exists()) {
        defaultDir.mkdirs();
      }
    } catch (Exception e) {
      logger.warn("Could not create default models directory: {}", e.getMessage());
    }
  }

  // Base directory where models are stored
  private final String modelsDirectory;

  /** Creates a new ModelManager with the default models directory. */
  public ModelManager() {
    this(getDefaultModelsDirectory());
  }

  /**
   * Creates a new ModelManager with a specified models directory.
   *
   * @param modelsDirectory The directory to store downloaded models
   */
  public ModelManager(String modelsDirectory) {
    this.modelsDirectory = modelsDirectory;
    ensureModelsDirectoryExists();
  }

  /**
   * Ensures a model is available locally, downloading it if necessary.
   *
   * @param modelName The name of the model file
   * @return The path to the local model file
   * @throws IOException If there's an error downloading or accessing the model
   */
  public String ensureModelAvailable(String modelName) throws IOException {
    if (!MODEL_URLS.containsKey(modelName)) {
      throw new IllegalArgumentException("Unknown model: " + modelName);
    }

    // Ensure model directory exists
    File modelDir = new File(modelsDirectory);
    if (!modelDir.exists()) {
      modelDir.mkdirs();
    }

    File modelFile = Paths.get(modelsDirectory, modelName).toFile();

    // Check if the model is already downloaded
    if (modelFile.exists() && modelFile.length() > 0) {
      downloadedModels.put(modelName, true);
      return modelFile.getAbsolutePath();
    }

    // Check if the model is available as a resource
    String resourcePath = "models/opennlp/" + modelName;
    if (getClass().getClassLoader().getResource(resourcePath) != null) {
      // Extract resource to the models directory
      try (InputStream resourceStream =
          getClass().getClassLoader().getResourceAsStream(resourcePath)) {
        if (resourceStream != null) {
          extractResourceToFile(resourceStream, modelFile);
          downloadedModels.put(modelName, true);
          return modelFile.getAbsolutePath();
        }
      }
    }

    // Download the model if not available locally
    ReentrantLock lock = modelLocks.get(modelName);
    lock.lock();
    try {
      // Double-check after acquiring the lock
      if (modelFile.exists() && modelFile.length() > 0) {
        downloadedModels.put(modelName, true);
        return modelFile.getAbsolutePath();
      }

      // Download the model
      String modelUrl = MODEL_URLS.get(modelName);
      downloadModel(modelUrl, modelFile);
      downloadedModels.put(modelName, true);
      return modelFile.getAbsolutePath();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Gets the default models directory, creating it if necessary.
   *
   * @return The path to the default models directory
   */
  public static String getDefaultModelsDirectory() {
    // Check if the models directory exists in the working directory
    File localModelsDir = new File(DEFAULT_MODELS_DIR);
    if (localModelsDir.exists() && localModelsDir.isDirectory()) {
      return localModelsDir.getAbsolutePath();
    }

    // Fall back to user home directory
    Path userHome = Paths.get(System.getProperty("user.home"), ".anonymize", DEFAULT_MODELS_DIR);
    try {
      Files.createDirectories(userHome);
    } catch (IOException e) {
      logger.error("Failed to create models directory: {}", e.getMessage(), e);
    }
    return userHome.toString();
  }

  /** Ensures the models directory exists. */
  private void ensureModelsDirectoryExists() {
    File dir = new File(modelsDirectory);
    if (!dir.exists()) {
      try {
        Files.createDirectories(dir.toPath());
      } catch (IOException e) {
        logger.error("Failed to create models directory: {}", e.getMessage(), e);
        throw new RuntimeException("Cannot create models directory: " + e.getMessage(), e);
      }
    }
  }

  /**
   * Downloads a model from a URL to a local file.
   *
   * @param modelUrl The URL to download from
   * @param modelFile The local file to save to
   * @throws IOException If there's an error during download
   */
  private void downloadModel(String modelUrl, File modelFile) throws IOException {
    System.out.println("Downloading model from: " + modelUrl);
    logger.info("Downloading model from: {}", modelUrl);

    // Create parent directories if needed
    FileUtils.forceMkdirParent(modelFile);

    // Download with progress tracking
    URL url = new URL(modelUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestProperty("User-Agent", "Anonymize Library/1.0");

    try (ReadableByteChannel readableByteChannel =
            Channels.newChannel(connection.getInputStream());
        FileOutputStream fileOutputStream = new FileOutputStream(modelFile);
        FileChannel fileChannel = fileOutputStream.getChannel()) {

      long fileSize = connection.getContentLength();
      long transferredBytes = 0;
      long chunkSize = 1024 * 1024; // 1MB chunks

      System.out.println("Download started, file size: " + (fileSize / 1024) + " KB");

      while (transferredBytes < fileSize) {
        long bytesTransferred =
            fileChannel.transferFrom(readableByteChannel, transferredBytes, chunkSize);

        if (bytesTransferred <= 0) {
          break; // Exit if no more bytes are transferred
        }

        transferredBytes += bytesTransferred;

        // Log progress
        int progress = (int) (100 * transferredBytes / fileSize);
        if (progress % 20 == 0) {
          System.out.println("Download progress: " + progress + "%");
        }
      }
    } catch (Exception e) {
      System.err.println("Error downloading model: " + e.getMessage());
      throw e;
    }

    System.out.println("Model downloaded successfully: " + modelFile.getAbsolutePath());
    logger.info("Model downloaded successfully: {}", modelFile.getAbsolutePath());
  }

  /**
   * Extracts a resource to a local file.
   *
   * @param resourceStream The input stream for the resource
   * @param targetFile The file to write to
   * @throws IOException If there's an error during extraction
   */
  private void extractResourceToFile(InputStream resourceStream, File targetFile)
      throws IOException {
    // Create parent directories if needed
    FileUtils.forceMkdirParent(targetFile);

    // Copy the resource to the target file
    try (FileOutputStream fos = new FileOutputStream(targetFile)) {
      byte[] buffer = new byte[1024];
      int bytesRead;
      while ((bytesRead = resourceStream.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
      }
    }
  }

  /**
   * Checks if a model has been downloaded.
   *
   * @param modelName The name of the model
   * @return true if the model has been downloaded, false otherwise
   */
  public boolean isModelDownloaded(String modelName) {
    return downloadedModels.getOrDefault(modelName, false)
        || Paths.get(modelsDirectory, modelName).toFile().exists();
  }

  /**
   * Gets the path to a model.
   *
   * @param modelName The name of the model
   * @return The path to the model file
   */
  public String getModelPath(String modelName) {
    return Paths.get(modelsDirectory, modelName).toString();
  }
}
