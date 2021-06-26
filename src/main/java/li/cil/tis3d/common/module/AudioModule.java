package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.NoteBlockInstrument;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.NoteBlockEvent;

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
        final World world = getCasing().getCasingLevel();

        stepInput();

        lastStep = world.getGameTime();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void render(final RenderContext context) {
        if (!getCasing().isEnabled()) {
            return;
        }

        context.drawAtlasQuadLit(Textures.LOCATION_OVERLAY_MODULE_AUDIO);
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
                final World world = getCasing().getCasingLevel();
                if (world.getGameTime() > lastStep) {
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
        if (instrumentId >= NoteBlockInstrument.values().length) instrumentId = 0;

        // Skip mute sounds.
        if (volume < 1) {
            return;
        }

        // Send event to check if the sound may be played / should be modulated.
        final World world = getCasing().getCasingLevel();
        final BlockPos pos = getCasing().getPosition();
        NoteBlockInstrument instrument = NoteBlockInstrument.values()[instrumentId];
        final NoteBlockEvent.Play event = new NoteBlockEvent.Play(world, pos, world.getBlockState(pos), noteId, instrument);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            // Not cancelled, get pitch, sound effect name.
            final int note = event.getVanillaNoteId();
            final float pitch = (float) Math.pow(2, (note - 12) / 12.0);
            instrument = event.getInstrument();

            // Offset to have the actual origin be in front of the module.
            final Direction facing = Face.toDirection(getFace());
            final double x = pos.getX() + 0.5 + facing.getStepX() * 0.6;
            final double y = pos.getY() + 0.5 + facing.getStepY() * 0.6;
            final double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.6;

            // Let there be sound!
            world.playSound(null, x, y, z, instrument.getSoundEvent(), SoundCategory.BLOCKS, volume, pitch);
            if (world instanceof ServerWorld) {
                final ServerWorld serverWorld = (ServerWorld) world;
                serverWorld.sendParticles(ParticleTypes.NOTE, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
