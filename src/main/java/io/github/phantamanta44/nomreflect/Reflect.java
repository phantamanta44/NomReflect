package io.github.phantamanta44.nomreflect;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Reflect {

    /**
     * Constructs a filter from all loaded classes in the current thread's classloader.
     * @return A {@link TypeFilter} that matches all loaded classes.
     */
    public static TypeFilter types() {
        return new TypeFilter(new FastClasspathScanner(getClassloaderUrls()));
    }

    /**
     * Constructs a filter from all methods declared in all loaded classes in the current thread's classloader.
     * @return A {@link MethodFilter} that matches all loaded methods.
     */
    public static MethodFilter methods() {
        return new MethodFilter(new FastClasspathScanner(getClassloaderUrls()));
    }

    /**
     * Constructs a filter from all methods decalred in all loaded classes in the current thread's classloader.
     * @return A {@link FieldFilter} that matches all loaded fields.
     */
    public static FieldFilter fields() {
        return new FieldFilter(new FastClasspathScanner(getClassloaderUrls()));
    }

    /**
     * Constructs a filter from all classes loaded by the provided loader.
     * @param loader The classloader to scan.
     * @return A {@link TypeFilter} that matches all loaded classes.
     */
    public static TypeFilter types(ClassLoader loader) {
        return new TypeFilter(new FastClasspathScanner(getClassloaderUrls(loader)));
    }

    /**
     * Constructs a filter from all methods declared in classes loaded by the provided loader.
     * @param loader The classloader to scan.
     * @return A {@link MethodFilter} that matches all loaded methods.
     */
    public static MethodFilter methods(ClassLoader loader) {
        return new MethodFilter(new FastClasspathScanner(getClassloaderUrls(loader)));
    }

    /**
     * Constructs a filter from all fields declared in classes loaded by the provided loader.
     * @param loader The classloader to scan.
     * @return A {@link FieldFilter} that matches all loaded fields.
     */
    public static FieldFilter fields(ClassLoader loader) {
        return new FieldFilter(new FastClasspathScanner(getClassloaderUrls(loader)));
    }

    /**
     * Constructs a filter from all classes in the provided packages. This is non-recursive.
     * @param packages The packages to scan.
     * @return A {@link TypeFilter} that matches all scanned classes.
     */
    public static TypeFilter types(String... packages) {
        return new TypeFilter(new FastClasspathScanner(packages));
    }


    /**
     * Constructs a filter from all methods defined in classes in the provided packages. This is non-recursive.
     * @param packages The packages to scan.
     * @return A {@link MethodFilter} that matches all scanned methods.
     */
    public static MethodFilter methods(String... packages) {
        return new MethodFilter(new FastClasspathScanner(packages));
    }

    /**
     * Constructs a filter from all fields defined in classes in the provided packages. This is non-recursive.
     * @param packages The packages to scan.
     * @return A {@link FieldFilter} that matches all scanned fields.
     */
    public static FieldFilter fields(String... packages) {
        return new FieldFilter(new FastClasspathScanner(packages));
    }

    /**
     * Utility function that checks if a bitmask contains certain flags.
     * @param mask The bitmask to test for flags.
     * @param flags The bitmask defining the flags to test for.
     * @return Whether the mask contains the flags.
     */
    static boolean hasFlags(int mask, int flags) {
        return mask & flags == flags;
    }

    /**
     * Utility function that takes an array of bitmasks and uses bitwise or to fold them into a single mask;
     * @param flags The bitmasks to fold.
     * @return Folded masks.
     */
    static int foldFlags(int[] flags) {
        return Arrays.stream(flags).reduce(0, (a, b) -> a | b);
    }

    /**
     * Utility function that checks if an array contains at least all the elements of another array.
     * @param container The array to check for elements.
     * @param objects The array of elements to check for.
     * @return Whether the array contains the elements.
     */
    static boolean containsAll(Object[] container, Object... objects) {
        return Arrays.asList(container).containsAll(Arrays.asList(objects));
    }

    /**
     * Utility function that gets all URLs from the current thread's {@link URLClassLoader}.
     * @return The URLs, mapped to strings.
     */
    private static String[] getClassloaderUrls() {
        return getClassloaderUrls(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Utility function that gets all URLs from the provided {@link URLClassLoader}s.
     * @param loaders ClassLoaders to get URLs from.
     * @return The URLs, mapped to strings.
     */
    private static String[] getClassloaderUrls(ClassLoader... loaders) {
        Set<URL> urls = new HashSet<>();
        for (ClassLoader topLoader : loaders) {
            ClassLoader loader = topLoader;
            while (loader != null) {
                if (loader instanceof URLClassLoader)
                    Collections.addAll(urls, ((URLClassLoader) loader).getURLs());
                loader = loader.getParent();
            }
        }
        return urls.stream().map(URL::toExternalForm).toArray(String[]::new);
    }

}
