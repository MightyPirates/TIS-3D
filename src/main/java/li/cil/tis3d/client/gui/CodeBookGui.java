package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.platform.GlStateManager;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CodeBookDataMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GUI for the code book, used to write and manage ASM programs.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Environment(EnvType.CLIENT)
public final class CodeBookGui extends Screen {
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

    private ButtonChangePage buttonNextPage;
    private ButtonChangePage buttonPreviousPage;
    private ButtonDeletePage buttonDeletePage;

    private final PlayerEntity player;
    private final Hand hand;
    private final CodeBookItem.Data data;
    private final List<StringBuilder> lines = new ArrayList<>();

    private int guiX = 0;
    private int guiY = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private Optional<ParseException> compileError = Optional.empty();

    // --------------------------------------------------------------------- //

    CodeBookGui(final PlayerEntity player, final Hand hand) {
        super(new LiteralText("Code Book"));
        this.player = player;
        this.hand = hand;
        this.data = CodeBookItem.Data.loadFromStack(player.getStackInHand(hand));

        rebuildLines();
    }

    // --------------------------------------------------------------------- //
    // GuiScreen

    @Override
    public void init() {
        super.init();

        guiX = (width - GUI_WIDTH) / 2;
        guiY = 2;

        // Buttons for next / previous page of pages.

        this.addButton(buttonPreviousPage = new ButtonChangePage(guiX + BUTTON_PAGE_CHANGE_PREV_X, guiY + BUTTON_PAGE_CHANGE_Y, PageChangeType.Previous, (button) -> changePage(-1)));
        this.addButton(buttonNextPage = new ButtonChangePage(guiX + BUTTON_PAGE_CHANGE_NEXT_X, guiY + BUTTON_PAGE_CHANGE_Y, PageChangeType.Next, (button) -> changePage(1)));
        this.addButton(buttonDeletePage = new ButtonDeletePage(guiX + BUTTON_PAGE_DELETE_X, guiY + BUTTON_PAGE_DELETE_Y, (button) -> {
            data.removePage(data.getSelectedPage());
            rebuildLines();
        }));

        client.keyboard.enableRepeatEvents(true);
    }

    @Override
    public void onClose() {
        super.onClose();

        // Write changes back to our data tag.
        saveProgram();

        // Save any changes made and send them to the server.
        final CompoundTag nbt = new CompoundTag();
        data.writeToNBT(nbt);
        Network.INSTANCE.sendToServer(new CodeBookDataMessage(nbt, hand));

        client.keyboard.enableRepeatEvents(false);
    }

    //~ @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        if (player.removed || !Items.isBookCode(player.getStackInHand(hand))) {
            client.openScreen(null);
            return;
        }

        // Background.
        GlStateManager.color4f(1, 1, 1, 1);
        client.getTextureManager().bindTexture(Textures.LOCATION_GUI_BOOK_CODE_BACKGROUND);
        //~ blit(guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // Check page change button availability.
        buttonPreviousPage.visible = data.getSelectedPage() > 0 && data.getPageCount() > 0;
        buttonNextPage.visible = (data.getSelectedPage() < data.getPageCount() - 1) ||
            (data.getSelectedPage() == data.getPageCount() - 1 && isCurrentProgramNonEmpty());
        buttonDeletePage.visible = data.getPageCount() > 1 || isCurrentProgramNonEmpty();

        //~ super.render(mouseX, mouseY, partialTicks);

        // Draw current program.
        drawProgram(mouseX, mouseY);

        // Draw page number.
        final String pageInfo = String.format("%d/%d", data.getSelectedPage() + 1, data.getPageCount());
        //~ getTextRenderer().draw(pageInfo, (float)(guiX + PAGE_NUMBER_X - getTextRenderer().getWidth(pageInfo) / 2), guiY + PAGE_NUMBER_Y, COLOR_CODE);
    }

    @Override
    public boolean mouseClicked(final double mouseXd, final double mouseYd, final int mouseButton) {
        if (super.mouseClicked(mouseXd, mouseYd, mouseButton)) {
            return true;
        }

        final int mouseX = (int)Math.round(mouseXd);
        final int mouseY = (int)Math.round(mouseYd);

        if (isInCodeArea(mouseX, mouseY)) {
            final int line = cursorToLine(mouseY);
            final int column = cursorToColumn(mouseX + 2, mouseY);
            selectionStart = selectionEnd = positionToIndex(line, column);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseDragged(final double mouseXd, final double mouseYd, final int clickedMouseButton, final double da, final double db) {
        if (super.mouseDragged(mouseXd, mouseYd, clickedMouseButton, da, db)) {
            return true;
        }

        final int mouseX = (int)Math.round(mouseXd);
        final int mouseY = (int)Math.round(mouseYd);

        if (isInCodeArea(mouseX, mouseY)) {
            final int line = cursorToLine(mouseY);
            final int column = cursorToColumn(mouseX + 2, mouseY);
            selectionEnd = positionToIndex(line, column);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scancode, final int mods) {
        if (super.keyPressed(keyCode, scancode, mods)) {
            return true;
        }

        final int line = indexToLine(getSelectionStart());
        final int column = indexToColumn(getSelectionStart());

        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (column > 0 || line > 0) {
                if (hasShiftDown()) {
                    selectionEnd = selectionEnd - 1;
                } else {
                    selectionStart = selectionEnd = selectionEnd - 1;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (column < lines.get(line).length() || line < lines.size() - 1) {
                if (hasShiftDown()) {
                    selectionEnd = selectionEnd + 1;
                } else {
                    selectionStart = selectionEnd = selectionEnd + 1;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_UP) {
            final int currLine = indexToLine(selectionEnd);
            if (currLine > 0) {
                final int currColumn = indexToColumn(selectionEnd);
                final int x = columnToX(currLine, currColumn) + 2;
                final int prevLine = currLine - 1;
                final int prevColumn = xToColumn(x, prevLine);
                final int index = positionToIndex(prevLine, prevColumn);

                if (hasShiftDown()) {
                    selectionEnd = index;
                } else {
                    selectionStart = selectionEnd = index;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_DOWN) {
            final int currLine = indexToLine(selectionEnd);
            if (currLine < lines.size() - 1) {
                final int currColumn = indexToColumn(selectionEnd);
                final int x = columnToX(currLine, currColumn) + 2;
                final int nextLine = currLine + 1;
                final int nextColumn = xToColumn(x, nextLine);
                final int index = positionToIndex(nextLine, nextColumn);

                if (hasShiftDown()) {
                    selectionEnd = index;
                } else {
                    selectionStart = selectionEnd = index;
                }
            }
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            final int currLine = indexToLine(selectionEnd);
            if (hasShiftDown()) {
                selectionEnd = positionToIndex(currLine, 0);
            } else {
                selectionStart = selectionEnd = positionToIndex(currLine, 0);
            }
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            final int currLine = indexToLine(selectionEnd);
            if (hasShiftDown()) {
                selectionEnd = positionToIndex(currLine, lines.get(currLine).length());
            } else {
                selectionStart = selectionEnd = positionToIndex(currLine, lines.get(currLine).length());
            }
        } else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (!deleteSelection()) {
                if (hasShiftDown()) {
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
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
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
        } else if (keyCode == GLFW.GLFW_KEY_ENTER) {
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
        } else if (hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                selectionStart = 0;
                selectionEnd = positionToIndex(Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else if (keyCode == GLFW.GLFW_KEY_C) {
                client.keyboard.setClipboard(selectionToString());
            } else if (keyCode == GLFW.GLFW_KEY_X) {
                client.keyboard.setClipboard(selectionToString());
                deleteSelection();

                recompile();
            } else if (keyCode == GLFW.GLFW_KEY_V) {
                deleteSelection();

                final String[] pastedLines = Constants.PATTERN_LINES.split(client.keyboard.getClipboard());
                if (!isValidPaste(pastedLines)) {
                    return true;
                }

                lines.get(line).insert(indexToColumn(column), pastedLines[0].toUpperCase(Locale.US));
                lines.addAll(line + 1, Arrays
                    .stream(pastedLines)
                    .skip(1)
                    .map(l -> l.toUpperCase(Locale.US))
                    .map(StringBuilder::new)
                    .collect(Collectors.toList()));

                selectionStart = selectionEnd = selectionEnd + pastedLines[0].length();
                for (int i = 1; i < pastedLines.length; i++) {
                    selectionStart = selectionEnd = selectionEnd + 1 + pastedLines[i].length();
                }

                recompile();
            }
        } else {
            return false;
        }

        return true;
    }

    @Override
    public boolean charTyped(final char chr, final int code) {
        if (super.charTyped(chr, code)) {
            return true;
        } else if (Character.isISOControl(chr)) {
            return false;
        }

        deleteSelection();

        final int line = indexToLine(getSelectionStart());
        final int column = indexToColumn(getSelectionStart());

        if (lines.get(line).length() < Settings.maxColumnsPerLine) {
            lines.get(line).insert(column, String.valueOf(chr).toUpperCase(Locale.US));
            selectionStart = selectionEnd = selectionEnd + 1;
        }

        recompile();

        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Nullable
    @Override
    public Element getFocused() {
        return null; // We don't want space to trigger buttons, all input is interpreted as text input in this UI.
    }

    // --------------------------------------------------------------------- //

    private TextRenderer getTextRenderer() {
        return textRenderer;
    }

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
        return Math.max(0, Math.min(Math.min(lines.size() - 1, Constants.MAX_LINES_PER_PAGE), (y - guiY - CODE_POS_Y) / getTextRenderer().fontHeight));
    }

    private int cursorToColumn(final int x, final int y) {
        return xToColumn(x, cursorToLine(y));
    }

    private int xToColumn(final int x, final int line) {
        final int relX = Math.max(0, x - guiX - CODE_POS_X);
        return getTextRenderer().trimToWidth(lines.get(line).toString(), relX).length();
    }

    private int columnToX(final int line, final int column) {
        return guiX + CODE_POS_X + getTextRenderer().getWidth(lines.get(line).substring(0, Math.min(column, lines.get(line).length())));
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

    private boolean isInCodeArea(final double mouseX, final double mouseY) {
        return mouseX >= guiX + CODE_POS_X - CODE_MARGIN && mouseX <= guiX + CODE_POS_X + CODE_WIDTH + CODE_MARGIN &&
            mouseY >= guiY + CODE_POS_Y - CODE_MARGIN && mouseY <= guiY + CODE_POS_Y + getTextRenderer().fontHeight * Constants.MAX_LINES_PER_PAGE + CODE_MARGIN;
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
                lines.subList(startLine + 1, endLine + 1).clear();
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
            final int offsetY = lineNumber * getTextRenderer().fontHeight;
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
                //~ getTextRenderer().draw(prefixText, currX, lineY, COLOR_CODE);
                currX += getTextRenderer().getWidth(prefixText);

                final String selectedText = line.substring(prefix, prefix + selected);
                final int selectedWidth = getTextRenderer().getWidth(selectedText);
                //~ fill(currX - 1, lineY - 1, currX + selectedWidth, lineY + getTextRenderer().fontHeight - 1, COLOR_SELECTION);
                //~ getTextRenderer().draw(selectedText, currX, lineY, COLOR_CODE_SELECTED);
                currX += selectedWidth;

                final String postfixString = line.substring(prefix + selected);
                //~ getTextRenderer().draw(postfixString, currX, lineY, COLOR_CODE);
            } else {
                // No selection here, just draw the line. Get it? "draw the line"?
                //~ getTextRenderer().draw(line.toString(), lineX, lineY, COLOR_CODE);
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
            final int startY = guiY + CODE_POS_Y + localLineNumber * getTextRenderer().fontHeight - 1;
            final int endX = Math.max(rawEndX, startX + getTextRenderer().getWidth(" "));

            //~ fill(startX - 1, startY + getTextRenderer().fontHeight - 1, endX, startY + getTextRenderer().fontHeight, 0xFFFF3333);

            // Draw selection position in text.
            drawTextCursor();

            // Part two of error handling, draw tooltip, *on top* of blinking cursor.
            if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= startY + getTextRenderer().fontHeight) {
                final List<String> tooltip = new ArrayList<>();
                if (isErrorOnPreviousPage) {
                    tooltip.add(I18n.translate(Constants.MESSAGE_ERROR_ON_PREVIOUS_PAGE));
                } else if (isErrorOnNextPage) {
                    tooltip.add(I18n.translate(Constants.MESSAGE_ERROR_ON_NEXT_PAGE));
                }
                tooltip.addAll(Arrays.asList(Constants.PATTERN_LINES.split(I18n.translate(exception.getMessage())))
                );
                //~ renderTooltip(tooltip, mouseX, mouseY);
                GlStateManager.disableLighting();
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
            final int x = guiX + CODE_POS_X + getTextRenderer().getWidth(sb.substring(0, column)) - 1;
            final int y = guiY + CODE_POS_Y + line * getTextRenderer().fontHeight - 1;
            //~ fill(x + 1, y + 1, x + 2 + 1, y + getTextRenderer().fontHeight + 1, 0xCC333333);
            //~ fill(x, y, x + 2, y + getTextRenderer().fontHeight, COLOR_CODE_SELECTED);
        }
    }

    // --------------------------------------------------------------------- //

    private enum PageChangeType {
        Previous,
        Next
    }

    private class ButtonChangePage extends ButtonWidget {
        private static final int TEXTURE_X = 110;
        private static final int TEXTURE_Y = 231;
        private static final int BUTTON_WIDTH = 23;
        private static final int BUTTON_HEIGHT = 12;

        private final PageChangeType type;

        ButtonChangePage(final int x, final int y, final PageChangeType type, final PressAction action) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, LiteralText.EMPTY, action);
            this.type = type;
        }

        //~ @Override
        public void render(final int mouseX, final int mouseY, final float partialTicks) {
            if (!visible) {
                return;
            }

            final boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            client.getTextureManager().bindTexture(Textures.LOCATION_GUI_BOOK_CODE_BACKGROUND);
            final int offsetX = isHovered ? BUTTON_WIDTH : 0;
            final int offsetY = type == PageChangeType.Previous ? BUTTON_HEIGHT : 0;
            //~ blit(x, y, TEXTURE_X + offsetX, TEXTURE_Y + offsetY, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }

    private class ButtonDeletePage extends ButtonWidget {
        private static final int TEXTURE_X = 158;
        private static final int TEXTURE_Y = 231;
        private static final int BUTTON_WIDTH = 14;
        private static final int BUTTON_HEIGHT = 14;

        ButtonDeletePage(final int x, final int y, final PressAction action) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, LiteralText.EMPTY, action);
        }

        //~ @Override
        public void render(final int mouseX, final int mouseY, final float partialTicks) {
            if (!visible) {
                return;
            }

            final boolean isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            client.getTextureManager().bindTexture(Textures.LOCATION_GUI_BOOK_CODE_BACKGROUND);
            final int offsetX = isHovered ? BUTTON_WIDTH : 0;
            //~ blit(x, y, TEXTURE_X + offsetX, TEXTURE_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
    }
}
