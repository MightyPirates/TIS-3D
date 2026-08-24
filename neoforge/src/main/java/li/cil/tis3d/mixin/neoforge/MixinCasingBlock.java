/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.mixin.neoforge;

import li.cil.tis3d.common.block.CasingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CasingBlock.class)
public abstract class MixinCasingBlock extends Block {
    private MixinCasingBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(final LevelReader level, final BlockPos pos, final BlockState state, final boolean includeData, final Player player) {
        // Allow picking modules installed in the casing.
        final ItemStack stack = CasingBlock.getPickedModule(level, pos, player);
        if (!stack.isEmpty()) {
            return stack;
        }
        return super.getCloneItemStack(level, pos, state, includeData, player);
    }
}
