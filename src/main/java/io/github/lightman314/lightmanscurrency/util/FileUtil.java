package io.github.lightman314.lightmanscurrency.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class FileUtil {

    public static Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    public static JsonObject convertItemStack(ItemStack item) {
        JsonObject json = new JsonObject();
        json.addProperty("ID", Registries.ITEM.getId(item.getItem()).toString());
        json.addProperty("Count", item.getCount());
        if(item.hasNbt())
        {
            String tag = item.getNbt().asString();
            json.addProperty("Tag", tag);
        }
        return json;
    }

    public static ItemStack parseItemStack(JsonObject json) {
        String id = json.get("ID").getAsString();
        int count = json.get("Count").getAsInt();
        ItemStack result = new ItemStack(Registries.ITEM.get(new Identifier(id)), count);
        try {
            if(json.has("Tag"))
            {
                JsonElement tag = json.get("Tag");
                if(tag.isJsonPrimitive() && tag.getAsJsonPrimitive().isString())
                {
                    //Parse the compound tag
                    NbtCompound compound = NbtHelper.fromNbtProviderString(tag.getAsString());
                    result.setNbt(compound);
                }
                else
                {
                    NbtCompound compound = NbtHelper.fromNbtProviderString(GSON.toJson(tag));
                    result.setNbt(compound);
                }
            }
        } catch(Exception e) { LightmansCurrency.LogError("Error parsing tag data.", e); }
        return result;
    }

    public static void writeStringToFile(File file, String string) throws IOException {

        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        PrintWriter writer = new PrintWriter(bw);

        writer.print(string);

        writer.close();

    }

}