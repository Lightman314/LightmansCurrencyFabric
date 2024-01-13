package io.github.lightman314.lightmanscurrency.network.server.messages.persistentdata;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.ClientToServerPacket;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class CMessageAddPersistentAuction extends ClientToServerPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "persistentdata_auction");

    private static final String GENERATE_ID_FORMAT = "auction_";

    private final NbtCompound auctionData;
    private final String id;
    public CMessageAddPersistentAuction(NbtCompound auctionData, String id) { super(PACKET_ID); this.auctionData = auctionData; this.id = id; }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        dataBuilder.setCompound("auction", this.auctionData)
                .setString("id", this.id);
    }

    private static JsonObject getAuctionJson(NbtCompound auctionData, String id) {
        AuctionTradeData auction = new AuctionTradeData(auctionData);
        JsonObject json = new JsonObject();
        json.addProperty("ID", id);
        json = auction.saveToJson(json);
        return json;
    }

    public static void handle(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        if(CommandLCAdmin.isAdminPlayer(player))
        {
            NbtCompound auctionNbt = data.getCompound("auction");
            String id = data.getString("id");

            boolean generateID = id.isBlank();
            if(!generateID) {

                JsonObject auctionJson = getAuctionJson(auctionNbt, id);

                JsonArray persistentAuctions = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_AUCTION_SECTION);
                for(int i = 0; i < persistentAuctions.size(); ++i)
                {
                    JsonObject auctionData = persistentAuctions.get(i).getAsJsonObject();
                    if(auctionData.has("ID") && auctionData.get("ID").getAsString().equals(id) || auctionData.has("id") && auctionData.get("id").getAsString().equals(id))
                    {
                        //Overwrite the existing entry with the same id.
                        persistentAuctions.set(i, auctionJson);
                        TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
                        player.sendMessage(Text.translatable("lightmanscurrency.message.persistent.auction.overwrite", id));
                        return;
                    }
                }

                //If no trader found with the id, add to list
                persistentAuctions.add(auctionJson);
                TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
                player.sendMessage(Text.translatable("lightmanscurrency.message.persistent.auction.add", id));
                return;
            }
            else
            {
                //Get a list of all known trader IDs
                List<String> knownIDs = new ArrayList<>();
                JsonArray persistentAuctions = TraderSaveData.getPersistentTraderJson(TraderSaveData.PERSISTENT_AUCTION_SECTION);
                for(int i = 0; i < persistentAuctions.size(); ++i)
                {
                    JsonObject auctionData = persistentAuctions.get(i).getAsJsonObject();
                    if(auctionData.has("ID"))
                        knownIDs.add(auctionData.get("ID").getAsString());
                    else if(auctionData.has("id"))
                        knownIDs.add(auctionData.get("id").getAsString());
                }

                //Check auction_1 -> auction_2147483646 to find an available id
                for(int i = 1; i < Integer.MAX_VALUE; ++i)
                {
                    String genID = GENERATE_ID_FORMAT + String.valueOf(i);
                    if(knownIDs.stream().noneMatch(kid -> kid.equals(genID)))
                    {
                        persistentAuctions.add(getAuctionJson(auctionNbt, genID));
                        TraderSaveData.setPersistentTraderSection(TraderSaveData.PERSISTENT_AUCTION_SECTION, persistentAuctions);
                        player.sendMessage(Text.translatable("lightmanscurrency.message.persistent.auction.add", genID));
                        return;
                    }
                }
                LightmansCurrency.LogError("Could not generate ID, as all auction_# ID's are somehow spoken for.");
            }
        }
    }


}
