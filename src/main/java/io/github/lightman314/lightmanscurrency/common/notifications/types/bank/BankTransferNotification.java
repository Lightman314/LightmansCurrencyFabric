package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BankTransferNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "bank_transfer");

    PlayerReference player;
    CoinValue amount = new CoinValue();
    MutableText accountName;
    MutableText otherAccount;
    boolean wasReceived;

    public BankTransferNotification(NbtCompound compound) { this.load(compound); }
    public BankTransferNotification(PlayerReference player, CoinValue amount, MutableText accountName, MutableText otherAccount, boolean wasReceived) {
        this.player = player;
        this.amount = amount;
        this.accountName = accountName;
        this.otherAccount = otherAccount;
        this.wasReceived = wasReceived;
    }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Override
    public MutableText getMessage() {
        return Text.translatable("log.bank.transfer", this.player.getName(true), this.amount.getComponent(), Text.translatable(this.wasReceived ? "log.bank.transfer.from" : "log.bank.transfer.to"), this.otherAccount);
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        this.amount.save(compound, "Amount");
        compound.putString("Account", Text.Serializer.toJson(this.accountName));
        compound.putString("Other", Text.Serializer.toJson(this.otherAccount));
        compound.putBoolean("Received", this.wasReceived);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.amount.load(compound, "Amount");
        this.accountName = Text.Serializer.fromJson(compound.getString("Account"));
        this.otherAccount = Text.Serializer.fromJson(compound.getString("Other"));
        this.wasReceived = compound.getBoolean("Received");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof BankTransferNotification)
        {
            BankTransferNotification n = (BankTransferNotification)other;
            return n.player.is(this.player) && n.amount.equals(this.amount) && n.accountName.equals(this.accountName) && n.otherAccount.equals(this.otherAccount) && n.wasReceived == this.wasReceived;
        }
        return false;
    }

}