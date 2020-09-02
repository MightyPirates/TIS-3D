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
import li.cil.tis3d.client.init.Textures;
import li.cil.tis3d.common.LocalAPI;
import li.cil.tis3d.common.Settings;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Deque;
import java.util.LinkedList;

public final class InfraredModule extends AbstractModule implements InfraredReceiver {
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

    public InfraredModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final World world = getCasing().getCasingWorld();

        stepOutput();
        stepInput();

        lastStep = world.getTime();
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

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                       final MatrixStack matrices, final VertexConsumerProvider vcp,
                       final int light, final int overlay) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final Sprite sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_INFRARED);
        final VertexConsumer vc = vcp.getBuffer(RenderLayer.getCutoutMipped());

        RenderUtil.drawQuad(sprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
    }

    @Override
    public void readFromNBT(final CompoundTag nbt) {
        super.readFromNBT(nbt);

        receiveQueue.clear();
        final int[] receiveQueueNbt = nbt.getIntArray(TAG_RECEIVE_QUEUE);
        for (final int value : receiveQueueNbt) {
            receiveQueue.addLast((short)value);
        }
    }

    @Override
    public void writeToNBT(final CompoundTag nbt) {
        super.writeToNBT(nbt);

        final int[] receiveQueueArray = new int[receiveQueue.size()];
        int i = 0;
        for (final int value : receiveQueue) {
            receiveQueueArray[i++] = value;
        }
        final IntArrayTag receiveQueueNbt = new IntArrayTag(receiveQueueArray);
        nbt.put(TAG_RECEIVE_QUEUE, receiveQueueNbt);
    }

    // --------------------------------------------------------------------- //
    // InfraredReceiver

    @Override
    public void onInfraredPacket(final InfraredPacket packet, final HitResult hit) {
        if (!getCasing().isEnabled()) {
            return;
        }

        final World world = getCasing().getCasingWorld();
        if (world.isClient) {
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
                //noinspection ConstantConditions We're never pushing null values.
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
                if (world.getTime() > lastStep) {
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
        final Direction facing = Face.toDirection(getFace());
        final BlockPos blockPos = getCasing().getPosition().offset(facing);

        final World world = getCasing().getCasingWorld();
        final Vec3d position = new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
        final Vec3d direction = new Vec3d(facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ());

        LocalAPI.common.infraredAPI.sendPacket(world, position, direction, value);
    }
}
