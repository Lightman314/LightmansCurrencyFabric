package io.github.lightman314.lightmanscurrency.common.traders.paygate;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.paygate.PaygateNotification;
import io.github.lightman314.lightmanscurrency.common.traders.ITradeSource;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PaygateTraderData extends TraderData implements ITradeSource<PaygateTradeData> {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "paygate");

    public static final int DURATION_MIN = 1;
    public static final int DURATION_MAX = 1200;

    @Override
    public boolean canShowOnTerminal() { return false; }

    protected List<PaygateTradeData> trades = PaygateTradeData.listOfSize(1);

    public PaygateTraderData() { super(TYPE); }
    public PaygateTraderData(World level, BlockPos pos) { super(TYPE, level, pos); }

    public int getTradeCount() { return this.trades.size(); }

    @Override
    public IconData getIcon() { return IconData.of(Items.REDSTONE_BLOCK); }

    @Override
    protected boolean allowAdditionalUpgradeType(UpgradeType type) { return false; }

    @Override
    public boolean canEditTradeCount() { return true; }

    @Override
    public int getMaxTradeCount() { return 8; }

    @Override
    public void addTrade(PlayerEntity requestor)
    {
        if(this.isClient())
            return;
        if(this.getTradeCount() >= TraderData.GLOBAL_TRADE_LIMIT)
            return;

        if(this.getTradeCount() >= this.getMaxTradeCount() && !CommandLCAdmin.isAdminPlayer(requestor))
        {
            Permissions.PermissionWarning(requestor, "add creative trade slot", Permissions.ADMIN_MODE);
            return;
        }
        if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(requestor, "add trade slot", Permissions.EDIT_TRADES);
            return;
        }
        this.overrideTradeCount(this.getTradeCount() + 1);
    }

    @Override
    public void removeTrade(PlayerEntity requestor)
    {
        if(this.isClient())
            return;
        if(this.getTradeCount() <= 1)
            return;

        if(!this.hasPermission(requestor, Permissions.EDIT_TRADES))
        {
            Permissions.PermissionWarning(requestor, "remove trade slot", Permissions.EDIT_TRADES);
            return;
        }
        this.overrideTradeCount(this.getTradeCount() - 1);
    }

    public void overrideTradeCount(int newTradeCount)
    {
        if(this.getTradeCount() == newTradeCount)
            return;

        int tradeCount = MathUtil.clamp(newTradeCount, 1, TraderData.GLOBAL_TRADE_LIMIT);
        List<PaygateTradeData> oldTrades = this.trades;
        this.trades = PaygateTradeData.listOfSize(tradeCount);
        //Write the old trade data into the array
        for(int i = 0; i < oldTrades.size() && i < this.trades.size(); ++i)
        {
            this.trades.set(i, oldTrades.get(i));
        }

        //Mark trades dirty
        this.markTradesDirty();

    }

    public PaygateTradeData getTrade(int tradeSlot) {
        if(tradeSlot < 0 || tradeSlot >= this.trades.size())
        {
            LightmansCurrency.LogError("Cannot get trade in index " + tradeSlot + " from a trader with only " + this.trades.size() + " trades.");
            return new PaygateTradeData();
        }
        return this.trades.get(tradeSlot);
    }

    public List<PaygateTradeData> getAllTrades() { return new ArrayList<>(this.trades); }

    @Override
    public List<? extends TradeData> getTradeData() { return this.getAllTrades(); }

    public int getTradeStock(int tradeIndex) { return 1; }

    private PaygateBlockEntity cachedBE = null;

    public void setCachedBE(PaygateBlockEntity be) { this.cachedBE = be; }

    private PaygateBlockEntity getBlockEntity() {
        if(this.cachedBE != null && !this.cachedBE.isRemoved())
            return this.cachedBE;
        MinecraftServer server = ServerHook.getServer();
        if(server != null && this.getLevel() != null)
        {
            ServerWorld l = server.getWorld(this.getLevel());
            if(l != null && l.isChunkLoaded(this.getPos()))
            {
                BlockEntity be = l.getBlockEntity(this.getPos());
                if(be instanceof PaygateBlockEntity)
                    return (PaygateBlockEntity)be;
                else if(be == null)
                    LightmansCurrency.LogWarning("Could not get Paygate BE as there is no block entity at its pos (" + this.getPos().toShortString() + "; " + this.getLevel().getValue() + ")");
                else
                    LightmansCurrency.LogWarning("Could not get Paygate BE as its block entity is the wrong type (" + be.getClass().getName() + ")");
            }
            else
            {
                if(l == null)
                    LightmansCurrency.LogWarning("Could not get Paygate BE as its dimension isn't loaded.");
                else
                    LightmansCurrency.LogWarning("Could not get Paygate BE as its pos (" + this.getPos().toShortString() + ";" + this.getLevel().getValue() + ") isn't loaded.");
            }
        }
        else
        {
            if(server == null)
                LightmansCurrency.LogWarning("Could not get Paygate BE as the Server could not be accessed.");
            else
                LightmansCurrency.LogWarning("Could not get Paygate BE as the data has no cached dimension id.");
        }
        return null;
    }

    public boolean isActive() {
        PaygateBlockEntity be = this.getBlockEntity();
        if(be != null)
            return be.isActive();
        return false;
    }

    private void activate(int duration) {
        PaygateBlockEntity be = this.getBlockEntity();
        if(be != null)
            be.activate(duration);
    }

    @Override
    public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        PaygateTradeData trade = this.getTrade(tradeIndex);
        //Abort if the trade is null
        if(trade == null)
        {
            LightmansCurrency.LogError("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
            return TradeResult.FAIL_INVALID_TRADE;
        }

        //Abort if the trade is not valid
        if(!trade.isValid())
        {
            LightmansCurrency.LogWarning("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
            return TradeResult.FAIL_INVALID_TRADE;
        }

        //Abort if the paygate is already activated
        if(this.isActive())
        {
            LightmansCurrency.LogWarning("Paygate is already activated. It cannot be activated until the previous timer is completed.");
            return TradeResult.FAIL_OUT_OF_STOCK;
        }

        //Abort if no player context is given
        if(!context.hasPlayerReference())
            return TradeResult.FAIL_NULL;

        //Check if the player is allowed to do the trade
        if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        //Get the cost of the trade
        CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();

        //Process a ticket trade
        if(trade.isTicketTrade())
        {
            //Abort if we don't have a valid ticket to extract
            if(!trade.canAfford(context))
            {
                LightmansCurrency.LogDebug("Ticket ID " + trade.getTicketID() + " could not be found in the players inventory to pay for trade " + tradeIndex + ". Cannot execute trade.");
                return TradeResult.FAIL_CANNOT_AFFORD;
            }

            //Abort if not enough room to put the ticket stub
            if(!context.canFitItem(new ItemStack(ModItems.TICKET_STUB)))
            {
                LightmansCurrency.LogDebug("Not enough room for the ticket stub. Aborting trade!");
                return TradeResult.FAIL_NO_OUTPUT_SPACE;
            }

            //Trade is valid, collect the ticket
            if(!context.collectTicket(trade.getTicketID()))
            {
                LightmansCurrency.LogDebug("Unable to collect the ticket. Aborting Trade!");
                return TradeResult.FAIL_CANNOT_AFFORD;
            }

            //Give the ticket stub
            context.putItem(new ItemStack(ModItems.TICKET_STUB));

            //Activate the paygate
            this.activate(trade.getDuration());

            //Push Notification
            this.pushNotification(() -> new PaygateNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));

            //We would normally change the internal inventory here, but for ticket trades that's not needed.

            //Push the post-trade event
            this.runPostTradeEvent(context.getPlayerReference(), trade, price);

            return TradeResult.SUCCESS;

        }
        //Process a coin trade
        else
        {
            //Abort if we don't have enough money
            if(!context.getPayment(price))
            {
                LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
                return TradeResult.FAIL_CANNOT_AFFORD;
            }

            //We have collected the payment, activate the paygate
            this.activate(trade.getDuration());

            //Push Notification
            this.pushNotification(() -> new PaygateNotification(trade, price, context.getPlayerReference(), this.getNotificationCategory()));

            //Don't store money if the trader is creative
            if(!this.isCreative())
            {
                //Give the paid cost to storage
                this.addStoredMoney(price);
            }

            //Push the post-trade event
            this.runPostTradeEvent(context.getPlayerReference(), trade, price);

            return TradeResult.SUCCESS;

        }
    }

    @Override
    public boolean hasValidTrade() {
        for(PaygateTradeData trade : this.trades)
        {
            if(trade.isValid())
                return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        this.saveTrades(compound);
    }

    protected final void saveTrades(NbtCompound compound) {
        PaygateTradeData.saveAllData(compound, this.trades);
    }

    @Override
    protected void saveAdditionalToJson(JsonObject json) { }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        //Load Trades
        if(compound.contains(PaygateTradeData.DEFAULT_KEY))
            this.trades = PaygateTradeData.loadAllData(compound);
    }

    @Override
    protected void loadAdditionalFromJson(JsonObject json) { }

    @Override
    protected void saveAdditionalPersistentData(NbtCompound compound) { }

    @Override
    protected void loadAdditionalPersistentData(NbtCompound compound) { }

    @Override
    protected void getAdditionalContents(List<ItemStack> results) { }

    @Override
    public void addInteractionSlots(List<InteractionSlotData> interactionSlots) { }

    @Override
    public boolean canMakePersistent() { return false; }

    @Override
    public void initStorageTabs(TraderStorageMenu menu) {
        menu.setTab(TraderStorageTab.TAB_TRADE_ADVANCED, new PaygateTradeEditTab(menu));
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void addSettingsTabs(List<SettingsTab> tabs) { }

    @Override
    @Environment(EnvType.CLIENT)
    protected void addPermissionOptions(List<PermissionOption> options) { }

}