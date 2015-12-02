package li.cil.tis3d.system.module;

import com.google.common.base.Strings;
import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.prefab.AbstractModule;
import li.cil.tis3d.system.module.execution.MachineImpl;
import li.cil.tis3d.system.module.execution.compiler.Compiler;
import li.cil.tis3d.system.module.execution.compiler.ParseException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.Constants;

/**
 * The programmable execution module.
 */
public final class ModuleExecution extends AbstractModule {
    // --------------------------------------------------------------------- //
    // Persisted data

    private final MachineImpl machine;
    private ParseException compileError;

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

    // --------------------------------------------------------------------- //
    // Module

    @Override
    public void step() {
        if (compileError == null) {
            machine.step();
        }
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
        }

        return true;
    }

    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        final NBTTagCompound stateNbt = nbt.getCompoundTag("state");
        machine.getState().readFromNBT(stateNbt);

        if (nbt.hasKey("compileError")) {
            final NBTTagCompound errorNbt = nbt.getCompoundTag("compileError");
            compileError = new ParseException(errorNbt.getString("message"), errorNbt.getInteger("lineNumber"), errorNbt.getInteger("column"));
        }
    }

    @Override
    public void writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        final NBTTagCompound stateNbt = new NBTTagCompound();
        machine.getState().writeToNBT(stateNbt);
        nbt.setTag("state", stateNbt);

        if (compileError != null) {
            final NBTTagCompound errorNbt = new NBTTagCompound();
            errorNbt.setString("message", compileError.getMessage());
            errorNbt.setInteger("lineNumber", compileError.getLineNumber());
            errorNbt.setInteger("column", compileError.getColumn());
            nbt.setTag("compileError", errorNbt);
        }
    }
}
