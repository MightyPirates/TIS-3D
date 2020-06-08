package li.cil.tis3d.client.gui;

import li.cil.tis3d.common.module.TerminalModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public final class GuiHelper {
    @Environment(EnvType.CLIENT)
    public static void openManualGui() {
        MinecraftClient.getInstance().openScreen(new ManualGui());
    }

    @Environment(EnvType.CLIENT)
    public static void openCodeBookGui(final PlayerEntity player, final Hand hand) {
        MinecraftClient.getInstance().openScreen(new CodeBookGui(player, hand));
    }

    @Environment(EnvType.CLIENT)
    public static void openTerminalGui(final TerminalModule terminal) {
        MinecraftClient.getInstance().openScreen(new TerminalModuleGui(terminal));
    }

    @Environment(EnvType.CLIENT)
    public static void openReadOnlyMemoryGui(final PlayerEntity player, final Hand hand) {
        MinecraftClient.getInstance().openScreen(new ReadOnlyMemoryModuleGui(player, hand));
    }
}
