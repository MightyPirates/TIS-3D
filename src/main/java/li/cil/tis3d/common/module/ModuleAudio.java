package li.cil.tis3d.common.module;

import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The audio module, emitting sounds like none other.
 */
public final class ModuleAudio extends AbstractModule {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * Resolve instrument ID to sound event used for instrument.
     */
    private static final SoundEvent[] INSTRUMENTS = new SoundEvent[]{SoundEvents.BLOCK_NOTE_HARP, SoundEvents.BLOCK_NOTE_BASEDRUM, SoundEvents.BLOCK_NOTE_SNARE, SoundEvents.BLOCK_NOTE_HAT, SoundEvents.BLOCK_NOTE_BASS, SoundEvents.BLOCK_NOTE_FLUTE, SoundEvents.BLOCK_NOTE_BELL, SoundEvents.BLOCK_NOTE_GUITAR, SoundEvents.BLOCK_NOTE_CHIME, SoundEvents.BLOCK_NOTE_XYLOPHONE};

    static {
        assert INSTRUMENTS.length == NoteBlockEvent.Instrument.values().length;
    }

    /**
     * The last tick we made a sound. Used to avoid emitting multiple sounds
     * per tick when overclocked, because that could quickly spam a lot of
     * packets, and sound horrible, too.
     */
    private long lastStep = 0L;

    // --------------------------------------------------------------------- //

    public ModuleAudio(final Casing casing, final Face face) {
        super(casing, face);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final World world = getCasing().getCasingWorld();

        stepInput();

        lastStep = world.getTotalWorldTime();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final boolean enabled, final float partialTicks) {
        if (!enabled) {
            return;
        }

        GlStateManager.enableBlend();

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_AUDIO_OVERLAY));

        GlStateManager.disableBlend();
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
                if (world.getTotalWorldTime() > lastStep) {
                    playNote(receivingPipe.read());

                    // Start reading again right away to read as fast as possible.
                    receivingPipe.beginRead();
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
        final int instrumentId = value & 0x000F;

        // Skip mute sounds.
        if (volume < 1) {
            return;
        }

        // Skip invalid instruments
        if (instrumentId >= INSTRUMENTS.length) {
            return;
        }

        // Send event to check if the sound may be played / should be modulated.
        final World world = getCasing().getCasingWorld();
        final BlockPos pos = getCasing().getPosition();
        final NoteBlockEvent.Play event = new NoteBlockEvent.Play(world, pos, world.getBlockState(pos), noteId, instrumentId);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            // Not cancelled, get pitch, sound effect name.
            final int note = event.getVanillaNoteId();
            final float pitch = (float) Math.pow(2, (note - 12) / 12.0);
            final SoundEvent sound = INSTRUMENTS[event.getInstrument().ordinal()];

            // Offset to have the actual origin be in front of the module.
            final EnumFacing facing = Face.toEnumFacing(getFace());
            final double x = pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.6;
            final double y = pos.getY() + 0.5 + facing.getFrontOffsetY() * 0.6;
            final double z = pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.6;

            // Let there be sound!
            world.playSound(null, x, y, z, sound, SoundCategory.BLOCKS, volume, pitch);
            final MessageParticleEffect message = new MessageParticleEffect(world, EnumParticleTypes.NOTE, x, y, z);
            final NetworkRegistry.TargetPoint target = Network.getTargetPoint(world, x, y, z, Network.RANGE_LOW);
            Network.INSTANCE.getWrapper().sendToAllAround(message, target);
        }
    }
}
