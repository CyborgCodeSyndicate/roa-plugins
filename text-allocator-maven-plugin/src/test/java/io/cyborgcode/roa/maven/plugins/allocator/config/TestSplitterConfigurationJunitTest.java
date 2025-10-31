package io.cyborgcode.roa.maven.plugins.allocator.config;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestSplitterConfigurationJunit Tests")
class TestSplitterConfigurationJunitTest {

   @Mock
   private MavenProject mavenProject;

   @Mock
   private File testOutputDirectory;

   @Nested
   @DisplayName("Constructor and Getter Tests")
   class ConstructorAndGetterTests {

      @Test
      @DisplayName("Should create JUnit configuration with include and exclude tags")
      void shouldCreateJunitConfigurationWithIncludeAndExcludeTags() {
         // Arrange
         Set<String> includeTags = Set.of("smoke", "integration");
         Set<String> excludeTags = Set.of("slow", "flaky");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertNotNull(config, "Configuration should not be null");
         assertEquals(includeTags, config.getIncludeTags(), "Include tags should match");
         assertEquals(excludeTags, config.getExcludeTags(), "Exclude tags should match");
         assertTrue(config.isEnabled(), "Should be enabled");
         assertEquals(20, config.getMaxMethodsPerBucket());
      }

      @Test
      @DisplayName("Should create JUnit configuration with empty include tags")
      void shouldCreateJunitConfigurationWithEmptyIncludeTags() {
         // Arrange
         Set<String> includeTags = Collections.emptySet();
         Set<String> excludeTags = Set.of("slow");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertNotNull(config.getIncludeTags(), "Include tags should not be null");
         assertTrue(config.getIncludeTags().isEmpty(), "Include tags should be empty");
         assertEquals(1, config.getExcludeTags().size(), "Should have one exclude tag");
      }

      @Test
      @DisplayName("Should create JUnit configuration with empty exclude tags")
      void shouldCreateJunitConfigurationWithEmptyExcludeTags() {
         // Arrange
         Set<String> includeTags = Set.of("smoke");
         Set<String> excludeTags = Collections.emptySet();

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertNotNull(config.getExcludeTags(), "Exclude tags should not be null");
         assertTrue(config.getExcludeTags().isEmpty(), "Exclude tags should be empty");
         assertEquals(1, config.getIncludeTags().size(), "Should have one include tag");
      }

      @Test
      @DisplayName("Should create JUnit configuration with both tags empty")
      void shouldCreateJunitConfigurationWithBothTagsEmpty() {
         // Arrange
         Set<String> includeTags = Collections.emptySet();
         Set<String> excludeTags = Collections.emptySet();

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertTrue(config.getIncludeTags().isEmpty(), "Include tags should be empty");
         assertTrue(config.getExcludeTags().isEmpty(), "Exclude tags should be empty");
      }

      @Test
      @DisplayName("Should inherit base configuration properties")
      void shouldInheritBaseConfigurationProperties() {
         // Arrange
         Set<String> includeTags = Set.of("smoke");
         Set<String> excludeTags = Set.of("slow");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               50,
               testOutputDirectory,
               mavenProject,
               "junit-output",
               "/project/root",
               false,
               10,
               includeTags,
               excludeTags
         );

         // Assert
         assertTrue(config.isEnabled());
         assertEquals(50, config.getMaxMethodsPerBucket());
         assertSame(testOutputDirectory, config.getTestOutputDirectory());
         assertSame(mavenProject, config.getMavenProject());
         assertEquals("junit-output", config.getJsonOutputFile());
         assertEquals("/project/root", config.getProjectRoot());
         assertTrue(!config.isParallelMethods());
         assertEquals(10, config.getMaxNumberOfParallelRunners());
      }

      @Test
      @DisplayName("Should create JUnit configuration with multiple include tags")
      void shouldCreateJunitConfigurationWithMultipleIncludeTags() {
         // Arrange
         Set<String> includeTags = Set.of("smoke", "integration", "regression", "e2e");
         Set<String> excludeTags = Set.of("slow");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertEquals(4, config.getIncludeTags().size(), "Should have 4 include tags");
         assertTrue(config.getIncludeTags().contains("smoke"));
         assertTrue(config.getIncludeTags().contains("integration"));
         assertTrue(config.getIncludeTags().contains("regression"));
         assertTrue(config.getIncludeTags().contains("e2e"));
      }

      @Test
      @DisplayName("Should create JUnit configuration with multiple exclude tags")
      void shouldCreateJunitConfigurationWithMultipleExcludeTags() {
         // Arrange
         Set<String> includeTags = Set.of("smoke");
         Set<String> excludeTags = Set.of("slow", "flaky", "manual", "wip");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertEquals(4, config.getExcludeTags().size(), "Should have 4 exclude tags");
         assertTrue(config.getExcludeTags().contains("slow"));
         assertTrue(config.getExcludeTags().contains("flaky"));
         assertTrue(config.getExcludeTags().contains("manual"));
         assertTrue(config.getExcludeTags().contains("wip"));
      }

      @Test
      @DisplayName("Should preserve tag case sensitivity")
      void shouldPreserveTagCaseSensitivity() {
         // Arrange
         Set<String> includeTags = Set.of("Smoke", "INTEGRATION", "regression");
         Set<String> excludeTags = Set.of("SLOW", "Flaky");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertTrue(config.getIncludeTags().contains("Smoke"), "Should preserve 'Smoke'");
         assertTrue(config.getIncludeTags().contains("INTEGRATION"), "Should preserve 'INTEGRATION'");
         assertTrue(config.getExcludeTags().contains("SLOW"), "Should preserve 'SLOW'");
         assertTrue(config.getExcludeTags().contains("Flaky"), "Should preserve 'Flaky'");
      }

      @Test
      @DisplayName("Should handle tags with special characters")
      void shouldHandleTagsWithSpecialCharacters() {
         // Arrange
         Set<String> includeTags = Set.of("smoke-test", "integration_test", "test.e2e");
         Set<String> excludeTags = Set.of("slow-running", "known_issue");

         // Act
         TestSplitterConfigurationJunit config = new TestSplitterConfigurationJunit(
               true,
               20,
               testOutputDirectory,
               mavenProject,
               "output",
               "/root",
               true,
               5,
               includeTags,
               excludeTags
         );

         // Assert
         assertTrue(config.getIncludeTags().contains("smoke-test"));
         assertTrue(config.getIncludeTags().contains("integration_test"));
         assertTrue(config.getIncludeTags().contains("test.e2e"));
         assertTrue(config.getExcludeTags().contains("slow-running"));
         assertTrue(config.getExcludeTags().contains("known_issue"));
      }
   }
}
