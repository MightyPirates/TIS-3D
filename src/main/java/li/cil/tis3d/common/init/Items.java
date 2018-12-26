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
    public static final Item BOOK_CODE = new ItemBookCode(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item BOOK_MANUAL = new ItemBookManual(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item KEY = new ItemKey(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item KEY_CREATIVE = new ItemKey(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final Item PRISM = new Item(new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final BlockItem CASING = new BlockItem(Blocks.CASING, new Item.Settings().itemGroup(ItemGroup.REDSTONE));
    public static final BlockItem CONTROLLER = new BlockItem(Blocks.CONTROLLER, new Item.Settings().itemGroup(ItemGroup.REDSTONE));

    private static final Map<Identifier, Item> modules = new HashMap<>();

    public static Map<Identifier, Item> getModules() {
        return modules;
    }

    // --------------------------------------------------------------------- //

    public static boolean isItem(final ItemStack stack, @Nullable final Item item) {
        return !stack.isEmpty() && stack.getItem() == item;
    }

    public static boolean isBookCode(final ItemStack stack) {
        return isItem(stack, BOOK_CODE);
    }

    public static boolean isBookManual(final ItemStack stack) {
        return isItem(stack, BOOK_MANUAL);
    }

    public static boolean isKey(final ItemStack stack) {
        return isItem(stack, KEY) || isKeyCreative(stack);
    }

    public static boolean isKeyCreative(final ItemStack stack) {
        return isItem(stack, KEY_CREATIVE);
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

        registerItem(BOOK_CODE, Constants.NAME_ITEM_BOOK_CODE);
        registerItem(BOOK_MANUAL, Constants.NAME_ITEM_BOOK_MANUAL);

        registerItem(KEY, Constants.NAME_ITEM_KEY);
        registerItem(KEY_CREATIVE, Constants.NAME_ITEM_KEY_CREATIVE);
        registerItem(PRISM, Constants.NAME_ITEM_PRISM);

        Registry.ITEM.register(Registry.BLOCK.getId(Blocks.CASING), CASING);
        Registry.ITEM.register(Registry.BLOCK.getId(Blocks.CONTROLLER), CONTROLLER);
    }

    // --------------------------------------------------------------------- //

    private static Item registerItem(final Item item, final Identifier identifier) {
        Registry.ITEM.register(identifier, item);
        return item;
    }

    @Nullable
    private static Item registerModule(final Identifier identifier) {
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
