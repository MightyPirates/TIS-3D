/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.gametest;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

import static li.cil.tis3d.gametest.TestSupport.CASING_POS;

public final class InventoryTests {
    public static void hoppersRespectTheCasingLock(final GameTestHelper helper) {
        final MachineFixture machine = MachineFixture.place(helper, CASING_POS).powerWithWire();
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
                casing.lock(new ItemStack(Items.KEY.get()));

                helper.setBlock(above, Blocks.HOPPER.defaultBlockState()
                    .setValue(HopperBlock.FACING, Direction.DOWN));
                TestSupport.requireBlockEntity(helper, above, HopperBlockEntity.class)
                    .setItem(0, new ItemStack(Items.RANDOM_MODULE.get()));

                helper.setBlock(below, Blocks.HOPPER.defaultBlockState()
                    .setValue(HopperBlock.FACING, Direction.DOWN));
            })
            .thenIdle(60)
            .thenExecute(() -> {
                final CasingBlockEntity casing = machine.casing(CASING_POS);
                helper.assertTrue(casing.getItem(slots[0]).isEmpty(),
                    "the hopper above inserted into a locked casing");
                helper.assertFalse(casing.getItem(slots[1]).isEmpty(),
                    "the hopper below extracted from a locked casing");
                helper.assertFalse(TestSupport.requireBlockEntity(helper, above, HopperBlockEntity.class).isEmpty(),
                    "the hopper above lost its module to a locked casing");
                helper.assertTrue(TestSupport.requireBlockEntity(helper, below, HopperBlockEntity.class).isEmpty(),
                    "a module reached the hopper below a locked casing");
            })
            .thenSucceed();
    }

    // --------------------------------------------------------------------- //

    private InventoryTests() {
    }
}
