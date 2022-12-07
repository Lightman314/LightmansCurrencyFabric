package io.github.lightman314.lightmanscurrency.network.server.messages.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankSaveData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageSelectBankAccount extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "bank_select_account");

    private final BankAccount.AccountReference account;
    public CMessageSelectBankAccount(BankAccount.AccountReference account) { super(PACKET_ID); this.account = account; }

    @Override
    protected void encode(PacketByteBuf buffer) { this.account.writeToBuffer(buffer); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        BankSaveData.SetSelectedBankAccount(player, BankAccount.LoadReference(false, buffer));
    }

}
