package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import java.util.List;
import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderDataArmor;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.EquipmentRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ArmorDisplayTraderBlockEntity extends ItemTraderBlockEntity{

    public static final int TRADE_COUNT = 4;
    private static final int TICK_DELAY = 20;

    UUID armorStandID = null;

    int updateTimer = 0;

    private boolean loaded = false;
    public void flagAsLoaded() { this.loaded = true; }

    public ArmorDisplayTraderBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.ARMOR_TRADER, pos, state, TRADE_COUNT);
    }

    @Override
    public ItemTraderData buildNewTrader() { return new ItemTraderDataArmor(this.world, this.pos); }

    @Override
    public void serverTick()
    {
        super.serverTick();

        if(!this.loaded)
            return;

        if(this.updateTimer <= 0)
        {
            this.updateTimer = TICK_DELAY;
            this.validateArmorStand();
            this.updateArmorStandArmor();
        }
        else
            this.updateTimer--;
    }

    /**
     * Validates the armor stands existence, the local ArmorStandID, and gets the local reference to the armor stand.
     * Logical Server only.
     */
    public void validateArmorStand() {
        if(this.isClient())
            return;
        ArmorStandEntity armorStand = this.getArmorStand();
        if(armorStand == null || armorStand.isRemoved())
        {
            //Spawn a new armor stand
            this.spawnArmorStand();
        }
        else
            this.validateArmorStandValues();
    }

    private void spawnArmorStand()
    {
        if(this.world == null || this.isClient())
            return;

        ArmorStandEntity armorStand = new ArmorStandEntity(this.world, this.pos.getX() + 0.5d, this.pos.getY(), this.pos.getZ() + 0.5d);
        armorStand.refreshPositionAndAngles(this.pos.getX() + 0.5d, this.pos.getY(), this.pos.getZ() + 0.5d, this.getStandRotation(), 0.0F);

        armorStand.setInvulnerable(true);
        armorStand.setNoGravity(true);
        armorStand.setSilent(true);
        NbtCompound compound = armorStand.writeNbt(new NbtCompound());
        compound.putBoolean("Marker", true);
        compound.putBoolean("NoBasePlate", true);
        armorStand.readNbt(compound);

        this.world.spawnEntity(armorStand);

        this.armorStandID = armorStand.getUuid();
        this.markDirty();

    }

    protected void updateArmorStandArmor() {
        ArmorStandEntity armorStand = this.getArmorStand();
        if(armorStand != null)
        {
            ItemTraderData trader = this.getTraderData();
            if(trader != null)
            {
                List<ItemTradeData> trades = trader.getTradeData();
                for(int i = 0; i < 4 && i < trades.size(); i++)
                {
                    ItemTradeData thisTrade = trades.get(i);
                    //Trade restrictions shall determine the slot type
                    ItemTradeRestriction r = thisTrade.getRestriction();
                    EquipmentSlot slot = null;
                    if(r instanceof EquipmentRestriction er)
                        slot = er.getEquipmentSlot();
                    if(slot != null)
                    {
                        if(thisTrade.hasStock(trader) || trader.isCreative())
                            armorStand.equipStack(slot, thisTrade.getSellItem(0));
                        else
                            armorStand.equipStack(slot, ItemStack.EMPTY);
                    }
                }
            }

        }
    }

    protected void validateArmorStandValues()
    {
        ArmorStandEntity armorStand = this.getArmorStand();
        if(armorStand == null)
            return;
        armorStand.refreshPositionAndAngles(this.pos.getX() + 0.5d, this.pos.getY(), this.pos.getZ() + 0.5f, this.getStandRotation(), 0f);
        if(!armorStand.isInvulnerable())
            armorStand.setInvulnerable(true);
        if(armorStand.isInvisible())
            armorStand.setInvisible(false);
        if(!armorStand.hasNoGravity())
            armorStand.setNoGravity(true);
        if(!armorStand.isSilent())
            armorStand.setSilent(true);
        if(!armorStand.isMarker() || !armorStand.shouldHideBasePlate())
        {
            NbtCompound compound = armorStand.writeNbt(new NbtCompound());
            if(!armorStand.isMarker())
                compound.putBoolean("Marker", true);
            if(!armorStand.shouldHideBasePlate())
                compound.putBoolean("NoBasePlate", true);
            armorStand.readNbt(compound);
        }
    }

    @Override
    public void writeNbt(NbtCompound compound)
    {
        this.writeArmorStandData(compound);

        super.writeNbt(compound);
    }

    protected void writeArmorStandData(NbtCompound compound)
    {
        if(this.armorStandID != null)
            compound.putUuid("ArmorStand", this.armorStandID);
    }

    @Override
    public void readNbt(NbtCompound compound)
    {
        this.loaded = true;
        if(compound.contains("ArmorStand"))
            this.armorStandID = compound.getUuid("ArmorStand");
        super.readNbt(compound);
    }

    protected ArmorStandEntity getArmorStand()
    {

        if(this.world instanceof ServerWorld level)
        {
            Entity entity = level.getEntity(this.armorStandID);
            if(entity instanceof ArmorStandEntity as)
                return as;
        }

        return null;
    }

    public void destroyArmorStand()
    {
        ArmorStandEntity armorStand = this.getArmorStand();
        if(armorStand != null)
            armorStand.kill();
    }

    protected float getStandRotation()
    {
        Direction facing = Direction.NORTH;
        if(this.getCachedState().getBlock() instanceof IRotatableBlock)
            facing = ((IRotatableBlock)this.getCachedState().getBlock()).getFacing(this.getCachedState());
        if(facing == Direction.SOUTH)
            return 180f;
        else if(facing == Direction.NORTH)
            return 0f;
        else if(facing == Direction.WEST)
            return -90f;
        else if(facing == Direction.EAST)
            return 90f;
        return 0f;
    }

}