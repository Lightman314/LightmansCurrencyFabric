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

public class ChangeCreativeNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "change_creative");

    PlayerReference player;
    boolean creative;

    public ChangeCreativeNotification(PlayerReference player, boolean creative) { this.player = player; this.creative = creative; }
    public ChangeCreativeNotification(NbtCompound compound) { this.load(compound); }
    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

    @Override
    public MutableText getMessage() {
        return Text.translatable("log.settings.creativemode", this.player.getName(true), Text.translatable(this.creative ? "log.settings.enabled" : "log.settings.disabled"));
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        compound.putBoolean("Creative", this.creative);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.creative = compound.getBoolean("Creative");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof ChangeCreativeNotification)
        {
            ChangeCreativeNotification n = (ChangeCreativeNotification)other;
            return n.player.is(this.player) && n.creative == this.creative;
        }
        return false;
    }

}