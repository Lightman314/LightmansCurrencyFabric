package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class NullCategory extends NotificationCategory {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "null");

    public static final NullCategory INSTANCE = new NullCategory();

    private NullCategory() {}

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.BARRIER); }

    @Override
    public MutableText getName() { return Text.literal("NULL"); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) { return other instanceof NullCategory; }

    @Override
    protected void saveAdditional(NbtCompound compound) { }

}