package io.cyborgcode.roa.maven.plugins.allocator.filtering;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("TestTagExtractor Tests")
class TestTagExtractorTest {

   @Nested
   @DisplayName("extractTags Tests")
   class ExtractTagsTests {

      @Test
      @DisplayName("Should extract single tag from method")
      void shouldExtractSingleTagFromMethod() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithSingleTag");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertNotNull(tags, "Tags should not be null");
         assertEquals(1, tags.size(), "Should extract exactly one tag");
         assertTrue(tags.contains("smoke"), "Should contain 'smoke' tag");
      }

      @Test
      @DisplayName("Should extract multiple tags from Tags annotation")
      void shouldExtractMultipleTagsFromTagsAnnotation() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithMultipleTags");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertNotNull(tags, "Tags should not be null");
         assertEquals(3, tags.size(), "Should extract three tags");
         assertTrue(tags.contains("integration"), "Should contain 'integration' tag");
         assertTrue(tags.contains("slow"), "Should contain 'slow' tag");
         assertTrue(tags.contains("database"), "Should contain 'database' tag");
      }

      @Test
      @DisplayName("Should return empty set for method without tags")
      void shouldReturnEmptySetForMethodWithoutTags() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithoutTags");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertNotNull(tags, "Tags should not be null");
         assertTrue(tags.isEmpty(), "Should return empty set for method without tags");
      }

      @Test
      @DisplayName("Should extract tags from meta-annotations")
      void shouldExtractTagsFromMetaAnnotations() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithMetaAnnotation");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertNotNull(tags, "Tags should not be null");
         assertFalse(tags.isEmpty(), "Should extract tags from meta-annotation");
         assertTrue(tags.contains("meta-tag"), "Should contain 'meta-tag' from meta-annotation");
      }

      @Test
      @DisplayName("Should extract both direct and meta tags")
      void shouldExtractBothDirectAndMetaTags() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithDirectAndMetaTags");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertNotNull(tags, "Tags should not be null");
         assertTrue(tags.size() >= 2, "Should extract at least 2 tags");
         assertTrue(tags.contains("direct"), "Should contain 'direct' tag");
         assertTrue(tags.contains("meta-tag"), "Should contain 'meta-tag' from meta-annotation");
      }

      @Test
      @DisplayName("Should handle method with only Test annotation")
      void shouldHandleMethodWithOnlyTestAnnotation() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithOnlyTestAnnotation");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertNotNull(tags, "Tags should not be null");
         assertTrue(tags.isEmpty(), "Should return empty set when only @Test annotation present");
      }

      @Test
      @DisplayName("Should not duplicate tags")
      void shouldNotDuplicateTags() throws NoSuchMethodException {
         // Arrange
         Method method = TaggedMethodsFixture.class.getDeclaredMethod("methodWithSingleTag");

         // Act
         Set<String> tags = TestTagExtractor.extractTags(method);

         // Assert
         assertEquals(1, tags.size(), "Set should prevent duplicate tags");
      }
   }

   // ===== Test Helper Classes =====

   @Tag("meta-tag")
   @Target({ElementType.METHOD})
   @Retention(RetentionPolicy.RUNTIME)
   @interface CustomMetaAnnotation {
   }

   /**
    * Helper class for reflection testing - not meant to be executed as tests.
    * Methods have @Test annotations so the TestTagExtractor can inspect them.
    */
   static class TaggedMethodsFixture {

      @Test
      @Tag("smoke")
      public void methodWithSingleTag() {
      }

      @Test
      @Tags({
            @Tag("integration"),
            @Tag("slow"),
            @Tag("database")
      })
      public void methodWithMultipleTags() {
      }

      @Test
      public void methodWithoutTags() {
      }

      @Test
      @CustomMetaAnnotation
      public void methodWithMetaAnnotation() {
      }

      @Test
      @Tag("direct")
      @CustomMetaAnnotation
      public void methodWithDirectAndMetaTags() {
      }

      @Test
      public void methodWithOnlyTestAnnotation() {
      }
   }
}
