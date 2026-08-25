/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity.ControllerState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;

import javax.annotation.Nullable;

import static li.cil.tis3d.gametest.TestSupport.*;

public final class MachineFixture {
    public static final int WIRE_POWER = 15 - (WIRE_POSITIONS.length - 1);

    private final GameTestHelper helper;
    private final BlockPos controllerPos;

    private MachineFixture(final GameTestHelper helper, final BlockPos controllerPos) {
        this.helper = helper;
        this.controllerPos = controllerPos;
    }

    // --------------------------------------------------------------------- //
    // Setup.

    public static MachineFixture place(final GameTestHelper helper, final BlockPos... casingPositions) {
        helper.setBlock(CONTROLLER_POS, li.cil.tis3d.common.block.Blocks.CONTROLLER.get());
        for (final BlockPos casingPos : casingPositions) {
            helper.setBlock(casingPos, li.cil.tis3d.common.block.Blocks.CASING.get());
        }
        return new MachineFixture(helper, CONTROLLER_POS);
    }

    public MachineFixture powerFully() {
        setLeverPowered(true);
        return this;
    }

    public MachineFixture powerWithWire() {
        helper.setBlock(WIRE_SOURCE_POS, leverState(true));
        for (final BlockPos wirePos : WIRE_POSITIONS) {
            helper.setBlock(wirePos, Blocks.REDSTONE_WIRE);
        }
        return this;
    }

    public void setLeverPowered(final boolean powered) {
        helper.setBlock(LEVER_POS, leverState(powered));
    }

    private static BlockState leverState(final boolean powered) {
        return Blocks.LEVER.defaultBlockState()
            .setValue(FaceAttachedHorizontalDirectionalBlock.FACE, AttachFace.FLOOR)
            .setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH)
            .setValue(LeverBlock.POWERED, powered);
    }

    // --------------------------------------------------------------------- //
    // Access.

    public ControllerBlockEntity controller() {
        return requireBlockEntity(helper, controllerPos, ControllerBlockEntity.class);
    }

    public CasingBlockEntity casing(final BlockPos casingPos) {
        return requireBlockEntity(helper, casingPos, CasingBlockEntity.class);
    }

    public <T extends Module> T install(final BlockPos casingPos, final Face face, final T module) {
        casing(casingPos).setModule(face, module);
        return module;
    }

    public TestModule install(final BlockPos casingPos, final Face face) {
        return install(casingPos, face, new TestModule(casing(casingPos), face));
    }

    public MachineFixture rotate(final BlockPos casingPos, final Rotation rotation) {
        final BlockPos absolute = helper.absolutePos(casingPos);
        final BlockState state = helper.getLevel().getBlockState(absolute);
        helper.getLevel().setBlockAndUpdate(absolute, state.rotate(rotation));
        return this;
    }

    public void reload(final BlockPos casingPos) {
        final HolderLookup.Provider registries = helper.getLevel().registryAccess();
        final CompoundTag tag = casing(casingPos).saveCustomOnly(registries);

        helper.setBlock(casingPos, Blocks.AIR);
        helper.setBlock(casingPos, li.cil.tis3d.common.block.Blocks.CASING.get());

        casing(casingPos).loadCustomOnly(tag, registries);
    }

    // --------------------------------------------------------------------- //
    // Assertions.

    public int power() {
        final BlockPos absolute = helper.absolutePos(controllerPos);
        int acc = 0;
        for (final Direction facing : Direction.values()) {
            acc += Math.clamp(helper.getLevel().getDirectSignal(absolute.relative(facing), facing), 0, 15);
        }
        return acc;
    }

    public void assertPower(final int expected) {
        final int actual = power();
        if (actual != expected) {
            final StringBuilder detail = new StringBuilder();
            final BlockPos absolute = helper.absolutePos(controllerPos);
            for (final Direction facing : Direction.values()) {
                final BlockPos neighbor = absolute.relative(facing);
                detail.append(' ').append(facing).append('=')
                    .append(helper.getLevel().getDirectSignal(neighbor, facing))
                    .append('/').append(helper.getLevel().getBlockState(neighbor));
            }
            throw new GameTestAssertException("controller sees power " + actual + ", expected " + expected
                + " --" + detail);
        }
    }

    public void assertState(final ControllerState expected) {
        assertState(controllerPos, expected);
    }

    public void assertState(final BlockPos pos, final ControllerState expected) {
        final ControllerState actual = requireBlockEntity(helper, pos, ControllerBlockEntity.class).getState();
        if (actual != expected) {
            throw new GameTestAssertException("controller at " + pos + " is " + actual + ", expected " + expected);
        }
    }

    public void assertRunning() {
        assertState(ControllerState.RUNNING);
    }

    public void assertCasingEnabled(final BlockPos casingPos, final boolean expected) {
        if (casing(casingPos).isCasingEnabled() != expected) {
            throw new GameTestAssertException("casing at " + casingPos + " is "
                + (expected ? "not enabled" : "enabled") + ", expected the opposite");
        }
    }

    public void assertModule(final BlockPos casingPos, final Face face, @Nullable final Class<?> type) {
        final Module module = casing(casingPos).getModule(face);
        if (type == null) {
            if (module != null) {
                throw new GameTestAssertException("expected no module on " + face + " of " + casingPos + ", found " + module);
            }
        } else if (!type.isInstance(module)) {
            throw new GameTestAssertException("expected a " + type.getSimpleName() + " on " + face
                + " of " + casingPos + ", found " + module);
        }
    }
}
