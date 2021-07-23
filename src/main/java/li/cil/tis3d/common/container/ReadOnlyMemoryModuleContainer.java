package li.cil.tis3d.common.container;

import li.cil.tis3d.common.item.Items;
import li.cil.tis3d.common.item.ReadOnlyMemoryModuleItem;
import li.cil.tis3d.common.network.Network;
import li.cil.tis3d.common.network.message.ServerReadOnlyMemoryModuleDataMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.fmllegacy.network.PacketDistributor;

import java.util.Arrays;

public final class ReadOnlyMemoryModuleContainer extends AbstractContainerMenu {
    public static ReadOnlyMemoryModuleContainer create(final int id, final Inventory playerInventory, final FriendlyByteBuf data) {
        final InteractionHand hand = data.readEnum(InteractionHand.class);
        return new ReadOnlyMemoryModuleContainer(id, playerInventory.player, hand);
    }

    // --------------------------------------------------------------------- //

    private final Player player;
    private final InteractionHand hand;

    private byte[] lastSentData;

    public ReadOnlyMemoryModuleContainer(final int id, final Player player, final InteractionHand hand) {
        super(Containers.READ_ONLY_MEMORY_MODULE.get(), id);
        this.player = player;
        this.hand = hand;
    }

    public InteractionHand getHand() {
        return hand;
    }

    // --------------------------------------------------------------------- //
    // Container

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();

        if (player instanceof final ServerPlayer serverPlayer) {
            final byte[] data = ReadOnlyMemoryModuleItem.loadFromStack(player.getItemInHand(hand));
            if (!Arrays.equals(data, lastSentData)) {
                lastSentData = data;
                final ServerReadOnlyMemoryModuleDataMessage message = new ServerReadOnlyMemoryModuleDataMessage(hand, data);
                Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), message);
            }
        }
    }

    @Override
    public boolean stillValid(final Player player) {
        return Items.is(player.getItemInHand(hand), Items.READ_ONLY_MEMORY_MODULE);
    }
}
