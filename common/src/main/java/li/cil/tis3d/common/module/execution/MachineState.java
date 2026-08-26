/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.module.execution;

import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.config.Constants;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

/**
 * Virtual machine state for executing TIS-100 assembly.
 */
public final class MachineState {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * Program counter, i.e. the index of the next operation to execute.
     */
    public int pc;

    /**
     * Accumulator register.
     */
    public short acc;

    /**
     * Backup register.
     */
    public short bak;

    /**
     * The port last read from.
     */
    public Optional<Port> last = Optional.empty();

    /**
     * Lines of original code this state was compiled from.
     */
    public String[] code;

    /**
     * State of program counter after last call to {@link #finishCycle()}.
     */
    private int pcPrev;

    /**
     * The value of an in-flight {@link li.cil.tis3d.common.module.execution.target.Target#ANY} write.
     * <p>
     * An ANY write is performed on all ports at once, but port arbitration prunes it down to one,
     * discarding the data on the other ports. We can't do retries in the instruction itself, because
     * it has already consumed its source at this point (and instructions are stateless themselves).
     * So the value is kept here until the write completes, which aligns with what we're doing in the
     * other modules that have this problem.
     */
    public Optional<Short> pendingAnyWrite = Optional.empty();

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_CODE = "code";
    private static final String TAG_PC = "pc";
    private static final String TAG_ACC = "acc";
    private static final String TAG_BAK = "bak";
    private static final String TAG_LAST = "last";
    private static final String TAG_PC_PREV = "pcPrev";
    private static final String TAG_PENDING_ANY_WRITE = "any";

    /**
     * List of instructions (the program) stored in the machine.
     */
    public final List<Instruction> instructions = new ArrayList<>(CommonConfig.maxLinesPerProgram);

    /**
     * List of labels and associated addresses.
     */
    public final HashMap<String, Integer> labels = new HashMap<>(CommonConfig.maxLinesPerProgram);

    /**
     * Instruction address to line number mapping.
     */
    public final HashMap<Integer, Integer> lineNumbers = new HashMap<>(CommonConfig.maxLinesPerProgram);

    // --------------------------------------------------------------------- //

    /**
     * Finishes an execution cycle, ensuring values of the state are valid ones and
     * returning whether the internal state changed since the last call to this method.
     *
     * @return <code>true</code> if the internal state had changed since the last call.
     */
    public boolean finishCycle() {
        // Check this before wrapping program counter because this also determines the run
        // state of the hosting execution module, so we need to report change when the
        // instruction at the current program counter position has finished (which we can
        // tell by seeing that it incremented / changed the program counter state).
        final boolean hasChanged = pc != pcPrev;

        // Set to zero even when running out at the end to have programs
        // restart automatically.
        if (pc < 0 || pc >= instructions.size()) {
            pc = 0;
        }

        pcPrev = pc;

        return hasChanged;
    }

    /**
     * Soft reset the machine state.
     */
    public void reset() {
        pc = 0;
        acc = 0;
        bak = 0;
        last = Optional.empty();
        pendingAnyWrite = Optional.empty();
    }

    /**
     * Clear code storage of the machine state. Retains run state.
     */
    public void clear() {
        instructions.clear();
        labels.clear();
        code = null;
        lineNumbers.clear();
    }

    // --------------------------------------------------------------------- //

    public void load(final CompoundTag tag) {
        if (tag.contains(TAG_CODE)) {
            try {
                Compiler.compile(Arrays.asList(Constants.PATTERN_LINES.split(tag.getStringOr(TAG_CODE, ""))), this);
            } catch (final ParseException ignored) {
                // Silent because this is also used to send code to the
                // clients to visualize errors, and code is also saved
                // in errored state.
            }
        }

        pc = tag.getIntOr(TAG_PC, 0);
        acc = tag.getShortOr(TAG_ACC, (short) 0);
        bak = tag.getShortOr(TAG_BAK, (short) 0);
        if (tag.contains(TAG_LAST)) {
            last = Optional.of(EnumUtils.load(Port.class, TAG_LAST, tag));
        } else {
            last = Optional.empty();
        }
        pcPrev = tag.getIntOr(TAG_PC_PREV, 0);
        pendingAnyWrite = tag.getShort(TAG_PENDING_ANY_WRITE);
    }

    public void save(final CompoundTag tag) {
        tag.putInt(TAG_PC, pc);
        tag.putShort(TAG_ACC, acc);
        tag.putShort(TAG_BAK, bak);
        last.ifPresent(port -> EnumUtils.save(port, TAG_LAST, tag));
        tag.putInt(TAG_PC_PREV, pcPrev);
        pendingAnyWrite.ifPresent(value -> tag.putShort(TAG_PENDING_ANY_WRITE, value));

        if (code != null) {
            tag.putString(TAG_CODE, String.join("\n", code));
        }
    }
}
