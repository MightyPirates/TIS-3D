package li.cil.tis3d.common.module;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.InfraredAPI;
import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.Settings;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.Deque;
import java.util.LinkedList;

public final class ModuleInfrared extends AbstractModule implements InfraredReceiver {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final Deque<Short> receiveQueue = new LinkedList<>();

    // --------------------------------------------------------------------- //
    // Computed data

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
        final World world = getCasing().getCasingWorld();

        stepOutput();
        stepInput();

        lastStep = world.getTotalWorldTime();
    }

    @Override
    public void onDisabled() {
        receiveQueue.clear();
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

        RenderUtil.ignoreLighting();

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_INFRARED_OVERLAY));
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        receiveQueue.clear();
        final int[] receiveQueueNbt = nbt.getIntArray(TAG_RECEIVE_QUEUE);
        for (final int value : receiveQueueNbt) {
            receiveQueue.addLast((short) value);
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
        final World world = getCasing().getCasingWorld();
        if (world.isRemote) {
            return;
        }

        final short value = packet.getPacketValue();
        if (receiveQueue.size() < Settings.maxInfraredQueueLength) {
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
                final World world = getCasing().getCasingWorld();
                if (world.getTotalWorldTime() > lastStep) {
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
    private void emitInfraredPacket(final short value) {
        final EnumFacing facing = Face.toEnumFacing(getFace());
        final int positionX = getCasing().getPositionX() + facing.getFrontOffsetX();
        final int positionY = getCasing().getPositionY() + facing.getFrontOffsetY();
        final int positionZ = getCasing().getPositionZ() + facing.getFrontOffsetZ();

        final World world = getCasing().getCasingWorld();
        final Vec3 position = Vec3.createVectorHelper(positionX + 0.5, positionY + 0.5, positionZ + 0.5);
        final Vec3 direction = Vec3.createVectorHelper(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());

        InfraredAPI.sendPacket(world, position, direction, value);
    }
}
