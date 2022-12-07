package io.github.lightman314.lightmanscurrency.common.notifications.types.trader.paygate;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PaygateNotification extends Notification{

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "paygate_trade");

    TraderCategory traderData;

    UUID ticketID = null;
    CoinValue cost = new CoinValue();

    int duration = 0;

    String customer;

    public PaygateNotification(PaygateTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {

        this.traderData = traderData;
        this.ticketID = trade.getTicketID();

        if(trade.isTicketTrade())
            this.ticketID = trade.getTicketID();
        else
            this.cost = cost;

        this.duration = trade.getDuration();

        this.customer = customer.getName(false);

    }

    public PaygateNotification(NbtCompound compound) { this.load(compound); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    public MutableText getMessage() {

        if(this.ticketID != null)
            return Text.translatable("notifications.message.paygate_trade.ticket", this.customer, this.ticketID.toString(), PaygateTradeData.formatDurationShort(this.duration));
        else
            return Text.translatable("notifications.message.paygate_trade.coin", this.customer, this.cost.getString(), PaygateTradeData.formatDurationShort(this.duration));

    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        compound.put("TraderInfo", this.traderData.save());
        compound.putInt("Duration", this.duration);
        if(this.ticketID != null)
            compound.putUuid("Ticket", this.ticketID);
        else
            this.cost.save(compound, "Price");
        compound.putString("Customer", this.customer);

    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
        this.duration = compound.getInt("Duration");
        if(compound.contains("Ticket"))
            this.ticketID = compound.getUuid("Ticket");
        else if(compound.contains("Price"))
            this.cost.load(compound, "Price");
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof PaygateNotification)
        {
            PaygateNotification pn = (PaygateNotification)other;
            if(!pn.traderData.matches(this.traderData))
                return false;
            if((pn.ticketID == null) != (this.ticketID == null))
                return false;
            if(pn.ticketID != null && !pn.ticketID.equals(this.ticketID))
                return false;
            if(pn.duration != this.duration)
                return false;
            if(pn.cost.getRawValue() != this.cost.getRawValue())
                return false;
            if(!pn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return true;
        }
        return false;
    }

}