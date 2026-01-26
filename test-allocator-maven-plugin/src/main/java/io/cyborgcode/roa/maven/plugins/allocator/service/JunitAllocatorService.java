package io.cyborgcode.roa.maven.plugins.allocator.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfigurationJunit;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.ClassFileDiscovery;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.TestClassLoader;
import io.cyborgcode.roa.maven.plugins.allocator.filtering.TestMethodFilter;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.maven.plugin.logging.Log;

/**
 * Allocates JUnit test classes into execution groups based on method count and filtering rules.
 *
 * <p>This service extends {@link BaseAllocatorService} and applies JUnit-specific filtering rules,
 * such as including/excluding test methods based on tags and handling parallel execution settings.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
@SuppressWarnings("java:S1452")
public class JunitAllocatorService extends BaseAllocatorService {

   /**
    * Constructs a new {@code JUnitAllocatorService} instance.
    *
    * @param log The Maven logger instance for recording allocation details.
    */
   public JunitAllocatorService(final Log log) {
      super(log);
   }

   /**
    * Calculates the number of matching test methods per JUnit test class.
    *
    * <p>This method:
    * <ul>
    *   <li>Retrieves the test classes from the provided list of class files.</li>
    *   <li>Loads each test class using the {@link TestClassLoader}.</li>
    *   <li>Filters test methods based on include/exclude tags.</li>
    *   <li>Determines if the test class should be treated as parallel or sequential.</li>
    *   <li>Returns a map of class names and their respective test method counts.</li>
    * </ul>
    *
    * @param classFiles      List of test class files.
    * @param testClassLoader The test class loader used to dynamically load test classes.
    * @param config          The JUnit-specific test allocation configuration.
    * @return A mapping of test class names to their number of executable test methods.
    */
   @Override
   @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST")
   public Map<String, Integer> calculateClassMethodCounts(
         List<File> classFiles,
         TestClassLoader testClassLoader,
         TestSplitterConfiguration config
   ) {
      TestSplitterConfigurationJunit configJunit = (TestSplitterConfigurationJunit) config;

      return classFiles.stream()
            .map(cf -> ClassFileDiscovery.fileToClassName(cf, configJunit.getTestOutputDirectory()))
            .map(className -> {
               Class<?> clazz = testClassLoader.loadClass(className);
               if (clazz == null) {
                  return null;
               }

               int matchingCount = TestMethodFilter.countMatchingTestMethods(
                     clazz,
                     configJunit.getIncludeTags(),
                     configJunit.getExcludeTags(),
                     configJunit.isParallelMethods()
               );

               return matchingCount > 0 ? Map.entry(className, matchingCount) : null;
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
   }

}
