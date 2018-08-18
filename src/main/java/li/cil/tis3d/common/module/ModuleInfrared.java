package li.cil.tis3d.common.module;

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
import li.cil.tis3d.common.capabilities.CapabilityInfraredReceiver;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Deque;
import java.util.LinkedList;

public final class ModuleInfrared extends AbstractModule implements ICapabilityProvider, InfraredReceiver {
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
    public void onBeforeWriteComplete(final Port port) {
        // Pop the top value (the one that was being written).
        receiveQueue.removeFirst();

        // If one completes, cancel all other writes to ensure a value is only
        // written once.
        cancelWrite();
    }

    @Override
    public void onWriteComplete(final Port port) {
        // Re-cancel in case step() was called after onBeforeWriteComplete() to
        // ensure all our writes are in sync.
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

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_OVERLAY_MODULE_INFRARED));
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
    // ICapabilityProvider

    @Override
    public boolean hasCapability(@Nonnull final Capability<?> capability, @Nullable final EnumFacing facing) {
        return capability == CapabilityInfraredReceiver.INFRARED_RECEIVER_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getCapability(@Nonnull final Capability<T> capability, @Nullable final EnumFacing facing) {
        if (capability == CapabilityInfraredReceiver.INFRARED_RECEIVER_CAPABILITY) {
            return (T) this;
        }

        return null;
    }

    // --------------------------------------------------------------------- //
    // InfraredReceiver

    @Override
    public void onInfraredPacket(final InfraredPacket packet, final RayTraceResult hit) {
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
        final BlockPos blockPos = getCasing().getPosition().offset(facing);

        final World world = getCasing().getCasingWorld();
        final Vec3d position = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        final Vec3d direction = new Vec3d(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());

        InfraredAPI.sendPacket(world, position, direction, value);
    }
}
