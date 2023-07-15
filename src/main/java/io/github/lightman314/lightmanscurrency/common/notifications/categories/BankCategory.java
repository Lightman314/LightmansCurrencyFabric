package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class BankCategory extends NotificationCategory {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "bank");

    private final MutableText name;

    public BankCategory(MutableText name) { this.name = name; }

    public BankCategory(NbtCompound compound) {
        this.name = Text.Serializer.fromJson(compound.getString("Name"));
    }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM); }

    @Override
    public MutableText getName() { return this.name; }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) {
        if(other instanceof BankCategory)
        {
            BankCategory bc = (BankCategory)other;
            return bc.name.getString().equals(this.name.getString());
        }
        return false;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.putString("Name", Text.Serializer.toJson(this.name));
    }

}