package io.cyborgcode.roa.maven.plugins.allocator.filtering;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TestMethodFilter Tests")
class TestMethodFilterTest {

   @Nested
   @DisplayName("countMatchingTestMethods Tests")
   class CountMatchingTestMethodsTests {

      @Test
      @DisplayName("Should count all test methods when no filters applied")
      void shouldCountAllTestMethodsWhenNoFiltersApplied() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Collections.emptySet(),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(5, count, "Should count all 5 test methods");
      }

      @Test
      @DisplayName("Should count only methods with included tags")
      void shouldCountOnlyMethodsWithIncludedTags() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Set.of("smoke"),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(2, count, "Should count only methods with 'smoke' tag");
      }

      @Test
      @DisplayName("Should exclude methods with excluded tags")
      void shouldExcludeMethodsWithExcludedTags() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Collections.emptySet(),
               Set.of("slow"),
               true
         );

         // Assert
         assertEquals(4, count, "Should exclude 1 method with 'slow' tag");
      }

      @Test
      @DisplayName("Should apply both include and exclude filters")
      void shouldApplyBothIncludeAndExcludeFilters() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Set.of("integration"),
               Set.of("slow"),
               true
         );

         // Assert
         assertEquals(1, count, "Should count methods with 'integration' but not 'slow'");
      }

      @Test
      @DisplayName("Should count multiple included tags")
      void shouldCountMultipleIncludedTags() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Set.of("smoke", "fast"),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(3, count, "Should count methods with either 'smoke' or 'fast' tag");
      }

      @Test
      @DisplayName("Should return 0 when no methods match filters")
      void shouldReturnZeroWhenNoMethodsMatchFilters() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Set.of("nonexistent"),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(0, count, "Should return 0 when no methods match include tags");
      }

      @Test
      @DisplayName("Should ignore non-test methods")
      void shouldIgnoreNonTestMethods() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithNonTestMethods.class,
               Collections.emptySet(),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(2, count, "Should count only methods with @Test annotation");
      }

      @Test
      @DisplayName("Should return 1 when parallelMethods is false and class has tests")
      void shouldReturnOneWhenParallelMethodsIsFalseAndClassHasTests() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Collections.emptySet(),
               Collections.emptySet(),
               false
         );

         // Assert
         assertEquals(1, count, "Should return 1 when parallelMethods is false");
      }

      @Test
      @DisplayName("Should return 0 when parallelMethods is false but no tests match")
      void shouldReturnZeroWhenParallelMethodsIsFalseButNoTestsMatch() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Set.of("nonexistent"),
               Collections.emptySet(),
               false
         );

         // Assert
         assertEquals(0, count, "Should return 0 when no tests match even with parallelMethods false");
      }

      @Test
      @DisplayName("Should handle class with no test methods")
      void shouldHandleClassWithNoTestMethods() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               ClassWithoutTests.class,
               Collections.emptySet(),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(0, count, "Should return 0 for class with no test methods");
      }

      @Test
      @DisplayName("Should return 1 for BaseTestSequential subclass when parallelMethods is true")
      void shouldReturnOneForBaseTestSequentialSubclassWhenParallelMethodsIsTrue() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               SequentialTestClass.class,
               Collections.emptySet(),
               Collections.emptySet(),
               true
         );

         // Assert
         assertEquals(1, count, "Should return 1 for sequential test classes regardless of method count");
      }

      @ParameterizedTest
      @DisplayName("Should handle various tag combinations")
      @MethodSource("provideTagCombinations")
      void shouldHandleVariousTagCombinations(Set<String> includeTags, Set<String> excludeTags, int expectedCount) {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               includeTags,
               excludeTags,
               true
         );

         // Assert
         assertEquals(expectedCount, count);
      }

      private static Stream<Arguments> provideTagCombinations() {
         return Stream.of(
               Arguments.of(Collections.emptySet(), Collections.emptySet(), 5),
               Arguments.of(Set.of("smoke"), Collections.emptySet(), 2),
               Arguments.of(Set.of("integration", "smoke"), Collections.emptySet(), 4),
               Arguments.of(Collections.emptySet(), Set.of("slow"), 4),
               Arguments.of(Set.of("fast"), Set.of("smoke"), 1)
         );
      }

      @Test
      @DisplayName("Should handle class with methods having multiple tags")
      void shouldHandleClassWithMethodsHavingMultipleTags() {
         // Act
         int count = TestMethodFilter.countMatchingTestMethods(
               TestClassWithMultipleMethods.class,
               Set.of("integration"),
               Collections.emptySet(),
               true
         );

         // Assert
         assertTrue(count > 0, "Should find methods with integration tag");
      }
   }

   // ===== Test Helper Classes =====

   static class TestClassWithMultipleMethods {

      @Test
      @Tag("smoke")
      public void smokeTest1() {
      }

      @Test
      @Tag("smoke")
      @Tag("fast")
      public void smokeTest2() {
      }

      @Test
      @Tags({
            @Tag("integration"),
            @Tag("slow")
      })
      public void integrationSlowTest() {
      }

      @Test
      @Tag("integration")
      @Tag("fast")
      public void integrationFastTest() {
      }

      @Test
      public void untaggedTest() {
      }
   }

   static class TestClassWithNonTestMethods {

      @Test
      public void testMethod1() {
      }

      @Test
      public void testMethod2() {
      }

      public void nonTestMethod1() {
      }

      public void nonTestMethod2() {
      }

      private void privateMethod() {
      }
   }

   static class ClassWithoutTests {
      public void regularMethod() {
      }

      private void privateMethod() {
      }
   }

   // Mock BaseTestSequential to test sequential test detection
   static class BaseTestSequential {
   }

   static class SequentialTestClass extends BaseTestSequential {
      @Test
      public void test1() {
      }

      @Test
      public void test2() {
      }

      @Test
      public void test3() {
      }
   }
}
