package li.cil.tis3d.common.module;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleWithRotation;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.gui.GuiHelper;
import li.cil.tis3d.client.gui.TerminalModuleGui;
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.client.render.font.AbstractFontRenderer;
import li.cil.tis3d.client.render.font.NormalFontRenderer;
import li.cil.tis3d.util.ColorUtils;
import li.cil.tis3d.util.NBTIds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;

public final class TerminalModule extends AbstractModuleWithRotation {
    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * Current displayed text, line by line.
     * <p>
     * As a linked list to allow fast removal from head an appending to tail
     * when scrolling (due to new line coming in and being at bottom of the
     * terminal screen).
     */
    private final LinkedList<StringBuilder> display = new LinkedList<>();

    /**
     * Current pending output, single line.
     * <p>
     * This is used to store pending output to be read from the terminal
     * module, in *reverse order* (so we can pop from the back).
     */
    private final StringBuilder output = new StringBuilder();

    /**
     * Current input, single line.
     * <p>
     * Only used on the client to store local user output.
     */
    private final StringBuilder input = new StringBuilder();

    // --------------------------------------------------------------------- //
    // Computed data

    // NBT tag names.
    private static final String TAG_DISPLAY = "display";
    private static final String TAG_OUTPUT = "output";

    // Data packet types.
    private static final byte DATA_TYPE_INPUT = 0;

    // Message types.
    private static final byte PACKET_INPUT = 0;
    private static final byte PACKET_DISPLAY = 1;
    private static final byte PACKET_CLEAR = 2;

    // Rendering/state constants.
    private static final int MAX_ROWS = 21;
    private static final int MAX_COLUMNS = 40;
    private static final int TAB_WIDTH = 2;

    // For string<->byte[] conversion when sending input to server.
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    // For short<->char conversion when reading/writing from/to ports.
    private static final Charset CP437 = Charset.forName("Cp437");

    // Reused buffers for converting between CP437 and chars.
    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1);
    private final CharBuffer charBuffer = CharBuffer.allocate(1);
    private final CharsetDecoder decoder = CP437.newDecoder();
    private final CharsetEncoder encoder = CP437.newEncoder();

    // Buffer for building packet to clients with newly printed characters.
    private ByteBuf sendBuffer;
    // Last time we sent the buffer to clients. Only try to send once per tick.
    private long lastSendTick = 0L;
    // Used on the client only, indicates whether input can currently be set.
    // This is false if the terminal is currently writing previous input to
    // adjacent modules. Only one command at a time can be processed.
    private boolean isInputEnabled;

    // --------------------------------------------------------------------- //

    public TerminalModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepOutput();
        stepInput();

        final World world = getCasing().getCasingWorld();
        if (sendBuffer != null && world.getTime() > lastSendTick) {
            getCasing().sendData(getFace(), sendBuffer);
            sendBuffer = null;
        }

        lastSendTick = world.getTime();
    }

    @Override
    public void onDisabled() {
        display.clear();
        output.setLength(0);

        final ByteBuf data = Unpooled.buffer();
        data.writeByte(PACKET_CLEAR);
        getCasing().sendData(getFace(), data);
    }

    @Override
    public void onDisposed() {
        if (getCasing().getCasingWorld().isClient) {
            //noinspection MethodCallSideOnly Guarded by isClient check.
            closeGui();
        }
    }

    @Override
    public void onBeforeWriteComplete(final Port port) {
        // Pop the value (that was being written).
        output.setLength(output.length() - 1);

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure all our writes are in sync.
        cancelWrite();

        // If we're done, tell clients we can input again.
        if (!isWriting()) {
            sendInputEnabled(true);
        } else {
            stepOutput();
        }
    }

    @Override
    public boolean onActivate(final PlayerEntity player, final Hand hand, final Vec3d hit) {
        if (player.isSneaking()) {
            return false;
        }

        // Reasoning: don't remove module from casing while activating the
        // module while the casing is disabled. Could be frustrating.
        if (!getCasing().isEnabled()) {
            return true;
        }

        final World world = player.getEntityWorld();
        if (world.isClient) {
            //noinspection MethodCallSideOnly Guarded by isClient check.
            GuiHelper.openTerminalGui(this);
        }

        return true;
    }

    @Override
    public void onData(final ByteBuf data) {
        if (getCasing().getCasingWorld().isClient) {
            // Server -> Client can be input state or output.
            switch (data.readByte()) {
                case PACKET_INPUT:
                    isInputEnabled = data.readBoolean();
                    if (!isInputEnabled) {
                        input.setLength(0);
                    }
                    break;
                case PACKET_DISPLAY:
                    while (data.isReadable()) {
                        writeToDisplay(data.readChar());
                    }
                    break;
                case PACKET_CLEAR:
                    display.clear();
                    output.setLength(0);
                    input.setLength(0);
                    isInputEnabled = true;
                    break;

            }
        } else if (!isWriting()) {
            // Client -> Server is only input.
            beginWriting(readString(data));
            sendInputEnabled(false);
        }
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                       final MatrixStack matrices, final VertexConsumerProvider vcp,
                       final int light, final int overlay) {
        if (!getCasing().isEnabled() || !isVisible()) {
            return;
        }

        matrices.push();
        rotateForRendering(matrices);

        if (rendererDispatcher.camera.getBlockPos().getSquaredDistance(getCasing().getPosition()) < 64) {
            // Player is close, render actual terminal text.
            renderText(matrices, vcp, RenderUtil.maxLight, overlay);
        } else {
            // Player too far away for details, draw static overlay.
            final Sprite baseSprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_TERMINAL);
            final VertexConsumer vc = vcp.getBuffer(RenderLayer.getCutoutMipped());
            RenderUtil.drawQuad(baseSprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
        }

        matrices.pop();
    }

    @Override
    public void readFromNBT(final NbtCompound nbt) {
        super.readFromNBT(nbt);

        final NbtList lines = nbt.getList(TAG_DISPLAY, NBTIds.TAG_STRING);
        display.clear();
        for (int tagIndex = 0; tagIndex < lines.size(); tagIndex++) {
            display.add(new StringBuilder(lines.getString(tagIndex)));
        }

        output.setLength(0);
        output.append(nbt.getString(TAG_OUTPUT));
        isInputEnabled = output.length() == 0;
    }

    @Override
    public void writeToNBT(final NbtCompound nbt) {
        super.writeToNBT(nbt);

        final NbtList lines = new NbtList();
        for (final StringBuilder line : display) {
            lines.add(NbtString.of(line.toString()));
        }
        nbt.put(TAG_DISPLAY, lines);

        nbt.putString(TAG_OUTPUT, output.toString());
    }

    // --------------------------------------------------------------------- //

    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                final char ch = toChar(receivingPipe.read());
                writeToDisplay(ch);
                sendDisplayToClient(ch);
            }
        }
    }

    private void stepOutput() {
        if (isWriting()) {
            for (final Port port : Port.VALUES) {
                final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
                if (!sendingPipe.isWriting()) {
                    sendingPipe.beginWrite(toShort(peekChar()));
                }
            }
        }
    }

    // --------------------------------------------------------------------- //
    // Rendering

    @Environment(EnvType.CLIENT)
    private void renderText(final MatrixStack matrices, final VertexConsumerProvider vcp,
                            final int light, final int overlay) {
        matrices.translate(2f / 16f, 2f / 16f, 0);
        matrices.scale(1 / 512f, 1 / 512f, 1);

        final AbstractFontRenderer fontRenderer = (AbstractFontRenderer)NormalFontRenderer.INSTANCE;
        // The order in which these VCs are acquired matters (only the last one is "current")
        // thanks Majong :^)
        final VertexConsumer vcFont = fontRenderer.chooseVertexConsumer(vcp);

        final int charWidth = fontRenderer.getCharWidth();
        final int charHeight = fontRenderer.getCharHeight();
        final int totalWidth = 12 * 32;
        final int textWidth = MAX_COLUMNS * charWidth;
        final float offsetX = (totalWidth - textWidth) / 2f;

        matrices.translate(offsetX, 10f, 0);
        renderDisplay(matrices, vcFont, light, overlay, fontRenderer);

        matrices.translate(0, (MAX_ROWS - display.size()) * charHeight + 4, 0);
        renderInput(matrices, vcFont, vcp, light, overlay, fontRenderer, textWidth);
    }

    @Environment(EnvType.CLIENT)
    private void renderDisplay(final MatrixStack matrices, final VertexConsumer vcFont,
                               final int light, final int overlay,
                               final AbstractFontRenderer fontRenderer) {
        for (final StringBuilder line : display) {
            fontRenderer.drawString(matrices.peek(), vcFont, light, overlay, line);
            matrices.translate(0, fontRenderer.getCharHeight(), 0);
        }
    }

    @Environment(EnvType.CLIENT)
    private void renderInput(final MatrixStack matrices, final VertexConsumer vcFont,
                             final VertexConsumerProvider vcp,
                             final int light, final int overlay,
                             final AbstractFontRenderer fontRenderer, final int textWidth) {
        int color = ColorUtils.WHITE;

        matrices.translate(0, 4, 0);

        // Draw input buffer
        fontRenderer.drawString(matrices.peek(), vcFont, light, overlay, input);

        final VertexConsumer vcColor = vcp.getBuffer(RenderLayer.getSolid());

        if (isInputEnabled && input.length() < MAX_COLUMNS && System.currentTimeMillis() % 800 > 400) {
            final int charWidth = fontRenderer.getCharWidth();
            final int charHeight = fontRenderer.getCharHeight();
            // Draw blinking cursor
            RenderUtil.drawColorQuad(matrices.peek(), vcColor,
                                     input.length() * charWidth, 0, charWidth, charHeight,
                                     color, light, overlay);
        }

        if (!isInputEnabled) {
            color = 0xFF808080;
        }

        // Draw input outline
        RenderUtil.drawColorQuad(matrices.peek(), vcColor,
                                 -4, -4, textWidth + 8, 2,
                                 color, light, overlay);
        RenderUtil.drawColorQuad(matrices.peek(), vcColor,
                                 -4, 18, textWidth + 8, 2,
                                 color, light, overlay);
        RenderUtil.drawColorQuad(matrices.peek(), vcColor,
                                 -4, -4, 2, 24,
                                 color, light, overlay);
        RenderUtil.drawColorQuad(matrices.peek(), vcColor,
                                 textWidth + 2, -4, 2, 24,
                                 color, light, overlay);

        // Draw input box background
        final int grey = 0xFF1A1A1A;
        matrices.translate(0, 0, 0.005f / 2);
        RenderUtil.drawColorQuad(matrices.peek(), vcColor,
                                 -4, -4, textWidth + 8, 24,
                                 grey, light, overlay);
    }

    @Environment(EnvType.CLIENT)
    private void closeGui() {
        final MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) {
            return;
        }

        final Screen screen = mc.currentScreen;
        if (screen instanceof TerminalModuleGui) {
            final TerminalModuleGui gui = (TerminalModuleGui)screen;
            if (gui.isFor(this)) {
                mc.openScreen(null);
            }
        }
    }

    // --------------------------------------------------------------------- //
    // Networking

    private void sendInputEnabled(final boolean value) {
        final ByteBuf response = Unpooled.buffer();
        response.writeByte(PACKET_INPUT);
        response.writeBoolean(value);
        getCasing().sendData(getFace(), response, DATA_TYPE_INPUT);
    }

    private void sendDisplayToClient(final char ch) {
        if (sendBuffer == null) {
            sendBuffer = Unpooled.buffer();
            sendBuffer.writeByte(PACKET_DISPLAY);
        }
        sendBuffer.writeChar(ch);
    }

    private void sendInputToServer() {
        final ByteBuf data = Unpooled.buffer();
        writeString(data, input.toString());
        getCasing().sendData(getFace(), data, DATA_TYPE_INPUT);
    }

    private static void writeString(final ByteBuf data, final String value) {
        final byte[] bytes = value.getBytes(UTF_8);
        final int byteCount = Math.min(0xFF, bytes.length);
        data.writeByte((byte)byteCount);
        data.writeBytes(bytes, 0, byteCount);
    }

    private static String readString(final ByteBuf data) {
        final int byteCount = data.readByte() & 0xFF;
        final byte[] bytes = new byte[byteCount];
        data.readBytes(bytes);
        return new String(bytes, UTF_8);
    }

    // --------------------------------------------------------------------- //
    // Input processing

    private char toChar(final short value) {
        byteBuffer.clear();
        charBuffer.clear();
        decoder.reset();
        byteBuffer.put((byte)value);
        byteBuffer.rewind();
        decoder.decode(byteBuffer, charBuffer, true);
        charBuffer.rewind();
        if (charBuffer.hasRemaining()) {
            return charBuffer.get();
        } else {
            return '\0';
        }
    }

    private short toShort(final char ch) {
        byteBuffer.clear();
        charBuffer.clear();
        encoder.reset();
        charBuffer.put(ch);
        charBuffer.rewind();
        encoder.encode(charBuffer, byteBuffer, true);
        byteBuffer.rewind();
        if (byteBuffer.hasRemaining()) {
            return (short)(byteBuffer.get() & 0xFF);
        } else {
            return 0;
        }
    }

    private void writeToDisplay(final char ch) {
        if (display.isEmpty()) {
            display.add(new StringBuilder());
        }

        if (ch == 0x07 /* '\a' */) {
            bell();
        } else if (ch == '\b') {
            backspace(display.getLast());
        } else if (ch == '\t') {
            wrapIfNecessary();
            tab(display.getLast());
        } else if (ch == '\n' || ch == '\r') {
            newLine();
        } else {
            wrapIfNecessary();
            character(display.getLast(), ch);
        }
    }

    // Called from the invisible GUI used to capture output.
    public void writeToInput(final char ch) {
        if (ch == '\b') {
            backspace(input);
        } else if (ch == '\t') {
            tab(input);
        } else if (ch == '\n' || ch == '\r') {
            sendInputToServer();
        } else if (toShort(ch) != 0) {
            character(input, toChar(toShort(ch)));
        }
    }

    private void bell() {
        final World world = getCasing().getCasingWorld();
        if (!world.isClient) {
            world.playSound(null, getCasing().getPosition(), SoundEvents.BLOCK_NOTE_BLOCK_HARP, SoundCategory.BLOCKS, 0.3f, 2f);
        }
    }

    private static void backspace(final StringBuilder line) {
        if (line.length() > 0) {
            line.setLength(line.length() - 1);
        }
    }

    private static void tab(final StringBuilder line) {
        if (line.length() < MAX_COLUMNS) {
            do {
                line.append(' ');
            }
            while (line.length() % TAB_WIDTH != 0 && line.length() < MAX_COLUMNS);
        }
    }

    private static void character(final StringBuilder line, final char ch) {
        if (line.length() < MAX_COLUMNS) {
            line.append(ch);
        }
    }

    private void wrapIfNecessary() {
        if (display.getLast().length() >= MAX_COLUMNS) {
            newLine();
        }
    }

    private void newLine() {
        StringBuilder line = null;
        while (display.size() >= MAX_ROWS) {
            line = display.removeFirst();
        }
        if (line == null) {
            line = new StringBuilder();
        } else {
            line.setLength(0);
        }
        display.addLast(line);
    }

    // --------------------------------------------------------------------- //
    // Utilities for pumping output string to output pipes

    private boolean isWriting() {
        return output.length() > 0;
    }

    private void beginWriting(final String value) {
        stopWriting();
        output.append(value);
        output.append('\0'); // Always terminate strings with a zero.
        output.reverse();
    }

    private char peekChar() {
        return output.charAt(output.length() - 1);
    }

    private void stopWriting() {
        output.setLength(0);
    }
}
