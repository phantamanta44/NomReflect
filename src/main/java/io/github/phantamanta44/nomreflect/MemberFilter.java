package io.github.phantamanta44.nomreflect;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class MemberFilter<M> {

    private final MemberFilter<M> parent;
    private final Predicate<M> test;
    private final FastClasspathScanner scanner;

    private Set<M> results;

    /**
     * Constructs a MemberFilter that uses the provided scanner.
     * @param scanner The scanner to use.
     */
    MemberFilter(FastClasspathScanner scanner) {
        this.parent = null;
        this.test = m -> true;
        this.scanner = scanner;
    }

    /**
     * Constructs a MemberFilter that acts as a pipeline segment.
     * @param parent The parent pipeline segment.
     * @param test The predicate to test members flowing through this segment.
     */
    MemberFilter(MemberFilter<M> parent, Predicate<M> test) {
        this.parent = parent;
        this.test = test;
        this.scanner = null;
    }

    /**
     * Finds all members matching the provided filters. This terminates the filter, meaning you won't be able to rescan using the same filter.
     * @return The result set.
     */
    public Set<M> find() {
        if (results == null)
            results = accumulate().filter(this::test).collect(Collectors.toSet());
        return results;
    }

    /**
     * Checks if at least one member matches the provided filters. This terminates the filter, meaning you won't be able to rescan using the same filter.
     * @return Whether a match exists or not.
     */
    public boolean match() {
        return !find().isEmpty();
    }

    /**
     * Returns the {@link FastClasspathScanner} that this filter is using to scan for members.
     * @return The scanner.
     */
    FastClasspathScanner getScanner() {
        return scanner != null ? scanner : parent.getScanner();
    }

    /**
     * Accumulates the appropriate members from the scanner.
     * @return The accumulated members.
     */
    abstract Stream<M> accumulate();

    /**
     * Checks to see if a member matches the filters in this pipeline.
     * @param member The member to test.
     * @return Whether the member matches or not.
     */
    boolean test(M member) {
        return test.test(member) && (parent == null || parent.test(member));
    }

}
