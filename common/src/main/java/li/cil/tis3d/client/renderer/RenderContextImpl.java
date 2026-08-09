package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import li.cil.manual.api.render.FontRenderer;
import li.cil.tis3d.api.util.RenderContext;
import li.cil.tis3d.util.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class RenderContextImpl implements RenderContext {
    private static final int DETAIL_RENDER_RANGE = 8;

    private final PoseStack matrixStack;
    private final MultiBufferSource buffer;
    private final Vec3 cameraPosition;
    private final HitResult cameraHitResult;
    private final float partialTicks;
    private final int light;
    private final int overlay;

    // --------------------------------------------------------------------- //

    public RenderContextImpl(final PoseStack matrixStack, final MultiBufferSource buffer,
                             final Vec3 cameraPosition, final HitResult cameraHitResult,
                             final float partialTicks, final int light, final int overlay) {
        this.matrixStack = matrixStack;
        this.buffer = buffer;
        this.cameraPosition = cameraPosition;
        this.cameraHitResult = cameraHitResult;
        this.partialTicks = partialTicks;
        this.light = light;
        this.overlay = overlay;
    }

    public RenderContextImpl(final RenderContextImpl other, final int light) {
        this(other.matrixStack, other.buffer, other.cameraPosition, other.cameraHitResult,
            other.partialTicks, light, other.overlay);
    }

    // --------------------------------------------------------------------- //

    @Override
    public HitResult getCameraHitResult() {
        return cameraHitResult;
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
        return position.closerToCenterThan(cameraPosition, DETAIL_RENDER_RANGE);
    }

    @Override
    public void drawString(final FontRenderer fontRenderer, final CharSequence value, final int argb) {
        fontRenderer.drawInBatch(value, argb, matrixStack.last().pose(), buffer);
    }

    @Override
    public void drawAtlasQuadLit(final Identifier location) {
        final VertexConsumer builder = buffer.getBuffer(RenderTypes.entityTranslucent(Textures.LOCATION_BLOCK_ATLAS));
        drawAtlasQuad(builder, getSprite(location), 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
    }

    @Override
    public void drawAtlasQuadUnlit(final Identifier location) {
        drawAtlasQuadUnlit(location, 0, 0, 1, 1, 0, 0, 1, 1, Color.WHITE);
    }

    @Override
    public void drawAtlasQuadUnlit(final Identifier location,
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
        final var last = getMatrixStack().last();
        final var pose = last.pose();
        final var up = new Vector3f(0, 0, -1);

        addVertex(builder, pose, last, up, x, y + height, u0, v1, argb);
        addVertex(builder, pose, last, up, x + width, y + height, u1, v1, argb);
        addVertex(builder, pose, last, up, x + width, y, u1, v0, argb);
        addVertex(builder, pose, last, up, x, y, u0, v0, argb);
    }

    // --------------------------------------------------------------------- //

    private void addVertex(final VertexConsumer builder, final Matrix4f pose, final PoseStack.Pose last,
                           final Vector3f up, final float x, final float y,
                           final float u, final float v, final int argb) {
        builder.addVertex(pose, x, y, 0)
            .setColor(argb)
            .setUv(u, v)
            .setOverlay(overlay)
            .setLight(light)
            .setNormal(last, up.x(), up.y(), up.z());
    }

    // --------------------------------------------------------------------- //

    private static TextureAtlasSprite getSprite(final Identifier location) {
        return Minecraft.getInstance().getAtlasManager().get(new Material(Textures.LOCATION_BLOCK_ATLAS, location));
    }
}
