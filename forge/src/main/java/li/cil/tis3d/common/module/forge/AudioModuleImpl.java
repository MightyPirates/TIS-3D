package li.cil.tis3d.common.module.forge;

import li.cil.tis3d.common.module.AudioModule;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.NoteBlockEvent;

import javax.annotation.Nullable;

public final class AudioModuleImpl {
    @Nullable
    public static AudioModule.Note transformNote(final AudioModule module, final AudioModule.Note note) {
        final var level = module.getCasing().getCasingLevel();
        final var pos = module.getCasing().getPosition();
        final NoteBlockEvent.Play event = new NoteBlockEvent.Play(level, pos, level.getBlockState(pos), note.id(), note.instrument());
        if (MinecraftForge.EVENT_BUS.post(event)) {
            return null; // Cancelled.
        }

        return new AudioModule.Note(event.getVanillaNoteId(), event.getInstrument());
    }
}
