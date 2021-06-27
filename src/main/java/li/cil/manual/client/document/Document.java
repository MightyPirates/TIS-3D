package li.cil.manual.client.document;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.client.document.segment.*;
import li.cil.tis3d.api.API;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Primitive Markdown parser, only supports a very small subset. Used for
 * parsing documentation into segments, to be displayed in a GUI somewhere.
 * <p>
 * General usage is: parse a string using parse(), render it using render().
 * <p>
 * The parser generates a list of segments, each segment representing a part
 * of the document, with a specific formatting / render type. For example,
 * links are their own segments, a bold section in a link would be its own
 * section and so on.
 * The data structure is essentially a very flat multi-tree, where the segments
 * returned are the leaves, and the roots are the individual lines, represented
 * as text segments.
 * Formatting is done by accumulating formatting information over the parent
 * nodes, up to the root.
 */
@OnlyIn(Dist.CLIENT)
public final class Document {
    /**
     * Parses a plain text document into a list of segments.
     *
     * @param manual   the manual the document belongs to.
     * @param style    the fonts to use when rendering the generated segments.
     * @param document iterator over the lines of the document to parse.
     * @return the first segment of the parsed document.
     */
    public static Segment parse(final Manual manual, final Style style, final Iterable<String> document) {
        // Get top-level list of text segments.
        List<Segment> segments = new ArrayList<>();
        for (final String line : document) {
            segments.add(new TextSegment(manual, style, null, StringUtils.stripEnd(line, null)));
        }

        // Refine text segments into sub-types.
        List<Segment> workSegments = new ArrayList<>();
        for (final PatternMapping type : SEGMENT_TYPES) {
            for (final Segment segment : segments) {
                segment.refine(type.pattern, type.refiner).forEach(workSegments::add);
            }

            // Swap buffers.
            final List<Segment> tmp = segments;
            segments = workSegments;
            workSegments = tmp;
            workSegments.clear();
        }

        for (int i = 0; i < segments.size() - 1; i++) {
            segments.get(i).setNext(segments.get(i + 1));
        }

        return segments.size() > 0 ? segments.get(0) : new TextSegment(manual, style, null, "");
    }

    /**
     * Compute the overall height of a document, e.g. for computation of scroll offsets.
     *
     * @param document the document to compute the height of.
     * @param width    the width available for rendering the document.
     * @return the height of the document.
     */
    public static int height(final Segment document, final int width) {
        int globalY = 0, lineHeight = 0;
        NextSegmentInfo current = new NextSegmentInfo(document);
        while (current.segment != null) {
            final Segment segment = current.segment;
            final int localX = current.absoluteX;
            final int relativeY = current.relativeY;
            final int segmentHeight = segment.getLineHeight(localX, width);

            globalY += relativeY;
            current = segment.getNext(localX, lineHeight, width);

            final boolean isFirstSegmentOnNewLine = current.relativeY > 0;
            if (isFirstSegmentOnNewLine) {
                lineHeight = current.absoluteX > 0 ? segmentHeight : 0;
            } else {
                lineHeight = Math.max(lineHeight, segmentHeight);
            }

            // For the last segment we also want to take into account that elements height.
            // Next sibling may still be allowed to render on same line, so we'd cut off
            // the last line in the height computation.
            if (current.segment == null) {
                if (current.relativeY > 0) {
                    globalY += current.relativeY;
                    if (current.absoluteX > 0) {
                        globalY += segmentHeight;
                    }
                } else {
                    globalY += lineHeight;
                }
            }
        }
        return globalY;
    }

    /**
     * Renders a list of segments and tooltips if a segment with a tooltip is hovered.
     * Returns the hovered interactive segment, if any.
     *
     * @param matrixStack the current matrix stack.
     * @param document    the document to render.
     * @param x           the x position to render at.
     * @param y           the y position to render at.
     * @param width       the width of the area to render the document in.
     * @param height      the height of the area to render the document in.
     * @param scrollY     the vertical scroll offset of the document.
     * @param mouseX      the x position of the mouse.
     * @param mouseY      the y position of the mouse.
     * @return the interactive segment being hovered, if any.
     */
    public static Optional<InteractiveSegment> render(final MatrixStack matrixStack, final Segment document, final int x, final int y, final int width, final int height, final int scrollY, final int mouseX, final int mouseY) {
        final Minecraft mc = Minecraft.getInstance();
        final MainWindow window = mc.getWindow();

        // On some systems/drivers/graphics cards the next calls won't update the
        // depth buffer correctly if alpha test is enabled. Guess how we found out?
        // By noticing that on those systems it only worked while chat messages
        // were visible. Yeah. I know.
        RenderSystem.disableAlphaTest();

        // Clear depth mask, then create masks in foreground above and below scroll area.
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        matrixStack.pushPose();
        matrixStack.translate(0, 0, 500);
        Screen.fill(matrixStack, 0, 0, window.getWidth(), y, 0);
        Screen.fill(matrixStack, 0, y + height, window.getWidth(), window.getHeight(), 0);
        matrixStack.popPose();

        matrixStack.pushPose();
        matrixStack.translate(x, 0, 0);

        // Variables with naming that makes their usage a bit more clear.
        final int visibleLeft = x, visibleRight = x + width;
        final int visibleTop = y, visibleBottom = y + height;

        // Actual rendering.
        Optional<InteractiveSegment> hovered = Optional.empty();
        int globalY = y - scrollY, lineHeight = 0;
        NextSegmentInfo current = new NextSegmentInfo(document);
        while (current.segment != null) {
            final Segment segment = current.segment;
            final int localX = current.absoluteX;
            final int relativeY = current.relativeY;
            final int segmentHeight = segment.getLineHeight(localX, width);

            globalY += relativeY;
            current = segment.getNext(localX, lineHeight, width);

            final int segmentTop = globalY;
            final int segmentBottom = segmentTop + Math.max(current.relativeY, Math.max(lineHeight, segmentHeight));

            if (segmentBottom >= visibleTop && segmentTop <= visibleBottom) {
                final int localMouseX = mouseX - x;
                final int localMouseY = mouseY - globalY;

                matrixStack.pushPose();
                matrixStack.translate(0, globalY, 0);

                final Optional<InteractiveSegment> result = segment.render(matrixStack, localX, lineHeight, width, localMouseX, localMouseY);

                matrixStack.popPose();

                if (!hovered.isPresent()) {
                    hovered = result;
                }
            }

            final boolean isFirstSegmentOnNewLine = current.relativeY > 0;
            if (isFirstSegmentOnNewLine) {
                lineHeight = current.absoluteX > 0 ? segmentHeight : 0;
            } else {
                lineHeight = Math.max(lineHeight, segmentHeight);
            }
        }

        matrixStack.popPose();

        // Suppress tooltips that are outside the visible area.
        if (mouseX < visibleLeft || mouseX > visibleRight ||
            mouseY < visibleTop || mouseY > visibleBottom) {
            hovered = Optional.empty();
        }

        hovered.ifPresent(InteractiveSegment::mouseHovered);

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        return hovered;
    }

    // ----------------------------------------------------------------------- //

    private static Segment HeaderSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        return new HeaderSegment(m, f, s, t.group(2), t.group(1).length());
    }

    private static Segment CodeSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        return new MonospaceSegment(m, f, s, t.group(2));
    }

    private static Segment LinkSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        return new LinkSegment(m, f, s, t.group(1), t.group(2));
    }

    private static Segment BoldSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        return new BoldSegment(m, f, s, t.group(2));
    }

    private static Segment ItalicSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        return new ItalicSegment(m, f, s, t.group(2));
    }

    private static Segment StrikethroughSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        return new StrikethroughSegment(m, f, s, t.group(1));
    }

    private static Segment ImageSegment(final Manual m, final Style f, final Segment s, final Matcher t) {
        final String title = t.group(1);
        final String url = t.group(2);
        final Optional<ContentRenderer> renderer = m.imageFor(url);
        if (renderer.isPresent()) {
            return new RenderSegment(m, f, s, new StringTextComponent(title), renderer.get());
        } else {
            return new TextSegment(m, f, s, I18n.get(API.MOD_ID + ".manual.warning.missing.content_render", url));
        }
    }

    private static final PatternMapping[] SEGMENT_TYPES = new PatternMapping[]{
        new PatternMapping("^(#+)\\s(.*)", Document::HeaderSegment), // headers: # ...
        new PatternMapping("(`)(.*?)\\1", Document::CodeSegment), // code: `...`
        new PatternMapping("!\\[([^\\[]*)\\]\\(([^\\)]+)\\)", Document::ImageSegment), // images: ![...](...)
        new PatternMapping("\\[([^\\[]+)\\]\\(([^\\)]+)\\)", Document::LinkSegment), // links: [...](...)
        new PatternMapping("(\\*\\*|__)(\\S.*?\\S|$)\\1", Document::BoldSegment), // bold: **...** | __...__
        new PatternMapping("(\\*|_)(\\S.*?\\S|$)\\1", Document::ItalicSegment), // italic: *...* | _..._
        new PatternMapping("~~(\\S.*?\\S|$)~~", Document::StrikethroughSegment) // strikethrough: ~~...~~
    };

    private static final class PatternMapping {
        final Pattern pattern;
        final SegmentRefiner refiner;

        PatternMapping(final String pattern, final SegmentRefiner refiner) {
            this.pattern = Pattern.compile(pattern);
            this.refiner = refiner;
        }
    }

    // ----------------------------------------------------------------------- //

    private Document() {
    }
}
