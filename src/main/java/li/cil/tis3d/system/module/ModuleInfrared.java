package li.cil.tis3d.system.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.prefab.AbstractModule;
import li.cil.tis3d.client.TextureLoader;
import li.cil.tis3d.common.entity.EntityInfraredPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.opengl.GL11;

import java.util.Deque;
import java.util.LinkedList;

public final class ModuleInfrared extends AbstractModule implements InfraredReceiver {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final Deque<Integer> receiveQueue = new LinkedList<>();

    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * Maximum number of items stored in our receiver queue.
     * <p>
     * If the queue runs full, additionally received packets will be dropped.
     */
    public static final int MAX_QUEUE_LENGTH = 16;

    // NBT tag names.
    private static final String TAG_RECEIVE_QUEUE = "receiveQueue";

    /**
     * The last tick we sent a packet. Used to avoid emitting multiple packets
     * per tick when overclocked, because that could quickly spam a lot of
     * entities, which is... not a great idea.
     */
    private long lastStep = 0L;

    // --------------------------------------------------------------------- //

    public ModuleInfrared(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        stepOutput();
        stepInput();

        lastStep = getCasing().getCasingWorld().getTotalWorldTime();
    }

    @Override
    public void onDisabled() {
        receiveQueue.clear();

        getCasing().markDirty();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Pop the top value (the one that was being written).
        receiveQueue.removeFirst();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();

        // Start writing again right away to write as fast as possible.
        stepOutput();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        final TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(TextureLoader.LOCATION_MODULE_INFRARED_OVERLAY.toString());
        drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());

        GL11.glDisable(GL11.GL_BLEND);
        RenderHelper.enableStandardItemLighting();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        receiveQueue.clear();
        final int[] receiveQueueNbt = nbt.getIntArray(TAG_RECEIVE_QUEUE);
        for (final int value : receiveQueueNbt) {
            receiveQueue.addLast(value);
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final int[] receiveQueueArray = new int[receiveQueue.size()];
        int i = 0;
        for (final int value : receiveQueue) {
            receiveQueueArray[i++] = value;
        }
        final NBTTagIntArray receiveQueueNbt = new NBTTagIntArray(receiveQueueArray);
        nbt.setTag(TAG_RECEIVE_QUEUE, receiveQueueNbt);
    }

    // --------------------------------------------------------------------- //
    // InfraredReceiver

    @Override
    public void onInfraredPacket(final InfraredPacket packet, final MovingObjectPosition hit) {
        if (getCasing().getCasingWorld().isRemote) {
            return;
        }

        final int value = packet.getPacketValue();
        if (receiveQueue.size() < MAX_QUEUE_LENGTH) {
            receiveQueue.addLast(value);
        }
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the outputs of the module, pushing the oldest received value.
     */
    private void stepOutput() {
        // Don't try to write if the queue is empty.
        if (receiveQueue.isEmpty()) {
            return;
        }

        for (final Port port : Port.VALUES) {
            final Pipe sendingPipe = getCasing().getSendingPipe(getFace(), port);
            if (!sendingPipe.isWriting()) {
                sendingPipe.beginWrite(receiveQueue.peekFirst());
            }
        }
    }

    /**
     * Update the input of the module, pushing the current input to any pipe.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports, emit packet when receiving a value.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                // Don't actually read more values if we already sent a packet this tick.
                if (getCasing().getCasingWorld().getTotalWorldTime() > lastStep) {
                    emitInfraredPacket(receivingPipe.read());

                    // Start reading again right away to read as fast as possible.
                    receivingPipe.beginRead();
                }
            }
        }
    }

    /**
     * Fire a single infrared packet with the specified value.
     *
     * @param value the value to transmit.
     */
    private void emitInfraredPacket(final int value) {
        final EntityInfraredPacket entity = new EntityInfraredPacket(getCasing().getCasingWorld());
        entity.configure(getCasing().getPositionX(), getCasing().getPositionY(), getCasing().getPositionZ(), Face.toEnumFacing(getFace()), value);
        getCasing().getCasingWorld().spawnEntityInWorld(entity);
    }
}
