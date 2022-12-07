package io.github.lightman314.lightmanscurrency.common.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

public class CashRegisterBlockEntity extends BlockEntity implements ITraderSource{

    List<BlockPos> positions = new ArrayList<>();

    public CashRegisterBlockEntity(BlockPos pos, BlockState state)
    {
        super(ModBlockEntities.CASH_REGISTER, pos, state);
    }

    public void loadDataFromItems(NbtCompound itemTag)
    {
        if(itemTag == null)
            return;
        readPositions(itemTag);
    }

    public void OpenContainer(PlayerEntity player)
    {
        if(this.getTraders().size() > 0)
            player.openHandledScreen(TraderData.getTraderMenuProvider(this.pos));
        else
            player.sendMessage(Text.translatable("message.lightmanscurrency.cash_register.notlinked"));
    }

    @Override
    public boolean isSingleTrader() { return false; }

    @Override
    @NotNull
    public List<TraderData> getTraders() {
        List<TraderData> traders = new ArrayList<>();
        for (BlockPos position : this.positions) {
            BlockEntity be = this.world.getBlockEntity(position);
            if (be instanceof TraderBlockEntity<?>) {
                TraderData trader = ((TraderBlockEntity<?>) be).getTraderData();
                if (trader != null)
                    traders.add(trader);
            }
        }
        return traders;
    }

    @Override
    public void writeNbt(NbtCompound compound)
    {

        NbtList storageList = new NbtList();
        for (BlockPos pos : positions) {
            NbtCompound thisEntry = new NbtCompound();
            thisEntry.putInt("x", pos.getX());
            thisEntry.putInt("y", pos.getY());
            thisEntry.putInt("z", pos.getZ());
            storageList.add(thisEntry);
        }

        if(storageList.size() > 0)
        {
            compound.put("TraderPos", storageList);
        }

        super.writeNbt(compound);
    }

    @Override
    public void readNbt(NbtCompound compound)
    {

        readPositions(compound);

        super.readNbt(compound);

    }

    private void readPositions(NbtCompound compound)
    {
        if(compound.contains("TraderPos"))
        {
            this.positions = new ArrayList<>();
            NbtList storageList = compound.getList("TraderPos", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < storageList.size(); i++)
            {
                NbtCompound thisEntry = storageList.getCompound(i);
                if(thisEntry.contains("x") && thisEntry.contains("y") && thisEntry.contains("z"))
                {
                    BlockPos thisPos = new BlockPos(thisEntry.getInt("x"), thisEntry.getInt("y"), thisEntry.getInt("z"));
                    this.positions.add(thisPos);
                }
            }
        }
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() { return this.createNbt(); }

}