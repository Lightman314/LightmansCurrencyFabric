package io.github.lightman314.lightmanscurrency.common.traderinterface;

import java.util.function.Function;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.common.traders.ITradeSource;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class NetworkTradeReference extends NetworkTraderReference{

    private final Function<NbtCompound,TradeData> tradeDeserializer;

    private int tradeIndex = -1;
    public int getTradeIndex() { return this.tradeIndex; }
    private TradeData tradeData = null;
    public boolean hasTrade() { return this.tradeIndex >= 0 && this.tradeData != null; }
    public TradeData getLocalTrade() { return this.tradeData; }

    public void setTrade(int tradeIndex) {
        this.tradeIndex = tradeIndex;
        this.tradeData = copyTrade(this.getTrueTrade());
        if(this.tradeData == null)
            this.tradeIndex = -1;
    }

    public void refreshTrade() {
        if(!this.hasTrade())
            return;
        TradeData newTrade = copyTrade(this.getTrueTrade());
        if(newTrade != null)
            this.tradeData = newTrade;
    }

    public TradeData copyTrade(TradeData trade) {
        if(trade == null)
            return null;
        return this.tradeDeserializer.apply(trade.getAsNBT());
    }

    public NetworkTradeReference(Supplier<Boolean> clientCheck, Function<NbtCompound,TradeData> tradeDeserializer) {
        super(clientCheck);
        this.tradeDeserializer = tradeDeserializer;
    }

    public TradeData getTrueTrade() {
        if(this.tradeIndex < 0)
            return null;
        TraderData trader = this.getTrader();
        if(trader instanceof ITradeSource<?> tradeSource)
            return tradeSource.getTrade(this.tradeIndex);
        return null;
    }

    public NbtCompound save() {
        NbtCompound compound = super.save();
        if(this.tradeData != null && this.tradeIndex >= 0)
        {
            compound.putInt("TradeIndex", this.tradeIndex);
            compound.put("Trade", this.tradeData.getAsNBT());
        }
        return compound;
    }

    public void load(NbtCompound compound) {
        super.load(compound);
        //Load trade index
        if(compound.contains("TradeIndex", NbtElement.INT_TYPE))
            this.tradeIndex = compound.getInt("TradeIndex");
        //Load trade
        if(compound.contains("Trade", NbtElement.COMPOUND_TYPE))
            this.tradeData = this.tradeDeserializer.apply(compound.getCompound("Trade"));
    }

}