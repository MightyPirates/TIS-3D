package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.module.ModuleTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
//import net.minecraft.client.renderer.GlStateManager;
//import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
        //GlStateManager.disableTexture2D();

        // To be on the safe side (see manual.Document#render).
        //GL11.glDisableAlpha();

        GL11.glColor4f(1, 1, 1, 1);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        //GL11.enableDepth();
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glColorMask(false, false, false, false);
        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 500);

        RenderUtil.drawUntexturedQuad(8, 8, width - 16, height - 16);

        GL11.glPopMatrix();
        GL11.glDepthMask(false);
        GL11.glColorMask(true, true, true, true);

        RenderUtil.drawUntexturedQuad(4, 4, width - 8, height - 8);

        //GlStateManager.enableTexture2D();
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            super.keyTyped(typedChar, keyCode);
            return;
        }

        if (typedChar != '\0') {
            module.writeToInput(typedChar);

            if (Settings.animateTypingHand) {
                Minecraft.getMinecraft().thePlayer.swingItem();
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
    }
}
