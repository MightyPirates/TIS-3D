package li.cil.tis3d.client;

import li.cil.tis3d.client.gui.CodeBookScreen;
import li.cil.tis3d.client.gui.ReadOnlyMemoryModuleScreen;
import li.cil.tis3d.client.gui.TerminalModuleScreen;
import li.cil.tis3d.common.module.TerminalModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public final class ClientHooks {
    public static void openCodeBookScreen(final Player player, final InteractionHand hand) {
        Minecraft.getInstance().setScreen(new CodeBookScreen(player, hand));
    }

    public static void openTerminalScreen(final TerminalModule module) {
        Minecraft.getInstance().setScreen(new TerminalModuleScreen(module));
    }

    public static void closeTerminalScreen(final TerminalModule module) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof final TerminalModuleScreen screen && screen.isFor(module)) {
            minecraft.setScreen(null);
        }
    }

    public static boolean isTerminalScreenOpen(final TerminalModule module) {
        return Minecraft.getInstance().screen instanceof final TerminalModuleScreen screen && screen.isFor(module);
    }

    public static void setReadOnlyMemoryModuleData(final byte[] data) {
        if (Minecraft.getInstance().screen instanceof final ReadOnlyMemoryModuleScreen screen) {
            screen.setData(data);
        }
    }

    public static int getBlockTintColor(final BlockState state, @Nullable final BlockAndTintGetter level,
                                       @Nullable final BlockPos pos, final int tintIndex) {
        return Minecraft.getInstance().getBlockColors().getColor(state, level, pos, tintIndex);
    }

    @Nullable
    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    // --------------------------------------------------------------------- //

    private ClientHooks() {
    }
}
