package io.github.lightman314.lightmanscurrency.common.ownership;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PlayerReference {

    public final UUID id;
    private boolean forceName = false;
    private String name = "";
    public String getName(boolean isClient)
    {
        if(isClient || this.forceName)
            return this.name;
        else
        {
            String n = getPlayerName(this.id);
            if(n == null || n.isBlank())
                return this.name;
            return n;
        }
    }
    public MutableText getNameComponent(boolean isClient) { return Text.literal(this.getName(isClient)); }

    private PlayerReference(UUID playerID, String name)
    {
        this.id = playerID;
        this.name = name;
    }

    /**
     * Used to run an action/interaction under a team's name.
     */
    public PlayerReference copyWithName(String name) {
        PlayerReference copy = new PlayerReference(this.id, name);
        copy.forceName = true;
        return copy;
    }

    public boolean is(PlayerReference player)
    {
        if(player == null)
            return false;
        return is(player.id);
    }

    public boolean is(GameProfile profile)
    {
        return is(profile.getId());
    }

    public boolean is(UUID entityID)
    {
        if(entityID == null)
            return false;
        return entityID.equals(this.id);
    }

    public boolean is(Entity entity)
    {
        if(entity == null)
            return false;
        return entity.getUuid().equals(this.id);
    }

    public boolean isOnline()
    {
        return this.getPlayer() != null;
    }

    public PlayerEntity getPlayer() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
            return server.getPlayerManager().getPlayer(this.id);
        return null;
    }

    public NbtCompound save()
    {
        NbtCompound compound = new NbtCompound();
        compound.putUuid("ID", this.id);
        compound.putString("Name", this.getName(false));
        if(this.forceName)
            compound.putBoolean("ForcedName", this.forceName);
        return compound;
    }

    public JsonObject saveAsJson() {
        JsonObject json = new JsonObject();
        json.addProperty("ID", this.id.toString());
        json.addProperty("Name", this.getName(false));
        return json;
    }

    public static PlayerReference load(NbtCompound compound)
    {
        try {
            UUID id = compound.getUuid("ID");
            String name = compound.getString("Name");
            PlayerReference pr = of(id, name);
            if(compound.contains("ForcedName"))
                pr.forceName = compound.getBoolean("ForcedName");
            return pr;
        } catch(Exception e) { LightmansCurrency.LogError("Error loading PlayerReference from tag.", e); return null; }
    }

    public static PlayerReference load(JsonElement json) {
        try {
            if(json.isJsonPrimitive() && json.getAsJsonPrimitive().isString())
                return PlayerReference.of(false, json.getAsString());
            JsonObject j = json.getAsJsonObject();
            UUID id = UUID.fromString(j.get("ID").getAsString());
            String name = j.get("Name").getAsString();
            return of(id, name);
        } catch(Exception e) {LightmansCurrency.LogError("Error loading PlayerReference from JsonObject", e); return null; }
    }

    public static void saveList(NbtCompound compound, List<PlayerReference> playerList, String tag)
    {
        NbtList list = new NbtList();
        for(int i = 0; i < playerList.size(); ++i)
        {
            NbtCompound thisCompound = playerList.get(i).save();
            list.add(thisCompound);
        }
        compound.put(tag, list);
    }

    public static List<PlayerReference> loadList(NbtCompound compound, String tag)
    {
        List<PlayerReference> playerList = Lists.newArrayList();
        NbtList list = compound.getList(tag, NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < list.size(); ++i)
        {
            NbtCompound thisCompound = list.getCompound(i);
            PlayerReference player = load(thisCompound);
            if(player != null)
                playerList.add(player);
        }
        return playerList;
    }

    public static PlayerReference of(UUID playerID, String name)
    {
        return new PlayerReference(playerID, name);
    }

    public static PlayerReference of(GameProfile playerProfile)
    {
        if(playerProfile == null)
            return null;
        return of(playerProfile.getId(), playerProfile.getName());
    }

    public static PlayerReference of(Entity entity)
    {
        if(entity instanceof PlayerEntity)
            return of((PlayerEntity)entity);
        return null;
    }

    public static PlayerReference of(PlayerEntity player)
    {
        if(player == null)
            return null;
        return of(player.getGameProfile());
    }

    public static PlayerReference of(boolean isClient, String playerName)
    {
        if(playerName.isBlank() || isClient)
            return null;
        UUID playerID = getPlayerID(playerName);
        if(playerID != null)
            return of(playerID, playerName);
        return null;
    }

    public static boolean listContains(List<PlayerReference> list, PlayerReference entry) { if(entry != null) return listContains(list, entry.id); return false; }

    public static boolean listContains(List<PlayerReference> list, UUID id)
    {
        for(PlayerReference player : list)
        {
            if(player.is(id))
                return true;
        }
        return false;
    }

    public static boolean removeFromList(List<PlayerReference> list, PlayerReference entry) { if(entry != null) return removeFromList(list, entry.id); return false; }

    public static boolean removeFromList(List<PlayerReference> list, UUID id)
    {
        for(int i = 0; i < list.size(); ++i)
        {
            if(list.get(i).is(id))
            {
                list.remove(i);
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() { return this.id.hashCode(); }

    /**
     * Only run on server.
     */
    public static String getPlayerName(UUID playerID)
    {
        try {
            MinecraftServer server = ServerHook.getServer();
            if(server != null)
            {
                GameProfile profile = server.getUserCache().getByUuid(playerID).orElse(null);
                if(profile != null)
                    return profile.getName();
            }
        } catch(Throwable t) { LightmansCurrency.LogError("Error getting player name.", t); }
        return null;
    }

    /**
     * Only run on server.
     */
    public static UUID getPlayerID(String playerName)
    {
        playerName = playerName.toLowerCase();
        try {
            MinecraftServer server = ServerHook.getServer();
            if(server != null)
            {
                GameProfile profile = server.getUserCache().findByName(playerName).orElse(null);
                if(profile != null)
                    return profile.getId();
            }

        } catch(Throwable t) { LightmansCurrency.LogError("Error getting player ID from name.", t); }
        return null;
    }

}