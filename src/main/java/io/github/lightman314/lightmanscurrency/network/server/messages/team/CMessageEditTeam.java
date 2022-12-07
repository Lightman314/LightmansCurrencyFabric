package io.github.lightman314.lightmanscurrency.network.server.messages.team;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class CMessageEditTeam extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "team_edit_members");

    private final long teamID;
    private final String playerName;
    private final String category;
    public CMessageEditTeam(long teamID, String playerName, String category) { super(PACKET_ID); this.teamID = teamID; this.playerName = playerName; this.category = category; }

    @Override
    protected void encode(PacketByteBuf buffer) { buffer.writeLong(this.teamID); buffer.writeString(this.playerName); buffer.writeString(this.category); }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buffer, PacketSender responseSender) {
        Team team = TeamSaveData.GetTeam(false, buffer.readLong());
        if(team != null)
            team.changeAny(player, buffer.readString(), buffer.readString());
    }
}
