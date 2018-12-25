package li.cil.tis3d.client.init;

import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererCasing;
import li.cil.tis3d.client.renderer.tileentity.TileEntitySpecialRendererController;
import li.cil.tis3d.common.block.entity.TileEntityCasing;
import li.cil.tis3d.common.block.entity.TileEntityController;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.client.render.BlockEntityRendererRegistry;

@SuppressWarnings("unused")
public class Renderers implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.INSTANCE.register(TileEntityCasing.class, new TileEntitySpecialRendererCasing());
        BlockEntityRendererRegistry.INSTANCE.register(TileEntityController.class, new TileEntitySpecialRendererController());
    }
}
