package io.github.lightman314.lightmanscurrency.network.server.messages.coinmint;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageMintCoin extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "coinmint_mint");

    private final boolean fullStack;
    public CMessageMintCoin(boolean fullStack) { super(PACKET_ID); this.fullStack = fullStack; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setBoolean("fullStack", this.fullStack); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(player.currentScreenHandler instanceof MintMenu menu)
            menu.coinMint.mintCoins(data.getBoolean("fullStack") ? 64 : 1);
    }

}
