package io.github.lightman314.lightmanscurrency.common.emergency_ejection;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import io.github.lightman314.lightmanscurrency.client.data.ClientEjectionData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateClientTrader;
import io.github.lightman314.lightmanscurrency.network.client.messages.data.SMessageUpdateEjectionData;
import io.github.lightman314.lightmanscurrency.server.ServerHook;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

public class EjectionSaveData extends PersistentState {

    private List<EjectionData> emergencyEjectionData = new ArrayList<>();

    private EjectionSaveData() {}
    private EjectionSaveData(NbtCompound compound) {

        NbtList ejectionData = compound.getList("EmergencyEjectionData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < ejectionData.size(); ++i)
        {
            try {
                EjectionData e = EjectionData.loadData(ejectionData.getCompound(i));
                if(e != null && !e.isEmpty())
                    this.emergencyEjectionData.add(e);
            } catch(Throwable t) { LightmansCurrency.LogError("Error loading ejection data entry " + i, t); }
        }
        LightmansCurrency.LogDebug("Server loaded " + this.emergencyEjectionData.size() + " ejection data entries from file.");

    }

    public NbtCompound writeNbt(NbtCompound compound) {

        NbtList ejectionData = new NbtList();
        this.emergencyEjectionData.forEach(data -> ejectionData.add(data.save()));
        compound.put("EmergencyEjectionData", ejectionData);

        return compound;
    }

    private static EjectionSaveData get() {
        MinecraftServer server = ServerHook.getServer();
        if(server != null)
        {
            ServerWorld level = server.getOverworld();
            if(level != null)
                return level.getPersistentStateManager().getOrCreate(EjectionSaveData::new, EjectionSaveData::new, "lightmanscurrency_ejection_data");
        }
        return null;
    }

    public static List<EjectionData> GetEjectionData(boolean isClient) {
        if(isClient)
        {
            return ClientEjectionData.GetEjectionData();
        }
        else
        {
            EjectionSaveData esd = get();
            if(esd != null)
                return new ArrayList<>(esd.emergencyEjectionData);
        }
        return new ArrayList<>();
    }

    public static List<EjectionData> GetValidEjectionData(boolean isClient, PlayerEntity player)
    {
        List<EjectionData> ejectionData = GetEjectionData(isClient);
        if(ejectionData != null)
            return ejectionData.stream().filter(e -> e.canAccess(player)).collect(Collectors.toList());
        return new ArrayList<>();
    }

    @Deprecated /** @deprecated Use only to transfer ejection data from the old Trading Office. */
    public static void GiveOldEjectionData(EjectionData data) {
        EjectionSaveData esd = get();
        if(esd != null && data != null && !data.isEmpty())
        {
            esd.emergencyEjectionData.add(data);
            MarkEjectionDataDirty();
        }
    }

    public static void HandleEjectionData(World level, BlockPos pos, EjectionData data) {
        if(level.isClient)
            return;
        Objects.requireNonNull(data);

        //if(Config.SERVER.safelyEjectIllegalBreaks.get())
        {
            EjectionSaveData esd = get();
            if(esd != null)
            {
                esd.emergencyEjectionData.add(data);
                MarkEjectionDataDirty();
            }
        }
        //else
        //    InventoryUtil.dumpContents(level, pos, data);
    }

    public static void RemoveEjectionData(EjectionData data) {
        EjectionSaveData esd = get();
        if(esd != null)
        {
            Objects.requireNonNull(data);
            if(esd.emergencyEjectionData.contains(data))
            {
                esd.emergencyEjectionData.remove(data);
                MarkEjectionDataDirty();
            }
        }
    }

    public static void MarkEjectionDataDirty() {
        EjectionSaveData esd = get();
        if(esd != null)
        {
            esd.markDirty();
            //Send update packet to all connected clients
            NbtCompound compound = new NbtCompound();
            NbtList ejectionList = new NbtList();
            esd.emergencyEjectionData.forEach(data -> {
                ejectionList.add(data.save());
            });
            compound.put("EmergencyEjectionData", ejectionList);
            new SMessageUpdateEjectionData(compound).sendToAll();
        }
    }

    public static void OnPlayerLogin(ServerPlayerEntity player, PacketSender sender)
    {
        EjectionSaveData esd = get();
        if(esd != null)
        {
            //Send ejection data
            NbtCompound compound = new NbtCompound();
            NbtList ejectionList = new NbtList();
            esd.emergencyEjectionData.forEach(data -> {
                ejectionList.add(data.save());
            });
            compound.put("EmergencyEjectionData", ejectionList);
            new SMessageUpdateClientTrader(compound).sendTo(sender);
        }

    }

}