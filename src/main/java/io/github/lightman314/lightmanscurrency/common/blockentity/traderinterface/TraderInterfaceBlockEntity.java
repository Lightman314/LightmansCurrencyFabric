package io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TickableBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.traderinterface.templates.TraderInterfaceBlock;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.IDumpable;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.ownership.OwnerData;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traderinterface.NetworkTradeReference;
import io.github.lightman314.lightmanscurrency.common.traderinterface.handlers.Handler;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.common.util.IClientTracker;
import io.github.lightman314.lightmanscurrency.network.server.messages.traderinterface.CMessageHandlerMessage;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TraderInterfaceBlockEntity extends TickableBlockEntity implements UpgradeType.IUpgradeable, IDumpable, IClientTracker, SidedStorageBlockEntity {

    public static final int INTERACTION_DELAY = 20;

    private boolean allowRemoval = false;
    public boolean allowRemoval() { return this.allowRemoval; }
    public void flagAsRemovable() { this.allowRemoval = true; }

    public enum InteractionType {
        RESTOCK_AND_DRAIN(true, true, true, false, 3),
        RESTOCK(true, true, false, false, 1),
        DRAIN(true, false, true, false, 2),
        TRADE(false, false, false, true, 0);

        public final boolean requiresPermissions;
        public final boolean restocks;
        public final boolean drains;
        public final boolean trades;
        public final int index;
        public final Text getDisplayText() { return Text.translatable("gui.lightmanscurrency.interface.type." + this.name().toLowerCase()); }

        public final InteractionType getNext() { return fromIndex(this.index + 1); }
        InteractionType(boolean requiresPermissions, boolean restocks, boolean drains, boolean trades, int index) {
            this.requiresPermissions =  requiresPermissions;
            this.restocks = restocks;
            this.drains = drains;
            this.trades = trades;
            this.index = index;
        }

        public static InteractionType fromIndex(int index) {
            for(InteractionType type : InteractionType.values())
            {
                if(type.index == index)
                    return type;
            }
            return TRADE;
        }

        public static int size() { return 4; }

    }

    public enum ActiveMode {
        DISABLED(0, be -> false),
        REDSTONE_OFF(1, be -> {
            if(be.world != null)
                return !be.world.isReceivingRedstonePower(be.pos);
            return false;
        }),
        REDSTONE_ONLY(2, be ->{
            if(be.world != null)
                return be.world.isReceivingRedstonePower(be.pos);
            return false;
        }),
        ALWAYS_ON(3, be -> true);

        public final int index;
        public final Text getDisplayText() { return Text.translatable("gui.lightmanscurrency.interface.mode." + this.name().toLowerCase()); }
        public final ActiveMode getNext() { return fromIndex(this.index + 1); }

        private final Function<TraderInterfaceBlockEntity,Boolean> active;
        public boolean isActive(TraderInterfaceBlockEntity blockEntity) { return this.active.apply(blockEntity); }

        ActiveMode(int index, Function<TraderInterfaceBlockEntity,Boolean> active) { this.index = index; this.active = active;}

        public static ActiveMode fromIndex(int index) {
            for(ActiveMode mode : ActiveMode.values())
            {
                if(mode.index == index)
                    return mode;
            }
            return DISABLED;
        }
    }

    public final OwnerData owner = new OwnerData(this, o -> BlockEntityUtil.sendUpdatePacket(this, this.saveOwner(this.saveMode(new NbtCompound()))));
    public void initOwner(PlayerEntity owner) { if(!this.owner.hasOwner()) this.owner.SetOwner(PlayerReference.of(owner)); }
    public void setOwner(String name) {
        PlayerReference newOwner = PlayerReference.of(this.isClient(), name);
        if(newOwner != null)
        {
            this.owner.SetOwner(newOwner);
            this.mode = ActiveMode.DISABLED;
            this.markDirty();
            if(!this.isClient())
                BlockEntityUtil.sendUpdatePacket(this, this.saveOwner(this.saveMode(new NbtCompound())));
        }
    }
    public void setTeam(long teamID) {
        Team team = TeamSaveData.GetTeam(this.isClient(), teamID);
        if(team != null)
            this.owner.SetOwner(team);
    }

    public PlayerReference getReferencedPlayer() { return this.owner.getPlayerForContext(); }

    public String getOwnerName() { return this.owner.getOwnerName(this.isClient()); }

    public BankAccount getBankAccount() {
        BankAccount.AccountReference reference = this.getAccountReference();
        if(reference != null)
            return reference.get();
        return null;
    }
    public BankAccount.AccountReference getAccountReference() {
        if(this.getOwner().hasTeam())
            return BankAccount.GenerateReference(this.isClient(), this.owner.getTeam());
        if(this.owner != null)
            return BankAccount.GenerateReference(this.isClient(), this.owner.getPlayer());
        return null;
    }

    List<Handler> handlers = new ArrayList<>();

    private ActiveMode mode = ActiveMode.DISABLED;
    public ActiveMode getMode() { return this.mode; }
    public void setMode(ActiveMode mode) { this.mode = mode; this.setModeDirty(); }

    private boolean onlineMode = false;
    public boolean isOnlineMode() { return this.onlineMode; }
    public void setOnlineMode(boolean onlineMode) { this.onlineMode = onlineMode; this.setOnlineModeDirty(); }

    private InteractionType interaction = InteractionType.TRADE;
    public InteractionType getInteractionType() { return this.interaction; }
    public void setInteractionType(InteractionType type) {
        if(this.getBlacklistedInteractions().contains(type))
        {
            LightmansCurrency.LogInfo("Attempted to set interaction type to " + type.name() + ", but that type is blacklisted for this interface type (" + this.getClass().getName() + ").");
            return;
        }
        this.interaction = type;
        this.setInteractionDirty();
    }
    public List<InteractionType> getBlacklistedInteractions() { return new ArrayList<>(); }

    NetworkTradeReference reference = new NetworkTradeReference(this::isClient, this::deserializeTrade);
    public boolean hasTrader() { return this.getTrader() != null; }
    public TraderData getTrader() {
        TraderData trader = this.reference.getTrader();
        if(this.interaction.requiresPermissions && !this.hasTraderPermissions(trader))
            return null;
        return trader;
    }
    public int getTradeIndex() { return this.reference.getTradeIndex(); }
    public TradeData getReferencedTrade() { return this.reference.getLocalTrade(); }
    public TradeData getTrueTrade() { return this.reference.getTrueTrade(); }

    private SimpleInventory upgradeSlots = new SimpleInventory(5);
    public Inventory getUpgradeInventory() { return this.upgradeSlots; }

    public void setUpgradeSlotsDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveUpgradeSlots(new NbtCompound()));
    }

    public void setTrader(long traderID) {
        //Trader is the same id. Ignore the change.
        if(this.reference.getTraderID() == traderID)
            return;
        this.reference.setTrader(traderID);
        this.reference.setTrade(-1);
        this.setTradeReferenceDirty();
    }

    public void setTradeIndex(int tradeIndex) {
        this.reference.setTrade(tradeIndex);
        this.setTradeReferenceDirty();
    }

    public void acceptTradeChanges() {
        this.reference.refreshTrade();
        this.setTradeReferenceDirty();
    }

    private TradeResult lastResult = TradeResult.SUCCESS;
    public TradeResult mostRecentTradeResult() { return this.lastResult; }

    protected abstract TradeData deserializeTrade(NbtCompound compound);

    private int waitTimer = INTERACTION_DELAY;

    public boolean canAccess(PlayerEntity player) { return this.owner.isMember(player); }

    /**
     * Whether the given player has owner-level permissions.
     * If owned by a team, this will return true for team admins & the team owner.
     */
    public boolean isOwner(PlayerEntity player) { return this.owner.isAdmin(player); }

    protected TraderInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    @Nullable
    public final Storage<FluidVariant> getFluidStorage(Direction side) {
        Direction relativeSide = this.getRelativeSide(side);
        for(Handler handler : this.handlers)
        {
            if(handler.hasFluidStorage(relativeSide))
                return handler.getFluidStorage(relativeSide);
        }
        return null;
    }
    @Override
    @Nullable
    public final Storage<ItemVariant> getItemStorage(Direction side) {
        Direction relativeSide = this.getRelativeSide(side);
        for(Handler handler : this.handlers)
        {
            if(handler.hasItemStorage(relativeSide))
                return handler.getItemStorage(relativeSide);
        }
        return null;
    }

    public void setModeDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveMode(new NbtCompound()));
    }

    public void setOnlineModeDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveOnlineMode(new NbtCompound()));
    }

    public void setLastResultDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveLastResult(new NbtCompound()));
    }

    protected abstract TradeContext.Builder buildTradeContext(TradeContext.Builder baseContext);

    //Don't mark final to prevent conflicts with LC Tech not yet updating to the new method
    public TradeContext getTradeContext() {
        if(this.interaction.trades)
            return this.buildTradeContext(TradeContext.create(this.getTrader(), this.getReferencedPlayer()).withBankAccount(this.getAccountReference()).withMoneyListener(this::trackMoneyInteraction)).build();
        return TradeContext.createStorageMode(this.getTrader());
    }

    public boolean isClient() { return this.world != null ? this.world.isClient : true; }

    protected final <H extends Handler> H addHandler(@NotNull H handler) {
        handler.setParent(this);
        this.handlers.add(handler);
        return handler;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return this.createNbt(); }

    @Override
    protected void writeNbt(NbtCompound compound) {
        this.saveOwner(compound);
        this.saveMode(compound);
        this.saveOnlineMode(compound);
        this.saveInteraction(compound);
        this.saveLastResult(compound);
        this.saveReference(compound);
        this.saveUpgradeSlots(compound);
        for(Handler handler : this.handlers) this.saveHandler(compound, handler);
    }

    protected final NbtCompound saveOwner(NbtCompound compound) {
        if(this.owner != null)
            compound.put("Owner", this.owner.save());
        return compound;
    }

    protected final NbtCompound saveMode(NbtCompound compound) {
        compound.putString("Mode", this.mode.name());
        return compound;
    }

    protected final NbtCompound saveOnlineMode(NbtCompound compound) {
        compound.putBoolean("OnlineMode", this.onlineMode);
        return compound;
    }

    protected final NbtCompound saveInteraction(NbtCompound compound) {
        compound.putString("InteractionType", this.interaction.name());
        return compound;
    }

    protected final NbtCompound saveLastResult(NbtCompound compound) {
        compound.putString("LastResult", this.lastResult.name());
        return compound;
    }

    protected final NbtCompound saveReference(NbtCompound compound) {
        compound.put("Trade", this.reference.save());
        return compound;
    }

    protected final NbtCompound saveUpgradeSlots(NbtCompound compound) {
        InventoryUtil.saveAllItems("Upgrades", compound, this.upgradeSlots);
        return compound;
    }

    protected final NbtCompound saveHandler(NbtCompound compound, Handler handler) {
        compound.put(handler.getTag(), handler.save());
        return compound;
    }

    public void setHandlerDirty(Handler handler) {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveHandler(new NbtCompound(), handler));
    }

    @Override
    public void readNbt(NbtCompound compound) {
        if(compound.contains("Owner", NbtElement.COMPOUND_TYPE))
            this.owner.load(compound.getCompound("Owner"));
        if(compound.contains("Mode"))
            this.mode = EnumUtil.enumFromString(compound.getString("Mode"), ActiveMode.values(), ActiveMode.DISABLED);
        if(compound.contains("OnlineMode"))
            this.onlineMode = compound.getBoolean("OnlineMode");
        if(compound.contains("InteractionType", NbtElement.STRING_TYPE))
            this.interaction = EnumUtil.enumFromString(compound.getString("InteractionType"), InteractionType.values(), InteractionType.TRADE);
        if(compound.contains("Trade", NbtElement.COMPOUND_TYPE))
            this.reference.load(compound.getCompound("Trade"));
        if(compound.contains("Upgrades"))
            this.upgradeSlots = InventoryUtil.loadAllItems("Upgrades", compound, 5);
        for(Handler handler : this.handlers) {
            if(compound.contains(handler.getTag(), NbtElement.COMPOUND_TYPE))
                handler.load(compound.getCompound(handler.getTag()));
        }
    }

    public void setInteractionDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveInteraction(new NbtCompound()));
    }

    public final Direction getRelativeSide(Direction side) {
        Direction relativeSide = side;
        if(relativeSide != null & this.getCachedState().getBlock() instanceof IRotatableBlock)
            relativeSide = IRotatableBlock.getRelativeSide(((IRotatableBlock)this.getCachedState().getBlock()).getFacing(this.getCachedState()), side);
        return relativeSide;
    }

    public void sendHandlerMessage(Identifier type, NbtCompound message) {
        if(this.isClient())
            new CMessageHandlerMessage(this.pos, type, message).sendToServer();
    }

    public void receiveHandlerMessage(Identifier type, PlayerEntity player, NbtCompound message) {
        if(!this.canAccess(player))
            return;
        for(int i = 0; i < this.handlers.size(); ++i) {
            if(this.handlers.get(i).getType().equals(type))
                this.handlers.get(i).receiveMessage(message);
        }
    }

    public void setTradeReferenceDirty() {
        this.markDirty();
        if(!this.isClient())
            BlockEntityUtil.sendUpdatePacket(this, this.saveReference(new NbtCompound()));
    }

    public TradeResult interactWithTrader() {
        TradeContext tradeContext = this.getTradeContext();
        TraderData trader = this.getTrader();
        if(trader != null)
            this.lastResult = trader.ExecuteTrade(tradeContext, this.reference.getTradeIndex());
        else
            this.lastResult = TradeResult.FAIL_NULL;
        this.setLastResultDirty();
        return this.lastResult;
    }

    protected void trackMoneyInteraction(CoinValue price, boolean isDeposit) { }

    public boolean isActive() { return this.mode.isActive(this) && this.onlineCheck(); }

    public boolean onlineCheck() {
        //Always return false on the client

        if(this.isClient())
            return false;
        if(!this.onlineMode)
            return true;

        MinecraftServer server = ServerHook.getServer();
        if(server == null)
            return false;
        if(this.owner.hasTeam())
        {
            Team team = this.owner.getTeam();
            for(PlayerReference member : team.getAllMembers())
            {
                if(member != null && server.getPlayerManager().getPlayer(member.id) != null)
                    return true;
            }
        }
        else if(this.owner.hasPlayer())
            return server.getPlayerManager().getPlayer(this.owner.getPlayer().id) != null;
        return false;
    }

    public final boolean hasTraderPermissions(TraderData trader) {
        if(trader == null)
            return false;
        Team team = this.owner.getTeam();
        if(team != null)
            return trader.getOwner().getTeam() == team;
        return trader.hasPermission(this.owner.getPlayer(), Permissions.INTERACTION_LINK);
    }

    @Override
    public void serverTick() {
        if(this.isActive())
        {
            this.waitTimer -= 1;
            if(this.waitTimer <= 0)
            {
                this.waitTimer = this.getInteractionDelay();
                if(this.interaction.requiresPermissions)
                {
                    if(!this.validTrader() || !this.hasTraderPermissions(this.getTrader()))
                        return;
                    if(this.interaction.drains)
                        this.drainTick();
                    if(this.interaction.restocks)
                        this.restockTick();
                }
                else if(this.interaction.trades)
                {
                    if(!this.validTrade())
                        return;
                    this.tradeTick();
                }
                if(this.hasHopperUpgrade())
                {
                    this.hopperTick();
                }
            }
        }

    }



    //Returns whether the trader referenced is valid
    public boolean validTrader() {
        TraderData trader = this.getTrader();
        return trader != null && this.validTraderType(trader);
    }

    public boolean validTrade() {
        TradeData expectedTrade = this.getReferencedTrade();
        TradeData trueTrade = this.getTrueTrade();
        if(expectedTrade == null || trueTrade == null)
            return false;
        return expectedTrade.AcceptableDifferences(expectedTrade.compare(trueTrade));
    }

    public abstract boolean validTraderType(TraderData trader);

    protected abstract void drainTick();

    protected abstract void restockTick();

    protected abstract void tradeTick();

    protected abstract void hopperTick();

    public void openMenu(PlayerEntity player) {
        if(this.canAccess(player))
        {
            NamedScreenHandlerFactory provider = this.getMenuProvider();
            if(provider == null)
                return;
            player.openHandledScreen(provider);
        }
    }

    protected ExtendedScreenHandlerFactory getMenuProvider() { return new InterfaceMenuProvider(this.pos); }

    public static class InterfaceMenuProvider implements ExtendedScreenHandlerFactory {
        private final BlockPos blockPos;
        public InterfaceMenuProvider(BlockPos blockPos) { this.blockPos = blockPos; }
        @Override
        public ScreenHandler createMenu(int windowID, PlayerInventory inventory, PlayerEntity player) { return new TraderInterfaceMenu(windowID, inventory, this.blockPos); }
        @Override
        public Text getDisplayName() { return Text.empty(); }
        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) { buf.writeBlockPos(this.blockPos); }
    }

    protected int getInteractionDelay() {
        int delay = INTERACTION_DELAY;
        for(int i = 0; i < this.upgradeSlots.size() && delay > 1; ++i)
        {
            ItemStack stack = this.upgradeSlots.getStack(i);
            if(stack.getItem() instanceof UpgradeItem)
            {
                UpgradeItem upgrade = (UpgradeItem)stack.getItem();
                if(upgrade.getUpgradeType() instanceof SpeedUpgrade)
                    delay -= UpgradeItem.getUpgradeData(stack).getIntValue(SpeedUpgrade.DELAY_AMOUNT);
            }
        }
        return delay;
    }

    public abstract void initMenuTabs(TraderInterfaceMenu menu);

    public boolean allowUpgrade(UpgradeType type) {
        return type == UpgradeType.SPEED || (type == UpgradeType.HOPPER && this.allowHopperUpgrade() && !this.hasHopperUpgrade()) || this.allowAdditionalUpgrade(type);
    }

    protected boolean allowHopperUpgrade() { return true; }

    protected boolean allowAdditionalUpgrade(UpgradeType type) { return false; }

    protected final boolean hasHopperUpgrade() { return UpgradeType.hasUpgrade(UpgradeType.HOPPER, this.upgradeSlots); }

    public final List<ItemStack> getContents(World level, BlockPos pos, BlockState state, boolean dropBlock) {
        List<ItemStack> contents = new ArrayList<>();

        //Drop trader block
        if(dropBlock)
        {
            if(state.getBlock() instanceof TraderInterfaceBlock)
                contents.add(((TraderInterfaceBlock)state.getBlock()).getDropBlockItem(state, this));
            else
                contents.add(new ItemStack(state.getBlock()));
        }

        //Drop upgrade slots
        for(int i = 0; i < this.upgradeSlots.size(); ++i)
        {
            if(!this.upgradeSlots.getStack(i).isEmpty())
                contents.add(this.upgradeSlots.getStack(i));
        }

        //Dump contents
        this.getAdditionalContents(contents);
        return contents;

    }

    protected abstract void getAdditionalContents(List<ItemStack> contents);

    @Override
    public OwnerData getOwner() { return this.owner; }


}