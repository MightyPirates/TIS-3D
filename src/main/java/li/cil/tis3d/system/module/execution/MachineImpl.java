package li.cil.tis3d.system.module.execution;

import com.google.common.collect.ImmutableMap;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.target.*;

import java.util.Map;

/**
 * Track machine state and ease communicating with ports of an execution module.
 */
public final class MachineImpl implements Machine {
    // --------------------------------------------------------------------- //
    // Computed data

    private final MachineState state;
    private final Map<Target, TargetInterface> interfaces;

    // --------------------------------------------------------------------- //

    public MachineImpl(final Casing casing, final Face face) {
        this.state = new MachineState();
        interfaces = ImmutableMap.<Target, TargetInterface>builder().
                put(Target.ACC, new TargetInterfaceAcc(this)).
                put(Target.BAK, new TargetInterfaceBak(this)).
                put(Target.NIL, new TargetInterfaceNil(this)).
                put(Target.LEFT, new TargetInterfaceSide(this, casing, face, Port.LEFT)).
                put(Target.RIGHT, new TargetInterfaceSide(this, casing, face, Port.RIGHT)).
                put(Target.UP, new TargetInterfaceSide(this, casing, face, Port.UP)).
                put(Target.DOWN, new TargetInterfaceSide(this, casing, face, Port.DOWN)).
                put(Target.ANY, new TargetInterfaceAny(this, casing, face)).
                put(Target.LAST, new TargetInterfaceLast(this, casing, face)).
                build();
    }

    public void step() {
        state.validate();

        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.step(this);
        }
    }

    public void onWriteCompleted(final Port port) {
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.onWriteCompleted(this, port);
        }
    }

    private Instruction getInstruction() {
        if (state.pc >= 0 && state.pc < state.instructions.size()) {
            return state.instructions.get(state.pc);
        }
        return null;
    }

    // --------------------------------------------------------------------- //

    @Override
    public MachineState getState() {
        return state;
    }

    @Override
    public boolean beginWrite(final Target target, final int value) {
        return interfaces.get(target).beginWrite(value);
    }

    @Override
    public void cancelWrite(final Target target) {
        interfaces.get(target).cancelWrite();
    }

    @Override
    public boolean isWriting(final Target target) {
        return interfaces.get(target).isWriting();
    }

    @Override
    public void beginRead(final Target target) {
        interfaces.get(target).beginRead();
    }

    @Override
    public boolean isReading(final Target target) {
        return interfaces.get(target).isReading();
    }

    @Override
    public boolean canRead(final Target target) {
        return interfaces.get(target).canRead();
    }

    @Override
    public int read(final Target target) {
        return interfaces.get(target).read();
    }
}
