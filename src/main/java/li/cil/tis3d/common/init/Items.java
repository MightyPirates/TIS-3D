package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemKey;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.dimdev.rift.listener.ItemAdder;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items implements ItemAdder {
    private static final Map<ResourceLocation, Item> modules = new HashMap<>();

    public static Item bookCode = null;
    public static Item bookManual = null;
    public static Item key = null;
    public static Item keyCreative = null;
    public static Item prism = null;
    public static ItemBlock casing = null;
    public static ItemBlock controller = null;

    public static Map<ResourceLocation, Item> getModules() {
        return modules;
    }

    public static List<Item> getAllItems() {
        final List<Item> result = new ArrayList<>(modules.values());
        result.addAll(Arrays.asList(
                bookCode,
                bookManual,
                key,
                keyCreative,
                prism,
                Item.getItemFromBlock(Blocks.casing),
                Item.getItemFromBlock(Blocks.controller)
        ));
        return result;
    }

    // --------------------------------------------------------------------- //

    public static boolean isItem(final ItemStack stack, @Nullable final Item item) {
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

    public static boolean isModuleReadOnlyMemory(final ItemStack stack) {
        return isItem(stack, modules.get(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY));
    }

    // --------------------------------------------------------------------- //


    @Override
    public void registerItems() {
        for (final ResourceLocation moduleName : Constants.MODULES) {
            final Item module = registerModule(moduleName);
            if (module != null) {
                modules.put(moduleName, module);
            }
        }

        registerItem(bookCode = new ItemBookCode(new Item.Builder().group(ItemGroup.REDSTONE)), Constants.NAME_ITEM_BOOK_CODE);
        registerItem(bookManual = new ItemBookManual(new Item.Builder().group(ItemGroup.REDSTONE)), Constants.NAME_ITEM_BOOK_MANUAL);

        registerItem(key = new ItemKey(new Item.Builder().group(ItemGroup.REDSTONE)), Constants.NAME_ITEM_KEY);
        registerItem(keyCreative = new ItemKey(new Item.Builder().group(ItemGroup.REDSTONE)), Constants.NAME_ITEM_KEY_CREATIVE);
        registerItem(prism = new Item(new Item.Builder().group(ItemGroup.REDSTONE)), Constants.NAME_ITEM_PRISM);

        Item.registerItemBlock(casing = new ItemBlock(Blocks.casing, new Item.Builder().group(ItemGroup.REDSTONE)));
	    Item.registerItemBlock(controller = new ItemBlock(Blocks.controller, new Item.Builder().group(ItemGroup.REDSTONE)));
    }

    // --------------------------------------------------------------------- //

    private static Item registerItem(final Item item, final ResourceLocation name) {
        Item.registerItem(name, item);
        return item;
    }

    @Nullable
    private static Item registerModule(final ResourceLocation name) {
    	Settings.load();
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        if (Objects.equals(name, Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)) {
            return registerItem(new ItemModuleReadOnlyMemory(new Item.Builder().group(ItemGroup.REDSTONE)), name);
        } else {
            return registerItem(new ItemModule(new Item.Builder().group(ItemGroup.REDSTONE)), name);
        }
    }

    // --------------------------------------------------------------------- //

}
