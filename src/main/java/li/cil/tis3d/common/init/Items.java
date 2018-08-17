package li.cil.tis3d.common.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.Settings;
import li.cil.tis3d.common.block.BlockCasing;
import li.cil.tis3d.common.item.ItemBookCode;
import li.cil.tis3d.common.item.ItemBookManual;
import li.cil.tis3d.common.item.ItemKey;
import li.cil.tis3d.common.item.ItemModule;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.dimdev.rift.listener.ItemAdder;
import pl.asie.protocharset.rift.listeners.RightClickListener;

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
public final class Items implements ItemAdder, RightClickListener {
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

    // TODO: This is a doesSneakBypassUse hack.
    @Override
    public EnumActionResult onRightClick(EntityPlayer entityPlayer, World world, EnumHand enumHand, RayTraceResult rayTraceResult) {
        if (!entityPlayer.isSneaking() || rayTraceResult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return EnumActionResult.PASS;
        }

        ItemStack stack = entityPlayer.getHeldItem(enumHand);
        if (stack.getItem() instanceof ItemBookCode || stack.getItem() instanceof ItemKey || stack.getItem() instanceof ItemModuleReadOnlyMemory) {
            IBlockState state = world.getBlockState(rayTraceResult.getBlockPos());
            if (state.getBlock() instanceof BlockCasing) {
                return state.onBlockActivated(world, rayTraceResult.getBlockPos(), entityPlayer, enumHand, rayTraceResult.sideHit,
                        (float) rayTraceResult.hitVec.x - rayTraceResult.getBlockPos().getX(),
                         (float) rayTraceResult.hitVec.y - rayTraceResult.getBlockPos().getY(),
                        (float) rayTraceResult.hitVec.z - rayTraceResult.getBlockPos().getZ()) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
            }
        }

        return EnumActionResult.PASS;
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
