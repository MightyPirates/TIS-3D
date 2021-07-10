package li.cil.tis3d.common.item;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.ManualStyle;
import li.cil.manual.api.prefab.item.AbstractManualItem;
import li.cil.tis3d.client.manual.Manuals;
import li.cil.tis3d.client.renderer.Textures;
import li.cil.tis3d.client.renderer.font.NormalFontRenderer;
import li.cil.tis3d.util.TooltipUtils;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The manual!
 */
public final class ManualItem extends AbstractManualItem {
    public ManualItem() {
        super(new Properties().tab(ItemGroups.COMMON));
    }

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag flag) {
        super.appendHoverText(stack, world, tooltip, flag);
        TooltipUtils.tryAddDescription(stack, tooltip);
    }

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    @Override
    protected ManualModel getManualModel() {
        return Manuals.MANUAL.get();
    }

    @OnlyIn(Dist.CLIENT)
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
            public Rectangle2d getDocumentRect() {
                return new Rectangle2d(16, 48, 220, 176);
            }

            @Override
            public Rectangle2d getScrollBarRect() {
                return new Rectangle2d(250, 48, 20, 180);
            }

            @Override
            public Rectangle2d getScrollButtonRect() {
                return new Rectangle2d(0, 0, 26, 13);
            }

            @Override
            public Rectangle2d getTabAreaRect() {
                return new Rectangle2d(-52, 40, 64, 224);
            }

            @Override
            public Rectangle2d getTabRect() {
                return new Rectangle2d(0, 0, 64, 32);
            }

            @Override
            public int getTabOverlap() {
                return 8;
            }
        };
    }
}
