package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemKey;
import li.cil.tis3d.common.item.ItemModule;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages setup, registration and lookup of items.
 */
@GameRegistry.ObjectHolder(API.MOD_ID)
public final class Items {
    private static final Map<String, Item> modules = new HashMap<>();

    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_BOOK_CODE)
    public static final Item bookCode = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_BOOK_MANUAL)
    public static final Item bookManual = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_KEY)
    public static final Item key = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_KEY_CREATIVE)
    public static final Item keyCreative = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_PRISM)
    public static final Item prism = null;

    public static Map<String, Item> getModules() {
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

    // --------------------------------------------------------------------- //

    public static void register(final IForgeRegistry<Item> registry) {
        for (final String moduleName : Constants.MODULES) {
            final Item module = registerModule(registry, moduleName);
            if (module != null) {
                modules.put(moduleName, module);
            }
        }

        // Prevent stacking of ROM modules to avoid inconsistencies between new
        // and already programmed modules, and allow sneak clicking with it to
        // program it to the clicked RAM/ROM module.
        if (!Settings.disabledModules.contains(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)) {
            ((ItemModule) modules.get(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)).
                    setShareTag(false).
                    setDoesSneakBypassUse(true).
                    setMaxStackSize(1);
        }

        registerItem(registry, new ItemBookCode(), Constants.NAME_ITEM_BOOK_CODE);
        registerItem(registry, new ItemBookManual(), Constants.NAME_ITEM_BOOK_MANUAL);

        registerItem(registry, new ItemKey(), Constants.NAME_ITEM_KEY);
        registerItem(registry, new ItemKey(), Constants.NAME_ITEM_KEY_CREATIVE);
        registerItem(registry, new Item(), Constants.NAME_ITEM_PRISM);

        for (final Block block : Blocks.getAllBlocks()) {
            registerItem(registry, new ItemBlock(block), block.getRegistryName().getResourcePath());
        }
    }

    // --------------------------------------------------------------------- //

    private static Item registerItem(final IForgeRegistry<Item> registry, final Item item, final String name) {
        registry.register(item.
                setUnlocalizedName(API.MOD_ID + "." + name).
                setCreativeTab(API.creativeTab).
                setRegistryName(name));
        return item;
    }

    @Nullable
    private static Item registerModule(final IForgeRegistry<Item> registry, final String name) {
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        return registerItem(registry, new ItemModule(), name);
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
