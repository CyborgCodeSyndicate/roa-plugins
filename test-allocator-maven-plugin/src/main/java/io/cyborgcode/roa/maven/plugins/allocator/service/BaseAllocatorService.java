package io.cyborgcode.roa.maven.plugins.allocator.service;

import com.google.gson.Gson;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.ClassFileDiscovery;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.TestClassLoader;
import io.cyborgcode.roa.maven.plugins.allocator.grouping.TestBucket;
import io.cyborgcode.roa.maven.plugins.allocator.grouping.TestBucketAllocator;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

/**
 * Abstract base class for test allocation services.
 *
 * <p>This class provides a template for allocating test classes into execution groups.
 * It discovers test classes, calculates the number of test methods, and organizes
 * them into execution buckets to optimize test suite performance.
 *
 * <p>Implementing classes must define how test method counts are calculated
 * for different test engines, such as JUnit or TestNG.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public abstract class BaseAllocatorService implements TestAllocatorService {

   /**
    * Logger instance for recording allocation process details.
    */
   private final Log log;

   /**
    * Constructs a new {@code BaseAllocatorService} instance.
    *
    * @param log The Maven logger instance for recording events.
    */
   protected BaseAllocatorService(final Log log) {
      this.log = log;
   }

   /**
    * Allocates test classes into execution buckets based on the given configuration.
    *
    * <p>This method:
    * <ul>
    *   <li>Loads test classes from the specified test output directory.</li>
    *   <li>Counts test methods per class.</li>
    *   <li>Groups test classes into execution buckets based on the configuration.</li>
    *   <li>Writes the allocated test groups to a JSON output file.</li>
    * </ul>
    *
    * @param configuration The test allocation configuration.
    * @throws MojoExecutionException If an error occurs during test allocation.
    */
   @Override
   public void allocateTests(final TestSplitterConfiguration configuration) throws MojoExecutionException {
      TestClassLoader testClassLoader = TestClassLoader.from(configuration);
      List<File> classFiles = ClassFileDiscovery.findClassFiles(configuration.getTestOutputDirectory());

      log.info("[TestSplitter] Found " + classFiles.size() + " class files.");

      Map<String, Integer> classMethodCounts = calculateClassMethodCounts(
            classFiles,
            testClassLoader,
            configuration
      );

      int classSize = classMethodCounts.size();
      log.info("[TestSplitter] classMethodCount size=" + classSize);

      List<TestBucket> buckets;

      if (classSize <= configuration.getMaxNumberOfParallelRunners()) {
         buckets = new ArrayList<>();
         classMethodCounts.forEach((key, value) ->
               buckets.add(new TestBucket(List.of(key), value))
         );
      } else {
         buckets = TestBucketAllocator.groupClasses(
               classMethodCounts,
               configuration.getMaxMethodsPerBucket()
         );
      }

      writeGroupedTestsToFile(buckets, configuration.getJsonOutputFile());
   }

   /**
    * Calculates the number of test methods in each test class.
    *
    * <p>Implementing classes must define this method to analyze test classes
    * and determine the number of test methods they contain.
    *
    * @param classFiles      List of test class files.
    * @param testClassLoader The test class loader for dynamically loading test classes.
    * @param config          The test allocation configuration.
    * @return A mapping of class names to the number of test methods they contain.
    */
   public abstract Map<String, Integer> calculateClassMethodCounts(List<File> classFiles,
                                                                   TestClassLoader testClassLoader,
                                                                   TestSplitterConfiguration config);

   /**
    * Writes the grouped test allocation results to a JSON file.
    *
    * @param buckets    List of test execution buckets.
    * @param outPutFile The output file path for storing test allocation details.
    * @throws MojoExecutionException If an error occurs while writing to the file.
    */
   private void writeGroupedTestsToFile(List<TestBucket> buckets, String outPutFile) throws MojoExecutionException {
      List<Map<String, Object>> output = new ArrayList<>();
      for (int i = 0; i < buckets.size(); i++) {
         TestBucket b = buckets.get(i);
         Map<String, Object> jobObj = new HashMap<>();
         jobObj.put("jobIndex", i);
         jobObj.put("classes", b.getClassNames());
         jobObj.put("totalMethods", b.getTotalMethods());
         output.add(jobObj);
      }

      Path outPath = Paths.get(outPutFile + ".json");
      try {
         Path parent = outPath.getParent();
         if (parent != null) {
            Files.createDirectories(parent);
         }

         try (Writer writer = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
            new Gson().toJson(output, writer);
         }
      } catch (IOException e) {
         throw new MojoExecutionException("Failed to write grouped-tests.json", e);
      }

      log.info("[TestSplitter] Wrote " + outPath.toAbsolutePath());
   }

}
