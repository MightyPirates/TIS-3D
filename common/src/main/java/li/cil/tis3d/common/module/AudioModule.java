package li.cil.tis3d.common.module;

import dev.architectury.injectables.annotations.ExpectPlatform;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.client.renderer.Textures;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import javax.annotation.Nullable;

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
        final Level level = getCasing().getCasingLevel();

        stepInput();

        lastStep = level.getGameTime();
    }

    @Environment(EnvType.CLIENT)
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
                final Level level = getCasing().getCasingLevel();
                if (level.getGameTime() > lastStep) {
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
        if (volume < 1) {
            return; // Skip mute sounds.
        }

        int instrumentId = value & 0x000F;
        if (instrumentId >= NoteBlockInstrument.values().length) instrumentId = 0;

        // Send event to check if the sound may be played / should be modulated.
        final var note = transformNote(this, new Note(noteId, NoteBlockInstrument.values()[instrumentId]));
        if (note != null) {
            // Offset to have the actual origin be in front of the module.
            final Direction facing = Face.toDirection(getFace());
            final BlockPos pos = getCasing().getPosition();
            final double x = pos.getX() + 0.5 + facing.getStepX() * 0.6;
            final double y = pos.getY() + 0.5 + facing.getStepY() * 0.6;
            final double z = pos.getZ() + 0.5 + facing.getStepZ() * 0.6;

            final float pitch = (float) Math.pow(2, (note.id() - 12) / 12.0);

            // Let there be sound!
            final Level level = getCasing().getCasingLevel();
            level.playSound(null, x, y, z, note.instrument().getSoundEvent().value(), SoundSource.BLOCKS, volume, pitch);
            if (level instanceof final ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.NOTE, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    public record Note(int id, NoteBlockInstrument instrument) { }

    @ExpectPlatform
    @Nullable
    private static Note transformNote(final AudioModule module, final Note note) {
        throw new AssertionError();
    }
}
