package li.cil.tis3d.mixin.fabric;

import li.cil.tis3d.client.gui.TerminalModuleScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At("HEAD"), cancellable = true)
    public void hideGuiInTerminalScreen(CallbackInfo ci) {
        if (Minecraft.getInstance().screen instanceof TerminalModuleScreen) {
            ci.cancel();
        }
    }
}
