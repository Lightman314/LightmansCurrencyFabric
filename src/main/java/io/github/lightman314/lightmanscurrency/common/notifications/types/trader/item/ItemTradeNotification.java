package io.github.lightman314.lightmanscurrency.common.notifications.types.trader.item;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ItemTradeNotification extends Notification{

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "item_trade");

    TraderCategory traderData;

    ItemTradeData.ItemTradeType tradeType;
    List<ItemData> items;
    CoinValue cost = new CoinValue();

    String customer;

    public ItemTradeNotification(ItemTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {

        this.traderData = traderData;
        this.tradeType = trade.getTradeType();

        this.items = new ArrayList<>();
        this.items.add(new ItemData(trade.getSellItem(0), trade.isPurchase() ? "" : trade.getCustomName(0)));
        this.items.add(new ItemData(trade.getSellItem(1), trade.isPurchase() ? "" : trade.getCustomName(1)));

        if(trade.isBarter())
        {
            this.items.add(new ItemData(trade.getBarterItem(0),""));
            this.items.add(new ItemData(trade.getBarterItem(1),""));
        }
        else
            this.cost = cost;

        this.customer = customer.getName(false);

    }

    public ItemTradeNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    public MutableText getMessage() {

        Text boughtText = Text.translatable("log.shoplog." + this.tradeType.name().toLowerCase());

        Text itemText = getItemNames(this.items.get(0), this.items.get(1));

        Text cost;
        if(this.tradeType == ItemTradeData.ItemTradeType.BARTER)
        {
            //Flip the cost and item text, as for barters the text is backwards "bartered *barter items* for *sold items*"
            cost = itemText;
            itemText = getItemNames(this.items.get(2), this.items.get(3));
        }
        else
            cost = Text.literal(this.cost.getString("0"));

        //Create log from stored data
        return Text.translatable("notifications.message.item_trade", this.customer, boughtText, itemText, cost);

    }

    private Text getItemNames(ItemData item1, ItemData item2) {
        if(item1.isEmpty && item2.isEmpty)
            return Text.literal("ERROR");
        else if(item2.isEmpty)
            return item1.format();
        else if(item1.isEmpty)
            return item2.format();
        else
            return item1.formatWith(item2);
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        compound.put("TraderInfo", this.traderData.save());
        compound.putInt("TradeType", this.tradeType.index);
        NbtList itemList = new NbtList();
        for(ItemData item : this.items)
            itemList.add(item.save());
        compound.put("Items", itemList);
        if(this.tradeType != ItemTradeData.ItemTradeType.BARTER)
            this.cost.save(compound, "Price");
        compound.putString("Customer", this.customer);

    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
        this.tradeType = ItemTradeData.ItemTradeType.fromIndex(compound.getInt("TradeType"));
        NbtList itemList = compound.getList("Items", NbtElement.COMPOUND_TYPE);
        this.items = new ArrayList<>();
        for(int i = 0; i < itemList.size(); ++i)
            this.items.add(new ItemData(itemList.getCompound(i)));
        if(this.tradeType != ItemTradeData.ItemTradeType.BARTER)
            this.cost.load(compound, "Price");
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof ItemTradeNotification)
        {
            ItemTradeNotification itn = (ItemTradeNotification)other;
            if(!itn.traderData.matches(this.traderData))
                return false;
            if(itn.tradeType != this.tradeType)
                return false;
            if(itn.items.size() != this.items.size())
                return false;
            for(int i = 0; i < this.items.size(); ++i)
            {
                ItemData i1 = this.items.get(i);
                ItemData i2 = itn.items.get(i);
                if(!i1.itemName.getString().equals(i2.itemName.getString()))
                    return false;
                if(i1.count != i2.count)
                    return false;
            }
            if(itn.cost.getRawValue() != this.cost.getRawValue())
                return false;
            if(!itn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return true;
        }
        return false;
    }

    public static class ItemData
    {
        final boolean isEmpty;
        final Text itemName;
        final int count;

        public ItemData(ItemStack item) { this(item, ""); }

        public ItemData(ItemStack item, String customName) {
            this.isEmpty = item.isEmpty();
            if(this.isEmpty)
            {
                this.itemName = Text.empty();
                this.count = 0;
                return;
            }
            if(customName.isEmpty())
                itemName = item.getName();
            else
                this.itemName = Text.literal(customName);
            this.count = item.getCount();
        }

        public ItemData(NbtCompound compound) {
            this.isEmpty = compound.contains("Empty");
            if(this.isEmpty)
            {
                this.itemName = Text.empty();
                this.count = 0;
                return;
            }
            this.itemName = Text.Serializer.fromJson(compound.getString("Name"));
            this.count = compound.getInt("Count");
        }

        public NbtCompound save() {
            NbtCompound compound = new NbtCompound();
            if(this.isEmpty)
            {
                compound.putBoolean("Empty", true);
                return compound;
            }
            compound.putString("Name", Text.Serializer.toJson(this.itemName));
            compound.putInt("Count", this.count);
            return compound;
        }

        public Text format() { return Text.translatable("log.shoplog.item.itemformat", this.count, this.itemName); }

        public Text formatWith(Text other) { return Text.translatable("log.shoplog.and", this.format(), other); }

        public Text formatWith(ItemData other) { return Text.translatable("log.shoplog.and", this.format(), other.format()); }

        public boolean matches(ItemData other)
        {
            return this.isEmpty == other.isEmpty && this.itemName.getString().equals(other.itemName.getString()) && this.count == other.count;
        }

    }

}