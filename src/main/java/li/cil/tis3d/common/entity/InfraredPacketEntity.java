package li.cil.tis3d.common.entity;

import li.cil.tis3d.api.infrared.InfraredPacket;
import li.cil.tis3d.api.infrared.InfraredReceiver;
import li.cil.tis3d.common.capabilities.Capabilities;
import li.cil.tis3d.common.event.InfraredPacketTickHandler;
import li.cil.tis3d.common.module.InfraredModule;
import li.cil.tis3d.util.Raytracing;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.EndGatewayTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.NetworkHooks;

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
    private static final DataParameter<Integer> DATA_VALUE = EntityDataManager.defineId(InfraredPacketEntity.class, DataSerializers.INT);

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

    public InfraredPacketEntity(final EntityType<?> type, final World world) {
        super(type, world);
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
    public void configure(final Vector3d start, final Vector3d direction, final short value) {
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
            remove();
        }
    }

    /**
     * Remove flag that the entity is dead; used to revive it when being redirected.
     */
    @Override
    public void revive() {
        super.revive();
        if (!getCommandSenderWorld().isClientSide()) {
            InfraredPacketTickHandler.watchPacket(this);
        }
    }

    // --------------------------------------------------------------------- //

    @Override
    protected void defineSynchedData() {
        getEntityData().define(DATA_VALUE, 0);
        if (!getCommandSenderWorld().isClientSide()) {
            InfraredPacketTickHandler.watchPacket(this);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (!getCommandSenderWorld().isClientSide()) {
            InfraredPacketTickHandler.unwatchPacket(this);
        }
    }

    @Override
    protected void readAdditionalSaveData(final CompoundNBT tag) {
        lifetime = tag.getInt(TAG_LIFETIME);
        value = tag.getShort(TAG_VALUE);
    }

    @Override
    protected void addAdditionalSaveData(final CompoundNBT tag) {
        tag.putInt(TAG_LIFETIME, lifetime);
        tag.putShort(TAG_VALUE, value);
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
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
        final RayTraceResult hit = checkCollisions();

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
    public Vector3d getPacketPosition() {
        return position();
    }

    @Override
    public Vector3d getPacketDirection() {
        return getDeltaMovement().normalize();
    }

    @Override
    public void redirectPacket(final Vector3d position, final Vector3d direction, final int addedLifetime) {
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

    private void setPositionAndUpdateBounds(final Vector3d pos) {
        setPos(pos.x, pos.y, pos.z);
    }

    private void emitParticles(@Nullable final RayTraceResult hit) {
        final World world = getCommandSenderWorld();
        if (!(world instanceof ServerWorld)) {
            // Entities regularly die too quickly for the client to have a
            // chance to simulate them, so we trigger the particles from
            // the server. Kinda meh, but whatever works.
            return;
        }

        // Spawn particle effect somewhere between current position and either the
        // position where the packet collided with something, or along the movement
        // direction (i.e. where the packet will be next).
        final ServerWorld serverWorld = (ServerWorld) world;
        final double t = random.nextDouble();
        final Vector3d delta = hit == null ? getDeltaMovement() : hit.getLocation().subtract(position());
        final Vector3d pos = position().add(delta.scale(t));
        serverWorld.sendParticles(RedstoneParticleData.REDSTONE, pos.x, pos.y, pos.z, 1, 0, 0, 0, 0);
    }

    @Nullable
    private RayTraceResult checkCollisions() {
        final RayTraceResult hit = checkCollision();
        if (hit instanceof BlockRayTraceResult) {
            onBlockCollision((BlockRayTraceResult) hit);
        } else if (hit instanceof EntityRayTraceResult) {
            onEntityCollision((EntityRayTraceResult) hit);
        }
        return hit;
    }

    @Nullable
    private RayTraceResult checkCollision() {
        final World world = getCommandSenderWorld();
        final Vector3d start = position();
        final Vector3d target = start.add(getDeltaMovement());

        // Check for block collisions.
        final RayTraceResult blockHit = Raytracing.raytrace(world, start, target, Raytracing::intersectIgnoringTransparent);

        // Check for entity collisions.
        final RayTraceResult entityHit = checkEntityCollision(world, start, target);

        // If we have both, pick the closer one.
        if (blockHit != null && blockHit.getType() != RayTraceResult.Type.MISS &&
            entityHit != null && entityHit.getType() != RayTraceResult.Type.MISS) {
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
    private RayTraceResult checkEntityCollision(final World world, final Vector3d start, final Vector3d target) {
        Entity entityHit = null;
        Vector3d entityHitVec = null;
        double bestSqrDistance = Double.POSITIVE_INFINITY;

        final List<Entity> collisions = world.getEntities(this, getBoundingBox().expandTowards(getDeltaMovement()));
        for (final Entity entity : collisions) {
            if (entity.canBeCollidedWith()) {
                final AxisAlignedBB entityBounds = entity.getBoundingBox();
                final Optional<Vector3d> hit = entityBounds.clip(start, target);
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

        return entityHit != null ? new EntityRayTraceResult(entityHit, entityHitVec) : null;
    }

    private void onBlockCollision(final BlockRayTraceResult hit) {
        final World world = level;

        final BlockPos pos = hit.getBlockPos();
        final BlockState blockState = world.getBlockState(pos);
        final Block block = blockState.getBlock();

        // Traveling through a portal?
        final TileEntity tileEntity = world.getBlockEntity(pos);
        if (blockState.is(Blocks.NETHER_PORTAL)) {
            handleInsidePortal(pos);
            return;
        } else if (blockState.is(Blocks.END_GATEWAY)) {
            if (tileEntity instanceof EndGatewayTileEntity && EndGatewayTileEntity.canEntityTeleport(this)) {
                ((EndGatewayTileEntity) tileEntity).teleportEntity(this);
                return;
            }
        }

        // First things first, we ded.
        remove();

        // Next up, notify receiver, if any.
        if (block instanceof InfraredReceiver) {
            ((InfraredReceiver) block).onInfraredPacket(this, hit);
        }
        onCapabilityProviderCollision(hit, hit.getDirection(), tileEntity);
    }

    private void onEntityCollision(final EntityRayTraceResult hit) {
        // First things first, we ded.
        remove();

        // Next up, notify receiver, if any.
        onCapabilityProviderCollision(hit, null, hit.getEntity());
    }

    private void onCapabilityProviderCollision(final RayTraceResult hit, @Nullable final Direction side, @Nullable final ICapabilityProvider provider) {
        if (provider instanceof InfraredReceiver) {
            ((InfraredReceiver) provider).onInfraredPacket(this, hit);
        } else if (provider != null) {
            final LazyOptional<InfraredReceiver> capability = provider.getCapability(Capabilities.INFRARED_RECEIVER, side);
            capability.ifPresent(receiver -> receiver.onInfraredPacket(this, hit));
        }
    }
}
