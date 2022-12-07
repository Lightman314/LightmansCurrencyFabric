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

public class ChangeAllyPermissionNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID,"change_ally_permissions");

    PlayerReference player;
    String permission;
    int newValue;
    int oldValue;

    public ChangeAllyPermissionNotification(PlayerReference player, String permission, int newValue, int oldValue) {
        this.player = player;
        this.permission = permission;
        this.newValue = newValue;
        this.oldValue = oldValue;
    }

    public ChangeAllyPermissionNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

    @Override
    public MutableText getMessage() {
        if(this.oldValue == 0)
            return Text.translatable("log.settings.permission.ally.simple", this.player.getName(true), this.permission, this.newValue);
        else
            return Text.translatable("log.settings.permission.ally", this.player.getName(true), this.permission, this.oldValue, this.newValue);
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        compound.putString("Permission", this.permission);
        compound.putInt("NewValue", this.newValue);
        compound.putInt("OldValue", this.oldValue);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.permission = compound.getString("Permission");
        this.newValue = compound.getInt("NewValue");
        this.oldValue = compound.getInt("OldValue");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof ChangeAllyPermissionNotification)
        {
            ChangeAllyPermissionNotification n = (ChangeAllyPermissionNotification)other;
            return n.player.is(this.player) && n.permission.equals(this.permission) && n.newValue == this.newValue && n.oldValue == this.oldValue;
        }
        return false;
    }

}