package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemKey;
import li.cil.tis3d.common.item.ItemModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items {
    public static final Map<String, Item> modules = new HashMap<>();
    public static Item bookCode;
    public static Item bookManual;
    public static Item key;
    public static Item keyCreative;
    public static Item prism;

    // --------------------------------------------------------------------- //

    public static boolean isItem(final ItemStack stack, final Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    public static boolean isBookCode(final ItemStack stack) {
        return isItem(stack, bookCode);
    }

    public static boolean isBookManual(final ItemStack stack) {
        return isItem(stack, bookManual);
    }

    public static boolean isKey(final ItemStack stack) {
        return isItem(stack, key) || isKeyCreative(stack);
    }

    public static boolean isKeyCreative(final ItemStack stack) {
        return isItem(stack, keyCreative);
    }

    // --------------------------------------------------------------------- //

    public static void register(final ProxyCommon proxy) {
        for (final String moduleName : Constants.MODULES) {
            final Item module = proxy.registerModule(moduleName);
            if (module != null) {
                modules.put(moduleName, module);
            }
        }

        // Prevent stacking of ROM modules to avoid inconsistencies between new
        // and already programmed modules, and allow sneak clicking with it to
        // program it to the clicked RAM/ROM module.
        ((ItemModule) modules.get(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)).
                setShareTag(false).
                setDoesSneakBypassUse(true).
                setMaxStackSize(1);

        bookCode = proxy.registerItem(Constants.NAME_ITEM_BOOK_CODE, ItemBookCode::new);
        bookManual = proxy.registerItem(Constants.NAME_ITEM_BOOK_MANUAL, ItemBookManual::new);

        key = proxy.registerItem(Constants.NAME_ITEM_KEY, ItemKey::new);
        keyCreative = proxy.registerItem(Constants.NAME_ITEM_KEY_CREATIVE, ItemKey::new);
        prism = proxy.registerItem(Constants.NAME_ITEM_PRISM, Item::new);
    }

    public static void addRecipes() {
        addModuleRecipe(Constants.NAME_ITEM_MODULE_AUDIO, Item.getItemFromBlock(net.minecraft.init.Blocks.NOTEBLOCK));
        addModuleRecipe(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, net.minecraft.init.Items.COMPARATOR);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_DISPLAY, prism);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_EXECUTION, "ingotGold");
        addModuleRecipe(Constants.NAME_ITEM_MODULE_INFRARED, net.minecraft.init.Items.SPIDER_EYE);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_KEYPAD, net.minecraft.init.Blocks.STONE_BUTTON);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_RANDOM, net.minecraft.init.Items.ENDER_PEARL);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, "gemEmerald");
        addModuleRecipe(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY, net.minecraft.init.Items.BOOK);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_REDSTONE, net.minecraft.init.Items.REPEATER);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_SERIAL_PORT, "blockQuartz");

        addModuleRecipe(Constants.NAME_ITEM_MODULE_STACK, Item.getItemFromBlock(net.minecraft.init.Blocks.CHEST), false);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_QUEUE, Item.getItemFromBlock(net.minecraft.init.Blocks.CHEST), true);
        addInversionRecipes(Constants.NAME_ITEM_MODULE_STACK, Constants.NAME_ITEM_MODULE_QUEUE);

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(modules.get(Constants.NAME_ITEM_MODULE_TERMINAL), 2),
                "KDS",
                "IQI",
                " R ",
                'K', modules.get(Constants.NAME_ITEM_MODULE_KEYPAD),
                'D', modules.get(Constants.NAME_ITEM_MODULE_DISPLAY),
                'S', modules.get(Constants.NAME_ITEM_MODULE_STACK),
                'I', "ingotIron",
                'R', "dustRedstone",
                'Q', "gemQuartz"));

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(key, 1),
                "GI ",
                "GI ",
                "LRQ",
                'G', "nuggetGold",
                'I', "ingotIron",
                'L', "gemLapis",
                'R', "dustRedstone",
                'Q', "gemQuartz"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(prism, 1),
                "gemQuartz",
                "dustRedstone",
                "gemLapis",
                "gemEmerald"));
    }

    private static void addModuleRecipe(final String name, final Object specialIngredient) {
        addModuleRecipe(name, specialIngredient, false);
    }

    private static void addModuleRecipe(final String name, final Object specialIngredient, final boolean invert) {
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        final Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(API.MOD_ID, name));
        assert item != null;
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(item, 2),
                "PPP",
                "ISI",
                " R ",
                'P', "paneGlassColorless",
                'I', "ingotIron",
                'R', invert ? net.minecraft.init.Blocks.REDSTONE_TORCH : "dustRedstone",
                'S', specialIngredient));
    }

    public static void addInversionRecipes(final String normal, final String inverted) {
        if (Settings.disabledModules.contains(normal) || Settings.disabledModules.contains(inverted)) {
            return;
        }

        final Item normalItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(API.MOD_ID, normal));
        final Item invertedItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation(API.MOD_ID, inverted));
        assert normalItem != null && invertedItem != null;

        GameRegistry.addShapelessRecipe(new ItemStack(invertedItem), normalItem, net.minecraft.init.Blocks.REDSTONE_TORCH);
        GameRegistry.addShapelessRecipe(new ItemStack(normalItem), invertedItem, net.minecraft.init.Blocks.REDSTONE_TORCH);
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
