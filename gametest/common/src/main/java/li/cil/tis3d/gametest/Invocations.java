package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Port;
import net.minecraft.gametest.framework.GameTestAssertException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Since we can't have nice things (Mockito) in game tests, we use manual counters to check for invocations.
 */
public final class Invocations {
    public enum Kind {
        WRITE,
        CANCEL,
        READ,
        WRITE_COMPLETE,
        ENABLED,
        DISABLED,
        INFRARED,
        SKIP,
        RESET,
    }

    public record Invocation(int step, Kind kind, @Nullable Port port, int value) {
        @Override
        public String toString() {
            return "step " + step + ": " + kind + (port != null ? "(" + port + ")" : "") + " = " + value;
        }
    }

    private final List<Invocation> invocations = new CopyOnWriteArrayList<>();

    // --------------------------------------------------------------------- //
    // Recording

    void record(final int step, final Kind kind, @Nullable final Port port, final int value) {
        invocations.add(new Invocation(step, kind, port, value));
    }

    void record(final int step, final Kind kind) {
        record(step, kind, null, 0);
    }

    void clear() {
        invocations.clear();
    }

    // --------------------------------------------------------------------- //
    // Queries

    public List<Invocation> all() {
        return List.copyOf(invocations);
    }

    public List<Invocation> of(final Kind kind) {
        return invocations.stream().filter(invocation -> invocation.kind() == kind).toList();
    }

    public List<Invocation> of(final Kind kind, final Port port) {
        return invocations.stream()
            .filter(invocation -> invocation.kind() == kind && invocation.port() == port)
            .toList();
    }

    public int count(final Kind kind) {
        return of(kind).size();
    }

    public int count(final Kind kind, final Port port) {
        return of(kind, port).size();
    }

    public boolean any(final Kind kind) {
        return count(kind) > 0;
    }

    public boolean any(final Kind kind, final Port port) {
        return count(kind, port) > 0;
    }

    public Invocation only(final Kind kind, final Port port) {
        return only(of(kind, port), kind + " on " + port);
    }

    public Invocation only(final Kind kind) {
        return only(of(kind), kind.toString());
    }

    public Invocation first(final Kind kind, final Port port) {
        return first(of(kind, port), kind + " on " + port);
    }

    public Invocation first(final Kind kind) {
        return first(of(kind), kind.toString());
    }

    public void assertInvoked(final Kind kind, final int value) {
        if (of(kind).stream().noneMatch(invocation -> invocation.value() == value)) {
            throw new GameTestAssertException("no " + kind + " with value " + value + " in " + describe());
        }
    }

    public void assertNeverInvoked(final Kind kind) {
        if (any(kind)) {
            throw new GameTestAssertException("expected no " + kind + ", got " + describe());
        }
    }

    // --------------------------------------------------------------------- //

    private Invocation first(final List<Invocation> matches, final String what) {
        if (matches.isEmpty()) {
            throw new GameTestAssertException("expected at least one " + what + ", got " + describe());
        }
        return matches.getFirst();
    }

    private Invocation only(final List<Invocation> matches, final String what) {
        if (matches.size() != 1) {
            throw new GameTestAssertException("expected exactly one " + what + ", got "
                + matches.size() + " in " + describe());
        }
        return matches.getFirst();
    }

    private String describe() {
        return invocations.isEmpty()
            ? "[nothing recorded]"
            : invocations.stream().map(Invocation::toString).collect(Collectors.joining("; ", "[", "]"));
    }
}
