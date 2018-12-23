package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.api.infrared.InfraredReceiverTile;
import li.cil.tis3d.common.event.TickHandlerInfraredPacket;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.util.Raytracing;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.particle.DustParticleParameters;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BoundingBox;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;


import javax.annotation.Nullable;
import java.util.List;

/**
 * Represents a single value in transmission, sent by an {@link li.cil.tis3d.common.module.ModuleInfrared}.
 */
public final class EntityInfraredPacket extends Entity implements InfraredPacket {
    public static EntityType<EntityInfraredPacket> TYPE;

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
    private static final TrackedData<Integer> DATA_VALUE = DataTracker.registerData(EntityInfraredPacket.class, TrackedDataHandlerRegistry.INTEGER);

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
        super(TYPE, world);
        fireImmune = true;
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
        setPosition(start.x, start.y, start.z);
        velocityX = direction.x * TRAVEL_SPEED;
        velocityY = direction.y * TRAVEL_SPEED;
        velocityZ = direction.z * TRAVEL_SPEED;
        lifetime = DEFAULT_LIFETIME + 1; // First update in next frame.
        this.value = value;
        getDataTracker().set(DATA_VALUE, value & 0xFFFF);
    }

    /**
     * Called from our watchdog each server tick to update our lifetime.
     */
    public void updateLifetime() {
        if (lifetime-- < 1) {
            invalidate();
        }
    }

    /**
     * Remove flag that the entity is dead; used to revive it when being redirected.
     */
    private void revive() {
        invalid = false;
        if (!getEntityWorld().isClient) {
            TickHandlerInfraredPacket.INSTANCE.watchPacket(this);
        }
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
    public void invalidate() {
        super.invalidate();
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
    public void updateLogic() {
        // Enforce lifetime, fail-safe, should be tracked in updateLifetime().
        if (lifetime < 1) {
            invalidate();
            return;
        }

        // Do general update logic.
        super.updateLogic();

        // Check for collisions and handle them.
        final HitResult hit = checkCollisions();

        // Emit some particles.
        emitParticles(hit);

        // Update position.
        x += velocityX;
        y += velocityY;
        z += velocityZ;

        // Update bounding box.
        setPosition(x, y, z);
    }

    @Override
    public boolean method_5713() {
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

	// TODO
/*    @Override
    public ItemStack getPickedResult(final RayTraceResult hit) {
        return ItemStack.EMPTY;
    } */


    @Override
    public boolean shouldRenderAtDistance(final double distance) {
        return false;
    }

    // TODO
/*    @Override
    public boolean shouldRenderInPass(final int pass) {
        return false;
    } */

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
        return new Vec3d(velocityX, velocityY, velocityZ).normalize();
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
                x = position.x * normalizer;
                y = position.y * normalizer;
                z = position.z * normalizer;
            } else {
                x = position.x;
                y = position.y;
                z = position.z;
            }

            // Apply new direction.
            final Vec3d velocityVec = direction.normalize();
            velocityX = velocityVec.x * TRAVEL_SPEED;
            velocityY = velocityVec.y * TRAVEL_SPEED;
            velocityZ = velocityVec.z * TRAVEL_SPEED;
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

        final double dx, dy, dz;
        if (hit == null || hit.pos == null) {
            dx = velocityX;
            dy = velocityY;
            dz = velocityZ;
        } else {
            dx = hit.pos.x - x;
            dy = hit.pos.y - y;
            dz = hit.pos.z - z;
        }

        final double xx = x + dx * t;
        final double yy = y + dy * t;
        final double zz = z + dz * t;

        ((ServerWorld) world).method_14199(
                new DustParticleParameters(1f, 0.2f, 0 , 1f),
                xx, yy, zz, 1, 0, 0, 0, 0
        );
    }

    @Nullable
    private HitResult checkCollisions() {
        final HitResult hit = checkCollision();
        if (hit != null) {
            // For travel distance adjustment, see below.
            final Vec3d oldPos = getPosVector();

            switch (hit.type) {
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
            final Vec3d newPos = getPosVector();
            final double delta = newPos.subtract(oldPos).length() / TRAVEL_SPEED;
            x -= velocityX * delta;
            y -= velocityY * delta;
            z -= velocityZ * delta;
        }
        return hit;
    }

    @Nullable
    private HitResult checkCollision() {
        final World world = getEntityWorld();
        final Vec3d start = new Vec3d(x, y, z);
        final Vec3d target = start.add(velocityX, velocityY, velocityZ);

        // Check for block collisions.
        final HitResult blockHit = Raytracing.raytrace(world, start, target, Raytracing::intersectIgnoringTransparent);

        // Check for entity collisions.
        final HitResult entityHit = checkEntityCollision(world, start, target);

        // If we have both, pick the closer one.
        if (blockHit != null && blockHit.type != HitResult.Type.NONE &&
            entityHit != null && entityHit.type != HitResult.Type.NONE) {
            if (blockHit.pos.squaredDistanceTo(start) < entityHit.pos.squaredDistanceTo(start)) {
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
        HitResult entityHit = null;
        double bestSqrDistance = Double.POSITIVE_INFINITY;

        final List<Entity> collisions = world.getEntities(this, getBoundingBox().stretch(velocityX, velocityY, velocityZ), EntityPredicates.EXCEPT_SPECTATOR);
        for (final Entity entity : collisions) {
            if (entity.doesCollide()) {
                final BoundingBox entityBounds = entity.getBoundingBox();
                final HitResult hit = entityBounds.rayTrace(start, target);
                if (hit != null) {
                    final double sqrDistance = start.squaredDistanceTo(hit.pos);
                    if (sqrDistance < bestSqrDistance) {
                        hit.entity = entity;
                        hit.type = HitResult.Type.ENTITY;
                        entityHit = hit;
                        bestSqrDistance = sqrDistance;
                    }
                }
            }
        }

        return entityHit;
    }

    private void onBlockCollision(final HitResult hit) {
        final World world = getEntityWorld();

        // Just in case...
        final BlockPos pos = hit.getBlockPos();
        if (pos == null) {
            return;
        }
        if (!world.isBlockLoaded(pos)) {
            return;
        }
        final Block block = world.getBlockState(pos).getBlock();

        // Traveling through a portal?
        if (hit.type == HitResult.Type.BLOCK && block == Blocks.OAK_TRAPDOOR) {
            setInPortal(pos);
            return;
        }

        // First things first, we ded.
        invalidate();

        // Next up, notify receiver, if any.
        if (block instanceof InfraredReceiver) {
            ((InfraredReceiver) block).onInfraredPacket(this, hit);
        }
        onCapabilityProviderCollision(hit, world.getBlockEntity(pos));
    }

    private void onEntityCollision(final HitResult hit) {
        // First things first, we ded.
        invalidate();

        // Next up, notify receiver, if any.
        onCapabilityProviderCollision(hit, hit.entity);
    }

	private void onCapabilityProviderCollision(final HitResult hit, @Nullable final Object provider) {
		if (provider instanceof InfraredReceiverTile) {
            final InfraredReceiver capability = ((InfraredReceiverTile) provider).getInfraredReceiver(hit.side);
            if (capability != null) {
                capability.onInfraredPacket(this, hit);
            }

            // TODO
			/* final InfraredReceiver capability = provider.getCapability(CapabilityInfraredReceiver.INFRARED_RECEIVER_CAPABILITY, hit.sideHit);
			if (capability != null) {
				capability.onInfraredPacket(this, hit);
			} */
		}
	}

    /* private void onCapabilityProviderCollision(final RayTraceResult hit, @Nullable final ICapabilityProvider provider) {
        if (provider != null) {
            final InfraredReceiver capability = provider.getCapability(CapabilityInfraredReceiver.INFRARED_RECEIVER_CAPABILITY, hit.sideHit);
            if (capability != null) {
                capability.onInfraredPacket(this, hit);
            }
        }
    } */
}
