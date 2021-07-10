package li.cil.tis3d.client.init;

import li.cil.tis3d.api.ClientAPI;
import li.cil.tis3d.api.ClientExtInitializer;
import li.cil.tis3d.api.ManualClientAPI;
import li.cil.tis3d.api.prefab.manual.ItemStackTabIconRenderer;
import li.cil.tis3d.api.prefab.manual.ResourceContentProvider;
import li.cil.tis3d.api.prefab.manual.TextureTabIconRenderer;
import li.cil.tis3d.client.api.FontRendererAPIImpl;
import li.cil.tis3d.client.manual.provider.BlockImageProvider;
import li.cil.tis3d.client.manual.provider.ItemImageProvider;
import li.cil.tis3d.client.manual.provider.TagImageProvider;
import li.cil.tis3d.client.manual.provider.TextureImageProvider;
import li.cil.tis3d.client.render.block.entity.CasingBlockEntityRenderer;
import li.cil.tis3d.client.render.block.entity.ControllerBlockEntityRenderer;
import li.cil.tis3d.client.render.entity.InvisibleEntityRenderer;
import li.cil.tis3d.common.API;
import li.cil.tis3d.common.Constants;
import li.cil.tis3d.common.api.SerialAPIImpl;
import li.cil.tis3d.common.block.CasingBlock;
import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import li.cil.tis3d.common.entity.InfraredPacketEntity;
import li.cil.tis3d.common.init.Blocks;
import li.cil.tis3d.common.init.BootstrapCommon;
import li.cil.tis3d.common.init.Entities;
import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.module.DisplayModule;
import li.cil.tis3d.common.network.Network;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPickBlockGatherCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.List;

@Environment(EnvType.CLIENT)
public final class BootstrapClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register network handler.
        Network.INSTANCE.initClient();

        // Initialize API.
        API.fontRenderer = new FontRendererAPIImpl();
        final ClientAPI clientAPI = new ClientAPI();
        clientAPI.fontRenderer = API.fontRenderer;
        clientAPI.manual = API.manual;

        // Register event handlers.
        ClientTickCallback.EVENT.register(client -> DisplayModule.LeakDetector.tick());
        ClientTickCallback.EVENT.register(client -> Network.INSTANCE.clientTick());
        ClientSpriteRegistryCallback.event(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).register((spriteAtlasTexture, registry) -> Textures.registerSprites(registry));
        ClientPickBlockGatherCallback.EVENT.register(BootstrapClient::handlePickBlock);

        // Register entity renderers
        EntityRendererRegistry.INSTANCE.register(Entities.INFRARED_PACKET, (dispatcher, context) -> new InvisibleEntityRenderer<InfraredPacketEntity>(dispatcher));

        // Set up block entity renderer for dynamic module content.
        BlockEntityRendererRegistry.INSTANCE.register(CasingBlockEntity.TYPE, CasingBlockEntityRenderer::new);
        BlockEntityRendererRegistry.INSTANCE.register(ControllerBlockEntity.TYPE, ControllerBlockEntityRenderer::new);

        // Add default manual providers for client side stuff.
        final ManualClientAPI manualAPI = API.manual;
        manualAPI.addProvider(new ResourceContentProvider(API.MOD_ID, "doc/"));
        manualAPI.addProvider(SerialAPIImpl.INSTANCE.getSerialProtocolContentProvider());
        manualAPI.addProvider("", new TextureImageProvider());
        manualAPI.addProvider("item", new ItemImageProvider());
        manualAPI.addProvider("block", new BlockImageProvider());
        manualAPI.addProvider("tag", new TagImageProvider());

        manualAPI.addTab(new TextureTabIconRenderer(new Identifier(API.MOD_ID, "textures/gui/manual_home.png")), "tis3d.manual.home", "%LANGUAGE%/index.md");
        manualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(Blocks.CONTROLLER)), "tis3d.manual.blocks", "%LANGUAGE%/block/index.md");
        manualAPI.addTab(new ItemStackTabIconRenderer(new ItemStack(findModuleItem())), "tis3d.manual.items", "%LANGUAGE%/item/index.md");
        manualAPI.addTab(new TextureTabIconRenderer(new Identifier(API.MOD_ID, "textures/gui/manual_serial_protocols.png")), "tis3d.manual.serial_protocols", "%LANGUAGE%/serial_protocols.md");

        // Initialize extensions.
        final String fullEntrypoint = BootstrapCommon.extensionEntrypoint("client");
        final List<ClientExtInitializer> extensions = FabricLoader.getInstance().getEntrypoints(fullEntrypoint, ClientExtInitializer.class);
        for (ClientExtInitializer initializer : extensions) {
            initializer.onInitializeClient(clientAPI);
        }
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

    private static ItemStack handlePickBlock(final PlayerEntity player, final HitResult hitResult) {
        if (hitResult.getType() != HitResult.Type.BLOCK) {
            return ItemStack.EMPTY;
        }

        assert hitResult instanceof BlockHitResult;
        final BlockHitResult blockHitResult = (BlockHitResult)hitResult;
        final BlockPos blockPos = blockHitResult.getBlockPos();
        final BlockState blockState = player.getEntityWorld().getBlockState(blockPos);
        final Block block = blockState.getBlock();
        if (block instanceof CasingBlock) {
            return (((CasingBlock)block).getPickStack(player.getEntityWorld(), blockPos, blockHitResult.getSide(), blockState));
        }

        return ItemStack.EMPTY;
    }
}
