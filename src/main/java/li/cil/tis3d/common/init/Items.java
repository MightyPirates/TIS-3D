package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items {
    public static final Item bookCode = new ItemBookCode(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item bookManual = new ItemBookManual(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item key = new ItemKey(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item keyCreative = new ItemKey(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item prism = new Item(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final BlockItem casing = new BlockItem(Blocks.casing, new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final BlockItem controller = new BlockItem(Blocks.controller, new Item.Settings().itemGroup(ItemGroup.REDSTONE));

    private static final Map<Identifier, Item> modules = new HashMap<>();

    public static Map<Identifier, Item> getModules() {
        return modules;
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

    static void registerItems() {
        for (final Identifier moduleName : Constants.MODULES) {
            final Item module = registerModule(moduleName);
            if (module != null) {
                modules.put(moduleName, module);
            }
        }

        registerItem(bookCode, Constants.NAME_ITEM_BOOK_CODE);
        registerItem(bookManual, Constants.NAME_ITEM_BOOK_MANUAL);

        registerItem(key, Constants.NAME_ITEM_KEY);
        registerItem(keyCreative, Constants.NAME_ITEM_KEY_CREATIVE);
        registerItem(prism, Constants.NAME_ITEM_PRISM);

        Registry.ITEM.register(Registry.BLOCK.getId(Blocks.casing), casing);
        Registry.ITEM.register(Registry.BLOCK.getId(Blocks.controller), controller);
    }

    // --------------------------------------------------------------------- //

    private static Item registerItem(final Item item, final Identifier identifier) {
        Registry.ITEM.register(identifier, item);
        return item;
    }

    @Nullable
    private static Item registerModule(final Identifier identifier) {
        Settings.load();
        if (Settings.disabledModules.contains(identifier.getPath())) {
            return null;
        }

        if (Objects.equals(identifier, Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)) {
            return registerItem(new ItemModuleReadOnlyMemory(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), identifier);
        } else {
            return registerItem(new ItemModule(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), identifier);
        }
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
