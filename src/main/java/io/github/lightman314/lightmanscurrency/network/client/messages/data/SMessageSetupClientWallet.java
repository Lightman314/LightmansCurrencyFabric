package io.github.lightman314.lightmanscurrency.network.client.messages.data;

import io.github.lightman314.lightmanscurrency.client.data.ClientWalletData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SMessageSetupClientWallet extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "datasync_wallet_setup");

    private final NbtCompound data;
    public SMessageSetupClientWallet(NbtCompound data) { super(PACKET_ID); this.data = data; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeNbt(this.data); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        Map<UUID, WalletHandler> walletData = new HashMap<>();
        NbtList walletList = buffer.readNbt().getList("PlayerWalletData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < walletList.size(); ++i)
        {
            NbtCompound tag = walletList.getCompound(i);
            UUID playerID = tag.getUuid("Player");
            WalletHandler walletHandler = new WalletHandler();
            walletHandler.load(tag.getCompound("WalletData"));
            walletData.put(playerID, walletHandler);
        }
        ClientWalletData.InitializeWallets(walletData);
    }

}
