package li.cil.tis3d.common.init;

import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.item.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.events.PlayerInteractionEvent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.block.BlockItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Manages setup, registration and lookup of items.
 */
public final class Items implements ModInitializer {
    private static final Map<Identifier, Item> modules = new HashMap<>();

    public static Item bookCode = null;
    public static Item bookManual = null;
    public static Item key = null;
    public static Item keyCreative = null;
    public static Item prism = null;
    public static BlockItem casing = null;
    public static BlockItem controller = null;

    public static Map<Identifier, Item> getModules() {
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

    // TODO This is a doesSneakBypassUse hack.
    public ActionResult onRightClick(PlayerEntity entityPlayer, World world, Hand enumHand, BlockPos pos, Direction direction, float hitX, float hitY, float hitZ) {
        if (!entityPlayer.isSneaking()) {
            return ActionResult.PASS;
        }

        ItemStack stack = entityPlayer.getStackInHand(enumHand);
        if (stack.getItem() instanceof ItemBookCode || stack.getItem() instanceof ItemKey || stack.getItem() instanceof ItemModuleReadOnlyMemory) {
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof BlockCasing) {
                return state.activate(world, pos, entityPlayer, enumHand, direction, hitX, hitY, hitZ) ? ActionResult.SUCCESS : ActionResult.PASS;
            }
        }

        return ActionResult.PASS;
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

    public void registerItems() {
        for (final Identifier moduleName : Constants.MODULES) {
            final Item module = registerModule(moduleName);
            if (module != null) {
                modules.put(moduleName, module);
            }
        }

        registerItem(bookCode = new ItemBookCode(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), Constants.NAME_ITEM_BOOK_CODE);
        registerItem(bookManual = new ItemBookManual(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), Constants.NAME_ITEM_BOOK_MANUAL);

        registerItem(key = new ItemKey(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), Constants.NAME_ITEM_KEY);
        registerItem(keyCreative = new ItemKey(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), Constants.NAME_ITEM_KEY_CREATIVE);
        registerItem(prism = new Item(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), Constants.NAME_ITEM_PRISM);

        Registry.ITEM.register(Registry.BLOCK.getId(Blocks.casing), casing = new BlockItem(Blocks.casing, new Item.Settings().itemGroup(ItemGroup.REDSTONE)));
        Registry.ITEM.register(Registry.BLOCK.getId(Blocks.controller), controller = new BlockItem(Blocks.controller, new Item.Settings().itemGroup(ItemGroup.REDSTONE)));
    }

    // --------------------------------------------------------------------- //

    private static Item registerItem(final Item item, final Identifier name) {
        Registry.ITEM.register(name, item);
        return item;
    }

    @Nullable
    private static Item registerModule(final Identifier name) {
        Settings.load();
        if (Settings.disabledModules.contains(name)) {
            return null;
        }

        if (Objects.equals(name, Constants.NAME_ITEM_MODULE_READ_ONLY_MEMORY)) {
            return registerItem(new ItemModuleReadOnlyMemory(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), name);
        } else {
            return registerItem(new ItemModule(new Item.Settings().itemGroup(ItemGroup.REDSTONE)), name);
        }
    }

    @Override
    public void onInitialize() {
        registerItems();
        PlayerInteractionEvent.INTERACT_BLOCK.register(this::onRightClick);
    }

    // --------------------------------------------------------------------- //

}
