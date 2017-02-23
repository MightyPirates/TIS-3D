package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

/**
 * Invisible GUI for the terminal module, purely used to grab keyboard input.
 */
@SideOnly(Side.CLIENT)
public class GuiModuleTerminal extends GuiScreen {
    private final ModuleTerminal module;

    public GuiModuleTerminal(final ModuleTerminal module) {
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
    protected void keyTyped(final char typedChar, final int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (keyCode == Keyboard.KEY_ESCAPE) {
            return;
        }

        if (typedChar != '\0') {
            module.writeToInput(typedChar);

            if (Settings.animateTypingHand) {
                Minecraft.getMinecraft().player.swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @SubscribeEvent
    public void handleRenderGameOverlay(final RenderGameOverlayEvent.Pre event) {
        event.setCanceled(true);
        mc.entityRenderer.setupOverlayRendering();
    }
}
