package io.github.lightman314.lightmanscurrency.network.client.messages.team;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;

public class SMessageCreateTeamResponse extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "team_create_reponse");

    private final long teamID;
    public SMessageCreateTeamResponse(long teamID) { super(PACKET_ID); this.teamID = teamID; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setLong("team",this.teamID); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(client.currentScreen instanceof TeamManagerScreen screen)
            screen.setActiveTeam(data.getLong("team"));
    }

}
