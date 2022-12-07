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

public class ChangeNameNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "changed_name");

    private PlayerReference player;
    private String oldName;
    private String newName;

    public ChangeNameNotification(PlayerReference player, String newName, String oldName) { this.player = player; this.newName = newName; this.oldName = oldName; }
    public ChangeNameNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

    @Override
    public MutableText getMessage() {
        if(oldName.isBlank())
            return Text.translatable("log.settings.changename.set", this.player.getName(true), this.newName);
        else if(newName.isBlank())
            return Text.translatable("log.settings.changename.reset", this.player.getName(true), this.oldName);
        else
            return Text.translatable("log.settings.changename", this.player.getName(true), this.oldName, this.newName);
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        compound.putString("OldName", this.oldName);
        compound.putString("NewName", this.newName);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.oldName = compound.getString("OldName");
        this.newName = compound.getString("NewName");
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof ChangeNameNotification)
        {
            ChangeNameNotification n = (ChangeNameNotification)other;
            return n.player.is(this.player) && n.newName.equals(this.newName) && n.oldName.equals(this.oldName);
        }
        return false;
    }

}