package li.cil.tis3d.client;

import net.minecraftforge.fml.config.ModConfig;

import static li.cil.tis3d.common.ConfigManager.*;

@Type(ModConfig.Type.CLIENT)
public final class ClientConfig {
    /**
     * Whether to swing the player's arm while typing in a terminal module.
     */
    @Path("module.terminal")
    @Comment("Whether to swing the player's arm while typing on a terminal module.")
    @Translation("animateTyping")
    public static boolean animateTypingHand = true;

    /**
     * Whether to automatically convert code to upper-case in the code bible.
     */
    @Path("code_book")
    @Comment("Whether to automatically convert code to upper-case in the code bible.")
    @Translation("autoCodeUpperCase")
    public static boolean autoCodeUpperCase = true;
}
