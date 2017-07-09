package li.cil.tis3d.client.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageBookCodeData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * GUI for the code book, used to write and manage ASM programs.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@SideOnly(Side.CLIENT)
public final class GuiBookCode extends GuiScreen {
    private static final int GUI_WIDTH = 148;
    private static final int GUI_HEIGHT = 230;
    private static final int BUTTON_PAGE_CHANGE_PREV_X = 8;
    private static final int BUTTON_PAGE_CHANGE_NEXT_X = 116;
    private static final int BUTTON_PAGE_CHANGE_Y = 224;
    private static final int BUTTON_PAGE_DELETE_X = 66;
    private static final int BUTTON_PAGE_DELETE_Y = 224;
    private static final int CODE_POS_X = 18;
    private static final int CODE_POS_Y = 16;
    private static final int CODE_WIDTH = 120;
    private static final int CODE_MARGIN = 30;
    private static final int PAGE_NUMBER_X = 74;
    private static final int PAGE_NUMBER_Y = 212;

    private static final int COLOR_CODE = 0xFF333333;
    private static final int COLOR_CODE_SELECTED = 0xFFEEEEEE;
    private static final int COLOR_SELECTION = 0xCC333399;

    private static final int ID_BUTTON_PAGE_NEXT = 1;
    private static final int ID_BUTTON_PAGE_PREV = 2;
    private static final int ID_BUTTON_PAGE_DELETE = 3;

    private ButtonChangePage buttonNextPage;
    private ButtonChangePage buttonPreviousPage;
    private ButtonDeletePage buttonDeletePage;

    private final EntityPlayer player;
    private final ItemBookCode.Data data;
    private final List<StringBuilder> lines = new ArrayList<>();

    private int guiX = 0;
    private int guiY = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private Optional<ParseException> compileError = Optional.empty();

    // --------------------------------------------------------------------- //

    public GuiBookCode(final EntityPlayer player) {
        this.player = player;
        this.data = ItemBookCode.Data.loadFromStack(player.getHeldItem());

        rebuildLines();
    }

    // --------------------------------------------------------------------- //

    @Override
    public void initGui() {
        super.initGui();

        guiX = (width - GUI_WIDTH) / 2;
        guiY = 2;

        // Buttons for next / previous page of pages.
        buttonList.add(buttonPreviousPage = new ButtonChangePage(ID_BUTTON_PAGE_PREV, guiX + BUTTON_PAGE_CHANGE_PREV_X, guiY + BUTTON_PAGE_CHANGE_Y, PageChangeType.Previous));
        buttonList.add(buttonNextPage = new ButtonChangePage(ID_BUTTON_PAGE_NEXT, guiX + BUTTON_PAGE_CHANGE_NEXT_X, guiY + BUTTON_PAGE_CHANGE_Y, PageChangeType.Next));
        buttonList.add(buttonDeletePage = new ButtonDeletePage(ID_BUTTON_PAGE_DELETE, guiX + BUTTON_PAGE_DELETE_X, guiY + BUTTON_PAGE_DELETE_Y));

        Keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();

        // Write changes back to our data tag.
        saveProgram();

        // Save any changes made and send them to the server.
        final NBTTagCompound nbt = new NBTTagCompound();
        data.writeToNBT(nbt);
        Network.INSTANCE.getWrapper().sendToServer(new MessageBookCodeData(nbt));

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        if (!player.isEntityAlive() || !Items.isBookCode(player.getHeldItem())) {
            Minecraft.getMinecraft().displayGuiScreen(null);
            return;
        }

        // Background.
        GL11.glColor4f(1, 1, 1, 1);
        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureLoader.LOCATION_BOOK_CODE_BACKGROUND);
        drawTexturedModalRect(guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // Check page change button availability.
        buttonPreviousPage.visible = data.getSelectedPage() > 0 && data.getPageCount() > 0;
        buttonNextPage.visible = (data.getSelectedPage() < data.getPageCount() - 1) ||
                                 (data.getSelectedPage() == data.getPageCount() - 1 && isCurrentProgramNonEmpty());
        buttonDeletePage.visible = data.getPageCount() > 1 || isCurrentProgramNonEmpty();

        super.drawScreen(mouseX, mouseY, partialTicks);

        // Draw current program.
        drawProgram(mouseX, mouseY);

        // Draw page number.
        final String pageInfo = String.format("%d/%d", data.getSelectedPage() + 1, data.getPageCount());
        fontRendererObj.drawString(pageInfo, guiX + PAGE_NUMBER_X - fontRendererObj.getStringWidth(pageInfo) / 2, guiY + PAGE_NUMBER_Y, COLOR_CODE);
    }

    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (isInCodeArea(mouseX, mouseY)) {
            final int line = cursorToLine(mouseY);
            final int column = cursorToColumn(mouseX + 2, mouseY);
            selectionStart = selectionEnd = positionToIndex(line, column);
        }
    }

    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int clickedMouseButton, final long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);

        if (isInCodeArea(mouseX, mouseY)) {
            final int line = cursorToLine(mouseY);
            final int column = cursorToColumn(mouseX + 2, mouseY);
            selectionEnd = positionToIndex(line, column);
        }
    }

    @Override
    protected void keyTyped(final char typedChar, final int keyCode) {
        super.keyTyped(typedChar, keyCode);

        final int line = indexToLine(getSelectionStart());
        final int column = indexToColumn(getSelectionStart());

        if (keyCode == Keyboard.KEY_LEFT) {
            if (column > 0 || line > 0) {
                if (isShiftKeyDown()) {
                    selectionEnd = selectionEnd - 1;
                } else {
                    selectionStart = selectionEnd = selectionEnd - 1;
                }
            }
        } else if (keyCode == Keyboard.KEY_RIGHT) {
            if (column < lines.get(line).length() || line < lines.size()) {
                if (isShiftKeyDown()) {
                    selectionEnd = selectionEnd + 1;
                } else {
                    selectionStart = selectionEnd = selectionEnd + 1;
                }
            }
        } else if (keyCode == Keyboard.KEY_UP) {
            final int currLine = indexToLine(selectionEnd);
            if (currLine > 0) {
                final int currColumn = indexToColumn(selectionEnd);
                final int x = columnToX(currLine, currColumn) + 2;
                final int prevLine = currLine - 1;
                final int prevColumn = xToColumn(x, prevLine);
                final int index = positionToIndex(prevLine, prevColumn);

                if (isShiftKeyDown()) {
                    selectionEnd = index;
                } else {
                    selectionStart = selectionEnd = index;
                }
            }
        } else if (keyCode == Keyboard.KEY_DOWN) {
            final int currLine = indexToLine(selectionEnd);
            if (currLine < lines.size() - 1) {
                final int currColumn = indexToColumn(selectionEnd);
                final int x = columnToX(currLine, currColumn) + 2;
                final int nextLine = currLine + 1;
                final int nextColumn = xToColumn(x, nextLine);
                final int index = positionToIndex(nextLine, nextColumn);

                if (isShiftKeyDown()) {
                    selectionEnd = index;
                } else {
                    selectionStart = selectionEnd = index;
                }
            }
        } else if (keyCode == Keyboard.KEY_HOME) {
            final int currLine = indexToLine(selectionEnd);
            if (isShiftKeyDown()) {
                selectionEnd = positionToIndex(currLine, 0);
            } else {
                selectionStart = selectionEnd = positionToIndex(currLine, 0);
            }
        } else if (keyCode == Keyboard.KEY_END) {
            final int currLine = indexToLine(selectionEnd);
            if (isShiftKeyDown()) {
                selectionEnd = positionToIndex(currLine, lines.get(currLine).length());
            } else {
                selectionStart = selectionEnd = positionToIndex(currLine, lines.get(currLine).length());
            }
        } else if (keyCode == Keyboard.KEY_DELETE) {
            if (!deleteSelection()) {
                if (isShiftKeyDown()) {
                    if (lines.size() > 1) {
                        lines.remove(line);
                    } else {
                        lines.get(0).setLength(0);
                    }
                    selectionStart = selectionEnd = positionToIndex(Math.min(lines.size() - 1, line), 0);
                } else if (column < lines.get(line).length()) {
                    lines.get(line).deleteCharAt(column);
                } else if (line < lines.size() - 1) {
                    final StringBuilder currLine = lines.get(line);
                    final StringBuilder nextLine = lines.get(line + 1);

                    if (currLine.length() + nextLine.length() < Settings.maxColumnsPerLine) {
                        currLine.append(nextLine);
                        lines.remove(line + 1);
                    }
                }
            }

            recompile();
        } else if (keyCode == Keyboard.KEY_BACK) {
            if (!deleteSelection()) {
                if (column > 0) {
                    lines.get(line).deleteCharAt(column - 1);
                } else if (line > 0) {
                    final StringBuilder prevLine = lines.get(line - 1);
                    final StringBuilder currLine = lines.get(line);

                    if (prevLine.length() + currLine.length() < Settings.maxColumnsPerLine) {
                        prevLine.append(currLine);
                        lines.remove(line);
                    }
                }

                selectionStart = selectionEnd = Math.max(0, selectionEnd - 1);
            }

            recompile();
        } else if (keyCode == Keyboard.KEY_RETURN) {
            deleteSelection();
            if (lines.size() < Constants.MAX_LINES_PER_PAGE) {
                final StringBuilder oldLine = lines.get(line);
                final StringBuilder newLine = new StringBuilder();
                if (column < oldLine.length()) {
                    newLine.append(oldLine.substring(column));
                    oldLine.setLength(column);
                }
                lines.add(line + 1, newLine);

                selectionStart = selectionEnd = selectionEnd + 1;
            }

            recompile();
        } else if (isCtrlKeyDown()) {
            if (keyCode == Keyboard.KEY_A) {
                selectionStart = 0;
                selectionEnd = positionToIndex(Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else if (keyCode == Keyboard.KEY_C) {
                setClipboardString(selectionToString());
            } else if (keyCode == Keyboard.KEY_X) {
                setClipboardString(selectionToString());
                deleteSelection();

                recompile();
            } else if (keyCode == Keyboard.KEY_V) {
                deleteSelection();

                final String[] pastedLines = Constants.PATTERN_LINES.split(getClipboardString());
                if (!isValidPaste(pastedLines)) {
                    return;
                }

                lines.get(line).insert(indexToColumn(column), pastedLines[0].toUpperCase(Locale.US));
                lines.addAll(line + 1, Arrays.stream(pastedLines).
                        skip(1).
                        map(l -> l.toUpperCase(Locale.US)).
                        map(StringBuilder::new).
                        collect(Collectors.toList()));

                selectionStart = selectionEnd = selectionEnd + pastedLines[0].length();
                for (int i = 1; i < pastedLines.length; i++) {
                    selectionStart = selectionEnd = selectionEnd + 1 + pastedLines[i].length();
                }

                recompile();
            }
        } else if (!Character.isISOControl(typedChar)) {
            deleteSelection();

            if (lines.get(line).length() < Settings.maxColumnsPerLine) {
                lines.get(line).insert(column, String.valueOf(typedChar).toUpperCase(Locale.US));
                selectionStart = selectionEnd = selectionEnd + 1;
            }

            recompile();
        }
    }

    @Override
    protected void actionPerformed(final GuiButton button) {
        if (button == buttonNextPage) {
            changePage(1);
        } else if (button == buttonPreviousPage) {
            changePage(-1);
        } else if (button == buttonDeletePage) {
            data.removePage(data.getSelectedPage());
            rebuildLines();
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    // --------------------------------------------------------------------- //

    private int getSelectionStart() {
        return Math.min(selectionStart, selectionEnd);
    }

    private int getSelectionEnd() {
        return Math.max(selectionStart, selectionEnd);
    }

    private boolean intersectsSelection(final int start, final int end) {
        return start < getSelectionEnd() && end > getSelectionStart();
    }

    private String selectionToString() {
        final int startLine = indexToLine(getSelectionStart());
        final int endLine = indexToLine(getSelectionEnd());
        if (selectionStart == selectionEnd) {
            return lines.get(startLine).toString();
        } else {
            final int startColumn = indexToColumn(getSelectionStart());
            final int endColumn = indexToColumn(getSelectionEnd());

            if (startLine == endLine) {
                return lines.get(startLine).substring(startColumn, endColumn);
            } else {
                final StringBuilder selection = new StringBuilder();
                selection.append(lines.get(startLine).subSequence(startColumn, lines.get(startLine).length())).append('\n');
                for (int line = startLine + 1; line < endLine; line++) {
                    selection.append(lines.get(line).toString()).append('\n');
                }
                selection.append(lines.get(endLine).subSequence(0, endColumn)).append('\n');
                return selection.toString();
            }
        }
    }

    private int cursorToLine(final int y) {
        return Math.max(0, Math.min(Math.min(lines.size() - 1, Constants.MAX_LINES_PER_PAGE), (y - guiY - CODE_POS_Y) / fontRendererObj.FONT_HEIGHT));
    }

    private int cursorToColumn(final int x, final int y) {
        return xToColumn(x, cursorToLine(y));
    }

    private int xToColumn(final int x, final int line) {
        final int relX = Math.max(0, x - guiX - CODE_POS_X);
        return fontRendererObj.trimStringToWidth(lines.get(line).toString(), relX).length();
    }

    private int columnToX(final int line, final int column) {
        return guiX + CODE_POS_X + fontRendererObj.getStringWidth(lines.get(line).substring(0, Math.min(column, lines.get(line).length())));
    }

    private int positionToIndex(final int line, final int column) {
        int index = 0;
        for (int l = 0; l < Math.min(line, lines.size()); l++) {
            index += lines.get(l).length() + 1;
        }
        index += Math.min(column, lines.get(Math.min(line, lines.size() - 1)).length());
        return index;
    }

    private int indexToLine(final int index) {
        int position = 0;
        for (int line = 0; line < lines.size(); line++) {
            position += lines.get(line).length() + 1;
            if (position > index) {
                return line;
            }
        }
        return lines.size() - 1; // Out of bounds, clamp.
    }

    private int indexToColumn(final int index) {
        int position = 0;
        for (final StringBuilder line : lines) {
            if (position + line.length() + 1 > index) {
                return index - position;
            }
            position += line.length() + 1;
        }
        return lines.get(lines.size() - 1).length(); // Out of bounds, clamp.
    }

    private boolean isInCodeArea(final int mouseX, final int mouseY) {
        return mouseX >= guiX + CODE_POS_X - CODE_MARGIN && mouseX <= guiX + CODE_POS_X + CODE_WIDTH + CODE_MARGIN &&
               mouseY >= guiY + CODE_POS_Y - CODE_MARGIN && mouseY <= guiY + CODE_POS_Y + fontRendererObj.FONT_HEIGHT * Constants.MAX_LINES_PER_PAGE + CODE_MARGIN;
    }

    private boolean isCurrentProgramNonEmpty() {
        return lines.size() > 1 || lines.get(0).length() > 0;
    }

    private void recompile() {
        compileError = Optional.empty();

        final List<String> program = lines.stream().map(StringBuilder::toString).collect(Collectors.toList());

        final List<String> leadingCode = new ArrayList<>();
        final List<String> trailingCode = new ArrayList<>();
        data.getExtendedProgram(data.getSelectedPage(), program, leadingCode, trailingCode);
        program.addAll(0, leadingCode);
        program.addAll(trailingCode);

        try {
            Compiler.compile(program, new MachineState());
        } catch (final ParseException e) {
            // Adjust line number for current page.
            final int lineNumber = e.getLineNumber() - leadingCode.size();
            compileError = Optional.of(new ParseException(e.getMessage(), lineNumber, e.getStart(), e.getEnd()));
        }
    }

    private boolean deleteSelection() {
        if (selectionStart != selectionEnd) {
            // Delete selection first.
            final int startLine = indexToLine(getSelectionStart());
            final int endLine = indexToLine(getSelectionEnd());

            final int startColumn = indexToColumn(getSelectionStart());
            final int endColumn = indexToColumn(getSelectionEnd());

            if (startLine == endLine) {
                lines.get(startLine).delete(startColumn, endColumn);
            } else {
                lines.get(startLine).delete(startColumn, lines.get(startLine).length());
                lines.get(endLine).delete(0, endColumn);
                lines.get(startLine).append(lines.get(endLine));
                for (int line = endLine; line > startLine; --line) {
                    lines.remove(line);
                }
            }

            selectionStart = selectionEnd = getSelectionStart();

            return true;
        }

        return false;
    }

    private boolean isValidPaste(final String[] pastedLines) {
        final int selectedLine = indexToLine(selectionEnd);

        if (pastedLines.length == 0) {
            return false; // Invalid paste, nothing to paste (this shouldn't even be possible).
        }
        if (pastedLines.length - 1 + lines.size() > Constants.MAX_LINES_PER_PAGE) {
            return false; // Invalid paste, too many resulting lines.
        }
        if (pastedLines[0].length() + lines.get(selectedLine).length() > Settings.maxColumnsPerLine) {
            return false; // Invalid paste, combined first line and current line too long.
        }
        for (final String pastedLine : pastedLines) {
            if (pastedLine.length() > Settings.maxColumnsPerLine) {
                return false; // Invalid paste, a line is too long.
            }
        }


        return true;
    }

    private void changePage(final int delta) {
        saveProgram();

        if (data.getSelectedPage() + delta == data.getPageCount()) {
            data.addPage();
        }
        data.setSelectedPage(data.getSelectedPage() + delta);
        selectionStart = selectionEnd = 0;

        rebuildLines();
    }

    private void saveProgram() {
        data.setPage(data.getSelectedPage(), lines.stream().map(StringBuilder::toString).collect(Collectors.toList()));
    }

    private void rebuildLines() {
        if (data.getPageCount() < 1) {
            data.addPage();
        }

        final List<String> program = data.getPage(data.getSelectedPage());
        lines.clear();
        program.forEach(line -> lines.add(new StringBuilder(line.toUpperCase(Locale.US))));

        recompile();
    }

    private void drawProgram(final int mouseX, final int mouseY) {
        int position = 0;
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            final StringBuilder line = lines.get(lineNumber);
            final int end = position + line.length();
            final int offsetY = lineNumber * fontRendererObj.FONT_HEIGHT;
            final int lineX = guiX + CODE_POS_X;
            final int lineY = guiY + CODE_POS_Y + offsetY;
            if (selectionStart != selectionEnd && intersectsSelection(position, end)) {
                // Line contains selection, highlight appropriately.
                int currX = lineX;
                // Number of chars before the selection in this line.
                final int prefix = Math.max(0, getSelectionStart() - position);
                // Number of chars selected in this line.
                final int selected = Math.min(line.length() - prefix, getSelectionEnd() - (position + prefix));

                final String prefixText = line.substring(0, prefix);
                fontRendererObj.drawString(prefixText, currX, lineY, COLOR_CODE, false);
                currX += fontRendererObj.getStringWidth(prefixText);

                final String selectedText = line.substring(prefix, prefix + selected);
                final int selectedWidth = fontRendererObj.getStringWidth(selectedText);
                drawRect(currX - 1, lineY - 1, currX + selectedWidth, lineY + fontRendererObj.FONT_HEIGHT - 1, COLOR_SELECTION);
                fontRendererObj.drawString(selectedText, currX, lineY, COLOR_CODE_SELECTED, false);
                currX += selectedWidth;

                final String postfixString = line.substring(prefix + selected);
                fontRendererObj.drawString(postfixString, currX, lineY, COLOR_CODE, false);
            } else {
                // No selection here, just draw the line. Get it? "draw the line"?
                fontRendererObj.drawString(line.toString(), lineX, lineY, COLOR_CODE, false);
            }

            position += line.length() + 1;
        }

        // Part one of error handling, draw red underline, *behind* the blinking cursor.
        if (compileError.isPresent()) {
            final ParseException exception = compileError.get();
            final int localLineNumber, startX, rawEndX;
            final boolean isErrorOnPreviousPage = exception.getLineNumber() < 0;
            final boolean isErrorOnNextPage = exception.getLineNumber() >= lines.size();
            if (isErrorOnPreviousPage) {
                localLineNumber = 0;
                startX = columnToX(localLineNumber, 0);
                rawEndX = columnToX(localLineNumber, Settings.maxColumnsPerLine);
            } else if (isErrorOnNextPage) {
                localLineNumber = lines.size() - 1;
                startX = columnToX(localLineNumber, 0);
                rawEndX = columnToX(localLineNumber, Settings.maxColumnsPerLine);
            } else {
                localLineNumber = exception.getLineNumber();
                startX = columnToX(localLineNumber, exception.getStart());
                rawEndX = columnToX(localLineNumber, exception.getEnd());
            }
            final int startY = guiY + CODE_POS_Y + localLineNumber * fontRendererObj.FONT_HEIGHT - 1;
            final int endX = Math.max(rawEndX, startX + fontRendererObj.getCharWidth(' '));

            drawRect(startX - 1, startY + fontRendererObj.FONT_HEIGHT - 1, endX, startY + fontRendererObj.FONT_HEIGHT, 0xFFFF3333);

            // Draw selection position in text.
            drawTextCursor();

            // Part two of error handling, draw tooltip, *on top* of blinking cursor.
            if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= startY + fontRendererObj.FONT_HEIGHT) {
                final List<String> tooltip = new ArrayList<>();
                if (isErrorOnPreviousPage) {
                    tooltip.add(I18n.format(Constants.MESSAGE_ERROR_ON_PREVIOUS_PAGE));
                } else if (isErrorOnNextPage) {
                    tooltip.add(I18n.format(Constants.MESSAGE_ERROR_ON_NEXT_PAGE));
                }
                tooltip.addAll(Arrays.asList(Constants.PATTERN_LINES.split(I18n.format(exception.getMessage()))));
                func_146283_a(tooltip, mouseX, mouseY);
                GL11.glDisable(GL11.GL_LIGHTING);
            }
        } else {
            // Draw selection position in text.
            drawTextCursor();
        }
    }

    private void drawTextCursor() {
        if (System.currentTimeMillis() % 800 <= 400) {
            final int line = indexToLine(selectionEnd);
            final int column = indexToColumn(selectionEnd);
            final StringBuilder sb = lines.get(line);
            final int x = guiX + CODE_POS_X + fontRendererObj.getStringWidth(sb.substring(0, column)) - 1;
            final int y = guiY + CODE_POS_Y + line * fontRendererObj.FONT_HEIGHT - 1;
            drawRect(x + 1, y + 1, x + 2 + 1, y + fontRendererObj.FONT_HEIGHT + 1, 0xCC333333);
            drawRect(x, y, x + 2, y + fontRendererObj.FONT_HEIGHT, COLOR_CODE_SELECTED);
        }
    }

    // --------------------------------------------------------------------- //

    private enum PageChangeType {
        Previous,
        Next
    }

    private class ButtonChangePage extends GuiButton {
        private static final int TEXTURE_X = 110;
        private static final int TEXTURE_Y = 231;
        private static final int BUTTON_WIDTH = 23;
        private static final int BUTTON_HEIGHT = 12;

        private final PageChangeType type;

        public ButtonChangePage(final int buttonId, final int x, final int y, final PageChangeType type) {
            super(buttonId, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, "");
            this.type = type;
        }

        @Override
        public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!visible) {
                return;
            }

            final boolean isHovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            GL11.glColor4f(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(TextureLoader.LOCATION_BOOK_CODE_BACKGROUND);
            final int offsetX = isHovered ? BUTTON_WIDTH : 0;
            final int offsetY = type == PageChangeType.Previous ? BUTTON_HEIGHT : 0;
            drawTexturedModalRect(xPosition, yPosition, TEXTURE_X + offsetX, TEXTURE_Y + offsetY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    private class ButtonDeletePage extends GuiButton {
        private static final int TEXTURE_X = 158;
        private static final int TEXTURE_Y = 231;
        private static final int BUTTON_WIDTH = 14;
        private static final int BUTTON_HEIGHT = 14;

        public ButtonDeletePage(final int buttonId, final int x, final int y) {
            super(buttonId, x, y, BUTTON_WIDTH, BUTTON_HEIGHT, "");
        }

        @Override
        public void drawButton(final Minecraft minecraft, final int mouseX, final int mouseY) {
            if (!visible) {
                return;
            }

            final boolean isHovered = mouseX >= xPosition && mouseY >= yPosition && mouseX < xPosition + width && mouseY < yPosition + height;
            GL11.glColor4f(1, 1, 1, 1);
            mc.getTextureManager().bindTexture(TextureLoader.LOCATION_BOOK_CODE_BACKGROUND);
            final int offsetX = isHovered ? BUTTON_WIDTH : 0;
            drawTexturedModalRect(xPosition, yPosition, TEXTURE_X + offsetX, TEXTURE_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }
}
