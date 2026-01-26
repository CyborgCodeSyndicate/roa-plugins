package io.cyborgcode.roa.maven.plugins.allocator.service;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.TestClassLoader;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseAllocatorService Tests")
class BaseAllocatorServiceTest {

   @Mock
   private Log log;

   @Mock
   private MavenProject mavenProject;

   @Mock
   private TestSplitterConfiguration config;

   private TestableAllocatorService service;

   @TempDir
   Path tempDir;

   @BeforeEach
   void setUp() throws Exception {
      service = new TestableAllocatorService(log);

      // Use lenient stubbing to avoid UnnecessaryStubbingException
      lenient().when(config.getMavenProject()).thenReturn(mavenProject);
      lenient().when(mavenProject.getTestClasspathElements()).thenReturn(Collections.emptyList());
      lenient().when(mavenProject.getCompileClasspathElements()).thenReturn(Collections.emptyList());
   }

   @AfterEach
   void tearDown() {
      // Clean up created files
   }

   @Nested
   @DisplayName("allocateTests Tests")
   class AllocateTestsTests {

      @Test
      @DisplayName("Should allocate tests and write output file")
      void shouldAllocateTestsAndWriteOutputFile() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         Path testClassFile = tempDir.resolve("test-classes/com/example/TestClass.class");
         Files.createDirectories(testClassFile.getParent());
         Files.createFile(testClassFile);

         String outputFile = tempDir.resolve("output").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(outputFile);
         // No getMaxMethodsPerBucket() stub needed: 2 classes <= 5 runners
         when(config.getMaxNumberOfParallelRunners()).thenReturn(5);

         service.setTestMethodCounts(Map.of("TestClass1", 10, "TestClass2", 15));

         doNothing().when(log).info(anyString());

         // Act
         service.allocateTests(config);

         // Assert
         verify(log).info(contains("Found"));
         verify(log).info(contains("Wrote"));

         File outputJsonFile = new File(outputFile + ".json");
         assertTrue(outputJsonFile.exists(), "Output JSON file should be created");
      }

      @Test
      @DisplayName("Should handle empty test directory")
      void shouldHandleEmptyTestDirectory() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("empty-test-classes").toFile();
         testOutputDir.mkdirs();

         String outputFile = tempDir.resolve("output-empty").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(outputFile);
         // No getMaxMethodsPerBucket() stub needed: 0 classes <= 5 runners
         when(config.getMaxNumberOfParallelRunners()).thenReturn(5);

         service.setTestMethodCounts(Collections.emptyMap());

         doNothing().when(log).info(anyString());

         // Act
         service.allocateTests(config);

         // Assert
         verify(log).info(contains("Found 0 class files"));
      }

      @Test
      @DisplayName("Should create one bucket per class when class count <= max runners")
      void shouldCreateOneBucketPerClassWhenClassCountLessThanMaxRunners() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         String outputFile = tempDir.resolve("output-small").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(outputFile);
         // No getMaxMethodsPerBucket() stub needed: 3 classes <= 10 runners
         when(config.getMaxNumberOfParallelRunners()).thenReturn(10);

         service.setTestMethodCounts(Map.of(
               "TestClass1", 5,
               "TestClass2", 8,
               "TestClass3", 3
         ));

         doNothing().when(log).info(anyString());

         // Act
         service.allocateTests(config);

         // Assert
         File outputJsonFile = new File(outputFile + ".json");
         assertTrue(outputJsonFile.exists(), "Output JSON file should be created");

         String jsonContent = Files.readString(outputJsonFile.toPath());
         assertNotNull(jsonContent);
         assertTrue(jsonContent.contains("jobIndex"), "JSON should contain jobIndex");
         assertTrue(jsonContent.contains("classes"), "JSON should contain classes");
         assertTrue(jsonContent.contains("totalMethods"), "JSON should contain totalMethods");
      }

      @Test
      @DisplayName("Should group classes when class count > max runners")
      void shouldGroupClassesWhenClassCountGreaterThanMaxRunners() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         String outputFile = tempDir.resolve("output-large").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(outputFile);
         // getMaxMethodsPerBucket() IS needed: 5 classes > 2 runners triggers grouping logic
         when(config.getMaxMethodsPerBucket()).thenReturn(20);
         when(config.getMaxNumberOfParallelRunners()).thenReturn(2);

         service.setTestMethodCounts(Map.of(
               "TestClass1", 5,
               "TestClass2", 8,
               "TestClass3", 3,
               "TestClass4", 6,
               "TestClass5", 4
         ));

         doNothing().when(log).info(anyString());

         // Act
         service.allocateTests(config);

         // Assert
         File outputJsonFile = new File(outputFile + ".json");
         assertTrue(outputJsonFile.exists(), "Output JSON file should be created");
      }

      @Test
      @DisplayName("Should throw MojoExecutionException when file write fails")
      void shouldThrowMojoExecutionExceptionWhenFileWriteFails() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         Path blockingFile = tempDir.resolve("output");
         Files.createFile(blockingFile);
         
         String invalidPath = tempDir.resolve("output/nested/file").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(invalidPath);
         when(config.getMaxNumberOfParallelRunners()).thenReturn(5);

         service.setTestMethodCounts(Map.of("TestClass1", 10));

         doNothing().when(log).info(anyString());

         // Act & Assert
         assertThrows(MojoExecutionException.class, () ->
               service.allocateTests(config),
               "Should throw MojoExecutionException when file write fails"
         );
      }

      @Test
      @DisplayName("Should log class method count size")
      void shouldLogClassMethodCountSize() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         String outputFile = tempDir.resolve("output-log").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(outputFile);
         // No getMaxMethodsPerBucket() stub needed: 3 classes <= 5 runners
         when(config.getMaxNumberOfParallelRunners()).thenReturn(5);

         service.setTestMethodCounts(Map.of(
               "TestClass1", 10,
               "TestClass2", 15,
               "TestClass3", 8
         ));

         doNothing().when(log).info(anyString());

         // Act
         service.allocateTests(config);

         // Assert
         verify(log).info(contains("classMethodCount size=3"));
      }

      @Test
      @DisplayName("Should write correct JSON structure")
      void shouldWriteCorrectJsonStructure() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         String outputFile = tempDir.resolve("output-structure").toString();

         when(config.getTestOutputDirectory()).thenReturn(testOutputDir);
         when(config.getJsonOutputFile()).thenReturn(outputFile);
         // No getMaxMethodsPerBucket() stub needed: 1 class <= 5 runners
         when(config.getMaxNumberOfParallelRunners()).thenReturn(5);

         service.setTestMethodCounts(Map.of("TestClass1", 10));

         doNothing().when(log).info(anyString());

         // Act
         service.allocateTests(config);

         // Assert
         File outputJsonFile = new File(outputFile + ".json");
         String jsonContent = Files.readString(outputJsonFile.toPath());

         assertTrue(jsonContent.contains("\"jobIndex\":0"), "Should contain jobIndex");
         assertTrue(jsonContent.contains("\"classes\":"), "Should contain classes array");
         assertTrue(jsonContent.contains("\"totalMethods\":10"), "Should contain totalMethods");
         assertTrue(jsonContent.contains("TestClass1"), "Should contain class name");
      }
   }

   // ===== Test Implementation =====

   /**
    * Testable concrete implementation of BaseAllocatorService
    */
   static class TestableAllocatorService extends BaseAllocatorService {
      private Map<String, Integer> testMethodCounts = Collections.emptyMap();

      public TestableAllocatorService(Log log) {
         super(log);
      }

      public void setTestMethodCounts(Map<String, Integer> counts) {
         this.testMethodCounts = counts;
      }

      @Override
      public Map<String, Integer> calculateClassMethodCounts(
            List<File> classFiles,
            TestClassLoader testClassLoader,
            TestSplitterConfiguration config) {
         return testMethodCounts;
      }
   }
}
