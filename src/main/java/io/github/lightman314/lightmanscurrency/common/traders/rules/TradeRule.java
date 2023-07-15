package io.github.lightman314.lightmanscurrency.common.traders.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.google.common.base.Supplier;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public abstract class TradeRule {

    public final Identifier type;
    public final MutableText getName() { return Text.translatable("traderule." + type.getNamespace() + "." + type.getPath()); }

    private boolean isActive = false;
    public boolean isActive() { return this.isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    public void beforeTrade(TradeEvent.PreTradeEvent event) {}
    public void tradeCost(TradeEvent.TradeCostEvent event) {}
    public void afterTrade(TradeEvent.PostTradeEvent event) {}

    protected TradeRule(Identifier type) { this.type = type; }

    public NbtCompound save()
    {
        NbtCompound compound = new NbtCompound();
        compound.putString("Type", this.type.toString());
        compound.putBoolean("Active", this.isActive);
        this.saveAdditional(compound);
        return compound;
    }

    protected abstract void saveAdditional(NbtCompound compound);

    public final void load(NbtCompound compound)
    {
        this.isActive = compound.getBoolean("Active");
        this.loadAdditional(compound);
    }
    protected abstract void loadAdditional(NbtCompound compound);

    public abstract JsonObject saveToJson(JsonObject json);
    public abstract void loadFromJson(JsonObject json);

    public abstract NbtCompound savePersistentData();
    public abstract void loadPersistentData(NbtCompound data);

    public abstract IconData getButtonIcon();

    public final void receiveUpdateMessage(NbtCompound updateInfo)
    {
        if(updateInfo.contains("SetActive"))
            this.isActive = updateInfo.getBoolean("SetActive");
        this.handleUpdateMessage(updateInfo);
    }

    protected abstract void handleUpdateMessage(NbtCompound updateInfo);

    public static void saveRules(NbtCompound compound, List<TradeRule> rules, String tag)
    {
        NbtList ruleData = new NbtList();
        for (TradeRule rule : rules) ruleData.add(rule.save());
        compound.put(tag, ruleData);
    }

    public static boolean savePersistentData(NbtCompound compound, List<TradeRule> rules, String tag) {
        NbtList ruleData = new NbtList();
        for (TradeRule rule : rules) {
            NbtCompound thisRuleData = rule.savePersistentData();
            if (thisRuleData != null) {
                thisRuleData.putString("Type", rule.type.toString());
                ruleData.add(thisRuleData);
            }
        }
        if(ruleData.size() == 0)
            return false;
        compound.put(tag, ruleData);
        return true;
    }

    public static JsonArray saveRulesToJson(List<TradeRule> rules) {
        JsonArray ruleData = new JsonArray();
        for (TradeRule rule : rules) {
            if (rule.isActive) {
                JsonObject thisRuleData = rule.saveToJson(new JsonObject());
                if (thisRuleData != null) {
                    thisRuleData.addProperty("Type", rule.type.toString());
                    ruleData.add(thisRuleData);
                }
            }
        }
        return ruleData;
    }

    public static List<TradeRule> loadRules(NbtCompound compound, String tag)
    {
        List<TradeRule> rules = new ArrayList<>();
        if(compound.contains(tag, NbtElement.LIST_TYPE))
        {
            NbtList ruleData = compound.getList(tag, NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < ruleData.size(); i++)
            {
                NbtCompound thisRuleData = ruleData.getCompound(i);
                TradeRule thisRule = Deserialize(thisRuleData);
                if(thisRule != null)
                    rules.add(thisRule);
            }
        }
        return rules;
    }

    public static void loadPersistentData(NbtCompound compound, List<TradeRule> tradeRules, String tag)
    {
        if(compound.contains(tag, NbtElement.LIST_TYPE))
        {
            NbtList ruleData = compound.getList(tag, NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < ruleData.size(); ++i)
            {
                NbtCompound thisRuleData = ruleData.getCompound(i);
                boolean query = true;
                for(int r = 0; query && r < tradeRules.size(); ++r)
                {
                    if(tradeRules.get(r).type.toString().contentEquals(thisRuleData.getString("Type")))
                    {
                        tradeRules.get(r).loadPersistentData(thisRuleData);
                        query = false;
                    }
                }
            }
        }
    }

    public static List<TradeRule> Parse(JsonArray tradeRuleData)
    {
        List<TradeRule> rules = new ArrayList<>();
        for(int i = 0; i < tradeRuleData.size(); ++i)
        {
            try {
                JsonObject thisRuleData = tradeRuleData.get(i).getAsJsonObject();
                TradeRule thisRule = Deserialize(thisRuleData);
                rules.add(thisRule);
            }
            catch(Throwable t) { LightmansCurrency.LogError("Error loading Trade Rule at index " + i + ".", t); }
        }
        return rules;
    }

    public static boolean ValidateTradeRuleList(List<TradeRule> rules, Function<TradeRule,Boolean> allowed)
    {
        boolean changed = false;
        for(Supplier<TradeRule> ruleSource : registeredDeserializers.values())
        {
            TradeRule rule = ruleSource.get();
            if(rule != null && allowed.apply(rule) && !HasTradeRule(rules,rule.type))
            {
                rules.add(rule);
                changed = true;
            }
        }
        return changed;
    }

    public static boolean HasTradeRule(List<TradeRule> rules, Identifier type) { return GetTradeRule(rules, type) != null; }

    public static TradeRule GetTradeRule(List<TradeRule> rules, Identifier type)
    {
        for(TradeRule rule : rules)
        {
            if(rule.type.equals(type))
                return rule;
        }
        return null;
    }

    @Environment(EnvType.CLIENT)
    public abstract GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule);

    @Environment(EnvType.CLIENT)
    public static abstract class GUIHandler
    {

        protected final TradeRuleScreen screen;
        private final Supplier<TradeRule> rule;
        protected final TradeRule getRuleRaw() { return rule.get(); }

        protected GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            this.screen = screen;
            this.rule = rule;
        }

        public abstract void initTab();

        public abstract void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks);

        public abstract void onTabClose();

        public void onScreenTick() { }

        public <T extends Element & Drawable & Selectable> T addCustomRenderable(T widget) { return screen.addCustomRenderable(widget); }

        public <T extends Element & Selectable> T addCustomWidget(T widget)
        {
            return screen.addCustomWidget(widget);
        }

        public <T extends Element> void removeCustomWidget(T widget)
        {
            screen.removeCustomWidget(widget);
        }

    }

    /**
     * Trade Rule Deserialization
     */
    static final Map<String,Supplier<TradeRule>> registeredDeserializers = new HashMap<>();

    public static void RegisterDeserializer(Identifier type, Supplier<TradeRule> deserializer) { RegisterDeserializer(type, deserializer, false); }

    public static void RegisterDeserializer(Identifier type, Supplier<TradeRule> deserializer, boolean suppressDebugMessage) { RegisterDeserializer(type.toString(), deserializer, suppressDebugMessage); }

    private static void RegisterDeserializer(String type, Supplier<TradeRule> deserializer, boolean suppressDebugMessage)
    {
        if(registeredDeserializers.containsKey(type))
        {
            LightmansCurrency.LogWarning("A trade rule deserializer of type '" + type + "' has already been registered.");
            return;
        }
        registeredDeserializers.put(type, deserializer);
        if(!suppressDebugMessage)
            LightmansCurrency.LogInfo("Registered trade rule deserializer of type " + type);
    }

    public static TradeRule CreateRule(Identifier ruleType)
    {
        String thisType = ruleType.toString();
        AtomicReference<TradeRule> data = new AtomicReference<TradeRule>();
        registeredDeserializers.forEach((type,deserializer) -> {
            if(thisType.equals(type))
            {
                TradeRule rule = deserializer.get();
                data.set(rule);
            }
        });
        if(data.get() != null)
            return data.get();
        LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Trade Rule.");
        return null;
    }

    public static TradeRule Deserialize(NbtCompound compound)
    {
        String thisType = compound.contains("Type") ? compound.getString("Type") : compound.getString("type");
        if(registeredDeserializers.containsKey(thisType))
        {
            try {
                TradeRule rule = registeredDeserializers.get(thisType).get();
                rule.load(compound);
                return rule;
            } catch(Throwable t) { LightmansCurrency.LogError("Error deserializing trade rule:", t); }
        }
        LightmansCurrency.LogError("Could not find a deserializer of type '" + thisType + "'. Unable to load the Trade Rule.");
        return null;
    }

    public static TradeRule Deserialize(JsonObject json) throws Exception{
        String thisType = json.get("Type").getAsString();
        if(registeredDeserializers.containsKey(thisType))
        {
            TradeRule rule = registeredDeserializers.get(thisType).get();
            rule.loadFromJson(json);
            rule.setActive(true);
            return rule;
        }
        throw new Exception("Could not find a deserializer of type '" + thisType + "'.");
    }

    public static TradeRule getRule(Identifier type, List<TradeRule> rules) {
        for(TradeRule rule : rules)
        {
            if(rule.type.equals(type))
                return rule;
        }
        return null;
    }

    public static NbtCompound CreateRuleMessage() { NbtCompound tag = new NbtCompound(); tag.putBoolean("Create", true); return tag; }
    public static NbtCompound RemoveRuleMessage() { NbtCompound tag = new NbtCompound(); tag.putBoolean("Remove", true); return tag; }

    public static boolean isCreateMessage(NbtCompound tag) { return tag.contains("Create") && tag.getBoolean("Create"); }
    public static boolean isRemoveMessage(NbtCompound tag) { return tag.contains("Remove") && tag.getBoolean("Remove"); }

}