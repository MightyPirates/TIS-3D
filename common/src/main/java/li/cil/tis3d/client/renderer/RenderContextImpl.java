package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Vector3f;

public final class RenderContextImpl implements RenderContext {
    private static final int DETAIL_RENDER_RANGE = 8;

    private final BlockEntityRenderDispatcher dispatcher;
    private final PoseStack matrixStack;
    private final MultiBufferSource buffer;
    private final float partialTicks;
    private final int light;
    private final int overlay;

    // --------------------------------------------------------------------- //

    public RenderContextImpl(final BlockEntityRenderDispatcher dispatcher, final PoseStack matrixStack,
                             final MultiBufferSource buffer, final float partialTicks,
                             final int light, final int overlay) {
        this.dispatcher = dispatcher;
        this.matrixStack = matrixStack;
        this.buffer = buffer;
        this.partialTicks = partialTicks;
        this.light = light;
        this.overlay = overlay;
    }

    public RenderContextImpl(final RenderContextImpl other, final int light) {
        this(other.dispatcher, other.matrixStack, other.buffer, other.partialTicks, light, other.overlay);
    }

    // --------------------------------------------------------------------- //

    @Override
    public BlockEntityRenderDispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    public PoseStack getMatrixStack() {
        return matrixStack;
    }

    @Override
    public float getPartialTicks() {
        return partialTicks;
    }

    @Override
    public MultiBufferSource getBuffer() {
        return buffer;
    }

    @Override
    public boolean closeEnoughForDetails(final BlockPos position) {
        return position.closerToCenterThan(dispatcher.camera.getPosition(), (float) DETAIL_RENDER_RANGE);
    }

    @Override
    public void drawString(final FontRenderer fontRenderer, final CharSequence value, final int argb) {
        fontRenderer.drawBatch(matrixStack, buffer, value, argb);
    }

    @Override
    public void drawAtlasQuadLit(final ResourceLocation location) {
        final VertexConsumer builder = buffer.getBuffer(RenderType.translucentNoCrumbling());
        drawAtlasQuad(builder, getSprite(location), 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
    }

    @Override
    public void drawAtlasQuadUnlit(final ResourceLocation location) {
        drawAtlasQuadUnlit(location, 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
    }

    @Override
    public void drawAtlasQuadUnlit(final ResourceLocation location,
                                   final float x, final float y, final float width, final float height,
                                   final float u0, final float v0, final float u1, final float v1,
                                   final int argb) {
        final VertexConsumer builder = buffer.getBuffer(ModRenderType.unlitAtlasTexture());
        drawAtlasQuad(builder, getSprite(location), x, y, width, height, u0, v0, u1, v1, argb);
    }

    @Override
    public void drawQuadUnlit(final float x, final float y, final float width, final float height, final int argb) {
        final VertexConsumer builder = buffer.getBuffer(ModRenderType.unlit());
        drawQuad(builder, x, y, width, height, 0, 0, 1, 1, argb);
    }

    @Override
    public void drawQuad(final VertexConsumer builder, final float x, final float y, final float width, final float height) {
        drawQuad(builder, x, y, width, height, Color.WHITE);
    }

    @Override
    public void drawQuad(final VertexConsumer builder,
                         final float x, final float y, final float width, final float height,
                         final float u0, final float v0, final float u1, final float v1,
                         final int argb) {
        final var pose = getMatrixStack().last().pose();
        final var normal = getMatrixStack().last().normal();
        final var up = new Vector3f(0, 0, -1);

        final int a = Color.getAlphaU8(argb);
        final int r = Color.getRedU8(argb);
        final int g = Color.getGreenU8(argb);
        final int b = Color.getBlueU8(argb);

        builder.vertex(pose, x, y + height, 0)
            .color(r, g, b, a)
            .uv(u0, v1)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(normal, up.x(), up.y(), up.z())
            .endVertex();

        builder.vertex(pose, x + width, y + height, 0)
            .color(r, g, b, a)
            .uv(u1, v1)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(normal, up.x(), up.y(), up.z())
            .endVertex();

        builder.vertex(pose, x + width, y, 0)
            .color(r, g, b, a)
            .uv(u1, v0)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(normal, up.x(), up.y(), up.z())
            .endVertex();

        builder.vertex(pose, x, y, 0)
            .color(r, g, b, a)
            .uv(u0, v0)
            .overlayCoords(overlay)
            .uv2(light)
            .normal(normal, up.x(), up.y(), up.z())
            .endVertex();
    }

    // --------------------------------------------------------------------- //

    private static TextureAtlasSprite getSprite(final ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }
}
