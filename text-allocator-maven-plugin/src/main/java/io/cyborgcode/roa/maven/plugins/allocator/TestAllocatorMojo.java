package io.cyborgcode.roa.maven.plugins.allocator;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfigurationJunit;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfigurationTestng;
import io.cyborgcode.roa.maven.plugins.allocator.service.JunitAllocatorService;
import io.cyborgcode.roa.maven.plugins.allocator.service.TestAllocatorService;
import io.cyborgcode.roa.maven.plugins.allocator.service.TestNgAllocatorService;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin that allocates and splits tests into multiple execution groups.
 *
 * <p>This plugin helps distribute test methods across multiple execution buckets
 * for parallel execution based on the selected test engine (JUnit or TestNG).
 *
 * <p>Supports filtering by tags (JUnit) and suite names (TestNG), and allows
 * configuring the number of methods per group and parallel execution settings.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
@Mojo(
      name = "split",
      defaultPhase = LifecyclePhase.TEST_COMPILE,
      requiresDependencyResolution = ResolutionScope.TEST
)
public class TestAllocatorMojo extends AbstractMojo {

   /**
    * Enables or disables test splitting.
    */
   @Parameter(property = "testSplitter.enabled", defaultValue = "false")
   private boolean enabled;

   /**
    * Defines the test framework to use (JUnit or TestNG).
    */
   @Parameter(property = "testSplitter.test.engine", required = true, defaultValue = "junit")
   private String testEngine;

   /**
    * Comma-separated list of included JUnit tags.
    */
   @Parameter(property = "testSplitter.junit.tags.include")
   private String tagsInclude;

   /**
    * Comma-separated list of excluded JUnit tags.
    */
   @Parameter(property = "testSplitter.junit.tags.exclude")
   private String tagsExclude;

   /**
    * Comma-separated list of TestNG suite names.
    */
   @Parameter(property = "testSplitter.testng.suites")
   private String suites;

   /**
    * Maximum number of test methods per execution bucket.
    */
   @Parameter(property = "testSplitter.maxMethods", defaultValue = "20")
   private int maxMethods;

   /**
    * Directory containing compiled test classes.
    */
   @Parameter(defaultValue = "${project.build.testOutputDirectory}", readonly = true)
   private File testOutputDir;

   /**
    * Reference to the current Maven project.
    */
   @Parameter(defaultValue = "${project}", readonly = true)
   private MavenProject project;

   /**
    * Name of the output JSON file that stores the test allocation results.
    */
   @Parameter(property = "testSplitter.json.output", defaultValue = "grouped-tests")
   private String outputJsonFile;

   /**
    * Root directory of the project.
    */
   @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
   private File projectBaseDir;

   /**
    * Enables or disables parallel test execution.
    */
   @Parameter(property = "testSplitter.parallel.methods", defaultValue = "true", required = true)
   private Boolean parallelMethods;

   /**
    * Maximum number of parallel test runners.
    */
   @Parameter(property = "testSplitter.max.number.runners", defaultValue = "20", required = true)
   private int maxNumberOfParallelRunners;

   /**
    * Executes the test allocation process.
    *
    * @throws MojoExecutionException if an error occurs during test allocation.
    */
   @Override
   public void execute() throws MojoExecutionException {
      if (!enabled) {
         getLog().info("[TestSplitter] Disabled. Skipping.");
         return;
      }

      Pair<TestSplitterConfiguration, TestAllocatorService> pair = createConfigurationAndService();
      TestSplitterConfiguration config = pair.getLeft();
      TestAllocatorService testAllocatorService = pair.getRight();

      logConfiguration(config);
      testAllocatorService.allocateTests(config);
   }

   /**
    * Creates the appropriate test configuration and service based on the selected test engine.
    *
    * @return A pair containing the test configuration and the test allocation service.
    */
   private Pair<TestSplitterConfiguration, TestAllocatorService> createConfigurationAndService() {
      TestSplitterConfiguration config;
      TestAllocatorService service = switch (testEngine.toLowerCase()) {
         case "junit" -> {
            config = new TestSplitterConfigurationJunit(
                  enabled, maxMethods, testOutputDir, project, outputJsonFile,
                  projectBaseDir.getAbsolutePath(), parallelMethods, maxNumberOfParallelRunners,
                  parseInput(tagsInclude), parseInput(tagsExclude)
            );
            yield new JunitAllocatorService(getLog());
         }
         case "testng" -> {
            config = new TestSplitterConfigurationTestng(
                  enabled, maxMethods, testOutputDir, project, outputJsonFile,
                  projectBaseDir.getAbsolutePath(), this.parallelMethods, maxNumberOfParallelRunners, parseInput(suites)
            );
            yield new TestNgAllocatorService(getLog());
         }
         default -> throw new IllegalArgumentException(
               "Invalid test engine: " + testEngine + ". Supported: junit, testng."
         );
      };

      return new Pair<>(config, service);
   }


   private void logConfiguration(TestSplitterConfiguration config) {
      getLog().info("[TestSplitter] Starting test splitting...");
      getLog().info(String.format("[TestSplitter] testOutputDir = %s", config.getTestOutputDirectory()));
      getLog().info(String.format("[TestSplitter] testEngine = %s", testEngine));

      if (config instanceof TestSplitterConfigurationJunit junitConfig) {
         getLog().info(String.format("[TestSplitter] tagsInclude = %s", junitConfig.getIncludeTags()));
         getLog().info(String.format("[TestSplitter] tagsExclude = %s", junitConfig.getExcludeTags()));
      } else if (config instanceof TestSplitterConfigurationTestng testngConfig) {
         getLog().info(String.format("[TestSplitter] suites = %s", testngConfig.getSuites()));
      }

      getLog().info(String.format("[TestSplitter] maxMethods = %d", config.getMaxMethodsPerBucket()));
      getLog().info(String.format("[TestSplitter] outputJsonFile = %s", config.getJsonOutputFile()));
   }


   private static Set<String> parseInput(String input) {
      return Optional.ofNullable(input)
            .map(str -> str.split(","))
            .map(arr -> Arrays.stream(arr)
                  .map(String::trim)
                  .filter(s -> !s.isEmpty())
                  .collect(Collectors.toSet()))
            .orElse(new HashSet<>());
   }


   private static class Pair<L, R> {

      private final L left;
      private final R right;


      public Pair(L left, R right) {
         this.left = left;
         this.right = right;
      }


      public L getLeft() {
         return left;
      }


      public R getRight() {
         return right;
      }

   }

}