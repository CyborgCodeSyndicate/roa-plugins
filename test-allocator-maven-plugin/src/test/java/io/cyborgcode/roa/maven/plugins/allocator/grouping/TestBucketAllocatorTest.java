package io.cyborgcode.roa.maven.plugins.allocator.grouping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TestBucketAllocator Tests")
class TestBucketAllocatorTest {

   @Nested
   @DisplayName("groupClasses Tests")
   class GroupClassesTests {

      @Test
      @DisplayName("Should create single bucket when all classes fit")
      void shouldCreateSingleBucketWhenAllClassesFit() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "TestClass1", 5,
               "TestClass2", 8,
               "TestClass3", 7
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertEquals(1, buckets.size(), "Should create single bucket");
         assertEquals(20, buckets.get(0).getTotalMethods(), "Total methods should be 20");
         assertEquals(3, buckets.get(0).getClassNames().size(), "Should contain all 3 classes");
      }

      @Test
      @DisplayName("Should create multiple buckets when classes exceed limit")
      void shouldCreateMultipleBucketsWhenClassesExceedLimit() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "TestClass1", 10,
               "TestClass2", 15,
               "TestClass3", 12
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertTrue(buckets.size() > 1, "Should create multiple buckets");
         
         int totalMethods = buckets.stream()
               .mapToInt(TestBucket::getTotalMethods)
               .sum();
         assertEquals(37, totalMethods, "Total methods across buckets should be 37");
      }

      @Test
      @DisplayName("Should create separate bucket for class exceeding max methods")
      void shouldCreateSeparateBucketForClassExceedingMaxMethods() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "LargeTestClass", 50,
               "SmallTestClass1", 5,
               "SmallTestClass2", 8
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertTrue(buckets.size() >= 2, "Should create at least 2 buckets");
         
         // Find the large class bucket
         TestBucket largeBucket = buckets.stream()
               .filter(b -> b.getClassNames().contains("LargeTestClass"))
               .findFirst()
               .orElseThrow();
         
         assertEquals(1, largeBucket.getClassNames().size(), 
               "Large class should be in its own bucket");
         assertEquals(50, largeBucket.getTotalMethods(), 
               "Large bucket should have 50 methods");
      }

      @Test
      @DisplayName("Should sort classes by method count in descending order")
      void shouldSortClassesByMethodCountInDescendingOrder() {
         // Arrange
         Map<String, Integer> classMethodCounts = new HashMap<>();
         classMethodCounts.put("Small", 2);
         classMethodCounts.put("Large", 15);
         classMethodCounts.put("Medium", 8);

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertNotNull(buckets, "Buckets should not be null");
         assertTrue(buckets.size() > 0, "Should create at least one bucket");
         
         // First bucket should contain the largest class
         TestBucket firstBucket = buckets.get(0);
         assertTrue(firstBucket.getClassNames().contains("Large"), 
               "First bucket should process largest class first");
      }

      @Test
      @DisplayName("Should handle empty class method counts")
      void shouldHandleEmptyClassMethodCounts() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of();

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertNotNull(buckets, "Buckets should not be null");
         assertTrue(buckets.isEmpty(), "Should return empty list for empty input");
      }

      @Test
      @DisplayName("Should handle single class")
      void shouldHandleSingleClass() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of("SingleClass", 10);

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertEquals(1, buckets.size(), "Should create one bucket");
         assertEquals(1, buckets.get(0).getClassNames().size(), "Bucket should contain one class");
         assertEquals("SingleClass", buckets.get(0).getClassNames().get(0));
         assertEquals(10, buckets.get(0).getTotalMethods());
      }

      @Test
      @DisplayName("Should optimize bucket utilization")
      void shouldOptimizeBucketUtilization() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "Class1", 8,
               "Class2", 7,
               "Class3", 6,
               "Class4", 5,
               "Class5", 4
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 15);

         // Assert
         assertNotNull(buckets, "Buckets should not be null");
         assertTrue(buckets.size() >= 2, "Should create at least 2 buckets");
         
         // Verify no bucket exceeds max
         for (TestBucket bucket : buckets) {
            assertTrue(bucket.getTotalMethods() <= 15, 
                  "No bucket should exceed max methods");
         }
         
         // Verify all classes are allocated
         long totalClasses = buckets.stream()
               .flatMap(b -> b.getClassNames().stream())
               .count();
         assertEquals(5, totalClasses, "All classes should be allocated");
      }

      @Test
      @DisplayName("Should handle multiple large classes")
      void shouldHandleMultipleLargeClasses() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "LargeClass1", 100,
               "LargeClass2", 80,
               "LargeClass3", 90,
               "SmallClass", 5
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertTrue(buckets.size() >= 3, "Should create at least 3 buckets for 3 large classes");
         
         // Each large class should be in its own bucket
         long largeBuckets = buckets.stream()
               .filter(b -> b.getTotalMethods() > 20)
               .count();
         assertEquals(3, largeBuckets, "Should have 3 buckets for large classes");
      }

      @Test
      @DisplayName("Should handle exact fit scenarios")
      void shouldHandleExactFitScenarios() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "Class1", 10,
               "Class2", 10
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertEquals(1, buckets.size(), "Should create one bucket for exact fit");
         assertEquals(20, buckets.get(0).getTotalMethods(), "Should exactly fill bucket");
         assertEquals(2, buckets.get(0).getClassNames().size(), "Should contain both classes");
      }

      @ParameterizedTest
      @DisplayName("Should handle various bucket size scenarios")
      @MethodSource("provideBucketScenarios")
      void shouldHandleVariousBucketSizeScenarios(
            Map<String, Integer> classMethodCounts, 
            int maxMethods, 
            int expectedMinBuckets) {
         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, maxMethods);

         // Assert
         assertTrue(buckets.size() >= expectedMinBuckets, 
               "Should create at least " + expectedMinBuckets + " bucket(s)");
         
         // Verify all methods are accounted for
         int totalMethods = buckets.stream()
               .mapToInt(TestBucket::getTotalMethods)
               .sum();
         int expectedTotal = classMethodCounts.values().stream()
               .mapToInt(Integer::intValue)
               .sum();
         assertEquals(expectedTotal, totalMethods, "All methods should be accounted for");
      }

      private static Stream<Arguments> provideBucketScenarios() {
         return Stream.of(
               Arguments.of(
                     Map.of("C1", 5, "C2", 5, "C3", 5), 
                     10, 
                     2
               ),
               Arguments.of(
                     Map.of("C1", 25), 
                     10, 
                     1
               ),
               Arguments.of(
                     Map.of("C1", 1, "C2", 1, "C3", 1, "C4", 1), 
                     5, 
                     1
               )
         );
      }

      @Test
      @DisplayName("Should handle class with zero methods")
      void shouldHandleClassWithZeroMethods() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "ClassWithMethods", 10,
               "ClassWithZeroMethods", 0
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         assertFalse(buckets.isEmpty(), "Should create buckets");
         
         // Verify the class with 0 methods is still allocated
         boolean foundZeroClass = buckets.stream()
               .flatMap(b -> b.getClassNames().stream())
               .anyMatch(name -> name.equals("ClassWithZeroMethods"));
         assertTrue(foundZeroClass, "Class with zero methods should still be allocated");
      }

      @Test
      @DisplayName("Should create new bucket when current bucket cannot fit next class")
      void shouldCreateNewBucketWhenCurrentBucketCannotFitNextClass() {
         // Arrange
         Map<String, Integer> classMethodCounts = new HashMap<>();
         classMethodCounts.put("Class1", 12);
         classMethodCounts.put("Class2", 10);
         classMethodCounts.put("Class3", 8);

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 15);

         // Assert
         assertEquals(3, buckets.size(), "Should create 3 buckets since no classes can fit together");
         
         // Verify all buckets respect the limit
         for (TestBucket bucket : buckets) {
            assertTrue(bucket.getTotalMethods() <= 15, 
                  "Each bucket should not exceed limit");
            assertEquals(1, bucket.getClassNames().size(),
                  "Each bucket should contain exactly one class");
         }
         
         // Verify all methods are accounted for
         int totalMethods = buckets.stream()
               .mapToInt(TestBucket::getTotalMethods)
               .sum();
         assertEquals(30, totalMethods, "Total methods should be 30 (12+10+8)");
      }

      @Test
      @DisplayName("Should combine classes when they fit together in bucket")
      void shouldCombineClassesWhenTheyFitTogether() {
         // Arrange
         Map<String, Integer> classMethodCounts = new HashMap<>();
         classMethodCounts.put("Class1", 12);
         classMethodCounts.put("Class2", 10);
         classMethodCounts.put("Class3", 5);

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 15);

         // Assert
         assertEquals(2, buckets.size(), "Should create 2 buckets");
         
         // Find bucket with Class1 - should be alone
         TestBucket class1Bucket = buckets.stream()
               .filter(b -> b.getClassNames().contains("Class1"))
               .findFirst()
               .orElseThrow(() -> new AssertionError("Class1 should be in a bucket"));
         assertEquals(1, class1Bucket.getClassNames().size(),
               "Class1 should be alone in its bucket");
         assertEquals(12, class1Bucket.getTotalMethods(),
               "Class1 bucket should have 12 methods");
         
         // Find bucket with Class2 and Class3 - should be combined
         TestBucket combinedBucket = buckets.stream()
               .filter(b -> b.getClassNames().contains("Class2"))
               .findFirst()
               .orElseThrow(() -> new AssertionError("Class2 should be in a bucket"));
         assertEquals(2, combinedBucket.getClassNames().size(),
               "Combined bucket should have 2 classes");
         assertTrue(combinedBucket.getClassNames().contains("Class2"),
               "Combined bucket should contain Class2");
         assertTrue(combinedBucket.getClassNames().contains("Class3"),
               "Combined bucket should contain Class3");
         assertEquals(15, combinedBucket.getTotalMethods(),
               "Combined bucket should have 15 methods (10+5)");
      }

      @Test
      @DisplayName("Should maintain class integrity in buckets")
      void shouldMaintainClassIntegrityInBuckets() {
         // Arrange
         Map<String, Integer> classMethodCounts = Map.of(
               "TestA", 5,
               "TestB", 10,
               "TestC", 15
         );

         // Act
         List<TestBucket> buckets = TestBucketAllocator.groupClasses(classMethodCounts, 20);

         // Assert
         long totalClassOccurrences = buckets.stream()
               .flatMap(b -> b.getClassNames().stream())
               .count();
         
         assertEquals(3, totalClassOccurrences, 
               "Each class should appear exactly once across all buckets");
      }
   }
}
