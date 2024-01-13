package io.github.lightman314.lightmanscurrency.network.client.messages.data;

import io.github.lightman314.lightmanscurrency.client.data.ClientTeamData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
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

import java.util.ArrayList;
import java.util.List;

public class SMessageSetupClientTeam extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "datasync_team_setup");

    private final NbtCompound data;
    public SMessageSetupClientTeam(NbtCompound data) { super(PACKET_ID); this.data = data; }

    @Override
    public void encode(LazyPacketData.Builder dataBuilder) { dataBuilder.setCompound("data", this.data); }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        List<Team> teams = new ArrayList<>();
        NbtList teamList = data.getCompound("data").getList("Teams", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < teamList.size(); ++i)
        {
            Team team = Team.load(teamList.getCompound(i));
            if(team != null)
                teams.add(team);
        }
        ClientTeamData.InitTeams(teams);
    }

}
