package io.github.lightman314.lightmanscurrency.common.notifications.types.trader.settings;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class AddRemoveTradeNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "add_remove_trade");

    PlayerReference player;
    boolean isAdd;
    int newCount;

    public AddRemoveTradeNotification(PlayerReference player, boolean isAdd, int newCount) { this.player = player; this.isAdd = isAdd; this.newCount = newCount; }
    public AddRemoveTradeNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

    @Override
    public MutableText getMessage() {
        return Text.translatable("log.settings.addremovetrade", this.player.getName(true), Text.translatable(this.isAdd ? "log.settings.add" : "log.settings.remove"), this.newCount);
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        compound.putBoolean("Add", this.isAdd);
        compound.putInt("NewCount", this.newCount);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.isAdd = compound.getBoolean("Add");
        this.newCount = compound.getInt("NewCount");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof AddRemoveTradeNotification)
        {
            AddRemoveTradeNotification n = (AddRemoveTradeNotification)other;
            return n.player.is(this.player) && this.isAdd == n.isAdd && this.newCount == n.newCount;
        }
        return false;
    }

}