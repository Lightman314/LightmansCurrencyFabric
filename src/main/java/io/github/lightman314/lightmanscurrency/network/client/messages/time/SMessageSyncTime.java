package io.github.lightman314.lightmanscurrency.network.client.messages.time;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageSyncTime extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "team_create_reponse");

    private final long currentTime;
    private SMessageSyncTime(long currentTime) { super(PACKET_ID); this.currentTime = currentTime; }

    public static SMessageSyncTime CreatePacket() { return new SMessageSyncTime(System.currentTimeMillis()); }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setLong("time", this.currentTime); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        long timeOffset = data.getLong("time") - System.currentTimeMillis();
        //Round the time offset to the nearest second
        timeOffset = (timeOffset / 1000) * 1000;
        if(timeOffset < 10000) //Ignore offset if less than 10s, as it's likely due to ping
            timeOffset = 0;
        //Define the time offset
        TimeUtil.setTimeDesync(timeOffset);
    }

}
