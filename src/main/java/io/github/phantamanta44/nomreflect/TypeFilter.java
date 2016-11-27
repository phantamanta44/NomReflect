package io.github.phantamanta44.nomreflect;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class TypeFilter extends MemberFilter<Class<?>> {

    /**
     * Constructs a TypeFilter that scans classes in the given scanner.
     * @param scanner The scanner.
     */
    TypeFilter(FastClasspathScanner scanner) {
        super(scanner);
    }

    /**
     * Constructs a TypeFilter that acts as a pipeline segment to the given parent.
     * @param parent This pipeline segment's parent.
     * @param test The predicate to test types flowing through this segment.
     */
    TypeFilter(MemberFilter<Class<?>> parent, Predicate<Class<?>> test) {
        super(parent, test);
    }

    @Override
    Stream<Class<?>> accumulate() {
        Set<Class<?>> types = new HashSet<>();
        getScanner().scan().getNamesOfAllClasses().forEach(cn -> {
            try {
                types.add(Class.forName(cn));
            } catch (NoClassDefFoundError | ClassNotFoundException | ExceptionInInitializerError ignored) { }
        });
        return types.stream();
    }

    /**
     * Checks if types are classes.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter classes() {
        return new TypeFilter(this, c -> !c.isInterface() && !c.isEnum());
    }

    /**
     * Checks if types are enums.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter enums() {
        return new TypeFilter(this, Class::isEnum);
    }

    /**
     * Checks if types extend a provided superclass.
     * @param superClass Superclass to test for.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter extending(Class<?> superClass) {
        return new TypeFilter(this, superClass::isAssignableFrom);
    }

    /**
     * Checks if types are interfaces.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter interfaces() {
        return new TypeFilter(this, Class::isInterface);
    }

    /**
     * Checks if types contain all the {@link java.lang.reflect.Modifier Modifier}s in a bitmask.
     * @param mods Modifier mask.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter mask(int mods) {
        return new TypeFilter(this, t -> Reflect.hasFlags(t.getModifiers(), mods));
    }

    /**
     * Checks if types contain all the {@link java.lang.reflect.Modifier Modifier}s provided.
     * @param mods Modifiers to test for.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter mod(int... mods) {
        return mask(Reflect.foldFlags(mods));
    }

    /**
     * Checks if types are named a provided name.
     * @param name Type name to test for.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter name(String name) {
        return new TypeFilter(this, t -> t.getName().equals(name));
    }

    /**
     * Checks if types are non-abstract (i.e. not an interface and not abstract).
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter nonAbstract() {
        return new TypeFilter(this, t -> !(t.isInterface() || Reflect.hasFlags(t.getModifiers(), Modifier.ABSTRACT)));
    }

    /**
     * Checks if types are a superclass of a provided type.
     * @param childClass Child type to test for.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter supering(Class<?> childClass) {
        return new TypeFilter(this, t -> t.isAssignableFrom(childClass));
    }

    /**
     * Checks if types are tagged with the provided annotations.
     * @param annotations Annotations to test for.
     * @return A new pipeline that filters out invalid types.
     */
    public TypeFilter tagged(Class<?>... annotations) {
        return new TypeFilter(this, t -> Arrays.stream(annotations).allMatch(a -> t.isAnnotationPresent((Class<? extends Annotation>)a)));
    }

}
