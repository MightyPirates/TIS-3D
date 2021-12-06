package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.common.capabilities.Capabilities;
import li.cil.tis3d.common.event.InfraredPacketTickHandler;
import li.cil.tis3d.common.module.InfraredModule;
import li.cil.tis3d.util.Raytracing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkHooks;

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
    private static final EntityDataAccessor<Integer> DATA_VALUE = SynchedEntityData.defineId(InfraredPacketEntity.class, EntityDataSerializers.INT);

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

    public InfraredPacketEntity(final EntityType<?> type, final Level level) {
        super(type, level);
        setNoGravity(true);
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
    public void configure(final Vec3 start, final Vec3 direction, final short value) {
        setPos(start.x, start.y, start.z);
        setDeltaMovement(direction.scale(TRAVEL_SPEED));
        lifetime = DEFAULT_LIFETIME + 1; // First update in next frame.
        this.value = value;
        getEntityData().set(DATA_VALUE, value & 0xFFFF);
    }

    /**
     * Called from our watchdog each server tick to update our lifetime.
     */
    public void updateLifetime() {
        if (lifetime-- < 1) {
            discard();
        }
    }

    /**
     * Remove flag that the entity is dead; used to revive it when being redirected.
     */
    @Override
    public void revive() {
        super.revive();
        if (!level.isClientSide()) {
            InfraredPacketTickHandler.watchPacket(this);
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void defineSynchedData() {
        getEntityData().define(DATA_VALUE, 0);
        if (!level.isClientSide()) {
            InfraredPacketTickHandler.watchPacket(this);
        }
    }

    @Override
    public void remove(final RemovalReason reason) {
        super.remove(reason);
        if (!level.isClientSide()) {
            InfraredPacketTickHandler.unwatchPacket(this);
        }
    }

    @Override
    protected void readAdditionalSaveData(final CompoundTag tag) {
        lifetime = tag.getInt(TAG_LIFETIME);
        value = tag.getShort(TAG_VALUE);
    }

    @Override
    protected void addAdditionalSaveData(final CompoundTag tag) {
        tag.putInt(TAG_LIFETIME, lifetime);
        tag.putShort(TAG_VALUE, value);
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        // Enforce lifetime, fail-safe, should be tracked in updateLifetime().
        if (lifetime < 1) {
            discard();
            return;
        }

        // Do general update logic.
        super.tick();

        // Check for collisions and handle them.
        final HitResult hit = checkCollisions();

        // Emit some particles.
        emitParticles(hit);

        // Update position and bounding box
        setPositionAndUpdateBounds(position().add(getDeltaMovement()));
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public boolean ignoreExplosion() {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean shouldRenderAtSqrDistance(final double distance) {
        return false;
    }

    // --------------------------------------------------------------------- //
    // InfraredPacket

    @Override
    public short getPacketValue() {
        return value;
    }

    @Override
    public Vec3 getPacketPosition() {
        return position();
    }

    @Override
    public Vec3 getPacketDirection() {
        return getDeltaMovement().normalize();
    }

    @Override
    public void redirectPacket(final Vec3 position, final Vec3 direction, final int addedLifetime) {
        lifetime += addedLifetime;
        if (lifetime > 0) {
            // Revive!
            revive();

            // Apply new position.
            setPositionAndUpdateBounds(position);

            // Apply new direction.
            setDeltaMovement(direction.normalize().scale(TRAVEL_SPEED));
        }
    }

    // --------------------------------------------------------------------- //

    private void setPositionAndUpdateBounds(final Vec3 pos) {
        setPos(pos.x, pos.y, pos.z);
    }

    private void emitParticles(@Nullable final HitResult hit) {
        if (!(level instanceof final ServerLevel serverLevel)) {
            // Entities regularly die too quickly for the client to have a
            // chance to simulate them, so we trigger the particles from
            // the server. Kinda meh, but whatever works.
            return;
        }

        // Spawn particle effect somewhere between current position and either the
        // position where the packet collided with something, or along the movement
        // direction (i.e. where the packet will be next).
        final double t = random.nextDouble();
        final Vec3 delta = hit == null ? getDeltaMovement() : hit.getLocation().subtract(position());
        final Vec3 pos = position().add(delta.scale(t));
        serverLevel.sendParticles(DustParticleOptions.REDSTONE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }

    @Nullable
    private HitResult checkCollisions() {
        final HitResult hit = checkCollision();
        if (hit instanceof BlockHitResult) {
            onBlockCollision((BlockHitResult) hit);
        } else if (hit instanceof EntityHitResult) {
            onEntityCollision((EntityHitResult) hit);
        }
        return hit;
    }

    @Nullable
    private HitResult checkCollision() {
        final Vec3 start = position();
        final Vec3 target = start.add(getDeltaMovement());

        // Check for block collisions.
        final HitResult blockHit = Raytracing.raytrace(level, start, target, Raytracing::intersectIgnoringTransparent);

        // Check for entity collisions.
        final HitResult entityHit = checkEntityCollision(level, start, target);

        // If we have both, pick the closer one.
        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS &&
            entityHit != null && entityHit.getType() != HitResult.Type.MISS) {
            if (blockHit.getLocation().distanceToSqr(start) < entityHit.getLocation().distanceToSqr(start)) {
                return blockHit;
            } else {
                return entityHit;
            }
        }

        if (blockHit != null) {
            return blockHit;
        }

        if (entityHit != null) {
            return entityHit;
        }

        return null;
    }

    @Nullable
    private HitResult checkEntityCollision(final Level level, final Vec3 start, final Vec3 target) {
        Entity entityHit = null;
        Vec3 entityHitVec = null;
        double bestSqrDistance = Double.POSITIVE_INFINITY;

        final List<Entity> collisions = level.getEntities(this, getBoundingBox().expandTowards(getDeltaMovement()));
        for (final Entity entity : collisions) {
            if (entity.isPickable()) {
                final AABB entityBounds = entity.getBoundingBox();
                final Optional<Vec3> hit = entityBounds.clip(start, target);
                if (hit.isPresent()) {
                    final double sqrDistance = start.distanceToSqr(hit.get());
                    if (sqrDistance < bestSqrDistance) {
                        entityHit = entity;
                        entityHitVec = hit.get();
                        bestSqrDistance = sqrDistance;
                    }
                }
            }
        }

        return entityHit != null ? new EntityHitResult(entityHit, entityHitVec) : null;
    }

    private void onBlockCollision(final BlockHitResult hit) {
        final BlockPos pos = hit.getBlockPos();
        final BlockState blockState = level.getBlockState(pos);
        final Block block = blockState.getBlock();

        // Traveling through a portal?
        final BlockEntity tileEntity = level.getBlockEntity(pos);
        if (blockState.is(Blocks.NETHER_PORTAL)) {
            handleInsidePortal(pos);
            return;
        } else if (blockState.is(Blocks.END_GATEWAY)) {
            if (tileEntity instanceof TheEndGatewayBlockEntity endGateway && TheEndGatewayBlockEntity.canEntityTeleport(this)) {
                TheEndGatewayBlockEntity.teleportEntity(level, pos, blockState, this, endGateway);
                return;
            }
        }

        // First things first, we ded.
        discard();

        // Next up, notify receiver, if any.
        if (block instanceof InfraredReceiver) {
            ((InfraredReceiver) block).onInfraredPacket(this, hit);
        }
        onCapabilityProviderCollision(hit, hit.getDirection(), tileEntity);
    }

    private void onEntityCollision(final EntityHitResult hit) {
        // First things first, we ded.
        discard();

        // Next up, notify receiver, if any.
        onCapabilityProviderCollision(hit, null, hit.getEntity());
    }

    private void onCapabilityProviderCollision(final HitResult hit, @Nullable final Direction side, @Nullable final ICapabilityProvider provider) {
        if (provider instanceof InfraredReceiver) {
            ((InfraredReceiver) provider).onInfraredPacket(this, hit);
        } else if (provider != null) {
            final LazyOptional<InfraredReceiver> capability = provider.getCapability(Capabilities.INFRARED_RECEIVER, side);
            capability.ifPresent(receiver -> receiver.onInfraredPacket(this, hit));
        }
    }
}
