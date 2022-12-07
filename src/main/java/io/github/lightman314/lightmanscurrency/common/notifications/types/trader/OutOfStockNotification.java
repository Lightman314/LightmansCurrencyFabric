package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OutOfStockNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "out_of_stock");

    TraderCategory traderData;

    int tradeSlot;

    public OutOfStockNotification(TraderCategory traderData, int tradeIndex) {
        this.traderData = traderData;
        this.tradeSlot = tradeIndex + 1;
    }

    public OutOfStockNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    public MutableText getMessage() { return Text.translatable("notifications.message.out_of_stock", this.traderData.getTooltip(), this.tradeSlot); }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        compound.put("TraderInfo", this.traderData.save());
        compound.putInt("TradeSlot", this.tradeSlot);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
        this.tradeSlot = compound.getInt("TradeSlot");
    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

}