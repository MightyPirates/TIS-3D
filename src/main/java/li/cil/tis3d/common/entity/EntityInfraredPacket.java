package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Represents a single value in transmission, sent by an {@link li.cil.tis3d.system.module.ModuleInfrared}.
 */
public class EntityInfraredPacket extends Entity implements InfraredPacket {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * No we don't move at the speed of light, even though we're infrared.
     * <p>
     * Don't ask. This is Minecraft.
     */
    private final static float TRAVEL_SPEED = 4f;

    /**
     * The default lifetime of a packet, in ticks, implicitly controlling how
     * far packets travel (that being <tt>TRAVEL_SPEED * DEFAULT_LIFETIME</tt>).
     */
    private static final int DEFAULT_LIFETIME = 10;

    // NBT tag names.
    private static final String TAG_VALUE = "value";
    private static final String TAG_LIFETIME = "lifetime";

    // Data watcher ids.
    private static final int DATA_VALUE = 5;
    private static final int DATA_LIFETIME = 6;

    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The number of ticks that remain until the packet de-spawns.
     */
    private int lifetime;

    /**
     * The value carried by this packet.
     */
    private int value;

    public EntityInfraredPacket(final World world) {
        super(world);
        isImmuneToFire = true;
        setSize(0.25f, 0.25f);
    }

    // --------------------------------------------------------------------- //

    /**
     * Sets up the packet's starting position, velocity and value carried.
     * <p>
     * Called from {@link li.cil.tis3d.system.module.ModuleInfrared} directly
     * after instantiation of a new infrared packet entity.
     *
     * @param start     the position of the block that spawned the packet.
     * @param direction the direction in which the packet was emitted.
     * @param value     the value the packet carries.
     */
    public void configure(final BlockPos start, final EnumFacing direction, final int value) {
        final BlockPos offsetPosition = start.offset(direction);
        setPosition(offsetPosition.getX() + 0.5, offsetPosition.getY() + 0.5, offsetPosition.getZ() + 0.5);
        motionX = direction.getFrontOffsetX() * TRAVEL_SPEED;
        motionY = direction.getFrontOffsetY() * TRAVEL_SPEED;
        motionZ = direction.getFrontOffsetZ() * TRAVEL_SPEED;
        lifetime = DEFAULT_LIFETIME;
        this.value = value;
        dataWatcher.updateObject(DATA_VALUE, value);
        dataWatcher.updateObject(DATA_LIFETIME, lifetime);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void entityInit() {
        dataWatcher.addObject(DATA_VALUE, 0);
        dataWatcher.addObject(DATA_LIFETIME, DEFAULT_LIFETIME);
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound nbt) {
        lifetime = nbt.getInteger(TAG_LIFETIME);
        value = nbt.getInteger(TAG_VALUE);
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound nbt) {
        nbt.setInteger(TAG_LIFETIME, lifetime);
        nbt.setInteger(TAG_VALUE, value);
    }

    @Override
    public void onEntityUpdate() {
        // Enforce lifetime.
        if (lifetime-- <= 0) {
            setDead();
            return;
        }

        // Do general update logic.
        super.onEntityUpdate();

        // Emit some particles.
        emitParticles();

        // Check for collisions and handle them.
        checkCollisions();

        // Update position.
        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        // Update bounding box.
        setPosition(posX, posY, posZ);
    }

    @Override
    public boolean handleWaterMovement() {
        return false;
    }

    @Override
    public boolean isPushedByWater() {
        return false;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public ItemStack getPickedResult(final MovingObjectPosition hit) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isInRangeToRenderDist(final double distance) {
        return false;
    }

    @Override
    public boolean shouldRenderInPass(final int pass) {
        return false;
    }

    // --------------------------------------------------------------------- //
    // InfraredPacket

    @Override
    public int getPacketValue() {
        return value;
    }

    @Override
    public Vec3 getPacketPosition() {
        return getPositionVector();
    }

    @Override
    public Vec3 getPacketDirection() {
        return new Vec3(motionX, motionY, motionZ).normalize();
    }

    @Override
    public void redirectPacket(final Vec3 position, final Vec3 direction, final int addedLifetime) {
        lifetime += addedLifetime;
        if (lifetime > 0) {
            // Revive!
            isDead = false;

            // Apply new position.
            final Vec3 oldPos = getPositionVector();
            final Vec3 delta = position.subtract(oldPos);
            final double sqrDelta = delta.dotProduct(delta);
            if (sqrDelta > TRAVEL_SPEED * TRAVEL_SPEED) {
                // Clamp to an area where we won't get weird effects due to
                // the later adjustment of the position to compensate for
                // manual movement (see `checkCollisions`).
                final double normalizer = TRAVEL_SPEED * TRAVEL_SPEED / sqrDelta;
                posX = position.xCoord * normalizer;
                posY = position.yCoord * normalizer;
                posZ = position.zCoord * normalizer;
            } else {
                posX = position.xCoord;
                posY = position.yCoord;
                posZ = position.zCoord;
            }

            // Apply new direction.
            final Vec3 motionVec = direction.normalize();
            motionX = motionVec.xCoord * TRAVEL_SPEED;
            motionY = motionVec.yCoord * TRAVEL_SPEED;
            motionZ = motionVec.zCoord * TRAVEL_SPEED;
        }
    }

    // --------------------------------------------------------------------- //

    private void emitParticles() {
        final World world = getEntityWorld();
        if (world.isRemote) {
            // Entities regularly die too quickly for the client to have a
            // chance to simulate them, so we trigger the particles from
            // the server. Kinda meh, but whatever works.
            return;
        }

        final double t = rand.nextDouble();

        final double x = posX + motionX * t;
        final double y = posY + motionY * t;
        final double z = posZ + motionZ * t;

        final MessageParticleEffect message = new MessageParticleEffect(world, EnumParticleTypes.REDSTONE, x, y, z);
        final NetworkRegistry.TargetPoint target = Network.getTargetPoint(world, x, y, z, Network.RANGE_LOW);
        Network.INSTANCE.getWrapper().sendToAllAround(message, target);
    }

    private void checkCollisions() {
        final MovingObjectPosition hit = checkCollision();
        if (hit != null) {
            // For travel distance adjustment, see below.
            final Vec3 oldPos = getPositionVector();

            switch (hit.typeOfHit) {
                case BLOCK:
                    onBlockCollision(hit);
                    break;
                case ENTITY:
                    onEntityCollision(hit);
                    break;
                default:
                    return;
            }

            // Offset to compensate position adjustments. This way the total
            // distance the packet travels per tick stays constant, even if
            // it was moved around by a packet handler.
            final Vec3 newPos = getPositionVector();
            final double delta = newPos.subtract(oldPos).lengthVector() / TRAVEL_SPEED;
            posX -= motionX * delta;
            posY -= motionY * delta;
            posZ -= motionZ * delta;
        }
    }

    private MovingObjectPosition checkCollision() {
        final World world = getEntityWorld();
        final Vec3 start = new Vec3(posX, posY, posZ);
        final Vec3 target = start.addVector(motionX, motionY, motionZ);

        // Check for block collisions.
        final MovingObjectPosition blockHit = world.rayTraceBlocks(start, target);

        // Check for entity collisions.
        final List<Entity> collisions = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ));

        MovingObjectPosition entityHit = null;
        double bestSqrDistance = Double.POSITIVE_INFINITY;
        for (final Entity entity : collisions) {
            if (entity.canBeCollidedWith()) {
                final AxisAlignedBB entityBounds = entity.getEntityBoundingBox();
                final MovingObjectPosition hit = entityBounds.calculateIntercept(start, target);
                if (hit != null) {
                    final double sqrDistance = start.squareDistanceTo(hit.hitVec);
                    if (sqrDistance < bestSqrDistance) {
                        hit.entityHit = entity;
                        hit.typeOfHit = MovingObjectPosition.MovingObjectType.ENTITY;
                        entityHit = hit;
                        bestSqrDistance = sqrDistance;
                    }
                }
            }
        }

        if (blockHit != null && blockHit.typeOfHit != MovingObjectPosition.MovingObjectType.MISS &&
                entityHit != null && entityHit.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
            if (blockHit.hitVec.squareDistanceTo(start) < entityHit.hitVec.squareDistanceTo(start)) {
                return blockHit;
            } else {
                return entityHit;
            }
        }

        if (blockHit != null && blockHit.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
            return blockHit;
        }

        if (entityHit != null && entityHit.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
            return entityHit;
        }

        return null;
    }

    private void onBlockCollision(final MovingObjectPosition hit) {
        final World world = getEntityWorld();

        // Just in case...
        final BlockPos pos = hit.getBlockPos();
        if (!world.isBlockLoaded(pos)) {
            return;
        }
        final Block block = world.getBlockState(pos).getBlock();

        // Traveling through a portal?
        if (hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && block == Blocks.portal) {
            func_181015_d(pos);
            return;
        }

        // Air block?
        if (block.isAir(world, pos)) {
            return;
        }

        // Non-blocking block?
        if (!block.getMaterial().blocksLight()) {
            return;
        }

        // First things first, we ded.
        setDead();

        // Next up, notify receiver, if any.
        if (block instanceof InfraredReceiver) {
            ((InfraredReceiver) block).onInfraredPacket(this, hit);
        }

        final TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof InfraredReceiver) {
            ((InfraredReceiver) tileEntity).onInfraredPacket(this, hit);
        }
    }

    private void onEntityCollision(final MovingObjectPosition hit) {
        // First things first, we ded.
        setDead();

        // Next up, notify receiver, if any.
        final Entity entity = hit.entityHit;
        if (entity instanceof InfraredReceiver) {
            ((InfraredReceiver) entity).onInfraredPacket(this, hit);
        }
    }
}
