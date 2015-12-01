package li.cil.tis3d.system.module.execution.target;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valid sources and sinks for move operations.
 */
public enum Target {
    ACC,
    BAK,
    NIL,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    ANY,
    LAST;

    public static final Set<Target> VALID_TARGETS = Arrays.asList(Target.values()).stream().filter(t -> t != BAK).collect(Collectors.toSet());
}
