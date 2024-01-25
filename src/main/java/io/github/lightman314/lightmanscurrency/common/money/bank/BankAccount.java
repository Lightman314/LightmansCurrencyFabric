package io.github.lightman314.lightmanscurrency.common.money.bank;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.data.ClientBankData;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationSaveData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.BankTransferNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.DepositWithdrawNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.bank.LowBalanceNotification;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class BankAccount {

    public enum AccountType { Player(0), Team(1);

        public final int id;

        AccountType(int id) { this.id = id; }

        public static AccountType fromID(int id) {
            for(AccountType type : AccountType.values())
                if(type.id == id)
                    return type;
            return AccountType.Player;
        }

    }

    private final IMarkDirty markDirty;

    private CoinValue coinStorage = new CoinValue();
    public CoinValue getCoinStorage() { return this.coinStorage; }

    private CoinValue notificationLevel = new CoinValue();
    public CoinValue getNotificationValue() { return this.notificationLevel; }
    public long getNotificationLevel() { return this.notificationLevel.getRawValue(); }
    public void setNotificationValue(CoinValue value) { this.notificationLevel = value.copy(); this.markDirty(); }

    private Consumer<Supplier<Notification>> notificationSender;
    public void setNotificationConsumer(Consumer<Supplier<Notification>> notificationSender) { this.notificationSender = notificationSender; }
    public void pushLocalNotification(Notification notification) {
        this.logger.addNotification(notification);
        this.markDirty();
    }
    public void pushNotification(Supplier<Notification> notification) {
        this.pushLocalNotification(notification.get());
        if(this.notificationSender != null)
            this.notificationSender.accept(notification);
    }

    public static Consumer<Supplier<Notification>> generateNotificationAcceptor(UUID playerID) {
        return (notification) -> {
            NotificationSaveData.PushNotification(playerID, notification.get());
        };
    }

    private NotificationData logger = new NotificationData();
    public List<Notification> getNotifications() { return this.logger.getNotifications(); }

    private String ownerName = "Unknown";
    public String getOwnersName() { return this.ownerName; }
    public void updateOwnersName(String ownerName) { this.ownerName = ownerName; }
    public MutableText getName() { return Text.translatable("lightmanscurrency.bankaccount", this.ownerName); }

    public void depositCoins(CoinValue depositAmount) {
        this.coinStorage = new CoinValue(this.coinStorage.getRawValue() + depositAmount.getRawValue());
        this.markDirty();
    }

    public CoinValue withdrawCoins(CoinValue withdrawAmount) {
        long oldValue = this.coinStorage.getRawValue();
        if(withdrawAmount.getRawValue() > this.coinStorage.getRawValue())
            withdrawAmount = this.coinStorage.copy();
        //Cannot withdraw no money
        if(withdrawAmount.getRawValue() <= 0)
            return CoinValue.EMPTY;
        this.coinStorage.loadFromOldValue(this.coinStorage.getRawValue() - withdrawAmount.getRawValue());
        this.markDirty();
        //Check if we should push the notification
        if(oldValue >= this.getNotificationLevel() && this.coinStorage.getRawValue() < this.getNotificationLevel())
            this.pushNotification(() -> new LowBalanceNotification(this.getName(), this.notificationLevel));
        return withdrawAmount;
    }

    public void LogInteraction(PlayerEntity player, CoinValue amount, boolean isDeposit) {
        this.pushLocalNotification(new DepositWithdrawNotification.Player(PlayerReference.of(player), this.getName(), isDeposit, amount));
        this.markDirty();
    }

    public void LogInteraction(TraderData trader, CoinValue amount, boolean isDeposit) {
        this.pushLocalNotification(new DepositWithdrawNotification.Trader(trader.getName(), this.getName(), isDeposit, amount));
        this.markDirty();
    }

    public void LogTransfer(PlayerEntity player, CoinValue amount, MutableText otherAccount, boolean wasReceived) {
        this.pushLocalNotification(new BankTransferNotification(PlayerReference.of(player), amount, this.getName(), otherAccount, wasReceived));
        this.markDirty();
    }

    public static void DepositCoins(IBankAccountMenu menu, CoinValue amount)
    {
        if(menu == null)
            return;
        DepositCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
    }

    public static void DepositCoins(PlayerEntity player, Inventory coinInput, BankAccount account, CoinValue amount)
    {
        if(account == null)
            return;

        CoinValue actualAmount = MoneyUtil.getCoinValue(coinInput);
        //If amount is not defined, or the amount is more than the amount available, set the amount to deposit to the actual amount
        if(amount.getRawValue() > actualAmount.getRawValue() || amount.getRawValue() <= 0)
            amount = actualAmount;
        //Handle deposit removal the same as a payment
        MoneyUtil.ProcessPayment(coinInput, player, amount, true);
        //Add the deposit amount to the account
        account.depositCoins(amount);
        account.LogInteraction(player, amount, true);

    }

    public static void WithdrawCoins(IBankAccountMenu menu, CoinValue amount)
    {
        if(menu == null)
            return;
        WithdrawCoins(menu.getPlayer(), menu.getCoinInput(), menu.getBankAccount(), amount);
    }

    public static void WithdrawCoins(PlayerEntity player, Inventory coinOutput, BankAccount account, CoinValue amount)
    {
        if(account == null || amount.getRawValue() <= 0)
            return;

        CoinValue withdrawnAmount = account.withdrawCoins(amount);

        List<ItemStack> coins = MoneyUtil.getCoinsOfValue(withdrawnAmount);
        //Attempt to fill the coins into the coin slots
        for(int i = 0; i < coins.size(); ++i)
        {
            ItemStack remainder = InventoryUtil.TryPutItemStack(coinOutput, coins.get(i));
            if(!remainder.isEmpty())
            {
                //Attempt to give it to the player directly
                if(!player.giveItemStack(remainder))
                {
                    //Drop the remainder on the ground
                    InventoryUtil.dumpContents(player.getWorld(), player.getBlockPos(), remainder);
                }
            }
        }
        account.LogInteraction(player, withdrawnAmount, false);
    }

    public static MutableText TransferCoins(IBankAccountAdvancedMenu menu, CoinValue amount, AccountReference destination)
    {
        return TransferCoins(menu.getPlayer(), menu.getBankAccount(), amount, destination.get());
    }

    public static MutableText TransferCoins(PlayerEntity player, BankAccount fromAccount, CoinValue amount, BankAccount destinationAccount)
    {
        if(fromAccount == null)
            return Text.translatable("gui.bank.transfer.error.null.from");
        if(destinationAccount == null)
            return Text.translatable("gui.bank.transfer.error.null.to");
        if(amount.getRawValue() <= 0)
            return Text.translatable("gui.bank.transfer.error.amount", amount.getString("nothing"));
        if(fromAccount == destinationAccount)
            return Text.translatable("gui.bank.transfer.error.same");

        CoinValue withdrawnAmount = fromAccount.withdrawCoins(amount);
        if(withdrawnAmount.getRawValue() <= 0)
            return Text.translatable("gui.bank.transfer.error.nobalance", amount.getString());

        destinationAccount.depositCoins(withdrawnAmount);
        fromAccount.LogTransfer(player, withdrawnAmount, destinationAccount.getName(), false);
        destinationAccount.LogTransfer(player, withdrawnAmount, fromAccount.getName(), true);

        return Text.translatable("gui.bank.transfer.success", withdrawnAmount.getString(), destinationAccount.getName());

    }

    public static boolean ServerGiveCoins(BankAccount account, CoinValue amount)
    {
        if(account == null || amount.getEntries().size() == 0)
            return false;

        account.depositCoins(amount);
        account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), true, amount.copy()));
        return true;
    }

    public static Pair<Boolean, CoinValue> ServerTakeCoins(BankAccount account, CoinValue amount)
    {
        if(account == null || amount.getEntries().size() == 0)
            return Pair.of(false, new CoinValue());

        CoinValue taken = account.withdrawCoins(amount);
        account.pushNotification(() -> new DepositWithdrawNotification.Server(account.getName(), false, taken));
        return Pair.of(true,taken);
    }

    public BankAccount() { this((IMarkDirty)null); }
    public BankAccount(IMarkDirty markDirty) { this.markDirty = markDirty; }

    public BankAccount(NbtCompound compound) { this(null, compound); }
    public BankAccount(IMarkDirty markDirty, NbtCompound compound) {
        this.markDirty = markDirty;
        this.coinStorage.load(compound, "CoinStorage");
        this.logger.load(compound.getCompound("AccountLogs"));
        this.ownerName = compound.getString("OwnerName");
        this.notificationLevel.load(compound, "NotificationLevel");
    }

    public void markDirty()
    {
        if(this.markDirty != null)
            this.markDirty.markDirty();
    }

    public final NbtCompound save() {
        NbtCompound compound = new NbtCompound();
        this.coinStorage.save(compound, "CoinStorage");
        compound.put("AccountLogs", this.logger.save());
        compound.putString("OwnerName", this.ownerName);
        this.notificationLevel.save(compound, "NotificationLevel");
        return compound;
    }

    public static AccountReference GenerateReference(PlayerEntity player) { return GenerateReference(player.getWorld().isClient, player.getUuid()); }
    public static AccountReference GenerateReference(boolean isClient, @Nullable UUID playerID) { return new AccountReference(isClient, playerID); }
    public static AccountReference GenerateReference(boolean isClient, @Nullable PlayerReference player) { return GenerateReference(isClient, player == null ? null : player.id); }
    public static AccountReference GenerateReference(boolean isClient, Team team) { return GenerateReference(isClient, team.getID()); }
    public static AccountReference GenerateReference(boolean isClient, long teamID) { return new AccountReference(isClient, teamID); }


    public static AccountReference LoadReference(boolean isClient, NbtCompound compound) {
        if(compound.contains("PlayerID"))
        {
            UUID id = compound.getUuid("PlayerID");
            return GenerateReference(isClient, id);
        }
        if(compound.contains("TeamID"))
        {
            long id = compound.getLong("TeamID");
            return GenerateReference(isClient, id);
        }
        return null;
    }

    public static AccountReference LoadReference(boolean isClient, PacketByteBuf buffer) {
        try {
            AccountType accountType = AccountType.fromID(buffer.readInt());
            if(accountType == AccountType.Player)
            {
                UUID id = buffer.readUuid();
                return GenerateReference(isClient, id);
            }
            if(accountType == AccountType.Team)
            {
                long id = buffer.readLong();
                return GenerateReference(isClient, id);
            }
            return null;
        } catch(Exception e) { e.printStackTrace(); return null; }
    }

    public static AccountReference LoadReference(boolean isClient, LazyPacketData data) {
        try {
            AccountType accountType = AccountType.fromID(data.getInt("BankAccountType"));
            if(accountType == AccountType.Player)
            {
                UUID id = data.getUUID("BankAccountPlayer");
                return GenerateReference(isClient, id);
            }
            if(accountType == AccountType.Team)
            {
                long id = data.getLong("BankAccountTeam");
                return GenerateReference(isClient, id);
            }
            return null;
        } catch(Exception e) { e.printStackTrace(); return null; }
    }

    public static class AccountReference {

        private final boolean isClient;
        public final AccountType accountType;
        public final UUID playerID;
        public final long teamID;

        private AccountReference(boolean isClient, UUID playerID) { this.isClient = isClient; this.accountType = AccountType.Player; this.playerID = playerID; this.teamID = -1; }

        private AccountReference(boolean isClient, long teamID) { this.isClient = isClient; this.accountType = AccountType.Team; this.teamID = teamID; this.playerID = null; }

        public NbtCompound save() {
            NbtCompound compound = new NbtCompound();
            //compound.putInt("Type", this.accountType.id);
            if(this.playerID != null)
                compound.putUuid("PlayerID", this.playerID);
            if(this.teamID >= 0)
                compound.putLong("TeamID", this.teamID);
            return compound;
        }

        public void writeToBuffer(PacketByteBuf buffer) {
            buffer.writeInt(this.accountType.id);
            if(this.playerID != null)
                buffer.writeUuid(this.playerID);
            if(this.teamID >= 0)
                buffer.writeLong(this.teamID);
        }

        public void writeToBuffer(LazyPacketData.Builder builder) {
            builder.setInt("BankAccountType", this.accountType.id);
            builder.setUUID("BankAccountPlayer", this.playerID);
            builder.setLong("BankAccountTeam", this.teamID);
        }

        public BankAccount get() {
            switch(this.accountType) {
                case Player:
                    return BankSaveData.GetBankAccount(this.isClient, this.playerID);
                case Team:
                    Team team = TeamSaveData.GetTeam(this.isClient, this.teamID);
                    if(team != null && team.hasBankAccount())
                        return team.getBankAccount();
                default:
                    return null;
            }
        }

        public boolean allowedAccess(PlayerEntity player) {
            switch(this.accountType)
            {
                case Player:
                    return player.getUuid().equals(this.playerID) || CommandLCAdmin.isAdminPlayer(player);
                case Team:
                    Team team = TeamSaveData.GetTeam(this.isClient, this.teamID);
                    if(team != null && team.hasBankAccount())
                        return team.canAccessBankAccount(player);
                default:
                    return false;
            }
        }

    }

    public interface IMarkDirty { public void markDirty(); }

    public interface IBankAccountMenu
    {
        PlayerEntity getPlayer();
        Inventory getCoinInput();
        default void onDepositOrWithdraw() {}
        boolean isClient();
        default AccountReference getBankAccountReference() {
            return this.isClient() ? ClientBankData.GetLastSelectedAccount() : BankSaveData.GetSelectedBankAccount(this.getPlayer());
        }
        default BankAccount getBankAccount() {
            AccountReference reference = this.getBankAccountReference();
            return reference == null ? null : reference.get();
        }
    }

    public interface IBankAccountAdvancedMenu extends IBankAccountMenu
    {
        void setTransferMessage(Text component);
        default void setNotificationLevel(CoinValue amount) {
            BankAccount account = this.getBankAccount();
            if(account != null)
                account.setNotificationValue(amount);
        }
    }

}