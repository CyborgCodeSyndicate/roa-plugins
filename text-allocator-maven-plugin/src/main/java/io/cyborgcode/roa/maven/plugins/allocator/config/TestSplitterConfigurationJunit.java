package io.cyborgcode.roa.maven.plugins.allocator.config;

import java.io.File;
import java.util.Set;
import lombok.Getter;
import org.apache.maven.project.MavenProject;

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
public class TestSplitterConfigurationJunit extends TestSplitterConfiguration {

   /**
    * Set of tags used to filter included JUnit tests.
    */
   private final Set<String> includeTags;

   /**
    * Set of tags used to filter excluded JUnit tests.
    */
   private final Set<String> excludeTags;

   /**
    * Constructs a new {@code TestSplitterConfigurationJunit} instance.
    *
    * @param enabled                    Whether test splitting is enabled.
    * @param maxMethodsPerBucket        Maximum number of test methods per bucket.
    * @param testOutputDirectory        Directory where test outputs are stored.
    * @param mavenProject               The associated Maven project.
    * @param jsonOutputFile             Name of the output JSON file containing test groups.
    * @param projectRoot                Root directory of the project.
    * @param parallelMethods            Whether test methods should be executed in parallel.
    * @param maxNumberOfParallelRunners Maximum number of parallel test runners allowed.
    * @param includeTags                Set of tags specifying which tests to include.
    * @param excludeTags                Set of tags specifying which tests to exclude.
    */
   public TestSplitterConfigurationJunit(final boolean enabled, final int maxMethodsPerBucket,
                                         final File testOutputDirectory,
                                         final MavenProject mavenProject, final String jsonOutputFile,
                                         final String projectRoot,
                                         final boolean parallelMethods, final int maxNumberOfParallelRunners,
                                         final Set<String> includeTags, final Set<String> excludeTags) {
      super(enabled, maxMethodsPerBucket, testOutputDirectory, mavenProject, jsonOutputFile, projectRoot,
            parallelMethods, maxNumberOfParallelRunners);
      this.includeTags = includeTags;
      this.excludeTags = excludeTags;
   }

}
