package li.cil.tis3d.common.module.execution.target;

import li.cil.tis3d.api.machine.Port;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Valid sources and sinks for move operations.
 */
public enum Target {
    PC,
    ACC,
    BAK,
    NIL,
    LEFT,
    RIGHT,
    UP,
    DOWN,
    ANY,
    LAST;

    public static final Set<Target> VALID_TARGETS = Arrays.stream(Target.values()).filter(t -> t != BAK).collect(Collectors.toSet());

    public static final Port[] TO_PORT = new Port[]{Port.UP, Port.UP, Port.UP, Port.LEFT, Port.RIGHT, Port.UP, Port.DOWN, Port.UP, Port.UP};

    public static final Target[] FROM_PORT = new Target[]{Target.LEFT, Target.RIGHT, Target.UP, Target.DOWN};

    public static Port toPort(final Target target) {
        return TO_PORT[target.ordinal()];
    }

    public static Target fromPort(final Port port) {
        return FROM_PORT[port.ordinal()];
    }
}
