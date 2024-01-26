package io.github.lightman314.lightmanscurrency.common.traders;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.data.ClientTraderData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.PersistentAuctionData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageClearClientTraders;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageRemoveClientTrader;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateClientTrader;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TraderSaveData extends PersistentState {

    public static final String PERSISTENT_TRADER_FILENAME = "config/lightmanscurrency/PersistentTraders.json";

    public static final String PERSISTENT_TRADER_SECTION = "Traders";
    public static final String PERSISTENT_AUCTION_SECTION = "Auctions";

    private void validateAuctionHouse() {
        if(!LCConfig.SERVER.auctionHouseEnabled.get())
        {
            LightmansCurrency.LogInfo("Will not create or validate the auction house as the auction house is disabled.");
            return;
        }
        AtomicBoolean hasAuctionHouse = new AtomicBoolean(false);
        this.traderData.forEach((id,data) -> {
            if(data instanceof AuctionHouseTrader)
                hasAuctionHouse.set(true);
        });
        if(!hasAuctionHouse.get())
        {
            //Create the auction house manually
            AuctionHouseTrader ah = new AuctionHouseTrader();
            ah.setCreative(null, true);

            //Generate a trader ID
            long traderID = this.getNextID();

            //Apply it to the trader
            ah.setID(traderID);

            LightmansCurrency.LogInfo("Successfully created an auction house trader with id '" + traderID + "'!");
            this.traderData.put(traderID, ah);
            this.markDirty();
            //Send update packet to the connected clients
            NbtCompound compound = ah.save();
            new SMessageUpdateClientTrader(compound).sendToAll();
        }
    }

    private long nextID = 0;
    private long getNextID() {
        long id = nextID;
        this.nextID++;
        this.markDirty();
        return id;
    }
    private final Map<Long,TraderData> traderData = new HashMap<>();

    //Persistent Data
    private final Map<String,PersistentData> persistentTraderData = new HashMap<>();
    private final List<PersistentAuctionData> persistentAuctionData = new ArrayList<>();

    private JsonObject persistentTraderJson = new JsonObject();

    public TraderSaveData() { this.validateAuctionHouse(); this.loadPersistentTraders(); }

    public TraderSaveData(NbtCompound compound) {

        this.nextID = compound.getLong("NextID");
        LightmansCurrency.LogInfo("Loaded NextID (" + this.nextID + ") from tag.");

        NbtList traderData = compound.getList("TraderData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < traderData.size(); ++i)
        {
            try {
                NbtCompound traderTag = traderData.getCompound(i);
                TraderData trader = TraderData.Deserialize(false, traderTag);
                if(trader != null)
                    this.traderData.put(trader.getID(), trader.allowMarkingDirty());
                else
                    LightmansCurrency.LogError("Error loading TraderData entry at index " + i);
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading TraderData", t); }
        }

        NbtList persistentData = compound.getList("PersistentData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < persistentData.size(); ++i)
        {
            try {
                NbtCompound c = persistentData.getCompound(i);
                String name = c.getString("Name");
                long id = c.getLong("ID");
                NbtCompound tag = c.getCompound("Tag");
                this.persistentTraderData.put(name, new PersistentData(id,tag));
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading Persistent Data", t); }
        }

        this.validateAuctionHouse();
        this.loadPersistentTraders();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound) {

        compound.putLong("NextID", this.nextID);

        NbtList traderData = new NbtList();
        this.traderData.forEach((id,trader) -> {
            if(trader.isPersistent())
            {
                try {
                    this.putPersistentTag(trader.getPersistentID(), trader.savePersistentData());
                } catch(Throwable t) { LightmansCurrency.LogError("Error saving persistent trader data:", t); }
            }
            else
            {
                try {
                    traderData.add(trader.save());
                } catch(Throwable t) { LightmansCurrency.LogError("Error saving trader data:", t); }
            }
        });
        compound.put("TraderData", traderData);

        NbtList persistentData = new NbtList();
        this.persistentTraderData.forEach((id,data) -> {
            try {
                NbtCompound c = new NbtCompound();
                c.putString("Name", id);
                c.putLong("ID", data.id);
                c.put("Tag", data.tag);
                persistentData.add(c);
            } catch(Throwable t) { LightmansCurrency.LogError("Error saving Persistent Data:", t); }
        });
        compound.put("PersistentData", persistentData);

        return compound;
    }

    private long getPersistentID(String traderID) {
        if(this.persistentTraderData.containsKey(traderID))
            return this.persistentTraderData.get(traderID).id;
        return -1;
    }

    private void putPersistentID(String traderID, long id) {
        if(this.persistentTraderData.containsKey(traderID))
            this.persistentTraderData.get(traderID).id = id;
        else
            this.persistentTraderData.put(traderID, new PersistentData(id, new NbtCompound()));
        this.markDirty();
    }

    private NbtCompound getPersistentTag(String traderID) {
        if(this.persistentTraderData.containsKey(traderID))
            return this.persistentTraderData.get(traderID).tag;
        return new NbtCompound();
    }

    private void putPersistentTag(String traderID, NbtCompound tag) {
        if(this.persistentTraderData.containsKey(traderID))
            this.persistentTraderData.get(traderID).tag = tag == null ? new NbtCompound() : tag;
        else
            this.persistentTraderData.put(traderID, new PersistentData(-1, tag == null ? new NbtCompound() : tag));
        this.markDirty();
    }

    public static JsonObject getPersistentTraderJson() {
        //Force the Trader Data to be loaded.
        TraderSaveData tsd = get();
        if(tsd != null)
            return tsd.persistentTraderJson;
        return new JsonObject();
    }

    public static JsonArray getPersistentTraderJson(String section) {
        JsonObject json = getPersistentTraderJson();
        if(json != null)
        {
            if(!json.has(section))
            {
                JsonArray newSection = new JsonArray();
                json.add(section, newSection);
            }
            if(json.get(section).isJsonArray())
                return json.get(section).getAsJsonArray();
            else
                LightmansCurrency.LogError("Cannot get Persistent Data section '" + section + "' as it is not a JsonArray.");
        }
        return null;
    }

    public static void setPersistentTraderJson(JsonObject newData) {
        TraderSaveData tsd = get();
        if(tsd != null)
        {
            File ptf = new File(PERSISTENT_TRADER_FILENAME);
            try {
                tsd.loadPersistentTrader(newData);
            } catch(Exception e) {
                LightmansCurrency.LogError("Error loading modified Persistent Trader Data. Ignoring request.", e);
                return;
            }
            //Now that it's safely loaded, set the data and save to file
            tsd.persistentTraderJson = newData;
            tsd.savePersistentTraderJson(ptf);
            tsd.resendTraderData();
        }
    }

    public static void setPersistentTraderSection(String section, JsonArray newData) {
        JsonObject json = getPersistentTraderJson();
        json.add(section, newData);
        setPersistentTraderJson(json);
    }

    //PERSISTENT DATA LOADING

    public static void ReloadPersistentTraders() {
        TraderSaveData tsd = get();
        if(tsd != null)
        {
            tsd.loadPersistentTraders();
            tsd.resendTraderData();
        }
    }

    private void loadPersistentTraders() {
        //Get JSON file
        File ptf = new File(PERSISTENT_TRADER_FILENAME);
        if(!ptf.exists())
        {
            this.persistentTraderJson = generateDefaultPersistentTraderJson();
            this.savePersistentTraderJson(ptf);
        }
        try {
            this.persistentTraderJson = JsonHelper.deserialize(Files.readString(ptf.toPath()));
            this.loadPersistentTrader(this.persistentTraderJson);
        } catch(Throwable e) {
            LightmansCurrency.LogError("Error loading Persistent Traders.", e);
            //If an error occurs while loading, set the data to default.
            this.persistentTraderJson = generateDefaultPersistentTraderJson();
        }
    }

    private static JsonObject generateDefaultPersistentTraderJson() {
        JsonObject fileData = new JsonObject();
        JsonArray traderList = new JsonArray();
        fileData.add(PERSISTENT_TRADER_SECTION, traderList);
        JsonArray auctions = new JsonArray();
        fileData.add(PERSISTENT_AUCTION_SECTION, auctions);
        return fileData;
    }

    private void loadPersistentTrader(JsonObject fileData) throws Exception {
        boolean hadNone = true;
        if(fileData.has(PERSISTENT_TRADER_SECTION))
        {
            hadNone = false;

            //Remove persistent traders
            List<Long> removeTraderList = new ArrayList<>();
            this.traderData.forEach((id,trader) -> {
                if(trader.isPersistent())
                {
                    //Save persistent tag
                    this.putPersistentTag(trader.getPersistentID(), trader.savePersistentData());
                    removeTraderList.add(id);
                }
            });

            for(long id : removeTraderList)
                this.traderData.remove(id);

            List<String> loadedIDs = new ArrayList<>();
            JsonArray traderList = fileData.getAsJsonArray(PERSISTENT_TRADER_SECTION);
            for(int i = 0; i < traderList.size(); ++i)
            {
                try {

                    //Load the trader
                    JsonObject traderTag = traderList.get(i).getAsJsonObject();
                    String traderID;
                    if(traderTag.has("ID"))
                        traderID = traderTag.get("ID").getAsString();
                    else if(traderTag.has("id"))
                        traderID = traderTag.get("id").getAsString();
                    else
                        throw new Exception("Trader has no defined id.");
                    if(loadedIDs.contains(traderID))
                        throw new Exception("Trader with id '" + traderID + "' already exists. Cannot have duplicate ids.");
                    TraderData data = TraderData.Deserialize(traderTag);

                    //Load the persistent data
                    data.loadPersistentData(this.getPersistentTag(traderID));

                    //Match the persistent data with traders id
                    long id = this.getPersistentID(traderID);
                    if(id < 0) //If no ID has ever been generated for this persistent trader ID, generate one and add it to the list
                    {
                        id = this.getNextID();
                        this.putPersistentID(traderID, id);
                        this.markDirty();
                        LightmansCurrency.LogInfo("Generated new ID for persistent trader '" + traderID + "' (" + id + ")");
                    }
                    //Initialize the persistence (forces creative & terminal access)
                    data.makePersistent(id, traderID);

                    this.traderData.put(id, data);
                    loadedIDs.add(traderID);
                    LightmansCurrency.LogInfo("Successfully loaded persistent trader '" + traderID + "' with ID " + id + ".");

                } catch(Throwable e) { LightmansCurrency.LogError("Error loading Persistent Trader at index " + i, e); }
            }
        }
        if(fileData.has(PERSISTENT_AUCTION_SECTION))
        {
            hadNone = false;
            this.persistentAuctionData.clear();
            List<String> loadedIDs = new ArrayList<>();
            JsonArray auctionList = fileData.getAsJsonArray(PERSISTENT_AUCTION_SECTION);
            for(int i = 0; i < auctionList.size(); ++i)
            {
                try {

                    //Load the auction
                    JsonObject auctionTag = auctionList.get(i).getAsJsonObject();
                    PersistentAuctionData data = PersistentAuctionData.load(auctionTag);
                    if(loadedIDs.contains(data.id))
                        throw new Exception("Auction with id '" + data.id + "' already exists. Cannot have duplicate ids.");
                    else
                        loadedIDs.add(data.id);

                    this.persistentAuctionData.add(data);

                    LightmansCurrency.LogInfo("Successfully loaded persistent auction '" + data.id + "'");

                } catch(Throwable e) { LightmansCurrency.LogError("Error loading Persistent Auction at index " + i, e); }

            }

        }
        if(hadNone)
            throw new Exception("Json Data has no 'Traders' or 'Auctions' entry.");
    };

    private void savePersistentTraderJson(File ptf) {
        File dir = new File(ptf.getParent());
        if(!dir.exists())
            dir.mkdirs();
        if(dir.exists())
        {
            try {

                ptf.createNewFile();

                String jsonString = FileUtil.GSON.toJson(this.persistentTraderJson);

                FileUtil.writeStringToFile(ptf, jsonString);

                LightmansCurrency.LogInfo("PersistentTraders.json does not exist. Creating a fresh copy.");

            } catch(Throwable e) { LightmansCurrency.LogError("Error attempting to create 'persistentTraders.json' file.", e); }
        }
    }

    private static TraderSaveData get() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            ServerWorld level = server.getOverworld();
            if(level != null)
                return level.getPersistentStateManager().getOrCreate(TraderSaveData::new, TraderSaveData::new, "lightmanscurrency_trader_data");
        }
        return null;
    }

    public static void MarkTraderDirty(NbtCompound updateMessage) {

        TraderSaveData tsd = get();
        if(tsd != null)
        {
            tsd.markDirty();
            new SMessageUpdateClientTrader(updateMessage).sendToAll();
        }

    }

    public static long RegisterTrader(TraderData newTrader, @Nullable PlayerEntity player) {
        TraderSaveData tsd = get();
        if(tsd != null)
        {
            long newID = tsd.getNextID();
            newTrader.setID(newID);
            tsd.traderData.put(newID, newTrader.allowMarkingDirty());
            tsd.markDirty();
            new SMessageUpdateClientTrader(newTrader.save()).sendToAll();
            //TODO launch network trader creation event
            //if(newTrader.shouldAlwaysShowOnTerminal() && player != null)
            //    MinecraftForge.EVENT_BUS.post(new TraderEvent.CreateNetworkTraderEvent(newID, player));
            return newID;
        }
        return -1;
    }

    public static TraderData DeleteTrader(long traderID) {
        TraderSaveData tsd = get();
        if(tsd != null)
        {
            if(tsd.traderData.containsKey(traderID))
            {
                TraderData trader = tsd.traderData.get(traderID);
                tsd.traderData.remove(traderID);
                tsd.markDirty();
                new SMessageRemoveClientTrader(traderID).sendToAll();
                //TODO launch network trader removal event
                //if(trader.shouldAlwaysShowOnTerminal())
                //    MinecraftForge.EVENT_BUS.post(new TraderEvent.RemoveNetworkTraderEvent(traderID, trader));
                return trader;
            }
        }
        return null;
    }

    public static List<TraderData> GetAllTraders(boolean isClient)
    {
        if(isClient)
        {
            return ClientTraderData.GetAllTraders();
        }
        else
        {
            TraderSaveData tsd = get();
            if(tsd != null)
                return new ArrayList<>(tsd.traderData.values());
        }
        return new ArrayList<>();
    }

    public static List<TraderData> GetAllTerminalTraders(boolean isClient)
    {
        return GetAllTraders(isClient).stream().filter(trader -> trader.showOnTerminal()).collect(Collectors.toList());
    }

    public static TraderData GetTrader(boolean isClient, long traderID) {
        if(isClient)
        {
            return ClientTraderData.GetTrader(traderID);
        }
        else
        {
            TraderSaveData tsd = get();
            if(tsd != null)
            {
                if(tsd.traderData.containsKey(traderID))
                    return tsd.traderData.get(traderID);
            }
            return null;
        }
    }

    /**
     * Clean up invalid traders
     */
    public static void onServerTick(MinecraftServer server)
    {
        TraderSaveData tsd = get();
        if(tsd != null)
        {
            if(server.getTicks() % 1200 == 0)
            {

                tsd.traderData.values().removeIf(traderData -> {
                    if(!traderData.isPersistent() && traderData.shouldRemove(server))
                    {
                        if(LCConfig.SERVER.safelyEjectMachineContents.get())
                        {
                            try {
                                World level = server.getWorld(traderData.getLevel());
                                BlockPos pos = traderData.getPos();
                                EjectionData e = EjectionData.create(level, pos, null, traderData, false);
                                EjectionSaveData.HandleEjectionData(level, pos, e);
                            } catch(Throwable t) { t.printStackTrace(); }
                        }
                        new SMessageRemoveClientTrader(traderData.getID()).sendToAll();
                        return true;
                    }
                    return false;
                });
            }
            if(server.getTicks() % 20 == 0 && tsd.persistentAuctionData.size() > 0)
            {
                List<TraderData> traders = tsd.traderData.values().stream().collect(Collectors.toList());
                AuctionHouseTrader ah = null;
                for(int i = 0; i < traders.size() && ah == null; ++i)
                {
                    if(traders.get(i) instanceof AuctionHouseTrader)
                        ah = (AuctionHouseTrader)traders.get(i);
                }
                if(ah != null)
                {
                    for(PersistentAuctionData pad : tsd.persistentAuctionData)
                    {
                        if(!ah.hasPersistentAuction(pad.id))
                        {
                            AuctionTradeData trade = pad.createAuction();
                            if(trade != null)
                            {
                                ah.addTrade(trade, true);
                                LightmansCurrency.LogInfo("Successfully added Persistent Auction '" + pad.id + "' into the auction house.");
                            }
                        }
                    }
                }
            }
            //Tick the trader
            tsd.traderData.values().forEach(trader -> trader.onServerTick(server));
        }
    }

    public static void OnPlayerLogin(ServerPlayerEntity player, PacketSender sender)
    {
        TraderSaveData tsd = get();
        if(tsd != null)
        {
            //Send the clear message
            new SMessageClearClientTraders().sendTo(player);
            //Send update message to the newly connected client
            tsd.traderData.forEach((id,trader) -> new SMessageUpdateClientTrader(trader.save()).sendTo(sender));
        }
    }

    private void resendTraderData()
    {
        new SMessageClearClientTraders().sendToAll();
        this.traderData.forEach((id,trader) -> new SMessageUpdateClientTrader(trader.save()).sendToAll());
    }


    private static class PersistentData
    {

        public long id;
        public NbtCompound tag;

        public PersistentData(long id, NbtCompound tag) { this.id = id; this.tag = tag == null ? new NbtCompound() : tag; }

    }

}