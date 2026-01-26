package io.cyborgcode.roa.maven.plugins.allocator.config;

import java.io.File;
import java.util.Collections;
import java.util.Set;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestSplitterConfigurationTestng Tests")
class TestSplitterConfigurationTestngTest {

   @Mock
   private MavenProject mavenProject;

   @Mock
   private File testOutputDirectory;

   @Nested
   @DisplayName("Constructor and Getter Tests")
   class ConstructorAndGetterTests {

      @Test
      @DisplayName("Should create TestNG configuration with suites")
      void shouldCreateTestngConfigurationWithSuites() {
         // Arrange
         Set<String> suites = Set.of("smoke-suite", "integration-suite");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertNotNull(config, "Configuration should not be null");
         assertEquals(suites, config.getSuites(), "Suites should match");
         assertTrue(config.isEnabled(), "Should be enabled");
         assertEquals(20, config.getMaxMethodsPerBucket());
      }

      @Test
      @DisplayName("Should create TestNG configuration with empty suites")
      void shouldCreateTestngConfigurationWithEmptySuites() {
         // Arrange
         Set<String> suites = Collections.emptySet();

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertNotNull(config.getSuites(), "Suites should not be null");
         assertTrue(config.getSuites().isEmpty(), "Suites should be empty");
      }

      @Test
      @DisplayName("Should create TestNG configuration with single suite")
      void shouldCreateTestngConfigurationWithSingleSuite() {
         // Arrange
         Set<String> suites = Set.of("regression-suite");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertEquals(1, config.getSuites().size(), "Should have one suite");
         assertTrue(config.getSuites().contains("regression-suite"));
      }

      @Test
      @DisplayName("Should inherit base configuration properties")
      void shouldInheritBaseConfigurationProperties() {
         // Arrange
         Set<String> suites = Set.of("smoke-suite");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(50)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("testng-output")
               .projectRoot("/project/root")
               .parallelMethods(false)
               .maxNumberOfParallelRunners(10)
               .suites(suites)
               .build();

         // Assert
         assertTrue(config.isEnabled());
         assertEquals(50, config.getMaxMethodsPerBucket());
         assertSame(testOutputDirectory, config.getTestOutputDirectory());
         assertSame(mavenProject, config.getMavenProject());
         assertEquals("testng-output", config.getJsonOutputFile());
         assertEquals("/project/root", config.getProjectRoot());
         assertTrue(!config.isParallelMethods());
         assertEquals(10, config.getMaxNumberOfParallelRunners());
      }

      @Test
      @DisplayName("Should create TestNG configuration with multiple suites")
      void shouldCreateTestngConfigurationWithMultipleSuites() {
         // Arrange
         Set<String> suites = Set.of("smoke", "integration", "regression", "e2e", "performance");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertEquals(5, config.getSuites().size(), "Should have 5 suites");
         assertTrue(config.getSuites().contains("smoke"));
         assertTrue(config.getSuites().contains("integration"));
         assertTrue(config.getSuites().contains("regression"));
         assertTrue(config.getSuites().contains("e2e"));
         assertTrue(config.getSuites().contains("performance"));
      }

      @Test
      @DisplayName("Should preserve suite name case sensitivity")
      void shouldPreserveSuiteNameCaseSensitivity() {
         // Arrange
         Set<String> suites = Set.of("SmokeSuite", "INTEGRATION", "regression");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertTrue(config.getSuites().contains("SmokeSuite"), "Should preserve 'SmokeSuite'");
         assertTrue(config.getSuites().contains("INTEGRATION"), "Should preserve 'INTEGRATION'");
         assertTrue(config.getSuites().contains("regression"), "Should preserve 'regression'");
      }

      @Test
      @DisplayName("Should handle suite names with special characters")
      void shouldHandleSuiteNamesWithSpecialCharacters() {
         // Arrange
         Set<String> suites = Set.of("smoke-suite", "integration_suite", "suite.e2e");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertTrue(config.getSuites().contains("smoke-suite"));
         assertTrue(config.getSuites().contains("integration_suite"));
         assertTrue(config.getSuites().contains("suite.e2e"));
      }

      @Test
      @DisplayName("Should handle suite names with spaces")
      void shouldHandleSuiteNamesWithSpaces() {
         // Arrange
         Set<String> suites = Set.of("Smoke Suite", "Integration Test Suite");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertTrue(config.getSuites().contains("Smoke Suite"));
         assertTrue(config.getSuites().contains("Integration Test Suite"));
      }

      @Test
      @DisplayName("Should handle long suite names")
      void shouldHandleLongSuiteNames() {
         // Arrange
         String longSuiteName = "VeryLongSuiteNameForIntegrationTestingPurposesToVerifySystemBehavior";
         Set<String> suites = Set.of(longSuiteName);

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(true)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertTrue(config.getSuites().contains(longSuiteName), "Should handle long suite names");
      }

      @Test
      @DisplayName("Should create configuration with disabled state and suites")
      void shouldCreateConfigurationWithDisabledStateAndSuites() {
         // Arrange
         Set<String> suites = Set.of("smoke-suite");

         // Act
         TestSplitterConfigurationTestng config = TestSplitterConfigurationTestng.builder()
               .enabled(false)
               .maxMethodsPerBucket(20)
               .testOutputDirectory(testOutputDirectory)
               .mavenProject(mavenProject)
               .jsonOutputFile("output")
               .projectRoot("/root")
               .parallelMethods(true)
               .maxNumberOfParallelRunners(5)
               .suites(suites)
               .build();

         // Assert
         assertTrue(!config.isEnabled(), "Should be disabled");
         assertEquals(1, config.getSuites().size(), "Should still contain suites");
      }
   }
}