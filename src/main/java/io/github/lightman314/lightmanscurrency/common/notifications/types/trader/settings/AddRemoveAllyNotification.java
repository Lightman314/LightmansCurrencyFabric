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

public class AddRemoveAllyNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "add_remove_ally");

    PlayerReference player;
    boolean isAdd;
    PlayerReference ally;

    public AddRemoveAllyNotification(PlayerReference player, boolean isAdd, PlayerReference ally) {
        this.player = player;
        this.isAdd = isAdd;
        this.ally = ally;
    }
    public AddRemoveAllyNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

    @Override
    public MutableText getMessage() {
        return Text.translatable("log.settings.addremoveally", this.player.getName(true), Text.translatable(this.isAdd ? "log.settings.add" : "log.settings.remove"), this.ally.getName(true), Text.translatable(this.isAdd ? "log.settings.to" : "log.settings.from"));
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        compound.putBoolean("Add", this.isAdd);
        compound.put("Ally", this.ally.save());
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.isAdd = compound.getBoolean("Add");
        this.ally = PlayerReference.load(compound.getCompound("Ally"));
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof AddRemoveAllyNotification)
        {
            AddRemoveAllyNotification n = (AddRemoveAllyNotification)other;
            return n.player.is(this.player) && n.isAdd == this.isAdd && n.ally.is(this.ally);
        }
        return false;
    }

}