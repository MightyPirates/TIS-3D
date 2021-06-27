package li.cil.tis3d.common.module.execution;

import com.google.common.collect.ImmutableMap;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.ExecutionModule;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.common.module.execution.target.*;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Track machine state and ease communicating with ports of an execution module.
 */
public final class MachineImpl implements Machine {
    // --------------------------------------------------------------------- //
    // Persisted data
    private final MachineState state;

    // --------------------------------------------------------------------- //
    // Computed data

    private final ExecutionModule module;
    private final Map<Target, TargetInterface> interfaces;

    // --------------------------------------------------------------------- //

    public MachineImpl(final ExecutionModule module, final Face face) {
        this.state = new MachineState();
        this.module = module;
        this.interfaces = ImmutableMap.<Target, TargetInterface>builder().
            put(Target.ACC, new AccTargetInterface(this)).
            put(Target.BAK, new BakTargetInterface(this)).
            put(Target.NIL, new NilTargetInterface(this)).
            put(Target.LEFT, new SideTargetInterface(this, module, face, Port.LEFT)).
            put(Target.RIGHT, new SideTargetInterface(this, module, face, Port.RIGHT)).
            put(Target.UP, new SideTargetInterface(this, module, face, Port.UP)).
            put(Target.DOWN, new SideTargetInterface(this, module, face, Port.DOWN)).
            put(Target.ANY, new AnyTargetInterface(this, module, face)).
            put(Target.LAST, new LastTargetInterface(this, module, face)).
            build();
    }

    /**
     * Advance the virtual machine, ensures the machine's state is valid after
     * the instruction finishes.
     *
     * @return <tt>true</tt> if the current instruction changed (even if it's the same again).
     */
    public boolean step() {
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.step(this);
        }

        return state.finishCycle();
    }

    /**
     * Inform the active instruction that a write operation will be completed.
     *
     * @param port the port on which the write operation will be completed.
     * @see li.cil.tis3d.api.module.Module#onBeforeWriteComplete(Port)
     */
    public void onBeforeWriteComplete(final Port port) {
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.onBeforeWriteComplete(this, port);
        }
    }

    /**
     * Inform the active instruction that a write operation was completed.
     * <p>
     * Instructions are expected to wait / loop until a write operation they
     * initiated has been completed.
     *
     * @param port the port on which the write operation was completed.
     */
    public void onWriteCompleted(final Port port) {
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.onWriteCompleted(this, port);
        }
    }

    /**
     * Utility method for safely retrieving the current instruction.
     *
     * @return the currently active instruction, or {@code null}.
     */
    @Nullable
    private Instruction getInstruction() {
        if (state.pc >= 0 && state.pc < state.instructions.size()) {
            return state.instructions.get(state.pc);
        }
        return null;
    }

    // --------------------------------------------------------------------- //
    // Machine

    @Override
    public MachineState getState() {
        return state;
    }

    @Override
    public TargetInterface getInterface(final Target target) {
        final Target rotatedTarget = getRotatedTarget(target);
        return interfaces.get(rotatedTarget);
    }

    // --------------------------------------------------------------------- //

    /**
     * Adjust the specified target based on the execution module's rotation.
     * <p>
     * Will only do something for port targets.
     *
     * @param target the target to transform.
     * @return the adjusted target.
     */
    private Target getRotatedTarget(final Target target) {
        switch (target) {
            case LEFT:
            case RIGHT:
            case UP:
            case DOWN:
                int rotation = Port.ROTATION[module.getFacing().ordinal()];
                if (module.getFace() == Face.Y_NEG) {
                    rotation = -rotation;
                }
                final Port port = Target.toPort(target);
                final Port rotatedPort = port.rotated(rotation);
                return Target.fromPort(rotatedPort);
        }
        return target;
    }
}
