package li.cil.manual.client.document;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import li.cil.manual.api.Manual;
import li.cil.manual.api.Style;
import li.cil.manual.api.render.ContentRenderer;
import li.cil.manual.client.document.segment.*;
import li.cil.tis3d.api.API;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
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
    private final Manual manual;
    private final Style style;
    @Nullable private Segment root;
    @Nullable private InteractiveSegment lastHovered;

    private int lastScrollY;
    private int lastGlobalY;
    @Nullable private NextSegmentInfo lastFirstVisible;

    /**
     * Creates a new document instance with the specified configuration.
     *
     * @param style  the fonts to use when rendering the generated segments.
     * @param manual the manual the document belongs to.
     */
    public Document(final Style style, final Manual manual) {
        this.manual = manual;
        this.style = style;
    }

    /**
     * Parses a plain text document into a list of segments.
     * <p>
     * This replaces the current content of this document.
     *
     * @param document iterator over the lines of the document to parse.
     */
    public void parse(final Iterable<String> document) {
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

        root = segments.size() > 0 ? segments.get(0) : new TextSegment(manual, style, null, "");
    }

    /**
     * Compute the overall height of a document, e.g. for computation of scroll offsets.
     *
     * @param width the width available for rendering the document.
     * @return the height of the document.
     */
    public int height(final int width) {
        if (root == null) {
            return 0;
        }

        int globalY = 0, lineHeight = 0;
        NextSegmentInfo current = new NextSegmentInfo(root);
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
                    if (current.absoluteX > 0) {
                        globalY += current.relativeY + segmentHeight;
                    } else {
                        globalY += current.relativeY;
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
     * @param scrollY     the vertical scroll offset of the document.
     * @param width       the width of the area to render the document in.
     * @param height      the height of the area to render the document in.
     * @param mouseX      the x position of the mouse relative to the document.
     * @param mouseY      the y position of the mouse relative to the document.
     * @return the interactive segment being hovered, if any.
     */
    public Optional<InteractiveSegment> render(final MatrixStack matrixStack, final int scrollY, final int width, final int height, final int mouseX, final int mouseY) {
        if (root == null) {
            return Optional.empty();
        }

        // On some systems/drivers/graphics cards the next calls won't update the
        // depth buffer correctly if alpha test is enabled. Guess how we found out?
        // By noticing that on those systems it only worked while chat messages
        // were visible. Yeah. I know.
        RenderSystem.disableAlphaTest();

        // Clear depth mask, then create masks in foreground above and below scroll area.
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        matrixStack.pushPose();
        matrixStack.translate(0, 0, 500);
        Screen.fill(matrixStack, 0, -1000, width, 0, 0);
        Screen.fill(matrixStack, 0, height, width, height + 1000, 0);
        matrixStack.popPose();

        // Actual rendering.
        final boolean isMouseOverDocument = mouseX >= 0 || mouseX <= width || mouseY >= 0 || mouseY <= height;
        Optional<InteractiveSegment> hovered = Optional.empty();
        int globalY, lineHeight = 0;
        NextSegmentInfo current;
        if (scrollY != lastScrollY || lastFirstVisible == null) {
            current = new NextSegmentInfo(root);
            lastScrollY = scrollY;
            globalY = -scrollY;
            lastFirstVisible = null;
        } else {
            globalY = lastGlobalY;
            current = lastFirstVisible;
        }
        while (current.segment != null) {
            final NextSegmentInfo info = current;
            final Segment segment = current.segment;
            final int localX = current.absoluteX;
            final int relativeY = current.relativeY;
            final int segmentHeight = segment.getLineHeight(localX, width);

            globalY += relativeY;

            current = segment.getNext(localX, lineHeight, width);

            final int segmentTop = globalY;
            final int segmentBottom;
            if (current.relativeY > 0) {
                if (current.absoluteX > 0) {
                    segmentBottom = segmentTop + current.relativeY + segmentHeight;
                } else {
                    segmentBottom = segmentTop + current.relativeY;
                }
            } else {
                segmentBottom = segmentTop + segmentHeight;
            }

            final boolean isVisible = segmentBottom >= 0 && segmentTop <= height;
            if (isVisible) {
                if (lastFirstVisible == null) {
                    lastGlobalY = globalY - relativeY;
                    lastFirstVisible = info;
                }

                matrixStack.pushPose();
                matrixStack.translate(0, globalY, 0);

                final Optional<InteractiveSegment> result = segment.render(matrixStack, localX, lineHeight, width, mouseX, mouseY - globalY);

                matrixStack.popPose();

                if (isMouseOverDocument && !hovered.isPresent()) {
                    hovered = result;
                }
            }

            // We can stop rendering once we run out the bottom of the visible area.
            if (segmentTop > height) {
                break;
            }

            final boolean isFirstSegmentOnNewLine = current.relativeY > 0;
            if (isFirstSegmentOnNewLine) {
                lineHeight = current.absoluteX > 0 ? segmentHeight : 0;
            } else {
                lineHeight = Math.max(lineHeight, segmentHeight);
            }
        }

        setHoveredSegment(hovered.orElse(null));

        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, false);

        return hovered;
    }

    // ----------------------------------------------------------------------- //

    private void setHoveredSegment(@Nullable final InteractiveSegment hovered) {
        if (hovered == lastHovered) {
            return;
        }

        if (lastHovered != null) {
            lastHovered.setMouseHovered(false);
        }

        lastHovered = hovered;

        if (lastHovered != null) {
            lastHovered.setMouseHovered(true);
        }
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
}
