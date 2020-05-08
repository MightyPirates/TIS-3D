package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.init.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.enums.Instrument;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * The audio module, emitting sounds like none other.
 */
public final class AudioModule extends AbstractModule {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * The last tick we made a sound. Used to avoid emitting multiple sounds
     * per tick when overclocked, because that could quickly spam a lot of
     * packets, and sound horrible, too.
     */
    private long lastStep = 0L;

    // --------------------------------------------------------------------- //

    public AudioModule(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final World world = getCasing().getCasingWorld();

        stepInput();

        lastStep = world.getTime();
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void render(final BlockEntityRenderDispatcher rendererDispatcher, final float partialTicks,
                       final MatrixStack matrices, final VertexConsumerProvider vcp,
                       final int light, final int overlay) {
        if (!getCasing().isEnabled()) {
            return;
        }

        // XXX Somemthing isn't quite right yet, the sprite seems bigger than in 1.14
        // and is almost spilling out of the module quad bounds
        final Sprite sprite = RenderUtil.getSprite(Textures.LOCATION_OVERLAY_MODULE_AUDIO);
        final VertexConsumer vc = vcp.getBuffer(RenderLayer.getTranslucentNoCrumbling());

        RenderUtil.drawQuad(sprite, matrices.peek(), vc, RenderUtil.maxLight, overlay);
    }

    // --------------------------------------------------------------------- //

    /**
     * Update the input of the module, reading the type of note to play.
     */
    private void stepInput() {
        for (final Port port : Port.VALUES) {
            // Continuously read from all ports, play sound when receiving a value.
            final Pipe receivingPipe = getCasing().getReceivingPipe(getFace(), port);
            if (!receivingPipe.isReading()) {
                receivingPipe.beginRead();
            }
            if (receivingPipe.canTransfer()) {
                // Don't actually read more values if we already sent a packet this tick.
                final World world = getCasing().getCasingWorld();
                if (world.getTime() > lastStep) {
                    playNote(receivingPipe.read());
                }
            }
        }
    }

    /**
     * Decode the specified value into instrument, note and volume and play it.
     *
     * @param value the value defining the sound to play.
     */
    private void playNote(final int value) {
        final int noteId = (value & 0xFF00) >>> 8;
        final int volume = Math.min(4, (value & 0x00F0) >>> 4);
        int instrumentId = value & 0x000F;
        if (instrumentId >= Instrument.values().length) instrumentId = 0;

        // Skip mute sounds.
        if (volume < 1) {
            return;
        }

        // Get pitch, sound effect name.
        final float pitch = (float)Math.pow(2, (noteId - 12) / 12.0);
        final Instrument instrument = Instrument.values()[instrumentId];

        // Offset to have the actual origin be in front of the module.
        final Direction facing = Face.toDirection(getFace());
        final BlockPos pos = getCasing().getPosition();
        final double x = pos.getX() + 0.5 + facing.getOffsetX() * 0.6;
        final double y = pos.getY() + 0.5 + facing.getOffsetY() * 0.6;
        final double z = pos.getZ() + 0.5 + facing.getOffsetZ() * 0.6;

        // Let there be sound!
        final World world = getCasing().getCasingWorld();
        world.playSound(null, x, y, z, instrument.getSound(), SoundCategory.BLOCKS, volume, pitch);
        ((ServerWorld)world).spawnParticles(ParticleTypes.NOTE, x, y, z, 1, 0, 0, 0, 0);
    }
}
