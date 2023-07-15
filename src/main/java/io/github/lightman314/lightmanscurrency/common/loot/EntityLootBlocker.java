package io.github.lightman314.lightmanscurrency.common.loot;

import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityLootBlocker extends PersistentState {

    private final List<UUID> flaggedEntities = new ArrayList<>();

    private EntityLootBlocker() {}
    private EntityLootBlocker(NbtCompound compound)
    {
        if(compound.contains("FlaggedEntities"))
        {
            NbtList list = compound.getList("FlaggedEntities", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < list.size(); ++i)
            {
                NbtCompound data = list.getCompound(i);
                UUID id = data.getUuid("ID");
                if(id != null)
                    this.flaggedEntities.add(id);
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound compound)
    {
        NbtList list = new NbtList();
        for(UUID id : this.flaggedEntities)
        {
            NbtCompound data = new NbtCompound();
            data.putUuid("ID", id);
            list.add(data);
        }
        if(list.size() > 0)
            compound.put("FlaggedEntities", list);
        return compound;
    }

    private static EntityLootBlocker get()
    {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
            return server.getOverworld().getPersistentStateManager().getOrCreate(EntityLootBlocker::new, EntityLootBlocker::new, "lightmanscurrency_entity_tracker");
        return null;
    }

    public static boolean BlockEntityDrops(LivingEntity entity)
    {
        if(entity.getWorld().isClient)
            return true;
        EntityLootBlocker elb = get();
        return elb != null && elb.flaggedEntities.contains(entity.getUuid());
    }

    public static void StopTrackingEntity(Entity entity) {
        if(entity.getWorld().isClient)
            return;
        EntityLootBlocker elb = get();
        if(elb != null && elb.flaggedEntities.contains(entity.getUuid()))
        {
            elb.flaggedEntities.remove(entity.getUuid());
            elb.setDirty(true);
        }
    }

    public static void FlagEntity(LivingEntity entity)
    {
        if(entity.getWorld().isClient)
            return;

        EntityLootBlocker elb = get();
        if(elb != null && !elb.flaggedEntities.contains(entity.getUuid()))
        {
            elb.flaggedEntities.add(entity.getUuid());
            elb.setDirty(true);
        }
    }

}
