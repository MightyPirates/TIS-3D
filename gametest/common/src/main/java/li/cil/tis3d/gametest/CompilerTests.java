/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.ArrayList;
import java.util.List;

public final class CompilerTests {
    public static void onlyInstructionsCountTowardsProgramLength(final GameTestHelper helper) {
        final int limit = CommonConfig.maxLinesPerProgram;
        final List<String> code = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            code.add("# Comment " + i);
            code.add("#DEFINE V" + i + " " + i);
            code.add("LABEL" + i + ":");
            code.add("");
            code.add("#BWTM");
            code.add("NOP");
        }

        final MachineState state = new MachineState();
        try {
            Compiler.compile(code, state);
        } catch (final ParseException e) {
            throw new GameTestAssertException("a program with " + limit + " instructions and "
                + (code.size() - limit) + " non-instruction lines did not compile: " + e.getMessage());
        }

        helper.assertValueEqual(state.instructions.size(), limit, "compiled instructions");

        helper.succeed();
    }

    public static void tooManyInstructionsIsRejected(final GameTestHelper helper) {
        final int limit = CommonConfig.maxLinesPerProgram;
        final List<String> code = new ArrayList<>();
        for (int i = 0; i < limit + 1; i++) {
            code.add("NOP");
        }

        try {
            Compiler.compile(code, new MachineState());
        } catch (final ParseException e) {
            helper.succeed();
            return;
        }

        throw new GameTestAssertException("a program with " + (limit + 1)
            + " instructions compiled, but the limit is " + limit);
    }

    // --------------------------------------------------------------------- //

    private CompilerTests() {
    }
}
