package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;

public class ClientTeamData {

    private static final Map<Long,Team> loadedTeams = new HashMap<>();

    public static List<Team> GetAllTeams()
    {
        return new ArrayList<>(loadedTeams.values());
    }

    public static Team GetTeam(long teamID)
    {
        if(loadedTeams.containsKey(teamID))
            return loadedTeams.get(teamID);
        return null;
    }

    public static void InitTeams(List<Team> teams)
    {
        loadedTeams.clear();
        teams.forEach(team -> loadedTeams.put(team.getID(), team));
    }

    public static void UpdateTeam(NbtCompound compound)
    {
        Team updatedTeam = Team.load(compound);
        loadedTeams.put(updatedTeam.getID(), updatedTeam);
    }

    public static void RemoveTeam(long teamID)
    {
            loadedTeams.remove(teamID);
    }

    public static void onClientLogout(ClientPlayNetworkHandler handler, MinecraftClient client) {
        loadedTeams.clear();
    }
}