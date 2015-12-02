package li.cil.tis3d.system.module;

import com.google.common.base.Strings;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.prefab.AbstractModule;
import li.cil.tis3d.client.TextureLoader;
import li.cil.tis3d.system.module.execution.MachineImpl;
import li.cil.tis3d.system.module.execution.MachineState;
import li.cil.tis3d.system.module.execution.compiler.Compiler;
import li.cil.tis3d.system.module.execution.compiler.ParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * The programmable execution module.
 */
public final class ModuleExecution extends AbstractModule {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final MachineImpl machine;
    private ParseException compileError;
    private State state = State.IDLE;

    // --------------------------------------------------------------------- //
    // Computed data

    private enum State {
        IDLE,
        ERROR,
        RUNNING,
        WAITING
    }

    private static final String[] STATE_LOCATIONS = new String[]{
            null,
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_ERROR.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_RUNNING.toString(),
            TextureLoader.LOCATION_MODULE_EXECUTION_OVERLAY_WAITING.toString()
    };

    // --------------------------------------------------------------------- //

    public ModuleExecution(final Casing casing, final Face face) {
        super(casing, face);
        machine = new MachineImpl(casing, face);
    }

    private boolean isCodeSource(final ItemStack stack) {
        if (stack != null) {
            if (stack.getItem() == Items.written_book) {
                return true;
            }
            if (stack.getItem() == Items.writable_book) {
                return true;
            }
        }

        return false;
    }

    private String getSourceCode(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return null;
        }

        final NBTTagCompound nbt = stack.getTagCompound();
        final NBTTagList pages = nbt.getTagList("pages", Constants.NBT.TAG_STRING);
        if (pages.tagCount() < 1) {
            return null;
        }

        return pages.getStringTagAt(0);
    }

    private void compile(final String code, final EntityPlayer player) {
        compileError = null;
        try {
            machine.getState().clear();
            Compiler.compile(code, machine.getState());
        } catch (final ParseException e) {
            compileError = e;
            player.addChatMessage(new ChatComponentText(e.toString()));
        }
    }

    private void sendData(final boolean full) {
        final NBTTagCompound nbt = new NBTTagCompound();
        if (full) {
            writeToNBT(nbt);
        } else {
            nbt.setInteger("pc", machine.getState().pc);
            nbt.setInteger("acc", machine.getState().acc);
            nbt.setInteger("bak", machine.getState().bak);
            nbt.setString("state", state.name());
        }
        getCasing().sendData(getFace(), nbt);
    }

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        final State prevState = state;

        if (compileError != null) {
            state = State.ERROR;
        } else if (machine.getState().instructions.isEmpty()) {
            state = State.IDLE;
        } else {
            final int prevInstruction = machine.getState().pc;

            machine.step();

            if (machine.getState().pc != prevInstruction) {
                state = State.RUNNING;
                sendData(false);
                return; // Don't send data twice.
            } else {
                state = State.WAITING;
            }
        }

        if (prevState != state) {
            sendData(false);
        }
    }

    @Override
    public void onEnabled() {
        sendData(true);
    }

    @Override
    public void onDisabled() {
        machine.getState().reset();
        state = State.IDLE;
        sendData(false);
    }

    @Override
    public void onWriteComplete(final Port port) {
        if (compileError == null) {
            machine.onWriteCompleted(port);
        }
    }

    @Override
    public boolean onActivate(final EntityPlayer player, final float hitX, final float hitY, final float hitZ) {
        if (player.isSneaking()) {
            return false;
        }

        final ItemStack stack = player.getHeldItem();
        if (!isCodeSource(stack)) {
            return false;
        }

        final String code = getSourceCode(stack);
        if (Strings.isNullOrEmpty(code)) {
            return true; // Handled, but does nothing.
        }

        if (!getCasing().getWorld().isRemote) {
            compile(code, player);
            sendData(true);
        }

        return true;
    }

    @Override
    public void onData(final NBTTagCompound nbt) {
        if (nbt.hasKey("pc")) {
            machine.getState().pc = nbt.getInteger("pc");
            machine.getState().acc = nbt.getInteger("acc");
            machine.getState().bak = nbt.getInteger("bak");
            try {
                state = Enum.valueOf(State.class, nbt.getString("state"));
            } catch (final IllegalArgumentException ignored) {
            }
        } else {
            readFromNBT(nbt);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void render(final float partialTicks) {
        final MachineState machineState = machine.getState();
        if (state == State.IDLE || machineState.code == null) {
            return;
        }

        GlStateManager.pushAttrib();
        RenderHelper.disableStandardItemLighting();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 0 / 1.0F);

        Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
        final TextureAtlasSprite icon = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(STATE_LOCATIONS[state.ordinal()]);
        drawQuad(icon.getMinU(), icon.getMinV(), icon.getMaxU(), icon.getMaxV());

        GlStateManager.bindTexture(0);

        GlStateManager.translate(4 / 16f, 4 / 16f, 0);
        GlStateManager.scale(1 / 128f, 1 / 128f, 1);
        final FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        for (int lineNumber = 0; lineNumber < machineState.code.length; lineNumber++) {
            final String line = machineState.code[lineNumber];
            final boolean isCurrent = machineState.lineNumbers.get(machineState.pc) == lineNumber;
            final int color = isCurrent ? 0xFFFFFF : 0x999999;
            fontRenderer.drawString(line, 0, lineNumber * fontRenderer.FONT_HEIGHT, color);
        }
        fontRenderer.drawString("pc: " + machineState.pc + "; line: " + machineState.lineNumbers.get(machineState.pc), 0, -fontRenderer.FONT_HEIGHT, 0xFFFFFF);

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popAttrib();
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("compileError")) {
            final NBTTagCompound errorNbt = nbt.getCompoundTag("compileError");
            compileError = new ParseException(errorNbt.getString("message"), errorNbt.getInteger("lineNumber"), errorNbt.getInteger("column"));
        } else {
            final NBTTagCompound machineNbt = nbt.getCompoundTag("machine");
            machine.getState().readFromNBT(machineNbt);
            try {
                state = Enum.valueOf(State.class, nbt.getString("state"));
            } catch (final IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (compileError != null) {
            final NBTTagCompound errorNbt = new NBTTagCompound();
            errorNbt.setString("message", compileError.getMessage());
            errorNbt.setInteger("lineNumber", compileError.getLineNumber());
            errorNbt.setInteger("column", compileError.getColumn());
            nbt.setTag("compileError", errorNbt);
        } else {
            final NBTTagCompound machineNbt = new NBTTagCompound();
            machine.getState().writeToNBT(machineNbt);
            nbt.setTag("machine", machineNbt);
            nbt.setString("state", state.name());
        }
    }
}
