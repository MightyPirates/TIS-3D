package li.cil.tis3d.client.renderer.block.entity;

import li.cil.tis3d.common.block.entity.ControllerBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

import javax.annotation.Nullable;

public final class ControllerRenderState extends BlockEntityRenderState {
    @Nullable
    public ControllerBlockEntity.ControllerState controllerState;
    public boolean isObserverLookingAt;
    public double distanceToCameraSq;
}
