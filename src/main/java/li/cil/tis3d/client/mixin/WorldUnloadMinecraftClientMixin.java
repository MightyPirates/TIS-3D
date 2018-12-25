package li.cil.tis3d.client.mixin;

import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.machine.CasingImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class WorldUnloadMinecraftClientMixin {
    @Shadow
    public ClientWorld world;

    @Inject(method = "method_1550", at = @At("HEAD"))
    private void onBeforeSetWorld(ClientWorld newWorld, Gui gui, CallbackInfo ci) {
        if (world == null) {
            return;
        }

        for (final BlockEntity tileEntity : world.blockEntities) {
            if (tileEntity instanceof TileEntityCasing) {
                final TileEntityCasing tileEntityCasing = (TileEntityCasing) tileEntity;
                final CasingImpl casing = (CasingImpl) tileEntityCasing.getCasing();
                casing.onDisposed();
            }
        }
    }
}
