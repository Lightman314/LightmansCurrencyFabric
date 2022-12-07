package io.github.lightman314.lightmanscurrency.common.notifications.types;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TextNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "text");

    private MutableText text = Text.literal("");
    private NotificationCategory category = NullCategory.INSTANCE;

    public TextNotification(MutableText text){ this(text, NullCategory.INSTANCE); }
    public TextNotification(MutableText text, NotificationCategory category) { this.text = text; this.category = category; }

    public TextNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.category; }

    @Override
    public MutableText getMessage() { return text; }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.putString("Text", Text.Serializer.toJson(this.text));
        compound.put("Category", this.category.save());
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        if(compound.contains("Text", NbtElement.STRING_TYPE))
            this.text = Text.Serializer.fromJson(compound.getString("Text"));
        if(compound.contains("Category", NbtElement.COMPOUND_TYPE))
            this.category = NotificationCategory.deserialize(compound.getCompound("Category"));
    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof TextNotification)
        {
            TextNotification otherText = (TextNotification)other;
            if(otherText.text.getString() == this.text.getString())
                return true;
        }
        return false;
    }

}