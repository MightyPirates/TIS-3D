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
    protected void registerRecipes(final Consumer<IFinishedRecipe> consumer) {
        ShapedRecipeBuilder
            .shapedRecipe(Items.CASING.get(), 8)
            .patternLine("IRI")
            .patternLine("RSR")
            .patternLine("IRI")
            .key('I', Tags.Items.INGOTS_IRON)
            .key('R', Tags.Items.DUSTS_REDSTONE)
            .key('S', Tags.Items.STORAGE_BLOCKS_IRON)
            .addCriterion("has_redstone", inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .build(consumer);

        ShapedRecipeBuilder
            .shapedRecipe(Items.CONTROLLER.get())
            .patternLine("IRI")
            .patternLine("RSR")
            .patternLine("IRI")
            .key('I', Tags.Items.INGOTS_IRON)
            .key('R', Tags.Items.DUSTS_REDSTONE)
            .key('S', Tags.Items.GEMS_DIAMOND)
            .addCriterion("has_redstone", inventoryChange(Tags.Items.DUSTS_REDSTONE))
            .build(consumer);

        ShapedRecipeBuilder
            .shapedRecipe(Items.KEY.get())
            .patternLine("GI ")
            .patternLine("GI ")
            .patternLine("LRQ")
            .key('L', Tags.Items.GEMS_LAPIS)
            .key('G', Tags.Items.NUGGETS_GOLD)
            .key('I', Tags.Items.INGOTS_IRON)
            .key('Q', Tags.Items.GEMS_QUARTZ)
            .key('R', Tags.Items.DUSTS_REDSTONE)
            .addCriterion("has_casing", inventoryChange(Items.CASING.get()))
            .build(consumer);

        ShapelessRecipeBuilder
            .shapelessRecipe(Items.PRISM.get())
            .addIngredient(Tags.Items.GEMS_QUARTZ)
            .addIngredient(Tags.Items.DUSTS_REDSTONE)
            .addIngredient(Tags.Items.GEMS_LAPIS)
            .addIngredient(Tags.Items.GEMS_EMERALD)
            .addCriterion("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .build(consumer);

        module(Items.AUDIO_MODULE, 2, net.minecraft.item.Items.NOTE_BLOCK)
            .addCriterion("has_sequencer_module", inventoryChange(Items.SEQUENCER_MODULE.get()))
            .build(consumer);
        module(Items.DISPLAY_MODULE, 2, Items.PRISM.get())
            .addCriterion("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .build(consumer);
        module(Items.EXECUTION_MODULE, 2, Tags.Items.INGOTS_GOLD)
            .addCriterion("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .build(consumer);
        module(Items.FACADE_MODULE, 8, net.minecraft.item.Items.PAPER)
            .build(consumer);
        module(Items.INFRARED_MODULE, 2, net.minecraft.item.Items.SPIDER_EYE)
            .addCriterion("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .build(consumer);
        module(Items.KEYPAD_MODULE, 2, ItemTags.BUTTONS)
            .addCriterion("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .build(consumer);
        module(Items.RANDOM_MODULE, 2, Tags.Items.ENDER_PEARLS)
            .addCriterion("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .build(consumer);
        module(Items.RANDOM_ACCESS_MEMORY_MODULE, 2, Tags.Items.GEMS_EMERALD)
            .addCriterion("has_stack_module", inventoryChange(Items.STACK_MODULE.get()))
            .build(consumer);
        module(Items.READ_ONLY_MEMORY_MODULE, 2, li.cil.tis3d.common.tags.ItemTags.BOOKS)
            .addCriterion("has_stack_module", inventoryChange(Items.STACK_MODULE.get()))
            .build(consumer);
        module(Items.REDSTONE_MODULE, 2, net.minecraft.item.Items.REPEATER)
            .build(consumer);
        module(Items.SEQUENCER_MODULE, 2, ItemTags.MUSIC_DISCS)
            .addCriterion("has_queue", inventoryChange(Items.QUEUE_MODULE.get()))
            .build(consumer);
        module(Items.SERIAL_PORT_MODULE, 2, Tags.Items.GEMS_QUARTZ)
            .addCriterion("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .build(consumer);
        module(Items.STACK_MODULE, 2, Tags.Items.CHESTS)
            .addCriterion("has_redstone_module", inventoryChange(Items.REDSTONE_MODULE.get()))
            .build(consumer);
        module(Items.TIMER_MODULE, 2, Tags.Items.SAND)
            .addCriterion("has_execution_module", inventoryChange(Items.EXECUTION_MODULE.get()))
            .build(consumer);

        ShapedRecipeBuilder
            .shapedRecipe(Items.TERMINAL_MODULE.get())
            .patternLine("KDS")
            .patternLine("IQI")
            .patternLine(" R ")
            .key('K', Items.KEYPAD_MODULE.get())
            .key('D', Items.DISPLAY_MODULE.get())
            .key('S', Items.STACK_MODULE.get())
            .key('I', Tags.Items.INGOTS_IRON)
            .key('Q', Tags.Items.GEMS_QUARTZ)
            .key('R', Tags.Items.DUSTS_REDSTONE)
            .addCriterion("has_casing", inventoryChange(Items.CASING.get()))
            .addCriterion("has_keypad", inventoryChange(Items.KEYPAD_MODULE.get()))
            .build(consumer);

        ShapelessRecipeBuilder
            .shapelessRecipe(Items.QUEUE_MODULE.get())
            .addIngredient(Items.STACK_MODULE.get())
            .addCriterion("has_stack", inventoryChange(Items.STACK_MODULE.get()))
            .build(consumer, new ResourceLocation(API.MOD_ID, Items.QUEUE_MODULE.getId().getPath() + "/from_stack"));
        ShapelessRecipeBuilder
            .shapelessRecipe(Items.STACK_MODULE.get())
            .addIngredient(Items.QUEUE_MODULE.get())
            .addCriterion("has_queue", inventoryChange(Items.QUEUE_MODULE.get()))
            .build(consumer, new ResourceLocation(API.MOD_ID, Items.STACK_MODULE.getId().getPath() + "/from_queue"));
    }

    private static ShapedRecipeBuilder module(final RegistryObject<? extends Item> module, final int count, final Item item) {
        return module(module, count)
            .key('S', item);
    }

    private static ShapedRecipeBuilder module(final RegistryObject<? extends Item> module, final int count, final ITag<Item> tag) {
        return module(module, count)
            .key('S', tag);
    }

    private static ShapedRecipeBuilder module(final RegistryObject<? extends Item> module, final int count) {
        return ShapedRecipeBuilder
            .shapedRecipe(module.get(), count)
            .patternLine("PPP")
            .patternLine("ISI")
            .patternLine(" R ")
            .key('P', Tags.Items.GLASS_PANES_COLORLESS)
            .key('I', Tags.Items.INGOTS_IRON)
            .key('R', Tags.Items.DUSTS_REDSTONE)
            .addCriterion("has_casing", inventoryChange(Items.CASING.get()));
    }

    private static InventoryChangeTrigger.Instance inventoryChange(final ITag<Item> tag) {
        return InventoryChangeTrigger.Instance.forItems(ItemPredicate.Builder.create().tag(tag).build());
    }

    private static InventoryChangeTrigger.Instance inventoryChange(final IItemProvider item) {
        return InventoryChangeTrigger.Instance.forItems(item);
    }
}
