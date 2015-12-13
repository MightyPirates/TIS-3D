package li.cil.tis3d.api.prefab.client;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
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
 * @author Sangar, Vexatos
 */
public abstract class ModuleItemRenderer implements IItemRenderer {

	public abstract IIcon getFrontIcon(ItemRenderType type, ItemStack item, Object... data);

	protected ResourceLocation getTextureLocation(ItemRenderType type, ItemStack item, Object... data) {
		return TextureMap.locationBlocksTexture;
	}

	protected String getModelPath() {
		return "tis3d:models/item/module.obj";
	}

	// Actual rendering code

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch(type) {
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
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return helper != ItemRendererHelper.BLOCK_3D;
	}

	protected IModelCustom model = AdvancedModelLoader.loadModel(new ResourceLocation(getModelPath()));

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_CULL_FACE);
		RenderHelper.disableStandardItemLighting();
		GL11.glRotatef(180, 1, 0, 1);
		GL11.glTranslatef(0, -1f, 0);

		switch(type) {
			case ENTITY: {
				GL11.glTranslatef(-0.5f, 0.25f, -0.5f);
				if(RenderItem.renderInFrame) {
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

		renderFront(type, item, data);

		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		GL11.glPopAttrib();
	}

	protected void renderCasing(ItemRenderType type, ItemStack item, Object... data) {
		Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("tis3d:textures/blocks/casingModule.png"));
		model.renderAll();
	}

	protected void renderFront(ItemRenderType type, ItemStack item, Object... data) {
		GL11.glRotatef(90, 0, -1, 0);
		GL11.glTranslatef(1 / 16f, 1 / 16f, -0.5f - 1 / 4096f);
		GL11.glScalef(14 / 16f, 14 / 16f, 14 / 16f);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 0.0F);
		Minecraft.getMinecraft().getTextureManager().bindTexture(getTextureLocation(type, item, data));
		IIcon icon = getFrontIcon(type, item, data);
		if(icon != null) {
			drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());
		}
	}

	@SideOnly(Side.CLIENT)
	protected static void drawQuad(float x, float y, float w, float h, float u0, float v0, float u1, float v1) {
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV((double) x, (double) (y + h), 0.0D, (double) u0, (double) v1);
		tessellator.addVertexWithUV((double) (x + w), (double) (y + h), 0.0D, (double) u1, (double) v1);
		tessellator.addVertexWithUV((double) (x + w), (double) y, 0.0D, (double) u1, (double) v0);
		tessellator.addVertexWithUV((double) x, (double) y, 0.0D, (double) u0, (double) v0);
		tessellator.draw();
	}

	@SideOnly(Side.CLIENT)
	protected static void drawQuad(float u0, float v0, float u1, float v1) {
		drawQuad(0.0F, 0.0F, 1.0F, 1.0F, u0, v0, u1, v1);
	}
}
