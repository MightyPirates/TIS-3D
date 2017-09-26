package li.cil.tis3d.api.prefab.client;

import li.cil.tis3d.api.API;
import li.cil.tis3d.api.util.RenderUtil;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

/**
 * Base class provided for convenience of rendering module items in the same
 * way the modules in TIS-3D are rendered. This by default reuses the TIS-3D
 * model for modules and allows applying a custom overlay texture.
 * <p>
 * Use this by registering an instance of a specialization of this as the item
 * renderer of your module item.
 *
 * @author Sangar, Vexatos
 */
public abstract class AbstractModuleItemRenderer implements IItemRenderer {
    public static final String LOCATION_MODULE_MODEL = API.MOD_ID + ":models/item/module.obj";
    public static final String LOCATION_MODULE_TEXTURE = API.MOD_ID + ":textures/blocks/casing_module.png";
    public static final IModelCustom MODULE_MODEL = AdvancedModelLoader.loadModel(new ResourceLocation(LOCATION_MODULE_MODEL));

    // --------------------------------------------------------------------- //

    /**
     * The model used for rendering modules.
     */
    protected final IModelCustom model;

    /**
     * Construct the module renderer using the specified model.
     *
     * @param model the model to use for this renderer.
     */
    protected AbstractModuleItemRenderer(final IModelCustom model) {
        this.model = model;
    }

    /**
     * Construct the module renderer using the default model.
     */
    protected AbstractModuleItemRenderer() {
        this.model = MODULE_MODEL;
    }

    // --------------------------------------------------------------------- //

    /**
     * Determines the texture being used for rendering the module's overlay.
     *
     * @param type the current render type.
     * @param item the item stack of the module being rendered.
     * @param data additional data (unused).
     * @return the location of the texture to use.
     */
    protected ResourceLocation getTextureLocation(final ItemRenderType type, final ItemStack item, final Object... data) {
        return TextureMap.locationItemsTexture;
    }

    /**
     * Get the icon representation of the texture to use as the overlay of the
     * module (specifying the texture coordinates on the texture location
     * specified in {@link #getTextureLocation}).
     *
     * @param type the current render type.
     * @param item the item stack of the module being rendered.
     * @param data additional data (unused).
     * @return the icon of the overlay, or <tt>null</tt> for no overlay.
     */
    public abstract IIcon getOverlayIcon(ItemRenderType type, ItemStack item, Object... data);

    /**
     * Determines whether the overlay texture of the module should ignore lighting,
     * i.e. whether it should be fully lit regardless of light conditions, effectively
     * making it glow.
     *
     * @param type the current render type.
     * @param item the item stack of the module being rendered.
     * @param data additional data (unused).
     * @return <tt>true</tt> if the overlay texture should ignore lighting.
     */
    protected boolean shouldIgnoreLighting(final ItemRenderType type, final ItemStack item, final Object... data) {
        return false;
    }

    // --------------------------------------------------------------------- //

    /**
     * Renders the base model of the module.
     *
     * @param type the current render type.
     * @param item the item stack of the module being rendered.
     * @param data additional data (unused).
     */
    protected void renderCasing(final ItemRenderType type, final ItemStack item, final Object... data) {
        RenderUtil.bindTexture(new ResourceLocation(LOCATION_MODULE_TEXTURE));
        model.renderAll();
    }

    /**
     * Render the overlay of the module.
     *
     * @param type the current render type.
     * @param item the item stack of the module being rendered.
     * @param data additional data (unused).
     */
    protected void renderOverlay(final ItemRenderType type, final ItemStack item, final Object... data) {
        GL11.glRotatef(90, 0, -1, 0);
        GL11.glTranslatef(1 / 16f, 1 / 16f, -0.5f - 1 / 4096f);
        GL11.glScalef(14 / 16f, 14 / 16f, 14 / 16f);

        if (shouldIgnoreLighting(type, item, data)) {
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 0);
        }

        final IIcon icon = getOverlayIcon(type, item, data);
        if (icon != null) {
            RenderUtil.bindTexture(getTextureLocation(type, item, data));
            RenderUtil.drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
        }
    }

    // --------------------------------------------------------------------- //
    // IItemRenderer

    @Override
    public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
        switch (type) {
            case ENTITY:
            case EQUIPPED:
            case EQUIPPED_FIRST_PERSON:
            case INVENTORY:
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper) {
        return helper != ItemRendererHelper.BLOCK_3D;
    }

    @Override
    public void renderItem(final ItemRenderType type, final ItemStack item, final Object... data) {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glDisable(GL11.GL_CULL_FACE);
        RenderHelper.disableStandardItemLighting();

        GL11.glPushMatrix();
        GL11.glRotatef(180, 1, 0, 1);
        GL11.glTranslatef(0, -1f, 0);

        switch (type) {
            case ENTITY: {
                GL11.glTranslatef(-0.5f, 0.25f, -0.5f);
                if (RenderItem.renderInFrame) {
                    GL11.glTranslatef(0f, 0.125f, 0f);
                }
                break;
            }
            case EQUIPPED_FIRST_PERSON: {
                GL11.glRotatef(90, 0, 1, 0);
                GL11.glTranslatef(0, -0.5f, 0.25f);
                GL11.glScalef(0.8125f, 0.8125f, 0.8125f);
                break;
            }
            case INVENTORY: {
                GL11.glScalef(1.125f, 1.125f, 1.125f);
                break;
            }
            case EQUIPPED: {
                GL11.glRotatef(130, 0, 1, 0);
                GL11.glTranslatef(-1.1f, -0.4f, -0.2f);
            }
        }

        renderCasing(type, item, data);

        renderOverlay(type, item, data);

        GL11.glPopMatrix();

        RenderHelper.enableStandardItemLighting();

        GL11.glPopAttrib();
    }
}
