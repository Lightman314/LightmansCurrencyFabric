package io.github.lightman314.lightmanscurrency.network;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class LazyPacketData {

    //Normal Types
    public static final byte TYPE_NULL = 0;
    public static final byte TYPE_BOOLEAN = 1;
    public static final byte TYPE_INT = 2;
    public static final byte TYPE_LONG = 3;
    public static final byte TYPE_FLOAT = 4;
    public static final byte TYPE_DOUBLE = 5;
    public static final byte TYPE_STRING = 6;
    public static final byte TYPE_UUID = 10;

    //Minecraft Types
    public static final byte TYPE_TEXT = 64;
    public static final byte TYPE_NBT = 65;
    public static final byte TYPE_BLOCKPOS = 66;

    private final ImmutableMap<String,Data> dataMap;
    public Set<String> getAllKeys() { return this.dataMap.keySet(); }

    private LazyPacketData(Map<String,Data> data) { this.dataMap = ImmutableMap.copyOf(data); }

    private Data getData(String key) { return this.dataMap.getOrDefault(key, Data.NULL); }

    public boolean contains(String key) { return this.dataMap.containsKey(key); }
    public boolean contains(String key, byte type)
    {
        return this.contains(key) && this.getData(key).type == type;
    }

    public boolean getBoolean(String key) { return this.getBoolean(key, false); }
    public boolean getBoolean(String key, boolean defaultValue) {
        Data d = this.getData(key);
        if(d.type == TYPE_BOOLEAN)
            return (boolean)d.value;
        return defaultValue;
    }

    public int getInt(String key) { return this.getInt(key, 0); }
    public int getInt(String key, int defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_INT)
            return (int)d.value;
        return defaultValue;
    }

    public long getLong(String key) { return this.getLong(key, 0L); }
    public long getLong(String key, long defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_LONG)
            return (long)d.value;
        return defaultValue;
    }

    public float getFloat(String key) { return this.getFloat(key, 0f); }
    public float getFloat(String key, float defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_FLOAT)
            return (float)d.value;
        return defaultValue;
    }

    public double getDouble(String key) { return this.getDouble(key, 0d); }
    public double getDouble(String key, double defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_DOUBLE)
            return (double)d.value;
        return defaultValue;
    }

    public String getString(String key) { return this.getString(key, null); }
    public String getString(String key, String defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_STRING)
            return (String)d.value;
        return defaultValue;
    }

    public Identifier getResource(String key) { return this.getResource(key, null); }
    public Identifier getResource(String key, Identifier defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_STRING)
            return new Identifier((String)d.value);
        return defaultValue;
    }

    public UUID getUUID(String key) { return this.getUUID(key, null); }
    public UUID getUUID(String key, UUID defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_UUID)
            return (UUID)d.value;
        return defaultValue;
    }

    public Text getText(String key) { return this.getText(key, EasyText.empty()); }
    public Text getText(String key, Text defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_TEXT)
            return (Text)d.value;
        return defaultValue;
    }

    public NbtCompound getCompound(String key) { return this.getCompound(key, new NbtCompound()); }
    public NbtCompound getCompound(String key, NbtCompound defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
            return (NbtCompound)d.value;
        return defaultValue;
    }

    public BlockPos getBlockPos(String key) { return this.getBlockPos(key, new BlockPos(0,0,0)); }
    public BlockPos getBlockPos(String key, BlockPos defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_BLOCKPOS)
            return (BlockPos)d.value;
        return defaultValue;
    }

    public CoinValue getCoinValue(String key) { return this.getCoinValue(key, new CoinValue()); }
    public CoinValue getCoinValue(String key, CoinValue defaultValue)
    {
        Data d = this.getData(key);
        if(d.type == TYPE_NBT)
        {
            CoinValue result = new CoinValue();
            result.load((NbtCompound)d.value, CoinValue.DEFAULT_KEY);
            return result;
        }
        return defaultValue;
    }

    public void encode(PacketByteBuf buffer)
    {
        //Write the entry count
        buffer.writeInt(this.dataMap.entrySet().size());
        //Write each entry
        this.dataMap.forEach((key,data) -> {
            buffer.writeString(key);
            buffer.writeByte(data.type);
            data.encode(buffer);
        });
    }

    public static LazyPacketData decode(PacketByteBuf buffer) {
        int count = buffer.readInt();
        HashMap<String,Data> dataMap = new HashMap<>();
        for(int i = 0; i < count; ++i)
        {
            String key = buffer.readString();
            Data data = Data.decode(buffer);
            dataMap.put(key, data);
        }
        return new LazyPacketData(dataMap);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder
    {
        private Builder() {}
        Map<String,Data> data = new HashMap<>();

        public Builder setBoolean(String key, boolean value) { this.data.put(key, Data.ofBoolean(value)); return this; }
        public Builder setInt(String key, int value) { this.data.put(key, Data.ofInt(value)); return this; }
        public Builder setLong(String key, long value) { this.data.put(key, Data.ofLong(value)); return this; }
        public Builder setFloat(String key, float value) { this.data.put(key, Data.ofFloat(value)); return this; }
        public Builder setDouble(String key, double value) { this.data.put(key, Data.ofDouble(value)); return this; }
        public Builder setString(String key, String value) { this.data.put(key, Data.ofString(value)); return this; }
        public Builder setResource(String key, Identifier value) { this.data.put(key, Data.ofResource(value)); return this; }
        public Builder setUUID(String key, UUID value) { this.data.put(key, Data.ofUUID(value)); return this; }

        public Builder setText(String key, Text value) { this.data.put(key, Data.ofText(value)); return this; }
        public Builder setCompound(String key, NbtCompound value) { this.data.put(key, Data.ofNBT(value)); return this; }
        public Builder setBlockPos(String key, BlockPos value) { this.data.put(key, Data.ofBlockPos(value)); return this; }
        public Builder setCoinValue(String key, CoinValue value) { this.data.put(key, Data.ofCoinValue(value)); return this; }

        public Builder clone(LazyPacketData data) {
            this.data.putAll(data.dataMap);
            return this;
        }

        public LazyPacketData build() { return new LazyPacketData(this.data); }

    }

    private record Data(byte type, Object value) {

        static final Data NULL = new Data(TYPE_NULL, null);

        static Data ofNull() { return NULL; }
        static Data ofBoolean(boolean value) { return new Data(TYPE_BOOLEAN, value); }
        static Data ofInt(int value) { return new Data(TYPE_INT, value); }
        static Data ofLong(long value) { return new Data(TYPE_LONG, value); }
        static Data ofFloat(float value) { return new Data(TYPE_FLOAT, value); }
        static Data ofDouble(double value) { return new Data(TYPE_DOUBLE, value); }
        static Data ofString(String value) { return value == null ? NULL : new Data(TYPE_STRING, value); }
        static Data ofResource(Identifier value) { return value == null ? NULL : new Data(TYPE_STRING, value.toString()); }
        static Data ofUUID(UUID value) { return value == null ? NULL : new Data(TYPE_UUID, value); }
        static Data ofText(Text value) { return value == null ? NULL : new Data(TYPE_TEXT, value); }
        static Data ofNBT(NbtCompound value) { return value == null ? NULL : new Data(TYPE_NBT, value); }
        static Data ofBlockPos(BlockPos value) { return value == null ? NULL : new Data(TYPE_BLOCKPOS, value); }
        static Data ofCoinValue(CoinValue value) { return value == null ? NULL : ofNBT(value.save(new NbtCompound(), CoinValue.DEFAULT_KEY)); }


        void encode(PacketByteBuf buffer)
        {
            //Normal Values
            if(this.type == TYPE_BOOLEAN)
                buffer.writeBoolean((boolean) this.value);
            if(this.type == TYPE_INT)
                buffer.writeInt((int)this.value);
            if(this.type == TYPE_LONG)
                buffer.writeLong((long)this.value);
            if(this.type == TYPE_FLOAT)
                buffer.writeFloat((float)this.value);
            if(this.type == TYPE_DOUBLE)
                buffer.writeDouble((double)this.value);
            if(this.type == TYPE_STRING)
            {
                String s = (String)this.value;
                buffer.writeInt(s.length());
                buffer.writeString(s);
            }
            if(this.type == TYPE_UUID)
                buffer.writeUuid((UUID)this.value);
            //MC values
            if(this.type == TYPE_TEXT)
                buffer.writeString(Text.Serializer.toJson((Text)this.value));
            if(this.type == TYPE_NBT)
                buffer.writeNbt((NbtCompound) this.value);
            if(this.type == TYPE_BLOCKPOS)
            {
                BlockPos val = (BlockPos)this.value;
                buffer.writeInt(val.getX());
                buffer.writeInt(val.getY());
                buffer.writeInt(val.getZ());
            }
        }

        static Data decode(PacketByteBuf buffer)
        {
            byte type = buffer.readByte();
            //Normal Values
            if(type == TYPE_NULL)
                return ofNull();
            if(type == TYPE_BOOLEAN)
                return ofBoolean(buffer.readBoolean());
            if(type == TYPE_INT)
                return ofInt(buffer.readInt());
            if(type == TYPE_LONG)
                return ofLong(buffer.readLong());
            if(type == TYPE_FLOAT)
                return ofFloat(buffer.readFloat());
            if(type == TYPE_DOUBLE)
                return ofDouble(buffer.readDouble());
            if(type == TYPE_STRING)
            {
                int length = buffer.readInt();
                return ofString(buffer.readString(length));
            }
            if(type == TYPE_UUID)
                return ofUUID(buffer.readUuid());
            //Minecraft Values
            if(type == TYPE_TEXT)
                return ofText(Text.Serializer.fromJson(buffer.readString()));
            if(type == TYPE_NBT)
                return ofNBT(buffer.readUnlimitedNbt());
            if(type == TYPE_BLOCKPOS)
            {
                int x = buffer.readInt();
                int y = buffer.readInt();
                int z = buffer.readInt();
                return ofBlockPos(new BlockPos(x,y,z));
            }
            throw new RuntimeException("Could not decode entry of type " + type + "as it is not a valid data entry type!");
        }

    }

}