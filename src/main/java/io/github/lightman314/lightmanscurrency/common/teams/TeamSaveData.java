package io.github.lightman314.lightmanscurrency.common.teams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.client.data.ClientTeamData;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageRemoveClientTeam;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageSetupClientTeam;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateClientTeam;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

public class TeamSaveData extends PersistentState {

    private long nextID = 0;
    private long getNextID() {
        long id = this.nextID;
        this.nextID++;
        this.markDirty();
        return id;
    }
    private final Map<Long, Team> teams = new HashMap<>();


    private TeamSaveData() {}
    private TeamSaveData(NbtCompound compound) {

        this.nextID = compound.getLong("NextID");

        NbtList teamList = compound.getList("Teams", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < teamList.size(); ++i)
        {
            Team team = Team.load(teamList.getCompound(i));
            if(team != null)
                this.teams.put(team.getID(), team);
        }

    }

    public NbtCompound writeNbt(NbtCompound compound) {

        compound.putLong("NextID", this.nextID);

        NbtList teamList = new NbtList();
        this.teams.forEach((teamID, team) ->{
            if(team != null)
                teamList.add(team.save());
        });
        compound.put("Teams", teamList);

        return compound;
    }

    private static TeamSaveData get() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            ServerWorld level = server.getOverworld();
            if(level != null)
                return level.getPersistentStateManager().getOrCreate(TeamSaveData::new, TeamSaveData::new, "lightmanscurrency_team_data");
        }
        return null;
    }

    public static List<Team> GetAllTeams(boolean isClient)
    {
        if(isClient)
        {
            return ClientTeamData.GetAllTeams();
        }
        else
        {
            TeamSaveData tsd = get();
            if(tsd != null)
                return new ArrayList<>(tsd.teams.values());
            return new ArrayList<>();
        }
    }

    public static Team GetTeam(boolean isClient, long teamID)
    {
        if(isClient)
        {
            return ClientTeamData.GetTeam(teamID);
        }
        else
        {
            TeamSaveData tsd = get();
            if(tsd != null)
            {
                if(tsd.teams.containsKey(teamID))
                    return tsd.teams.get(teamID);
            }
            return null;
        }
    }

    public static void MarkTeamDirty(long teamID)
    {
        TeamSaveData tsd = get();
        if(tsd != null)
        {
            tsd.markDirty();
            //Send update packet to all connected clients
            Team team = GetTeam(false, teamID);
            if(team != null)
            {
                NbtCompound compound = team.save();
                new SMessageUpdateClientTeam(compound).sendToAll();
            }
        }
    }

    public static Team RegisterTeam(PlayerEntity owner, String teamName)
    {
        TeamSaveData tsd = get();
        if(tsd != null)
        {
            long teamID = tsd.getNextID();
            Team newTeam = Team.of(teamID, PlayerReference.of(owner), teamName);
            tsd.teams.put(teamID, newTeam);

            MarkTeamDirty(teamID);

            return newTeam;
        }
        return null;
    }

    public static void RemoveTeam(long teamID)
    {
        TeamSaveData tsd = get();
        if(tsd != null)
        {
            if(tsd.teams.containsKey(teamID))
            {
                tsd.teams.remove(teamID);
                tsd.markDirty();

                //Send update packet to the connected clients
                new SMessageRemoveClientTeam(teamID).sendToAll();
            }
        }
    }

    public static void OnPlayerLogin(ServerPlayerEntity player, PacketSender sender)
    {

        TeamSaveData tsd = get();
        if(tsd != null)
        {
            NbtCompound compound = new NbtCompound();
            NbtList teamList = new NbtList();
            tsd.teams.forEach((id, team) -> teamList.add(team.save()));
            compound.put("Teams", teamList);
            new SMessageSetupClientTeam(compound).sendTo(sender);
        }

    }


}