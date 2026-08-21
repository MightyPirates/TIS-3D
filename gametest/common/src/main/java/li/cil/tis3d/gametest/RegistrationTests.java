package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.module.RedstoneModule;
import li.cil.tis3d.common.module.SerialPortModule;
import li.cil.tis3d.common.provider.ModuleProviders;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import static li.cil.tis3d.gametest.Invocations.Kind.READ;
import static li.cil.tis3d.gametest.Invocations.Kind.WRITE;
import static li.cil.tis3d.gametest.TestSupport.CASING_NEIGHBOR_POS;
import static li.cil.tis3d.gametest.TestSupport.CASING_POS;

public final class RegistrationTests {
    private static final int REDSTONE_SIGNAL = 11;
    private static final int SERIAL_OFFERED_VALUE = 42;
    private static final int SERIAL_WRITTEN_VALUE = 7;

    // --------------------------------------------------------------------- //

    public static void thirdPartyModuleProviderIsUsed(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final ItemStack stack = new ItemStack(TestRegistry.TEST_MODULE_ITEM.get());

                helper.assertTrue(ModuleProviders.getProviderFor(stack, casing, Face.Y_POS).isPresent(),
                    "no module provider matched the third-party module item");

                casing.setInventorySlotContents(Face.Y_POS.ordinal(), stack, Port.UP);
                machine.assertModule(CASING_POS, Face.Y_POS, TestModule.class);
            })
            .thenWaitUntil(() -> {
                final TestModule module = (TestModule) machine.casing(CASING_POS).getModule(Face.Y_POS);
                helper.assertTrue(module != null && module.stepCount() > 0,
                    "the third-party module was installed but never stepped");
            })
            .thenSucceed();
    }

    public static void thirdPartyRedstoneInputProviderIsUsed(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] probe = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                helper.assertTrue(helper.getLevel().getSignal(helper.absolutePos(CASING_NEIGHBOR_POS), Direction.UP) == 0,
                    "the world already supplies a signal, the provider proves nothing");
                TestRedstoneInputProvider.setSignal(helper.absolutePos(CASING_POS), Direction.UP, REDSTONE_SIGNAL);

                machine.install(CASING_POS, Face.Y_POS, new RedstoneModule(machine.casing(CASING_POS), Face.Y_POS));
                probe[0] = machine.install(CASING_POS, Face.X_POS).readOn(Port.UP);
                machine.casing(CASING_POS).markRedstoneDirty();
            })
            .thenWaitUntil(() -> helper.assertTrue(probe[0].invocations().any(READ, Port.UP),
                "the redstone module never produced a value"))
            .thenExecute(() -> helper.assertValueEqual(probe[0].invocations().first(READ, Port.UP).value(), REDSTONE_SIGNAL,
                "value from the third-party redstone input provider"))
            .thenSucceed();
    }

    public static void thirdPartySerialInterfaceProviderIsUsed(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        helper.setBlock(CASING_NEIGHBOR_POS, Blocks.NOTE_BLOCK);

        final BlockPos interfacePos = helper.absolutePos(CASING_NEIGHBOR_POS);
        TestSerialInterfaceProvider.initialize(interfacePos, SERIAL_OFFERED_VALUE);

        final TestModule[] probe = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                helper.assertTrue(SerialInterfaceProviders
                        .getProviderFor(helper.getLevel(), interfacePos, Direction.DOWN)
                        .orElse(null) instanceof TestSerialInterfaceProvider,
                    "the third-party serial interface provider did not match the marker block");

                machine.install(CASING_POS, Face.Y_POS, new SerialPortModule(machine.casing(CASING_POS), Face.Y_POS));
                probe[0] = machine.install(CASING_POS, Face.X_POS)
                    .readOn(Port.UP)
                    .writeOn(Port.UP, SERIAL_WRITTEN_VALUE);
            })
            .thenWaitUntil(() -> helper.assertTrue(probe[0].invocations().any(READ, Port.UP),
                "the serial interface's value never reached the pipes"))
            .thenExecute(() -> helper.assertValueEqual(probe[0].invocations().first(READ, Port.UP).value(), SERIAL_OFFERED_VALUE,
                "value read from the third-party serial interface"))
            .thenWaitUntil(() -> helper.assertTrue(TestSerialInterfaceProvider.invocations(interfacePos).any(WRITE),
                "nothing was ever written to the third-party serial interface"))
            .thenExecute(() -> TestSerialInterfaceProvider.invocations(interfacePos)
                .assertInvoked(WRITE, SERIAL_WRITTEN_VALUE))
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private RegistrationTests() {
    }
}
