package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModuleRotatable;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.gui.GuiHandlerClient;
import li.cil.tis3d.client.gui.GuiModuleTerminal;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.client.renderer.font.FontRenderer;
import li.cil.tis3d.client.renderer.font.FontRendererNormal;
import li.cil.tis3d.common.TIS3D;
import li.cil.tis3d.util.OneEightCompat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.LinkedList;

public final class ModuleTerminal extends AbstractModuleRotatable {
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
    public static final String TAG_DISPLAY = "display";
    public static final String TAG_OUTPUT = "output";

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
    private static final Charset UTF_8 = Charset.forName("UTF-8");
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

    public ModuleTerminal(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final boolean isWriting = isWriting();
        for (final Port port : Port.VALUES) {
            stepInput(port);
            if (isWriting) {
                stepOutput(port);
            }
        }

        final World world = getCasing().getCasingWorld();
        if (sendBuffer != null && world.getTotalWorldTime() > lastSendTick) {
            getCasing().sendData(getFace(), sendBuffer);
            sendBuffer = null;
        }

        lastSendTick = world.getTotalWorldTime();
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
        if (getCasing().getCasingWorld().isRemote) {
            closeGui();
        }
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Pop the value (that was being written).
        output.setLength(output.length() - 1);

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();

        // If we're done, tell clients we can input again.
        if (!isWriting()) {
            sendInputEnabled(true);
        }
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }

        // Reasoning: don't remove module from casing while activating the
        // module while the casing is disabled. Could be frustrating.
        if (!getCasing().isEnabled()) {
            return true;
        }

        final World world = player.getEntityWorld();
        if (world.isRemote) {
            player.openGui(TIS3D.instance, GuiHandlerClient.GuiId.MODULE_TERMINAL.ordinal(), world, 0, 0, 0);
        }

        return true;
    }

    @Override
    public void onData(final ByteBuf data) {
        if (getCasing().getCasingWorld().isRemote) {
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

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled || !isVisible()) {
            return;
        }

        rotateForRendering();
        RenderUtil.ignoreLighting();
        GL11.glEnable(GL11.GL_BLEND);

        if (OneEightCompat.getDistanceSqToCenter(Minecraft.getMinecraft().thePlayer, getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ()) < 64) {
            // Player is close, render actual terminal text.
            renderText();
        } else {
            // Player too far away for details, draw static overlay.
            RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_TERMINAL_OVERLAY));
        }

        GL11.glDisable(GL11.GL_BLEND);
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final NBTTagList lines = nbt.getTagList(TAG_DISPLAY, Constants.NBT.TAG_STRING);
        display.clear();
        for (int tagIndex = 0; tagIndex < lines.tagCount(); tagIndex++) {
            display.add(new StringBuilder(lines.getStringTagAt(tagIndex)));
        }

        output.setLength(0);
        output.append(nbt.getString(TAG_OUTPUT));
        isInputEnabled = output.length() == 0;
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final NBTTagList lines = new NBTTagList();
        for (final StringBuilder line : display) {
            lines.appendTag(new NBTTagString(line.toString()));
        }
        nbt.setTag(TAG_DISPLAY, lines);

        nbt.setString(TAG_OUTPUT, output.toString());
    }

    // --------------------------------------------------------------------- //

    private void stepInput(final Port port) {
        // Continuously read from all ports.
        final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
        if (!receivingPipe.isReading()) {
            receivingPipe.beginRead();
        }
        if (receivingPipe.canTransfer()) {
            final char ch = toChar(receivingPipe.read());
            writeToDisplay(ch);
            sendDisplayToClient(ch);

            // Start reading again right away to read as fast as possible.
            receivingPipe.beginRead();
        }
    }

    private void stepOutput(final Port port) {
        final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
        if (!sendingPipe.isWriting()) {
            sendingPipe.beginWrite(toShort(peekChar()));
        }
    }

    // --------------------------------------------------------------------- //
    // Rendering

    @SideOnly(Side.CLIENT)
    private void renderText() {
        GL11.glTranslatef(2f / 16f, 2f / 16f, 0);
        GL11.glScalef(1 / 512f, 1 / 512f, 1);

        final FontRenderer fontRenderer = FontRendererNormal.INSTANCE;

        final int totalWidth = 12 * 32;
        final int textWidth = MAX_COLUMNS * fontRenderer.getCharWidth();
        final float offsetX = (totalWidth - textWidth) / 2f;
        GL11.glTranslatef(offsetX, 10f, 0);

        renderDisplay(fontRenderer);

        GL11.glTranslatef(0, (MAX_ROWS - display.size()) * fontRenderer.getCharHeight() + 4, 0);

        renderInput(fontRenderer, textWidth);
    }

    private void renderDisplay(final FontRenderer fontRenderer) {
        for (final StringBuilder line : display) {
            fontRenderer.drawString(line);
            GL11.glTranslatef(0, fontRenderer.getCharHeight(), 0);
        }
    }

    private void renderInput(final FontRenderer fontRenderer, final int textWidth) {
        if (!isInputEnabled) {
            GL11.glColor4f(0.5f, 0.5f, 0.5f, 1f);
        }

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);

        RenderUtil.drawUntexturedQuad(-4, 0, textWidth + 8, 24);
        GL11.glColor4f(0.1f, 0.1f, 0.1f, 1f);
        RenderUtil.drawUntexturedQuad(-2, 2, textWidth + 4, 20);
        if (!isInputEnabled) {
            GL11.glColor4f(0.5f, 0.5f, 0.5f, 1f);
        } else {
            GL11.glColor4f(1f, 1f, 1f, 1f);
        }

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glTranslatef(0, 4, 0);

        fontRenderer.drawString(input);

        if (isInputEnabled && input.length() < MAX_COLUMNS && System.currentTimeMillis() % 800 > 400) {
            GL11.glColor4f(1f, 1f, 1f, 1f);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            RenderUtil.drawUntexturedQuad(input.length() * fontRenderer.getCharWidth(), 0, fontRenderer.getCharWidth(), fontRenderer.getCharHeight());
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    @SideOnly(Side.CLIENT)
    private void closeGui() {
        final GuiScreen screen = Minecraft.getMinecraft().currentScreen;
        if (screen instanceof GuiModuleTerminal) {
            final GuiModuleTerminal gui = (GuiModuleTerminal) screen;
            if (gui.isFor(this)) {
                Minecraft.getMinecraft().displayGuiScreen(null);
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
        data.writeByte((byte) byteCount);
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
        byteBuffer.put((byte) value);
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
            return (short) (byteBuffer.get() & 0xFF);
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
        if (!world.isRemote) {
            world.playSoundEffect(getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ(), "note.pling", 0.3f, 0.2f);
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
            while (line.length() % TAB_WIDTH != 0 &&
                   line.length() < MAX_COLUMNS);
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
