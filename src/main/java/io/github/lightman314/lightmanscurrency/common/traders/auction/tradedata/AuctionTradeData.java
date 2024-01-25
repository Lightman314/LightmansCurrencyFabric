package io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.DisplayEntry;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.auction.*;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionPlayerStorage;
import io.github.lightman314.lightmanscurrency.common.traders.auction.PersistentAuctionData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.AlertData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class AuctionTradeData extends TradeData {

    public static long GetMinimumDuration() {
        if(LCConfig.SERVER.auctionHouseDurationMin.get() > 0)
            return TimeUtil.DURATION_DAY * LCConfig.SERVER.auctionHouseDurationMin.get();
        return TimeUtil.DURATION_HOUR;
    }
    public static long GetDefaultDuration() {
        if(LCConfig.SERVER.auctionHouseDurationMax.get() > 0)
            return TimeUtil.DURATION_DAY * LCConfig.SERVER.auctionHouseDurationMax.get();
        return TimeUtil.DURATION_DAY;
    }

    public boolean hasBid() { return this.lastBidPlayer != null; }

    private boolean cancelled;

    private String persistentID = "";
    public boolean isPersistentID(String id) { return this.persistentID.equals(id); }

    CoinValue lastBidAmount = new CoinValue();
    public CoinValue getLastBidAmount() { return this.lastBidAmount; }
    PlayerReference lastBidPlayer = null;
    public PlayerReference getLastBidPlayer() { return this.lastBidPlayer; }

    public void setStartingBid(CoinValue amount) {
        if(this.isActive())
            return;
        this.lastBidAmount = amount.copy();
    }

    CoinValue minBidDifference = new CoinValue(1);
    public CoinValue getMinBidDifference() { return this.minBidDifference; }
    public void setMinBidDifferent(CoinValue amount) {
        if(this.isActive())
            return;
        this.minBidDifference = amount.copy();
        if(this.minBidDifference.getRawValue() <= 0)
            this.minBidDifference = new CoinValue(1);
    }
    PlayerReference tradeOwner;
    public PlayerReference getOwner() { return this.tradeOwner; }
    public boolean isOwner(PlayerEntity player) {
        return (this.tradeOwner != null && this.tradeOwner.is(player)) || CommandLCAdmin.isAdminPlayer(player);
    }

    long startTime = 0;
    long duration = 0;
    public void setDuration(long duration) {
        if(this.isActive())
            return;
        this.duration = Math.max(GetMinimumDuration(), duration);
    }

    List<ItemStack> auctionItems = new ArrayList<>();
    public List<ItemStack> getAuctionItems() { return this.auctionItems; }
    public void setAuctionItems(Inventory auctionItems) {
        if(this.isActive())
            return;
        this.auctionItems.clear();
        for(int i = 0; i < auctionItems.size(); ++i)
        {
            ItemStack stack = auctionItems.getStack(i);
            if(!stack.isEmpty())
                this.auctionItems.add(stack.copy());
        }
    }

    public AuctionTradeData(PlayerEntity owner) { super(false); this.tradeOwner = PlayerReference.of(owner); this.setDuration(GetDefaultDuration()); }

    public AuctionTradeData(NbtCompound compound) { super(false); this.loadFromNBT(compound); }

    /**
     * Used to create an auction trade from persistent auction data
     */
    public AuctionTradeData(PersistentAuctionData data) {
        super(false);
        this.persistentID = data.id;
        this.setDuration(data.duration);
        this.auctionItems = data.getAuctionItems();
        this.setStartingBid(data.getStartingBid());
        this.setMinBidDifferent(data.getMinimumBidDifference());
    }

    public boolean isActive() { return this.startTime != 0 && !this.cancelled; }

    @Override
    public boolean isValid() {
        if(this.cancelled)
            return false;
        if(this.auctionItems.size() <= 0)
            return false;
        if(this.isActive() && this.hasExpired(TimeUtil.getCurrentTime()))
            return false;
        if(this.minBidDifference.getRawValue() <= 0)
            return false;
        if(this.lastBidAmount.getRawValue() <= 0)
            return false;
        return true;
    }

    public void startTimer() {
        if(!this.isActive())
            this.startTime = TimeUtil.getCurrentTime();
    }

    public long getRemainingTime(long currentTime) {
        if(!this.isActive())
            return this.duration;
        return Math.max(0, this.startTime + this.duration - currentTime);
    }

    public boolean hasExpired(long time) {
        if(this.isActive())
            return time >= this.startTime + this.duration;
        return false;
    }

    public boolean tryMakeBid(AuctionHouseTrader trader, PlayerEntity player, CoinValue amount) {
        if(!validateBidAmount(amount))
            return false;

        PlayerReference oldBidder = this.lastBidPlayer;

        if(this.lastBidPlayer != null)
        {
            //Refund the money to the previous bidder
            AuctionPlayerStorage storage = trader.getStorage(this.lastBidPlayer);
            storage.giveMoney(this.lastBidAmount);
            trader.markStorageDirty();
        }

        this.lastBidPlayer = PlayerReference.of(player);
        this.lastBidAmount = amount.copy();

        //Send notification to the previous bidder letting them know they've been out-bid.
        if(oldBidder != null)
            NotificationSaveData.PushNotification(oldBidder.id, new AuctionHouseBidNotification(this));

        return true;
    }

    public boolean validateBidAmount(CoinValue amount) {
        CoinValue minAmount = this.getMinNextBid();
        return amount.getRawValue() >= minAmount.getRawValue();
    }

    public CoinValue getMinNextBid() {
        return new CoinValue(this.lastBidPlayer == null ? this.lastBidAmount.getRawValue() : this.lastBidAmount.getRawValue() + this.minBidDifference.getRawValue());
    }

    public void ExecuteTrade(AuctionHouseTrader trader) {
        if(this.cancelled)
            return;
        this.cancelled = true;

        //Throw auction completed event
        //TODO auction completed event
        //AuctionCompletedEvent event = new AuctionCompletedEvent(trader, this);
        //MinecraftForge.EVENT_BUS.post(event);

        if(this.lastBidPlayer != null)
        {
            AuctionPlayerStorage buyerStorage = trader.getStorage(this.lastBidPlayer);
            //TODO get auction items from event
            List<ItemStack> rewards = this.getAuctionItems();
            //Reward the items to the last bidder
            for(int i = 0; i < rewards.size(); ++i)
                buyerStorage.giveItem(rewards.get(i));
            //Give the bid money to the trades owner
            if(this.tradeOwner != null)
            {
                AuctionPlayerStorage sellerStorage = trader.getStorage(this.tradeOwner);
                //TODO get money from event
                sellerStorage.giveMoney(this.lastBidAmount);
            }

            //Post notification to the auction winner
            NotificationSaveData.PushNotification(this.lastBidPlayer.id, new AuctionHouseBuyerNotification(this));

            //Post notification to the auction owner
            if(this.tradeOwner != null)
                NotificationSaveData.PushNotification(this.tradeOwner.id, new AuctionHouseSellerNotification(this));
        }
        else
        {
            //Nobody bid on the item(s), return the items to the auction owner
            if(this.tradeOwner != null)
            {
                AuctionPlayerStorage sellerStorage = trader.getStorage(this.tradeOwner);
                //TODO get items from event
                List<ItemStack> items = this.getAuctionItems();
                for(int i = 0; i < items.size(); ++i)
                    sellerStorage.giveItem(items.get(i));

                //Post notification to the auction owner
                NotificationSaveData.PushNotification(this.tradeOwner.id, new AuctionHouseSellerNobidNotification(this));

            }
        }
    }

    public void CancelTrade(AuctionHouseTrader trader, boolean giveToPlayer, PlayerEntity player)
    {
        if(this.cancelled)
            return;
        this.cancelled = true;

        if(this.lastBidPlayer != null)
        {
            //Give a refund to the last bidder
            AuctionPlayerStorage buyerStorage = trader.getStorage(this.lastBidPlayer);
            buyerStorage.giveMoney(this.lastBidAmount);

            //Send cancel notification
            NotificationSaveData.PushNotification(this.lastBidPlayer.id, new AuctionHouseCancelNotification(this));

        }
        //Return the items being sold to their owner
        if(giveToPlayer)
        {
            //Return items to the player who cancelled the trade
            for(ItemStack stack : this.auctionItems) InventoryUtil.GiveToPlayer(player, stack);
        }
        else
        {
            //Return items to the trader owners storage. Ignore the player
            if(this.tradeOwner != null)
            {
                AuctionPlayerStorage sellerStorage = trader.getStorage(this.tradeOwner != null ? this.tradeOwner : PlayerReference.of(player));
                for(ItemStack stack : this.auctionItems) sellerStorage.giveItem(stack);
            }
        }

        //TODO cancel auction event
        //CancelAuctionEvent event = new CancelAuctionEvent(trader, this, player);
        //MinecraftForge.EVENT_BUS.post(event);

    }

    @Override
    public NbtCompound getAsNBT() {
        //Do not run super.getAsNBT() as we don't need to save the price or trade rules.
        NbtCompound compound = new NbtCompound();
        NbtList itemList = new NbtList();
        for(int i = 0; i < this.auctionItems.size(); ++i)
        {
            itemList.add(this.auctionItems.get(i).writeNbt(new NbtCompound()));
        }
        compound.put("SellItems", itemList);
        this.lastBidAmount.save(compound, "LastBid");
        if(this.lastBidPlayer != null)
            compound.put("LastBidPlayer", this.lastBidPlayer.save());

        this.minBidDifference.save(compound, "MinBid");

        compound.putLong("StartTime", this.startTime);
        compound.putLong("Duration", this.duration);

        if(this.tradeOwner != null)
            compound.put("TradeOwner", this.tradeOwner.save());

        compound.putBoolean("Cancelled", this.cancelled);

        if(!this.persistentID.isBlank())
            compound.putString("PersistentID", this.persistentID);

        return compound;
    }

    public JsonObject saveToJson(JsonObject json) {

        for(int i = 0; i < this.auctionItems.size(); ++i)
            json.add("Item" + String.valueOf(i + 1), FileUtil.convertItemStack(this.auctionItems.get(i)));

        json.addProperty("Duration", this.duration);

        json.add("StartingBid", this.lastBidAmount.toJson());

        json.add("MinimumBid", this.minBidDifference.toJson());

        return json;
    }

    @Override
    public void loadFromNBT(NbtCompound compound) {
        //Do not run super.loadFromNBT() as we didn't save the default data in the first place
        NbtList itemList = compound.getList("SellItems", NbtElement.COMPOUND_TYPE);
        this.auctionItems.clear();
        for(int i = 0; i < itemList.size(); ++i)
        {
            ItemStack stack = ItemStack.fromNbt(itemList.getCompound(i));
            if(!stack.isEmpty())
                this.auctionItems.add(stack);
        }
        this.lastBidAmount.load(compound, "LastBid");
        if(compound.contains("LastBidPlayer"))
            this.lastBidPlayer = PlayerReference.load(compound.getCompound("LastBidPlayer"));
        else
            this.lastBidPlayer = null;

        this.minBidDifference.load(compound, "MinBid");

        this.startTime = compound.getLong("StartTime");
        this.duration = compound.getLong("Duration");

        if(compound.contains("TradeOwner", NbtElement.COMPOUND_TYPE))
            this.tradeOwner = PlayerReference.load(compound.getCompound("TradeOwner"));

        this.cancelled = compound.getBoolean("Cancelled");

        if(compound.contains("PersistentID", NbtElement.STRING_TYPE))
            this.persistentID = compound.getString("PersistentID");

    }

    @Override
    public int tradeButtonWidth(TradeContext context) { return 94; }

    @Override
    public DisplayData inputDisplayArea(TradeContext context) { return new DisplayData(1, 1, 34, 16); }

    @Override
    public DisplayData outputDisplayArea(TradeContext context) { return new DisplayData(58, 1, 34, 16); }

    @Override
    public Pair<Integer,Integer> arrowPosition(TradeContext context) { return Pair.of(36, 1); }

    @Override
    public Pair<Integer,Integer> alertPosition(TradeContext context) { return Pair.of(36, 1); }

    @Override
    public boolean hasAlert(TradeContext context) { return false; }

    @Override
    public List<DisplayEntry> getInputDisplays(TradeContext context) { return Lists.newArrayList(DisplayEntry.of(this.lastBidAmount, this.getBidInfo(), true)); }

    private List<Text> getBidInfo() {
        List<Text> bidInfo = new ArrayList<>();
        if(this.lastBidPlayer == null)
        {
            //First bid info
            bidInfo.add(Text.translatable("tooltip.lightmanscurrency.auction.nobidder"));
            bidInfo.add(Text.translatable("tooltip.lightmanscurrency.auction.minbid", this.lastBidAmount.getString()));
        }
        else
        {
            //Last bid info
            bidInfo.add(Text.translatable("tooltip.lightmanscurrency.auction.lastbidder", this.lastBidPlayer.getName(true)));
            bidInfo.add(Text.translatable("tooltip.lightmanscurrency.auction.currentbid", this.lastBidAmount.getString()));
            //Next bid info
            bidInfo.add(Text.translatable("tooltip.lightmanscurrency.auction.minbid", this.getMinNextBid().getString()));
        }
        return bidInfo;
    }

    @Override
    public List<DisplayEntry> getOutputDisplays(TradeContext context) {
        List<DisplayEntry> entries = new ArrayList<>();
        for(int i = 0; i < this.auctionItems.size(); ++i)
        {
            ItemStack item = this.auctionItems.get(i);
            if(!item.isEmpty())
                entries.add(DisplayEntry.of(item, item.getCount(), ItemRenderUtil.getTooltipFromItem(item)));
        }
        return entries;
    }

    @Override
    protected void getAdditionalAlertData(TradeContext context, List<AlertData> alerts) { }

    @Override
    public void onInputDisplayInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) { this.openCancelAuctionTab(tab, clientHandler); }

    @Override
    public void onOutputDisplayInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int index, int button, ItemStack heldItem) { this.openCancelAuctionTab(tab, clientHandler); }

    @Override
    public void onInteraction(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler, int mouseX, int mouseY, int button, ItemStack heldItem) { this.openCancelAuctionTab(tab, clientHandler); }

    private void openCancelAuctionTab(BasicTradeEditTab tab, TraderStorageMenu.IClientMessage clientHandler) {

        TraderData t = tab.menu.getTrader();
        if(t instanceof AuctionHouseTrader)
        {
            AuctionHouseTrader trader = (AuctionHouseTrader)t;
            int tradeIndex = trader.getTradeIndex(this);
            if(tradeIndex < 0)
                return;

            NbtCompound extraData = new NbtCompound();
            extraData.putInt("TradeIndex", tradeIndex);
            tab.sendOpenTabMessage(TraderStorageTab.TAB_TRADE_ADVANCED, extraData);

        }

    }

    @Override
    @Environment(EnvType.CLIENT)
    public void renderAdditional(ClickableWidget button, DrawContext gui, int mouseX, int mouseY, TradeContext context) {
        //Draw remaining time
        TimeUtil.TimeData time = new TimeUtil.TimeData(this.getRemainingTime(TimeUtil.getCurrentTime()));
        TextRenderUtil.drawCenteredText(gui, time.getShortString(1), button.getX() + button.getWidth() / 2, button.getY() + button.getHeight() - 9, this.getTextColor(time));
    }

    public List<Text> getAdditionalTooltips(TradeContext context, int mouseX, int mouseY) {
        TimeUtil.TimeData time = new TimeUtil.TimeData(this.getRemainingTime(TimeUtil.getCurrentTime()));
        return Lists.newArrayList(Text.translatable("gui.lightmanscurrency.auction.time_remaining", Text.literal(time.getString()).styled(s -> s.withColor(this.getTextColor(time)))));
    }

    private int getTextColor(TimeUtil.TimeData remainingTime) {

        if(remainingTime.miliseconds < TimeUtil.DURATION_HOUR)
        {
            if(remainingTime.miliseconds < 5 * TimeUtil.DURATION_MINUTE) //Red if less than 5 minutes
                return 0xFF0000;
            //Yellow if less than 1 hour
            return 0xFFFF00;
        }
        //Green if more than 1 hour
        return 0x00FF00;
    }

    @Override
    public TradeDirection getTradeDirection() { return TradeDirection.NONE; }

    @Override
    public TradeComparisonResult compare(TradeData otherTrade) { return new TradeComparisonResult(); }

    @Override
    public boolean AcceptableDifferences(TradeComparisonResult result) { return false; }

    @Override
    public List<Text> GetDifferenceWarnings(TradeComparisonResult differences) { return new ArrayList<>(); }

}