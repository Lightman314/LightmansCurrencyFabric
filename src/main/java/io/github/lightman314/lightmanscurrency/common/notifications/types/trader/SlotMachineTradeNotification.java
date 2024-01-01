package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;


import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.item.ItemTradeNotification.ItemData;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineTradeNotification extends Notification {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "slot_machine_trade");

    TraderCategory traderData;

    List<ItemData> items;
    CoinValue cost = new CoinValue();
    CoinValue money = new CoinValue();

    String customer;

    public SlotMachineTradeNotification(NbtCompound tag) { this.load(tag); }

    protected SlotMachineTradeNotification(SlotMachineEntry entry, CoinValue cost, PlayerReference customer, TraderCategory traderData)
    {
        this.traderData = traderData;
        this.cost = cost.copy();
        this.items = new ArrayList<>();
        if(entry.isMoney())
            this.money = entry.getMoneyValue();
        else
        {
            for(ItemStack item : InventoryUtil.combineQueryItems(entry.items))
                this.items.add(new ItemData(item));
        }

        this.customer = customer.getName(false);
    }

    public static Supplier<Notification> create(SlotMachineEntry entry, CoinValue cost, PlayerReference customer, TraderCategory traderData) { return () -> new SlotMachineTradeNotification(entry, cost, customer, traderData); }

    @Override
    protected Identifier getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return this.traderData; }

    @Override
    public MutableText getMessage() {
        Text rewardText;
        if(this.money.getRawValue() > 0)
            rewardText = this.money.getComponent("0");
        else
            rewardText = getItemNames(this.items);

        return EasyText.translatable("notifications.message.slot_machine_trade", this.customer, this.cost.getString("0"), rewardText);
    }

    public static Text getItemNames(List<ItemData> items) {
        Text result = null;
        for (ItemData item : items) {
            if (result != null)
                result = item.formatWith(result);
            else
                result = item.format();
        }
        return result == null ? EasyText.literal("ERROR") : result;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        compound.put("TraderInfo", this.traderData.save());
        NbtList itemList = new NbtList();
        for(ItemData item : this.items)
            itemList.add(item.save());
        compound.put("Items", itemList);
        this.money.save(compound, "Money");
        this.cost.save(compound,"Price");
        compound.putString("Customer", this.customer);

    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
        NbtList itemList = compound.getList("Items", NbtElement.COMPOUND_TYPE);
        this.items = new ArrayList<>();
        for(int i = 0; i < itemList.size(); ++i)
            this.items.add(new ItemData(itemList.getCompound(i)));
        this.money = new CoinValue();
        this.money.load(compound, "Money");
        this.cost = new CoinValue();
        this.cost.load(compound,"Price");
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) {
        if(other instanceof SlotMachineTradeNotification smtn)
        {
            if(!smtn.traderData.matches(this.traderData))
                return false;
            if(smtn.items.size() != this.items.size())
                return false;
            for(int i = 0; i < this.items.size(); ++i)
            {
                ItemData i1 = this.items.get(i);
                ItemData i2 = smtn.items.get(i);
                if(!i1.matches(i2))
                    return false;
            }
            if(!smtn.money.equals(this.money))
                return false;
            if(!smtn.cost.equals(this.cost))
                return false;
            if(!smtn.customer.equals(this.customer))
                return false;
            //Passed all checks. Allow merging.
            return true;
        }
        return false;
    }
}