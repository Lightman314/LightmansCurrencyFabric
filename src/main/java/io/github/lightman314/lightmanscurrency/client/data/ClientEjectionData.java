package io.github.lightman314.lightmanscurrency.client.data;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;

import java.util.ArrayList;
import java.util.List;


@Environment(EnvType.CLIENT)
public class ClientEjectionData {

    private static final List<EjectionData> emergencyEjectionData = new ArrayList<>();

    public static List<EjectionData> GetEjectionData() { return new ArrayList<>(emergencyEjectionData); }

    public static void UpdateEjectionData(NbtCompound compound) {
        emergencyEjectionData.clear();
        NbtList ejectionList = compound.getList("EmergencyEjectionData", NbtElement.COMPOUND_TYPE);
        for(int i = 0; i < ejectionList.size(); ++i)
        {
            try {
                EjectionData e = EjectionData.loadData(ejectionList.getCompound(i));
                if(e != null)
                {
                    emergencyEjectionData.add(e);
                    e.flagAsClient();
                }
                else
                    throw new RuntimeException("EmergencyEjectionData entry " + i + " loaded as null.");
            } catch(Throwable t) { t.printStackTrace(); }
        }
        LightmansCurrency.LogDebug("Client loaded " + emergencyEjectionData.size() + " ejection data entries from the server.");
    }

    public static void onClientLogout(ClientPlayNetworkHandler handler, MinecraftClient client) {
        emergencyEjectionData.clear();
    }

}