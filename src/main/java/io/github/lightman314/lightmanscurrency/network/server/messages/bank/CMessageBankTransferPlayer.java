package io.github.lightman314.lightmanscurrency.network.server.messages.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.client.messages.bank.SMessageBankTransferResponse;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class CMessageBankTransferPlayer extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "bank_transfer_player");

    private final String playerName;
    private final CoinValue amount;
    public CMessageBankTransferPlayer(String playerName, CoinValue amount) { super(PACKET_ID); this.playerName = playerName; this.amount = amount; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeString(this.playerName); buffer.writeNbt(amount.save(new NbtCompound(), CoinValue.DEFAULT_KEY)); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof BankAccount.IBankAccountAdvancedMenu menu)
        {
            BankAccount.AccountReference destination = BankAccount.GenerateReference(false, PlayerReference.of(false, buffer.readString()));
            MutableText response = BankAccount.TransferCoins(menu, new CoinValue(buffer.readUnlimitedNbt()), destination);
            new SMessageBankTransferResponse(response).sendTo(responseSender);
        }
    }

}
