package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public abstract class DepositWithdrawNotification extends Notification {

    public static final Identifier PLAYER_TYPE = new Identifier(LightmansCurrency.MODID, "bank_deposit_player");
    public static final Identifier TRADER_TYPE = new Identifier(LightmansCurrency.MODID, "bank_deposit_trader");
    public static final Identifier SERVER_TYPE = new Identifier(LightmansCurrency.MODID, "bank_deposit_server");

    protected MutableText accountName;
    protected boolean isDeposit;
    protected CoinValue amount = new CoinValue();

    protected DepositWithdrawNotification(MutableText accountName, boolean isDeposit, CoinValue amount) { this.accountName = accountName; this.isDeposit = isDeposit; this.amount = amount; }
    protected DepositWithdrawNotification() {}

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.putString("Name", Text.Serializer.toJson(this.accountName));
        compound.putBoolean("Deposit", this.isDeposit);
        this.amount.save(compound, "Amount");
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.accountName = Text.Serializer.fromJson(compound.getString("Name"));
        this.isDeposit = compound.getBoolean("Deposit");
        this.amount.load(compound, "Amount");

    }

    protected abstract MutableText getName();

    @Override
    public MutableText getMessage() {
        return Text.translatable("log.bank", this.getName(), Text.translatable(this.isDeposit ? "log.bank.deposit" : "log.bank.withdraw"), this.amount.getComponent());
    }

    public static class Player extends DepositWithdrawNotification {

        PlayerReference player;

        public Player(PlayerReference player, MutableText accountName, boolean isDeposit, CoinValue amount) { super(accountName, isDeposit, amount); this.player = player; }
        public Player(NbtCompound compound) { this.load(compound); }

        @Override
        protected MutableText getName() { return this.player.getNameComponent(true); }

        @Override
        protected Identifier getType() { return PLAYER_TYPE; }

        @Override
        protected void saveAdditional(NbtCompound compound) {
            super.saveAdditional(compound);
            compound.put("Player", this.player.save());
        }

        @Override
        protected void loadAdditional(NbtCompound compound) {
            super.loadAdditional(compound);
            this.player = PlayerReference.load(compound.getCompound("Player"));
        }

        @Override
        protected boolean canMerge(Notification other) {
            if(other instanceof Player n)
            {
                return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.player.is(this.player);
            }
            return false;
        }

    }

    public static class Trader extends DepositWithdrawNotification {
        MutableText traderName;

        public Trader(MutableText traderName, MutableText accountName, boolean isDeposit, CoinValue amount) { super(accountName, isDeposit, amount); this.traderName = traderName; }
        public Trader(NbtCompound compound) { this.load(compound); }

        @Override
        protected MutableText getName() { return this.traderName; }

        @Override
        protected Identifier getType() { return TRADER_TYPE; }

        @Override
        protected void saveAdditional(NbtCompound compound) {
            super.saveAdditional(compound);
            compound.putString("Trader", Text.Serializer.toJson(this.traderName));
        }

        @Override
        protected void loadAdditional(NbtCompound compound) {
            super.loadAdditional(compound);
            this.traderName = Text.Serializer.fromJson(compound.getString("Trader"));
        }

        @Override
        protected boolean canMerge(Notification other) {
            if(other instanceof Trader n)
            {
                return n.accountName.equals(this.accountName) && n.isDeposit == this.isDeposit && n.amount.equals(this.amount) && n.traderName.equals(this.traderName);
            }
            return false;
        }

    }

    public static class Server extends DepositWithdrawNotification {

        public Server(MutableText accountName, boolean isDeposit, CoinValue amount) { super(accountName, isDeposit, amount); }
        public Server(NbtCompound tag) { this.load(tag); }

        @Override
        protected MutableText getName() { return EasyText.translatable("notifications.bank.server"); }

        @Override
        protected Identifier getType() { return SERVER_TYPE; }

        @Override
        protected boolean canMerge(@NotNull Notification other) { return false; }

    }

}