package io.github.lightman314.lightmanscurrency.common.notifications.types.bank;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.BankCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class LowBalanceNotification extends Notification{

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "bank_low_balance");

    private MutableText accountName;
    private CoinValue value = new CoinValue();

    public LowBalanceNotification(MutableText accountName, CoinValue value) {
        this.accountName = accountName;
        this.value = value;
    }

    public LowBalanceNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return new BankCategory(this.accountName); }

    @Override
    public MutableText getMessage() {
        return Text.translatable("notifications.message.bank_low_balance", this.value.getString());
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.putString("Name", Text.Serializer.toJson(this.accountName));
        this.value.save(compound, "Amount");
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.accountName = Text.Serializer.fromJson(compound.getString("Name"));
        this.value.load(compound, "Amount");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof LowBalanceNotification)
        {
            LowBalanceNotification lbn = (LowBalanceNotification)other;
            if(!lbn.accountName.getString().equals(this.accountName.getString()))
                return false;
            if(lbn.value.getRawValue() != this.value.getRawValue())
                return false;
            //Passed all of the checks.
            return true;
        }
        return false;
    }

}