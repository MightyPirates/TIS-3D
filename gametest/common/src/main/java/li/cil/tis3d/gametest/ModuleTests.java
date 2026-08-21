/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.InfraredAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.module.RedstoneModule;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import static li.cil.tis3d.gametest.Invocations.Kind.INFRARED;
import static li.cil.tis3d.gametest.Invocations.Kind.READ;
import static li.cil.tis3d.gametest.TestSupport.CASING_NEIGHBOR_POS;
import static li.cil.tis3d.gametest.TestSupport.CASING_POS;

public final class ModuleTests {
    private static final int OUTPUT_VALUE = 9;
    private static final int INFRARED_VALUE = 321;

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

    // --------------------------------------------------------------------- //

    private ModuleTests() {
    }
}
