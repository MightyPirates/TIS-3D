package li.cil.tis3d.system.module.execution;

import li.cil.tis3d.Settings;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.system.module.execution.compiler.Compiler;
import li.cil.tis3d.system.module.execution.compiler.ParseException;
import li.cil.tis3d.system.module.execution.instruction.Instruction;
import net.minecraft.nbt.NBTTagCompound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Virtual machine state for executing TIS-100 assembly.
 */
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
    public int acc = 0;

    /**
     * Backup register.
     */
    public int bak = 0;

    /**
     * The port last read from.
     */
    public Optional<Port> last = Optional.empty();

    /**
     * Lines of original code this state was compiled from.
     */
    public String[] code;

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    public static final String TAG_CODE = "code";
    public static final String TAG_PC = "pc";
    public static final String TAG_ACC = "acc";
    public static final String TAG_BAK = "bak";
    public static final String TAG_LAST = "last";

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
     * Ensure values of the state are valid ones.
     */
    public void validate() {
        // Set to zero even when running out at the end to have programs
        // restart automatically.
        if (pc < 0 || pc >= instructions.size()) {
            pc = 0;
        }
        acc = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, acc));
        bak = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, bak));
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

    public void readFromNBT(final NBTTagCompound nbt) {
        if (nbt.hasKey(TAG_CODE)) {
            try {
                Compiler.compile(nbt.getString(TAG_CODE), this);
            } catch (final ParseException ignored) {
                // Silent because this is also used to send code to the
                // clients to visualize errors, and code is also saved
                // in errored state.
            }
        }

        pc = nbt.getInteger(TAG_PC);
        acc = nbt.getInteger(TAG_ACC);
        bak = nbt.getInteger(TAG_BAK);
        if (nbt.hasKey(TAG_LAST)) {
            last = Optional.of(Port.valueOf(nbt.getString(TAG_LAST)));
        }

        validate();

    }

    public void writeToNBT(final NBTTagCompound nbt) {
        nbt.setInteger(TAG_PC, pc);
        nbt.setInteger(TAG_ACC, acc);
        nbt.setInteger(TAG_BAK, bak);
        last.ifPresent(port -> nbt.setString(TAG_LAST, port.name()));

        if (code != null) {
            nbt.setString(TAG_CODE, String.join("\n", code));
        }
    }
}
