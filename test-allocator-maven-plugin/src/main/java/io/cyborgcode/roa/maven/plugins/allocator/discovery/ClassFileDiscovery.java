package io.cyborgcode.roa.maven.plugins.allocator.discovery;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Utility class for discovering compiled Java class files within a directory.
 *
 * <p>This class provides methods to:
 * <ul>
 *   <li>Recursively search for `.class` files in a given base directory.</li>
 *   <li>Convert `.class` file paths into fully qualified class names.</li>
 * </ul>
 *
 * <p>It is designed to assist in test discovery and execution strategies
 * in a Maven-based test automation setup.
 *
 * <p>This class cannot be instantiated.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public final class ClassFileDiscovery {

   /**
    * Private constructor to prevent instantiation.
    */
   private ClassFileDiscovery() {
   }

   /**
    * Recursively searches for all `.class` files within the specified directory.
    *
    * @param baseDir The base directory to search for class files.
    * @return A list of `.class` files found in the directory and its subdirectories.
    */
   public static List<File> findClassFiles(File baseDir) {
      List<File> results = new ArrayList<>();
      Queue<File> queue = new LinkedList<>();
      queue.add(baseDir);

      while (!queue.isEmpty()) {
         File current = queue.poll();

         if (current.isDirectory()) {
            File[] children = current.listFiles();
            if (children != null) {
               Collections.addAll(queue, children);
            }
         } else if (current.getName().endsWith(".class")) {
            results.add(current);
         }
      }

      return results;
   }

   /**
    * Converts a `.class` file path into a fully qualified class name.
    *
    * <p>Example: If the base directory is `/path/to/classes/`
    * and a class file is found at `/path/to/classes/com/example/MyClass.class`,
    * this method will return `com.example.MyClass`.
    *
    * @param classFile The `.class` file.
    * @param baseDir   The base directory containing the class files.
    * @return The fully qualified class name derived from the file path.
    */
   public static String fileToClassName(File classFile, File baseDir) {
      String absolutePath = classFile.getAbsolutePath();
      String basePath = baseDir.getAbsolutePath();

      return absolutePath.substring(basePath.length() + 1)
            .replace(File.separatorChar, '.')
            .replace(".class", "");
   }

}
