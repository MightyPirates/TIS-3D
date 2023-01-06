package li.cil.tis3d.mixin.forge;

import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CasingBlock.class)
public abstract class MixinCasingBlock extends Block {
    private MixinCasingBlock(final Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getCloneItemStack(final BlockState state, final HitResult hit, final BlockGetter level, final BlockPos pos, final Player player) {
        // Allow picking modules installed in the casing.
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing && hit instanceof final BlockHitResult blockHit) {
            final ItemStack stack = casing.getItem(blockHit.getDirection().ordinal());
            if (!stack.isEmpty()) {
                return stack.copy();
            }
        }
        return super.getCloneItemStack(state, hit, level, pos, player);
    }
}
