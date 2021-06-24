package li.cil.tis3d.api.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import li.cil.tis3d.client.renderer.RenderLayerAccess;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RenderContext {
    private static final int DETAIL_RENDER_RANGE = 8;

    private final TileEntityRendererDispatcher dispatcher;
    private final MatrixStack matrixStack;
    public final IRenderTypeBuffer bufferFactory;
    private final float partialTicks;
    public final int light;
    public final int overlay;

    // --------------------------------------------------------------------- //

    public RenderContext(final TileEntityRendererDispatcher dispatcher, final MatrixStack matrixStack,
                         final IRenderTypeBuffer bufferFactory, final float partialTicks,
                         final int light, final int overlay) {
        this.dispatcher = dispatcher;
        this.matrixStack = matrixStack;
        this.bufferFactory = bufferFactory;
        this.partialTicks = partialTicks;
        this.light = light;
        this.overlay = overlay;
    }

    public RenderContext(final RenderContext other, final int light) {
        this(other.dispatcher, other.matrixStack, other.bufferFactory, other.partialTicks, light, other.overlay);
    }

    // --------------------------------------------------------------------- //

    public TileEntityRendererDispatcher getDispatcher() {
        return dispatcher;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public IVertexBuilder getBuffer(final RenderType layer) {
        return bufferFactory.getBuffer(layer);
    }

    public boolean isWithinRange(final BlockPos position, final float range) {
        return position.withinDistance(dispatcher.renderInfo.getProjectedView(), range);
    }

    public boolean isWithinDetailRange(final BlockPos position) {
        return isWithinRange(position, DETAIL_RENDER_RANGE);
    }

    public void drawAtlasSpriteLit(final ResourceLocation location) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderType.getTranslucentNoCrumbling());
        RenderUtil.drawQuad(this, buffer, sprite, 0, 0, 1, 1, 0, 0, 1, 1);
    }

    public void drawAtlasSpriteUnlit(final ResourceLocation location) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        RenderUtil.drawQuad(this, buffer, sprite, 0, 0, 1, 1, 0, 0, 1, 1);
    }

    public void drawAtlasSpriteUnlit(final ResourceLocation location, final int argb) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        final float u0 = sprite.getInterpolatedU(0);
        final float v0 = sprite.getInterpolatedV(0);
        final float u1 = sprite.getInterpolatedU(16);
        final float v1 = sprite.getInterpolatedV(16);
        RenderUtil.drawQuad(this, buffer, 0, 0, 1, 1, u0, v0, u1, v1, argb);
    }

    public void drawQuadUnlit(final IVertexBuilder buffer, final float x, final float y, final float width, final float height, final int argb) {
        RenderUtil.drawQuad(this, buffer, x, y, width, height, 0, 0, 1, 1, argb);
    }

    public void drawQuadUnlit(final IVertexBuilder buffer, final float x, final float y, final float width, final float height) {
        drawQuadUnlit(buffer, x, y, width, height, Color.WHITE);
    }

    public void drawQuadUnlit(final float x, final float y, final float width, final float height, final int argb) {
        final TextureAtlasSprite sprite = getSprite(Textures.LOCATION_WHITE);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        final float u0 = sprite.getInterpolatedU(0);
        final float v0 = sprite.getInterpolatedV(0);
        final float u1 = sprite.getInterpolatedU(16);
        final float v1 = sprite.getInterpolatedV(16);
        RenderUtil.drawQuad(this, buffer, x, y, width, height, u0, v0, u1, v1, argb);
    }

    public void drawAtlasSpriteUnlit(final ResourceLocation location, final float x, final float y, final float width, final float height) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        RenderUtil.drawQuad(this, buffer, sprite, x, y, width, height, x, y, x + width, y + height);
    }

    // --------------------------------------------------------------------- //

    private static TextureAtlasSprite getSprite(final ResourceLocation location) {
        return Minecraft.getInstance().getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE).apply(location);
    }
}
