/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.module.execution;

import com.mojang.serialization.Codec;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.config.Constants;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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

    public void load(final ValueInput input) {
        final Optional<String> source = input.getString(TAG_CODE);
        if (source.isPresent()) {
            try {
                Compiler.compile(Arrays.asList(Constants.PATTERN_LINES.split(source.get())), this);
            } catch (final ParseException ignored) {
                // Silent because this is also used to send code to the
                // clients to visualize errors, and code is also saved
                // in errored state.
            }
        }

        pc = input.getIntOr(TAG_PC, 0);
        acc = (short) input.getShortOr(TAG_ACC, (short) 0);
        bak = (short) input.getShortOr(TAG_BAK, (short) 0);
        last = input.getInt(TAG_LAST).map(ordinal -> Port.VALUES[ordinal]);
        pcPrev = input.getIntOr(TAG_PC_PREV, 0);
        pendingAnyWrite = input.read(TAG_PENDING_ANY_WRITE, Codec.SHORT);
    }

    public void save(final ValueOutput output) {
        output.putInt(TAG_PC, pc);
        output.putShort(TAG_ACC, acc);
        output.putShort(TAG_BAK, bak);
        last.ifPresent(port -> EnumUtils.save(port, TAG_LAST, output));
        output.putInt(TAG_PC_PREV, pcPrev);
        pendingAnyWrite.ifPresent(value -> output.putShort(TAG_PENDING_ANY_WRITE, value));

        if (code != null) {
            output.putString(TAG_CODE, String.join("\n", code));
        }
    }
}
