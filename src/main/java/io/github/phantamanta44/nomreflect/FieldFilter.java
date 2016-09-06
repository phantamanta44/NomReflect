package io.github.phantamanta44.nomreflect;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FieldFilter extends MemberFilter<Field> {

    /**
     * Constructs a FieldFilter that scans classes in the given scanner.
     * @param scanner The scanner.
     */
    FieldFilter(FastClasspathScanner scanner) {
        super(scanner);
    }

    /**
     * Constructs a FieldFilter that acts as a pipeline segment to the given parent.
     * @param parent This pipeline segment's parent.
     * @param test The predicate to test fields flowing through this segment.
     */
    FieldFilter(MemberFilter<Field> parent, Predicate<Field> test) {
        super(parent, test);
    }

    @Override
    Stream<Field> accumulate() {
        Set<Class> types = new HashSet<>();
        getScanner().matchAllClasses(types::add).scan();
        return types.stream().flatMap(t -> Arrays.stream(t.getDeclaredFields()));
    }

    /**
     * Checks if fields contain all the {@link java.lang.reflect.Modifier Modifier}s in a bitmask.
     * @param mods Modifier mask.
     * @return A new pipeline that filters out invalid fields.
     */
    public FieldFilter mask(int mods) {
        return new FieldFilter(this, m -> Reflect.hasFlags(m.getModifiers(), mods));
    }

    /**
     * Checks if fields contain all the {@link java.lang.reflect.Modifier Modifier}s provided.
     * @param mods Modifiers to test for.
     * @return A new pipeline that filters out invalid fields.
     */
    public FieldFilter mod(int... mods) {
        return mask(Reflect.foldFlags(mods));
    }

    /**
     * Checks if fields are named a provided name.
     * @param name Field name to test for.
     * @return A new pipeline that filters out invalid fields.
     */
    public FieldFilter name(String name) {
        return new FieldFilter(this, m -> m.getName().equals(name));
    }

    /**
     * Checks if fields are tagged with the provided annotations.
     * @param annotations Annotations to test for.
     * @return A new pipeline that filters out invalid fields.
     */
    public FieldFilter tagged(Class<?>... annotations) {
        return new FieldFilter(this, m -> Reflect.containsAll(m.getAnnotations(), (Object[])annotations));
    }

    /**
     * Checks if fields are of the provided type.
     * @param fieldType Field type to check for.
     * @return A new pipeline that filters out invalid fields.
     */
    public FieldFilter type(Class<?> fieldType) {
        return new FieldFilter(this, f -> f.getType().equals(fieldType));
    }

}
