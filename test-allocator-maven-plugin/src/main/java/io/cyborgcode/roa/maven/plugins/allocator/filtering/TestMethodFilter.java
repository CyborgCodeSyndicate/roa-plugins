package io.cyborgcode.roa.maven.plugins.allocator.filtering;

import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Utility class for filtering and counting test methods based on include/exclude tags.
 *
 * <p>This class provides functionality to filter test methods in a given class according to specific
 * inclusion and exclusion rules. It is primarily used for dynamically determining which test methods
 * should be executed in parallel or sequentially.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public final class TestMethodFilter {

   /**
    * Private constructor to prevent instantiation of this utility class.
    */
   private TestMethodFilter() {
   }

   /**
    * Counts the number of test methods in the given class that match the provided tag inclusion and exclusion rules.
    *
    * <p>The method filters test methods based on the following criteria:
    * <ul>
    *     <li>Methods must be annotated with {@link Test}.</li>
    *     <li>Methods with excluded tags are ignored.</li>
    *     <li>If include tags are provided, at least one tag must match.</li>
    * </ul>
    *
    * <p>If the class extends a sequential test framework (e.g., `BaseTestSequential`), the count is adjusted
    * to reflect sequential execution requirements.
    *
    * @param clazz           The class containing test methods.
    * @param includeTags     A set of tags to include (empty set allows all).
    * @param excludeTags     A set of tags to exclude.
    * @param parallelMethods Whether the test methods can be executed in parallel.
    * @return The number of test methods matching the criteria.
    */
   public static int countMatchingTestMethods(
         Class<?> clazz,
         Set<String> includeTags,
         Set<String> excludeTags,
         boolean parallelMethods
   ) {
      int count = 0;

      for (Method method : clazz.getDeclaredMethods()) {
         if (!method.isAnnotationPresent(Test.class)) {
            continue;
         }

         Set<String> methodTags = TestTagExtractor.extractTags(method);

         if (isMethodIncluded(methodTags, includeTags, excludeTags)) {
            count++;
         }
      }

      return (!parallelMethods || isSequentialClassTheRingFramework(clazz)) && count > 0 ? 1 : count;
   }

   /**
    * Determines if a method should be included based on its tags.
    *
    * @param methodTags  The tags associated with the method.
    * @param includeTags The set of tags that should be included.
    * @param excludeTags The set of tags that should be excluded.
    * @return {@code true} if the method meets the inclusion criteria; {@code false} otherwise.
    */
   private static boolean isMethodIncluded(
         Set<String> methodTags,
         Set<String> includeTags,
         Set<String> excludeTags
   ) {
      return !hasExcludedTag(methodTags, excludeTags)
            && isIncludedByTagRules(methodTags, includeTags);
   }

   /**
    * Checks if a method contains any excluded tags.
    *
    * @param methodTags  The tags associated with the method.
    * @param excludeTags The set of tags to exclude.
    * @return {@code true} if any excluded tag is present; {@code false} otherwise.
    */
   private static boolean hasExcludedTag(Set<String> methodTags, Set<String> excludeTags) {
      return excludeTags.stream().anyMatch(methodTags::contains);
   }

   /**
    * Checks if a method satisfies the inclusion tag rules.
    *
    * <p>If no include tags are specified, the method is included by default.
    *
    * @param methodTags  The tags associated with the method.
    * @param includeTags The set of tags to include.
    * @return {@code true} if the method meets the inclusion criteria; {@code false} otherwise.
    */
   private static boolean isIncludedByTagRules(Set<String> methodTags, Set<String> includeTags) {
      return includeTags.isEmpty() || includeTags.stream().anyMatch(methodTags::contains);
   }

   /**
    * Determines if the given class is part of a sequential test execution framework.
    *
    * <p>Classes that extend `BaseTestSequential` or similar frameworks should be executed
    * sequentially instead of in parallel.
    *
    * @param clazz The class to check.
    * @return {@code true} if the class requires sequential execution; {@code false} otherwise.
    */
   private static boolean isSequentialClassTheRingFramework(Class<?> clazz) {
      return clazz.getSuperclass() != null && clazz.getSuperclass().getSimpleName().contains("BaseTestSequential");
   }

}
