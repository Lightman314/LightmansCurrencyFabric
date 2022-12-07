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

public abstract class ChangeSettingNotification extends Notification {

    public static final Identifier ADVANCED_TYPE = new Identifier(LightmansCurrency.MODID, "change_settings_advanced");
    public static final Identifier SIMPLE_TYPE = new Identifier(LightmansCurrency.MODID, "change_settings_simple");

    protected PlayerReference player;
    protected String setting;

    protected ChangeSettingNotification(PlayerReference player, String setting) { this.player = player; this.setting = setting; }
    protected ChangeSettingNotification() {}

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("Player", this.player.save());
        compound.putString("Setting", this.setting);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.player = PlayerReference.load(compound.getCompound("Player"));
        this.setting = compound.getString("Setting");
    }

    public static class Advanced extends ChangeSettingNotification
    {

        String newValue;
        String oldValue;

        public Advanced(PlayerReference player, String setting, String newValue, String oldValue) { super(player, setting); this.newValue = newValue; this.oldValue = oldValue; }
        public Advanced(NbtCompound compound) { this.load(compound); }

        @Override
        protected Identifier getType() { return ADVANCED_TYPE; }

        @Override
        public MutableText getMessage() { return Text.translatable("log.settings.change", this.player.getName(true), this.setting, this.oldValue, this.newValue); }

        @Override
        protected void saveAdditional(NbtCompound compound) {
            super.saveAdditional(compound);
            compound.putString("NewValue", this.newValue);
            compound.putString("OldValue", this.oldValue);
        }

        @Override
        protected void loadAdditional(NbtCompound compound) {
            super.loadAdditional(compound);
            this.newValue = compound.getString("NewValue");
            this.oldValue = compound.getString("OldValue");
        }

        @Override
        protected boolean canMerge(Notification other) {
            if(other instanceof Advanced)
            {
                Advanced n = (Advanced)other;
                return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue) && n.oldValue.equals(this.oldValue);
            }
            return false;
        }

    }

    public static class Simple extends ChangeSettingNotification
    {

        String newValue;

        public Simple(PlayerReference player, String setting, String newValue) { super(player, setting); this.newValue = newValue; }
        public Simple(NbtCompound compound) { this.load(compound); }

        @Override
        protected Identifier getType() { return SIMPLE_TYPE; }

        @Override
        public MutableText getMessage() {
            return Text.translatable("log.settings.change.simple", this.player.getName(true), this.setting, this.newValue);
        }

        @Override
        protected void saveAdditional(NbtCompound compound) {
            super.saveAdditional(compound);
            compound.putString("NewValue", this.newValue);
        }

        @Override
        protected void loadAdditional(NbtCompound compound) {
            super.loadAdditional(compound);
            this.newValue = compound.getString("NewValue");
        }

        @Override
        protected boolean canMerge(Notification other) {
            if(other instanceof Simple)
            {
                Simple n = (Simple)other;
                return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue);
            }
            return false;
        }

    }

}