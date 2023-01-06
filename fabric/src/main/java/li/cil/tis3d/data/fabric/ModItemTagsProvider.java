package li.cil.tis3d.data.fabric;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

import static li.cil.tis3d.common.item.Items.*;
import static li.cil.tis3d.common.tags.ItemTags.*;

public class ModItemTagsProvider extends FabricTagProvider.ItemTagProvider {
    public ModItemTagsProvider(final FabricDataOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void addTags(final HolderLookup.Provider provider) {
        tag(COMPUTERS).add(
            key(CASING.get()),
            key(CONTROLLER.get())
        );

        tag(MODULES).add(
            key(AUDIO_MODULE.get()),
            key(DISPLAY_MODULE.get()),
            key(EXECUTION_MODULE.get()),
            key(FACADE_MODULE.get()),
            key(INFRARED_MODULE.get()),
            key(KEYPAD_MODULE.get()),
            key(QUEUE_MODULE.get()),
            key(RANDOM_MODULE.get()),
            key(RANDOM_ACCESS_MEMORY_MODULE.get()),
            key(READ_ONLY_MEMORY_MODULE.get()),
            key(REDSTONE_MODULE.get()),
            key(SEQUENCER_MODULE.get()),
            key(SERIAL_PORT_MODULE.get()),
            key(STACK_MODULE.get()),
            key(TERMINAL_MODULE.get()),
            key(TIMER_MODULE.get())
        );

        tag(BOOKS).add(
            key(BOOK_CODE.get()),
            key(BOOK_MANUAL.get()),
            key(Items.BOOK),
            key(Items.ENCHANTED_BOOK),
            key(Items.WRITABLE_BOOK),
            key(Items.WRITTEN_BOOK)
        );

        tag(KEYS).add(
            key(KEY.get()),
            key(KEY_CREATIVE.get())
        );

        tag(CommonItemTags.CHESTS).add(key(Items.CHEST));
        tag(CommonItemTags.DIAMOND_GEMS).add(key(Items.DIAMOND));
        tag(CommonItemTags.EMERALDS).add(key(Items.EMERALD));
        tag(CommonItemTags.ENDER_PEARLS).add(key(Items.ENDER_PEARL));
        tag(CommonItemTags.GLASS_PANES).add(key(Items.GLASS_PANE));
        tag(CommonItemTags.GOLD_INGOTS).add(key(Items.GOLD_INGOT));
        tag(CommonItemTags.GOLD_NUGGETS).add(key(Items.GOLD_NUGGET));
        tag(CommonItemTags.IRON_BLOCKS).add(key(Items.IRON_BLOCK));
        tag(CommonItemTags.IRON_INGOTS).add(key(Items.IRON_INGOT));
        tag(CommonItemTags.LAPIS_LAZULIS).add(key(Items.LAPIS_LAZULI));
        tag(CommonItemTags.QUARTZ_GEMS).add(key(Items.QUARTZ));
        tag(CommonItemTags.REDSTONE_DUSTS).add(key(Items.REDSTONE));
        tag(CommonItemTags.SAND).add(key(Items.SAND));
    }

    private static ResourceKey<Item> key(final Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
