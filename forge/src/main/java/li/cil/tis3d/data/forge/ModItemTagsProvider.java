package li.cil.tis3d.data.forge;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.tags.BlockTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static li.cil.tis3d.common.item.Items.*;
import static li.cil.tis3d.common.tags.ItemTags.*;

public final class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider, final BlockTagsProvider blockTagsProvider, final ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTagsProvider.contentsGetter(), API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        copy(BlockTags.COMPUTERS, COMPUTERS);

        tag(MODULES).add(
            AUDIO_MODULE.get(),
            DISPLAY_MODULE.get(),
            EXECUTION_MODULE.get(),
            FACADE_MODULE.get(),
            INFRARED_MODULE.get(),
            KEYPAD_MODULE.get(),
            QUEUE_MODULE.get(),
            RANDOM_MODULE.get(),
            RANDOM_ACCESS_MEMORY_MODULE.get(),
            READ_ONLY_MEMORY_MODULE.get(),
            REDSTONE_MODULE.get(),
            SEQUENCER_MODULE.get(),
            SERIAL_PORT_MODULE.get(),
            STACK_MODULE.get(),
            TERMINAL_MODULE.get(),
            TIMER_MODULE.get()
        );

        tag(BOOKS).add(
            BOOK_CODE.get(),
            BOOK_MANUAL.get(),
            Items.BOOK,
            Items.ENCHANTED_BOOK,
            Items.WRITABLE_BOOK,
            Items.WRITTEN_BOOK
        );

        tag(KEYS).add(
            KEY.get(),
            KEY_CREATIVE.get()
        );
    }
}
