package li.cil.tis3d.client.renderer.color;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public final class CasingBlockColor implements BlockColor {
    @Override
    public int getColor(final BlockState state, @Nullable final BlockAndTintGetter level, @Nullable final BlockPos pos, final int tintIndex) {
        if (level == null || pos == null) {
            return 0;
        }

        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing) {
            for (final Face face : Face.VALUES) {
                final Module module = casing.getModule(face);
                if (module instanceof final ModuleWithBakedModel moduleWithModel) {
                    if (moduleWithModel.hasModel()) {
                        final OptionalInt optional = moduleWithModel.getTintColor(level, pos, tintIndex);
                        if (optional.isPresent()) {
                            return optional.getAsInt();
                        }
                    }
                }
            }
        }

        return 0;
    }
}
