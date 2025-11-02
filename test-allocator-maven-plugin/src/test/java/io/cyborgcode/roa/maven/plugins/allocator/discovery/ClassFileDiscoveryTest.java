package io.cyborgcode.roa.maven.plugins.allocator.discovery;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ClassFileDiscovery Tests")
class ClassFileDiscoveryTest {

   @Nested
   @DisplayName("findClassFiles Tests")
   class FindClassFilesTests {

      @Test
      @DisplayName("Should find all class files in directory structure")
      void shouldFindAllClassFilesInDirectoryStructure(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path packageDir = tempDir.resolve("com/example/test");
         Files.createDirectories(packageDir);

         File class1 = packageDir.resolve("TestClass1.class").toFile();
         File class2 = packageDir.resolve("TestClass2.class").toFile();
         File class3 = tempDir.resolve("RootClass.class").toFile();

         Files.createFile(class1.toPath());
         Files.createFile(class2.toPath());
         Files.createFile(class3.toPath());

         // Create a non-class file that should be ignored
         Files.createFile(packageDir.resolve("README.txt"));

         // Act
         List<File> classFiles = ClassFileDiscovery.findClassFiles(tempDir.toFile());

         // Assert
         assertEquals(3, classFiles.size(), "Should find exactly 3 class files");
         assertTrue(classFiles.contains(class1), "Should contain TestClass1.class");
         assertTrue(classFiles.contains(class2), "Should contain TestClass2.class");
         assertTrue(classFiles.contains(class3), "Should contain RootClass.class");
      }

      @Test
      @DisplayName("Should return empty list when no class files exist")
      void shouldReturnEmptyListWhenNoClassFilesExist(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path packageDir = tempDir.resolve("com/example");
         Files.createDirectories(packageDir);
         Files.createFile(packageDir.resolve("README.txt"));
         Files.createFile(packageDir.resolve("config.xml"));

         // Act
         List<File> classFiles = ClassFileDiscovery.findClassFiles(tempDir.toFile());

         // Assert
         assertNotNull(classFiles, "Result should not be null");
         assertTrue(classFiles.isEmpty(), "Should return empty list when no class files exist");
      }

      @Test
      @DisplayName("Should handle empty directory")
      void shouldHandleEmptyDirectory(@TempDir Path tempDir) {
         // Act
         List<File> classFiles = ClassFileDiscovery.findClassFiles(tempDir.toFile());

         // Assert
         assertNotNull(classFiles, "Result should not be null");
         assertTrue(classFiles.isEmpty(), "Should return empty list for empty directory");
      }

      @Test
      @DisplayName("Should handle nested directory structures")
      void shouldHandleNestedDirectoryStructures(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path level1 = tempDir.resolve("level1");
         Path level2 = level1.resolve("level2");
         Path level3 = level2.resolve("level3");
         Files.createDirectories(level3);

         File class1 = level1.resolve("Class1.class").toFile();
         File class2 = level2.resolve("Class2.class").toFile();
         File class3 = level3.resolve("Class3.class").toFile();

         Files.createFile(class1.toPath());
         Files.createFile(class2.toPath());
         Files.createFile(class3.toPath());

         // Act
         List<File> classFiles = ClassFileDiscovery.findClassFiles(tempDir.toFile());

         // Assert
         assertEquals(3, classFiles.size(), "Should find all class files in nested structure");
         assertTrue(classFiles.contains(class1));
         assertTrue(classFiles.contains(class2));
         assertTrue(classFiles.contains(class3));
      }

      @Test
      @DisplayName("Should ignore directories with class-like names")
      void shouldIgnoreDirectoriesWithClassLikeNames(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path weirdDir = tempDir.resolve("SomeDir.class");
         Files.createDirectories(weirdDir);

         File actualClass = tempDir.resolve("ActualClass.class").toFile();
         Files.createFile(actualClass.toPath());

         // Act
         List<File> classFiles = ClassFileDiscovery.findClassFiles(tempDir.toFile());

         // Assert
         assertEquals(1, classFiles.size(), "Should only find actual files, not directories");
         assertTrue(classFiles.contains(actualClass));
      }

      @Test
      @DisplayName("Should handle directory with only subdirectories and no files")
      void shouldHandleDirectoryWithOnlySubdirectories(@TempDir Path tempDir) throws IOException {
         // Arrange
         Files.createDirectories(tempDir.resolve("dir1/dir2/dir3"));

         // Act
         List<File> classFiles = ClassFileDiscovery.findClassFiles(tempDir.toFile());

         // Assert
         assertTrue(classFiles.isEmpty(), "Should return empty list when only directories exist");
      }
   }

   @Nested
   @DisplayName("fileToClassName Tests")
   class FileToClassNameTests {

      @Test
      @DisplayName("Should convert simple class file to class name")
      void shouldConvertSimpleClassFileToClassName(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path packageDir = tempDir.resolve("com/example/test");
         Files.createDirectories(packageDir);
         File classFile = packageDir.resolve("MyTestClass.class").toFile();
         Files.createFile(classFile.toPath());

         // Act
         String className = ClassFileDiscovery.fileToClassName(classFile, tempDir.toFile());

         // Assert
         assertEquals("com.example.test.MyTestClass", className,
               "Should convert file path to fully qualified class name");
      }

      @Test
      @DisplayName("Should convert nested package class file to class name")
      void shouldConvertNestedPackageClassFileToClassName(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path deepPackage = tempDir.resolve("io/cyborgcode/roa/maven/plugins/test");
         Files.createDirectories(deepPackage);
         File classFile = deepPackage.resolve("DeepClass.class").toFile();
         Files.createFile(classFile.toPath());

         // Act
         String className = ClassFileDiscovery.fileToClassName(classFile, tempDir.toFile());

         // Assert
         assertEquals("io.cyborgcode.roa.maven.plugins.test.DeepClass", className,
               "Should handle deeply nested package structures");
      }

      @Test
      @DisplayName("Should convert root level class file to class name")
      void shouldConvertRootLevelClassFileToClassName(@TempDir Path tempDir) throws IOException {
         // Arrange
         File classFile = tempDir.resolve("RootClass.class").toFile();
         Files.createFile(classFile.toPath());

         // Act
         String className = ClassFileDiscovery.fileToClassName(classFile, tempDir.toFile());

         // Assert
         assertEquals("RootClass", className,
               "Should handle class files in root directory");
      }

      @Test
      @DisplayName("Should handle class file with inner class notation")
      void shouldHandleClassFileWithInnerClassNotation(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path packageDir = tempDir.resolve("com/example");
         Files.createDirectories(packageDir);
         File innerClassFile = packageDir.resolve("OuterClass$InnerClass.class").toFile();
         Files.createFile(innerClassFile.toPath());

         // Act
         String className = ClassFileDiscovery.fileToClassName(innerClassFile, tempDir.toFile());

         // Assert
         assertEquals("com.example.OuterClass$InnerClass", className,
               "Should preserve inner class notation");
      }

      @Test
      @DisplayName("Should handle single character package names")
      void shouldHandleSingleCharacterPackageNames(@TempDir Path tempDir) throws IOException {
         // Arrange
         Path singleCharPackage = tempDir.resolve("a/b/c");
         Files.createDirectories(singleCharPackage);
         File classFile = singleCharPackage.resolve("Test.class").toFile();
         Files.createFile(classFile.toPath());

         // Act
         String className = ClassFileDiscovery.fileToClassName(classFile, tempDir.toFile());

         // Assert
         assertEquals("a.b.c.Test", className,
               "Should handle single character package names");
      }
   }
}
