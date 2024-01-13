package io.github.lightman314.lightmanscurrency.network.server.messages.persistentdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CMessageAddPersistentTrader extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "persistentdata_trader");
    private static final String GENERATE_ID_FORMAT = "trader_";

    private final long traderID;
    private final String id;
    private final String owner;
    public CMessageAddPersistentTrader(long traderID, String id, String owner) { super(PACKET_ID); this.traderID = traderID; this.id = id; this.owner = owner; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setLong("trader", this.traderID)
                .setString("id", this.id)
                .setString("owner", this.owner);
    }

    private static JsonObject getTraderJson(TraderData trader, String id, String owner) throws Exception {
        JsonObject traderJson = trader.saveToJson();
        traderJson.addProperty("ID", id);
        traderJson.addProperty("OwnerName", owner);
        return traderJson;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(CommandLCAdmin.isAdminPlayer(player))
        {
            TraderData trader = TraderSaveData.GetTrader(false, data.getLong("trader"));
            if(trader != null && trader.canMakePersistent())
            {
                String id = data.getString("id");
                String owner = data.getString("owner");

                boolean generateID = id.isBlank();

                if(!generateID)
                {
                    try {
                        JsonObject traderJson = getTraderJson(trader, id, owner);

                        JsonArray persistentTraders = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_TRADER_SECTION);
                        //Check for traders with the same id, and replace any entries that match
                        for(int i = 0; i < persistentTraders.size(); ++i)
                        {
                            JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
                            if(traderData.has("ID") && traderData.get("ID").getAsString().equals(id) || traderData.has("id") && traderData.get("id").getAsString().equals(id))
                            {
                                //Overwrite the existing entry with the same id.
                                persistentTraders.set(i, traderJson);
                                TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
                                player.sendMessage(Text.translatable("lightmanscurrency.message.persistent.trader.overwrite", id));
                                return;
                            }
                        }

                        //If no trader found with the id, add to list
                        persistentTraders.add(traderJson);
                        TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
                        player.sendMessage(Text.translatable("lightmanscurrency.message.persistent.trader.add", id));
                        return;
                    } catch (Throwable t) { t.printStackTrace(); }
                }
                else
                {
                    try {
                        //Get a list of all known trader IDs
                        List<String> knownIDs = new ArrayList<>();
                        JsonArray persistentTraders = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_TRADER_SECTION);
                        for(int i = 0; i < persistentTraders.size(); ++i)
                        {
                            JsonObject traderData = persistentTraders.get(i).getAsJsonObject();
                            if(traderData.has("id"))
                                knownIDs.add(traderData.get("id").getAsString());
                            if(traderData.has("ID"))
                                knownIDs.add(traderData.get("ID").getAsString());
                        }

                        //Check trader_1 -> trader_2147483646 to find an available id
                        for(int i = 1; i < Integer.MAX_VALUE; ++i)
                        {
                            String genID = GENERATE_ID_FORMAT + String.valueOf(i);
                            if(knownIDs.stream().noneMatch(ID -> ID.equals(genID)))
                            {
                                persistentTraders.add(getTraderJson(trader, genID, owner));
                                TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_TRADER_SECTION, persistentTraders);
                                player.sendMessage(Text.translatable("lightmanscurrency.message.persistent.trader.add", genID));
                                return;
                            }
                        }
                        LightmansCurrency.LogError("Could not generate ID, as all trader_# ID's are somehow spoken for.");
                    } catch(Throwable t) { t.printStackTrace(); }
                }
            }
        }

    }
}
