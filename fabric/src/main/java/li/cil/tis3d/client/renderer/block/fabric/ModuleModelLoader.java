/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.fabric;

import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;

public final class ModuleModelLoader {
    public static void initialize() {
        CustomUnbakedBlockStateModel.register(ModuleUnbakedModel.ID, ModuleUnbakedModel.CODEC);
    }

    private ModuleModelLoader() {
    }
}
