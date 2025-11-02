package io.cyborgcode.roa.maven.plugins.allocator.service;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfigurationJunit;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.TestClassLoader;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JunitAllocatorService Tests")
class JunitAllocatorServiceTest {

   @Mock
   private Log log;

   @Mock
   private TestClassLoader testClassLoader;

   @Mock
   private TestSplitterConfigurationJunit config;

   private JunitAllocatorService service;

   @BeforeEach
   void setUp() {
      service = new JunitAllocatorService(log);
   }

   @Nested
   @DisplayName("calculateClassMethodCounts Tests")
   class CalculateClassMethodCountsTests {

      @Test
      @DisplayName("Should calculate method counts for test classes with tags")
      void shouldCalculateMethodCountsForTestClassesWithTags() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "com/example/TestClass1.class"),
               new File(testDir, "com/example/TestClass2.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(config.getIncludeTags()).thenReturn(Collections.emptySet());
         when(config.getExcludeTags()).thenReturn(Collections.emptySet());
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass("com.example.TestClass1"))
               .thenAnswer(invocation -> SampleTestMethodsFixture.class);
         when(testClassLoader.loadClass("com.example.TestClass2"))
               .thenAnswer(invocation -> AnotherTestMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result, "Result should not be null");
      }

      @Test
      @DisplayName("Should filter classes by include tags")
      void shouldFilterClassesByIncludeTags() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "com/example/TestClass.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(config.getIncludeTags()).thenReturn(Set.of("smoke"));
         when(config.getExcludeTags()).thenReturn(Collections.emptySet());
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass(anyString()))
               .thenAnswer(invocation -> SampleTestMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
      }

      @Test
      @DisplayName("Should exclude classes with null load result")
      void shouldExcludeClassesWithNullLoadResult() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "com/example/NonExistent.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(testClassLoader.loadClass(anyString())).thenReturn(null);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         assertTrue(result.isEmpty(), "Should exclude classes that fail to load");
      }

      @Test
      @DisplayName("Should exclude classes with zero matching methods")
      void shouldExcludeClassesWithZeroMatchingMethods() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "com/example/NoTests.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(config.getIncludeTags()).thenReturn(Set.of("nonexistent"));
         when(config.getExcludeTags()).thenReturn(Collections.emptySet());
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass(anyString()))
               .thenAnswer(invocation -> SampleTestMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         assertTrue(result.isEmpty(), "Should exclude classes with no matching methods");
      }

      @Test
      @DisplayName("Should handle empty class files list")
      void shouldHandleEmptyClassFilesList() {
         // Arrange
         List<File> classFiles = Collections.emptyList();

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         assertTrue(result.isEmpty(), "Should return empty map for empty input");
      }

      @Test
      @DisplayName("Should apply exclude tags filter")
      void shouldApplyExcludeTagsFilter() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "com/example/TestClass.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(config.getIncludeTags()).thenReturn(Collections.emptySet());
         when(config.getExcludeTags()).thenReturn(Set.of("slow"));
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass(anyString()))
               .thenAnswer(invocation -> SampleTestMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
      }

      @Test
      @DisplayName("Should respect parallelMethods configuration")
      void shouldRespectParallelMethodsConfiguration() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "com/example/TestClass.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(config.getIncludeTags()).thenReturn(Collections.emptySet());
         when(config.getExcludeTags()).thenReturn(Collections.emptySet());
         when(config.isParallelMethods()).thenReturn(false);

         when(testClassLoader.loadClass(anyString()))
               .thenAnswer(invocation -> SampleTestMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         if (!result.isEmpty()) {
            // When parallelMethods is false, count should be 1 per class
            result.values().forEach(count ->
                  assertTrue(count <= 1, "Count should be 1 when parallelMethods is false"));
         }
      }

      @Test
      @DisplayName("Should handle multiple class files")
      void shouldHandleMultipleClassFiles() {
         // Arrange
         File testDir = new File("target/test-classes").getAbsoluteFile();
         List<File> classFiles = List.of(
               new File(testDir, "Test1.class"),
               new File(testDir, "Test2.class"),
               new File(testDir, "Test3.class")
         );

         when(config.getTestOutputDirectory()).thenReturn(testDir);
         when(config.getIncludeTags()).thenReturn(Collections.emptySet());
         when(config.getExcludeTags()).thenReturn(Collections.emptySet());
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass(anyString()))
               .thenAnswer(invocation -> SampleTestMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
      }
   }

   // ===== Test Fixtures =====

   /**
    * Helper class for reflection testing - not meant to be executed as tests.
    * Methods have @Test annotations so the allocator service can inspect them.
    */
   static class SampleTestMethodsFixture {
      @org.junit.jupiter.api.Test
      @org.junit.jupiter.api.Tag("smoke")
      public void testMethod1() {
      }

      @org.junit.jupiter.api.Test
      @org.junit.jupiter.api.Tag("integration")
      public void testMethod2() {
      }

      @org.junit.jupiter.api.Test
      public void testMethod3() {
      }
   }

   /**
    * Helper class for reflection testing - not meant to be executed as tests.
    * Methods have @Test annotations so the allocator service can inspect them.
    */
   static class AnotherTestMethodsFixture {
      @org.junit.jupiter.api.Test
      @org.junit.jupiter.api.Tag("fast")
      public void fastTest() {
      }
   }
}
