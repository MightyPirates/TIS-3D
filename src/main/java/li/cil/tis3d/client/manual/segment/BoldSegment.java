package li.cil.tis3d.client.manual.segment;


import com.mojang.realmsclient.gui.ChatFormatting;

public final class BoldSegment extends TextSegment {
    public BoldSegment(final Segment parent, final String text) {
        super(parent, text);
    }

    @Override
    protected String format() {
        return ChatFormatting.BOLD.toString();
    }

    @Override
    public String toString() {
        return String.format("**%s**", text());
    }
}
