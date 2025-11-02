package io.cyborgcode.roa.maven.plugins.allocator.config;

import java.util.Set;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Configuration class for TestNG test splitting in a Maven project.
 *
 * <p>Extends {@link TestSplitterConfiguration} to include specific settings
 * for selecting TestNG suites.
 *
 * <p>This configuration allows the user to specify:
 * <ul>
 *   <li>A set of TestNG suite names to be executed.</li>
 * </ul>
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
@Getter
@SuperBuilder
public class TestSplitterConfigurationTestng extends TestSplitterConfiguration {

   /**
    * Set of TestNG suites to be executed.
    */
   private final Set<String> suites;


}
