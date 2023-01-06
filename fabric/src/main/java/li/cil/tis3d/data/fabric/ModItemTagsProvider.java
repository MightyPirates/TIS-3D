package li.cil.tis3d.data.fabric;

import li.cil.tis3d.common.tags.BlockTags;
import li.cil.tis3d.common.tags.ItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.world.item.Items;

import static li.cil.tis3d.common.item.Items.*;
import static li.cil.tis3d.common.tags.ItemTags.*;

public class ModItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagsProvider(final FabricDataGenerator generator, final BlockTagProvider blockTagProvider) {
        super(generator, blockTagProvider);
    }

    @Override
    protected void generateTags() {
        copy(BlockTags.COMPUTERS, ItemTags.COMPUTERS);

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

        tag(CommonItemTags.CHESTS).add(Items.CHEST);
        tag(CommonItemTags.DIAMOND_GEMS).add(Items.DIAMOND);
        tag(CommonItemTags.EMERALDS).add(Items.EMERALD);
        tag(CommonItemTags.ENDER_PEARLS).add(Items.ENDER_PEARL);
        tag(CommonItemTags.GLASS_PANES).add(Items.GLASS_PANE);
        tag(CommonItemTags.GOLD_INGOTS).add(Items.GOLD_INGOT);
        tag(CommonItemTags.GOLD_NUGGETS).add(Items.GOLD_NUGGET);
        tag(CommonItemTags.IRON_BLOCKS).add(Items.IRON_BLOCK);
        tag(CommonItemTags.IRON_INGOTS).add(Items.IRON_INGOT);
        tag(CommonItemTags.LAPIS_LAZULIS).add(Items.LAPIS_LAZULI);
        tag(CommonItemTags.QUARTZ_GEMS).add(Items.QUARTZ);
        tag(CommonItemTags.REDSTONE_DUSTS).add(Items.REDSTONE);
        tag(CommonItemTags.SAND).add(Items.SAND);
    }
}
