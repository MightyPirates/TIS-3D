/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity.ControllerState;
import li.cil.tis3d.common.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;

import static li.cil.tis3d.gametest.Invocations.Kind.DISABLED;
import static li.cil.tis3d.gametest.Invocations.Kind.ENABLED;
import static li.cil.tis3d.gametest.TestSupport.*;

public final class MachineTests {
    public static void controllerScansAndRuns(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS);
        machine.setLeverPowered(false);

        helper.startSequence()
            .thenExecuteAfter(5, () -> {
                machine.assertPower(0);
                machine.assertState(ControllerState.READY);
                machine.assertCasingEnabled(CASING_POS, false);
            })
            .thenExecute(machine::powerFully)
            .thenExecuteAfter(2, () -> machine.assertPower(15))
            .thenWaitUntil(() -> {
                machine.assertRunning();
                machine.assertCasingEnabled(CASING_POS, true);
            })
            .thenSucceed();
    }

    public static void controllerRejectsTwoControllers(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        helper.setBlock(CONTROLLER_2_POS, li.cil.tis3d.common.block.Blocks.CONTROLLER.get());

        helper.startSequence()
            .thenWaitUntil(() -> {
                machine.assertState(CONTROLLER_POS, ControllerState.MULTIPLE_CONTROLLERS);
                machine.assertState(CONTROLLER_2_POS, ControllerState.MULTIPLE_CONTROLLERS);
            })
            .thenExecute(() -> machine.assertCasingEnabled(CASING_POS, false))
            .thenSucceed();
    }

    public static void controllerRejectsTooManyCasings(final GameTestHelper helper) {
        final BlockPos[] casings = new BlockPos[TOO_MANY_CASINGS];
        for (int i = 0; i < casings.length; i++) {
            casings[i] = casingRunPos(i);
        }

        final MachineFixture machine = MachineFixture.place(helper, casings).powerFully();

        helper.startSequence()
            .thenWaitUntil(() -> machine.assertState(ControllerState.TOO_COMPLEX))
            .thenSucceed();
    }

    public static void disablingControllerDisablesModules(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] module = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> module[0] = machine.install(CASING_POS, Face.Y_POS))
            .thenExecute(() -> {
                helper.assertValueEqual(module[0].invocations().count(ENABLED), 1, "onEnabled calls after install");
                helper.assertValueEqual(module[0].invocations().count(DISABLED), 0, "onDisabled calls while running");
            })
            .thenExecute(() -> machine.setLeverPowered(false))
            .thenWaitUntil(() -> {
                machine.assertState(ControllerState.READY);
                machine.assertCasingEnabled(CASING_POS, false);
            })
            .thenExecute(() -> helper.assertValueEqual(module[0].invocations().count(DISABLED), 1, "onDisabled calls after power cut"))
            .thenSucceed();
    }

    public static void stepsOncePerTickAtFullPower(final GameTestHelper helper) {
        assertStepRate(helper, MachineFixture.place(helper, CASING_POS).powerFully(), 15, 20);
    }

    public static void stepsEveryOtherTickAtPartialPower(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerWithWire();
        assertStepRate(helper, machine, MachineFixture.WIRE_POWER, 20 / (15 - MachineFixture.WIRE_POWER));
    }

    public static void casingLocksAndUnlocksWithKey(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                helper.assertFalse(casing.isLocked(), "a fresh casing is locked");

                final ItemStack key = new ItemStack(Items.KEY.get());
                casing.lock(key);
                helper.assertTrue(casing.isLocked(), "casing did not lock");

                casing.unlock(new ItemStack(Items.KEY.get()));
                helper.assertTrue(casing.isLocked(), "casing unlocked with a different key");

                casing.unlock(key);
                helper.assertFalse(casing.isLocked(), "casing did not unlock with its own key");

                casing.lock(new ItemStack(Items.KEY.get()));
                casing.unlock(new ItemStack(Items.KEY_CREATIVE.get()));
                helper.assertFalse(casing.isLocked(), "casing did not unlock with the creative key");
            })
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private static void assertStepRate(final GameTestHelper helper, final MachineFixture machine,
                                       final int expectedPower, final int expectedSteps) {
        final TestModule[] module = new TestModule[1];
        final int[] start = new int[1];

        helper.startSequence()
            .thenExecuteAfter(5, () -> machine.assertPower(expectedPower))
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> module[0] = machine.install(CASING_POS, Face.Y_POS))
            .thenExecuteAfter(5, () -> start[0] = module[0].stepCount())
            .thenExecuteAfter(20, () -> helper.assertValueEqual(
                module[0].stepCount() - start[0], expectedSteps, "steps in 20 ticks at power " + expectedPower))
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private MachineTests() {
    }
}
