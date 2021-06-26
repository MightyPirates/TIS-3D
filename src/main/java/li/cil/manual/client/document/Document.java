package li.cil.manual.client.document;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.manual.api.ContentRenderer;
import li.cil.manual.api.Manual;
import li.cil.manual.client.document.segment.*;
import li.cil.tis3d.api.API;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
     * @param document iterator over the lines of the document to parse.
     * @return the first segment of the parsed document.
     */
    public static Segment parse(final Manual manual, final Iterable<String> document) {
        // Get top-level list of text segments.
        List<Segment> segments = new ArrayList<>();
        for (final String line : document) {
            segments.add(new TextSegment(manual, null, trimRight(line)));
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

        return segments.size() > 0 ? segments.get(0) : new TextSegment(manual, null, "");
    }

    /**
     * Compute the overall height of a document, e.g. for computation of scroll offsets.
     *
     * @param document the document to compute the height of.
     * @param maxWidth the maximum height available.
     * @param renderer the font renderer used.
     * @return the height of the document.
     */
    public static int height(final Segment document, final int maxWidth, final FontRenderer renderer) {
        int currentX = 0;
        int currentY = 0;
        Segment segment = document;
        while (segment != null) {
            currentY += segment.nextY(currentX, maxWidth, renderer);
            currentX = segment.nextX(currentX, maxWidth, renderer);
            segment = segment.next();
        }
        return currentY;
    }

    /**
     * Line height for a normal line of text.
     *
     * @param renderer the font renderer used.
     * @return the height of a single line.
     */
    public static int lineHeight(final FontRenderer renderer) {
        return renderer.lineHeight + 1;
    }

    /**
     * Renders a list of segments and tooltips if a segment with a tooltip is hovered.
     * Returns the hovered interactive segment, if any.
     *
     * @param matrixStack the current matrix stack.
     * @param document    the document to render.
     * @param x           the x position to render at.
     * @param y           the y position to render at.
     * @param maxWidth    the width of the area to render the document in.
     * @param maxHeight   the height of the area to render the document in.
     * @param yOffset     the vertical scroll offset of the document.
     * @param renderer    the font renderer to use.
     * @param mouseX      the x position of the mouse.
     * @param mouseY      the y position of the mouse.
     * @return the interactive segment being hovered, if any.
     */
    public static Optional<InteractiveSegment> render(final MatrixStack matrixStack, final Segment document, final int x, final int y, final int maxWidth, final int maxHeight, final int yOffset, final FontRenderer renderer, final int mouseX, final int mouseY) {
        final Minecraft mc = Minecraft.getInstance();
        final MainWindow window = mc.getWindow();

        // On some systems/drivers/graphics cards the next calls won't update the
        // depth buffer correctly if alpha test is enabled. Guess how we found out?
        // By noticing that on those systems it only worked while chat messages
        // were visible. Yeah. I know.
        RenderSystem.disableAlphaTest();

        // Clear depth mask, then create masks in foreground above and below scroll area.
        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        matrixStack.pushPose();
        matrixStack.translate(0, 0, 500);
        Screen.fill(matrixStack, 0, 0, window.getWidth(), y, 0);
        Screen.fill(matrixStack, 0, y + maxHeight, window.getWidth(), window.getHeight(), 0);
        matrixStack.popPose();

        // Actual rendering.
        Optional<InteractiveSegment> hovered = Optional.empty();
        int indent = 0;
        int currentY = y - yOffset;
        final int minY = y - lineHeight(renderer);
        final int maxY = y + maxHeight + lineHeight(renderer);
        Segment segment = document;
        while (segment != null) {
            final int segmentHeight = segment.nextY(indent, maxWidth, renderer);
            if (currentY + segmentHeight >= minY && currentY <= maxY) {
                final Optional<InteractiveSegment> result = segment.render(matrixStack, x, currentY, indent, maxWidth, renderer, mouseX, mouseY);
                if (!hovered.isPresent()) {
                    hovered = result;
                }
            }
            currentY += segmentHeight;
            indent = segment.nextX(indent, maxWidth, renderer);
            segment = segment.next();
        }
        if (mouseX < x || mouseX > x + maxWidth || mouseY < y || mouseY > y + maxHeight) {
            hovered = Optional.empty();
        }
        hovered.ifPresent(InteractiveSegment::notifyHover);

        GlStateManager._clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        return hovered;
    }

    // ----------------------------------------------------------------------- //

    private static String trimRight(final String s) {
        for (int i = s.length(); i > 0; i--) {
            if (!Character.isWhitespace(s.charAt(i - 1))) {
                return s.substring(0, i);
            }
        }
        return s;
    }

    // ----------------------------------------------------------------------- //

    private static Segment HeaderSegment(final Manual m, final Segment s, final Matcher t) {
        return new HeaderSegment(m, s, t.group(2), t.group(1).length());
    }

    private static Segment CodeSegment(final Manual m, final Segment s, final Matcher t) {
        return new MonospaceSegment(m, s, t.group(2));
    }

    private static Segment LinkSegment(final Manual m, final Segment s, final Matcher t) {
        return new LinkSegment(m, s, t.group(1), t.group(2));
    }

    private static Segment BoldSegment(final Manual m, final Segment s, final Matcher t) {
        return new BoldSegment(m, s, t.group(2));
    }

    private static Segment ItalicSegment(final Manual m, final Segment s, final Matcher t) {
        return new ItalicSegment(m, s, t.group(2));
    }

    private static Segment StrikethroughSegment(final Manual m, final Segment s, final Matcher t) {
        return new StrikethroughSegment(m, s, t.group(1));
    }

    private static Segment ImageSegment(final Manual m, final Segment s, final Matcher t) {
        final String title = t.group(1);
        final String url = t.group(2);
        final Optional<ContentRenderer> renderer = m.imageFor(url);
        if (renderer.isPresent()) {
            return new RenderSegment(m, s, new StringTextComponent(title), renderer.get());
        } else {
            return new TextSegment(m, s, I18n.get(API.MOD_ID + ".manual.warning.missing.content_render", url));
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

    private Document() {
    }
}
