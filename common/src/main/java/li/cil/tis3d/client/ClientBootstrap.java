/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client;

import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import li.cil.tis3d.client.renderer.color.CasingBlockColor;
import li.cil.tis3d.common.block.Blocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ClientBootstrap {
    public static void run() {
        ColorHandlerRegistry.registerBlockColors(new CasingBlockColor(), Blocks.CASING);
    }
}
