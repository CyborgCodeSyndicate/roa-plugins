package io.cyborgcode.roa.maven.plugins.allocator.service;

import io.cyborgcode.roa.maven.plugins.allocator.config.TestSplitterConfiguration;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Interface for test allocation services that distribute test classes across execution groups.
 *
 * <p>Implementations of this interface define the logic for dynamically splitting test methods
 * across multiple execution units (e.g., parallel test runs). This ensures optimal test
 * execution efficiency, reducing overall test suite runtime.
 *
 * <p>Implementations may handle different test engines such as JUnit or TestNG and apply
 * filtering, sorting, and bucketing strategies for test distribution.
 *
 * @author Cyborg Code Syndicate 💍👨💻
 */
public interface TestAllocatorService {

   /**
    * Allocates tests into execution groups based on the provided configuration.
    *
    * <p>Implementations will analyze test classes and methods, apply relevant filters,
    * and distribute them into appropriate test buckets for execution.
    *
    * @param configuration The test allocation configuration containing settings such as
    *                      test engine, parallel execution options, and method bucketing rules.
    * @throws MojoExecutionException If test allocation fails due to an error.
    */
   void allocateTests(TestSplitterConfiguration configuration) throws MojoExecutionException;

}
