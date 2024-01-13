package io.github.lightman314.lightmanscurrency.common.atm;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.client.ServerToClientPacket;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public class ATMData extends ServerToClientPacket {

    public static final Identifier PACKET_ID = new Identifier(LightmansCurrency.MODID, "atm_data_packet");

    public static final String ATM_FILE_LOCATION = "config/lightmanscurrency/ATMData.json";

    private final List<ATMConversionButtonData> conversionButtons;
    public final List<ATMConversionButtonData> getConversionButtons() { return ImmutableList.copyOf(this.conversionButtons); }

    private ATMData(JsonObject jsonData) throws Exception {

        super(PACKET_ID);

        //LightmansCurrency.LogInfo("Loading ATM Data from json:\n" + FileUtil.GSON.toJson(jsonData));

        this.conversionButtons = new ArrayList<>();
        if(jsonData.has("ConversionButtons"))
        {
            JsonArray conversionButtonDataList = jsonData.getAsJsonArray("ConversionButtons");
            for(int i = 0; i < conversionButtonDataList.size(); ++i)
            {
                try {
                    this.conversionButtons.add(ATMConversionButtonData.parse(conversionButtonDataList.get(i).getAsJsonObject()));
                } catch(Exception e) { LightmansCurrency.LogError("Error parsing Conversion Button #" + String.valueOf(i + 1) + ".", e); }
            }
        }
        else
        {
            LightmansCurrency.LogWarning("ATM Data has no 'ConversionButtons' list entry. Conversion tab will have no buttons.");
        }

    }

    private ATMData(List<ATMConversionButtonData> conversionButtons) {
        super(PACKET_ID);
        this.conversionButtons = Lists.newArrayList(conversionButtons);
    }

    public JsonObject save() {
        JsonObject data = new JsonObject();

        JsonArray conversionButtonDataList = new JsonArray();
        for(int i = 0; i < this.conversionButtons.size(); ++i)
            conversionButtonDataList.add(this.conversionButtons.get(i).save());
        data.add("ConversionButtons", conversionButtonDataList);

        return data;
    }

    private static ATMData loadedData = null;
    public static ATMData get() {
        if(loadedData == null)
            reloadATMData();
        return loadedData;
    }

    @Override
    protected void encode(LazyPacketData.Builder dataBuilder) {
        JsonObject json = this.save();
        String jsonString = FileUtil.GSON.toJson(json);
        dataBuilder.setString("json", jsonString);
    }

    public static ATMData decode(LazyPacketData data) {
        try {
            LightmansCurrency.LogDebug("Decoding atm data packet:");
            String jsonString = data.getString("json");
            JsonObject json = JsonParser.parseString(jsonString).getAsJsonObject();
            return new ATMData(json);
        } catch(Throwable t) { LightmansCurrency.LogError("Error decoding ATMData.", t); return generateDefault(); }
    }

    @Environment(EnvType.CLIENT)
    public static void handle(MinecraftClient client, ClientPlayNetworkHandler handler, LazyPacketData data, PacketSender responseSender) {
        loadedData = decode(data);
    }

    private static ATMData generateDefault() {
        return new ATMData(ATMConversionButtonData.generateDefault());
    }

    public static void reloadATMData() {
        LightmansCurrency.LogInfo("Reloading ATM Data");
        File file = new File(ATM_FILE_LOCATION);
        if(!file.exists())
        {
            createATMDataFile(file);
        }
        try {
            JsonObject fileData = JsonHelper.deserialize(Files.readString(file.toPath()));
            loadedData = new ATMData(fileData);
        } catch(Throwable e) {
            LightmansCurrency.LogError("Error loading ATM Data. Using default values for now.", e);
            loadedData = generateDefault();
        }
        loadedData.sendToAll();
    }

    private static void createATMDataFile(File file) {
        File dir = new File(file.getParent());
        if(!dir.exists())
            dir.mkdirs();
        if(dir.exists())
        {
            try {

                ATMData defaultData = generateDefault();

                file.createNewFile();

                FileUtil.writeStringToFile(file, new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create().toJson(defaultData.save()));

                LightmansCurrency.LogInfo("ATMData.json does not exist. Creating a fresh copy.");

            } catch(Throwable e) { LightmansCurrency.LogError("Error attempting to create 'ATMData.json' file.", e); }
        }
    }

}