package li.cil.tis3d.common.item;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.ManualStyle;
import li.cil.manual.api.prefab.item.AbstractManualItem;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.util.TooltipUtils;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The manual!
 */
public final class ManualItem extends AbstractManualItem {
    public ManualItem() {
        super(new Properties().tab(ModCreativeTabs.COMMON));
    }

    // --------------------------------------------------------------------- //

    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final Level level, final List<Component> tooltip, final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipUtils.tryAddDescription(stack, tooltip);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected ManualModel getManualModel() {
        return Manuals.MANUAL.get();
    }

    @Override
    protected ManualStyle getManualStyle() {
        return new ManualStyle() {
            @Override
            public li.cil.manual.api.render.FontRenderer getMonospaceFont() {
                return NormalFontRenderer.INSTANCE;
            }
        };
    }

    @Override
    protected ManualScreenStyle getScreenStyle() {
        return new ManualScreenStyle() {
            @Override
            public ResourceLocation getWindowBackground() {
                return Textures.LOCATION_GUI_MANUAL_BACKGROUND;
            }

            @Override
            public ResourceLocation getScrollButtonTexture() {
                return Textures.LOCATION_GUI_MANUAL_SCROLL;
            }

            @Override
            public ResourceLocation getTabButtonTexture() {
                return Textures.LOCATION_GUI_MANUAL_TAB;
            }

            @Override
            public Rect2i getDocumentRect() {
                return new Rect2i(16, 48, 220, 176);
            }

            @Override
            public Rect2i getScrollBarRect() {
                return new Rect2i(250, 48, 20, 180);
            }

            @Override
            public Rect2i getScrollButtonRect() {
                return new Rect2i(0, 0, 26, 13);
            }

            @Override
            public Rect2i getTabAreaRect() {
                return new Rect2i(-52, 40, 64, 224);
            }

            @Override
            public Rect2i getTabRect() {
                return new Rect2i(0, 0, 64, 32);
            }

            @Override
            public int getTabOverlap() {
                return 8;
            }
        };
    }
}
