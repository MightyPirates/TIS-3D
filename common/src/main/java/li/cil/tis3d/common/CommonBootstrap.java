package li.cil.tis3d.common;

import li.cil.tis3d.api.API;
import li.cil.tis3d.client.ClientConfig;
import li.cil.tis3d.common.block.Blocks;
import li.cil.tis3d.common.block.entity.BlockEntities;
import li.cil.tis3d.common.config.CommonConfig;
import li.cil.tis3d.common.container.Containers;
import li.cil.tis3d.common.entity.Entities;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.provider.ModuleProviders;
import li.cil.tis3d.common.provider.RedstoneInputProviders;
import li.cil.tis3d.common.provider.SerialInterfaceProviders;
import li.cil.tis3d.common.tags.BlockTags;
import li.cil.tis3d.common.tags.ItemTags;
import li.cil.tis3d.util.ConfigManager;
import li.cil.tis3d.util.RegistryUtils;

public final class CommonBootstrap {
    public static void run() {
        ConfigManager.add(CommonConfig::new);
        ConfigManager.add(ClientConfig::new);
        ConfigManager.initialize();

        RegistryUtils.initialize(API.MOD_ID);

        ItemTags.initialize();
        BlockTags.initialize();
        Blocks.initialize();
        Items.initialize();
        BlockEntities.initialize();
        Entities.initialize();
        Containers.initialize();

        ModuleProviders.initialize();
        SerialInterfaceProviders.initialize();
        RedstoneInputProviders.initialize();
    }
}
