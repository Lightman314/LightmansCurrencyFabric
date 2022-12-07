package io.github.lightman314.lightmanscurrency.client.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;

@Environment(EnvType.CLIENT)
public class ClientTraderData {

    private static final Map<Long, TraderData> loadedTraders = new HashMap<>();

    public static List<TraderData> GetAllTraders() { return new ArrayList<>(loadedTraders.values()); }

    public static TraderData GetTrader(long traderID) {
        if(loadedTraders.containsKey(traderID))
            return loadedTraders.get(traderID);
        return null;
    }

    public static void ClearTraders() { loadedTraders.clear(); }

    public static void UpdateTrader(NbtCompound compound)
    {
        long traderID = compound.getLong("ID");
        if(loadedTraders.containsKey(traderID))
        {
            loadedTraders.get(traderID).load(compound);
        }
        else
        {
            TraderData trader = TraderData.Deserialize(true, compound);
            if(trader != null)
                loadedTraders.put(traderID, trader);
        }
    }

    public static void RemoveTrader(long traderID) { loadedTraders.remove(traderID); }

    public static void onClientLogout(ClientPlayNetworkHandler handler, MinecraftClient client) {
        //Reset loaded traders
        ClearTraders();
    }

}