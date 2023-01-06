package li.cil.tis3d.api.module.traits;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

import javax.annotation.Nullable;
import java.util.OptionalInt;

/**
 * Modules implementing this interface will be queried when the quads for rendering the side of the
 * {@link li.cil.tis3d.api.machine.Casing} the module is currently installed in are collected.
 * <p>
 * Also see the Fabric and Forge specific specializations of this interface..
 */
public interface ModuleWithBakedModel {
    /**
     * Whether this module has a module to render.
     * <p>
     * If this returns {@code false}, none of the other methods will be called, and the regular
     * blank module face will be rendered.
     * <p>
     * This supports dynamically changing models, which is used by the facade module, for example.
     *
     * @return whether this module has a model.
     */
    default boolean hasModel() {
        return true;
    }

    /**
     * Get the tint color to use for the quads returned by this module.
     * <p>
     * Note that only the first tint color by any module in one casing will be used. Using facades with varying tint
     * color in a single casing will lead to wrong results. There's no way around it, so we just live with it.
     *
     * @param level     the render-thread safe world access.
     * @param pos       the position of the casing.
     * @param tintIndex the tint index to resolve.
     * @return the color for the specified tint index, if possible.
     */
    @Environment(EnvType.CLIENT)
    default OptionalInt getTintColor(@Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex) {
        return OptionalInt.empty();
    }
}
