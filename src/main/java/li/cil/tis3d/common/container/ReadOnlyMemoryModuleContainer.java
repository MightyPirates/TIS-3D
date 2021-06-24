package li.cil.tis3d.common.container;

import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.ServerReadOnlyMemoryModuleDataMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.Arrays;

public final class ReadOnlyMemoryModuleContainer extends Container {
    public static ReadOnlyMemoryModuleContainer create(final int id, final PlayerInventory playerInventory, final PacketBuffer data) {
        final Hand hand = data.readEnum(Hand.class);
        return new ReadOnlyMemoryModuleContainer(id, playerInventory.player, hand);
    }

    // --------------------------------------------------------------------- //

    private final PlayerEntity player;
    private final Hand hand;

    private byte[] lastSentData;

    public ReadOnlyMemoryModuleContainer(final int id, final PlayerEntity player, final Hand hand) {
        super(Containers.READ_ONLY_MEMORY_MODULE.get(), id);
        this.player = player;
        this.hand = hand;
    }

    public Hand getHand() {
        return hand;
    }

    // --------------------------------------------------------------------- //
    // Container

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (player instanceof ServerPlayerEntity) {
            final ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            final byte[] data = ItemModuleReadOnlyMemory.loadFromStack(player.getItemInHand(hand));
            if (!Arrays.equals(data, lastSentData)) {
                lastSentData = data;
                final ServerReadOnlyMemoryModuleDataMessage message = new ServerReadOnlyMemoryModuleDataMessage(hand, data);
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
            }
        }
    }

    @Override
    public boolean stillValid(final PlayerEntity player) {
        return Items.is(player.getItemInHand(hand), Items.READ_ONLY_MEMORY_MODULE);
    }
}
