package li.cil.tis3d.system;

import li.cil.tis3d.api.Casing;
import li.cil.tis3d.api.Face;
import li.cil.tis3d.api.Port;
import li.cil.tis3d.api.Side;
import li.cil.tis3d.api.module.Module;
import li.cil.tis3d.api.module.Redstone;
import li.cil.tis3d.common.tile.TileEntityCasing;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

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
    private static final Side[][] SIDE_MAPPING;

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
        SIDE_MAPPING = new Side[][]{
                {Side.DOWN, Side.DOWN, Side.DOWN, Side.DOWN},   // Y_NEG
                {Side.UP, Side.UP, Side.UP, Side.UP},     // Y_POS
                {Side.RIGHT, Side.LEFT, Side.DOWN, Side.DOWN},   // Z_NEG
                {Side.RIGHT, Side.LEFT, Side.UP, Side.UP},     // Z_POS
                {Side.RIGHT, Side.LEFT, Side.RIGHT, Side.LEFT},   // X_NEG
                {Side.RIGHT, Side.LEFT, Side.LEFT, Side.RIGHT}   // X_POS
                //    LEFT        RIGHT       UP          DOWN
        };
    }

    /**
     * The tile entity hosting this casing.
     */
    private final TileEntityCasing tileEntity;

    /**
     * The flat list of all {@link Port} on this casing, for enumeration.
     */
    private final PortImpl[] ports = new PortImpl[24];

    // --------------------------------------------------------------------- //

    public CasingImpl(final TileEntityCasing tileEntity) {
        this.tileEntity = tileEntity;

        for (final Face face : Face.VALUES) {
            for (final Side side : Side.VALUES) {
                ports[pack(face, side)] = new PortImpl(this, mapFace(face, side), mapSide(face, side));
            }
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

        final NBTTagList portsNbt = nbt.getTagList("ports", Constants.NBT.TAG_COMPOUND);
        final int portCount = Math.min(portsNbt.tagCount(), ports.length);
        for (int i = 0; i < portCount; i++) {
            ports[i].readFromNBT(portsNbt.getCompoundTagAt(i));
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

        final NBTTagList portsNbt = new NBTTagList();
        for (final PortImpl port : ports) {
            final NBTTagCompound portNbt = new NBTTagCompound();
            port.writeToNBT(portNbt);
            portsNbt.appendTag(portNbt);
        }
        nbt.setTag("ports", portsNbt);
    }

    private static int pack(final Face face, final Side side) {
        return face.ordinal() * Side.VALUES.length + side.ordinal();
    }

    private static int packMapped(final Face face, final Side side) {
        return mapFace(face, side).ordinal() * Side.VALUES.length + mapSide(face, side).ordinal();
    }

    private static Face mapFace(final Face face, final Side side) {
        return FACE_MAPPING[face.ordinal()][side.ordinal()];
    }

    private static Side mapSide(final Face face, final Side side) {
        return SIDE_MAPPING[face.ordinal()][side.ordinal()];
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

        final boolean hadRedstone = getModule(face) instanceof Redstone;

        modules[face.ordinal()] = module;

        if (module == null) {
            for (final Side side : Side.VALUES) {
                getInputPort(face, side).cancelRead();
                getOutputPort(face, side).cancelWrite();
            }
        }

        if (hadRedstone) {
            if (!getWorld().isRemote) {
                tileEntity.markDirty();
                getWorld().notifyNeighborsOfStateChange(getPosition(), tileEntity.getBlockType());
            }
        }
    }

    @Override
    public Port getInputPort(final Face face, final Side side) {
        return ports[pack(face, side)];
    }

    @Override
    public Port getOutputPort(final Face face, final Side side) {
        return ports[packMapped(face, side)];
    }

    @Override
    public void step() {
        for (final Module module : modules) {
            if (module != null) {
                module.step();
            }
        }
        for (final PortImpl port : ports) {
            port.step();
        }
    }
}
