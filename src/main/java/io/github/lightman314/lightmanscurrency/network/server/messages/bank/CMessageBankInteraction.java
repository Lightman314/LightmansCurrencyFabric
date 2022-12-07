package io.github.lightman314.lightmanscurrency.network.server.messages.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
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
    protected void encode(PacketByteBuf buffer) { buffer.writeBoolean(this.deposit); this.amount.save(new NbtCompound(), CoinValue.DEFAULT_KEY); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof BankAccount.IBankAccountMenu menu)
        {
            boolean isDeposit = buffer.readBoolean();
            CoinValue amount = new CoinValue(buffer.readNbt());
            if(isDeposit)
                BankAccount.DepositCoins(menu, amount);
            else
                BankAccount.WithdrawCoins(menu,amount);
            menu.onDepositOrWithdraw();
        }
    }



}
