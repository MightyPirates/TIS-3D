/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.neoforge;

import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

public final class ModuleModelLoader {
    public static void register(final RegisterBlockStateModels event) {
        event.registerModel(ModuleUnbakedModel.ID, ModuleUnbakedModel.CODEC);
    }

    private ModuleModelLoader() {
    }
}
