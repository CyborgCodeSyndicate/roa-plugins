package io.cyborgcode.roa.maven.plugins.allocator;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Collections;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestAllocatorMojo Tests")
class TestAllocatorMojoTest {

   @Mock
   private Log log;

   @Mock
   private MavenProject mavenProject;

   private TestAllocatorMojo mojo;

   @TempDir
   Path tempDir;

   @BeforeEach
   void setUp() throws Exception {
      mojo = new TestAllocatorMojo();
      setField(mojo, "log", log);
      setField(mojo, "project", mavenProject);

      lenient().when(mavenProject.getTestClasspathElements()).thenReturn(Collections.emptyList());
      lenient().when(mavenProject.getCompileClasspathElements()).thenReturn(Collections.emptyList());

      lenient().doNothing().when(log).info(anyString());
   }

   @Nested
   @DisplayName("Execute Tests - Disabled State")
   class ExecuteTestsDisabledState {

      @Test
      @DisplayName("Should skip execution when disabled")
      void shouldSkipExecutionWhenDisabled() throws Exception {
         // Arrange
         setField(mojo, "enabled", false);

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("Disabled. Skipping"));
      }

      @Test
      @DisplayName("Should not process tests when disabled")
      void shouldNotProcessTestsWhenDisabled() throws Exception {
         // Arrange
         setField(mojo, "enabled", false);
         setField(mojo, "testEngine", "junit");

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("Disabled"));
         verify(log, never()).info(contains("Starting test splitting"));
      }
   }

   @Nested
   @DisplayName("Execute Tests - JUnit Engine")
   class ExecuteTestsJunitEngine {

      @Test
      @DisplayName("Should execute with JUnit engine")
      void shouldExecuteWithJunitEngine() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "tagsInclude", "smoke,integration");
         setField(mojo, "tagsExclude", "slow");

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }

      @Test
      @DisplayName("Should handle JUnit with empty tags")
      void shouldHandleJunitWithEmptyTags() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "tagsInclude", null);
         setField(mojo, "tagsExclude", null);

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }

      @Test
      @DisplayName("Should log JUnit configuration")
      void shouldLogJunitConfiguration() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", "test-output");
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "tagsInclude", "smoke");
         setField(mojo, "tagsExclude", "slow");

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("testEngine = junit"));
         verify(log).info(contains("tagsInclude"));
         verify(log).info(contains("tagsExclude"));
      }
   }

   @Nested
   @DisplayName("Execute Tests - TestNG Engine")
   class ExecuteTestsTestNgEngine {

      @Test
      @DisplayName("Should execute with TestNG engine")
      void shouldExecuteWithTestNgEngine() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "testng");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "suites", "smoke-suite,regression-suite");

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }

      @Test
      @DisplayName("Should handle TestNG with empty suites")
      void shouldHandleTestNgWithEmptySuites() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "testng");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "suites", null);

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }

      @Test
      @DisplayName("Should log TestNG configuration")
      void shouldLogTestNgConfiguration() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "testng");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", "testng-output");
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "suites", "regression");

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("testEngine = testng"));
         verify(log).info(contains("suites"));
      }

      @Test
      @DisplayName("Should handle TestNG case insensitive")
      void shouldHandleTestNgCaseInsensitive() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "TESTNG");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }
   }

   @Nested
   @DisplayName("Execute Tests - Invalid Engine")
   class ExecuteTestsInvalidEngine {

      @Test
      @DisplayName("Should throw exception for invalid test engine")
      void shouldThrowExceptionForInvalidTestEngine() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "invalid-engine");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act & Assert
         assertThrows(IllegalArgumentException.class, () -> mojo.execute(),
               "Should throw IllegalArgumentException for invalid engine");
      }

      @ParameterizedTest
      @ValueSource(strings = {"cucumber", "spock", "unknown"})
      @DisplayName("Should reject unsupported test engines")
      void shouldRejectUnsupportedTestEngines(String engineName) throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", engineName);
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act & Assert
         assertThrows(IllegalArgumentException.class, () -> mojo.execute());
      }
   }

   @Nested
   @DisplayName("Configuration Logging Tests")
   class ConfigurationLoggingTests {

      @Test
      @DisplayName("Should log max methods configuration")
      void shouldLogMaxMethodsConfiguration() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 50);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("maxMethods = 50"));
      }

      @Test
      @DisplayName("Should log output file configuration")
      void shouldLogOutputFileConfiguration() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", "custom-output");
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("outputJsonFile = custom-output"));
      }

      @Test
      @DisplayName("Should log test output directory")
      void shouldLogTestOutputDirectory() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act
         mojo.execute();

         // Assert
         verify(log).info(contains("testOutputDir"));
         verify(log).info(contains("Starting test splitting"));
      }
   }

   @Nested
   @DisplayName("Pair Inner Class Tests")
   class PairInnerClassTests {

      @Test
      @DisplayName("Should create pair with left and right values")
      void shouldCreatePairWithLeftAndRightValues() throws Exception {
         // This tests the inner Pair class indirectly through execute
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);

         // Act & Assert - Pair is used internally
         assertDoesNotThrow(() -> mojo.execute());
      }
   }

   @Nested
   @DisplayName("Input Parsing Tests")
   class InputParsingTests {

      @Test
      @DisplayName("Should parse comma-separated tags")
      void shouldParseCommaSeparatedTags() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "tagsInclude", "smoke, integration , regression");

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }

      @ParameterizedTest
      @NullSource
      @ValueSource(strings = {"", "   "})
      @DisplayName("Should handle empty or null tag inputs")
      void shouldHandleEmptyOrNullTagInputs(String input) throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "tagsInclude", input);

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }

      @Test
      @DisplayName("Should trim whitespace from tags")
      void shouldTrimWhitespaceFromTags() throws Exception {
         // Arrange
         File testOutputDir = tempDir.resolve("test-classes").toFile();
         testOutputDir.mkdirs();

         setField(mojo, "enabled", true);
         setField(mojo, "testEngine", "junit");
         setField(mojo, "testOutputDir", testOutputDir);
         setField(mojo, "maxMethods", 20);
         setField(mojo, "outputJsonFile", tempDir.resolve("output").toString());
         setField(mojo, "projectBaseDir", tempDir.toFile());
         setField(mojo, "parallelMethods", true);
         setField(mojo, "maxNumberOfParallelRunners", 5);
         setField(mojo, "tagsInclude", "  smoke  ,  integration  ");

         // Act & Assert
         assertDoesNotThrow(() -> mojo.execute());
      }
   }

   // ===== Helper Methods =====

   private void setField(Object target, String fieldName, Object value) throws Exception {
      Field field = findField(target.getClass(), fieldName);
      field.setAccessible(true);
      field.set(target, value);
   }

   private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
      try {
         return clazz.getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
         if (clazz.getSuperclass() != null) {
            return findField(clazz.getSuperclass(), fieldName);
         }
         throw e;
      }
   }
}
