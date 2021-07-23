package li.cil.tis3d.client;

import li.cil.tis3d.common.ConfigManager.Comment;
import li.cil.tis3d.common.ConfigManager.Path;
import li.cil.tis3d.common.ConfigManager.Translation;
import li.cil.tis3d.common.ConfigManager.Type;
import net.minecraftforge.fml.config.ModConfig;

@Type(ModConfig.Type.CLIENT)
public final class ClientConfig {
    /**
     * Whether to swing the player's arm while typing in a terminal module.
     */
    @Path("module.terminal")
    @Comment("Whether to swing the player's arm while typing on a terminal module.")
    @Translation("animateTyping")
    public static boolean animateTypingHand = true;
}
