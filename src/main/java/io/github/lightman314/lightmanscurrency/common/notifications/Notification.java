package io.github.lightman314.lightmanscurrency.common.notifications;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Notification {

    private static final Map<String,Function<NbtCompound,Notification>> DESERIALIZERS = new HashMap<>();

    public static void register(Identifier type, Supplier<Notification> deserializer) {
        register(type, c -> {
            Notification n = deserializer.get();
            n.load(c);
            return n;
        });
    }

    public static void register(Identifier type, Function<NbtCompound,Notification> deserializer) {
        String t = type.toString();
        if(DESERIALIZERS.containsKey(t))
        {
            LightmansCurrency.LogError("Notification of type " + t + " is already registered.");
            return;
        }
        if(deserializer == null)
        {
            LightmansCurrency.LogError("Deserializer of notification type " + t + " is null. Unable to register.");
            return;
        }
        DESERIALIZERS.put(t, deserializer);
    }

    public static Notification deserialize(NbtCompound compound) {
        if(compound.contains("Type") || compound.contains("type"))
        {
            String type = compound.contains("Type") ? compound.getString("Type") : compound.getString("type");
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

    private boolean seen = false;
    public boolean wasSeen() { return this.seen; }
    public void setSeen() { this.seen = true; }

    private int count = 1;
    public int getCount() { return this.count; }

    protected abstract Identifier getType();

    public abstract NotificationCategory getCategory();

    public abstract MutableText getMessage();

    public MutableText getGeneralMessage() {
        return Text.translatable("notifications.source.general.format", this.getCategory().getName(), this.getMessage());
    }

    public MutableText getChatMessage() {
        return Text.translatable("notifications.chat.format",
                Text.translatable("notifications.chat.format.title", this.getCategory().getName()).formatted(Formatting.GOLD),
                this.getMessage());
    }

    public final NbtCompound save() {
        NbtCompound compound = new NbtCompound();
        if(this.seen)
            compound.putBoolean("Seen", true);
        compound.putInt("Count", this.count);
        compound.putString("Type", this.getType().toString());
        this.saveAdditional(compound);
        return compound;
    }

    protected abstract void saveAdditional(NbtCompound compound);

    public final void load(NbtCompound compound) {
        if(compound.contains("Seen"))
            this.seen = true;
        if(compound.contains("Count", NbtElement.INT_TYPE))
            this.count = compound.getInt("Count");
        this.loadAdditional(compound);
    }

    protected abstract void loadAdditional(NbtCompound compound);

    /**
     * Determines whether the new notification should stack or not.
     * @param other The other notification. Use this to determine if the other notification is a duplicate or not.
     * @return True if the notification was stacked.
     */
    public boolean onNewNotification(Notification other) {
        if(this.canMerge(other))
        {
            this.count++;
            this.seen = false;
            return true;
        }
        return false;
    }

    /**
     * Whether the other notification should be merged with this one.
     */
    protected abstract boolean canMerge(Notification other);

}