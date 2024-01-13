package io.github.lightman314.lightmanscurrency.network.client.messages.data;

import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SMessageSetupClientBank extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "datasync_bankaccount_setup");

    private final NbtCompound data;
    public SMessageSetupClientBank(NbtCompound data) { super(PACKET_ID); this.data = data; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setCompound("data", this.data); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        Map<UUID, BankAccount> bankAccounts = new HashMap<>();
        NbtList accountList = data.getCompound("data").getList("BankAccounts", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < accountList.size(); ++i)
        {
            NbtCompound compound = accountList.getCompound(i);
            UUID player = compound.getUuid("Player");
            bankAccounts.put(player, new BankAccount(compound));
        }
        ClientBankData.InitBankAccounts(bankAccounts);
    }
}
