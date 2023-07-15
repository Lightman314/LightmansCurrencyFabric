package io.github.lightman314.lightmanscurrency.common.notifications;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.ITab;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class NotificationCategory implements ITab
{

    public static final Identifier GENERAL_TYPE = new Identifier(LightmansCurrency.MODID, "general");

    private static final Map<String,Function<NbtCompound,NotificationCategory>> DESERIALIZERS = new HashMap<>();

    public static final void register(Identifier type, Function<NbtCompound,NotificationCategory> deserializer) {
        String t = type.toString();
        if(DESERIALIZERS.containsKey(t))
        {
            LightmansCurrency.LogError("Category of type " + t + " is already registered.");
            return;
        }
        if(deserializer == null)
        {
            LightmansCurrency.LogError("Deserializer of category type " + t + " is null. Unable to register.");
            return;
        }
        DESERIALIZERS.put(t, deserializer);
    }

    public static final NotificationCategory deserialize(NbtCompound compound) {
        if(compound.contains("type"))
        {
            String type = compound.getString("type");
            if(DESERIALIZERS.containsKey(type))
            {
                return DESERIALIZERS.get(type).apply(compound);
            }
            else
            {
                LightmansCurrency.LogError("Cannot deserialize notification type " + type + " as no deserializer has been registered.");
                return null;
            }
        }
        else
        {
            LightmansCurrency.LogError("Cannot deserialize notification as tag is missing the 'type' tag.");
            return null;
        }
    }

    /* Obsolete as this is covered by ITab
    public abstract IconData getIcon();
    */
    public final MutableText getTooltip() { return this.getName(); }
    public abstract MutableText getName();
    public final int getColor() { return 0xFFFFFF; }
    protected abstract Identifier getType();

    public abstract boolean matches(NotificationCategory other);

    public static final NotificationCategory GENERAL = new NotificationCategory() {
        @Override
        public @NotNull IconData getIcon() { return IconData.of(Items.CHEST); }
        @Override
        public MutableText getName() { return Text.translatable("notifications.source.general"); }
        @Override
        public boolean matches(NotificationCategory other) { return other == GENERAL; }
        @Override
        protected Identifier getType() { return GENERAL_TYPE; }
        @Override
        protected void saveAdditional(NbtCompound compound) {}
    };

    public final NbtCompound save() {
        NbtCompound compound = new NbtCompound();
        compound.putString("type", this.getType().toString());
        this.saveAdditional(compound);
        return compound;
    }

    protected abstract void saveAdditional(NbtCompound compound);

}