/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ComputerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Rotation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static li.cil.tis3d.gametest.TestSupport.CONTROLLER_POS;

public final class TransferTests {
    private static final Direction[] DIRECTIONS = Direction.values();

    public static void singleCasing(final GameTestHelper helper, final Rotation rotation) {
        run(helper, new BlockPos[]{CONTROLLER_POS.east()}, new Rotation[]{rotation});
    }

    public static void planarCasings(final GameTestHelper helper, final Rotation first, final Rotation second) {
        run(helper, new BlockPos[]{CONTROLLER_POS.east(), CONTROLLER_POS.east(2)},
            new Rotation[]{first, second});
    }

    public static void cornerCasings(final GameTestHelper helper, final Rotation first, final Rotation second) {
        run(helper, new BlockPos[]{CONTROLLER_POS.east(), CONTROLLER_POS.east(2), CONTROLLER_POS.east(2).above()},
            new Rotation[]{first, second, first});
    }

    // --------------------------------------------------------------------- //

    private static void run(final GameTestHelper helper, final BlockPos[] positions, final Rotation[] rotations) {
        final MachineFixture machine = MachineFixture.place(helper, positions).powerFully();
        for (int i = 0; i < positions.length; i++) {
            machine.rotate(positions[i], rotations[i]);
        }

        // Reader identity -> the module that reads there. Keyed in world space throughout.
        final Map<Endpoint, TestModule> readers = new LinkedHashMap<>();

        helper.startSequence()
            .thenWaitUntil(machine::assertRunning)
            .thenExecute(() -> {
                for (int i = 0; i < positions.length; i++) {
                    final CasingBlockEntity casing = machine.casing(positions[i]);
                    for (final Direction side : DIRECTIONS) {
                        if (isBlocked(positions, positions[i], side)) {
                            continue;
                        }

                        final TestModule module = machine.install(positions[i], casing.toLocal(side));
                        for (final Port worldPort : Port.VALUES) {
                            final Port localPort = casing.toLocal(side, worldPort);
                            module.writeOn(localPort, encode(new Endpoint(i, side, worldPort)));
                            module.readOn(localPort);
                            readers.put(new Endpoint(i, side, worldPort), module);
                        }
                    }
                }
            })
            .thenIdle(40)
            .thenExecute(() -> {
                final List<String> problems = new ArrayList<>();

                readers.forEach((endpoint, module) -> {
                    final CasingBlockEntity casing = machine.casing(positions[endpoint.casing()]);
                    final Port localPort = casing.toLocal(endpoint.side(), endpoint.port());

                    final Endpoint expected = expectedSender(positions, endpoint);
                    final boolean expectTransfer = expected != null && readers.containsKey(expected);

                    if (!module.invocations().any(Invocations.Kind.READ, localPort)) {
                        if (expectTransfer) {
                            problems.add(endpoint + " received nothing, expected from " + expected);
                        }
                        return;
                    }

                    final Endpoint actual = decode(module.invocations().first(Invocations.Kind.READ, localPort).value());
                    if (!expectTransfer) {
                        problems.add(endpoint + " received from " + actual + ", expected nothing");
                    } else if (!actual.equals(expected)) {
                        problems.add(endpoint + " received from " + actual + ", expected " + expected);
                    }
                });

                if (!problems.isEmpty()) {
                    throw new GameTestAssertException(problems.size() + " wrong connections: "
                        + String.join("; ", problems.subList(0, Math.min(4, problems.size()))));
                }
            })
            .thenSucceed();
    }

    @Nullable
    private static Endpoint expectedSender(final BlockPos[] positions, final Endpoint receiver) {
        if (isBlocked(positions, positions[receiver.casing()], receiver.side())) {
            return null; // A face against another computer holds no module.
        }

        BlockPos pos = positions[receiver.casing()];
        Direction side = receiver.side();
        Port port = receiver.port();

        for (int steps = 0; steps < positions.length; steps++) {
            // Which edge a face and port pick out is a property of the cube, not of a casing's
            // orientation, so the unrotated tables read as world geometry here. That equivalence is
            // what RotationTests#edgeTopologyIsRotationCoherent asserts.
            final Face otherFace = Face.fromDirection(side);
            final Direction otherSide = Face.toDirection(ComputerBlockEntity.mapFace(otherFace, port));
            final Port otherPort = ComputerBlockEntity.mapPort(otherFace, port);

            final BlockPos neighborPos = pos.relative(otherSide);
            final int neighbor = indexOf(positions, neighborPos);
            if (neighbor < 0 && !neighborPos.equals(CONTROLLER_POS)) {
                // Open face, so this is where the value is written.
                final int index = indexOf(positions, pos);
                return index < 0 ? null : new Endpoint(index, otherSide, otherPort);
            }
            if (neighbor < 0) {
                return null; // Reached the controller, which has no modules.
            }

            pos = neighborPos;
            side = otherSide.getOpposite();
            port = flipSide(otherSide, otherPort);

            if (pos.equals(positions[receiver.casing()])) {
                return null; // Walked in a circle.
            }
        }

        return null;
    }

    private static boolean isBlocked(final BlockPos[] positions, final BlockPos pos, final Direction side) {
        final BlockPos neighbor = pos.relative(side);
        return neighbor.equals(CONTROLLER_POS) || indexOf(positions, neighbor) >= 0;
    }

    private static int indexOf(final BlockPos[] positions, final BlockPos pos) {
        for (int i = 0; i < positions.length; i++) {
            if (positions[i].equals(pos)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Independent copy so the test actually checks something.
     */
    private static Port flipSide(final Direction side, final Port port) {
        if (side.getAxis() == Direction.Axis.Y) {
            return (port == Port.UP || port == Port.DOWN) ? port.getOpposite() : port;
        }
        return (port == Port.LEFT || port == Port.RIGHT) ? port.getOpposite() : port;
    }

    private static int encode(final Endpoint endpoint) {
        return 1 + (endpoint.casing() * DIRECTIONS.length + endpoint.side().ordinal()) * Port.VALUES.length
            + endpoint.port().ordinal();
    }

    private static Endpoint decode(final int value) {
        final int packed = value - 1;
        return new Endpoint(packed / (DIRECTIONS.length * Port.VALUES.length),
            DIRECTIONS[packed / Port.VALUES.length % DIRECTIONS.length],
            Port.VALUES[packed % Port.VALUES.length]);
    }

    private record Endpoint(int casing, Direction side, Port port) {
        @Override
        public String toString() {
            return "casing " + casing + " " + side + "/" + port;
        }
    }

    // --------------------------------------------------------------------- //

    private TransferTests() {
    }
}
