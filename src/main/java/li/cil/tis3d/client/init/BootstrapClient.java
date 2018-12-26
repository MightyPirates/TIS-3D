package li.cil.tis3d.client.init;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.prefab.manual.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.manual.TextureTabIconRenderer;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.TagImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.client.render.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.client.render.block.entity.ControllerBlockEntityRenderer;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.client.render.BlockEntityRendererRegistry;
import net.fabricmc.fabric.events.client.ClientTickEvent;
import net.fabricmc.fabric.events.client.SpriteEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import java.util.Collection;

@SuppressWarnings("unused")
public final class BootstrapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register event handlers.
        ClientTickEvent.CLIENT.register(client -> Network.INSTANCE.clientTick());
        SpriteEvent.PROVIDE.register(Textures::registerSprites);

        // Set up tile entity renderer for dynamic module content.
        BlockEntityRendererRegistry.INSTANCE.register(CasingBlockEntity.class, new CasingBlockEntityRenderer());
        BlockEntityRendererRegistry.INSTANCE.register(ControllerBlockEntity.class, new ControllerBlockEntityRenderer());

        // Add default manual providers for client side stuff.
        ManualAPI.addProvider("", new TextureImageProvider());
        ManualAPI.addProvider("item", new ItemImageProvider());
        ManualAPI.addProvider("block", new BlockImageProvider());
        ManualAPI.addProvider("tag", new TagImageProvider());

        ManualAPI.addTab(new TextureTabIconRenderer(new Identifier(API.MOD_ID, "textures/gui/manual_home.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(Blocks.CONTROLLER)), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        ManualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(findModuleItem())), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
        ManualAPI.addTab(new TextureTabIconRenderer(new Identifier(API.MOD_ID, "textures/gui/manual_serial_protocols.png")), "tis3d.manual.serial_protocols", "%LANGUAGE%/serial_protocols.md");
    }

    private static Item findModuleItem() {
        final Item module;
        if (Items.getModules().containsKey(Constants.NAME_ITEM_MODULE_EXECUTION)) {
            module = Items.getModules().get(Constants.NAME_ITEM_MODULE_EXECUTION);
        } else {
            final Collection<Item> allModules = Items.getModules().values();
            if (allModules.isEmpty()) {
                module = net.minecraft.item.Items.REDSTONE;
            } else {
                module = allModules.iterator().next();
            }
        }
        return module;
    }
}
