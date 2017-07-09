package li.cil.tis3d.common.module;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import li.cil.tis3d.api.machine.Casing;
import li.cil.tis3d.api.machine.Face;
import li.cil.tis3d.api.machine.Pipe;
import li.cil.tis3d.api.machine.Port;
import li.cil.tis3d.api.prefab.module.AbstractModule;
import li.cil.tis3d.api.util.RenderUtil;
import li.cil.tis3d.client.renderer.TextureLoader;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.NoteBlockEvent;
import org.lwjgl.opengl.GL11;

/**
 * The audio module, emitting sounds like none other.
 */
public final class ModuleAudio extends AbstractModule {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * Resolve instrument ID to name of sound used for instrument.
     */
    private static final String[] INSTRUMENT_SOUND_NAMES = new String[]{"note.harp", "note.bd", "note.snare", "note.hat", "note.bassattack"};

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

        GL11.glEnable(GL11.GL_BLEND);

        RenderUtil.drawQuad(RenderUtil.getSprite(TextureLoader.LOCATION_MODULE_AUDIO_OVERLAY));

        GL11.glDisable(GL11.GL_BLEND);
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

        // Send event to check if the sound may be played / should be modulated.
        final World world = getCasing().getCasingWorld();
        final int posX = getCasing().getPositionX();
        final int posY = getCasing().getPositionY();
        final int posZ = getCasing().getPositionZ();
        final NoteBlockEvent.Play event = new NoteBlockEvent.Play(world, posX, posY, posZ, world.getBlockMetadata(posX, posY, posZ), noteId, instrumentId);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            // Not cancelled, get pitch, sound effect name.
            final int note = event.getVanillaNoteId();
            final float pitch = (float) Math.pow(2, (note - 12) / 12.0);
            final String sound = INSTRUMENT_SOUND_NAMES[event.instrument.ordinal()];

            // Offset to have the actual origin be in front of the module.
            final EnumFacing facing = Face.toEnumFacing(getFace());
            final double x = posX + 0.5 + facing.getFrontOffsetX() * 0.6;
            final double y = posY + 0.5 + facing.getFrontOffsetY() * 0.6;
            final double z = posZ + 0.5 + facing.getFrontOffsetZ() * 0.6;

            // Let there be sound!
            world.playSoundEffect(x, y, z, sound, volume, pitch);
            final MessageParticleEffect message = new MessageParticleEffect(world, "note", x, y, z);
            final NetworkRegistry.TargetPoint target = Network.getTargetPoint(world, x, y, z, Network.RANGE_LOW);
            Network.INSTANCE.getWrapper().sendToAllAround(message, target);
        }
    }
}
