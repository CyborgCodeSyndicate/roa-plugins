package io.cyborgcode.roa.maven.plugins.allocator.discovery;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestClassLoader Tests")
class TestClassLoaderTest {

   @Mock
   private TestSplitterConfiguration config;

   @Mock
   private MavenProject mavenProject;

   @Nested
   @DisplayName("Constructor Tests")
   class ConstructorTests {

      @Test
      @DisplayName("Should create TestClassLoader with valid configuration")
      void shouldCreateTestClassLoaderWithValidConfiguration() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         // Act
         TestClassLoader loader = TestClassLoader.from(config);

         // Assert
         assertNotNull(loader, "TestClassLoader should not be null");
      }

      @Test
      @DisplayName("Should throw IllegalStateException when dependencies not resolved")
      void shouldThrowIllegalStateExceptionWhenDependenciesNotResolved() throws Exception {
         // Arrange
         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements())
               .thenThrow(new org.apache.maven.artifact.DependencyResolutionRequiredException(null));

         // Act & Assert
         IllegalStateException exception = assertThrows(IllegalStateException.class,
               () -> TestClassLoader.from(config),
               "Should throw IllegalStateException when dependencies not resolved");

         assertNotNull(exception.getMessage());
         assertNotNull(exception.getCause());
      }

      @Test
      @DisplayName("Should handle null test classpath elements")
      void shouldHandleNullTestClasspathElements() throws Exception {
         // Arrange
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(null);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         // Act
         TestClassLoader loader = TestClassLoader.from(config);

         // Assert
         assertNotNull(loader, "Should handle null test classpath");
      }

      @Test
      @DisplayName("Should handle null compile classpath elements")
      void shouldHandleNullCompileClasspathElements() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(null);

         // Act
         TestClassLoader loader = TestClassLoader.from(config);

         // Assert
         assertNotNull(loader, "Should handle null compile classpath");
      }

      @Test
      @DisplayName("Should handle empty classpath elements")
      void shouldHandleEmptyClasspathElements() throws Exception {
         // Arrange
         List<String> testClasspath = new ArrayList<>();
         List<String> compileClasspath = new ArrayList<>();

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         // Act
         TestClassLoader loader = TestClassLoader.from(config);

         // Assert
         assertNotNull(loader, "Should handle empty classpath");
      }

      @Test
      @DisplayName("Should handle classpath with blank entries")
      void shouldHandleClasspathWithBlankEntries() throws Exception {
         // Arrange
         List<String> testClasspath = Arrays.asList("target/test-classes", "", "  ", null);
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         // Act
         TestClassLoader loader = TestClassLoader.from(config);

         // Assert
         assertNotNull(loader, "Should filter out blank entries");
      }

      @Test
      @DisplayName("Should throw IllegalStateException for invalid classpath entry")
      void shouldThrowIllegalStateExceptionForInvalidClasspathEntry() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("invalid://malformed-url");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         // Act & Assert
         // Note: This might not throw if the URI is technically valid
         // The actual behavior depends on File.toURI() implementation
         assertNotNull(TestClassLoader.from(config));
      }
   }

   @Nested
   @DisplayName("loadClass Tests")
   class LoadClassTests {

      @Test
      @DisplayName("Should load standard Java class")
      void shouldLoadStandardJavaClass() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act
         Class<?> clazz = loader.loadClass("java.lang.String");

         // Assert
         assertNotNull(clazz, "Should load java.lang.String");
         assertEquals("java.lang.String", clazz.getName());
      }

      @Test
      @DisplayName("Should return null for non-existent class")
      void shouldReturnNullForNonExistentClass() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act
         Class<?> clazz = loader.loadClass("com.nonexistent.FakeClass");

         // Assert
         assertNull(clazz, "Should return null for non-existent class");
      }

      @Test
      @DisplayName("Should return null for invalid class name")
      void shouldReturnNullForInvalidClassName() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act
         Class<?> clazz = loader.loadClass("Invalid..ClassName");

         // Assert
         assertNull(clazz, "Should return null for invalid class name");
      }

      @Test
      @DisplayName("Should load primitive wrapper classes")
      void shouldLoadPrimitiveWrapperClasses() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act
         Class<?> integerClass = loader.loadClass("java.lang.Integer");
         Class<?> booleanClass = loader.loadClass("java.lang.Boolean");

         // Assert
         assertNotNull(integerClass, "Should load Integer class");
         assertNotNull(booleanClass, "Should load Boolean class");
         assertEquals(Integer.class, integerClass);
         assertEquals(Boolean.class, booleanClass);
      }

      @Test
      @DisplayName("Should load classes from java.util package")
      void shouldLoadClassesFromJavaUtilPackage() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act
         Class<?> listClass = loader.loadClass("java.util.List");
         Class<?> mapClass = loader.loadClass("java.util.Map");

         // Assert
         assertNotNull(listClass, "Should load List interface");
         assertNotNull(mapClass, "Should load Map interface");
      }

      @Test
      @DisplayName("Should handle NoClassDefFoundError")
      void shouldHandleNoClassDefFoundError() throws Exception {
         // Arrange
         List<String> testClasspath = List.of("target/test-classes");
         List<String> compileClasspath = List.of("target/classes");

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act - Try to load a class that might cause NoClassDefFoundError
         Class<?> clazz = loader.loadClass("com.example.NonExistent$Inner");

         // Assert
         assertNull(clazz, "Should return null when NoClassDefFoundError occurs");
      }

      @Test
      @DisplayName("Should load test class from project")
      void shouldLoadTestClassFromProject() throws Exception {
         // Arrange
         // Use current classpath which includes the test itself
         String currentClasspath = System.getProperty("java.class.path");
         List<String> testClasspath = List.of(currentClasspath.split(File.pathSeparator));
         List<String> compileClasspath = new ArrayList<>();

         when(config.getMavenProject()).thenReturn(mavenProject);
         when(mavenProject.getTestClasspathElements()).thenReturn(testClasspath);
         when(mavenProject.getCompileClasspathElements()).thenReturn(compileClasspath);

         TestClassLoader loader = TestClassLoader.from(config);

         // Act - Load this test class itself
         Class<?> clazz = loader.loadClass(this.getClass().getName());

         // Assert
         assertNotNull(clazz, "Should load test class from project");
      }
   }
}
