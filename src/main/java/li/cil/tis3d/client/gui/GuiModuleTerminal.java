package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHand;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * Invisible GUI for the terminal module, purely used to grab keyboard input.
 */

public final class GuiModuleTerminal extends GuiScreen {
    private final ModuleTerminal module;

    GuiModuleTerminal(final ModuleTerminal module) {
        this.module = module;
    }

    public boolean isFor(final ModuleTerminal that) {
        return that == module;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager.disableTexture2D();

        // To be on the safe side (see manual.Document#render).
        GlStateManager.disableAlpha();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);
        GlStateManager.colorMask(false, false, false, false);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, 500);

        RenderUtil.drawUntexturedQuad(8, 8, width - 16, height - 16);

        GlStateManager.popMatrix();
        GlStateManager.depthMask(false);
        GlStateManager.colorMask(true, true, true, true);

        RenderUtil.drawUntexturedQuad(4, 4, width - 8, height - 8);

        GlStateManager.enableTexture2D();
    }

    @Override
    public boolean keyPressed(int keyCode, int scancode, int mods) {
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
    public boolean charTyped(char chr, int code) {
        if (super.charTyped(chr, code)) {
            return true;
        }

        if (chr != '\0') {
            return writeToModule(chr);
        } else {
            return false;
        }
    }

    private boolean writeToModule(char chr) {
        module.writeToInput(chr);

        if (Settings.animateTypingHand) {
            mc.player.swingArm(EnumHand.MAIN_HAND);
        }
        return true;
    }

    @Override
    public void initGui() {
        // TODO Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        // TODO Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // TODO
    /* @SubscribeEvent
    public void handleRenderGameOverlay(final RenderGameOverlayEvent.Pre event) {
        event.setCanceled(true);
        mc.entityRenderer.setupOverlayRendering();
    } */
}
