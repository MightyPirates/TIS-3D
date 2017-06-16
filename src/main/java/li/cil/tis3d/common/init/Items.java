package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.ProxyCommon;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemKey;
import li.cil.tis3d.common.item.ItemModule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
