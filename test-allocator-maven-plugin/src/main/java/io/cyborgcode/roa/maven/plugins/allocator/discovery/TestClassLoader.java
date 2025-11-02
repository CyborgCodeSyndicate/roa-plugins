package io.cyborgcode.roa.maven.plugins.allocator.discovery;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A custom class loader for loading test classes from the project's compiled output directories.
 *
 * <p>This class constructs a {@link URLClassLoader} using the test and compile classpath elements
 * from the provided Maven project configuration. It is used for dynamically loading test classes
 * for test allocation and execution purposes.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public final class TestClassLoader {

   /**
    * The URL-based class loader for loading test classes dynamically.
    */
   private final URLClassLoader classLoader;

   /**
    * Private constructor; assumes the URLClassLoader is already safely created.
    */
   private TestClassLoader(URLClassLoader classLoader) {
      this.classLoader = classLoader;
   }

   /**
    * Creates a new {@code TestClassLoader} from the given configuration.
    *
    * <p>This method performs all operations that may fail (missing Maven deps, bad URLs) and
    * wraps them into an unchecked exception, keeping the actual constructor simple so static
    * analyzers do not complain about throwing from a constructor.
    *
    * @param config the test splitter configuration
    * @return a fully initialized {@code TestClassLoader}
    * @throws IllegalStateException if the classloader cannot be created
    */
   public static TestClassLoader from(TestSplitterConfiguration config) {
      try {
         URLClassLoader cl = createClassLoader(config);
         return new TestClassLoader(cl);
      } catch (IllegalStateException e) {
         // re-throw with a clearer top-level context (optional)
         throw new IllegalStateException("Failed to create TestClassLoader", e);
      }
   }

   /**
    * Creates a new {@link URLClassLoader} with classpath elements from the provided configuration.
    *
    * @param config The test splitter configuration containing Maven project information.
    * @return A new {@link URLClassLoader} instance.
    */
   private static URLClassLoader createClassLoader(TestSplitterConfiguration config) {
      final Set<URI> uris = new LinkedHashSet<>();

      final List<String> testElements;
      final List<String> compileElements;
      try {
         testElements = config.getMavenProject().getTestClasspathElements();
         compileElements = config.getMavenProject().getCompileClasspathElements();
      } catch (org.apache.maven.artifact.DependencyResolutionRequiredException e) {
         throw new IllegalStateException(
               "Cannot create class loader: Maven dependencies are not resolved for test/compile classpaths.",
               e
         );
      }

      addUrisFromPaths(testElements, uris);
      addUrisFromPaths(compileElements, uris);

      final URL[] urlArray = uris.stream()
            .map(uri -> {
               try {
                  return uri.toURL();
               } catch (MalformedURLException e) {
                  throw new IllegalStateException("Invalid classpath entry: " + uri, e);
               }
            })
            .toArray(URL[]::new);

      return new URLClassLoader(urlArray, TestClassLoader.class.getClassLoader());
   }

   private static void addUrisFromPaths(List<String> paths, Set<URI> urls) {
      if (paths == null) {
         return;
      }
      for (String path : paths) {
         if (path != null && !path.isBlank()) {
            urls.add(new File(path).toURI());
         }
      }
   }

   /**
    * Attempts to load a class by its fully qualified name.
    *
    * @param className The fully qualified name of the class to load.
    * @return The {@code Class} object if found, or {@code null} if the class cannot be loaded.
    */
   public Class<?> loadClass(String className) {
      try {
         return classLoader.loadClass(className);
      } catch (ClassNotFoundException | NoClassDefFoundError e) {
         return null;
      }
   }
}
