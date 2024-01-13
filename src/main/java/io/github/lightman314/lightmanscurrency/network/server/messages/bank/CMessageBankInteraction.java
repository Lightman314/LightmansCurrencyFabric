package io.github.lightman314.lightmanscurrency.network.server.messages.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageBankInteraction extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "bank_menu_interaction");

    private final boolean deposit;
    private final CoinValue amount;
    public CMessageBankInteraction(boolean deposit, CoinValue amount) { super(PACKET_ID); this.deposit = deposit; this.amount = amount; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setBoolean("deposit", this.deposit)
                .setCoinValue("amount", this.amount);
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof BankAccount.IBankAccountMenu menu)
        {
            boolean isDeposit = data.getBoolean("deposit");
            CoinValue amount = data.getCoinValue("amount");
            if(isDeposit)
                BankAccount.DepositCoins(menu, amount);
            else
                BankAccount.WithdrawCoins(menu,amount);
            menu.onDepositOrWithdraw();
        }
    }



}
