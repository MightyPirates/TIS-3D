package li.cil.tis3d.common.api;

import li.cil.tis3d.api.detail.FontRendererAPI;
import li.cil.tis3d.client.render.font.FontRendererTextureMonospace;

/**
 * Provide access to our tiny mono-space font.
 */
public final class FontRendererAPIImpl implements FontRendererAPI {
    @Override
    public void drawString(final String string) {
        FontRendererTextureMonospace.drawString(string);
    }

    @Override
    public void drawString(final String string, final int maxChars) {
        FontRendererTextureMonospace.drawString(string, maxChars);
    }

    @Override
    public int getCharWidth() {
        return FontRendererTextureMonospace.CHAR_WIDTH;
    }

    @Override
    public int getCharHeight() {
        return FontRendererTextureMonospace.CHAR_HEIGHT;
    }
}
