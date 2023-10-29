package io.github.lightman314.lightmanscurrency.common.traders.item.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.AlertData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.IBarterTrade;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeComparisonResult.ProductComparisonResult;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ItemTradeData extends TradeData implements IBarterTrade {

    public ItemTradeData(boolean validateRules) { super(validateRules); }

    public enum ItemTradeType { SALE(0,1), PURCHASE(1,2), BARTER(2,0);
        public final int index;
        private final int nextIndex;
        public final ItemTradeType next() { return fromIndex(this.nextIndex); }
        ItemTradeType(int index, int nextIndex) { this.index = index; this.nextIndex = nextIndex; }
        public static ItemTradeType fromIndex(int index) {
            for(ItemTradeType type : ItemTradeType.values())
            {
                if(type.index == index)
                    return type;
            }
            return ItemTradeType.SALE;
        }
    }

    public static int MaxTradeTypeStringLength()
    {
        int length = 0;
        for(ItemTradeType value : ItemTradeType.values())
        {
            int thisLength = value.name().length();
            if(thisLength > length)
                length = thisLength;
        }
        return length;
    }

    public static final int MAX_CUSTOMNAME_LENGTH = 30;

    ItemTradeRestriction restriction = ItemTradeRestriction.NONE;
    SimpleInventory items = new SimpleInventory(4);
    ItemTradeType tradeType = ItemTradeType.SALE;
    String customName1 = "";
    String customName2 = "";

    public ItemStack getSellItem(int index)
    {
        if(index >= 0 && index < 2)
            return this.restriction.modifySellItem(this.items.getStack(index).copy(), this.getCustomName(index), this);
        return ItemStack.EMPTY;
    }

    public ItemStack getBarterItem(int index)
    {
        if(index >= 0 && index < 2)
            return this.items.getStack(index + 2).copy();
        return ItemStack.EMPTY;
    }

    public ItemStack getItem(int index)
    {
        if(index >= 0 && index < 2)
            return this.getSellItem(index);
        else if(index >= 2 && index < 4)
            return this.getBarterItem(index - 2);
        return ItemStack.EMPTY;
    }

    public void setItem(ItemStack itemStack, int index)
    {
        if(index >= 0 && index < 4)
        {
            if(index < 2)
            {
                if(this.restriction.allowSellItem(itemStack) || itemStack.isEmpty())
                    this.items.setStack(index, this.restriction.filterSellItem(itemStack).copy());
            }
            else
                this.items.setStack(index, itemStack.copy());
        }
        else
            LightmansCurrency.LogError("Cannot define the item trades item at index " + index + ". Must be between 0-3!");
    }

    public boolean allowItemInStorage(ItemStack item) {
        for(int i = 0; i < (this.isBarter() ? 4 : 2); ++i)
        {
            if(InventoryUtil.ItemMatches(item, this.getItem(i)))
                return true;
        }
        return this.restriction.allowExtraItemInStorage(item);
    }

    public boolean hasCustomName(int index) { return !this.getCustomName(index).isEmpty(); }

    public String getCustomName(int index)
    {
        switch(index) {
            case 0:
                return this.customName1;
            case 1:
                return this.customName2;
            default:
                return "";
        }
    }

    public void setCustomName(int index, String customName)
    {
        switch(index) {
            case 0:
                this.customName1 = customName;
                return;
            case 1:
                this.customName2 = customName;
                return;
        }
    }

    @Override
    public TradeDirection getTradeDirection()
    {
        switch(this.tradeType)
        {
            case SALE:
                return TradeDirection.SALE;
            case PURCHASE:
                return TradeDirection.PURCHASE;
            default:
                return TradeDirection.NONE;
        }
    }

    public ItemTradeType getTradeType() { return this.tradeType; }

    public boolean isSale() { return this.tradeType == ItemTradeType.SALE; }
    public boolean isPurchase() { return this.tradeType == ItemTradeType.PURCHASE; }
    public boolean isBarter() { return this.tradeType == ItemTradeType.BARTER; }

    public void setTradeType(ItemTradeType tradeDirection)
    {
        this.tradeType = tradeDirection;
    }

    public int getSlotCount()
    {
        return this.tradeType == ItemTradeType.BARTER ? 2 : 1;
    }

    public ItemTradeRestriction getRestriction() { return this.restriction; }

    public void setRestriction(ItemTradeRestriction restriction) { this.restriction = restriction; }

    @Override
    public boolean isValid()
    {
        if(this.tradeType == ItemTradeType.BARTER)
            return this.sellItemsDefined() && this.barterItemsDefined();
        return super.isValid() && this.sellItemsDefined();
    }

    public boolean sellItemsDefined() {
        return !this.getSellItem(0).isEmpty() || !this.getSellItem(1).isEmpty();
    }

    public boolean barterItemsDefined() {
        return !this.getBarterItem(0).isEmpty() || !this.getBarterItem(1).isEmpty();
    }

    public boolean hasStock(ItemTraderData trader)
    {
        if(!this.sellItemsDefined())
            return false;
        return stockCount(trader) > 0;
    }

    public boolean hasStock(TradeContext context)
    {
        if(!this.sellItemsDefined())
            return false;
        return stockCount(context) > 0;
    }

    public boolean hasSpace(ItemTraderData trader)
    {
        switch(this.tradeType)
        {
            case PURCHASE:
                return trader.getStorage().canFitItems(this.getSellItem(0), this.getSellItem(1));
            case BARTER:
                return trader.getStorage().canFitItems(this.getBarterItem(0), this.getBarterItem(1));
            default:
                return true;
        }
    }

    public int stockCount(ItemTraderData trader)
    {
        if(!this.sellItemsDefined())
            return 0;

        if(this.tradeType == ItemTradeType.PURCHASE)
        {
            if(this.cost.isFree())
                return 1;
            if(this.cost.getRawValue() == 0)
                return 0;
            long coinValue = trader.getStoredMoney().getRawValue();
            long price = this.cost.getRawValue();
            return (int)(coinValue / price);
        }
        else if(this.tradeType == ItemTradeType.SALE || this.tradeType == ItemTradeType.BARTER)
        {
            return this.restriction.getSaleStock(trader.getStorage(), this.getSellItem(0), this.getSellItem(1));
        }
        else //Other types are not handled yet.
            return 0;
    }

    public int stockCount(TradeContext context)
    {
        if(!this.sellItemsDefined())
            return 0;

        if(!context.hasTrader() || !(context.getTrader() instanceof ItemTraderData))
            return 0;

        ItemTraderData trader = (ItemTraderData)context.getTrader();
        if(trader.isCreative())
            return 1;

        if(this.tradeType == ItemTradeType.PURCHASE)
        {
            if(this.cost.isFree())
                return 1;
            if(this.cost.getRawValue() == 0)
                return 0;
            long coinValue = trader.getStoredMoney().getRawValue();
            long price = this.getCost(context).getRawValue();
            return (int)(coinValue / price);
        }
        else if(this.tradeType == ItemTradeType.SALE || this.tradeType == ItemTradeType.BARTER)
        {
            return this.restriction.getSaleStock(trader.getStorage(), this.getSellItem(0), this.getSellItem(1));
        }
        else //Other types are not handled yet.
            return 0;
    }

    public boolean canAfford(TradeContext context) {
        if(this.isSale())
            return context.hasFunds(this.getCost(context));
        if(this.isPurchase())
            return context.hasItems(this.getSellItem(0), this.getSellItem(1));
        if(this.isBarter())
            return context.hasItems(this.getBarterItem(0), this.getBarterItem(1));
        return false;
    }

    public void RemoveItemsFromStorage(TraderItemStorage storage)
    {
        this.restriction.removeItemsFromStorage(storage, this.getSellItem(0), this.getSellItem(1));
    }

    @Override
    public NbtCompound getAsNBT() {
        NbtCompound tradeNBT = super.getAsNBT();
        InventoryUtil.saveAllItems("Items", tradeNBT, this.items);
        tradeNBT.putString("TradeDirection", this.tradeType.name());
        tradeNBT.putString("CustomName1", this.customName1);
        tradeNBT.putString("CustomName2", this.customName2);
        return tradeNBT;
    }

    public static NbtCompound saveAllData(NbtCompound nbt, List<ItemTradeData> data)
    {
        return saveAllData(nbt, data, DEFAULT_KEY);
    }

    public static NbtCompound saveAllData(NbtCompound nbt, List<ItemTradeData> data, String key)
    {
        NbtList listNBT = new NbtList();

        for(int i = 0; i < data.size(); i++)
        {
            listNBT.add(data.get(i).getAsNBT());
        }

        nbt.put(key, listNBT);

        return nbt;
    }

    public static ItemTradeData loadData(NbtCompound compound, boolean validateRules) {
        ItemTradeData trade = new ItemTradeData(validateRules);
        trade.loadFromNBT(compound);
        return trade;
    }

    public static List<ItemTradeData> loadAllData(NbtCompound nbt, boolean validateRules)
    {
        return loadAllData(DEFAULT_KEY, nbt, validateRules);
    }

    public static List<ItemTradeData> loadAllData(String key, NbtCompound compound, boolean validateRules)
    {
        List<ItemTradeData> data = new ArrayList<>();

        NbtList listNBT = compound.getList(key, NbtElement.COMPOUND_TYPE);

        for(int i = 0; i < listNBT.size(); i++)
            data.add(loadData(listNBT.getCompound(i), validateRules));

        return data;
    }

    @Override
    public void loadFromNBT(NbtCompound nbt)
    {

        super.loadFromNBT(nbt);

        if(nbt.contains("Items", NbtElement.LIST_TYPE)) //Load Sale/Barter Items
            this.items = InventoryUtil.loadAllItems("Items", nbt, 4);

        //Set the Trade Direction
        if(nbt.contains("TradeDirection", NbtElement.STRING_TYPE))
            this.tradeType = loadTradeType(nbt.getString("TradeDirection"));
        else
            this.tradeType = ItemTradeType.SALE;

        if(nbt.contains("CustomName1"))
            this.customName1 = nbt.getString("CustomName1");
        else if(nbt.contains("CustomName"))
            this.customName1 = nbt.getString("CustomName");
        else
            this.customName1 = "";

        if(nbt.contains("CustomName2"))
            this.customName2 = nbt.getString("CustomName2");
        else
            this.customName2 = "";
    }

    public static ItemTradeType loadTradeType(String name)
    {
        ItemTradeType value = ItemTradeType.SALE;
        try {
            value = ItemTradeType.valueOf(name);
        }
        catch (IllegalArgumentException exception)
        {
            LightmansCurrency.LogError("Could not load '" + name + "' as a TradeDirection.");
        }
        return value;
    }

    public static List<ItemTradeData> listOfSize(int tradeCount, boolean validateRules)
    {
        List<ItemTradeData> data = Lists.newArrayList();
        while(data.size() < tradeCount)
            data.add(new ItemTradeData(validateRules));
        return data;
    }

    public void markRulesDirty() { }


    public TradeComparisonResult compare(TradeData otherTrade) {
        TradeComparisonResult result = new TradeComparisonResult();
        if(otherTrade instanceof ItemTradeData)
        {
            ItemTradeData otherItemTrade = (ItemTradeData)otherTrade;
            //Flag as compatible
            result.setCompatible();
            //Compare sell items
            result.addProductResults(ProductComparisonResult.CompareTwoItems(this.getSellItem(0), this.getSellItem(1), otherItemTrade.getSellItem(0), otherItemTrade.getSellItem(1)));
            //Compare barter items
            if(this.isBarter())
                result.addProductResults(ProductComparisonResult.CompareTwoItems(this.getBarterItem(0), this.getBarterItem(1), otherItemTrade.getBarterItem(0), otherItemTrade.getBarterItem(1)));
            //Compare prices
            if(!this.isBarter())
                result.setPriceResult(this.getCost().getRawValue() - otherTrade.getCost().getRawValue());
            //Compare types
            result.setTypeResult(this.tradeType == otherItemTrade.tradeType);
        }
        //Return the comparison results
        return result;
    }

    public boolean AcceptableDifferences(TradeComparisonResult result) {

        //Confirm the types match
        if(!result.TypeMatches() || !result.isCompatible())
            return false;

        //Confirm the sell item is acceptable
        if(result.getProductResultCount() < 2)
            return false;
        for(int i = 0; i < 2; ++i)
        {
            ProductComparisonResult sellResult = result.getProductResult(i);
            if(sellResult.SameProductType() && sellResult.SameProductNBT())
            {
                if(this.isSale() || this.isBarter())
                {
                    //Sell product should be greater than or equal to pass
                    if(sellResult.ProductQuantityDifference() > 0)
                        return false;
                }
                else if(this.isPurchase())
                {
                    //Purchase product should be less than or equal to pass
                    if(sellResult.ProductQuantityDifference() < 0)
                        return false;
                }
            }
            else //Item & tag don't match. Failure.
                return false;
        }
        //Confirm the barter item is acceptable
        if(this.isBarter())
        {
            if(result.getProductResultCount() < 4)
                return false;
            for(int i = 0; i < 2; ++i)
            {
                ProductComparisonResult barterResult = result.getProductResult(i + 2);
                if(barterResult.SameProductType() && barterResult.SameProductNBT())
                {
                    //Barter product should be less than or equal to pass
                    if(barterResult.ProductQuantityDifference() < 0)
                        return false;
                }
                else //Item & tag don't match. Failure.
                    return false;
            }
        }
        //Product is acceptable, now check the price
        if(this.isSale() && result.isPriceExpensive())
            return false;
        if(this.isPurchase() && result.isPriceCheaper())
            return false;

        //Products, price, and types are all acceptable.
        return true;
    }

    @Override
    public List<Text> GetDifferenceWarnings(TradeComparisonResult differences) {
        List<Text> list = new ArrayList<>();
        //Price check
        if(!differences.PriceMatches())
        {
            //Price difference (intended - actual = difference)
            long difference = differences.priceDifference();
            if(difference < 0) //More expensive
                list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.expensive", MoneyUtil.getStringOfValue(-difference)).formatted(Formatting.RED));
            else //Cheaper
                list.add(EasyText.translatable("gui.lightmanscurrency.interface.difference.cheaper", MoneyUtil.getStringOfValue(difference)).formatted(Formatting.RED));
        }
        for(int i = 0; i < differences.getProductResultCount(); ++i)
        {
            Text slotName = EasyText.translatable("gui.lightmanscurrency.interface.item.difference.product." + i);
            ProductComparisonResult productCheck = differences.getProductResult(i);
            if(!productCheck.SameProductType())
                list.add(EasyText.translatable("gui.lightmanscurrency.interface.item.difference.itemtype", slotName).formatted(Formatting.RED));
            else
            {
                if(!productCheck.SameProductNBT()) //Don't announce changes in NBT if the item is also different
                    list.add(EasyText.translatable("gui.lightmanscurrency.interface.item.difference.itemnbt").formatted(Formatting.RED));
                else if(!productCheck.SameProductQuantity()) //Don't announce changes in quantity if the item or nbt is also different
                {
                    int quantityDifference = productCheck.ProductQuantityDifference();
                    if(quantityDifference < 0) //More items
                        list.add(EasyText.translatable("gui.lightmanscurrency.interface.item.difference.quantity.more", slotName, -quantityDifference).formatted(Formatting.RED));
                    else //Less items
                        list.add(EasyText.translatable("gui.lightmanscurrency.interface.item.difference.quantity.less", slotName, quantityDifference).formatted(Formatting.RED));
                }
            }
        }
        return list;
    }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) {
        //If this is a sale, this is the price
        if(this.isSale())
            return Lists.newArrayList(DisplayEntry.of(this.getCost(context), context.isStorageMode ? Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.price_edit")) : null));
        if(this.isPurchase())
            return this.getSaleItemEntries(context);
        if(this.isBarter())
            return this.getBarterItemEntries(context);
        return new ArrayList<>();
    }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        if(this.isSale() || this.isBarter())
            return this.getSaleItemEntries(context);
        if(this.isPurchase())
            return Lists.newArrayList(DisplayEntry.of(this.getCost(context)));
        return new ArrayList<>();
    }

    private List<DisplayEntry> getSaleItemEntries(TradeContext context) {

        List<DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = this.getSellItem(i);
            if(!item.isEmpty())
                entries.add(DisplayEntry.of(item, item.getCount(), this.getSaleItemTooltip(item, this.getCustomName(i), context)));
            else if(context.isStorageMode)
                entries.add(DisplayEntry.of(this.restriction.getEmptySlotBG(), Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.item_edit"))));
        }
        return entries;
    }

    private List<Text> getSaleItemTooltip(ItemStack stack, String customName, TradeContext context)
    {
        if(stack.isEmpty())
        {
            if(context.isStorageMode)
                return Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.item_edit"));
            return null;
        }

        List<Text> tooltips = ItemRenderUtil.getTooltipFromItem(stack);
        Text originalName = null;
        if(!customName.isEmpty() && (this.isSale() || this.isBarter()))
        {
            originalName = tooltips.get(0);
            tooltips.set(0, EasyText.literal(customName).formatted(Formatting.GOLD));
        }
        //Stop here if this is in storage mode, and there's no custom name
        if(context.isStorageMode && originalName == null)
            return tooltips;

        //Trade Info
        tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.trader.info").formatted(Formatting.GOLD));
        //Custom Name
        if(originalName != null)
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.trader.originalname", originalName).formatted(Formatting.GOLD));

        if(context.hasTrader() && context.hasPlayerReference())
        {
            //Stock
            if(context.getTrader() instanceof ItemTraderData)
            {
                ItemTraderData trader = (ItemTraderData)context.getTrader();
                tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.trader.stock", trader.isCreative() ? EasyText.translatable("tooltip.lightmanscurrency.trader.stock.infinite").formatted(Formatting.GOLD) : EasyText.literal(String.valueOf(this.stockCount(context))).formatted(Formatting.GOLD)).formatted(Formatting.GOLD));
            }
        }

        return tooltips;

    }

    private List<DisplayEntry> getBarterItemEntries(TradeContext context) {
        List<DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = this.getBarterItem(i);
            if(!item.isEmpty())
                entries.add(DisplayEntry.of(item, item.getCount(), ItemRenderUtil.getTooltipFromItem(item)));
            else if(context.isStorageMode)
                entries.add(DisplayEntry.of(ItemTradeRestriction.BACKGROUND, Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.trader.item_edit"))));
        }
        return entries;
    }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(59, 1, 34, 16); }

    @Override
    public Pair<Integer,Integer> arrowPosition(TradeContext context) { return Pair.of(36, 1); }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) {
        if(context.hasTrader() && context.getTrader() instanceof ItemTraderData)
        {

            ItemTraderData trader = (ItemTraderData)context.getTrader();
            if(!trader.isCreative())
            {
                //Check Stock
                if(this.stockCount(context) <= 0)
                    alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofstock")));
                //Check Space
                if(!this.hasSpace(trader))
                    alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.outofspace")));
            }
            //Check whether they can afford the cost
            if(!this.canAfford(context))
                alerts.add(AlertData.warn(EasyText.translatable("tooltip.lightmanscurrency.cannotafford")));
        }
    }

    @Override
    public void onInputDisplayInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof ItemTraderData)
        {
            ItemTraderData it = (ItemTraderData)tab.menu.getTrader();
            int tradeIndex = it.indexOfTrade(this);
            if(tradeIndex < 0)
                return;
            if(this.isSale())
            {
                NbtCompound extraData = new NbtCompound();
                extraData.putInt("TradeIndex", tradeIndex);
                extraData.putInt("StartingSlot", -1);
                tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
            }
            if(this.isPurchase() && index >= 0 && index < 2)
            {
                //Set the item to the held item
                ItemStack sellItem = this.getSellItem(index);
                if(sellItem.isEmpty() && heldItem.isEmpty())
                {
                    //Open Item Edit for this slot
                    NbtCompound extraData = new NbtCompound();
                    extraData.putInt("TradeIndex", tradeIndex);
                    extraData.putInt("StartingSlot", index);
                    tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
                }
                else if(InventoryUtil.ItemMatches(sellItem, heldItem) && button == 1)
                {
                    sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxCount()));
                    this.setItem(sellItem, index);
                }
                else
                {
                    ItemStack setItem = heldItem.copy();
                    if(button == 1)
                        setItem.setCount(1);
                    this.setItem(setItem, index);
                }
                //Only send message on client, otherwise we get an infinite loop
                if(tab.menu.isClient())
                    tab.sendInputInteractionMessage(tradeIndex, index, button, heldItem);
            }
            else if(this.isBarter() && index >= 0 && index < 2)
            {
                //Set the item to the held item
                ItemStack barterItem = this.getBarterItem(index);
                if(barterItem.isEmpty() && heldItem.isEmpty())
                {
                    //Open Item Edit for this slot
                    NbtCompound extraData = new NbtCompound();
                    extraData.putInt("TradeIndex", tradeIndex);
                    extraData.putInt("StartingSlot", index + 2);
                    tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
                }
                if(InventoryUtil.ItemMatches(barterItem, heldItem) && button == 1)
                {
                    barterItem.setCount(Math.min(barterItem.getCount() + 1, barterItem.getMaxCount()));
                    this.setItem(barterItem, index + 2);
                }
                else
                {
                    ItemStack setItem = heldItem.copy();
                    if(button == 1)
                        setItem.setCount(1);
                    this.setItem(setItem, index + 2);
                }
                //Only send message on client, otherwise we get an infinite loop
                if(tab.menu.isClient())
                    tab.sendInputInteractionMessage(tradeIndex, index, button, heldItem);
            }
        }
    }

    /**
     * Code used for item slot interactions. Works on the assumption that we're in the Item Edit Tab
     */
    public void onSlotInteraction(ItemTradeEditTab tab, int index, ItemStack heldItem, int button) {
        if(index < 2)
        {
            //Set the item to the held item
            ItemStack sellItem = this.getSellItem(index);
            if(sellItem.isEmpty() && heldItem.isEmpty())
                return;
            if(InventoryUtil.ItemMatches(sellItem, heldItem) && button == 1)
            {
                sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxCount()));
                this.setItem(sellItem, index);
            }
            else
            {
                ItemStack setItem = heldItem.copy();
                if(button == 1)
                    setItem.setCount(1);
                this.setItem(setItem, index);
            }
        }
        if(this.isBarter() && index >= 2 && index < 4)
        {
            //Set the item to the held item
            ItemStack barterItem = this.getItem(index);
            if(barterItem.isEmpty() && heldItem.isEmpty())
                return;
            if(InventoryUtil.ItemMatches(barterItem, heldItem) && button == 1)
            {
                barterItem.setCount(Math.min(barterItem.getCount() + 1, barterItem.getMaxCount()));
                this.setItem(barterItem, index);
            }
            else
            {
                ItemStack setItem = heldItem.copy();
                if(button == 1)
                    setItem.setCount(1);
                this.setItem(setItem, index);
            }
        }
    }

    @Override
    public void onOutputDisplayInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof ItemTraderData)
        {
            ItemTraderData it = (ItemTraderData)tab.menu.getTrader();
            int tradeIndex = it.indexOfTrade(this);
            if(tradeIndex < 0)
                return;
            if((this.isSale() || this.isBarter()) && index >= 0 && index < 2)
            {
                //Set the item to the held item
                ItemStack sellItem = this.getSellItem(index);
                if(sellItem.isEmpty() && heldItem.isEmpty())
                {
                    //Open Item Edit for this slot
                    NbtCompound extraData = new NbtCompound();
                    extraData.putInt("TradeIndex", tradeIndex);
                    extraData.putInt("StartingSlot", index);
                    tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
                }
                if(InventoryUtil.ItemMatches(sellItem, heldItem) && button == 1)
                {
                    sellItem.setCount(Math.min(sellItem.getCount() + 1, sellItem.getMaxCount()));
                    this.setItem(sellItem, index);
                }
                else
                {
                    ItemStack setItem = heldItem.copy();
                    if(button == 1)
                        setItem.setCount(1);
                    this.setItem(setItem, index);
                }
                //Only send message on client, otherwise we get an infinite loop
                if(tab.menu.isClient())
                    tab.sendOutputInteractionMessage(tradeIndex, index, button, heldItem);
            }
            else if(this.isPurchase())
            {
                NbtCompound extraData = new NbtCompound();
                extraData.putInt("TradeIndex", tradeIndex);
                extraData.putInt("StartingSlot", -1);
                tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
            }
        }
    }

    @Override
    //Open the trade edit tab if you click on a non-interaction slot.
    public void onInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) {
        if(tab.menu.getTrader() instanceof ItemTraderData)
        {
            ItemTraderData it = (ItemTraderData)tab.menu.getTrader();
            int tradeIndex = it.indexOfTrade(this);
            if(tradeIndex < 0)
                return;
            NbtCompound extraData = new NbtCompound();
            extraData.putInt("TradeIndex", tradeIndex);
            tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);
        }
    }

}