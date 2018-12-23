package li.cil.tis3d.common.gui;

import li.cil.tis3d.common.init.Items;
import li.cil.tis3d.common.item.ItemModuleReadOnlyMemory;
import li.cil.tis3d.common.network.message.MessageModuleReadOnlyMemoryData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import li.cil.tis3d.charset.PacketRegistry;

/**
 * GUI handler for the client side - which is, still, all we need.
 */
public class GuiHandlerCommon {
    public enum GuiId {
        BOOK_MANUAL,
        BOOK_CODE,
        MODULE_TERMINAL,
        MODULE_MEMORY;

        public static final GuiId[] VALUES = values();
    }

    public static void sendModuleMemory(final PlayerEntity player) {
        if (!(player instanceof ServerPlayerEntity)) {
            return;
        }

        final ItemStack heldItem = player.getStackInHand(Hand.MAIN);
        if (!Items.isModuleReadOnlyMemory(heldItem)) {
            return;
        }

        MessageModuleReadOnlyMemoryData data = new MessageModuleReadOnlyMemoryData(ItemModuleReadOnlyMemory.loadFromStack(heldItem));
        ((ServerPlayerEntity) player).networkHandler.sendPacket(PacketRegistry.SERVER.wrap(data));
    }
}