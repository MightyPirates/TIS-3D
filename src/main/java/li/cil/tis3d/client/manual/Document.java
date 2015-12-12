package li.cil.tis3d.client.manual;

import com.google.common.base.Strings;
import li.cil.tis3d.api.ManualAPI;
import li.cil.tis3d.api.manual.ImageRenderer;
import li.cil.tis3d.client.manual.segment.BoldSegment;
import li.cil.tis3d.client.manual.segment.CodeSegment;
import li.cil.tis3d.client.manual.segment.HeaderSegment;
import li.cil.tis3d.client.manual.segment.InteractiveSegment;
import li.cil.tis3d.client.manual.segment.ItalicSegment;
import li.cil.tis3d.client.manual.segment.LinkSegment;
import li.cil.tis3d.client.manual.segment.RenderSegment;
import li.cil.tis3d.client.manual.segment.Segment;
import li.cil.tis3d.client.manual.segment.SegmentRefiner;
import li.cil.tis3d.client.manual.segment.StrikethroughSegment;
import li.cil.tis3d.client.manual.segment.TextSegment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
public final class Document {
    /**
     * Parses a plain text document into a list of segments.
     *
     * @param document iterator over the lines of the document to parse.
     * @return the first segment of the parsed document.
     */
    public static Segment parse(final Iterable<String> document) {
        // Get top-level list of text segments.
        List<Segment> segments = new ArrayList<>();
        for (final String line : document) {
            segments.add(new TextSegment(null, trimRight(line)));
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
        return segments.size() > 0 ? segments.get(0) : new TextSegment(null, "");
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
        return renderer.FONT_HEIGHT + 1;
    }

    /**
     * Renders a list of segments and tooltips if a segment with a tooltip is hovered.
     * Returns the hovered interactive segment, if any.
     *
     * @param document  the document to render.
     * @param x         the x position to render at.
     * @param y         the y position to render at.
     * @param maxWidth  the width of the area to render the document in.
     * @param maxHeight the height of the area to render the document in.
     * @param yOffset   the vertical scroll offset of the document.
     * @param renderer  the font renderer to use.
     * @param mouseX    the x position of the mouse.
     * @param mouseY    the y position of the mouse.
     * @return the interactive segment being hovered, if any.
     */
    public static Optional<InteractiveSegment> render(final Segment document, final int x, final int y, final int maxWidth, final int maxHeight, final int yOffset, final FontRenderer renderer, final int mouseX, final int mouseY) {
        final Minecraft mc = Minecraft.getMinecraft();

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        // On some systems/drivers/graphics cards the next calls won't update the
        // depth buffer correctly if alpha test is enabled. Guess how we found out?
        // By noticing that on those systems it only worked while chat messages
        // were visible. Yeah. I know.
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        // Clear depth mask, then create masks in foreground above and below scroll area.
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDepthMask(true);
        GL11.glColorMask(false, false, false, false);

        GL11.glPushMatrix();
        GL11.glTranslatef(0, 0, 500);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(0, y);
        GL11.glVertex2f(mc.displayWidth, y);
        GL11.glVertex2f(mc.displayWidth, 0);
        GL11.glVertex2f(0, 0);
        GL11.glVertex2f(0, mc.displayHeight);
        GL11.glVertex2f(mc.displayWidth, mc.displayHeight);
        GL11.glVertex2f(mc.displayWidth, y + maxHeight);
        GL11.glVertex2f(0, y + maxHeight);
        GL11.glEnd();
        GL11.glPopMatrix();
        GL11.glColorMask(true, true, true, true);

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
                final Optional<InteractiveSegment> result = segment.render(x, currentY, indent, maxWidth, renderer, mouseX, mouseY);
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

        GL11.glPopAttrib();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        return hovered;
    }

    // ----------------------------------------------------------------------- //

    private static String trimRight(final String s) {
        if (s == null) {
            return "";
        }

        for (int i = s.length(); i > 0; i--) {
            if (!Character.isWhitespace(s.charAt(i - 1))) {
                return s.substring(0, i);
            }
        }
        return s;
    }

    // ----------------------------------------------------------------------- //

    private static Segment HeaderSegment(final Segment s, final Matcher m) {
        return new HeaderSegment(s, m.group(2), m.group(1).length());
    }

    private static Segment CodeSegment(final Segment s, final Matcher m) {
        return new CodeSegment(s, m.group(2));
    }

    private static Segment LinkSegment(final Segment s, final Matcher m) {
        return new LinkSegment(s, m.group(1), m.group(2));
    }

    private static Segment BoldSegment(final Segment s, final Matcher m) {
        return new BoldSegment(s, m.group(2));
    }

    private static Segment ItalicSegment(final Segment s, final Matcher m) {
        return new ItalicSegment(s, m.group(2));
    }

    private static Segment StrikethroughSegment(final Segment s, final Matcher m) {
        return new StrikethroughSegment(s, m.group(1));
    }

    private static Segment ImageSegment(final Segment s, final Matcher m) {
        try {
            final ImageRenderer renderer = ManualAPI.imageFor(m.group(2));
            if (renderer != null) {
                return new RenderSegment(s, m.group(1), renderer);
            } else {
                return new TextSegment(s, "No renderer found for: " + m.group(2));
            }
        } catch (final Throwable t) {
            return new TextSegment(s, Strings.isNullOrEmpty(t.toString()) ? "Unknown error." : t.toString());
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
        public final Pattern pattern;
        public final SegmentRefiner refiner;

        private PatternMapping(final String pattern, final SegmentRefiner refiner) {
            this.pattern = Pattern.compile(pattern);
            this.refiner = refiner;
        }
    }

    private Document() {
    }
}
