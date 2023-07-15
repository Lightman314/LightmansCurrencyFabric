package io.github.lightman314.lightmanscurrency.common.menu;

import java.util.HashMap;
import java.util.Map;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.InfoTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.OwnershipTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.TradeSelectTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.TraderSelectTab;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.network.server.messages.traderinterface.CMessageInterfaceInteraction;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class TraderInterfaceMenu extends Menu {

    private final TraderInterfaceBlockEntity traderInterface;
    public final TraderInterfaceBlockEntity getTraderInterface() { return this.traderInterface; }

    public final PlayerEntity player;

    public static final int SLOT_OFFSET = 15;

    private boolean canEditTabs = true;
    Map<Integer, TraderInterfaceTab> availableTabs = new HashMap<>();
    public Map<Integer,TraderInterfaceTab> getAllTabs() { return this.availableTabs; }
    public void setTab(int key, TraderInterfaceTab tab) { if(canEditTabs && tab != null) this.availableTabs.put(key, tab); else if(tab == null) LightmansCurrency.LogError("Attempted to set a null storage tab in slot " + key); else LightmansCurrency.LogError("Attempted to define the tab in " + key + " but the tabs have been locked."); }
    int currentTab = TraderInterfaceTab.TAB_INFO;
    public int getCurrentTabIndex() { return this.currentTab; }
    public TraderInterfaceTab getCurrentTab() { return this.availableTabs.get(this.currentTab); }

    public boolean isClient() { return this.player.getWorld().isClient; }

    public TraderInterfaceMenu(int windowID, PlayerInventory inventory, BlockPos blockPos) {
        super(ModMenus.TRADER_INTERFACE, windowID);

        this.player = inventory.player;
        BlockEntity blockEntity = this.player.getWorld().getBlockEntity(blockPos);
        if(blockEntity instanceof TraderInterfaceBlockEntity)
            this.traderInterface = (TraderInterfaceBlockEntity)blockEntity;
        else
            this.traderInterface = null;

        this.setTab(TraderInterfaceTab.TAB_INFO, new InfoTab(this));
        this.setTab(TraderInterfaceTab.TAB_TRADER_SELECT, new TraderSelectTab(this));
        this.setTab(TraderInterfaceTab.TAB_TRADE_SELECT, new TradeSelectTab(this));
        this.setTab(TraderInterfaceTab.TAB_OWNERSHIP, new OwnershipTab(this));
        if(this.traderInterface != null)
            this.traderInterface.initMenuTabs(this);
        this.canEditTabs = false;

        //Player inventory
        for(int y = 0; y < 3; y++)
        {
            for(int x = 0; x < 9; x++)
            {
                this.addSlot(new Slot(inventory, x + y * 9 + 9, SLOT_OFFSET + 8 + x * 18, 154 + y * 18));
            }
        }
        //Player hotbar
        for(int x = 0; x < 9; x++)
        {
            this.addSlot(new Slot(inventory, x, SLOT_OFFSET + 8 + x * 18, 212));
        }

        this.availableTabs.forEach((key, tab) -> tab.addStorageMenuSlots(this::addSlot));

        //Run the tab open code for the current tab
        try {
            this.getCurrentTab().onTabOpen();
        } catch(Throwable t) { t.printStackTrace(); }

    }

    @Override
    public boolean canUse(PlayerEntity player) { return this.traderInterface != null && !this.traderInterface.isRemoved() && this.traderInterface.canAccess(player); }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.availableTabs.forEach((key, tab) -> tab.onMenuClose());
    }


    public TradeContext getTradeContext() {
        return this.traderInterface.getTradeContext();
    }

    @Override
    public ItemStack quickMove(PlayerEntity playerEntity, int index)
    {

        ItemStack clickedStack = ItemStack.EMPTY;

        Slot slot = this.slots.get(index);

        if(slot != null && slot.hasStack())
        {
            ItemStack slotStack = slot.getStack();
            clickedStack = slotStack.copy();
            if(index < 36)
            {
                //Move from inventory to current tab
                if(!this.getCurrentTab().quickMoveStack(slotStack))
                {
                    //Else, move from inventory to additional slots
                    if(!this.insertItem(slotStack, 36, this.slots.size(), false))
                    {
                        return ItemStack.EMPTY;
                    }
                }
            }
            else if(index < this.slots.size())
            {
                //Move from coin/interaction slots to inventory
                if(!this.insertItem(slotStack, 0, 36, false))
                {
                    return ItemStack.EMPTY;
                }
            }

            if(slotStack.isEmpty())
            {
                slot.setStack(ItemStack.EMPTY);
            }
            else
            {
                slot.markDirty();
            }
        }

        return clickedStack;

    }

    public void changeTab(int key, NbtCompound extraData) {
        if(this.currentTab == key)
            return;
        if(this.availableTabs.containsKey(key))
        {
            if(this.availableTabs.get(key).canOpen(this.player))
            {
                //Close the old tab
                this.getCurrentTab().onTabClose();
                //Change the tab
                this.currentTab = key;
                //Open the new tab
                this.getCurrentTab().onTabOpen();
                this.getCurrentTab().receiveMessage(extraData);
            }
        }
        else
            LightmansCurrency.LogWarning("Trader Storage Menu doesn't have a tab defined for " + key);
    }

    public void changeMode(TraderInterfaceBlockEntity.ActiveMode newMode) {
        this.traderInterface.setMode(newMode);
        if(this.isClient())
        {
            NbtCompound message = new NbtCompound();
            message.putInt("ModeChange", newMode.index);
            this.sendMessage(message);
        }
    }

    public void setOnlineMode(boolean newMode) {
        this.traderInterface.setOnlineMode(newMode);
        if(this.isClient())
        {
            NbtCompound message = new NbtCompound();
            message.putBoolean("OnlineModeChange", newMode);
            this.sendMessage(message);
        }
    }

    public NbtCompound createTabChangeMessage(int newTab, @Nullable NbtCompound extraData) {
        NbtCompound message = extraData == null ? new NbtCompound() : extraData;
        message.putInt("ChangeTab", newTab);
        return message;
    }

    public void sendMessage(NbtCompound message) {
        if(this.isClient())
        {
            new CMessageInterfaceInteraction(message).sendToServer();
            //LightmansCurrency.LogInfo("Sending message:\n" + message.getAsString());
        }
    }

    public void receiveMessage(NbtCompound message) {
        //LightmansCurrency.LogInfo("Received nessage:\n" + message.getAsString());
        if(message.contains("ChangeTab", NbtElement.INT_TYPE))
            this.changeTab(message.getInt("ChangeTab"), message);
        if(message.contains("ModeChange"))
            this.changeMode(TraderInterfaceBlockEntity.ActiveMode.fromIndex(message.getInt("ModeChange")));
        if(message.contains("OnlineModeChange"))
            this.setOnlineMode(message.getBoolean("OnlineModeChange"));
        try { this.getCurrentTab().receiveMessage(message); }
        catch(Throwable t) { }
    }

    public interface IClientMessage {
        public void selfMessage(NbtCompound message);
    }

}