package li.cil.tis3d.data;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.tags.BlockTags;
import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.ItemTagsProvider;
import net.minecraft.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

import static li.cil.tis3d.common.item.Items.*;
import static li.cil.tis3d.common.tags.ItemTags.*;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(final DataGenerator generator, final BlockTagsProvider blockTagProvider, final ExistingFileHelper existingFileHelper) {
        super(generator, blockTagProvider, API.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerTags() {
        copy(BlockTags.COMPUTERS, COMPUTERS);

        getOrCreateBuilder(MODULES).add(
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

        getOrCreateBuilder(BOOKS).add(
            BOOK_CODE.get(),
            BOOK_MANUAL.get(),
            Items.BOOK,
            Items.ENCHANTED_BOOK,
            Items.WRITABLE_BOOK,
            Items.WRITTEN_BOOK
        );

        getOrCreateBuilder(KEYS).add(
            KEY.get(),
            KEY_CREATIVE.get()
        );
    }
}
