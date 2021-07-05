package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.TerminalModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * Invisible GUI for the terminal module, purely used to grab keyboard input.
 */
@Environment(EnvType.CLIENT)
public final class TerminalModuleGui extends Screen {
    private final TerminalModule module;

    TerminalModuleGui(final TerminalModule module) {
        super(new LiteralText("Terminal"));
        this.module = module;
    }

    public boolean isFor(final TerminalModule that) {
        return that == module;
    }

    @Override
    public void render(final MatrixStack matrices, final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager._disableTexture();

        // To be on the safe side (see manual.Document#render).
        GlStateManager._disableDepthTest();

        RenderSystem.setShaderColor(1, 1, 1, 1);
        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT, false);
        GlStateManager._enableDepthTest();
        GlStateManager._depthFunc(GL11.GL_LEQUAL);
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(false, false, false, false);
        matrices.push();
        matrices.translate(0, 0, 500);

        RenderUtil.drawUntexturedQuad(8, 8, width - 16, height - 16);

        matrices.pop();
        GlStateManager._depthMask(false);
        GlStateManager._colorMask(true, true, true, true);

        RenderUtil.drawUntexturedQuad(4, 4, width - 8, height - 8);

        GlStateManager._enableTexture();
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scancode, final int mods) {
        if (super.keyPressed(keyCode, scancode, mods)) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            return writeToModule('\b');
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            return writeToModule('\n');
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            return writeToModule('\t');
        }

        return false;
    }

    @Override
    public boolean charTyped(final char chr, final int code) {
        if (super.charTyped(chr, code)) {
            return true;
        }

        if (chr != '\0') {
            return writeToModule(chr);
        } else {
            return false;
        }
    }

    private boolean writeToModule(final char chr) {
        module.writeToInput(chr);

        if (Settings.animateTypingHand) {
            client.player.swingHand(Hand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public void init() {
        super.init();
        client.keyboard.setRepeatEvents(true);
    }

    @Override
    public void onClose() {
        super.onClose();
        client.keyboard.setRepeatEvents(false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
