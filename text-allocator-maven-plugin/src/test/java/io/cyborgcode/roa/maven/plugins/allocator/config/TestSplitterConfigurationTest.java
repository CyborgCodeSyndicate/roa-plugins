package io.cyborgcode.roa.maven.plugins.allocator.config;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestSplitterConfiguration Tests")
class TestSplitterConfigurationTest {

   @Mock
   private MavenProject mavenProject;

   @Mock
   private File testOutputDirectory;

   @Nested
   @DisplayName("Constructor and Getter Tests")
   class ConstructorAndGetterTests {

      @Test
      @DisplayName("Should create configuration with all parameters")
      void shouldCreateConfigurationWithAllParameters() {
         // Arrange
         boolean enabled = true;
         int maxMethodsPerBucket = 50;
         String jsonOutputFile = "test-output";
         String projectRoot = "/project/root";
         boolean parallelMethods = true;
         int maxNumberOfParallelRunners = 10;

         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               enabled,
               maxMethodsPerBucket,
               testOutputDirectory,
               mavenProject,
               jsonOutputFile,
               projectRoot,
               parallelMethods,
               maxNumberOfParallelRunners
         );

         // Assert
         assertNotNull(config, "Configuration should not be null");
         assertTrue(config.isEnabled(), "Enabled should be true");
         assertEquals(50, config.getMaxMethodsPerBucket(), "Max methods per bucket should be 50");
         assertSame(testOutputDirectory, config.getTestOutputDirectory(), "Test output directory should match");
         assertSame(mavenProject, config.getMavenProject(), "Maven project should match");
         assertEquals("test-output", config.getJsonOutputFile(), "JSON output file should match");
         assertEquals("/project/root", config.getProjectRoot(), "Project root should match");
         assertTrue(config.isParallelMethods(), "Parallel methods should be true");
         assertEquals(10, config.getMaxNumberOfParallelRunners(), "Max parallel runners should be 10");
      }

      @Test
      @DisplayName("Should create configuration with disabled state")
      void shouldCreateConfigurationWithDisabledState() {
         // Arrange
         boolean enabled = false;

         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               enabled,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               false,
               5
         );

         // Assert
         assertFalse(config.isEnabled(), "Enabled should be false");
      }

      @Test
      @DisplayName("Should create configuration with parallelMethods disabled")
      void shouldCreateConfigurationWithParallelMethodsDisabled() {
         // Arrange
         boolean parallelMethods = false;

         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               parallelMethods,
               5
         );

         // Assert
         assertFalse(config.isParallelMethods(), "Parallel methods should be false");
      }

      @Test
      @DisplayName("Should create configuration with minimum values")
      void shouldCreateConfigurationWithMinimumValues() {
         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               false,
               1,
               testOutputDirectory,
               mavenProject,
               "min",
               "/",
               false,
               1
         );

         // Assert
         assertEquals(1, config.getMaxMethodsPerBucket(), "Should accept minimum bucket size");
         assertEquals(1, config.getMaxNumberOfParallelRunners(), "Should accept minimum runners");
      }

      @Test
      @DisplayName("Should create configuration with large values")
      void shouldCreateConfigurationWithLargeValues() {
         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               10000,
               testOutputDirectory,
               mavenProject,
               "large-output",
               "/very/long/project/root/path",
               true,
               1000
         );

         // Assert
         assertEquals(10000, config.getMaxMethodsPerBucket(), "Should accept large bucket size");
         assertEquals(1000, config.getMaxNumberOfParallelRunners(), "Should accept large runner count");
      }

      @Test
      @DisplayName("Should preserve exact project root path")
      void shouldPreserveExactProjectRootPath() {
         // Arrange
         String projectRoot = "/home/user/projects/my-project";

         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               projectRoot,
               true,
               5
         );

         // Assert
         assertEquals(projectRoot, config.getProjectRoot(), "Project root should be preserved exactly");
      }

      @Test
      @DisplayName("Should preserve exact JSON output file name")
      void shouldPreserveExactJsonOutputFileName() {
         // Arrange
         String jsonOutputFile = "custom-test-results";

         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               jsonOutputFile,
               "/root",
               true,
               5
         );

         // Assert
         assertEquals(jsonOutputFile, config.getJsonOutputFile(), "JSON output file should be preserved exactly");
      }

      @Test
      @DisplayName("Should handle Windows-style paths")
      void shouldHandleWindowsStylePaths() {
         // Arrange
         String projectRoot = "C:\\Users\\Developer\\Projects\\MyProject";

         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               projectRoot,
               true,
               5
         );

         // Assert
         assertEquals(projectRoot, config.getProjectRoot(), "Should handle Windows paths");
      }

      @Test
      @DisplayName("Should create configuration with zero max methods per bucket")
      void shouldCreateConfigurationWithZeroMaxMethodsPerBucket() {
         // Act
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               0,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5
         );

         // Assert
         assertEquals(0, config.getMaxMethodsPerBucket(), "Should accept zero max methods");
      }

      @Test
      @DisplayName("Should allow modification of maxNumberOfParallelRunners")
      void shouldAllowModificationOfMaxNumberOfParallelRunners() {
         // Arrange
         TestSplitterConfiguration config = new TestSplitterConfiguration(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5
         );

         // Act
         config.setMaxNumberOfParallelRunners(15);

         // Assert
         assertEquals(15, config.getMaxNumberOfParallelRunners(), 
               "Should update maxNumberOfParallelRunners");
      }
   }
}
