package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.*;
import net.minecraft.item.Item;
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
    public static final Item BOOK_CODE = new CodeBookItem(new Item.Settings().stackSize(1).itemGroup(API.itemGroup));
    public static final Item BOOK_MANUAL = new ManualBookItem(new Item.Settings().stackSize(16).itemGroup(API.itemGroup));
    public static final Item KEY = new KeyItem(new Item.Settings().stackSize(1).itemGroup(API.itemGroup));
    public static final Item KEY_CREATIVE = new KeyItem(new Item.Settings().stackSize(1).itemGroup(API.itemGroup));
    public static final Item PRISM = new Item(new Item.Settings().stackSize(32).itemGroup(API.itemGroup));
    public static final BlockItem CASING = new BlockItem(Blocks.CASING, new Item.Settings().itemGroup(API.itemGroup));
    public static final BlockItem CONTROLLER = new BlockItem(Blocks.CONTROLLER, new Item.Settings().itemGroup(API.itemGroup));

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

        registerItem(CASING, Constants.NAME_BLOCK_CASING);
        registerItem(CONTROLLER, Constants.NAME_BLOCK_CONTROLLER);
    }

    // --------------------------------------------------------------------- //

    private static void registerItem(final Item item, final Identifier identifier) {
        registerItem(item, identifier, true);
    }

    private static Item registerItem(final Item item, final Identifier identifier, final boolean isListed) {
        Registry.ITEM.register(identifier, item);
        if (!isListed) {
            return null;
        }

        return item;
    }

    private static Item registerModule(final Identifier identifier) {
        final boolean isListed = !Settings.disabledModules.contains(identifier);
        final Item.Settings settings = isListed ? new Item.Settings().itemGroup(API.itemGroup) : new Item.Settings();
        if (Objects.equals(identifier, Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)) {
            return registerItem(new ReadOnlyMemoryModuleItem(settings), identifier, isListed);
        } else {
            return registerItem(new ModuleItem(settings), identifier, isListed);
        }
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
