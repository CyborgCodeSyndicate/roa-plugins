package io.cyborgcode.roa.maven.plugins.allocator.config;

import java.io.File;
import java.util.Set;
import lombok.Getter;
import org.apache.maven.project.MavenProject;

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
public class TestSplitterConfigurationTestng extends TestSplitterConfiguration {

   /**
    * Set of TestNG suites to be executed.
    */
   private final Set<String> suites;

   /**
    * Constructs a new {@code TestSplitterConfigurationTestng} instance.
    *
    * @param enabled                    Whether test splitting is enabled.
    * @param maxMethodsPerBucket        Maximum number of test methods per bucket.
    * @param testOutputDirectory        Directory where test outputs are stored.
    * @param mavenProject               The associated Maven project.
    * @param jsonOutputFile             Name of the output JSON file containing test groups.
    * @param projectRoot                Root directory of the project.
    * @param parallelMethods            Whether test methods should be executed in parallel.
    * @param maxNumberOfParallelRunners Maximum number of parallel test runners allowed.
    * @param suites                     Set of TestNG suite names to be executed.
    */
   public TestSplitterConfigurationTestng(final boolean enabled, final int maxMethodsPerBucket,
                                          final File testOutputDirectory,
                                          final MavenProject mavenProject, final String jsonOutputFile,
                                          final String projectRoot,
                                          final boolean parallelMethods, final int maxNumberOfParallelRunners,
                                          final Set<String> suites) {
      super(enabled, maxMethodsPerBucket, testOutputDirectory, mavenProject, jsonOutputFile, projectRoot,
            parallelMethods, maxNumberOfParallelRunners);
      this.suites = suites;
   }

}
