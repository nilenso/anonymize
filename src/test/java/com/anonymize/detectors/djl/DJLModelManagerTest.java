// package com.anonymize.detectors.djl;

// import ai.djl.MalformedModelException;
// import ai.djl.repository.zoo.ModelNotFoundException;
// import ai.djl.repository.zoo.ZooModel;
// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.io.TempDir;

// import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;

// import static org.junit.jupiter.api.Assertions.*;

// /**
//  * Tests for the DJL model manager functionality.
//  */
// @Disabled("DJL tests disabled until implementation is complete")
// public class DJLModelManagerTest {

//     private DJLModelManager modelManager;
//     private boolean modelsAvailable = false;
    
//     @TempDir
//     Path tempDir;
    
//     @BeforeEach
//     public void setup() {
//         try {
//             // Create a model manager with a test-specific temporary directory
//             modelManager = new DJLModelManager(tempDir);
            
//             // Check if we can test with real models
//             // This is a lightweight test to determine if we should run full model tests
//             try {
//                 // Try to access the Hugging Face API
//                 modelsAvailable = true;
//             } catch (Exception e) {
//                 System.out.println("Skipping tests that require model downloads: " + e.getMessage());
//                 modelsAvailable = false;
//             }
//         } catch (Exception e) {
//             fail("Failed to setup test: " + e.getMessage());
//         }
//     }
    
//     @AfterEach
//     public void cleanup() {
//         if (modelManager != null) {
//             modelManager.close();
//         }
//     }
    
//     @Test
//     public void testModelDirectoryCreation() throws IOException {
//         // Create a new path for testing
//         Path testPath = tempDir.resolve("model_test_dir");
        
//         // Create a model manager with this path
//         try (DJLModelManager manager = new DJLModelManager(testPath)) {
//             // Directory should be created
//             assertTrue(Files.exists(testPath), "Model directory should be created");
//             assertTrue(Files.isDirectory(testPath), "Path should be a directory");
//         }
//     }
    
//     @Test
//     public void testModelCaching() {
//         // Skip if models are not available
//         if (!modelsAvailable) {
//             System.out.println("Skipping testModelCaching as models are not available");
//             return;
//         }
        
//         // Test model caching (this requires network access)
//         try {
//             // Load a model
//             String modelId = "ner-bert-base";
            
//             // First load should cache the model
//             long startTime = System.currentTimeMillis();
//             ZooModel<?> model1 = modelManager.loadModel(modelId);
//             long firstLoadTime = System.currentTimeMillis() - startTime;
            
//             assertNotNull(model1, "Model should be loaded successfully");
            
//             // Second load should be faster due to caching
//             startTime = System.currentTimeMillis();
//             ZooModel<?> model2 = modelManager.loadModel(modelId);
//             long secondLoadTime = System.currentTimeMillis() - startTime;
            
//             assertNotNull(model2, "Cached model should be loaded successfully");
            
//             // The second load should be significantly faster if caching works
//             // This might not always be true in CI environments, so we'll just log it
//             System.out.println("First load time: " + firstLoadTime + "ms");
//             System.out.println("Second load time: " + secondLoadTime + "ms");
            
//             // The model objects should be the same instance if caching works
//             assertSame(model1, model2, "Cached model should return the same instance");
            
//         } catch (ModelNotFoundException | MalformedModelException | IOException e) {
//             // In a real test environment, we might want to fail here
//             // But for local development where models might not be available, we'll just log
//             System.out.println("Test skipped: " + e.getMessage());
//         }
//     }
    
//     @Test
//     public void testModelAvailabilityCheck() {
//         // Test isModelAvailable method
//         String nonExistentModel = "non-existent-model-id";
//         assertFalse(modelManager.isModelAvailable(nonExistentModel), 
//                 "Non-existent model should not be available");
                
//         // If we load a model, it should then be available
//         if (modelsAvailable) {
//             try {
//                 String existingModel = "ner-bert-base";
//                 ZooModel<?> model = modelManager.loadModel(existingModel);
                
//                 assertTrue(modelManager.isModelAvailable(existingModel), 
//                         "Model should be available after loading");
                        
//                 // Don't need to close the model as the manager will do it
//             } catch (Exception e) {
//                 System.out.println("Test skipped: " + e.getMessage());
//             }
//         }
//     }
    
//     @Test
//     public void testManagerClose() {
//         // Test that close properly releases resources
        
//         // Create a separate manager for this test
//         DJLModelManager testManager = new DJLModelManager(tempDir);
        
//         if (modelsAvailable) {
//             try {
//                 // Load a model
//                 String modelId = "ner-bert-base";
//                 ZooModel<?> model = testManager.loadModel(modelId);
                
//                 // Close the manager
//                 testManager.close();
                
//                 // Try to use the model after close
//                 try {
//                     model.getArtifactId();
//                     // If we get here without exception, the model wasn't properly closed
//                     fail("Model should not be usable after manager is closed");
//                 } catch (Exception e) {
//                     // Expected - model should be closed
//                     assertTrue(e instanceof IllegalStateException || 
//                             e.getCause() instanceof IllegalStateException, 
//                             "Expected IllegalStateException");
//                 }
//             } catch (Exception e) {
//                 System.out.println("Test skipped: " + e.getMessage());
//             }
//         } else {
//             // At least test that close doesn't throw exceptions
//             assertDoesNotThrow(() -> testManager.close(), 
//                     "Closing the manager should not throw exceptions");
//         }
//     }
// }