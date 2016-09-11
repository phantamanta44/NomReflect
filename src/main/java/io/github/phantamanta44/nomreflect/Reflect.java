package io.github.phantamanta44.nomreflect;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public class Reflect {

    /**
     * Constructs a filter from all loaded classes in the current thread's classloader.
     * @return A {@link TypeFilter} that matches all loaded classes.
     */
    public static TypeFilter types() {
        return new TypeFilter(new FastClasspathScanner(getJavaClasspathUrls()));
    }

    /**
     * Constructs a filter from all methods declared in all loaded classes in the current thread's classloader.
     * @return A {@link MethodFilter} that matches all loaded methods.
     */
    public static MethodFilter methods() {
        return new MethodFilter(new FastClasspathScanner(getJavaClasspathUrls()));
    }

    /**
     * Constructs a filter from all methods decalred in all loaded classes in the current thread's classloader.
     * @return A {@link FieldFilter} that matches all loaded fields.
     */
    public static FieldFilter fields() {
        return new FieldFilter(new FastClasspathScanner(getJavaClasspathUrls()));
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
        return (mask & flags) == flags;
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
        return urls.stream().flatMap(Reflect::packagesFrom).toArray(String[]::new);
    }

    /**
     * Utility function that gets all packages in the Java classpath.
     * @return The packages retrieved.
     */
    private static String[] getJavaClasspathUrls() {
        Set<URL> urls = new HashSet<>();
        for (String path : System.getProperty("java.class.path").split(File.pathSeparator)) {
            try {
                urls.add(new File(path).toURI().toURL());
            } catch (Exception ignored) { }
        }
        return urls.stream().flatMap(Reflect::packagesFrom).toArray(String[]::new);
    }

    /**
     * Utility function that retrieves all packages in a jarfile.
     * @param path URL locating the jartile.
     * @return The packages retrieved.
     */
    private static Stream<String> packagesFrom(URL path) {
        try {
            List<String> packages = new ArrayList<>();
            Enumeration<JarEntry> entries = new JarFile(path.getFile()).entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.isDirectory() && !entry.getName().startsWith("META-INF/") && !entry.getName().startsWith("java/"))
                    packages.add(entry.getName().replace('/', '.'));
            }
            return packages.stream().map(s -> s.substring(0, s.length() - 1));
        } catch (Exception e) {
            return Stream.of();
        }
    }

}
