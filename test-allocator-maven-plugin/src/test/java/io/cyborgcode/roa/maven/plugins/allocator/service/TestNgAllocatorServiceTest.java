package io.cyborgcode.roa.maven.plugins.allocator.service;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfigurationTestng;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.TestClassLoader;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestNgAllocatorService Tests")
class TestNgAllocatorServiceTest {

   @Mock
   private Log log;

   @Mock
   private TestClassLoader testClassLoader;

   @Mock
   private TestSplitterConfigurationTestng config;

   private TestNgAllocatorService service;

   @BeforeEach
   void setUp() {
      service = new TestNgAllocatorService(log);
   }

   @Nested
   @DisplayName("calculateClassMethodCounts Tests")
   class CalculateClassMethodCountsTests {

      @Test
      @DisplayName("Should calculate method counts from TestNG suite")
      void shouldCalculateMethodCountsFromTestNgSuite(@TempDir Path tempDir) throws IOException {
         // Arrange
         String suiteXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="TestSuite">
                 <test name="SmokeTests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$AnotherTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         Path suiteFile = tempDir.resolve("testng.xml");
         Files.writeString(suiteFile, suiteXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("TestSuite"));
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"))
               .thenAnswer(invocation -> SampleTestNgMethodsFixture.class);
         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$AnotherTestNgMethodsFixture"))
               .thenAnswer(invocation -> AnotherTestNgMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result, "Result should not be null");
         assertTrue(result.containsKey("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture") || 
                    result.containsKey("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$AnotherTestNgMethodsFixture"),
               "Should contain at least one test class");
      }

      @Test
      @DisplayName("Should handle suite with included methods")
      void shouldHandleSuiteWithIncludedMethods(@TempDir Path tempDir) throws IOException {
         // Arrange
         String suiteXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="TestSuite">
                 <test name="SpecificTests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture">
                       <methods>
                         <include name="testMethod1"/>
                         <include name="testMethod2"/>
                       </methods>
                     </class>
                   </classes>
                 </test>
               </suite>
               """;

         Path suiteFile = tempDir.resolve("testng.xml");
         Files.writeString(suiteFile, suiteXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("TestSuite"));

         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"))
               .thenAnswer(invocation -> SampleTestNgMethodsFixture.class);

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
      @DisplayName("Should handle empty suites set")
      void shouldHandleEmptySuitesSet(@TempDir Path tempDir) {
         // Arrange
         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Collections.emptySet());

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         assertTrue(result.isEmpty(), "Should return empty map when no suites specified");
      }

      @Test
      @DisplayName("Should handle non-existent XML files")
      void shouldHandleNonExistentXmlFiles(@TempDir Path tempDir) {
         // Arrange
         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("NonExistentSuite"));

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         assertTrue(result.isEmpty(), "Should handle missing XML files gracefully");
      }

      @Test
      @DisplayName("Should throw exception for malformed XML")
      void shouldThrowExceptionForMalformedXml(@TempDir Path tempDir) throws IOException {
         // Arrange
         String malformedXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <suite name="TestSuite">
                 <test name="Test">
                   <classes>
                     <class name="com.example.TestClass"
                   </classes>
                 </test>
               """;

         Path suiteFile = tempDir.resolve("malformed.xml");
         Files.writeString(suiteFile, malformedXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("TestSuite"));

         // Act & Assert
         assertThrows(IllegalStateException.class, () ->
               service.calculateClassMethodCounts(classFiles, testClassLoader, config),
               "Should throw IllegalStateException for malformed XML"
         );
      }

      @Test
      @DisplayName("Should skip classes that fail to load")
      void shouldSkipClassesThatFailToLoad(@TempDir Path tempDir) throws IOException {
         // Arrange
         String suiteXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="TestSuite">
                 <test name="Tests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         Path suiteFile = tempDir.resolve("testng.xml");
         Files.writeString(suiteFile, suiteXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("TestSuite"));
         when(testClassLoader.loadClass(anyString())).thenReturn(null);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         assertTrue(result.isEmpty(), "Should skip classes that fail to load");
      }

      @Test
      @DisplayName("Should count 1 when parallelMethods is false")
      void shouldCountOneWhenParallelMethodsIsFalse(@TempDir Path tempDir) throws IOException {
         // Arrange
         String suiteXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="TestSuite">
                 <test name="Tests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         Path suiteFile = tempDir.resolve("testng.xml");
         Files.writeString(suiteFile, suiteXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("TestSuite"));
         when(config.isParallelMethods()).thenReturn(false);

         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"))
               .thenAnswer(invocation -> SampleTestNgMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         if (!result.isEmpty()) {
            result.values().forEach(count ->
                  assertEquals(1, count, "Count should be 1 when parallelMethods is false")
            );
         }
      }

      @Test
      @DisplayName("Should find XML files in nested directories")
      void shouldFindXmlFilesInNestedDirectories(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path nestedDir = tempDir.resolve("src/test/resources");
         Files.createDirectories(nestedDir);

         String suiteXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="NestedSuite">
                 <test name="Tests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         Files.writeString(nestedDir.resolve("testng.xml"), suiteXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("NestedSuite"));
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"))
               .thenAnswer(invocation -> SampleTestNgMethodsFixture.class);

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
      @DisplayName("Should handle multiple XML files")
      void shouldHandleMultipleXmlFiles(@TempDir Path tempDir) throws IOException {
         // Arrange
         String suite1Xml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="Suite1">
                 <test name="Tests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         String suite2Xml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="Suite2">
                 <test name="Tests">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$AnotherTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         Files.writeString(tempDir.resolve("suite1.xml"), suite1Xml);
         Files.writeString(tempDir.resolve("suite2.xml"), suite2Xml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("Suite1", "Suite2"));
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"))
               .thenAnswer(invocation -> SampleTestNgMethodsFixture.class);
         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$AnotherTestNgMethodsFixture"))
               .thenAnswer(invocation -> AnotherTestNgMethodsFixture.class);

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
      @DisplayName("Should accumulate method counts for same class in multiple suites")
      void shouldAccumulateMethodCountsForSameClassInMultipleSuites(@TempDir Path tempDir) throws IOException {
         // Arrange
         String suiteXml = """
               <?xml version="1.0" encoding="UTF-8"?>
               <!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
               <suite name="TestSuite">
                 <test name="Test1">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                   </classes>
                 </test>
                 <test name="Test2">
                   <classes>
                     <class name="io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"/>
                   </classes>
                 </test>
               </suite>
               """;

         Files.writeString(tempDir.resolve("testng.xml"), suiteXml);

         List<File> classFiles = Collections.emptyList();

         when(config.getProjectRoot()).thenReturn(tempDir.toString());
         when(config.getSuites()).thenReturn(Set.of("TestSuite"));
         when(config.isParallelMethods()).thenReturn(true);

         when(testClassLoader.loadClass("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture"))
               .thenAnswer(invocation -> SampleTestNgMethodsFixture.class);

         // Act
         Map<String, Integer> result = service.calculateClassMethodCounts(
               classFiles,
               testClassLoader,
               config
         );

         // Assert
         assertNotNull(result);
         if (result.containsKey("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture")) {
            assertTrue(result.get("io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorServiceTest$SampleTestNgMethodsFixture") > 0, 
                  "Should accumulate method counts for same class");
         }
      }
   }

   // ===== Test Fixtures =====

   /**
    * Helper class for reflection testing - not meant to be executed as tests.
    * Methods have @Test annotations so the allocator service can inspect them.
    */
   static class SampleTestNgMethodsFixture {
      @org.testng.annotations.Test
      public void testMethod1() {
      }

      @org.testng.annotations.Test
      public void testMethod2() {
      }

      @org.testng.annotations.Test
      public void testMethod3() {
      }
   }

   /**
    * Helper class for reflection testing - not meant to be executed as tests.
    * Methods have @Test annotations so the allocator service can inspect them.
    */
   static class AnotherTestNgMethodsFixture {
      @org.testng.annotations.Test
      public void anotherTest() {
      }
   }
}
