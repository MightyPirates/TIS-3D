package li.cil.manual.api.prefab.item;

import li.cil.manual.api.ManualModel;
import li.cil.manual.api.ManualScreenStyle;
import li.cil.manual.api.ManualStyle;
import li.cil.manual.api.util.ShowManualScreenEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;

/**
 * Base class that may be used for manual items, used as a representation of some manual and
 * to open said manual.
 */
public abstract class AbstractManualItem extends Item {
    protected AbstractManualItem(final Properties properties) {
        super(properties);
    }

    // --------------------------------------------------------------------- //
    // Item

    @Override
    public ActionResultType useOn(final ItemUseContext context) {
        final World world = context.getLevel();
        if (world.isClientSide()) {
            openManualFor(context, world);
        }
        return ActionResultType.sidedSuccess(world.isClientSide());
    }

    @Override
    public ActionResult<ItemStack> use(final World world, final PlayerEntity player, final Hand hand) {
        if (world.isClientSide()) {
            openManual(player);
        }
        return ActionResult.sidedSuccess(player.getItemInHand(hand), world.isClientSide());
    }

    // --------------------------------------------------------------------- //

    /**
     * The manual instance represented by this item.
     *
     * @return the manual.
     */
    @OnlyIn(Dist.CLIENT)
    protected abstract ManualModel getManualModel();

    /**
     * The style used when displaying the manual.
     *
     * @return the style.
     */
    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected ManualStyle getManualStyle() {
        return null;
    }

    /**
     * The style used for the default manual screen.
     *
     * @return the screen style.
     */
    @OnlyIn(Dist.CLIENT)
    @Nullable
    protected ManualScreenStyle getScreenStyle() {
        return null;
    }

    /**
     * Opens the {@link net.minecraft.client.gui.screen.Screen} used to display the manual represented by this item.
     * <p>
     * By default this will open the built-in manual screen, which can be customized using the style returned by
     * {@link #getManualStyle()} and {@link #getScreenStyle()}. To use a custom screen implementation, override this
     * method.
     */
    @OnlyIn(Dist.CLIENT)
    protected void showManualScreen() {
        MinecraftForge.EVENT_BUS.post(new ShowManualScreenEvent(getManualModel(), getManualStyle(), getScreenStyle()));
    }

    // --------------------------------------------------------------------- //

    @OnlyIn(Dist.CLIENT)
    private void openManualFor(final ItemUseContext context, final World world) {
        final ManualModel model = getManualModel();
        model.reset();
        model.pathFor(world, context.getClickedPos(), context.getClickedFace()).ifPresent(model::push);
        showManualScreen();
    }

    @OnlyIn(Dist.CLIENT)
    private void openManual(final PlayerEntity player) {
        final ManualModel manual = getManualModel();
        if (player.isShiftKeyDown()) {
            manual.reset();
        }
        showManualScreen();
    }
}
