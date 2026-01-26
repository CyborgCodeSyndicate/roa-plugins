package io.cyborgcode.roa.maven.plugins.allocator.config;

import java.util.Set;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Configuration class for JUnit test splitting in a Maven project.
 *
 * <p>Extends {@link TestSplitterConfiguration} to include specific settings
 * for filtering tests based on JUnit tags.
 *
 * <p>This configuration allows the user to specify:
 * <ul>
 *   <li>Tags to include in test execution.</li>
 *   <li>Tags to exclude from test execution.</li>
 * </ul>
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
@Getter
@SuperBuilder
public class TestSplitterConfigurationJunit extends TestSplitterConfiguration {

   /**
    * Set of tags used to filter included JUnit tests.
    */
   private final Set<String> includeTags;

   /**
    * Set of tags used to filter excluded JUnit tests.
    */
   private final Set<String> excludeTags;


}
