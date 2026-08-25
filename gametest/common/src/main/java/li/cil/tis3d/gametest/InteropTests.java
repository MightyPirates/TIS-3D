/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.InfraredAPI;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import li.cil.tis3d.common.module.InfraredModule;
import li.cil.tis3d.common.module.RedstoneModule;
import li.cil.tis3d.common.module.SerialPortModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import static li.cil.tis3d.gametest.Invocations.Kind.READ;
import static li.cil.tis3d.gametest.Invocations.Kind.WRITE;
import static li.cil.tis3d.gametest.TestSupport.CASING_POS;

public final class InteropTests {
    private static final Direction MODULE_SIDE = Direction.SOUTH;
    private static final Direction PROBE_SIDE = Direction.UP;
    private static final Port PROBE_PORT = Port.UP;

    private static final int WRITTEN_VALUE = 9;
    private static final int SERIAL_OFFERED_VALUE = 23;

    public static void redstoneReadsFromWorld(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);
        final TestModule[] probe = new TestModule[1];

        helper.setBlock(neighborPos(), Blocks.REDSTONE_BLOCK);

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final Face local = casing.toLocal(MODULE_SIDE);
                machine.install(CASING_POS, local, new RedstoneModule(casing, local));
                probe[0] = machine.install(CASING_POS, casing.toLocal(PROBE_SIDE)).readOn(probePort(machine));
                casing.markRedstoneDirty();
            })
            .thenWaitUntil(() -> helper.assertTrue(probe[0].invocations().any(READ, probePort(machine)),
                "the redstone module never put the world's signal onto its pipes, rotated " + rotation))
            .thenExecute(() -> helper.assertValueEqual(probe[0].invocations().first(READ, probePort(machine)).value(), 15,
                "signal read from the world, rotated " + rotation))
            .thenSucceed();
    }

    public static void redstoneWritesToWorld(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final Face local = casing.toLocal(MODULE_SIDE);
                machine.install(CASING_POS, local, new RedstoneModule(casing, local));
                machine.install(CASING_POS, casing.toLocal(PROBE_SIDE)).writeOn(probePort(machine), WRITTEN_VALUE);
                casing.markRedstoneDirty();
            })
            .thenWaitUntil(() -> helper.assertValueEqual(
                // getSignal takes the side facing away from the module's face.
                helper.getLevel().getSignal(helper.absolutePos(CASING_POS), MODULE_SIDE.getOpposite()),
                WRITTEN_VALUE, "signal emitted into the world, rotated " + rotation))
            .thenSucceed();
    }

    public static void serialPortExchangesWithWorld(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);
        final TestModule[] probe = new TestModule[1];

        helper.setBlock(neighborPos(), Blocks.NOTE_BLOCK);
        final BlockPos interfacePos = helper.absolutePos(neighborPos());
        TestSerialInterfaceProvider.initialize(interfacePos, SERIAL_OFFERED_VALUE);

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final Face local = casing.toLocal(MODULE_SIDE);
                machine.install(CASING_POS, local, new SerialPortModule(casing, local));
                probe[0] = machine.install(CASING_POS, casing.toLocal(PROBE_SIDE))
                    .readOn(probePort(machine))
                    .writeOn(probePort(machine), WRITTEN_VALUE);
            })
            .thenWaitUntil(() -> helper.assertTrue(probe[0].invocations().any(READ, probePort(machine)),
                "the serial interface's value never reached the pipes, rotated " + rotation))
            .thenExecute(() -> helper.assertValueEqual(probe[0].invocations().first(READ, probePort(machine)).value(),
                SERIAL_OFFERED_VALUE, "value read from the serial interface, rotated " + rotation))
            .thenWaitUntil(() -> helper.assertTrue(TestSerialInterfaceProvider.invocations(interfacePos).any(WRITE),
                "nothing was written to the serial interface, rotated " + rotation))
            .thenExecute(() -> TestSerialInterfaceProvider.invocations(interfacePos).assertInvoked(WRITE, WRITTEN_VALUE))
            .thenSucceed();
    }

    public static void infraredSendsIntoWorld(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final Face local = casing.toLocal(MODULE_SIDE);
                machine.install(CASING_POS, local, new InfraredModule(casing, local));
                machine.install(CASING_POS, casing.toLocal(PROBE_SIDE)).writeOn(probePort(machine), WRITTEN_VALUE);
            })
            .thenWaitUntil(() -> helper.assertTrue(!packets(helper).isEmpty(),
                "the infrared module never emitted a packet, rotated " + rotation))
            .thenExecute(() -> {
                final Vec3 movement = packets(helper).getFirst().getDeltaMovement().normalize();
                final Vec3 expected = Vec3.atLowerCornerOf(MODULE_SIDE.getNormal());
                helper.assertTrue(movement.dot(expected) > 0.9,
                    "the packet left towards " + movement + " instead of " + expected + ", rotated " + rotation);
            })
            .thenSucceed();
    }

    public static void infraredReceivesFromWorld(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);
        final TestModule[] probe = new TestModule[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final Face local = casing.toLocal(MODULE_SIDE);
                machine.install(CASING_POS, local, new InfraredModule(casing, local));
                probe[0] = machine.install(CASING_POS, casing.toLocal(PROBE_SIDE)).readOn(probePort(machine));
            })
            .thenExecute(() -> {
                // Fire at the module's world face from two blocks out.
                final Direction facing = MODULE_SIDE;
                final Vec3 origin = Vec3.atCenterOf(helper.absolutePos(CASING_POS))
                    .add(Vec3.atLowerCornerOf(facing.getNormal()).scale(2));
                InfraredAPI.sendPacket(helper.getLevel(), origin,
                    Vec3.atLowerCornerOf(facing.getOpposite().getNormal()), (short) WRITTEN_VALUE);
            })
            .thenWaitUntil(() -> helper.assertTrue(probe[0].invocations().any(READ, probePort(machine)),
                "the infrared module never forwarded the packet, rotated " + rotation))
            .thenExecute(() -> helper.assertValueEqual(probe[0].invocations().first(READ, probePort(machine)).value(),
                WRITTEN_VALUE, "value received over infrared, rotated " + rotation))
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private static BlockPos neighborPos() {
        return CASING_POS.relative(MODULE_SIDE);
    }

    private static Port probePort(final MachineFixture machine) {
        return machine.casing(CASING_POS).toLocal(PROBE_SIDE, PROBE_PORT);
    }

    private static List<InfraredPacketEntity> packets(final GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(InfraredPacketEntity.class,
            new AABB(helper.absolutePos(CASING_POS)).inflate(6));
    }

    private InteropTests() {
    }
}
