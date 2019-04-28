package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.item.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * Manages setup, registration and lookup of items.
 */
@GameRegistry.ObjectHolder(API.MOD_ID)
public final class Items {
    private static final Map<String, Item> MODULES = new HashMap<>();
    private static final Map<String, Supplier<ItemModule>> ITEM_CONSTRUCTOR_OVERRIDES = new HashMap<>();

    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_BOOK_CODE)
    public static final Item BOOK_CODE = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_BOOK_MANUAL)
    public static final Item BOOK_MANUAL = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_KEY)
    public static final Item KEY = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_KEY_CREATIVE)
    public static final Item KEY_CREATIVE = null;
    @GameRegistry.ObjectHolder(Constants.NAME_ITEM_PRISM)
    public static final Item PRISM = null;

    public static Map<String, Item> getModules() {
        return MODULES;
    }

    public static void registerModuleItemOverride(final String moduleName, final Supplier<ItemModule> itemConstructor) {
        ITEM_CONSTRUCTOR_OVERRIDES.put(moduleName, itemConstructor);
    }

    public static List<Item> getAllItems() {
        final List<Item> result = new ArrayList<>(MODULES.values());
        result.addAll(Arrays.asList(
            BOOK_CODE,
            BOOK_MANUAL,
            KEY,
            KEY_CREATIVE,
            PRISM,
            Item.getItemFromBlock(Blocks.CASING),
            Item.getItemFromBlock(Blocks.CONTROLLER)
        ));
        return result;
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
        return isItem(stack, MODULES.get(Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY));
    }

    // --------------------------------------------------------------------- //

    public static void register(final IForgeRegistry<Item> registry) {
        for (final String moduleName : Constants.MODULES) {
            final Item module = registerModule(registry, moduleName);
            if (module != null) {
                MODULES.put(moduleName, module);
            }
        }

        registerItem(registry, new ItemBookCode(), Constants.NAME_ITEM_BOOK_CODE);
        registerItem(registry, new ItemBookManual(), Constants.NAME_ITEM_BOOK_MANUAL);

        registerItem(registry, new ItemKey(), Constants.NAME_ITEM_KEY);
        registerItem(registry, new ItemKey(), Constants.NAME_ITEM_KEY_CREATIVE);
        registerItem(registry, new Item(), Constants.NAME_ITEM_PRISM);

        for (final Block block : Blocks.getAllBlocks()) {
            registerItem(registry, new ItemBlock(block), block.getRegistryName().getPath());
        }
    }

    // --------------------------------------------------------------------- //

    private static Item registerItem(final IForgeRegistry<Item> registry, final Item item, final String name) {
        registry.register(item.
            setTranslationKey(API.MOD_ID + "." + name).
            setCreativeTab(API.creativeTab).
            setRegistryName(name));
        return item;
    }

    @Nullable
    private static Item registerModule(final IForgeRegistry<Item> registry, final String name) {
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        return registerItem(registry, ITEM_CONSTRUCTOR_OVERRIDES.getOrDefault(name, ItemModule::new).get(), name);
    }

    // --------------------------------------------------------------------- //

    private Items() {
    }
}
