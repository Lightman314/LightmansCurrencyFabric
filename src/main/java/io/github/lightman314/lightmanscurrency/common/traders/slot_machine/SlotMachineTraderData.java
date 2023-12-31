package io.github.lightman314.lightmanscurrency.common.traders.slot_machine;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menu.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.slot_machine.*;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.SlotMachineTradeNotification;
import io.github.lightman314.lightmanscurrency.common.traders.InteractionSlotData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade_data.SlotMachineTrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlotMachineTraderData extends TraderData implements TraderItemStorage.ITraderItemFilter {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "slot_machine_trader");

    private CoinValue price = new CoinValue();
    public final CoinValue getPrice() { return this.price.copy(); }
    public void setPrice(CoinValue newValue) { this.price = newValue.copy(); this.markPriceDirty(); }
    public final boolean isPriceValid() { return this.price.isValid(); }

    private List<ItemStack> lastReward = new ArrayList<>();
    public List<ItemStack> getLastRewards() { return ImmutableList.copyOf(this.lastReward); }

    private final List<SlotMachineEntry> entries = Lists.newArrayList(SlotMachineEntry.create());
    public final List<SlotMachineEntry> getAllEntries() { return new ArrayList<>(this.entries); }
    public final List<SlotMachineEntry> getValidEntries() { return this.entries.stream().filter(SlotMachineEntry::isValid).toList(); }
    private boolean entriesChanged = false;
    public boolean areEntriesChanged() { return this.entriesChanged; }
    public void clearEntriesChangedCache() { this.entriesChanged = false; }
    public void addEntry() { if(this.entries.size() >= TraderData.GLOBAL_TRADE_LIMIT) return; this.entries.add(SlotMachineEntry.create()); this.markEntriesDirty(); }
    public void removeEntry(int entryIndex) {
        if(entryIndex < 0 || entryIndex >= this.entries.size())
            return;
        this.entries.remove(entryIndex);
        this.markEntriesDirty();
    }
    public final int getTotalWeight() {
        int weight = 0;
        for(SlotMachineEntry entry : this.getValidEntries())
            weight += entry.getWeight();
        return weight;
    }

    @Nullable
    public final SlotMachineEntry getRandomizedEntry(TradeContext context)
    {
        World level;
        if(context.hasPlayer())
            level = context.getPlayer().getWorld();
        else
            return this.getRandomizedEntry(new Random().nextInt(this.getTotalWeight()) + 1);
        return this.getRandomizedEntry(level.random.nextInt(this.getTotalWeight()) + 1);
    }

    private SlotMachineEntry getRandomizedEntry(int rand)
    {
        for(SlotMachineEntry entry : this.getValidEntries())
        {
            rand -= entry.getWeight();
            if(rand <= 0)
                return entry;
        }
        return null;
    }

    public final List<Text> getSlotMachineInfo()
    {
        List<Text> tooltips = new ArrayList<>();
        //Return undefined info if not yet defined
        if(!this.hasValidTrade())
        {
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.undefined").formatted(Formatting.RED));
            return tooltips;
        }

        if(!this.hasStock())
            tooltips.add(EasyText.translatable("tooltip.lightmanscurrency.outofstock").formatted(Formatting.RED));

        return tooltips;
    }

    public String getOdds(int weight)
    {
        DecimalFormat df = new DecimalFormat();
        double odds = ((double)weight/(double)this.getTotalWeight()) * 100d;
        df.setMaximumFractionDigits(odds < 1d ? 2 : 0);
        return df.format(odds);
    }

    private final TraderItemStorage storage = new TraderItemStorage(this);
    public final TraderItemStorage getStorage() { return this.storage; }

    public SlotMachineTraderData() { super(TYPE); }
    public SlotMachineTraderData(@NotNull World level, @NotNull BlockPos pos) { super(TYPE, level, pos); }

    private final ImmutableList<SlotMachineTrade> trade = ImmutableList.of(new SlotMachineTrade(this));

    @Override
    public IconData getIcon() { return IconAndButtonUtil.ICON_TRADER_ALT; }

    @Override
    protected boolean allowAdditionalUpgradeType(UpgradeType type) { return type == UpgradeType.ITEM_CAPACITY; }

    @Override
    public int getTradeCount() { return 1; }

    @Override
    public int getTradeStock(int tradeIndex) { return this.hasStock() ? 1 : 0; }

    public boolean hasStock()
    {
        //Return false if no valid entries exist.
        if(!this.hasValidTrade())
            return false;
        if(this.isCreative())
            return true;
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isValid() && !entry.hasStock(this))
                return false;
        }
        return true;
    }

    @Override
    public boolean hasValidTrade() { return this.entries.stream().anyMatch(SlotMachineEntry::isValid) && this.isPriceValid(); }

    @Override
    protected void saveTrades(NbtCompound compound) { }

    @Override
    protected ExtendedScreenHandlerFactory getTraderMenuProvider() { return new SlotMachineMenuProvider(this.getID()); }

    private record SlotMachineMenuProvider(long traderID) implements ExtendedScreenHandlerFactory {

        @Override
        public ScreenHandler createMenu(int windowID, PlayerInventory inventory, PlayerEntity player) { return new SlotMachineMenu(windowID, inventory, this.traderID); }

        @Override
        public Text getDisplayName() { return EasyText.empty(); }
        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) { buf.writeLong(this.traderID); }
    }

    public final void markStorageDirty() { this.markDirty(this::saveStorage); }
    public final void markLastRewardDirty() { this.markDirty(this::saveLastRewards); }
    public final void markEntriesDirty() { this.markDirty(this::saveEntries); }
    public final void markPriceDirty() { this.markDirty(this::savePrice); }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        this.saveStorage(compound);
        this.saveLastRewards(compound);
        this.saveEntries(compound);
        this.savePrice(compound);
    }

    protected final void saveStorage(NbtCompound compound) { this.storage.save(compound,"Storage"); }

    protected final void saveLastRewards(NbtCompound compound) {
        NbtList itemList = new NbtList();
        for(ItemStack reward : this.lastReward)
        {
            if(reward.isEmpty())
                continue;
            itemList.add(reward.writeNbt(new NbtCompound()));
        }
        compound.put("LastReward", itemList);
    }

    protected final void saveEntries(NbtCompound compound) {
        NbtList list = new NbtList();
        for(SlotMachineEntry entry : this.entries)
            list.add(entry.save());
        compound.put("Entries", list);
    }

    protected final void savePrice(NbtCompound compound) { this.price.save(compound, "Price"); }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        if(compound.contains("Storage"))
            this.storage.load(compound, "Storage");
        if(compound.contains("LastReward"))
        {
            this.lastReward.clear();
            NbtList itemList = compound.getList("LastReward", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = ItemStack.fromNbt(itemList.getCompound(i));
                if(!stack.isEmpty())
                    this.lastReward.add(stack);
            }
        }
        if(compound.contains("Entries"))
        {
            this.entries.clear();
            NbtList list = compound.getList("Entries", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < list.size(); ++i)
                this.entries.add(SlotMachineEntry.load(list.getCompound(i)));
            this.entriesChanged = true;
        }
        if(compound.contains("Price"))
            this.price.load(compound, "Price");
    }

    @Override
    protected void saveAdditionalToJson(JsonObject json) {
        //Price
        json.add("Price", this.price.toJson());
        //Entries
        JsonArray entryList = new JsonArray();
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isValid())
                entryList.add(entry.toJson());
        }
        json.add("Entries", entryList);
    }

    @Override
    protected void loadAdditionalFromJson(JsonObject json) throws Exception {

        if(json.has("Price"))
            this.price = CoinValue.Parse(json.get("Price"));
        else
            throw new JsonSyntaxException("Expected a 'Price' entry!");

        this.entries.clear();
        JsonArray entryList = JsonHelper.getArray(json, "Entries");
        for(int i = 0; i < entryList.size(); ++i)
        {
            try{
                this.entries.add(SlotMachineEntry.parse(JsonHelper.asObject(entryList.get(i), "Entries[" + i + "]")));
            } catch(JsonSyntaxException | InvalidIdentifierException t) { LightmansCurrency.LogError("Error parsing Slot Machine Trader Entry #" + (i + 1), t); }
        }
        if(this.entries.size() == 0)
            throw new JsonSyntaxException("Slot Machine Trader had no valid Entries!");

    }

    //No need for persistent data
    @Override
    protected void saveAdditionalPersistentData(NbtCompound compound) {
        this.saveLastRewards(compound);
    }

    @Override
    protected void loadAdditionalPersistentData(NbtCompound compound) {
        if(compound.contains("LastReward"))
        {
            this.lastReward = new ArrayList<>();
            NbtList itemList = compound.getList("LastReward", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = ItemStack.fromNbt(itemList.getCompound(i));
                if(!stack.isEmpty())
                    this.lastReward.add(stack);
            }
        }
    }

    @Override
    protected void getAdditionalContents(List<ItemStack> results) { results.addAll(this.storage.getSplitContents()); }

    @Override
    public List<SlotMachineTrade> getTradeData() { return this.trade; }

    @Nullable
    public SlotMachineTrade getTrade(int tradeIndex) { return this.trade.get(0); }

    //Trades are not added/removed like other traders
    @Override
    public void addTrade(PlayerEntity requestor) {}
    @Override
    public void removeTrade(PlayerEntity requestor) {}

    @Override
    public TradeContext.TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        if(!this.hasValidTrade())
            return TradeResult.FAIL_INVALID_TRADE;

        SlotMachineTrade trade = this.trade.get(0);
        if(trade == null)
            trade = new SlotMachineTrade(this);

        if(!context.hasPlayerReference())
            return TradeResult.FAIL_NULL;

        if(!this.hasStock())
            return TradeResult.FAIL_OUT_OF_STOCK;

        //Check if the player is allowed to do the trade
        if(this.runPreTradeEvent(context.getPlayerReference(), trade).isCanceled())
            return TradeResult.FAIL_TRADE_RULE_DENIAL;

        //Get the cost of the trade
        CoinValue price = this.runTradeCostEvent(context.getPlayerReference(), trade).getCostResult();

        //Get the Result Items
        SlotMachineEntry loot = this.getRandomizedEntry(context);
        if(loot == null)
        {
            LightmansCurrency.LogError("Slot Machine encountered an error randomizing the loot.");
            return TradeResult.FAIL_NULL;
        }

        //Confirm that the customer can hold the rewards
        if(!loot.CanGiveToCustomer(context))
            return TradeResult.FAIL_NO_OUTPUT_SPACE;

        //Accept the payment
        if(context.getPayment(price))
        {
            if(!loot.GiveToCustomer(this, context))
            {
                //Refund the money taken
                context.givePayment(price);
                return TradeResult.FAIL_NO_OUTPUT_SPACE;
            }

            this.lastReward = loot.getDisplayItems();
            this.markLastRewardDirty();

            //Ignore editing internal storage if this is flagged as creative.
            if(!this.isCreative())
            {
                //Give the paid cost to storage
                this.addStoredMoney(price);

                //Push out of stock notification
                if(!this.hasStock())
                    this.pushNotification(() -> new OutOfStockNotification(this.getNotificationCategory(), -1));
            }

            //Push Notification
            this.pushNotification(SlotMachineTradeNotification.create(loot, price, context.getPlayerReference(), this.getNotificationCategory()));

            //Push the post-trade event
            this.runPostTradeEvent(context.getPlayerReference(), trade, price);

            return TradeResult.SUCCESS;

        }
        else
            return TradeResult.FAIL_CANNOT_AFFORD;
    }

    @Override
    public boolean canMakePersistent() { return true; }

    @Override
    protected void addSettingsTabs(List<SettingsTab> tabs) {

    }

    @Override
    public void initStorageTabs(TraderStorageMenu menu) {

        //Set basic tab to Entry Edit Tab
        menu.setTab(TraderStorageTab.TAB_TRADE_BASIC, new SlotMachineEntryTab(menu));
        //Price tab
        menu.setTab(1, new SlotMachinePriceTab(menu));
        //Storage Tab
        menu.setTab(2, new SlotMachineStorageTab(menu));
    }

    @Override
    @Environment(EnvType.CLIENT)
    protected void addPermissionOptions(List<PermissionOption> options) { }

    @Override
    public void addInteractionSlots(List<InteractionSlotData> interactionSlots) { }

    @Override
    public boolean isItemRelevant(ItemStack item) {
        for(SlotMachineEntry entry : this.entries)
        {
            if(entry.isItemRelevant(item))
                return true;
        }
        return false;
    }

    @Override
    public int getStorageStackLimit() {
        int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
        for(int i = 0; i < this.getUpgrades().size(); ++i)
        {
            ItemStack stack = this.getUpgrades().getStack(i);
            if(stack.getItem() instanceof UpgradeItem upgradeItem)
            {
                if(this.allowUpgrade(upgradeItem) && upgradeItem.getUpgradeType() instanceof CapacityUpgrade)
                    limit += UpgradeItem.getUpgradeData(stack).getIntValue(CapacityUpgrade.CAPACITY);
            }
        }
        return limit;
    }

}