package li.cil.tis3d.client.mixin;

import li.cil.tis3d.client.gui.GuiModuleTerminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.settings.GameOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(InGameHud.class)
public abstract class HideHudInTerminalMixin extends Drawable {
    @Redirect(at = @At(value = "FIELD", target = "net.minecraft.client.settings.GameOptions.field_1842"), method = "draw(F)V")
    private boolean draw(GameOptions options) {
        return MinecraftClient.getInstance().currentGui instanceof GuiModuleTerminal || options.field_1842;
    }
}
