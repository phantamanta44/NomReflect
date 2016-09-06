package io.github.phantamanta44.nomreflect;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class MethodFilter extends MemberFilter<Method> {

    /**
     * Constructs a MethodFilter that scans classes in the given scanner.
     * @param scanner The scanner.
     */
    MethodFilter(FastClasspathScanner scanner) {
        super(scanner);
    }

    /**
     * Constructs a MethodFilter that acts as a pipeline segment to the given parent.
     * @param parent This pipeline segment's parent.
     * @param test The predicate to test methods flowing through this segment.
     */
    MethodFilter(MemberFilter<Method> parent, Predicate<Method> test) {
        super(parent, test);
    }

    @Override
    Stream<Method> accumulate() {
        Set<Class> types = new HashSet<>();
        getScanner().matchAllClasses(types::add).scan();
        return types.stream().flatMap(t -> Arrays.stream(t.getDeclaredMethods()));
    }

    /**
     * Checks if methods contain all the {@link java.lang.reflect.Modifier Modifier}s in a bitmask.
     * @param mods Modifier mask.
     * @return A new pipeline that filters out invalid methods.
     */
    public MethodFilter mask(int mods) {
        return new MethodFilter(this, m -> Reflect.hasFlags(m.getModifiers(), mods));
    }

    /**
     * Checks if methods contain all the {@link java.lang.reflect.Modifier Modifier}s provided.
     * @param mods Modifiers to test for.
     * @return A new pipeline that filters out invalid methods.
     */
    public MethodFilter mod(int... mods) {
        return mask(Reflect.foldFlags(mods));
    }

    /**
     * Checks if methods are named a provided name.
     * @param name Method name to test for.
     * @return A new pipeline that filters out invalid methods.
     */
    public MethodFilter name(String name) {
        return new MethodFilter(this, m -> m.getName().equals(name));
    }

    /**
     * Checks if methods accept the given parameter types.
     * @param paramTypes Parameter types to test for.
     * @return A new pipeline that filters out invalid methods.
     */
    public MethodFilter params(Class<?>... paramTypes) {
        return new MethodFilter(this, m -> Arrays.equals(m.getParameterTypes(), paramTypes));
    }

    /**
     * Checks if methods return the given type.
     * @param returnType Return type to test for.
     * @return A new pipeline that filters out invalid methods.
     */
    public MethodFilter returns(Class<?> returnType) {
        return new MethodFilter(this, m -> m.getReturnType().equals(returnType));
    }

    /**
     * Checks if methods are tagged with the provided annotations.
     * @param annotations Annotations to test for.
     * @return A new pipeline that filters out invalid methods.
     */
    public MethodFilter tagged(Class<?>... annotations) {
        return new MethodFilter(this, m -> Reflect.containsAll(m.getAnnotations(), (Object[])annotations));
    }

}
