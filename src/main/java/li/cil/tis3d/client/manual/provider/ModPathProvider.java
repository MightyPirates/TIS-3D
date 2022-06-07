package li.cil.tis3d.client.manual.provider;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.prefab.provider.NamespacePathProvider;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;
import java.util.Optional;

public class ModPathProvider extends NamespacePathProvider {
    public ModPathProvider() {
        super(API.MOD_ID);
    }

    @Override
    public boolean matches(final ManualModel manual) {
        return Objects.equals(manual, Manuals.MANUAL.get());
    }

    @Override
    public Optional<String> pathFor(final Level level, final BlockPos pos, final Direction face) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof final CasingBlockEntity casing) {
            final ItemStack moduleStack = casing.getItem(face.ordinal());
            final Optional<String> path = pathFor(moduleStack);
            if (path.isPresent()) {
                return path;
            }
        }

        return super.pathFor(level, pos, face);
    }
}
