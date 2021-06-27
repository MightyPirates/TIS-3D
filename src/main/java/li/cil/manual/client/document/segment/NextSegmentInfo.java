package li.cil.manual.client.document.segment;

import javax.annotation.Nullable;

public final class NextSegmentInfo {
    @Nullable public final Segment segment;
    public int absoluteX;
    public int relativeY;

    // --------------------------------------------------------------------- //

    public NextSegmentInfo(@Nullable final Segment segment, final int absoluteX, final int relativeY) {
        this.segment = segment;
        this.absoluteX = absoluteX;
        this.relativeY = relativeY;
    }

    public NextSegmentInfo(final Segment segment) {
        this.segment = segment;
    }
}
