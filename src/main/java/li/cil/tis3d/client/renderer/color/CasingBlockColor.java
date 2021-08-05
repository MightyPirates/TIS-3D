package li.cil.tis3d.client.renderer.color;

import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.traits.ModuleWithBakedModel;
import li.cil.tis3d.common.tileentity.CasingTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.OptionalInt;

@OnlyIn(Dist.CLIENT)
public final class CasingBlockColor implements IBlockColor {
    @Override
    public int getColor(final BlockState state, @Nullable final IBlockDisplayReader level, @Nullable final BlockPos pos, final int tintIndex) {
        if (level == null || pos == null) {
            return 0;
        }

        final TileEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof CasingTileEntity) {
            final CasingTileEntity casing = (CasingTileEntity) blockEntity;
            for (final Face face : Face.VALUES) {
                final Module module = casing.getModule(face);
                if (module instanceof ModuleWithBakedModel) {
                    final ModuleWithBakedModel moduleWithModel = (ModuleWithBakedModel) module;
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
