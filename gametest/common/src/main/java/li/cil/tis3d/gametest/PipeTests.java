/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.gametest.Invocations.Invocation;
import net.minecraft.gametest.framework.GameTestHelper;

import static li.cil.tis3d.gametest.Invocations.Kind.*;
import static li.cil.tis3d.gametest.TestSupport.CASING_2_POS;
import static li.cil.tis3d.gametest.TestSupport.CASING_POS;

public final class PipeTests {
    private static final int VALUE = 1234;

    public static void valueCrossesBetweenFacesOfOneCasing(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] modules = new TestModule[2];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                modules[0] = machine.install(CASING_POS, Face.Y_POS).writeOn(Port.LEFT, VALUE);
                modules[1] = machine.install(CASING_POS, Face.X_POS).readOn(Port.UP);
            })
            .thenWaitUntil(() -> helper.assertTrue(modules[1].invocations().any(READ, Port.UP), "the reader never got the value"))
            .thenExecute(() -> assertTwoStepTransfer(helper, modules[0], Port.LEFT, modules[1], Port.UP))
            .thenSucceed();
    }

    public static void valueCrossesCasingBoundary(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS, CASING_2_POS).powerFully();
        final TestModule[] modules = new TestModule[2];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                modules[0] = machine.install(CASING_POS, Face.Y_POS).writeOn(Port.LEFT, VALUE);
                modules[1] = machine.install(CASING_2_POS, Face.Y_POS).readOn(Port.RIGHT);
            })
            .thenWaitUntil(() -> helper.assertTrue(modules[1].invocations().any(READ, Port.RIGHT),
                "the value did not cross the casing boundary"))
            .thenExecute(() -> assertTwoStepTransfer(helper, modules[0], Port.LEFT, modules[1], Port.RIGHT))
            .thenSucceed();
    }

    public static void writeCompleteFiresAfterTransfer(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] modules = new TestModule[2];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                modules[0] = machine.install(CASING_POS, Face.Y_POS).writeOn(Port.LEFT, VALUE);
                modules[1] = machine.install(CASING_POS, Face.X_POS).readOn(Port.UP);
            })
            .thenWaitUntil(() -> helper.assertTrue(modules[0].invocations().any(WRITE_COMPLETE),
                "the writer never got its onWriteComplete"))
            .thenExecute(() -> {
                final Invocation write = modules[0].invocations().only(WRITE, Port.LEFT);
                final Invocation writeComplete = modules[0].invocations().only(WRITE_COMPLETE);
                final Invocation read = modules[1].invocations().only(READ, Port.UP);

                helper.assertValueEqual(writeComplete.step(), read.step(),
                    "step of onWriteComplete vs. step of the read");
                helper.assertValueEqual(writeComplete.step(), write.step() + 1,
                    "steps between beginWrite and onWriteComplete");
            })
            .thenSucceed();
    }

    public static void cancelledWriteDoesNotTransfer(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] modules = new TestModule[2];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                modules[0] = machine.install(CASING_POS, Face.Y_NEG).writeAndCancelOn(Port.LEFT, VALUE);
                modules[1] = machine.install(CASING_POS, Face.X_POS).readOn(Port.DOWN);
            })
            .thenWaitUntil(() -> helper.assertTrue(modules[0].invocations().any(CANCEL), "the write was never cancelled"))
            .thenExecuteAfter(20, () -> {
                helper.assertFalse(modules[1].invocations().any(READ, Port.DOWN), "a cancelled write still transferred");
                modules[0].invocations().assertNeverInvoked(WRITE_COMPLETE);

                final Pipe sending = machine.casing(CASING_POS).getSendingPipe(Face.Y_NEG, Port.LEFT);
                helper.assertFalse(sending.isWriting(), "the sending half of the pipe did not go back to idle");
            })
            .thenSucceed();
    }

    public static void readerWithoutWriterBlocks(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] module = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> module[0] = machine.install(CASING_POS, Face.X_POS).readOn(Port.UP))
            .thenExecuteAfter(20, () -> {
                helper.assertFalse(module[0].invocations().any(READ, Port.UP), "a reader completed with no writer");
                helper.assertTrue(module[0].stepCount() > 10, "the module barely stepped, the test proves nothing");

                final Pipe receiving = machine.casing(CASING_POS).getReceivingPipe(Face.X_POS, Port.UP);
                helper.assertTrue(receiving.isReading(), "the reader is not parked in the pipe");
                helper.assertFalse(receiving.canTransfer(), "the pipe claims it can transfer with no writer");
            })
            .thenSucceed();
    }

    public static void pipeStateSurvivesReload(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] writer = new TestModule[1];
        final TestModule[] reader = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> writer[0] = machine.install(CASING_POS, Face.Y_POS).writeOn(Port.LEFT, VALUE))
            .thenWaitUntil(() -> helper.assertTrue(writer[0].invocations().any(WRITE, Port.LEFT), "the writer never wrote"))
            .thenExecute(() -> {
                helper.assertTrue(machine.casing(CASING_POS).getSendingPipe(Face.Y_POS, Port.LEFT).isWriting(),
                    "the pipe is not holding the write");
                machine.reload(CASING_POS);
            })
            .thenExecute(() -> {
                machine.assertModule(CASING_POS, Face.Y_POS, null);
                helper.assertTrue(machine.casing(CASING_POS).getSendingPipe(Face.Y_POS, Port.LEFT).isWriting(),
                    "the write did not survive the reload");
            })
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> reader[0] = machine.install(CASING_POS, Face.X_POS).readOn(Port.UP))
            .thenWaitUntil(() -> helper.assertTrue(reader[0].invocations().any(READ, Port.UP), "the reloaded transfer never completed"))
            .thenExecute(() -> helper.assertValueEqual(reader[0].invocations().first(READ, Port.UP).value(), VALUE,
                "value carried across the reload"))
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private static void assertTwoStepTransfer(final GameTestHelper helper,
                                              final TestModule writer, final Port writePort,
                                              final TestModule reader, final Port readPort) {
        helper.assertValueEqual(writer.stepCount(), reader.stepCount(),
            "the two modules were not installed in the same tick, so their step counters cannot be compared");

        final Invocation write = writer.invocations().only(WRITE, writePort);
        final Invocation read = reader.invocations().only(READ, readPort);

        helper.assertValueEqual(read.value(), VALUE, "transferred value");
        helper.assertValueEqual(read.step(), write.step() + 1,
            "steps between beginWrite and read (the value must not be readable in the same step)");
    }

    // --------------------------------------------------------------------- //

    private PipeTests() {
    }
}
