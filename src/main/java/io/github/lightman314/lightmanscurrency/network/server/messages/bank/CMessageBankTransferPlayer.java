package io.github.lightman314.lightmanscurrency.network.server.messages.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.messages.bank.SMessageBankTransferResponse;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
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
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setString("player", this.playerName)
                .setCoinValue("amount", this.amount);
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof BankAccount.IBankAccountAdvancedMenu menu)
        {
            BankAccount.AccountReference destination = BankAccount.GenerateReference(false, PlayerReference.of(false, data.getString("player")));
            MutableText response = BankAccount.TransferCoins(menu, data.getCoinValue("amount"), destination);
            new SMessageBankTransferResponse(response).sendTo(responseSender);
        }
    }

}
