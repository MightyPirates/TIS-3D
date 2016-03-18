package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.MessageParticleEffect;
import li.cil.tis3d.util.Raytracing;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/**
 * Represents a single value in transmission, sent by an {@link li.cil.tis3d.common.module.ModuleInfrared}.
 */
public final class EntityInfraredPacket extends Entity implements InfraredPacket {
    // --------------------------------------------------------------------- //
    // Computed data

    /**
     * No we don't move at the speed of light, even though we're infrared.
     * <p>
     * Don't ask. This is Minecraft.
     */
    private final static float TRAVEL_SPEED = 24f;

    /**
     * The default lifetime of a packet, in ticks, implicitly controlling how
     * far packets travel (that being <tt>TRAVEL_SPEED * DEFAULT_LIFETIME</tt>).
     */
    private static final int DEFAULT_LIFETIME = 2;

    // NBT tag names.
    private static final String TAG_VALUE = "value";
    private static final String TAG_LIFETIME = "lifetime";

    // Data watcher ids.
    private static final DataParameter<Integer> DATA_VALUE = EntityDataManager.createKey(EntityInfraredPacket.class, DataSerializers.VARINT);

    // --------------------------------------------------------------------- //
    // Persisted data

    /**
     * The number of ticks that remain until the packet de-spawns.
     */
    private int lifetime;

    /**
     * The value carried by this packet.
     */
    private short value;

    public EntityInfraredPacket(final World world) {
        super(world);
        isImmuneToFire = true;
        setSize(0.25f, 0.25f);
    }

    // --------------------------------------------------------------------- //

    /**
     * Sets up the packet's starting position, velocity and value carried.
     * <p>
     * Called from {@link li.cil.tis3d.common.module.ModuleInfrared} directly
     * after instantiation of a new infrared packet entity.
     *
     * @param start     the position of the block that spawned the packet.
     * @param direction the direction in which the packet was emitted.
     * @param value     the value the packet carries.
     */
    public void configure(final Vec3d start, final Vec3d direction, final short value) {
        setPosition(start.xCoord, start.yCoord, start.zCoord);
        motionX = direction.xCoord * TRAVEL_SPEED;
        motionY = direction.yCoord * TRAVEL_SPEED;
        motionZ = direction.zCoord * TRAVEL_SPEED;
        lifetime = DEFAULT_LIFETIME;
        this.value = value;
        dataWatcher.set(DATA_VALUE, value & 0xFFFF);
    }

    /**
     * Called from our watchdog each server tick to update our lifetime.
     */
    public void updateLifetime() {
        if (lifetime-- < 1) {
            setDead();
        }
    }

    /**
     * Remove flag that the entity is dead; used to revive it when being redirected.
     */
    private void revive() {
        isDead = false;
        if (!getEntityWorld().isRemote) {
            TickHandlerInfraredPacket.INSTANCE.watchPacket(this);
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void entityInit() {
        dataWatcher.register(DATA_VALUE, 0);
        if (!getEntityWorld().isRemote) {
            TickHandlerInfraredPacket.INSTANCE.watchPacket(this);
        }
    }

    @Override
    public void setDead() {
        super.setDead();
        if (!getEntityWorld().isRemote) {
            TickHandlerInfraredPacket.INSTANCE.unwatchPacket(this);
        }
    }

    @Override
    protected void readEntityFromNBT(final NBTTagCompound nbt) {
        lifetime = nbt.getInteger(TAG_LIFETIME);
        value = nbt.getShort(TAG_VALUE);
    }

    @Override
    protected void writeEntityToNBT(final NBTTagCompound nbt) {
        nbt.setInteger(TAG_LIFETIME, lifetime);
        nbt.setShort(TAG_VALUE, value);
    }

    @Override
    public void onEntityUpdate() {
        // Enforce lifetime, fail-safe, should be tracked in updateLifetime().
        if (lifetime < 1) {
            setDead();
            return;
        }

        // Do general update logic.
        super.onEntityUpdate();

        // Check for collisions and handle them.
        final RayTraceResult hit = checkCollisions();

        // Emit some particles.
        emitParticles(hit);

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
    public ItemStack getPickedResult(final RayTraceResult hit) {
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
    public short getPacketValue() {
        return value;
    }

    @Override
    public Vec3d getPacketPosition() {
        return getPositionVector();
    }

    @Override
    public Vec3d getPacketDirection() {
        return new Vec3d(motionX, motionY, motionZ).normalize();
    }

    @Override
    public void redirectPacket(final Vec3d position, final Vec3d direction, final int addedLifetime) {
        lifetime += addedLifetime;
        if (lifetime > 0) {
            // Revive!
            revive();

            // Apply new position.
            final Vec3d oldPos = getPositionVector();
            final Vec3d delta = position.subtract(oldPos);
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
            final Vec3d motionVec = direction.normalize();
            motionX = motionVec.xCoord * TRAVEL_SPEED;
            motionY = motionVec.yCoord * TRAVEL_SPEED;
            motionZ = motionVec.zCoord * TRAVEL_SPEED;
        }
    }

    // --------------------------------------------------------------------- //

    private void emitParticles(final RayTraceResult hit) {
        final World world = getEntityWorld();
        if (world.isRemote) {
            // Entities regularly die too quickly for the client to have a
            // chance to simulate them, so we trigger the particles from
            // the server. Kinda meh, but whatever works.
            return;
        }

        final double t = rand.nextDouble();

        final double dx, dy, dz;
        if (hit == null || hit.hitVec == null) {
            dx = motionX;
            dy = motionY;
            dz = motionZ;
        } else {
            dx = hit.hitVec.xCoord - posX;
            dy = hit.hitVec.yCoord - posY;
            dz = hit.hitVec.zCoord - posZ;
        }

        final double x = posX + dx * t;
        final double y = posY + dy * t;
        final double z = posZ + dz * t;

        final MessageParticleEffect message = new MessageParticleEffect(world, EnumParticleTypes.REDSTONE, x, y, z);
        final NetworkRegistry.TargetPoint target = Network.getTargetPoint(world, x, y, z, Network.RANGE_LOW);
        Network.INSTANCE.getWrapper().sendToAllAround(message, target);
    }

    private RayTraceResult checkCollisions() {
        final RayTraceResult hit = checkCollision();
        if (hit != null) {
            // For travel distance adjustment, see below.
            final Vec3d oldPos = getPositionVector();

            switch (hit.typeOfHit) {
                case BLOCK:
                    onBlockCollision(hit);
                    break;
                case ENTITY:
                    onEntityCollision(hit);
                    break;
                default:
                    return null;
            }

            // Offset to compensate position adjustments. This way the total
            // distance the packet travels per tick stays constant, even if
            // it was moved around by a packet handler.
            final Vec3d newPos = getPositionVector();
            final double delta = newPos.subtract(oldPos).lengthVector() / TRAVEL_SPEED;
            posX -= motionX * delta;
            posY -= motionY * delta;
            posZ -= motionZ * delta;
        }
        return hit;
    }

    private RayTraceResult checkCollision() {
        final World world = getEntityWorld();
        final Vec3d start = new Vec3d(posX, posY, posZ);
        final Vec3d target = start.addVector(motionX, motionY, motionZ);

        // Check for block collisions.
        final RayTraceResult blockHit = Raytracing.raytrace(world, start, target, Raytracing::intersectIgnoringTransparent);

        // Check for entity collisions.
        final RayTraceResult entityHit = checkEntityCollision(world, start, target);

        // If we have both, pick the closer one.
        if (blockHit != null && blockHit.typeOfHit != RayTraceResult.Type.MISS &&
                entityHit != null && entityHit.typeOfHit != RayTraceResult.Type.MISS) {
            if (blockHit.hitVec.squareDistanceTo(start) < entityHit.hitVec.squareDistanceTo(start)) {
                return blockHit;
            } else {
                return entityHit;
            }
        } else if (blockHit != null) {
            return blockHit;
        } else if (entityHit != null) {
            return entityHit;
        } else {
            return null;
        }
    }

    private RayTraceResult checkEntityCollision(final World world, final Vec3d start, final Vec3d target) {
        RayTraceResult entityHit = null;
        double bestSqrDistance = Double.POSITIVE_INFINITY;

        final List<Entity> collisions = world.getEntitiesWithinAABBExcludingEntity(this, getEntityBoundingBox().addCoord(motionX, motionY, motionZ));
        for (final Entity entity : collisions) {
            if (entity.canBeCollidedWith()) {
                final AxisAlignedBB entityBounds = entity.getEntityBoundingBox();
                final RayTraceResult hit = entityBounds.calculateIntercept(start, target);
                if (hit != null) {
                    final double sqrDistance = start.squareDistanceTo(hit.hitVec);
                    if (sqrDistance < bestSqrDistance) {
                        hit.entityHit = entity;
                        hit.typeOfHit = RayTraceResult.Type.ENTITY;
                        entityHit = hit;
                        bestSqrDistance = sqrDistance;
                    }
                }
            }
        }

        return entityHit;
    }

    private void onBlockCollision(final RayTraceResult hit) {
        final World world = getEntityWorld();

        // Just in case...
        final BlockPos pos = hit.getBlockPos();
        if (!world.isBlockLoaded(pos)) {
            return;
        }
        final Block block = world.getBlockState(pos).getBlock();

        // Traveling through a portal?
        if (hit.typeOfHit == RayTraceResult.Type.BLOCK && block == Blocks.portal) {
            setPortal(pos);
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

    private void onEntityCollision(final RayTraceResult hit) {
        // First things first, we ded.
        setDead();

        // Next up, notify receiver, if any.
        final Entity entity = hit.entityHit;
        if (entity instanceof InfraredReceiver) {
            ((InfraredReceiver) entity).onInfraredPacket(this, hit);
        }
    }
}
