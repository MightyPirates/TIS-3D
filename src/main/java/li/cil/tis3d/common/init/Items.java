package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemModuleLightweight;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items {
    public static Map<String, Item> modules = new HashMap<>();
    public static Item bookCode;
    public static Item bookManual;
    public static Item key;
    public static Item keyCreative;
    public static Item prism;

    // --------------------------------------------------------------------- //

    public static boolean isItem(final ItemStack stack, final Item item) {
        return stack != null && stack.getItem() == item;
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
        for (final String module : Constants.MODULES) {
            modules.put(module, proxy.registerModule(module));
        }

        bookCode = proxy.registerItem(Constants.NAME_ITEM_BOOK_CODE, ItemBookCode::new);
        bookManual = proxy.registerItem(Constants.NAME_ITEM_BOOK_MANUAL, ItemBookManual::new);

        key = proxy.registerItem(Constants.NAME_ITEM_KEY, Item::new).setMaxStackSize(1);
        keyCreative = proxy.registerItem(Constants.NAME_ITEM_KEY_CREATIVE, Item::new).setMaxStackSize(1);
        prism = proxy.registerItem(Constants.NAME_ITEM_PRISM, Item::new);
    }

    public static void addRecipes() {
        addModuleRecipe(Constants.NAME_ITEM_MODULE_AUDIO, Item.getItemFromBlock(net.minecraft.init.Blocks.noteblock));
        addModuleRecipe(Constants.NAME_ITEM_MODULE_BUNDLED_REDSTONE, net.minecraft.init.Items.comparator);
        if (Settings.customModules.length != 0)
            addModuleRecipe(Constants.NAME_ITEM_MODULE_CUSTOM, net.minecraft.init.Items.apple);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_DISPLAY, prism);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_EXECUTION, "ingotGold");
        addModuleRecipe(Constants.NAME_ITEM_MODULE_INFRARED, net.minecraft.init.Items.spider_eye);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_KEYPAD, net.minecraft.init.Blocks.stone_button);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_RANDOM, net.minecraft.init.Items.ender_pearl);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_RANDOM_ACCESS_MEMORY, "gemEmerald");
        addModuleRecipe(Constants.NAME_ITEM_MODULE_REDSTONE, net.minecraft.init.Items.repeater);
        addModuleRecipe(Constants.NAME_ITEM_MODULE_SERIAL_PORT, "blockQuartz");
        addModuleRecipe(Constants.NAME_ITEM_MODULE_STACK, Item.getItemFromBlock(net.minecraft.init.Blocks.chest));

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
        if (Settings.disabledModules.contains(name)) {
            return;
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(GameRegistry.findItem(API.MOD_ID, name), 2),
                "PPP",
                "ISI",
                " R ",
                'P', "paneGlassColorless",
                'I', "ingotIron",
                'R', "dustRedstone",
                'S', specialIngredient));
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
