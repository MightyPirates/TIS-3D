package li.cil.tis3d.client.manual;

import li.cil.tis3d.api.API;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public final class Strings {
    public static final ITextComponent WARNING_IMAGE_MISSING = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.image");
    public static final ITextComponent WARNING_ITEM_MISSING = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.item");
    public static final ITextComponent WARNING_BLOCK_MISSING = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.block");
    public static final ITextComponent WARNING_TAG_MISSING = new TranslationTextComponent(API.MOD_ID + ".manual.warning.missing.tag");

    // --------------------------------------------------------------------- //

    private Strings() {
    }
}
