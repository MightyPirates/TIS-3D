/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.common.module.neoforge;

import li.cil.tis3d.common.module.AudioModule;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

import javax.annotation.Nullable;

public final class AudioModuleImpl {
    @Nullable
    public static AudioModule.Note transformNote(final AudioModule module, final AudioModule.Note note) {
        final var level = module.getCasing().getCasingLevel();
        final var pos = module.getCasing().getPosition();
        final NoteBlockEvent.Play event = new NoteBlockEvent.Play(level, pos, level.getBlockState(pos), note.id(), note.instrument());
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return null; // Cancelled.
        }

        return new AudioModule.Note(event.getVanillaNoteId(), event.getInstrument());
    }
}
