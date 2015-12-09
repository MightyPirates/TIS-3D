package li.cil.tis3d.client.gui;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.ItemCodeBook;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageCodeBookData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * GUI for the code book, used to write and manage ASM programs.
 */
public class GuiCodeBook extends GuiScreen {
    private static final ResourceLocation LOCATION_BACKGROUND = new ResourceLocation(API.MOD_ID, "textures/gui/codeBook.png");
    private static final Pattern PATTERN_LINES = Pattern.compile("\r?\n");
    private static final int GUI_WIDTH = 148;
    private static final int GUI_HEIGHT = 230;
    private static final int BUTTON_PAGE_CHANGE_PREV_X = 8;
    private static final int BUTTON_PAGE_CHANGE_NEXT_X = 116;
    private static final int BUTTON_PAGE_CHANGE_Y = 224;
    private static final int CODE_POS_X = 18;
    private static final int CODE_POS_Y = 16;

    private static final int ID_BUTTON_PAGE_NEXT = 1;
    private static final int ID_BUTTON_PAGE_PREV = 2;

    private PageChangeButton buttonNextPage;
    private PageChangeButton buttonPreviousPage;

    private final EntityPlayer player;
    private final ItemCodeBook.Data data;

    private int selectedLine = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;

    // --------------------------------------------------------------------- //

    public GuiCodeBook(final EntityPlayer player) {
        this.player = player;
        this.data = ItemCodeBook.Data.loadFromStack(player.getHeldItem());
    }

    // --------------------------------------------------------------------- //

    @Override
    public void initGui() {
        super.initGui();

        final int x = (width - GUI_WIDTH) / 2;
        final int y = 2;

        // Buttons for next / previous page of pages.
        buttonList.add(buttonNextPage = new PageChangeButton(ID_BUTTON_PAGE_PREV, x + BUTTON_PAGE_CHANGE_PREV_X, y + BUTTON_PAGE_CHANGE_Y, PageChangeType.Previous));
        buttonList.add(buttonPreviousPage = new PageChangeButton(ID_BUTTON_PAGE_NEXT, x + BUTTON_PAGE_CHANGE_NEXT_X, y + BUTTON_PAGE_CHANGE_Y, PageChangeType.Next));
    }

    @Override
    protected void actionPerformed(final GuiButton button) throws IOException {
        if (button == buttonNextPage) {
            changePage(1);
        } else if (button == buttonPreviousPage) {
            changePage(-1);
        }
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

        // Check page change button availability.
        buttonNextPage.visible = data.getSelectedProgram() < data.getProgramCount() - 1;
        buttonPreviousPage.visible = data.getSelectedProgram() > 0 && data.getProgramCount() > 0;

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw current program.
        drawProgram(mouseX, mouseY);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // Save any changes made and send them to the server.
        final NBTTagCompound nbt = new NBTTagCompound();
        data.writeToNBT(nbt);
        Network.INSTANCE.getWrapper().sendToServer(new MessageCodeBookData(nbt));
    }

    // --------------------------------------------------------------------- //

    private void changePage(final int delta) {
        data.setSelectedProgram(data.getSelectedProgram() + delta);
    }

    private void drawProgram(final int mouseX, final int mouseY) {
        if (data.getProgramCount() < 1) {
            // Insert first program.
            data.addProgram("# Hello " + player.getDisplayNameString());
        }

        final String code = data.getProgram(data.getSelectedProgram());
        final String[] lines = PATTERN_LINES.split(code);

        final int x = (width - GUI_WIDTH) / 2;
        final int y = 2;

        for (int lineNumber = 0; lineNumber < lines.length; lineNumber++) {
            final String line = lines[lineNumber];
            final int offsetY = lineNumber * fontRendererObj.FONT_HEIGHT;
            final int lineX = x + CODE_POS_X;
            final int lineY = y + CODE_POS_Y + offsetY;
            if (lineNumber == selectedLine && selectionStart > selectionEnd) {
                final String left = line.substring(0, selectionStart);
                final String selected = line.substring(selectionStart, selectionEnd);
                final String right = line.substring(selectionEnd);
                final int offsetX = fontRendererObj.getStringWidth(left);
                final int selectionWidth = fontRendererObj.getStringWidth(selected);
                final int selectionX = x + CODE_POS_X + offsetX;
                drawRect(selectionX, lineY, selectionX + selectionWidth, lineY + fontRendererObj.FONT_HEIGHT, 0x99333333);
                fontRendererObj.drawString(left, lineX, lineY, 0xFF333333, false);
                fontRendererObj.drawString(left, selectionX, lineY, 0xFFEEEEEE, false);
                fontRendererObj.drawString(right, selectionX + selectionWidth, lineY, 0xFF333333, false);
            } else {
                fontRendererObj.drawString(line, lineX, lineY, 0xFF333333, false);
            }
        }
    }

    // --------------------------------------------------------------------- //

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

    private enum PageChangeType {
        Previous,
        Next
    }
}
