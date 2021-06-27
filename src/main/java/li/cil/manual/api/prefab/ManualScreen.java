package li.cil.manual.api.prefab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.manual.api.Manual;
import li.cil.manual.api.Tab;
import li.cil.manual.client.document.Document;
import li.cil.manual.api.Style;
import li.cil.manual.client.document.segment.InteractiveSegment;
import li.cil.manual.client.document.segment.Segment;
import li.cil.tis3d.client.renderer.Textures;
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

import java.util.*;

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

    private final Manual manual;
    private final Style style;
    private String currentPath;

    private int leftPos = 0;
    private int topPos = 0;

    private boolean isDragging = false;
    private Segment document = null;
    private int documentHeight = 0;
    private Optional<InteractiveSegment> currentSegment = Optional.empty();

    private ScrollButton scrollButton = null;

    public ManualScreen(final Manual manual, final Style style) {
        super(new StringTextComponent("Manual"));
        this.manual = manual;
        this.style = style;
    }

    public Manual getManual() {
        return manual;
    }

    @Override
    public void init() {
        super.init();

        this.leftPos = (width - windowWidth) / 2;
        this.topPos = (height - windowHeight) / 2;

        IterableUtils.forEachWithIndex(manual.getTabs(), (i, tab) -> {
            if (i >= maxTabsPerSide) return;
            final int x = leftPos + tabPosX;
            final int y = topPos + tabPosY + i * (tabHeight - tabOverlap);
            addButton(new TabButton(x, y, tabWidth, tabHeight - tabOverlap - 1, tab, (button) -> manual.push(tab.getPath())));
        });

        scrollButton = addButton(new ScrollButton(leftPos + scrollPosX, topPos + scrollPosY, 26, 13));
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        RenderSystem.enableBlend();

        if (!Objects.equals(currentPath, manual.peek())) {
            refreshPage();
            currentPath = manual.peek();
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        getMinecraft().getTextureManager().bind(Textures.LOCATION_GUI_MANUAL_BACKGROUND);
        blit(matrixStack, leftPos, topPos, 0, 0, windowWidth, windowHeight, windowWidth, windowHeight);

        scrollButton.active = canScroll();

        currentSegment = Document.render(matrixStack, document, leftPos + 16, topPos + 48, documentMaxWidth, documentMaxHeight, getScrollPosition(), mouseX, mouseY);

        currentSegment.flatMap(InteractiveSegment::getTooltip).ifPresent(t ->
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
            manual.pop();
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
            return currentSegment.map(s -> s.mouseClicked(mouseX, mouseY)).orElse(false);
        } else if (button == 1) {
            manual.pop();
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
        return maxScrollPosition() > 0;
    }

    private int getScrollPosition() {
        return manual.getUserData(ScrollOffset.class).
            map(offset -> offset.value).
            orElse(0);
    }

    private void setScrollPosition(final int value) {
        manual.setUserData(new ScrollOffset(value));
    }

    private int maxScrollPosition() {
        return documentHeight - documentMaxHeight;
    }

    private void refreshPage() {
        final Optional<Iterable<String>> content = manual.contentFor(manual.peek());
        document = Document.parse(manual, style, content.orElse(Collections.singleton("Page not found: " + manual.peek())));
        documentHeight = Document.height(document, documentMaxWidth);
        scrollTo(getScrollPosition());
    }

    private void scrollMouse(final double mouseY) {
        scrollTo((int) Math.round((mouseY - topPos - scrollPosY - 6.5) * maxScrollPosition() / (scrollHeight - 13.0)));
    }

    private void scroll(final double amount) {
        scrollTo(getScrollPosition() - (int) Math.round(style.getLineHeight() * 3 * amount));
    }

    private void scrollTo(final int row) {
        setScrollPosition(Math.max(0, Math.min(maxScrollPosition(), row)));
        final int yMin = topPos + scrollPosY;
        if (maxScrollPosition() > 0) {
            scrollButton.y = yMin + (scrollHeight - 13) * getScrollPosition() / maxScrollPosition();
        } else {
            scrollButton.y = yMin;
        }
    }

    private boolean isCoordinateOverScrollBar(final double x, final double y) {
        return x > scrollPosX && x < scrollPosX + scrollWidth &&
               y >= scrollPosY && y < scrollPosY + scrollHeight;
    }

    private static final class ScrollOffset {
        public final int value;

        public ScrollOffset(final int value) {
            this.value = value;
        }
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
            renderTooltip(matrixStack, new StringTextComponent(100 * getScrollPosition() / maxScrollPosition() + "%"),
                leftPos + scrollPosX + scrollWidth,
                y + getHeight() + 1);
        }
    }
}
