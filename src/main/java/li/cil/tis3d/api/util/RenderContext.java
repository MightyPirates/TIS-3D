package li.cil.tis3d.api.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import li.cil.tis3d.client.renderer.RenderLayerAccess;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.font.FontRenderer;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class RenderContext {
    private static final int DETAIL_RENDER_RANGE = 8;

    private final TileEntityRendererDispatcher dispatcher;
    private final MatrixStack matrixStack;
    private final IRenderTypeBuffer bufferFactory;
    private final float partialTicks;
    private final int light;
    private final int overlay;

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

    public boolean closeEnoughForDetails(final BlockPos position) {
        return position.closerThan(dispatcher.camera.getPosition(), (float) DETAIL_RENDER_RANGE);
    }

    public void drawString(final FontRenderer fontRenderer, final CharSequence value) {
        fontRenderer.drawString(matrixStack, bufferFactory, value);
    }

    public void drawString(final FontRenderer fontRenderer, final CharSequence value, final int argb, final int maxChars) {
        fontRenderer.drawString(matrixStack, bufferFactory, value, argb, maxChars);
    }

    public void drawAtlasSpriteLit(final ResourceLocation location) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderType.translucentNoCrumbling());
        drawQuad(buffer, sprite, 0, 0, 1, 1, 0, 0, 1, 1);
    }

    public void drawAtlasSpriteUnlit(final ResourceLocation location) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        drawQuad(buffer, sprite, 0, 0, 1, 1, 0, 0, 1, 1);
    }

    public void drawAtlasSpriteUnlit(final ResourceLocation location, final int argb) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        final float u0 = sprite.getU(0);
        final float v0 = sprite.getV(0);
        final float u1 = sprite.getU(16);
        final float v1 = sprite.getV(16);
        drawQuad(buffer, 0, 0, 1, 1, u0, v0, u1, v1, argb);
    }

    public void drawQuadUnlit(final IVertexBuilder buffer, final float x, final float y, final float width, final float height, final int argb) {
        drawQuad(buffer, x, y, width, height, 0, 0, 1, 1, argb);
    }

    public void drawQuadUnlit(final IVertexBuilder buffer, final float x, final float y, final float width, final float height) {
        drawQuadUnlit(buffer, x, y, width, height, Color.WHITE);
    }

    public void drawQuadUnlit(final float x, final float y, final float width, final float height, final int argb) {
        final TextureAtlasSprite sprite = getSprite(Textures.LOCATION_WHITE);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        final float u0 = sprite.getU(0);
        final float v0 = sprite.getV(0);
        final float u1 = sprite.getU(16);
        final float v1 = sprite.getV(16);
        drawQuad(buffer, x, y, width, height, u0, v0, u1, v1, argb);
    }

    public void drawAtlasSpriteUnlit(final ResourceLocation location, final float x, final float y, final float width, final float height) {
        final TextureAtlasSprite sprite = getSprite(location);
        final IVertexBuilder buffer = getBuffer(RenderLayerAccess.getModuleOverlay());
        drawQuad(buffer, sprite, x, y, width, height, x, y, x + width, y + height);
    }

    private void drawQuad(final IVertexBuilder buffer,
                          final float x, final float y, final float w, final float h,
                          final float u0, final float v0, final float u1, final float v1,
                          final int argb) {
        final Matrix4f modMat = getMatrixStack().last().pose();
        final Matrix3f normMat = getMatrixStack().last().normal();
        final Vector3f normDir = new Vector3f(0, 0, -1);

        final int a = Color.getAlphaU8(argb);
        final int r = Color.getRedU8(argb);
        final int g = Color.getGreenU8(argb);
        final int b = Color.getBlueU8(argb);

        buffer.vertex(modMat, x, y + h, 0).color(r, g, b, a).uv(u0, v1)
            .overlayCoords(overlay).uv2(light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();

        buffer.vertex(modMat, x + w, y + h, 0).color(r, g, b, a).uv(u1, v1)
            .overlayCoords(overlay).uv2(light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();

        buffer.vertex(modMat, x + w, y, 0).color(r, g, b, a).uv(u1, v0)
            .overlayCoords(overlay).uv2(light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();

        buffer.vertex(modMat, x, y, 0).color(r, g, b, a).uv(u0, v0)
            .overlayCoords(overlay).uv2(light)
            .normal(normMat, normDir.x(), normDir.y(), normDir.z())
            .endVertex();
    }

    private void drawQuad(final IVertexBuilder buffer, final TextureAtlasSprite sprite,
                          final float x, final float y, final float w, final float h,
                          final float u0, final float v0, final float u1, final float v1) {
        drawQuad(buffer,
            x, y, w, h,
            sprite.getU(u0 * 16), sprite.getV(v0 * 16),
            sprite.getU(u1 * 16), sprite.getV(v1 * 16),
            Color.WHITE);
    }

    // --------------------------------------------------------------------- //

    private static TextureAtlasSprite getSprite(final ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(PlayerContainer.BLOCK_ATLAS).apply(location);
    }
}
