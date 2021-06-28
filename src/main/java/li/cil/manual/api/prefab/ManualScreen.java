package li.cil.manual.api.prefab;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import li.cil.manual.api.Tab;
import li.cil.manual.client.document.Document;
import li.cil.manual.client.document.segment.InteractiveSegment;
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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.*;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@OnlyIn(Dist.CLIENT)
public final class ManualScreen extends Screen {
    private static final int DOCUMENT_WIDTH = 220;
    private static final int DOCUMENT_HEIGHT = 176;
    private static final int SCROLL_BAR_X = 250;
    private static final int SCROLL_BAR_Y = 48;
    private static final int SCROLL_BAR_WIDTH = 26;
    private static final int SCROLL_BAR_HEIGHT = 180;
    private static final int TAB_ACTIVE_SHIFT_X = 20;
    private static final int TABS_X = -52 + TAB_ACTIVE_SHIFT_X;
    private static final int TABS_Y = 40;
    private static final int TAB_WIDTH = 64;
    private static final int TAB_HEIGHT = 32;
    private static final int TAB_OVERlAP = 8;
    private static final int MAX_TABS = 7;
    private static final int WINDOW_WIDTH = 256;
    private static final int WINDOW_HEIGHT = 256;

    private final Manual manual;
    private final Style style;
    private final Document document;
    private String currentPath;

    private int leftPos = 0;
    private int topPos = 0;
    private float scrollPos = 0;

    private boolean isDragging = false;
    private int documentHeight = 0;
    private Optional<InteractiveSegment> currentSegment = Optional.empty();

    private ScrollButton scrollButton = null;

    public ManualScreen(final Manual manual, final Style style) {
        super(new StringTextComponent("Manual"));
        this.manual = manual;
        this.style = style;
        this.document = new Document(style, manual);
    }

    public Manual getManual() {
        return manual;
    }

    @Override
    public void init() {
        super.init();

        this.leftPos = (width - WINDOW_WIDTH) / 2;
        this.topPos = (height - WINDOW_HEIGHT) / 2;

        IterableUtils.forEachWithIndex(manual.getTabs(), (i, tab) -> {
            if (i >= MAX_TABS) return;
            final int x = leftPos + TABS_X;
            final int y = topPos + TABS_Y + i * (TAB_HEIGHT - TAB_OVERlAP);
            addButton(new TabButton(x, y, tab, (button) -> manual.push(tab.getPath())));
        });

        scrollButton = addButton(new ScrollButton(leftPos + SCROLL_BAR_X, topPos + SCROLL_BAR_Y, 26, 13));
    }

    @Override
    public void render(final MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        RenderSystem.enableBlend();

        if (!Objects.equals(currentPath, manual.peek())) {
            refreshPage();
            currentPath = manual.peek();
        }

        scrollPos = MathHelper.lerp(partialTicks * 0.5f, scrollPos, getScrollPosition());
        scrollButton.y = getScrollButtonY();

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        getMinecraft().getTextureManager().bind(Textures.LOCATION_GUI_MANUAL_BACKGROUND);
        blit(matrixStack, leftPos, topPos, 0, 0, WINDOW_WIDTH, WINDOW_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT);

        scrollButton.active = canScroll();

        final int documentX = leftPos + 16;
        final int documentY = topPos + 48;

        matrixStack.pushPose();
        matrixStack.translate(documentX, documentY, 0);

        currentSegment = document.render(matrixStack, getSmoothScrollPosition(), DOCUMENT_WIDTH, DOCUMENT_HEIGHT, mouseX - documentX, mouseY - documentY);

        matrixStack.popPose();

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

        scrollBy(delta);
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
            scrollTo(mouseY);
            return true;
        } else if (button == 0) {
            return currentSegment.map(InteractiveSegment::mouseClicked).orElse(false);
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
            scrollTo(mouseY);
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
        return documentHeight - DOCUMENT_HEIGHT;
    }

    private void refreshPage() {
        final Optional<Iterable<String>> content = manual.contentFor(manual.peek());
        document.parse(content.orElse(Collections.singleton("Page not found: " + manual.peek())));
        documentHeight = document.height(DOCUMENT_WIDTH);
        scrollPos = getScrollPosition();
    }

    private void scrollTo(final double mouseY) {
        scrollTo((int) Math.round((mouseY - topPos - SCROLL_BAR_Y - 6.5) * maxScrollPosition() / (SCROLL_BAR_HEIGHT - 13.0)), true);
    }

    private void scrollBy(final double amount) {
        scrollTo(getScrollPosition() - (int) Math.round(style.getLineHeight() * 3 * amount), false);
    }

    private void scrollTo(final int row, final boolean immediate) {
        setScrollPosition(Math.max(0, Math.min(maxScrollPosition(), row)));
        if (immediate) {
            scrollPos = getScrollPosition();
        }
    }

    private int getSmoothScrollPosition() {
        if (scrollPos < getScrollPosition()) {
            return (int) Math.ceil(scrollPos);
        } else {
            return (int) Math.floor(scrollPos);
        }
    }

    private int getScrollButtonY() {
        final int yMin = topPos + SCROLL_BAR_Y;
        if (maxScrollPosition() > 0) {
            return yMin + (SCROLL_BAR_HEIGHT - 13) * getSmoothScrollPosition() / maxScrollPosition();
        } else {
            return yMin;
        }
    }

    private boolean isCoordinateOverScrollBar(final double x, final double y) {
        return x > SCROLL_BAR_X && x < SCROLL_BAR_X + SCROLL_BAR_WIDTH &&
               y >= SCROLL_BAR_Y && y < SCROLL_BAR_Y + SCROLL_BAR_HEIGHT;
    }

    private static final class ScrollOffset {
        public final int value;

        public ScrollOffset(final int value) {
            this.value = value;
        }
    }

    private class TabButton extends Button {
        private final Tab tab;
        private final int baseX;
        private float currentX;
        private int targetX;

        TabButton(final int x, final int y, final Tab tab, final IPressable action) {
            super(x, y, TAB_WIDTH, TAB_HEIGHT - TAB_OVERlAP - 1, StringTextComponent.EMPTY, action);
            this.tab = tab;
            this.baseX = x;
            this.currentX = x;
            this.targetX = x;
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
            if (isHovered()) {
                targetX = baseX - TAB_ACTIVE_SHIFT_X;
            } else {
                targetX = baseX;
            }

            currentX = MathHelper.lerp(partialTicks * 0.5f, currentX, targetX);

            if (currentX < targetX) {
                x = (int) Math.ceil(currentX);
            } else {
                x = (int) Math.floor(currentX);
            }

            width = TAB_WIDTH - TAB_ACTIVE_SHIFT_X - (x - baseX);

            getMinecraft().getTextureManager().bind(Textures.LOCATION_GUI_MANUAL_TAB);
            blit(matrixStack, x, y, 0, isHovered() ? TAB_HEIGHT : 0, TAB_WIDTH, TAB_HEIGHT, TAB_WIDTH, TAB_HEIGHT * 2);

            matrixStack.pushPose();
            matrixStack.translate(x + 15, (float) (y + 4), 0);

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
                leftPos + SCROLL_BAR_X + SCROLL_BAR_WIDTH,
                y + getHeight() + 1);
        }
    }
}
