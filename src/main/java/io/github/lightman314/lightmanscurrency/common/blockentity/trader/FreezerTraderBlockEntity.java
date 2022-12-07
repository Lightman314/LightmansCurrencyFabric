package io.github.lightman314.lightmanscurrency.common.blockentity.trader;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.block.BlockState;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class FreezerTraderBlockEntity extends ItemTraderBlockEntity {

    /** The current angle of the door (between 0 and 1) */
    private float doorAngle;
    /** The angle of the door last tick */
    private float prevDoorAngle;

    public FreezerTraderBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.FREEZER_TRADER, pos, state);
    }

    public FreezerTraderBlockEntity(BlockPos pos, BlockState state, int tradeCount)
    {
        super(ModBlockEntities.FREEZER_TRADER, pos, state, tradeCount);
    }

    public float getDoorAngle(float partialTicks) {
        return MathHelper.lerp(partialTicks, this.prevDoorAngle, this.doorAngle);
    }

    private final float distancePerTick = 0.1f;

    @Override
    public void clientTick()
    {

        super.clientTick();

        TraderData trader = this.getTraderData();
        if(trader != null)
        {
            int userCount = trader.getUserCount();

            this.prevDoorAngle = this.doorAngle;
            //Play the opening sound
            if (userCount > 0 && this.doorAngle == 0.0F) {
                this.world.playSound(this.pos.getX() + 0.5d, this.pos.getY() + 0.5d, this.pos.getZ() + 0.5d, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
                //this.level.playSound(null, this.worldPosition, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
            }
            if(userCount > 0 && this.doorAngle < 1f)
            {
                this.doorAngle += distancePerTick;
            }
            else if(userCount <= 0 && doorAngle > 0f)
            {
                this.doorAngle -= distancePerTick;
                if (this.doorAngle < 0.5F && this.prevDoorAngle >= 0.5F) {
                    this.world.playSound(this.pos.getX() + 0.5d, this.pos.getY() + 0.5d, this.pos.getZ() + 0.5d, SoundEvents.BLOCK_CHEST_CLOSE, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F, false);
                }
            }
            if(this.doorAngle > 1f)
                this.doorAngle = 1f;
            else if(this.doorAngle < 0f)
                this.doorAngle = 0f;

        }

    }

}