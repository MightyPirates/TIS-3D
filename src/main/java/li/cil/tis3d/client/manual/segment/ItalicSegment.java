package li.cil.tis3d.client.manual.segment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public final class ItalicSegment extends TextSegment {
    public ItalicSegment(final Segment parent, final String text) {
        super(parent, text);
    }

    @Override
    protected String format() {
        return Formatting.ITALIC.toString();
    }

    @Override
    public String toString() {
        return String.format("*%s*", text());
    }
}
