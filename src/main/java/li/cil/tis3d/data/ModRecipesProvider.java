package li.cil.tis3d.data;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.item.Items;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.Consumer;

public class ModRecipesProvider extends RecipeProvider {
    public ModRecipesProvider(final DataGenerator generator) {
        super(generator);
    }

    @Override
    protected void buildShapelessRecipes(final Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder
            .shaped(Items.CASING.get(), 8)
            .pattern("IRI")
            .pattern("RSR")
            .pattern("IRI")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('S', Tags.Items.STORAGE_BLOCKS_IRON)
            .unlockedBy("has_redstone", inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .save(consumer);

        ShapedRecipeBuilder
            .shaped(Items.CONTROLLER.get())
            .pattern("IRI")
            .pattern("RSR")
            .pattern("IRI")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .define('S', Tags.Items.GEMS_DIAMOND)
            .unlockedBy("has_redstone", inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .save(consumer);

        ShapedRecipeBuilder
            .shaped(Items.KEY.get())
            .pattern("GI ")
            .pattern("GI ")
            .pattern("LRQ")
            .define('L', Tags.Items.GEMS_LAPIS)
            .define('G', Tags.Items.NUGGETS_GOLD)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('Q', Tags.Items.GEMS_QUARTZ)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_casing", inventoryChange(Items.CASING.get()))
            .save(consumer);

        ShapelessRecipeBuilder
            .shapeless(Items.PRISM.get())
            .requires(Tags.Items.GEMS_QUARTZ)
            .requires(Tags.Items.DUSTS_REDSTONE)
            .requires(Tags.Items.GEMS_LAPIS)
            .requires(Tags.Items.GEMS_EMERALD)
            .unlockedBy("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(consumer);

        module(Items.AUDIO_MODULE, 2, net.minecraft.item.Items.NOTE_BLOCK)
            .unlockedBy("has_sequencer_module", inventoryChange(Items.SEQUENCER_MODULE.get()))
            .save(consumer);
        module(Items.DISPLAY_MODULE, 2, Items.PRISM.get())
            .unlockedBy("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(consumer);
        module(Items.EXECUTION_MODULE, 2, Tags.Items.INGOTS_GOLD)
            .unlockedBy("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(consumer);
        module(Items.FACADE_MODULE, 8, net.minecraft.item.Items.PAPER)
            .save(consumer);
        module(Items.INFRARED_MODULE, 2, net.minecraft.item.Items.SPIDER_EYE)
            .unlockedBy("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(consumer);
        module(Items.KEYPAD_MODULE, 2, ItemTags.BUTTONS)
            .unlockedBy("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(consumer);
        module(Items.RANDOM_MODULE, 2, Tags.Items.ENDER_PEARLS)
            .unlockedBy("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(consumer);
        module(Items.RANDOM_ACCESS_MEMORY_MODULE, 2, Tags.Items.GEMS_EMERALD)
            .unlockedBy("has_stack_module", inventoryChange(Items.STACK_MODULE.get()))
            .save(consumer);
        module(Items.READ_ONLY_MEMORY_MODULE, 2, li.cil.tis3d.common.tags.ItemTags.BOOKS)
            .unlockedBy("has_stack_module", inventoryChange(Items.STACK_MODULE.get()))
            .save(consumer);
        module(Items.REDSTONE_MODULE, 2, net.minecraft.item.Items.REPEATER)
            .save(consumer);
        module(Items.SEQUENCER_MODULE, 2, ItemTags.MUSIC_DISCS)
            .unlockedBy("has_queue", inventoryChange(Items.QUEUE_MODULE.get()))
            .save(consumer);
        module(Items.SERIAL_PORT_MODULE, 2, Tags.Items.GEMS_QUARTZ)
            .unlockedBy("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(consumer);
        module(Items.STACK_MODULE, 2, Tags.Items.CHESTS)
            .unlockedBy("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .save(consumer);
        module(Items.TIMER_MODULE, 2, Tags.Items.SAND)
            .unlockedBy("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .save(consumer);

        ShapedRecipeBuilder
            .shaped(Items.TERMINAL_MODULE.get())
            .pattern("KDS")
            .pattern("IQI")
            .pattern(" R ")
            .define('K', Items.KEYPAD_MODULE.get())
            .define('D', Items.DISPLAY_MODULE.get())
            .define('S', Items.STACK_MODULE.get())
            .define('I', Tags.Items.INGOTS_IRON)
            .define('Q', Tags.Items.GEMS_QUARTZ)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_casing", inventoryChange(Items.CASING.get()))
            .unlockedBy("has_keypad", inventoryChange(Items.KEYPAD_MODULE.get()))
            .save(consumer);

        ShapelessRecipeBuilder
            .shapeless(Items.QUEUE_MODULE.get())
            .requires(Items.STACK_MODULE.get())
            .unlockedBy("has_stack", inventoryChange(Items.STACK_MODULE.get()))
            .save(consumer, new ResourceLocation(API.MOD_ID, Items.QUEUE_MODULE.getId().getPath() + "/from_stack"));
        ShapelessRecipeBuilder
            .shapeless(Items.STACK_MODULE.get())
            .requires(Items.QUEUE_MODULE.get())
            .unlockedBy("has_queue", inventoryChange(Items.QUEUE_MODULE.get()))
            .save(consumer, new ResourceLocation(API.MOD_ID, Items.STACK_MODULE.getId().getPath() + "/from_queue"));
    }

    private static ShapedRecipeBuilder module(final RegistryObject<? extends Item> module, final int count, final Item item) {
        return module(module, count)
            .define('S', item);
    }

    private static ShapedRecipeBuilder module(final RegistryObject<? extends Item> module, final int count, final ITag<Item> tag) {
        return module(module, count)
            .define('S', tag);
    }

    private static ShapedRecipeBuilder module(final RegistryObject<? extends Item> module, final int count) {
        return ShapedRecipeBuilder
            .shaped(module.get(), count)
            .pattern("PPP")
            .pattern("ISI")
            .pattern(" R ")
            .define('P', Tags.Items.GLASS_PANES_COLORLESS)
            .define('I', Tags.Items.INGOTS_IRON)
            .define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_casing", inventoryChange(Items.CASING.get()));
    }

    private static InventoryChangeTrigger.Instance inventoryChange(final ITag<Item> tag) {
        return InventoryChangeTrigger.Instance.hasItems(ItemPredicate.Builder.item().of(tag).build());
    }

    private static InventoryChangeTrigger.Instance inventoryChange(final IItemProvider item) {
        return InventoryChangeTrigger.Instance.hasItems(item);
    }
}
