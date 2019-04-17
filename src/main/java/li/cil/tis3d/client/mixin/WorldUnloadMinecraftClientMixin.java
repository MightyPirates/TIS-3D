package li.cil.tis3d.client.mixin;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.machine.CasingImpl;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Screen;
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

    @Inject(method = "joinWorld", at = @At("HEAD"))
    private void onBeforeJoinWorld(final ClientWorld newWorld, final CallbackInfo ci) {
        tis3d_tryDisposeCasingBlockEntities();
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/Screen;)V", at = @At("HEAD"))
    private void onBeforeDisconnect(final Screen screen, final CallbackInfo ci) {
        tis3d_tryDisposeCasingBlockEntities();
    }

    private void tis3d_tryDisposeCasingBlockEntities() {
        if (world == null) {
            return;
        }

        for (final BlockEntity blockEntity : world.blockEntities) {
            if (blockEntity instanceof CasingBlockEntity) {
                final CasingBlockEntity casingBlockEntity = (CasingBlockEntity)blockEntity;
                final CasingImpl casing = (CasingImpl)casingBlockEntity.getCasing();
                casing.onDisposed();
            }
        }
    }
}
