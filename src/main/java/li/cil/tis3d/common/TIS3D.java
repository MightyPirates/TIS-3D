package li.cil.tis3d.common;

import li.cil.manual.client.Bootstrap;
import li.cil.tis3d.api.API;
import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.client.ClientSetup;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.container.Containers;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.provider.ModuleProviders;
import li.cil.tis3d.common.provider.RedstoneInputProviders;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import li.cil.tis3d.common.tags.BlockTags;
import li.cil.tis3d.common.tags.ItemTags;
import li.cil.tis3d.common.tileentity.TileEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(API.MOD_ID)
public final class TIS3D {
    public TIS3D() {
        ConfigManager.add(CommonConfig::new);
        ConfigManager.add(ClientConfig::new);
        ConfigManager.initialize();

        ItemTags.initialize();
        BlockTags.initialize();
        Blocks.initialize();
        Items.initialize();
        TileEntities.initialize();
        Entities.initialize();
        Containers.initialize();

        ModuleProviders.initialize();
        SerialInterfaceProviders.initialize();
        RedstoneInputProviders.initialize();
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            Bootstrap.initialize();
            Manuals.initialize();
        });

        FMLJavaModLoadingContext.get().getModEventBus().register(CommonSetup.class);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> FMLJavaModLoadingContext.get().getModEventBus().register(ClientSetup.class));
    }
}
