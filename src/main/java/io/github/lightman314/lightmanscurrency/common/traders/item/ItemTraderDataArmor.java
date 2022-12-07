package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemTraderDataArmor extends ItemTraderData {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "item_trader_armor");

    public ItemTraderDataArmor() { super(TYPE); }
    public ItemTraderDataArmor(World level, BlockPos pos) { super(TYPE, 4, level, pos); }

    @Override
    protected ItemTradeRestriction getTradeRestriction(int tradeIndex)
    {
        switch(tradeIndex % 4)
        {
            case 0:
                return new EquipmentRestriction(EquipmentSlot.HEAD);
            case 1:
                return new EquipmentRestriction(EquipmentSlot.CHEST);
            case 2:
                return new EquipmentRestriction(EquipmentSlot.LEGS);
            default:
                return new EquipmentRestriction(EquipmentSlot.FEET);
        }
    }

}