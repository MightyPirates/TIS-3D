package li.cil.tis3d.client.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.prefab.manual.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.manual.TextureTabIconRenderer;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class Manual implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ManualAPI.addProvider("", new TextureImageProvider());
        ManualAPI.addProvider("item", new ItemImageProvider());
        ManualAPI.addProvider("block", new BlockImageProvider());
        // TODO: TagImageProvider
        // ManualAPI.addProvider("oredict", new OreDictImageProvider());

        ManualAPI.addTab(new TextureTabIconRenderer(new Identifier(API.MOD_ID, "textures/gui/manual_home.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(Blocks.controller)), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        final Item module;
        if (Items.getModules().containsKey(Constants.NAME_ITEM_MODULE_EXECUTION)) {
            module = Items.getModules().get(Constants.NAME_ITEM_MODULE_EXECUTION);
        } else {
            final Collection<Item> allModules = Items.getModules().values();
            if (allModules.isEmpty()) {
                module = net.minecraft.item.Items.field_8158;
            } else {
                module = allModules.iterator().next();
            }
        }
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(module)), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
        ManualAPI.addTab(new TextureTabIconRenderer(new Identifier(API.MOD_ID, "textures/gui/manual_serial_protocols.png")), "tis3d.manual.serialProtocols", "%LANGUAGE%/serial_protocols.md");
    }
}
