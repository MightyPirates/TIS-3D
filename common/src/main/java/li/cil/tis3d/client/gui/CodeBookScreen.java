package li.cil.tis3d.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.common.config.Constants;
import li.cil.tis3d.common.item.CodeBookItem;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.module.execution.MachineState;
import li.cil.tis3d.common.module.execution.compiler.Compiler;
import li.cil.tis3d.common.module.execution.compiler.ParseException;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.CodeBookDataMessage;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * GUI for the code book, used to write and manage ASM programs.
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public final class CodeBookScreen extends Screen {
    private static final Component ERROR_ON_PREVIOUS_PAGE_TOOLTIP = Component.translatable("tis3d.code_book.error_on_previous_page");
    private static final Component ERROR_ON_NEXT_PAGE_TOOLTIP = Component.translatable("tis3d.code_book.error_on_next_page");
    private static final Component PREVIOUS_PAGE_TOOLTIP = Component.translatable("tis3d.code_book.previous_page");
    private static final Component NEXT_PAGE_TOOLTIP = Component.translatable("tis3d.code_book.next_page");
    private static final Component DELETE_PAGE_TOOLTIP = Component.translatable("tis3d.code_book.delete_page");

    private static final int GUI_WIDTH = 218;
    private static final int GUI_HEIGHT = 230;
    private static final int BUTTON_PAGE_CHANGE_PREV_X = 8;
    private static final int BUTTON_PAGE_CHANGE_NEXT_X = 186;
    private static final int BUTTON_PAGE_CHANGE_Y = 224;
    private static final int BUTTON_PAGE_DELETE_X = 101;
    private static final int BUTTON_PAGE_DELETE_Y = 224;
    private static final int CODE_POS_X = 18;
    private static final int CODE_POS_Y = 16;
    private static final int CODE_WIDTH = 190;
    private static final int CODE_MARGIN = 10;
    private static final int PAGE_NUMBER_X = 109;
    private static final int PAGE_NUMBER_Y = 212;

    private static final int COLOR_CODE = 0xFF333333;
    private static final int COLOR_CODE_SELECTED = 0xFFEEEEEE;
    private static final int COLOR_SELECTION = 0xCC333399;

    private ButtonChangePage buttonNextPage;
    private ButtonChangePage buttonPreviousPage;
    private ButtonDeletePage buttonDeletePage;

    private final Player player;
    private final InteractionHand hand;
    private final CodeBookItem.Data data;
    private final List<StringBuilder> lines = new ArrayList<>();

    private int guiX = 0;
    private int guiY = 0;
    private int selectionStart = 0;
    private int selectionEnd = 0;
    private Optional<ParseException> compileError = Optional.empty();

    // --------------------------------------------------------------------- //

    public CodeBookScreen(final Player player, final InteractionHand hand) {
        super(Component.literal("Code Book"));
        this.player = player;
        this.hand = hand;
        this.data = CodeBookItem.Data.loadFromStack(player.getItemInHand(InteractionHand.MAIN_HAND));

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
        buttonPreviousPage = addRenderableWidget(new ButtonChangePage(guiX + BUTTON_PAGE_CHANGE_PREV_X, guiY + BUTTON_PAGE_CHANGE_Y, PageChangeType.Previous, button -> changePage(-1)));
        buttonNextPage = addRenderableWidget(new ButtonChangePage(guiX + BUTTON_PAGE_CHANGE_NEXT_X, guiY + BUTTON_PAGE_CHANGE_Y, PageChangeType.Next, button -> changePage(1)));
        buttonDeletePage = addRenderableWidget(new ButtonDeletePage(guiX + BUTTON_PAGE_DELETE_X, guiY + BUTTON_PAGE_DELETE_Y, button -> deletePage()));

        getMinecraft().keyboardHandler.setSendRepeatsToGui(true);
    }

    @Override
    public void removed() {
        super.removed();

        // Write changes back to our data tag.
        saveProgram();

        // Save any changes made and send them to the server.
        final CompoundTag tag = new CompoundTag();
        data.save(tag);
        Network.sendToServer(new CodeBookDataMessage(hand, tag));

        getMinecraft().keyboardHandler.setSendRepeatsToGui(false);
    }

    @Override
    public void render(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        if (!player.isAlive() || !Items.is(player.getItemInHand(hand), Items.BOOK_CODE)) {
            Minecraft.getInstance().setScreen(null);
            return;
        }

        renderBackground(matrixStack);

        // Background.
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, Textures.LOCATION_GUI_BOOK_CODE_BACKGROUND);
        blit(matrixStack, guiX, guiY, 0, 0, GUI_WIDTH, GUI_HEIGHT);

        // Check page change button availability.
        buttonPreviousPage.visible = data.getSelectedPage() > 0 && data.getPageCount() > 0;
        buttonNextPage.visible = (data.getSelectedPage() < data.getPageCount() - 1) ||
            (data.getSelectedPage() == data.getPageCount() - 1 && isCurrentProgramNonEmpty());
        buttonDeletePage.visible = data.getPageCount() > 1 || isCurrentProgramNonEmpty();

        super.render(matrixStack, mouseX, mouseY, partialTicks);

        // Draw current program.
        drawProgram(matrixStack, mouseX, mouseY);

        // Draw page number.
        final String pageInfo = String.format("%d/%d", data.getSelectedPage() + 1, data.getPageCount());
        final int x = guiX + PAGE_NUMBER_X - getFontRenderer().width(pageInfo) / 2;
        final int y = guiY + PAGE_NUMBER_Y;
        getFontRenderer().draw(matrixStack, pageInfo, x, y, COLOR_CODE);
    }

    @Override
    public boolean mouseClicked(final double mouseX, final double mouseY, final int mouseButton) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton)) {
            return true;
        }

        if (isInCodeArea(mouseX, mouseY)) {
            final int line = cursorToLine(mouseY);
            final int column = cursorToColumn(mouseX + 2, mouseY);
            selectionStart = selectionEnd = positionToIndex(line, column);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseDragged(final double mouseX, final double mouseY, final int button, final double dragX, final double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        }

        if (isInCodeArea(mouseX, mouseY)) {
            final int line = cursorToLine(mouseY);
            final int column = cursorToColumn(mouseX + 2, mouseY);
            selectionEnd = positionToIndex(line, column);
            return true;
        }

        return false;
    }

    @Override
    public boolean keyPressed(final int keyCode, final int scanCode, final int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
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

            return true;
        } else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (column < lines.get(line).length() || line < lines.size() - 1) {
                if (hasShiftDown()) {
                    selectionEnd = selectionEnd + 1;
                } else {
                    selectionStart = selectionEnd = selectionEnd + 1;
                }
            }

            return true;
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

            return true;
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

            return true;
        } else if (keyCode == GLFW.GLFW_KEY_HOME) {
            final int currLine = indexToLine(selectionEnd);
            if (hasShiftDown()) {
                selectionEnd = positionToIndex(currLine, 0);
            } else {
                selectionStart = selectionEnd = positionToIndex(currLine, 0);
            }

            return true;
        } else if (keyCode == GLFW.GLFW_KEY_END) {
            final int currLine = indexToLine(selectionEnd);
            if (hasShiftDown()) {
                selectionEnd = positionToIndex(currLine, lines.get(currLine).length());
            } else {
                selectionStart = selectionEnd = positionToIndex(currLine, lines.get(currLine).length());
            }

            return true;
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

                    if (currLine.length() + nextLine.length() < Constants.MAX_CHARS_PER_LINE) {
                        currLine.append(nextLine);
                        lines.remove(line + 1);
                    }
                }
            }

            recompile();

            return true;
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!deleteSelection()) {
                if (column > 0) {
                    lines.get(line).deleteCharAt(column - 1);
                } else if (line > 0) {
                    final StringBuilder prevLine = lines.get(line - 1);
                    final StringBuilder currLine = lines.get(line);

                    if (prevLine.length() + currLine.length() < Constants.MAX_CHARS_PER_LINE) {
                        prevLine.append(currLine);
                        lines.remove(line);
                    }
                }

                selectionStart = selectionEnd = Math.max(0, selectionEnd - 1);
            }

            recompile();

            return true;
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

            return true;
        } else if (hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_A) {
                selectionStart = 0;
                selectionEnd = positionToIndex(Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else if (keyCode == GLFW.GLFW_KEY_C) {
                getMinecraft().keyboardHandler.setClipboard(selectionToString());
            } else if (keyCode == GLFW.GLFW_KEY_X) {
                getMinecraft().keyboardHandler.setClipboard(selectionToString());
                deleteSelection();

                recompile();
            } else if (keyCode == GLFW.GLFW_KEY_V) {
                deleteSelection();

                final String[] pastedLines = Constants.PATTERN_LINES.split(getMinecraft().keyboardHandler.getClipboard());
                if (!isValidPaste(pastedLines)) {
                    return true;
                }

                lines.get(line).insert(indexToColumn(column), applyCodeStyle(pastedLines[0]));
                lines.addAll(line + 1, Arrays.stream(pastedLines).
                    skip(1).
                    map(CodeBookScreen::applyCodeStyle).
                    map(StringBuilder::new).
                    toList());

                selectionStart = selectionEnd = selectionEnd + pastedLines[0].length();
                for (int i = 1; i < pastedLines.length; i++) {
                    selectionStart = selectionEnd = selectionEnd + 1 + pastedLines[i].length();
                }

                recompile();
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean charTyped(final char codePoint, final int modifiers) {
        if (super.charTyped(codePoint, modifiers)) {
            return true;
        }
        if (Character.isISOControl(codePoint)) {
            return false;
        }

        deleteSelection();

        final int line = indexToLine(getSelectionStart());
        final int column = indexToColumn(getSelectionStart());

        if (lines.get(line).length() < Constants.MAX_CHARS_PER_LINE) {
            lines.get(line).insert(column, applyCodeStyle(String.valueOf(codePoint)));
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
    public GuiEventListener getFocused() {
        // We don't want space to trigger buttons, all input is interpreted as text input in this UI.
        return null;
    }

    // --------------------------------------------------------------------- //

    private static String applyCodeStyle(final String value) {
        if (ClientConfig.autoCodeUpperCase) {
            return value.toUpperCase(Locale.US);
        } else {
            return value;
        }
    }

    private Minecraft getMinecraft() {
        return Objects.requireNonNull(minecraft);
    }

    private Font getFontRenderer() {
        return font;
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

    private int cursorToLine(final double y) {
        return (int) Math.max(0, Math.min(Math.min(lines.size() - 1, Constants.MAX_LINES_PER_PAGE), (y - guiY - CODE_POS_Y) / getFontRenderer().lineHeight));
    }

    private int cursorToColumn(final double x, final double y) {
        return xToColumn(x, cursorToLine(y));
    }

    private int xToColumn(final double x, final int line) {
        final int relX = (int) Math.max(0, x - guiX - CODE_POS_X);
        return getFontRenderer().plainSubstrByWidth(lines.get(line).toString(), relX).length();
    }

    private int columnToX(final int line, final int column) {
        return guiX + CODE_POS_X + getFontRenderer().width(lines.get(line).substring(0, Math.min(column, lines.get(line).length())));
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
            mouseY >= guiY + CODE_POS_Y - CODE_MARGIN && mouseY <= guiY + CODE_POS_Y + getFontRenderer().lineHeight * Constants.MAX_LINES_PER_PAGE + CODE_MARGIN;
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
            compileError = Optional.of(new ParseException(e.getDisplayMessage(), lineNumber, e.getStart(), e.getEnd()));
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
        if (pastedLines[0].length() + lines.get(selectedLine).length() > Constants.MAX_CHARS_PER_LINE) {
            return false; // Invalid paste, combined first line and current line too long.
        }
        for (final String pastedLine : pastedLines) {
            if (pastedLine.length() > Constants.MAX_CHARS_PER_LINE) {
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

    private void deletePage() {
        data.removePage(data.getSelectedPage());
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
        program.forEach(line -> lines.add(new StringBuilder(applyCodeStyle(line))));

        recompile();
    }

    private void drawProgram(final PoseStack matrixStack, final int mouseX, final int mouseY) {
        int position = 0;
        for (int lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
            final StringBuilder line = lines.get(lineNumber);
            final int end = position + line.length();
            final int offsetY = lineNumber * getFontRenderer().lineHeight;
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
                getFontRenderer().draw(matrixStack, prefixText, currX, lineY, COLOR_CODE);
                currX += getFontRenderer().width(prefixText);

                final String selectedText = line.substring(prefix, prefix + selected);
                final int selectedWidth = getFontRenderer().width(selectedText);
                fill(matrixStack, currX - 1, lineY - 1, currX + selectedWidth, lineY + getFontRenderer().lineHeight - 1, COLOR_SELECTION);
                getFontRenderer().draw(matrixStack, selectedText, currX, lineY, COLOR_CODE_SELECTED);
                currX += selectedWidth;

                final String postfixString = line.substring(prefix + selected);
                getFontRenderer().draw(matrixStack, postfixString, currX, lineY, COLOR_CODE);
            } else {
                // No selection here, just draw the line. Get it? "draw the line"?
                getFontRenderer().draw(matrixStack, line.toString(), lineX, lineY, COLOR_CODE);
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
                rawEndX = columnToX(localLineNumber, Constants.MAX_CHARS_PER_LINE);
            } else if (isErrorOnNextPage) {
                localLineNumber = lines.size() - 1;
                startX = columnToX(localLineNumber, 0);
                rawEndX = columnToX(localLineNumber, Constants.MAX_CHARS_PER_LINE);
            } else {
                localLineNumber = exception.getLineNumber();
                startX = columnToX(localLineNumber, exception.getStart());
                rawEndX = columnToX(localLineNumber, exception.getEnd());
            }
            final int startY = guiY + CODE_POS_Y + localLineNumber * getFontRenderer().lineHeight - 1;
            final int endX = Math.max(rawEndX, startX + getFontRenderer().width(" "));

            fill(matrixStack, startX - 1, startY + getFontRenderer().lineHeight - 1, endX, startY + getFontRenderer().lineHeight, Color.RED);

            // Draw selection position in text.
            drawTextCursor(matrixStack);

            // Part two of error handling, draw tooltip, *on top* of blinking cursor.
            if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= startY + getFontRenderer().lineHeight) {
                final List<Component> tooltip = new ArrayList<>();
                if (isErrorOnPreviousPage) {
                    tooltip.add(ERROR_ON_PREVIOUS_PAGE_TOOLTIP);
                } else if (isErrorOnNextPage) {
                    tooltip.add(ERROR_ON_NEXT_PAGE_TOOLTIP);
                }
                tooltip.add(exception.getDisplayMessage());
                renderComponentTooltip(matrixStack, tooltip, mouseX, mouseY);
            }
        } else {
            // Draw selection position in text.
            drawTextCursor(matrixStack);
        }
    }

    private void drawTextCursor(final PoseStack matrixStack) {
        if (System.currentTimeMillis() % 800 <= 400) {
            final int line = indexToLine(selectionEnd);
            final int column = indexToColumn(selectionEnd);
            final StringBuilder sb = lines.get(line);
            final int x = guiX + CODE_POS_X + getFontRenderer().width(sb.substring(0, column)) - 1;
            final int y = guiY + CODE_POS_Y + line * getFontRenderer().lineHeight - 1;
            fill(matrixStack, x + 1, y + 1, x + 2 + 1, y + getFontRenderer().lineHeight + 1, Color.GRAY);
            fill(matrixStack, x, y, x + 2, y + getFontRenderer().lineHeight, COLOR_CODE_SELECTED);
        }
    }

    // --------------------------------------------------------------------- //

    private enum PageChangeType {
        Previous,
        Next
    }

    private class ButtonChangePage extends Button {
        private static final int TEXTURE_X = 110;
        private static final int TEXTURE_Y = 231;
        private static final int BUTTON_WIDTH = 23;
        private static final int BUTTON_HEIGHT = 12;

        private final PageChangeType type;

        ButtonChangePage(final int x, final int y, final PageChangeType type, final OnPress action) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), action);
            this.type = type;
        }

        @Override
        public void renderButton(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, Textures.LOCATION_GUI_BOOK_CODE_BACKGROUND);
            final int offsetX = isHoveredOrFocused() ? BUTTON_WIDTH : 0;
            final int offsetY = type == PageChangeType.Previous ? BUTTON_HEIGHT : 0;
            blit(matrixStack, x, y, TEXTURE_X + offsetX, TEXTURE_Y + offsetY, BUTTON_WIDTH, BUTTON_HEIGHT);

            if (isHoveredOrFocused()) {
                renderToolTip(matrixStack, mouseX, mouseY);
            }
        }

        @Override
        public void renderToolTip(final PoseStack matrixStack, final int mouseX, final int mouseY) {
            final Component tooltip = type == PageChangeType.Previous
                ? PREVIOUS_PAGE_TOOLTIP
                : NEXT_PAGE_TOOLTIP;
            renderTooltip(matrixStack, tooltip, mouseX, mouseY);
        }
    }

    private class ButtonDeletePage extends Button {
        private static final int TEXTURE_X = 158;
        private static final int TEXTURE_Y = 231;
        private static final int BUTTON_WIDTH = 14;
        private static final int BUTTON_HEIGHT = 14;

        ButtonDeletePage(final int x, final int y, final OnPress action) {
            super(x, y, BUTTON_WIDTH, BUTTON_HEIGHT, Component.empty(), action);
        }

        @Override
        public void renderButton(final PoseStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, Textures.LOCATION_GUI_BOOK_CODE_BACKGROUND);
            final int offsetX = isHoveredOrFocused() ? BUTTON_WIDTH : 0;
            blit(matrixStack, x, y, TEXTURE_X + offsetX, TEXTURE_Y, BUTTON_WIDTH, BUTTON_HEIGHT);

            if (isHoveredOrFocused()) {
                renderToolTip(matrixStack, mouseX, mouseY);
            }
        }

        @Override
        public void renderToolTip(final PoseStack matrixStack, final int mouseX, final int mouseY) {
            renderTooltip(matrixStack, DELETE_PAGE_TOOLTIP, mouseX, mouseY);
        }
    }
}
