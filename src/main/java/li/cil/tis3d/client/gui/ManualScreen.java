package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.manual.Tab;
import li.cil.tis3d.client.manual.Document;
import li.cil.tis3d.client.manual.segment.InteractiveSegment;
import li.cil.tis3d.client.manual.segment.Segment;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.api.ManualAPIImpl;
import li.cil.tis3d.util.IterableUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@OnlyIn(Dist.CLIENT)
public final class ManualScreen extends Screen {
    private static final int documentMaxWidth = 220;
    private static final int documentMaxHeight = 176;
    private static final int scrollPosX = 250;
    private static final int scrollPosY = 48;
    private static final int scrollWidth = 26;
    private static final int scrollHeight = 180;
    private static final int tabPosX = -52;
    private static final int tabPosY = 40;
    private static final int tabWidth = 64;
    private static final int tabHeight = 32;
    private static final int tabOverlap = 8;
    private static final int maxTabsPerSide = 7;
    private static final int windowWidth = 256;
    private static final int windowHeight = 256;

    private int leftPos = 0;
    private int topPos = 0;

    private boolean isDragging = false;
    private Segment document = null;
    private int documentHeight = 0;
    private Optional<InteractiveSegment> currentSegment = Optional.empty();

    private ScrollButton scrollButton = null;

    public ManualScreen() {
        super(new StringTextComponent("Manual"));
    }

    public void pushPage(final String path) {
        if (!ManualAPIImpl.peekPath().equals(path)) {
            ManualAPIImpl.pushPath(path);
            refreshPage();
        }
    }

    @Override
    public void init() {
        super.init();

        this.leftPos = (width - windowWidth) / 2;
        this.topPos = (height - windowHeight) / 2;

        IterableUtils.forEachWithIndex(ManualAPIImpl.getTabs(), (i, tab) -> {
            if (i >= maxTabsPerSide) return;
            final int x = leftPos + tabPosX;
            final int y = topPos + tabPosY + i * (tabHeight - tabOverlap);
            addButton(new TabButton(x, y, tabWidth, tabHeight - tabOverlap - 1, tab, (button) -> ManualAPI.navigate(tab.getPath())));
        });

        scrollButton = addButton(new ScrollButton(leftPos + scrollPosX, topPos + scrollPosY, 26, 13));

        refreshPage();
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        GlStateManager._enableBlend();

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        getMinecraft().getTextureManager().bind(Textures.LOCATION_GUI_MANUAL_BACKGROUND);
        blit(matrixStack, leftPos, topPos, 0, 0, windowWidth, windowHeight, windowWidth, windowHeight);

        scrollButton.active = canScroll();

        currentSegment = Document.render(matrixStack, document, leftPos + 16, topPos + 48, documentMaxWidth, documentMaxHeight, offset(), getFontRenderer(), mouseX, mouseY);

        currentSegment.flatMap(InteractiveSegment::tooltip).ifPresent(t ->
            renderWrappedToolTip(matrixStack, Collections.singletonList(t), mouseX, mouseY, getFontRenderer()));

        for (final Widget widget : this.buttons) {
            if (widget.active && (!isDragging || widget instanceof ScrollButton)) {
                widget.renderToolTip(matrixStack, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double delta) {
        if (super.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }

        scroll(delta);
        return true;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (getMinecraft().options.keyJump.matches(keyCode, scanCode)) {
            popPage();
            return true;
        } else if (getMinecraft().options.keyInventory.matches(keyCode, scanCode)) {
            final ClientPlayerEntity player = getMinecraft().player;
            if (player != null) {
                player.closeContainer();
            }
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        if (canScroll() && button == 0 && isCoordinateOverScrollBar(mouseX - leftPos, mouseY - topPos)) {
            isDragging = true;
            scrollButton.playDownSound(Minecraft.getInstance().getSoundManager());
            scrollMouse(mouseY);
            return true;
        } else if (button == 0) {
            currentSegment.ifPresent(s -> s.onMouseClick(mouseX, mouseY));
            return true;
        } else if (button == 1) {
            popPage();
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (isDragging) {
            scrollMouse(mouseY);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseReleased(final double mouseX, final double mouseY, final int button) {
        super.mouseReleased(mouseX, mouseY, button);

        if (button == 0) {
            isDragging = false;
        }

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // --------------------------------------------------------------------- //

    private FontRenderer getFontRenderer() {
        return font;
    }

    private boolean canScroll() {
        return maxOffset() > 0;
    }

    private int offset() {
        return ManualAPIImpl.peekOffset();
    }

    private int maxOffset() {
        return documentHeight - documentMaxHeight;
    }

    private void refreshPage() {
        final Iterable<String> content = ManualAPI.contentFor(ManualAPIImpl.peekPath());
        document = Document.parse(content != null ? content : Collections.singletonList("Document not found: " + ManualAPIImpl.peekPath()));
        documentHeight = Document.height(document, documentMaxWidth, getFontRenderer());
        scrollTo(offset());
    }

    private void popPage() {
        if (ManualAPIImpl.getHistorySize() > 1) {
            ManualAPIImpl.popPath();
            refreshPage();
        } else {
            final ClientPlayerEntity player = getMinecraft().player;
            if (player != null) {
                player.closeContainer();
            }
        }
    }

    private void scrollMouse(final double mouseY) {
        scrollTo((int) Math.round((mouseY - topPos - scrollPosY - 6.5) * maxOffset() / (scrollHeight - 13.0)));
    }

    private void scroll(final double amount) {
        scrollTo(offset() - (int) Math.round(Document.lineHeight(getFontRenderer()) * 3 * amount));
    }

    private void scrollTo(final int row) {
        ManualAPIImpl.setOffset(Math.max(0, Math.min(maxOffset(), row)));
        final int yMin = topPos + scrollPosY;
        if (maxOffset() > 0) {
            scrollButton.y = yMin + (scrollHeight - 13) * offset() / maxOffset();
        } else {
            scrollButton.y = yMin;
        }
    }

    private boolean isCoordinateOverScrollBar(final double x, final double y) {
        return x > scrollPosX && x < scrollPosX + scrollWidth &&
               y >= scrollPosY && y < scrollPosY + scrollHeight;
    }

    private class TabButton extends Button {
        private final Tab tab;

        TabButton(final int x, final int y, final int w, final int h, final Tab tab, final IPressable action) {
            super(x, y, w, h, StringTextComponent.EMPTY, action);
            this.tab = tab;
        }

        @Override
        public void renderToolTip(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
            if (!isHovered()) {
                return;
            }

            final List<ITextComponent> tooltip = new ArrayList<>();
            tab.getTooltip(tooltip);
            if (!tooltip.isEmpty()) {
                ManualScreen.this.renderWrappedToolTip(matrixStack, tooltip, mouseX, mouseY, getFontRenderer());
            }
        }

        @Override
        public void renderButton(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
            getMinecraft().getTextureManager().bind(Textures.LOCATION_GUI_MANUAL_TAB);
            blit(matrixStack, x, y, 0, isHovered() ? tabHeight : 0, tabWidth, tabHeight, tabWidth, tabHeight * 2);

            matrixStack.pushPose();
            matrixStack.translate(x + 30, (float) (y + 4), 0);

            tab.renderIcon(matrixStack);

            matrixStack.popPose();
        }
    }

    private class ScrollButton extends Button {
        ScrollButton(final int x, final int y, final int w, final int h) {
            super(x, y, w, h, StringTextComponent.EMPTY, (button) -> {});
        }

        @Override
        protected boolean clicked(final double mouseX, final double mouseY) {
            if (super.clicked(mouseX, mouseY)) {
                playDownSound(Minecraft.getInstance().getSoundManager());
            }
            return false;
        }

        @Override
        public void renderButton(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
            final int x0 = x;
            final int x1 = x + width;
            final int y0 = y;
            final int y1 = y + height;

            final float u0 = 0;
            final float u1 = u0 + 1;
            final float v0 = (isDragging || isHovered()) ? 0.5f : 0;
            final float v1 = v0 + 0.5f;

            getMinecraft().getTextureManager().bind(Textures.LOCATION_GUI_MANUAL_SCROLL);

            final Tessellator t = Tessellator.getInstance();
            final BufferBuilder builder = t.getBuilder();
            builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            builder.vertex(x0, y1, getBlitOffset()).uv(u0, v1).endVertex();
            builder.vertex(x1, y1, getBlitOffset()).uv(u1, v1).endVertex();
            builder.vertex(x1, y0, getBlitOffset()).uv(u1, v0).endVertex();
            builder.vertex(x0, y0, getBlitOffset()).uv(u0, v0).endVertex();
            t.end();
        }

        @Override
        public void renderToolTip(final MatrixStack matrixStack, final int mouseX, final int mouseY) {
            if (!isDragging && !isHovered() && !isCoordinateOverScrollBar(mouseX - leftPos, mouseY - topPos)) {
                return;
            }
            renderTooltip(matrixStack, new StringTextComponent(100 * offset() / maxOffset() + "%"),
                leftPos + scrollPosX + scrollWidth,
                y + getHeight() + 1);
        }
    }
}
