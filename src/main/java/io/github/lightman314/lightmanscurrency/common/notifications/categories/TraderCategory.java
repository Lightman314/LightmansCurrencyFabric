package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TraderCategory extends NotificationCategory {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID,"trader");

    private final Item trader;
    private final long traderID;
    private final MutableText traderName;
    public MutableText getTraderName() { return this.traderName; }

    public TraderCategory(ItemConvertible trader, MutableText traderName, long traderID) {
        this.trader = trader.asItem();
        this.traderName = traderName;
        this.traderID = traderID;
    }

    public TraderCategory(NbtCompound compound) {

        if(compound.contains("Icon"))
            this.trader = Registries.ITEM.get(new Identifier(compound.getString("Icon")));
        else
            this.trader = ModItems.TRADING_CORE;

        if(compound.contains("TraderName"))
            this.traderName = Text.Serializer.fromJson(compound.getString("TraderName"));
        else
            this.traderName = Text.translatable("gui.lightmanscurrency.universaltrader.default");

        if(compound.contains("TraderID"))
            this.traderID = compound.getLong("TraderID");
        else
            this.traderID = -1;

    }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(this.trader); }

    @Override
    public MutableText getName() { return this.traderName; }

    @Override
    public Identifier getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) {
        if(other instanceof TraderCategory otherTrader)
        {
            if(this.traderID == otherTrader.traderID && this.traderID != -1)
                return true;
            //Confirm the trader name matches.
            return this.traderName.getString().contentEquals(otherTrader.traderName.getString()) && this.trader.equals(otherTrader.trader);
        }
        return false;
    }

    public void saveAdditional(NbtCompound compound) {
        compound.putString("Icon", Registries.ITEM.getId(this.trader).toString());
        compound.putString("TraderName", Text.Serializer.toJson(this.traderName));
        compound.putLong("TraderID", this.traderID);
    }

}