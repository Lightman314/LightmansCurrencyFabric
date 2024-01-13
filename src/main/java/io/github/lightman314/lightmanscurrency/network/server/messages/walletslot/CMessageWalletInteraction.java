package io.github.lightman314.lightmanscurrency.network.server.messages.walletslot;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageWalletInteraction extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "wallet_slot_interaction");

    private final int clickedSlot;
    private final boolean shiftHeld;
    private final ItemStack heldStack;
    public CMessageWalletInteraction(int clickedSlot, boolean shiftHeld, ItemStack heldStack) { super(PACKET_ID); this.clickedSlot = clickedSlot; this.shiftHeld = shiftHeld; this.heldStack = heldStack; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setInt("slot", this.clickedSlot)
                .setBoolean("shift", this.shiftHeld)
                .setCompound("heldStack", this.heldStack.writeNbt(new NbtCompound()));
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        WalletHandler.WalletSlotInteraction(player, data.getInt("slot"), data.getBoolean("shift"), ItemStack.fromNbt(data.getCompound("heldStack")));
    }

}
