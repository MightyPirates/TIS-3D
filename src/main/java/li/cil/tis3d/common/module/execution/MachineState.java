package li.cil.tis3d.common.module.execution;

import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.module.execution.instruction.Instruction;
import li.cil.tis3d.util.EnumUtils;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

/**
 * Virtual machine state for executing TIS-100 assembly.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class MachineState {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * Program counter, i.e. the index of the next operation to execute.
     */
    public int pc = 0;

    /**
     * Accumulator register.
     */
    public short acc = 0;

    /**
     * Backup register.
     */
    public short bak = 0;

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

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_CODE = "code";
    private static final String TAG_PC = "pc";
    private static final String TAG_ACC = "acc";
    private static final String TAG_BAK = "bak";
    private static final String TAG_LAST = "last";
    private static final String TAG_PC_PREV = "pcPrev";

    /**
     * List of instructions (the program) stored in the machine.
     */
    public final List<Instruction> instructions = new ArrayList<>(Settings.maxLinesPerProgram);

    /**
     * List of labels and associated addresses.
     */
    public final HashMap<String, Integer> labels = new HashMap<>(Settings.maxLinesPerProgram);

    /**
     * Instruction address to line number mapping.
     */
    public final HashMap<Integer, Integer> lineNumbers = new HashMap<>(Settings.maxLinesPerProgram);

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
    }

    /**
     * Hard reset the machine state.
     */
    public void clear() {
        reset();

        instructions.clear();
        labels.clear();
        code = null;
        lineNumbers.clear();
    }

    // --------------------------------------------------------------------- //

    public void readFromNBT(final CompoundTag nbt) {
        if (nbt.contains(TAG_CODE)) {
            try {
                Compiler.compile(Arrays.asList(Constants.PATTERN_LINES.split(nbt.getString(TAG_CODE))), this);
            } catch (final ParseException ignored) {
                // Silent because this is also used to send code to the
                // clients to visualize errors, and code is also saved
                // in errored state.
            }
        }

        pc = nbt.getInt(TAG_PC);
        acc = nbt.getShort(TAG_ACC);
        bak = nbt.getShort(TAG_BAK);
        if (nbt.contains(TAG_LAST)) {
            last = Optional.of(EnumUtils.readFromNBT(Port.class, TAG_LAST, nbt));
        } else {
            last = Optional.empty();
        }
        pcPrev = nbt.getInt(TAG_PC_PREV);
    }

    public void writeToNBT(final CompoundTag nbt) {
        nbt.putInt(TAG_PC, pc);
        nbt.putShort(TAG_ACC, acc);
        nbt.putShort(TAG_BAK, bak);
        last.ifPresent(port -> EnumUtils.writeToNBT(port, TAG_LAST, nbt));
        nbt.putInt(TAG_PC_PREV, pcPrev);

        if (code != null) {
            nbt.putString(TAG_CODE, String.join("\n", (CharSequence[])code));
        }
    }
}
