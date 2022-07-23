package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.common.module.TerminalModule;
import li.cil.tis3d.util.Color;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * Invisible GUI for the terminal module, purely used to grab keyboard input.
 */
@OnlyIn(Dist.CLIENT)
public final class TerminalModuleScreen extends Screen {
    private final TerminalModule module;

    public TerminalModuleScreen(final TerminalModule module) {
        super(Component.empty());
        this.module = module;
    }

    public boolean isFor(final TerminalModule that) {
        return that == module;
    }

    @Override
    protected void init() {
        super.init();

        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void removed() {
        super.removed();

        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @Override
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.depthMask(true);
        RenderSystem.colorMask(false, false, false, false);
        matrixStack.pushPose();
        matrixStack.translate(0, 0, 500);

        fill(matrixStack, 8, 8, width - 8, height - 8, 0xFFFFFFFF);

        matrixStack.popPose();
        RenderSystem.depthMask(false);
        RenderSystem.colorMask(true, true, true, true);

        fill(matrixStack, 4, 4, width - 4, height - 4, Color.WHITE);
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            writeToModule('\b');
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            writeToModule('\n');
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_TAB) {
            writeToModule('\t');
            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(final char typedChar, final int modifiers) {
        if (super.charTyped(typedChar, modifiers)) {
            return true;
        }

        if (typedChar != '\0') {
            writeToModule(typedChar);
            return true;
        }

        return false;
    }

    private void writeToModule(final char value) {
        module.writeToInput(value);

        final LocalPlayer player = getMinecraft().player;
        if (ClientConfig.animateTypingHand && player != null) {
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @SubscribeEvent
    public void handleRenderGameOverlay(final RenderGuiOverlayEvent.Pre event) {
        event.setCanceled(true);
    }
}
