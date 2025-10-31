package io.cyborgcode.roa.maven.plugins.allocator.grouping;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for grouping test classes into execution buckets based on method count.
 *
 * <p>This class sorts test classes by the number of test methods and then distributes them
 * into buckets while ensuring that each bucket does not exceed the configured limit
 * for maximum methods per bucket.
 *
 * <p>Classes with a high number of test methods that exceed the bucket limit are placed
 * in their own separate buckets.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public final class TestBucketAllocator {

   /**
    * Private constructor to prevent instantiation of utility class.
    */
   private TestBucketAllocator() {
   }

   /**
    * Groups test classes into buckets based on the number of test methods per class.
    *
    * <p>The algorithm sorts test classes by method count in descending order and then
    * distributes them into buckets, ensuring that each bucket does not exceed the
    * maximum number of methods allowed.
    *
    * @param classMethodCounts   A map containing test class names and their corresponding method counts.
    * @param maxMethodsPerBucket The maximum number of test methods allowed per bucket.
    * @return A list of {@link TestBucket} objects representing grouped test classes.
    */
   public static List<TestBucket> groupClasses(
         Map<String, Integer> classMethodCounts,
         int maxMethodsPerBucket
   ) {
      List<Map.Entry<String, Integer>> sortedClasses = sortClassesByMethodCount(classMethodCounts);

      List<TestBucket> buckets = new ArrayList<>();
      List<String> currentBucketClasses = new ArrayList<>();
      int currentBucketMethodCount = 0;

      for (Map.Entry<String, Integer> entry : sortedClasses) {
         int methods = entry.getValue();
         String className = entry.getKey();

         if (methods > maxMethodsPerBucket) {
            buckets.add(createSingleClassBucket(className, methods));
            continue;
         }

         if (currentBucketMethodCount + methods <= maxMethodsPerBucket) {
            currentBucketClasses.add(className);
            currentBucketMethodCount += methods;
         } else {
            buckets.add(createBucket(currentBucketClasses, currentBucketMethodCount));

            currentBucketClasses = new ArrayList<>(List.of(className));
            currentBucketMethodCount = methods;
         }
      }

      if (!currentBucketClasses.isEmpty()) {
         buckets.add(createBucket(currentBucketClasses, currentBucketMethodCount));
      }

      return buckets;
   }

   /**
    * Sorts test classes by the number of test methods in descending order.
    *
    * @param classMethodCounts A map containing test class names and their respective test method counts.
    * @return A sorted list of map entries, ordered by test method count in descending order.
    */
   private static List<Map.Entry<String, Integer>> sortClassesByMethodCount(
         Map<String, Integer> classMethodCounts
   ) {
      return classMethodCounts.entrySet().stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .toList();
   }

   /**
    * Creates a test bucket for a single class that has more test methods than the allowed per bucket.
    *
    * @param className The name of the test class.
    * @param methods   The number of test methods in the class.
    * @return A {@link TestBucket} containing only the specified class.
    */
   private static TestBucket createSingleClassBucket(String className, int methods) {
      return new TestBucket(List.of(className), methods);
   }

   /**
    * Creates a test bucket with a set of classes and their combined method count.
    *
    * @param classes      The list of test class names to include in the bucket.
    * @param totalMethods The total number of test methods within the bucket.
    * @return A {@link TestBucket} containing the specified test classes.
    */
   private static TestBucket createBucket(List<String> classes, int totalMethods) {
      return new TestBucket(classes, totalMethods);
   }

}
