package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.item.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class AuctionHouseSellerNotification extends AuctionHouseNotification{

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "auction_house_seller");

    List<ItemTradeNotification.ItemData> items;
    CoinValue cost = new CoinValue();

    String customer;

    public AuctionHouseSellerNotification(AuctionTradeData trade) {

        this.cost = trade.getLastBidAmount().copy();
        this.customer = trade.getLastBidPlayer().getName(false);

        this.items = new ArrayList<>();
        for(int i = 0; i < trade.getAuctionItems().size(); ++i)
            this.items.add(new ItemTradeNotification.ItemData(trade.getAuctionItems().get(i)));

    }

    public AuctionHouseSellerNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public MutableText getMessage() {

        Text itemText = getItemNames(this.items);

        Text cost = this.cost.getComponent("0");

        //Create log from stored data
        return Text.translatable("notifications.message.auction.seller", this.customer, itemText, cost);

    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        NbtList itemList = new NbtList();
        for(ItemTradeNotification.ItemData item : this.items)
            itemList.add(item.save());
        compound.put("Items", itemList);
        this.cost.save(compound, "Price");
        compound.putString("Customer", this.customer);

    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        NbtList itemList = compound.getList("Items", NbtElement.COMPOUND_TYPE);
        this.items = new ArrayList<>();
        for(int i = 0; i < itemList.size(); ++i)
            this.items.add(new ItemTradeNotification.ItemData(itemList.getCompound(i)));
        this.cost.load(compound, "Price");
        this.customer = compound.getString("Customer");

    }

}