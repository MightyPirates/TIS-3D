package li.cil.tis3d.system;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Pipe;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.common.Network;
import li.cil.tis3d.common.network.MessageModuleData;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

/**
 * Implementation of a {@link Casing}, holding up to six {@link Module}s.
 */
public final class CasingImpl implements Casing {
    // --------------------------------------------------------------------- //
    // Persisted data.

    /**
     * The {@link Module}s currently installed in this {@link Casing}.
     */
    private final Module[] modules = new Module[Face.VALUES.length];

    // --------------------------------------------------------------------- //
    // Computed data.

    // Mapping for faces and sides around edges, i.e. to get the other side
    // of an edge specified by a face and side.
    private static final Face[][] FACE_MAPPING;
    private static final Port[][] PORT_MAPPING;

    static {
        FACE_MAPPING = new Face[][]{
                {Face.X_NEG, Face.X_POS, Face.Z_POS, Face.Z_NEG}, // Y_NEG
                {Face.X_POS, Face.X_NEG, Face.Z_POS, Face.Z_NEG}, // Y_POS
                {Face.X_POS, Face.X_NEG, Face.Y_POS, Face.Y_NEG}, // Z_NEG
                {Face.X_NEG, Face.X_POS, Face.Y_POS, Face.Y_NEG}, // Z_POS
                {Face.Z_NEG, Face.Z_POS, Face.Y_POS, Face.Y_NEG}, // X_NEG
                {Face.Z_POS, Face.Z_NEG, Face.Y_POS, Face.Y_NEG}  // X_POS
                //    LEFT        RIGHT       UP          DOWN
        };
        PORT_MAPPING = new Port[][]{
                {Port.DOWN,  Port.DOWN,  Port.DOWN,  Port.DOWN},   // Y_NEG
                {Port.UP,    Port.UP,    Port.UP,    Port.UP},     // Y_POS
                {Port.RIGHT, Port.LEFT,  Port.DOWN,  Port.DOWN},   // Z_NEG
                {Port.RIGHT, Port.LEFT,  Port.UP,    Port.UP},     // Z_POS
                {Port.RIGHT, Port.LEFT,  Port.RIGHT, Port.LEFT},   // X_NEG
                {Port.RIGHT, Port.LEFT,  Port.LEFT,  Port.RIGHT}   // X_POS
                //    LEFT        RIGHT       UP          DOWN
        };
    }

    /**
     * The tile entity hosting this casing.
     */
    private final TileEntityCasing tileEntity;

    /**
     * The flat list of all {@link Pipe} on this casing, for enumeration.
     */
    private final PipeImpl[] pipes = new PipeImpl[24];

    // --------------------------------------------------------------------- //

    public CasingImpl(final TileEntityCasing tileEntity) {
        this.tileEntity = tileEntity;

        for (final Face face : Face.VALUES) {
            for (final Port port : Port.VALUES) {
                pipes[pack(face, port)] = new PipeImpl(this, face, mapFace(face, port), mapSide(face, port));
            }
        }
    }

    public void stepModules() {
        for (final Module module : modules) {
            if (module != null) {
                module.step();
            }
        }
    }

    public void stepPipes() {
        for (final PipeImpl pipe : pipes) {
            pipe.step();
        }
    }

    public void readFromNBT(final NBTTagCompound nbt) {
        final NBTTagList modulesNbt = nbt.getTagList("modules", Constants.NBT.TAG_COMPOUND);
        final int moduleCount = Math.min(modulesNbt.tagCount(), modules.length);
        for (int i = 0; i < moduleCount; i++) {
            if (modules[i] != null) {
                modules[i].readFromNBT(modulesNbt.getCompoundTagAt(i));
            }
        }

        final NBTTagList portsNbt = nbt.getTagList("pipes", Constants.NBT.TAG_COMPOUND);
        final int portCount = Math.min(portsNbt.tagCount(), pipes.length);
        for (int i = 0; i < portCount; i++) {
            pipes[i].readFromNBT(portsNbt.getCompoundTagAt(i));
        }
    }

    public void writeToNBT(final NBTTagCompound nbt) {
        final NBTTagList modulesNbt = new NBTTagList();
        for (final Module module : modules) {
            final NBTTagCompound moduleNbt = new NBTTagCompound();
            if (module != null) {
                module.writeToNBT(moduleNbt);
            }
            modulesNbt.appendTag(moduleNbt);
        }
        nbt.setTag("modules", modulesNbt);

        final NBTTagList pipesNbt = new NBTTagList();
        for (final PipeImpl pipe : pipes) {
            final NBTTagCompound portNbt = new NBTTagCompound();
            pipe.writeToNBT(portNbt);
            pipesNbt.appendTag(portNbt);
        }
        nbt.setTag("pipes", pipesNbt);
    }

    // --------------------------------------------------------------------- //

    private static int pack(final Face face, final Port port) {
        return face.ordinal() * Port.VALUES.length + port.ordinal();
    }

    private static int packMapped(final Face face, final Port port) {
        return mapFace(face, port).ordinal() * Port.VALUES.length + mapSide(face, port).ordinal();
    }

    private static Face mapFace(final Face face, final Port port) {
        return FACE_MAPPING[face.ordinal()][port.ordinal()];
    }

    private static Port mapSide(final Face face, final Port port) {
        return PORT_MAPPING[face.ordinal()][port.ordinal()];
    }

    // --------------------------------------------------------------------- //
    // Casing

    @Override
    public World getWorld() {
        return tileEntity.getWorld();
    }

    @Override
    public BlockPos getPosition() {
        return tileEntity.getPos();
    }

    @Override
    public void markDirty() {
        tileEntity.markDirty();
    }

    @Override
    public Module getModule(final Face face) {
        return modules[face.ordinal()];
    }

    public void setModule(final Face face, final Module module) {
        if (getModule(face) == module) {
            return;
        }

        // Remember for below.
        final boolean hadRedstone = getModule(face) instanceof Redstone;

        // Apply new module before adjust remaining state.
        modules[face.ordinal()] = module;

        // Reset pipe state controlled by a potential previous module.
        for (final Port port : Port.VALUES) {
            getReceivingPipe(face, port).cancelRead();
            getSendingPipe(face, port).cancelWrite();
        }

        // Reset redstone output if the previous module was redstone capable.
        if (hadRedstone) {
            if (!getWorld().isRemote) {
                tileEntity.markDirty();
                getWorld().notifyNeighborsOfStateChange(getPosition(), tileEntity.getBlockType());
            }
        }
    }

    @Override
    public Pipe getReceivingPipe(final Face face, final Port port) {
        return pipes[pack(face, port)];
    }

    @Override
    public Pipe getSendingPipe(final Face face, final Port port) {
        return pipes[packMapped(face, port)];
    }

    @Override
    public void sendData(final Face face, final NBTTagCompound data) {
        final MessageModuleData message = new MessageModuleData(this, face, data);
        if (getWorld().isRemote) {
            Network.INSTANCE.getWrapper().sendToServer(message);
        } else {
            Network.INSTANCE.sendModuleData(this, message);
        }
    }
}
