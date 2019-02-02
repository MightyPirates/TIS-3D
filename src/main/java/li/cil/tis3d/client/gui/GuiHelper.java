package li.cil.tis3d.client.gui;

import li.cil.tis3d.common.module.TerminalModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public final class GuiHelper {
    public static void openManualGui() {
        MinecraftClient.getInstance().openScreen(new ManualGui());
    }

    public static void openCodeBookGui(final PlayerEntity player, final Hand hand) {
        MinecraftClient.getInstance().openScreen(new CodeBookGui(player, hand));
    }

    public static void openTerminalGui(final TerminalModule terminal) {
        MinecraftClient.getInstance().openScreen(new TerminalModuleGui(terminal));
    }

    public static void openReadOnlyMemoryGui(final PlayerEntity player, final Hand hand) {
        MinecraftClient.getInstance().openScreen(new ReadOnlyMemoryModuleGui(player, hand));
    }
}
