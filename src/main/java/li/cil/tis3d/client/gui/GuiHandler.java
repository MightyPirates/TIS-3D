package li.cil.tis3d.client.gui;

import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public final class GuiHandler {
    public static void openManualGui() {
        MinecraftClient.getInstance().openGui(new GuiManual());
    }

    public static void openCodeBookGui(PlayerEntity player, Hand hand) {
        MinecraftClient.getInstance().openGui(new GuiBookCode(player, hand));
    }

    public static void openTerminalGui(ModuleTerminal terminal) {
        MinecraftClient.getInstance().openGui(new GuiModuleTerminal(terminal));
    }

    public static void openReadOnlyMemoryGui(PlayerEntity player, Hand hand) {
        MinecraftClient.getInstance().openGui(new GuiModuleMemory(player, hand));
    }
}
