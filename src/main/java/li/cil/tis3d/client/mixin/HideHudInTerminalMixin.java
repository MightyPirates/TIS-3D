package li.cil.tis3d.client.mixin;

import li.cil.tis3d.client.gui.TerminalModuleGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HideHudInTerminalMixin extends Drawable {
    @Inject(method = "draw", at = @At("HEAD"), cancellable = true)
    private void hideHud(final float tickDelta, final CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentGui instanceof TerminalModuleGui) {
            ci.cancel();
        }
    }
}
