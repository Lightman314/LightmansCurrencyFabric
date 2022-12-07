package io.github.lightman314.lightmanscurrency.network.server.messages.team;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.client.messages.team.SMessageCreateTeamResponse;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageCreateTeam extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "team_create");

    private final String teamName;
    public CMessageCreateTeam(String teamName) { super(PACKET_ID); this.teamName = teamName; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeString(this.teamName); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        Team newTeam = TeamSaveData.RegisterTeam(player, buffer.readString());
        if(newTeam != null)
            new SMessageCreateTeamResponse(newTeam.getID()).sendTo(responseSender);
    }
}
