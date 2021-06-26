package li.cil.manual.client.document;

import li.cil.tis3d.api.API;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class Strings {
    public static final ITextComponent NO_SUCH_IMAGE = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.image");
    public static final ITextComponent NO_SUCH_ITEM = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.item");
    public static final ITextComponent NO_SUCH_BLOCK = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.block");
    public static final ITextComponent NO_SUCH_TAG = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.tag");

    // --------------------------------------------------------------------- //

    private Strings() {
    }
}
