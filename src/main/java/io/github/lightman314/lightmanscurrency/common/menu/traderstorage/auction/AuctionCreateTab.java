package io.github.lightman314.lightmanscurrency.common.menu.traderstorage.auction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction.AuctionCreateClientTab;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;

public class AuctionCreateTab extends TraderStorageTab {

    public AuctionCreateTab(TraderStorageMenu menu) { super(menu); }

    @Override
    @Environment(EnvType.CLIENT)
    public TraderStorageClientTab<?> createClientTab(TraderStorageScreen screen) { return new AuctionCreateClientTab(screen, this); }

    @Override
    public boolean canOpen(PlayerEntity player) { return this.menu.getTrader() instanceof AuctionHouseTrader; }

    List<SimpleSlot> slots = new ArrayList<>();
    public List<SimpleSlot> getSlots() { return this.slots; }
    SimpleInventory auctionItems = new SimpleInventory(2);
    public SimpleInventory getAuctionItems() { return this.auctionItems; }

    @Override
    public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {

        for(int i = 0; i < this.auctionItems.size(); ++i)
        {
            SimpleSlot newSlot = new SimpleSlot(this.auctionItems, i, TraderMenu.SLOT_OFFSET + 8 + i * 18, 122);
            addSlot.apply(newSlot);
            this.slots.add(newSlot);
        }
        SimpleSlot.SetActive(this.slots, false);

    }

    @Override
    public void onTabOpen() {
        SimpleSlot.SetActive(this.slots);
        for(SimpleSlot slot : this.slots)
            slot.locked = false;
    }

    @Override
    public void onTabClose() {
        SimpleSlot.SetInactive(this.slots);
        this.menu.clearContainer(this.auctionItems);
    }

    @Override
    public void onMenuClose() {
        this.menu.clearContainer(this.auctionItems);
    }

    public void createAuction(AuctionTradeData trade) {
        TraderData t = this.menu.getTrader();
        if(t instanceof AuctionHouseTrader trader)
        {
            if(this.menu.isClient())
            {
                NbtCompound message = new NbtCompound();
                message.put("CreateAuction", trade.getAsNBT());
                this.menu.sendMessage(message);
                return;
            }
            //Set the trade's auction items based on the items currently in the auction item slots
            trade.setAuctionItems(this.auctionItems);
            if(!trade.isValid())
            {
                //Send failure message to the client.
                NbtCompound message = new NbtCompound();
                message.putBoolean("AuctionCreated", false);
                this.menu.sendMessage(message);
                //LightmansCurrency.LogInfo("Failed to create the auction as the auction is not valid.");
                return;
            }
            trader.addTrade(trade, false);
            //Delete the contents of the auctionItems
            this.auctionItems.clear();
            //Send response message to the client
            NbtCompound message = new NbtCompound();
            message.putBoolean("AuctionCreated", true);
            this.menu.sendMessage(message);
            for(SimpleSlot slot : this.slots) slot.locked = true;
            //LightmansCurrency.LogInfo("Successfully created the auction!");
        }
    }

    @Override
    public void receiveMessage(NbtCompound message)
    {
        if(message.contains("CreateAuction"))
        {
            //LightmansCurrency.LogInfo("Received Auction from the client.\n" + message.getCompound("CreateAuction").getAsString());
            this.createAuction(new AuctionTradeData(message.getCompound("CreateAuction")));
        }
    }

}