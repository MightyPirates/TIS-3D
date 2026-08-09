package li.cil.tis3d.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;

public final class SubmitBufferSource implements MultiBufferSource {
    private final List<Segment> segments = new ArrayList<>();

    private record Segment(RenderType renderType, Recorder recorder) {
    }

    @Override
    public VertexConsumer getBuffer(final RenderType renderType) {
        if (!segments.isEmpty()) {
            final Segment last = segments.get(segments.size() - 1);
            if (last.renderType().equals(renderType)) {
                return last.recorder();
            }
        }
        final Segment segment = new Segment(renderType, new Recorder());
        segments.add(segment);
        return segment.recorder();
    }

    public void submitTo(final SubmitNodeCollector collector, final PoseStack poseStack) {
        for (int i = 0; i < segments.size(); i++) {
            final Segment segment = segments.get(i);
            if (segment.recorder().vertexCount == 0) {
                continue;
            }
            final Recorder replay = segment.recorder().copy();
            collector.order(i).submitCustomGeometry(poseStack, segment.renderType(),
                (pose, consumer) -> replay.replay(consumer));
        }
        segments.clear();
    }

    // --------------------------------------------------------------------- //

    private static final int HAS_COLOR = 1;
    private static final int HAS_UV = 1 << 1;
    private static final int HAS_UV1 = 1 << 2;
    private static final int HAS_UV2 = 1 << 3;
    private static final int HAS_NORMAL = 1 << 4;

    private static final class Recorder implements VertexConsumer {
        private float[] positions = new float[3 * 16];
        private int[] colors = new int[16];
        private float[] uvs = new float[2 * 16];
        private int[] uv1s = new int[2 * 16];
        private int[] uv2s = new int[2 * 16];
        private float[] normals = new float[3 * 16];
        private int[] masks = new int[16];
        private int vertexCount;

        private Recorder copy() {
            final Recorder other = new Recorder();
            other.positions = positions.clone();
            other.colors = colors.clone();
            other.uvs = uvs.clone();
            other.uv1s = uv1s.clone();
            other.uv2s = uv2s.clone();
            other.normals = normals.clone();
            other.masks = masks.clone();
            other.vertexCount = vertexCount;
            return other;
        }

        private void replay(final VertexConsumer consumer) {
            for (int i = 0; i < vertexCount; i++) {
                consumer.addVertex(positions[i * 3], positions[i * 3 + 1], positions[i * 3 + 2]);
                final int mask = masks[i];
                if ((mask & HAS_COLOR) != 0) {
                    consumer.setColor(colors[i]);
                }
                if ((mask & HAS_UV) != 0) {
                    consumer.setUv(uvs[i * 2], uvs[i * 2 + 1]);
                }
                if ((mask & HAS_UV1) != 0) {
                    consumer.setUv1(uv1s[i * 2], uv1s[i * 2 + 1]);
                }
                if ((mask & HAS_UV2) != 0) {
                    consumer.setUv2(uv2s[i * 2], uv2s[i * 2 + 1]);
                }
                if ((mask & HAS_NORMAL) != 0) {
                    consumer.setNormal(normals[i * 3], normals[i * 3 + 1], normals[i * 3 + 2]);
                }
            }
        }

        private void grow() {
            if (vertexCount < masks.length) {
                return;
            }
            final int capacity = masks.length * 2;
            positions = java.util.Arrays.copyOf(positions, capacity * 3);
            colors = java.util.Arrays.copyOf(colors, capacity);
            uvs = java.util.Arrays.copyOf(uvs, capacity * 2);
            uv1s = java.util.Arrays.copyOf(uv1s, capacity * 2);
            uv2s = java.util.Arrays.copyOf(uv2s, capacity * 2);
            normals = java.util.Arrays.copyOf(normals, capacity * 3);
            masks = java.util.Arrays.copyOf(masks, capacity);
        }

        @Override
        public VertexConsumer addVertex(final float x, final float y, final float z) {
            grow();
            positions[vertexCount * 3] = x;
            positions[vertexCount * 3 + 1] = y;
            positions[vertexCount * 3 + 2] = z;
            masks[vertexCount] = 0;
            vertexCount++;
            return this;
        }

        @Override
        public VertexConsumer setColor(final int red, final int green, final int blue, final int alpha) {
            return setColor(alpha << 24 | red << 16 | green << 8 | blue);
        }

        @Override
        public VertexConsumer setColor(final int argb) {
            final int index = vertexCount - 1;
            colors[index] = argb;
            masks[index] |= HAS_COLOR;
            return this;
        }

        @Override
        public VertexConsumer setUv(final float u, final float v) {
            final int index = vertexCount - 1;
            uvs[index * 2] = u;
            uvs[index * 2 + 1] = v;
            masks[index] |= HAS_UV;
            return this;
        }

        @Override
        public VertexConsumer setUv1(final int u, final int v) {
            final int index = vertexCount - 1;
            uv1s[index * 2] = u;
            uv1s[index * 2 + 1] = v;
            masks[index] |= HAS_UV1;
            return this;
        }

        @Override
        public VertexConsumer setUv2(final int u, final int v) {
            final int index = vertexCount - 1;
            uv2s[index * 2] = u;
            uv2s[index * 2 + 1] = v;
            masks[index] |= HAS_UV2;
            return this;
        }

        @Override
        public VertexConsumer setNormal(final float x, final float y, final float z) {
            final int index = vertexCount - 1;
            normals[index * 3] = x;
            normals[index * 3 + 1] = y;
            normals[index * 3 + 2] = z;
            masks[index] |= HAS_NORMAL;
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(final float width) {
            return this;
        }
    }
}
