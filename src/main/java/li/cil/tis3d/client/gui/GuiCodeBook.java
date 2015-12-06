package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.ItemCodeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

/**
 * GUI for the code book, used to write and manage ASM programs.
 */
public class GuiCodeBook extends GuiScreen {
    private static final ResourceLocation LOCATION_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/codeBook.png");
    private static final int GUI_WIDTH = 148;
    private static final int GUI_HEIGHT = 230;
    private static final int BUTTON_PAGE_CHANGE_PREV_X = 8;
    private static final int BUTTON_PAGE_CHANGE_NEXT_X = 116;
    private static final int BUTTON_PAGE_CHANGE_Y = 224;
    private static final int BUTTON_PROGRAM_X = 20;
    private static final int BUTTON_PROGRAM_BASE_Y = 10;
    private static final int BUTTON_PROGRAM_HEIGHT = 26;
    private static final int BUTTONS_PER_PAGE = 8;

    private static final int ID_BUTTON_PAGE_NEXT = 1;
    private static final int ID_BUTTON_PAGE_PREV = 2;
    private static final int ID_BUTTON_PROGRAM_BASE = 3;

    private PageChangeButton buttonNextPage;
    private PageChangeButton buttonPreviousPage;

    private final EntityPlayer player;
    private final ItemCodeBook.Data data;
    private int page = 0;
    private int program = -1;

    public GuiCodeBook(final EntityPlayer player) {
        this.player = player;
        this.data = ItemCodeBook.Data.loadFromStack(player.getHeldItem());
    }

    @Override
    public void initGui() {
        super.initGui();

        final int x = (width - GUI_WIDTH) / 2;
        final int y = 2;

        // Buttons for next / previous page of pages.
        buttonList.add(buttonNextPage = new PageChangeButton(ID_BUTTON_PAGE_PREV, x + BUTTON_PAGE_CHANGE_PREV_X, y + BUTTON_PAGE_CHANGE_Y, PageChangeType.Previous));
        buttonList.add(buttonPreviousPage = new PageChangeButton(ID_BUTTON_PAGE_NEXT, x + BUTTON_PAGE_CHANGE_NEXT_X, y + BUTTON_PAGE_CHANGE_Y, PageChangeType.Next));

        for (int i = 0; i < BUTTONS_PER_PAGE; i++) {
            buttonList.add(new ProgramButton(ID_BUTTON_PROGRAM_BASE + i, x + BUTTON_PROGRAM_X, y + BUTTON_PROGRAM_BASE_Y + i * BUTTON_PROGRAM_HEIGHT));
        }

        updateButtons();
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        if (button == buttonNextPage) {
            changePage(1);
        } else if (button == buttonPreviousPage) {
            changePage(-1);
        }
    }

    private void changePage(final int delta) {
        page = Math.max(0, Math.min(data.getPageCount() - 1, page + delta));
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (!player.isEntityAlive() || !ItemCodeBook.isCodeBook(player.getHeldItem())) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }

        // Background.
        GlStateManager.color(1, 1, 1, 1);
        final int x = (width - GUI_WIDTH) / 2;
        final int y = 2;
        Minecraft.getMinecraft().getTextureManager().bindTexture(LOCATION_BACKGROUND);
        drawTexturedModalRect(x, y, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void updateButtons() {
        // TODO
        // Set visibility of buttons based on current page and set text.

        // Always use last button as "Add new program" button.
    }

    private class PageChangeButton extends GuiButton {
        private final static int BUTTON_WIDTH = 23;
        private final static int BUTTON_HEIGHT = 12;

        private final PageChangeType type;

        public PageChangeButton(final int buttonId, final int x, final int y, final PageChangeType type) {
            super(buttonId, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, "");
            this.type = type;
        }

        @Override
        public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!visible) {
                return;
            }

            final boolean isHovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(LOCATION_BACKGROUND);
            final int baseX = 110;
            final int baseY = 231;
            final int hoverOffsetX = isHovered ? BUTTON_WIDTH : 0;
            final int typeOffsetY = type == PageChangeType.Previous ? BUTTON_HEIGHT : 0;
            final int x = baseX + hoverOffsetX;
            final int y = baseY + typeOffsetY;
            drawTexturedModalRect(xPosition, yPosition, x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    public enum PageChangeType {
        Previous,
        Next
    }

    private class ProgramButton extends GuiButton {
        private final static int BUTTON_WIDTH = 104;
        private final static int BUTTON_HEIGHT = BUTTON_PROGRAM_HEIGHT;

        public ProgramButton(final int buttonId, final int x, final int y) {
            super(buttonId, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, "asd");
        }

        @Override
        public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!visible) {
                return;
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(LOCATION_BACKGROUND);
            GlStateManager.enableBlend();
            drawTexturedModalRect(xPosition, yPosition, 0, 230, BUTTON_WIDTH, BUTTON_HEIGHT);
            GlStateManager.disableBlend();

            // TODO
            //drawCenteredString(mc.fontRendererObj, displayString, xPosition + width / 2, yPosition + (height - 8) / 2, 0);

            final boolean isHovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            if (isHovered) {
                drawRect(xPosition, yPosition, xPosition + BUTTON_WIDTH, yPosition + BUTTON_HEIGHT, 0x33FFFFFF);
            }
        }
    }
}
