package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.init.Entities;
import li.cil.tis3d.common.module.InfraredModule;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.util.Raytracing;
import li.cil.tis3d.util.WorldUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * Represents a single value in transmission, sent by an {@link InfraredModule}.
 */
public final class InfraredPacketEntity extends Entity implements InfraredPacket {
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
    private static final TrackedData<Integer> DATA_VALUE = DataTracker.registerData(InfraredPacketEntity.class, TrackedDataHandlerRegistry.INTEGER);

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

    // --------------------------------------------------------------------- //
    // Entity

    public InfraredPacketEntity(final EntityType<?> type, final World world) {
        super(type, world);
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }

    // --------------------------------------------------------------------- //

    /**
     * Sets up the packet's starting position, velocity and value carried.
     * <p>
     * Called from {@link InfraredModule} directly
     * after instantiation of a new infrared packet entity.
     *
     * @param start     the position of the block that spawned the packet.
     * @param direction the direction in which the packet was emitted.
     * @param value     the value the packet carries.
     */
    public void configure(final Vec3d start, final Vec3d direction, final short value) {
        updatePosition(start.x, start.y, start.z);
        setVelocity(direction.multiply(TRAVEL_SPEED));
        lifetime = DEFAULT_LIFETIME + 1; // First update in next frame.
        this.value = value;
        getDataTracker().set(DATA_VALUE, value & 0xFFFF);
    }

    /**
     * Called from our watchdog each server tick to update our lifetime.
     */
    public void updateLifetime() {
        if (lifetime-- < 1) {
            remove();
        }
    }

    /**
     * Remove flag that the entity is dead; used to revive it when being redirected.
     */
    private void revive() {
        removed = false;
        if (!getEntityWorld().isClient) {
            TickHandlerInfraredPacket.INSTANCE.watchPacket(this);
        }
    }

    private void setPos(final Vec3d pos) {
        setPos(pos.x, pos.y, pos.z);
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(DATA_VALUE, 0);
        if (!getEntityWorld().isClient) {
            TickHandlerInfraredPacket.INSTANCE.watchPacket(this);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (!getEntityWorld().isClient) {
            TickHandlerInfraredPacket.INSTANCE.unwatchPacket(this);
        }
    }

    @Override
    protected void readCustomDataFromTag(final CompoundTag nbt) {
        lifetime = nbt.getInt(TAG_LIFETIME);
        value = nbt.getShort(TAG_VALUE);
    }

    @Override
    protected void writeCustomDataToTag(final CompoundTag nbt) {
        nbt.putInt(TAG_LIFETIME, lifetime);
        nbt.putShort(TAG_VALUE, value);
    }

    @Override
    public void tick() {
        // Enforce lifetime, fail-safe, should be tracked in updateLifetime().
        if (lifetime < 1) {
            remove();
            return;
        }

        // Do general update logic.
        super.tick();

        // Check for collisions and handle them.
        final HitResult hit = checkCollisions();

        // Emit some particles.
        emitParticles(hit);

        // Update position.
        final Vec3d newPos = getPos().add(getVelocity());
        setPos(newPos);

        // Update bounding box.
        refreshPosition();
    }

    @Override
    public boolean checkWaterState() {
        return false;
    }

    @Override
    public boolean canFly() {
        return false;
    }

    @Override
    public boolean isImmuneToExplosion() {
        return true;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public boolean shouldRender(final double distance) {
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
        return getPosVector();
    }

    @Override
    public Vec3d getPacketDirection() {
        return getVelocity().normalize();
    }

    @Override
    public void redirectPacket(final Vec3d position, final Vec3d direction, final int addedLifetime) {
        lifetime += addedLifetime;
        if (lifetime > 0) {
            // Revive!
            revive();

            // Apply new position.
            final Vec3d oldPos = getPosVector();
            final Vec3d delta = position.subtract(oldPos);
            final double sqrDelta = delta.dotProduct(delta);
            if (sqrDelta > TRAVEL_SPEED * TRAVEL_SPEED) {
                // Clamp to an area where we won't get weird effects due to
                // the later adjustment of the position to compensate for
                // manual movement (see `checkCollisions`).
                final double normalizer = TRAVEL_SPEED * TRAVEL_SPEED / sqrDelta;
                setPos(position.multiply(normalizer));
            } else {
                setPos(position);
            }

            // Apply new direction.
            this.setVelocity(direction.normalize().multiply(TRAVEL_SPEED));
        }
    }

    // --------------------------------------------------------------------- //

    private void emitParticles(@Nullable final HitResult hit) {
        final World world = getEntityWorld();
        if (world.isClient) {
            // Entities regularly die too quickly for the client to have a
            // chance to simulate them, so we trigger the particles from
            // the server. Kinda meh, but whatever works.
            return;
        }

        final double t = random.nextDouble();
        final Vec3d d;
        if (hit == null || hit.getPos() == null) {
            d = getVelocity();
        } else {
            d = hit.getPos().subtract(getPos());
        }

        final Vec3d pp = getPos().add(d.multiply(t));
        Network.INSTANCE.sendRedstoneEffect(world, pp.x, pp.y, pp.z);
    }

    @Nullable
    private HitResult checkCollisions() {
        final HitResult hitResult = checkCollision();
        if (hitResult != null) {
            // For travel distance adjustment, see below.
            final Vec3d oldPos = getPosVector();

            switch (hitResult.getType()) {
                case BLOCK:
                    onBlockCollision((BlockHitResult)hitResult);
                    break;
                case ENTITY:
                    onEntityCollision((EntityHitResult)hitResult);
                    break;
                default:
                    return null;
            }

            // Offset to compensate position adjustments. This way the total
            // distance the packet travels per tick stays constant, even if
            // it was moved around by a packet handler.
            final Vec3d curPos = getPosVector();
            final double delta = curPos.subtract(oldPos).length() / TRAVEL_SPEED;

            setPos(curPos.subtract(getVelocity().multiply(delta)));
        }

        return hitResult;
    }

    @Nullable
    private HitResult checkCollision() {
        final World world = getEntityWorld();
        final Vec3d start = getPos();
        final Vec3d target = start.add(getVelocity());

        // Check for block collisions.
        final HitResult blockHit = Raytracing.raytrace(world, start, target, Raytracing::intersectIgnoringTransparent);

        // Check for entity collisions.
        final HitResult entityHit = checkEntityCollision(world, start, target);

        // If we have both, pick the closer one.
        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS &&
            entityHit != null && entityHit.getType() != HitResult.Type.MISS) {
            if (blockHit.getPos().squaredDistanceTo(start) < entityHit.getPos().squaredDistanceTo(start)) {
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

    @Nullable
    private HitResult checkEntityCollision(final World world, final Vec3d start, final Vec3d target) {
        EntityHitResult entityHit = null;
        double bestSqrDistance = Double.POSITIVE_INFINITY;

        final List<Entity> collisions = world.getEntities(this, getBoundingBox().stretch(getVelocity()), EntityPredicates.EXCEPT_SPECTATOR);
        for (final Entity entity : collisions) {
            if (entity.collides()) {
                final Box entityBounds = entity.getBoundingBox();
                final Optional<Vec3d> hit = entityBounds.rayTrace(start, target);
                if (hit.isPresent()) {
                    final double sqrDistance = start.squaredDistanceTo(hit.get());
                    if (sqrDistance < bestSqrDistance) {
                        entityHit = new EntityHitResult(entity, hit.get());
                        bestSqrDistance = sqrDistance;
                    }
                }
            }
        }

        return entityHit;
    }

    private void onBlockCollision(final BlockHitResult hit) {
        final World world = getEntityWorld();

        // Just in case...
        final BlockPos pos = hit.getBlockPos();
        if (pos == null) {
            return;
        }
        if (!WorldUtils.isBlockLoaded(world, pos)) {
            return;
        }
        final Block block = world.getBlockState(pos).getBlock();

        // Traveling through a portal?
        if (block == Blocks.NETHER_PORTAL || block == Blocks.END_PORTAL) {
            setInNetherPortal(pos);
            return;
        }

        // First things first, we ded.
        remove();

        // Next up, notify receiver, if any.
        onInfraredReceiverCollision(hit, block);
        onInfraredReceiverCollision(hit, world.getBlockEntity(pos));
    }

    private void onEntityCollision(final EntityHitResult hit) {
        // First things first, we ded.
        remove();

        // Next up, notify receiver, if any.
        onInfraredReceiverCollision(hit, hit.getEntity());
    }

    private void onInfraredReceiverCollision(final HitResult hit, @Nullable final Object provider) {
        if (provider instanceof InfraredReceiver) {
            ((InfraredReceiver)provider).onInfraredPacket(this, hit);
        }
    }
}
