package io.github.lightman314.lightmanscurrency.common.money;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;

public class MoneyData extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "money_data_packet");

    private List<CoinData> coinList = new ArrayList<>();
    private List<CoinData> sortedCoinList = new ArrayList<>();

    public MoneyData(CoinDataCollector collector)
    {
        super(PACKET_ID);
        for(CoinData.Builder dataBuilder : collector.coinBuilderList)
            this.addCoinItem(dataBuilder);
        this.sortCoinList();
    }

    public static MoneyData fromJson(JsonObject json) throws Throwable {
        JsonArray array = json.get("CoinEntries").getAsJsonArray();
        CoinDataCollector collector = new CoinDataCollector();
        for(int i = 0; i < array.size(); ++i)
        {
            try {
                CoinData.Builder builder = CoinData.getBuilder(array.get(i).getAsJsonObject());
                collector.addCoinBuilder(builder);
            } catch(Exception e) { LightmansCurrency.LogError("Error loading coin entry " + (i + 1), e); }
        }
        return new MoneyData(collector);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonArray dataList = new JsonArray();
        for(int i = 0; i < this.coinList.size(); ++i)
        {
            try {
                JsonObject entry = this.coinList.get(i).toJson();
                dataList.add(entry);
            } catch(Throwable t) {}
        }
        json.add("CoinEntries", dataList);
        return json;
    }

    public void encode(LazyPacketData.Builder dataBuilder) {
        JsonObject json = this.toJson();
        String jsonString = FileUtil.GSON.toJson(json);
        dataBuilder.setString("json", jsonString);
    }

    private static MoneyData decode(LazyPacketData data) {
        try {
            LightmansCurrency.LogDebug("Decoding money data packet:");
            String jsonString = data.getString("json");
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            return fromJson(json);
        } catch(Throwable t) { LightmansCurrency.LogError("Error decoding MoneyData.", t); return generateDefault(); }
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        LightmansCurrency.LogInfo("Received money data packet from server. Synchronizing coin list.");
        MoneyUtil.receiveMoneyData(decode(data));
    }

    public static MoneyData generateDefault() {
        CoinDataCollector dataCollector = new CoinDataCollector();
        DefaultMoneyDataCollection.EVENT.invoker().appendMoneyData(dataCollector);
        return new MoneyData(dataCollector);
    }

    /**
     * Adds a coin to the official coin list, thus allowing it to be acknowledged as a coin
     */
    private void addCoinItem(CoinData.Builder newCoinDataBuilder)
    {
        CoinData newCoinData = newCoinDataBuilder.build();
        for(CoinData coinData : coinList)
        {
            //Confirm that there's no duplicate coin
            if(coinData.coinItem == newCoinData.coinItem)
            {
                LightmansCurrency.LogWarning("Attempted to add Duplicate Coin Item (" + Registries.ITEM.getId(newCoinData.coinItem).toString() + ") to the coin list.");
                //LightmansCurrency.LOGGER.warn("Please use MoneyUtil.changeCoinValue if you wish to change a coins value.");
                return;
            }
            //Confirm that there's no duplicate dependent (Ignore this if either party is hidden, as conversion will be ignored for that coin)
            if(coinData.worthOtherCoin == newCoinData.worthOtherCoin && !newCoinData.isHidden && !coinData.isHidden && newCoinData.worthOtherCoin != null && coinData.chain.contentEquals(newCoinData.chain))
            {
                LightmansCurrency.LogWarning("Attempted to add a new Coin Item '" + Registries.ITEM.getId(newCoinData.coinItem) + "' with the same dependent (" + Registries.ITEM.getId(coinData.worthOtherCoin) + ") as another coin (" + Registries.ITEM.getId(coinData.coinItem) + ") in the same chain '" + coinData.chain + "'.\nEntry will be flagged as hidden.");
                newCoinData = newCoinDataBuilder.setHidden().build();
            }
        }
        LightmansCurrency.LogInfo("Registered " + Registries.ITEM.getId(newCoinData.coinItem) + " as a coin.");
        coinList.add(newCoinData);

    }

    private void sortCoinList()
    {
        this.sortedCoinList = new ArrayList<>();
        List<CoinData> copyList = Lists.newArrayList(this.coinList);
        while(copyList.size() > 0)
        {
            int highestValueIndex = 0;
            long highestValue = copyList.get(0).getValue(this);
            for(int i = 1; i < copyList.size(); i++)
            {
                if(copyList.get(i).getValue(this) > highestValue)
                {
                    highestValueIndex = i;
                    highestValue = copyList.get(i).getValue(this);
                }
            }
            sortedCoinList.add(copyList.get(highestValueIndex));
            copyList.remove(highestValueIndex);
        }
    }

    public MutableText getPluralName(Item coinItem) {
        CoinData data = this.getData(coinItem);
        if(data != null)
            return data.getPlural();
        else
            return MoneyUtil.getDefaultPlural(coinItem);
    }

    public CoinData getData(Item coinItem) {
        for(CoinData data : this.coinList) {
            if(data.coinItem == coinItem)
                return data;
        }
        return null;
    }

    /**
     * An unsorted copy of the coin list. Used for display/debugging purposes.
     */
    @Deprecated
    public List<CoinData> getCoinList() {
        return this.coinList;
    }

    @Deprecated
    public List<CoinData> getCoinList(String chain) {
        List<CoinData> results = new ArrayList<>();
        for(int i = 0; i < this.coinList.size(); ++i)
        {
            CoinData data = this.coinList.get(i);
            if(data.chain.contentEquals(chain))
                results.add(data);
        }
        return results;
    }

    public List<CoinData> getSortedCoinList() {
        return this.sortedCoinList;
    }

    public List<CoinData> getSortedCoinList(String chain) {
        List<CoinData> results = new ArrayList<>();
        for(int i = 0; i < this.sortedCoinList.size(); ++i)
        {
            CoinData data = this.sortedCoinList.get(i);
            if(data.chain.contentEquals(chain))
                results.add(data);
        }
        return results;
    }

    public static class CoinDataCollector
    {
        List<CoinData.Builder> coinBuilderList = new ArrayList<>();

        public void addCoinBuilder(CoinData.Builder coinBuilder) {
            this.coinBuilderList.add(coinBuilder);
        }

    }

}