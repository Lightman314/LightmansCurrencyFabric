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

public class CMessageSetBankNotificationLevel extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "bank_set_notificationlevel");

    private final CoinValue amount;
    public CMessageSetBankNotificationLevel(CoinValue amount) { super(PACKET_ID); this.amount = amount; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeNbt(this.amount.save(new NbtCompound(), CoinValue.DEFAULT_KEY)); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof BankAccount.IBankAccountAdvancedMenu menu)
        {
            menu.setNotificationLevel(new CoinValue(buffer.readUnlimitedNbt()));
        }
    }
}
