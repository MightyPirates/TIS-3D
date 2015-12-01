package li.cil.tis3d.system.module.execution;

import com.google.common.collect.ImmutableMap;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import li.cil.tis3d.system.module.execution.target.AccInterface;
import li.cil.tis3d.system.module.execution.target.AnyInterface;
import li.cil.tis3d.system.module.execution.target.BakInterface;
import li.cil.tis3d.system.module.execution.target.LastInterface;
import li.cil.tis3d.system.module.execution.target.NilInterface;
import li.cil.tis3d.system.module.execution.target.SideInterface;
import li.cil.tis3d.system.module.execution.target.Target;
import li.cil.tis3d.system.module.execution.target.TargetInterface;

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
                put(Target.ACC, new AccInterface(this)).
                put(Target.BAK, new BakInterface(this)).
                put(Target.NIL, new NilInterface(this)).
                put(Target.LEFT, new SideInterface(this, casing, face, Side.LEFT)).
                put(Target.RIGHT, new SideInterface(this, casing, face, Side.RIGHT)).
                put(Target.UP, new SideInterface(this, casing, face, Side.UP)).
                put(Target.DOWN, new SideInterface(this, casing, face, Side.DOWN)).
                put(Target.ANY, new AnyInterface(this, casing, face)).
                put(Target.LAST, new LastInterface(this, casing, face)).
                build();
    }

    public void step() {
        state.validate();

        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.step(this);
        }
    }

    public void onWriteCompleted(final Side side) {
        final Instruction instruction = getInstruction();
        if (instruction != null) {
            instruction.onWriteCompleted(this, side);
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
    public void beginWrite(final Target target, final int value) {
        interfaces.get(target).beginWrite(value);
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
    public boolean isOutputTransferring(final Target target) {
        return interfaces.get(target).isOutputTransferring();
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
    public boolean isInputTransferring(final Target target) {
        return interfaces.get(target).isInputTransferring();
    }

    @Override
    public int read(final Target target) {
        return interfaces.get(target).read();
    }
}
