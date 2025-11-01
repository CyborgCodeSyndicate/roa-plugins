package io.cyborgcode.roa.maven.plugins.allocator.config;

import java.io.File;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.apache.maven.project.MavenProject;

/**
 * Configuration class for test splitting in a Maven project.
 *
 * <p>This class holds various settings related to test allocation, including:
 * <ul>
 *   <li>Enabling or disabling test splitting.</li>
 *   <li>Defining the maximum number of methods per test bucket.</li>
 *   <li>Specifying the test output directory.</li>
 *   <li>Providing the Maven project reference.</li>
 *   <li>Setting the JSON output file name.</li>
 *   <li>Indicating the root directory of the project.</li>
 *   <li>Determining if tests should be executed in parallel.</li>
 *   <li>Setting the maximum number of parallel test runners.</li>
 * </ul>
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
@Getter
@SuperBuilder
public class TestSplitterConfiguration {

   /**
    * Indicates whether test splitting is enabled.
    */
   private final boolean enabled;

   /**
    * Maximum number of test methods allowed per test bucket.
    */
   private final int maxMethodsPerBucket;

   /**
    * Directory where test outputs are stored.
    */
   private final File testOutputDirectory;

   /**
    * The associated Maven project.
    */
   private final MavenProject mavenProject;

   /**
    * Name of the output JSON file containing the test groups.
    */
   private final String jsonOutputFile;

   /**
    * Root directory of the project.
    */
   private final String projectRoot;

   /**
    * Indicates whether test methods should be executed in parallel.
    */
   private final boolean parallelMethods;

   /**
    * Maximum number of parallel test runners allowed.
    */
   @Setter
   private int maxNumberOfParallelRunners;

}
