package io.cyborgcode.roa.maven.plugins.allocator.grouping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TestBucket Tests")
class TestBucketTest {

   @Nested
   @DisplayName("Constructor and Getter Tests")
   class ConstructorAndGetterTests {

      @Test
      @DisplayName("Should create bucket with class names and total methods")
      void shouldCreateBucketWithClassNamesAndTotalMethods() {
         // Arrange
         List<String> classNames = List.of("TestClass1", "TestClass2", "TestClass3");
         int totalMethods = 25;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertNotNull(bucket, "Bucket should not be null");
         assertEquals(classNames, bucket.getClassNames(), "Class names should match");
         assertEquals(totalMethods, bucket.getTotalMethods(), "Total methods should match");
      }

      @Test
      @DisplayName("Should create bucket with single class")
      void shouldCreateBucketWithSingleClass() {
         // Arrange
         List<String> classNames = List.of("SingleTestClass");
         int totalMethods = 10;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertEquals(1, bucket.getClassNames().size(), "Should have one class");
         assertEquals("SingleTestClass", bucket.getClassNames().get(0));
         assertEquals(10, bucket.getTotalMethods());
      }

      @Test
      @DisplayName("Should create bucket with empty class list")
      void shouldCreateBucketWithEmptyClassList() {
         // Arrange
         List<String> classNames = Collections.emptyList();
         int totalMethods = 0;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertNotNull(bucket.getClassNames(), "Class names should not be null");
         assertTrue(bucket.getClassNames().isEmpty(), "Class names should be empty");
         assertEquals(0, bucket.getTotalMethods());
      }

      @Test
      @DisplayName("Should create bucket with zero total methods")
      void shouldCreateBucketWithZeroTotalMethods() {
         // Arrange
         List<String> classNames = List.of("TestClass");
         int totalMethods = 0;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertEquals(1, bucket.getClassNames().size());
         assertEquals(0, bucket.getTotalMethods(), "Total methods should be 0");
      }

      @Test
      @DisplayName("Should create bucket with large number of methods")
      void shouldCreateBucketWithLargeNumberOfMethods() {
         // Arrange
         List<String> classNames = List.of("LargeTestClass");
         int totalMethods = 1000;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertEquals(1000, bucket.getTotalMethods(), "Should handle large method counts");
      }

      @Test
      @DisplayName("Should create bucket with multiple classes and verify order")
      void shouldCreateBucketWithMultipleClassesAndVerifyOrder() {
         // Arrange
         List<String> classNames = List.of("Alpha", "Beta", "Gamma", "Delta");
         int totalMethods = 50;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertEquals(4, bucket.getClassNames().size());
         assertEquals("Alpha", bucket.getClassNames().get(0));
         assertEquals("Beta", bucket.getClassNames().get(1));
         assertEquals("Gamma", bucket.getClassNames().get(2));
         assertEquals("Delta", bucket.getClassNames().get(3));
      }

      @Test
      @DisplayName("Should preserve fully qualified class names")
      void shouldPreserveFullyQualifiedClassNames() {
         // Arrange
         List<String> classNames = List.of(
               "com.example.test.TestClass1",
               "com.example.test.integration.TestClass2",
               "com.example.test.unit.TestClass3"
         );
         int totalMethods = 30;

         // Act
         TestBucket bucket = new TestBucket(classNames, totalMethods);

         // Assert
         assertEquals(3, bucket.getClassNames().size());
         assertTrue(bucket.getClassNames().get(0).contains("com.example.test.TestClass1"));
         assertTrue(bucket.getClassNames().get(1).contains("integration"));
      }
   }
}
