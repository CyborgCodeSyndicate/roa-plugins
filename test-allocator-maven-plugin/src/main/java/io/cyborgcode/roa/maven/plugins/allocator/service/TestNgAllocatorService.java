package io.cyborgcode.roa.maven.plugins.allocator.service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfigurationTestng;
import io.cyborgcode.roa.maven.plugins.allocator.discovery.TestClassLoader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.maven.plugin.logging.Log;
import org.testng.annotations.Test;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlInclude;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.testng.xml.internal.Parser;

/**
 * Allocates TestNG test classes into execution groups based on suite configurations and method count.
 *
 * <p>This service extends {@link BaseAllocatorService} and applies TestNG-specific logic for allocating test classes.
 * It parses TestNG XML suite files to determine which test methods belong to which suites and organizes them
 * accordingly into execution groups.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public class TestNgAllocatorService extends BaseAllocatorService {

   /**
    * Constructs a new {@code TestNgAllocatorService} instance.
    *
    * @param log The Maven logger instance for recording allocation details.
    */
   public TestNgAllocatorService(final Log log) {
      super(log);
   }

   /**
    * Calculates the number of matching test methods per TestNG test class.
    *
    * <p>This method:
    * <ul>
    *   <li>Parses TestNG suite XML files to identify relevant test classes.</li>
    *   <li>Loads each test class using the {@link TestClassLoader}.</li>
    *   <li>Counts the number of test methods within each class based on TestNG annotations.</li>
    *   <li>Filters test methods according to the suites specified in the configuration.</li>
    *   <li>Returns a map of class names and their respective test method counts.</li>
    * </ul>
    *
    * @param classFiles      List of test class files.
    * @param testClassLoader The test class loader used to dynamically load test classes.
    * @param config          The TestNG-specific test allocation configuration.
    * @return A mapping of test class names to their number of executable test methods.
    */
   @Override
   @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST")
   public Map<String, Integer> calculateClassMethodCounts(final List<File> classFiles,
                                                          final TestClassLoader testClassLoader,
                                                          final TestSplitterConfiguration config) {

      final TestSplitterConfigurationTestng configTestNg = (TestSplitterConfigurationTestng) config;
      final Set<String> suiteNames = configTestNg.getSuites();

      final Map<String, Integer> classMethodCounts = new HashMap<>();
      final List<File> suiteXmlFiles = findAllXmlFilesInProject(Paths.get(configTestNg.getProjectRoot()));

      for (File xmlFile : suiteXmlFiles) {
         try {
            final List<XmlSuite> xmlSuites = new Parser(xmlFile.getAbsolutePath()).parseToList();
            for (XmlSuite xmlSuite : xmlSuites) {
               if (suiteNames.contains(xmlSuite.getName())) {
                  processSuite(xmlSuite, testClassLoader, config, classMethodCounts);
               }
            }
         } catch (Exception e) {
            throw new IllegalStateException("Failed to parse TestNG suite file: " + xmlFile.getAbsolutePath(), e);
         }
      }

      return classMethodCounts;
   }



   /**
    * Processes a single TestNG {@link XmlSuite} and accumulates method counts
    * for its tests and classes that match the configured suite names.
    *
    * <p>Delegates traversal to {@link #processTest(XmlTest, TestClassLoader, TestSplitterConfiguration, Map)}.</p>
    *
    * @param suite   the parsed TestNG suite to process
    * @param loader  the class loader used to resolve test classes
    * @param config  splitter configuration (used for parallel-by-methods policy)
    * @param counts  mutable accumulator of class name -> test method count
    */
   private void processSuite(XmlSuite suite,
                             TestClassLoader loader,
                             TestSplitterConfiguration config,
                             Map<String, Integer> counts) {
      for (XmlTest xmlTest : suite.getTests()) {
         processTest(xmlTest, loader, config, counts);
      }
   }


   /**
    * Processes a single TestNG {@link XmlTest} and accumulates method counts
    * for each declared {@link XmlClass}.
    *
    * <p>Delegates class-level handling to
    * {@link #processClass(XmlClass, TestClassLoader, TestSplitterConfiguration, Map)}.</p>
    *
    * @param xmlTest the TestNG test block inside a suite
    * @param loader  the class loader used to resolve test classes
    * @param config  splitter configuration (used for parallel-by-methods policy)
    * @param counts  mutable accumulator of class name -> test method count
    */
   private void processTest(XmlTest xmlTest,
                            TestClassLoader loader,
                            TestSplitterConfiguration config,
                            Map<String, Integer> counts) {
      for (XmlClass xmlClass : xmlTest.getXmlClasses()) {
         processClass(xmlClass, loader, config, counts);
      }
   }

   /**
    * Resolves the given {@link XmlClass}, determines how many test methods
    * it contributes based on includes and configuration, and merges the count
    * into the provided accumulator.
    *
    * <ul>
    *   <li>If the class cannot be loaded, it is skipped silently (no change to counts).</li>
    *   <li>If includes are present, only matching {@code @Test} methods are counted.</li>
    *   <li>If no includes are present and {@code parallelMethods == false}, the class counts as 1.</li>
    *   <li>Otherwise, all declared {@code @Test} methods are counted.</li>
    * </ul>
    *
    * @param xmlClass the class declaration from the TestNG XML
    * @param loader   the class loader used to resolve the class
    * @param config   splitter configuration (uses {@code isParallelMethods()})
    * @param counts   mutable accumulator of class name -> test method count
    */
   private void processClass(XmlClass xmlClass,
                             TestClassLoader loader,
                             TestSplitterConfiguration config,
                             Map<String, Integer> counts) {
      final String className = xmlClass.getName();
      final Class<?> clazz = loader.loadClass(className);
      if (clazz == null) {
         return;
      }

      final List<XmlInclude> includes = xmlClass.getIncludedMethods();
      final int testCount = (includes != null && !includes.isEmpty())
            ? countIncludedMethods(clazz, includes)
            : countClassTests(clazz, config);

      counts.merge(className, testCount, Integer::sum);
   }

   /**
    * Counts how many included method names from the TestNG XML correspond to
    * declared methods on {@code clazz} that are annotated with {@link org.testng.annotations.Test @Test}.
    *
    * <p>Method name matching is exact (by {@link Method#getName()}).</p>
    *
    * @param clazz    the resolved test class
    * @param includes list of included method entries from the TestNG XML
    * @return the number of included methods that exist on the class and are annotated with {@code @Test}
    */
   private int countIncludedMethods(Class<?> clazz, List<XmlInclude> includes) {
      int matchedCount = 0;
      for (XmlInclude include : includes) {
         final String methodName = include.getName();
         for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && m.isAnnotationPresent(Test.class)) {
               matchedCount++;
            }
         }
      }
      return matchedCount;
   }

   /**
    * Computes the number of test methods contributed by {@code clazz} when no includes are specified.
    *
    * <ul>
    *   <li>If {@code config.isParallelMethods()} is {@code false}, returns {@code 1}
    *       (treat the class as a single execution unit).</li>
    *   <li>Otherwise, returns the count of declared methods annotated with
    *       {@link org.testng.annotations.Test @Test}.</li>
    * </ul>
    *
    * @param clazz  the resolved test class
    * @param config splitter configuration (uses {@code isParallelMethods()})
    * @return {@code 1} when not running in parallel-by-methods mode, otherwise the number of {@code @Test} methods
    */
   private int countClassTests(Class<?> clazz, TestSplitterConfiguration config) {
      if (!config.isParallelMethods()) {
         return 1;
      }

      int testCount = 0;
      for (Method m : clazz.getDeclaredMethods()) {
         if (m.isAnnotationPresent(Test.class)) {
            testCount++;
         }
      }
      return testCount;
   }

   /**
    * Scans the project directory for TestNG XML suite files.
    *
    * <p>This method searches for all `.xml` files under the given project root directory.
    * It is primarily used to locate TestNG suite configurations for test allocation.
    *
    * @param projectRoot The root directory of the project.
    * @return A list of {@link File} objects representing TestNG XML suite files.
    */
   private List<File> findAllXmlFilesInProject(Path projectRoot) {
      List<File> result = new ArrayList<>();
      try (Stream<Path> pathStream = Files.walk(projectRoot)) {
         pathStream
               .filter(Files::isRegularFile)
               .filter(p -> p.getFileName().toString().endsWith(".xml"))
               .forEach(p -> result.add(p.toFile()));
      } catch (IOException e) {
         throw new UncheckedIOException("Failed to traverse project root: " + projectRoot, e);
      }
      return result;
   }

}
