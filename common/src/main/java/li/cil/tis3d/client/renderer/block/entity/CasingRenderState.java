/* SPDX-License-Identifier: MIT */

package li.cil.tis3d.client.renderer.block.entity;

import li.cil.tis3d.common.block.entity.CasingBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class CasingRenderState extends BlockEntityRenderState {
    @Nullable
    public CasingBlockEntity casing;

    public final int[] neighborLight = new int[Direction.values().length];
    public Vec3 cameraPosition = Vec3.ZERO;
    @Nullable
    public HitResult cameraHitResult;
    public boolean observerHoldingKey;
    public boolean observerSneaking;
    public float partialTicks;
}
