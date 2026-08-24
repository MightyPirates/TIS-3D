/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.InfraredAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import li.cil.tis3d.common.module.ExecutionModule;
import li.cil.tis3d.common.module.ReadOnlyMemoryModule;
import li.cil.tis3d.common.module.RedstoneModule;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

import static li.cil.tis3d.gametest.Invocations.Kind.INFRARED;
import static li.cil.tis3d.gametest.Invocations.Kind.READ;
import static li.cil.tis3d.gametest.TestSupport.*;

public final class ModuleTests {
    private static final int OUTPUT_VALUE = 9;
    private static final int INFRARED_VALUE = 321;

    private static final String TAG_MEMORY = "memory";

    private static final byte[] ROM_DATA = {3, 4, 5, 6};
    private static final int ROM_ADDRESS = 0;
    private static final int INTERFERING_VALUE = 42;

    // Ticks to let an ANY write settle, at one step per tick and at one step per two ticks.
    private static final int FULL_POWER_STEP_TICKS = 20;
    private static final int WIRE_POWER_STEP_TICKS = 40;

    public static void redstoneModuleExchangesWithWorld(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        helper.setBlock(CASING_NEIGHBOR_POS, Blocks.REDSTONE_BLOCK);

        final TestModule[] probe = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                machine.install(CASING_POS, Face.Y_POS, new RedstoneModule(machine.casing(CASING_POS), Face.Y_POS));
                probe[0] = machine.install(CASING_POS, Face.X_POS).readOn(Port.UP).writeOn(Port.UP, OUTPUT_VALUE);
                machine.casing(CASING_POS).markRedstoneDirty();
            })
            .thenWaitUntil(() -> helper.assertTrue(probe[0].invocations().any(READ, Port.UP),
                "the redstone module never put the world's signal onto its pipes"))
            .thenExecute(() -> helper.assertValueEqual(probe[0].invocations().first(READ, Port.UP).value(), 15,
                "value read from the redstone module's input"))
            .thenWaitUntil(() -> helper.assertValueEqual(
                helper.getLevel().getSignal(helper.absolutePos(CASING_POS), Direction.DOWN), OUTPUT_VALUE,
                "redstone signal emitted on the module's face"))
            .thenSucceed();
    }

    public static void infraredPacketReachesReceiver(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final TestModule[] receiver = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> receiver[0] = machine.install(CASING_POS, Face.Y_POS))
            .thenExecute(() -> {
                final Vec3 origin = Vec3.atCenterOf(helper.absolutePos(CASING_POS)).add(0, 2, 0);
                InfraredAPI.sendPacket(helper.getLevel(), origin, new Vec3(0, -1, 0), (short) INFRARED_VALUE);
            })
            .thenWaitUntil(() -> helper.assertTrue(receiver[0].invocations().any(INFRARED), "the infrared packet never arrived"))
            .thenExecute(() -> helper.assertValueEqual(receiver[0].invocations().first(INFRARED).value(), INFRARED_VALUE,
                "infrared packet value"))
            .thenSucceed();
    }

    public static void readOnlyMemoryModuleIgnoresConcurrentWrite(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();

        final ReadOnlyMemoryModule[] rom = new ReadOnlyMemoryModule[1];
        final TestModule[] reader = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                rom[0] = installReadOnlyMemory(machine);

                // The ROM's LEFT port faces X_POS, its RIGHT port faces X_NEG. One module addresses
                // the ROM and reads the addressed value, the other writes at the same time.
                reader[0] = machine.install(CASING_POS, Face.X_POS).writeOn(Port.UP, ROM_ADDRESS).readOn(Port.UP);
                machine.install(CASING_POS, Face.X_NEG).writeOn(Port.UP, INTERFERING_VALUE);
            })
            .thenIdle(10)
            .thenExecute(() -> assertMemoryIntact(helper, rom[0]))
            .thenWaitUntil(() -> helper.assertTrue(reader[0].invocations().any(READ, Port.UP),
                "the ROM module never served the addressed value"))
            .thenExecute(() -> {
                helper.assertValueEqual(reader[0].invocations().first(READ, Port.UP).value(),
                    (int) ROM_DATA[ROM_ADDRESS], "value read from the ROM module");
                assertMemoryIntact(helper, rom[0]);
            })
            .thenSucceed();
    }

    public static void anyWriteUsesFirstPortOnYPos(final GameTestHelper helper) {
        assertAnyWriteUsesFirstPortAboveController(helper, Face.Y_POS);
    }

    public static void anyWriteUsesFirstPortOnYNeg(final GameTestHelper helper) {
        assertAnyWriteUsesFirstPortBelowController(helper, Face.Y_NEG);
    }

    public static void anyWriteUsesFirstPortOnXPos(final GameTestHelper helper) {
        assertAnyWriteUsesFirstPortBelowController(helper, Face.X_POS);
    }

    public static void anyWriteUsesFirstPortOnXNeg(final GameTestHelper helper) {
        assertAnyWriteUsesFirstPortBelowController(helper, Face.X_NEG);
    }

    public static void anyWriteUsesFirstPortOnZPos(final GameTestHelper helper) {
        assertAnyWriteUsesFirstPortBelowController(helper, Face.Z_POS);
    }

    public static void anyWriteUsesFirstPortOnZNeg(final GameTestHelper helper) {
        assertAnyWriteUsesFirstPortBelowController(helper, Face.Z_NEG);
    }

    // --------------------------------------------------------------------- //

    private static ReadOnlyMemoryModule installReadOnlyMemory(final MachineFixture machine) {
        final ItemStack stack = new ItemStack(Items.READ_ONLY_MEMORY_MODULE.get());
        ReadOnlyMemoryModuleItem.saveToStack(stack, ROM_DATA);

        final ReadOnlyMemoryModule module = new ReadOnlyMemoryModule(machine.casing(CASING_POS), Face.Y_POS);
        module.onInstalled(stack);

        return machine.install(CASING_POS, Face.Y_POS, module);
    }

    private static void assertMemoryIntact(final GameTestHelper helper, final ReadOnlyMemoryModule rom) {
        final CompoundTag tag = new CompoundTag();
        rom.save(tag);

        final byte[] memory = tag.getByteArray(TAG_MEMORY).orElse(new byte[0]);
        for (int address = 0; address < ROM_DATA.length; address++) {
            helper.assertValueEqual(memory[address], ROM_DATA[address], "ROM memory at address " + address);
        }
    }

    private static void assertAnyWriteUsesFirstPortBelowController(final GameTestHelper helper, final Face exeFace) {
        final BlockPos casingPos = CONTROLLER_POS.below();
        assertAnyWriteUsesFirstPort(helper, exeFace, casingPos, Face.Y_POS,
            MachineFixture.place(helper, casingPos).powerFully(), FULL_POWER_STEP_TICKS);
    }

    private static void assertAnyWriteUsesFirstPortAboveController(final GameTestHelper helper, final Face exeFace) {
        final BlockPos casingPos = CONTROLLER_POS.above();
        assertAnyWriteUsesFirstPort(helper, exeFace, casingPos, Face.Y_NEG,
            MachineFixture.place(helper, casingPos).powerWithWire(), WIRE_POWER_STEP_TICKS);
    }

    private static void assertAnyWriteUsesFirstPort(final GameTestHelper helper, final Face exeFace, final BlockPos casingPos, final Face controllerFace, final MachineFixture machine, final int ticks) {
        final ExecutionModule[] exe = new ExecutionModule[1];
        final List<TestModule> readers = new ArrayList<>();

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                exe[0] = machine.install(casingPos, exeFace, new ExecutionModule(machine.casing(casingPos), exeFace));
                try {
                    Compiler.compile(List.of("MOV 1 ANY"), exe[0].getState());
                } catch (final ParseException e) {
                    throw new GameTestAssertException(Component.literal("failed compiling test program: " + e), 0);
                }

                // A willing reader on every port of every other face, so that all four ports of the
                // execution module could complete their write.
                for (final Face face : Face.VALUES) {
                    if (face == exeFace || face == controllerFace) {
                        continue;
                    }

                    final TestModule reader = machine.install(casingPos, face);
                    for (final Port port : Port.VALUES) {
                        reader.readOn(port);
                    }
                    readers.add(reader);
                }
            })
            .thenIdle(ticks)
            .thenExecute(() -> {
                helper.assertTrue(exe[0].getState().last.isPresent(),
                    "no ANY write completed at all with the execution module on " + exeFace);
                helper.assertValueEqual(exe[0].getState().last.get(), Port.VALUES[0],
                    "port the ANY write completed on with the execution module on " + exeFace);

                final long fed = readers.stream().filter(reader -> reader.invocations().any(READ)).count();
                helper.assertValueEqual(fed, 1L,
                    "modules that received a value with the execution module on " + exeFace);
            })
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private ModuleTests() {
    }
}
