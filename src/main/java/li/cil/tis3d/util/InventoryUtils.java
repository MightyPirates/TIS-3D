package li.cil.tis3d.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Utility class for interacting with inventories.
 */
public final class InventoryUtils {
    /**
     * Drop some items from an inventory into a level.
     *
     * @param level     the level to drop the item into.
     * @param pos       the position in the level to drop the item at.
     * @param inventory the inventory to drop the item from.
     * @param index     the slot of the inventory to drop the item from.
     * @param count     the number of items to drop from the stack in that slot.
     * @param towards   the direction in which to drop the item.
     * @return the entity representing the dropped item stack, or {@code null} if the stack was null or empty.
     */
    @Nullable
    public static ItemEntity drop(final Level level, final BlockPos pos, final Container inventory, final int index, final int count, final Direction towards) {
        final ItemStack stack = inventory.removeItem(index, count);
        return spawnStackInLevel(level, pos, stack, towards);
    }

    /**
     * Spawns an item stack in the level.
     *
     * @param level   the level to spawn the item stack in.
     * @param pos     the position to spawn the item stack at.
     * @param stack   the item stack to spawn in the level.
     * @param towards the direction in which to drop the item.
     * @return the entity representing the dropped item stack, or {@code null} if the stack was null or empty.
     */
    @Nullable
    public static ItemEntity spawnStackInLevel(final Level level, final BlockPos pos, final ItemStack stack, final Direction towards) {
        if (stack.isEmpty()) {
            return null;
        }

        final RandomSource rng = level.random;

        final double ox = towards.getStepX();
        final double oy = towards.getStepY();
        final double oz = towards.getStepZ();
        final double tx = 0.1 * (rng.nextDouble() - 0.5) + ox * 0.65;
        final double ty = 0.1 * (rng.nextDouble() - 0.5) + oy * 0.75 + (ox + oz) * 0.25;
        final double tz = 0.1 * (rng.nextDouble() - 0.5) + oz * 0.65;
        final double px = pos.getX() + 0.5 + tx;
        final double py = pos.getY() + 0.5 + ty;
        final double pz = pos.getZ() + 0.5 + tz;

        final ItemEntity entity = new ItemEntity(level, px, py, pz, stack.copy());

        entity.setDeltaMovement(
            0.0125 * (rng.nextDouble() - 0.5) + ox * 0.03,
            0.0125 * (rng.nextDouble() - 0.5) + oy * 0.08 + (ox + oz) * 0.03,
            0.0125 * (rng.nextDouble() - 0.5) + oz * 0.03
        );
        entity.setPickUpDelay(15);
        level.addFreshEntity(entity);

        return entity;
    }

    // --------------------------------------------------------------------- //

    private InventoryUtils() {
    }
}
