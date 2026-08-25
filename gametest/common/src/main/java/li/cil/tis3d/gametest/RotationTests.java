/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.util.TransformUtil;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ComputerBlockEntity;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.module.ExecutionModule;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static li.cil.tis3d.gametest.Invocations.Kind.READ;
import static li.cil.tis3d.gametest.TestSupport.CASING_2_POS;
import static li.cil.tis3d.gametest.TestSupport.CASING_POS;

public final class RotationTests {
    private static final int VALUE = 1234;
    private static final int PROGRAM_VALUE = 42;

    public static void facesRoundTripThroughWorldSpace(final GameTestHelper helper) {
        for (final Rotation rotation : Rotation.values()) {
            for (final Face face : Face.VALUES) {
                helper.assertValueEqual(
                    TransformUtil.toLocal(TransformUtil.toWorld(face, rotation), rotation), face,
                    "face " + face + " round tripped through " + rotation);
            }
        }

        helper.succeed();
    }

    public static void portsRoundTripThroughWorldSpace(final GameTestHelper helper) {
        for (final Rotation rotation : Rotation.values()) {
            for (final Face face : Face.VALUES) {
                for (final Port port : Port.VALUES) {
                    helper.assertValueEqual(
                        TransformUtil.toLocal(TransformUtil.toWorld(face, rotation),
                            TransformUtil.toWorld(face, port, rotation), rotation), port,
                        "port " + port + " on " + face + " round tripped through " + rotation);
                }
            }
        }

        helper.succeed();
    }

    public static void edgeTopologyIsRotationCoherent(final GameTestHelper helper) {
        for (final Rotation rotation : Rotation.values()) {
            for (final Face face : Face.VALUES) {
                for (final Port port : Port.VALUES) {
                    final Face worldSideAsFace = Face.fromDirection(TransformUtil.toWorld(face, rotation));
                    final Port worldPort = TransformUtil.toWorld(face, port, rotation);

                    helper.assertValueEqual(
                        Face.toDirection(ComputerBlockEntity.mapFace(worldSideAsFace, worldPort)),
                        TransformUtil.toWorld(ComputerBlockEntity.mapFace(face, port), rotation),
                        "edge face for " + face + "/" + port + " under " + rotation);
                    helper.assertValueEqual(
                        ComputerBlockEntity.mapPort(worldSideAsFace, worldPort),
                        TransformUtil.toWorld(ComputerBlockEntity.mapFace(face, port),
                            ComputerBlockEntity.mapPort(face, port), rotation),
                        "edge port for " + face + "/" + port + " under " + rotation);
                }
            }
        }

        helper.succeed();
    }

    public static void rotatingBlockStateMovesFaceFlags(final GameTestHelper helper) {
        for (final Rotation rotation : Rotation.values()) {
            BlockState state = Blocks.CASING.get().defaultBlockState();
            for (final Direction direction : Direction.values()) {
                state = state.setValue(CasingBlock.DIRECTION_TO_PROPERTY.get(direction),
                    direction == Direction.EAST || direction == Direction.UP);
            }

            final BlockState rotated = state.rotate(rotation);

            helper.assertValueEqual(rotated.getValue(CasingBlock.FACING),
                rotation.rotate(state.getValue(CasingBlock.FACING)), "facing after " + rotation);
            for (final Direction direction : Direction.values()) {
                final Direction rotatedDirection = rotation.rotate(direction);
                helper.assertValueEqual(
                    rotated.getValue(CasingBlock.DIRECTION_TO_PROPERTY.get(rotatedDirection)),
                    state.getValue(CasingBlock.DIRECTION_TO_PROPERTY.get(direction)),
                    "flag for " + direction + " moved to " + rotatedDirection + " under " + rotation);
            }
        }

        helper.succeed();
    }

    public static void valueCrossesFacesWhenRotated(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);
        final TestModule[] modules = new TestModule[2];
        final Port[] readPort = new Port[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                readPort[0] = casing.toLocal(Direction.EAST, Port.UP);

                modules[0] = machine.install(CASING_POS, casing.toLocal(Direction.UP))
                    .writeOn(casing.toLocal(Direction.UP, Port.LEFT), VALUE);
                modules[1] = machine.install(CASING_POS, casing.toLocal(Direction.EAST))
                    .readOn(readPort[0]);
            })
            .thenWaitUntil(() -> helper.assertTrue(modules[1].invocations().any(READ, readPort[0]),
                "the reader never got the value with the casing rotated " + rotation))
            .thenExecute(() -> helper.assertValueEqual(modules[1].invocations().first(READ, readPort[0]).value(), VALUE,
                "value read with the casing rotated " + rotation))
            .thenSucceed();
    }

    public static void valueCrossesCasingsWhenRotated(final GameTestHelper helper, final Rotation first, final Rotation second) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS, CASING_2_POS).powerFully()
            .rotate(CASING_POS, first)
            .rotate(CASING_2_POS, second);
        final TestModule[] modules = new TestModule[2];
        final Port[] readPort = new Port[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity writer = machine.casing(CASING_POS);
                final CasingBlockEntity reader = machine.casing(CASING_2_POS);
                readPort[0] = reader.toLocal(Direction.UP, Port.RIGHT);

                modules[0] = machine.install(CASING_POS, writer.toLocal(Direction.UP))
                    .writeOn(writer.toLocal(Direction.UP, Port.LEFT), VALUE);
                modules[1] = machine.install(CASING_2_POS, reader.toLocal(Direction.UP))
                    .readOn(readPort[0]);
            })
            .thenWaitUntil(() -> helper.assertTrue(modules[1].invocations().any(READ, readPort[0]),
                "the value did not cross the casing boundary with casings rotated " + first + " and " + second))
            .thenExecute(() -> helper.assertValueEqual(modules[1].invocations().first(READ, readPort[0]).value(), VALUE,
                "value read across the casing boundary with casings rotated " + first + " and " + second))
            .thenSucceed();
    }

    public static void valueCrossesStackedCasingsWhenRotated(final GameTestHelper helper, final Rotation first, final Rotation second) {
        final BlockPos upperPos = CASING_POS.above();
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS, upperPos).powerFully()
            .rotate(CASING_POS, first)
            .rotate(upperPos, second);
        final List<TestModule> readers = new ArrayList<>();
        final List<Face> readerFaces = new ArrayList<>();

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity lower = machine.casing(CASING_POS);
                final CasingBlockEntity upper = machine.casing(upperPos);

                machine.install(CASING_POS, lower.toLocal(Direction.EAST))
                    .writeOn(lower.toLocal(Direction.EAST, Port.UP), VALUE);

                // Read on every port of every free face, so the test finds where the value comes
                // out rather than assuming it.
                for (final Face face : Face.VALUES) {
                    if (face == upper.toLocal(Direction.DOWN)) {
                        continue;
                    }
                    final TestModule reader = machine.install(upperPos, face);
                    for (final Port port : Port.VALUES) {
                        reader.readOn(port);
                    }
                    readers.add(reader);
                    readerFaces.add(face);
                }
            })
            .thenIdle(20)
            .thenExecute(() -> {
                final CasingBlockEntity upper = machine.casing(upperPos);
                final List<String> arrivals = new ArrayList<>();
                for (int i = 0; i < readers.size(); i++) {
                    for (final Port port : Port.VALUES) {
                        if (readers.get(i).invocations().any(READ, port)) {
                            final Face localFace = readerFaces.get(i);
                            arrivals.add(upper.toWorld(localFace) + "/" + upper.toWorld(localFace, port));
                        }
                    }
                }

                helper.assertValueEqual(String.join(", ", arrivals), Direction.EAST + "/" + Port.DOWN,
                    "where the value surfaced with casings rotated " + first + " and " + second);
            })
            .thenSucceed();
    }

    public static void rotatingModuleAgainstNeighborDropsIt(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS, CASING_2_POS).powerFully();
        final int[] slot = new int[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                // Rotating clockwise moves this face onto EAST, which is where the other casing is.
                slot[0] = casing.toLocal(Direction.NORTH).ordinal();
                casing.setItem(slot[0], new ItemStack(Items.RANDOM_MODULE.get()));

                helper.assertFalse(casing.getItem(slot[0]).isEmpty(), "the module was not installed to begin with");
            })
            .thenIdle(2)
            .thenExecute(() -> machine.rotate(CASING_POS, Rotation.CLOCKWISE_90))
            .thenIdle(5)
            .thenExecute(() -> helper.assertTrue(machine.casing(CASING_POS).getItem(slot[0]).isEmpty(),
                "the module was not dropped after being rotated against the neighboring casing"))
            .thenSucceed();
    }

    public static void executionModulePortsFollowWorldFacingOnCapFace(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully().rotate(CASING_POS, rotation);
        final List<TestModule> readers = new ArrayList<>();
        final List<Face> readerFaces = new ArrayList<>();

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final Face exeFace = casing.toLocal(Direction.DOWN);
                final Face controllerFace = casing.toLocal(Direction.WEST);

                final ExecutionModule exe = machine.install(CASING_POS, exeFace, new ExecutionModule(casing, exeFace));
                // Oriented the way installing it while facing north does: a world facing, mapped in.
                exe.setFacing(casing.toLocal(Direction.DOWN, Port.fromDirection(Direction.NORTH)));
                try {
                    Compiler.compile(List.of("MOV " + PROGRAM_VALUE + " UP"), exe.getState());
                } catch (final ParseException e) {
                    throw new GameTestAssertException("failed compiling test program: " + e);
                }

                // Read on every port of every free face, so the test finds where the value comes out
                // rather than assuming it.
                for (final Face face : Face.VALUES) {
                    if (face == exeFace || face == controllerFace) {
                        continue;
                    }
                    final TestModule reader = machine.install(CASING_POS, face);
                    for (final Port port : Port.VALUES) {
                        reader.readOn(port);
                    }
                    readers.add(reader);
                    readerFaces.add(face);
                }
            })
            .thenIdle(40)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final List<String> arrivals = new ArrayList<>();
                for (int i = 0; i < readers.size(); i++) {
                    for (final Port port : Port.VALUES) {
                        if (readers.get(i).invocations().any(READ, port)) {
                            final Face localFace = readerFaces.get(i);
                            arrivals.add(casing.toWorld(localFace) + "/" + casing.toWorld(localFace, port));
                        }
                    }
                }

                helper.assertValueEqual(String.join(", ", arrivals), Direction.SOUTH + "/" + Port.DOWN,
                    "where the execution module's UP port surfaced, rotated " + rotation);
            })
            .thenSucceed();
    }

    public static void settingFacingDirectlyRederivesFaceFlags(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerFully();
        final int[] slot = new int[1];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                slot[0] = casing.toLocal(Direction.NORTH).ordinal();
                casing.setItem(slot[0], new ItemStack(Items.RANDOM_MODULE.get()));
            })
            .thenIdle(2)
            .thenExecute(() -> {
                final BlockPos absolute = helper.absolutePos(CASING_POS);
                // Deliberately not Block#rotate: only the facing moves, the flags stay put.
                final BlockState state = helper.getLevel().getBlockState(absolute);
                helper.getLevel().setBlockAndUpdate(absolute, state.setValue(CasingBlock.FACING, Direction.EAST));
            })
            .thenIdle(5)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                final BlockState state = helper.getLevel().getBlockState(helper.absolutePos(CASING_POS));
                for (final Direction direction : Direction.values()) {
                    helper.assertValueEqual(
                        state.getValue(CasingBlock.DIRECTION_TO_PROPERTY.get(direction)),
                        !casing.getItem(casing.toLocal(direction).ordinal()).isEmpty(),
                        "flag for " + direction + " after setting the facing directly");
                }
            })
            .thenSucceed();
    }

    public static void hoppersReachTheCasingThroughWorldFaces(final GameTestHelper helper, final Rotation rotation) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerWithWire().rotate(CASING_POS, rotation);
        final BlockPos above = CASING_POS.above();
        final BlockPos below = CASING_POS.below();
        final int[] slots = new int[2];

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                slots[0] = casing.toLocal(Direction.UP).ordinal();
                slots[1] = casing.toLocal(Direction.DOWN).ordinal();

                casing.setItem(slots[1], new ItemStack(Items.RANDOM_MODULE.get()));

                helper.setBlock(above, net.minecraft.world.level.block.Blocks.HOPPER.defaultBlockState()
                    .setValue(HopperBlock.FACING, Direction.DOWN));
                TestSupport.requireBlockEntity(helper, above, HopperBlockEntity.class)
                    .setItem(0, new ItemStack(Items.RANDOM_MODULE.get()));

                helper.setBlock(below, net.minecraft.world.level.block.Blocks.HOPPER.defaultBlockState()
                    .setValue(HopperBlock.FACING, Direction.DOWN));
            })
            .thenIdle(60)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                helper.assertFalse(casing.getItem(slots[0]).isEmpty(),
                    "the hopper above did not insert into the top face, rotated " + rotation);
                helper.assertTrue(casing.getItem(slots[1]).isEmpty(),
                    "the hopper below did not extract from the bottom face, rotated " + rotation);
                helper.assertFalse(TestSupport.requireBlockEntity(helper, below, HopperBlockEntity.class).isEmpty(),
                    "the extracted module never reached the hopper below, rotated " + rotation);
            })
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private RotationTests() {
    }
}
