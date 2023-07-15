package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PlayerTradeLimit extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "player_trade_limit");

    private int limit = 1;
    public int getLimit() { return this.limit; }
    public void setLimit(int newLimit) { this.limit = newLimit; }

    private long timeLimit = 0;
    private boolean enforceTimeLimit() { return this.timeLimit > 0; }
    public long getTimeLimit() { return this.timeLimit; }
    public void setTimeLimit(int newValue) { this.timeLimit = newValue; }

    Map<UUID,List<Long>> memory = new HashMap<>();
    public void resetMemory() { this.memory.clear(); }

    public PlayerTradeLimit() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {

        int tradeCount = getTradeCount(event.getPlayerReference().id);
        if(tradeCount >= this.limit)
        {
            if(this.enforceTimeLimit())
                event.addDenial(Text.translatable("traderule.lightmanscurrency.tradelimit.denial.timed", tradeCount, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
            else
                event.addDenial(Text.translatable("traderule.lightmanscurrency.tradelimit.denial", tradeCount));
            event.addDenial(Text.translatable("traderule.lightmanscurrency.tradelimit.denial.limit", this.limit));
        }
        else
        {
            if(this.enforceTimeLimit())
                event.addHelpful(Text.translatable("traderule.lightmanscurrency.tradelimit.info.timed", tradeCount, this.limit, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
            else
                event.addHelpful(Text.translatable("traderule.lightmanscurrency.tradelimit.info", tradeCount, this.limit));
        }
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event) {

        this.addEvent(event.getPlayerReference().id, TimeUtil.getCurrentTime());

        this.clearExpiredData();

        event.markDirty();

    }

    private void addEvent(UUID player, Long time)
    {
        List<Long> eventTimes = new ArrayList<>();
        if(this.memory.containsKey(player))
            eventTimes = this.memory.get(player);
        eventTimes.add(time);
        this.memory.put(player, eventTimes);
    }

    private void clearExpiredData()
    {
        if(!this.enforceTimeLimit())
            return;
        List<UUID> emptyEntries = new ArrayList<>();
        this.memory.forEach((id, eventTimes) ->{
            for(int i = 0; i < eventTimes.size(); i++)
            {
                if(!TimeUtil.compareTime(this.timeLimit, eventTimes.get(i)))
                {
                    eventTimes.remove(i);
                    i--;
                }
            }
            if(eventTimes.size() <= 0)
                emptyEntries.add(id);
        });
        emptyEntries.forEach(id -> this.memory.remove(id));
    }

    private int getTradeCount(UUID playerID)
    {
        int count = 0;
        if(this.memory.containsKey(playerID))
        {
            List<Long> eventTimes = this.memory.get(playerID);
            if(!this.enforceTimeLimit())
                return eventTimes.size();
            for (Long eventTime : eventTimes) {
                if (TimeUtil.compareTime(this.timeLimit, eventTime))
                    count++;
            }
        }
        return count;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        compound.putInt("Limit", this.limit);
        NbtList memoryList = new NbtList();
        this.memory.forEach((id, eventTimes) ->{
            NbtCompound thisMemory = new NbtCompound();
            thisMemory.putUuid("ID", id);
            thisMemory.putLongArray("Times", eventTimes);
            memoryList.add(thisMemory);
        });
        compound.put("Memory", memoryList);
        compound.putLong("ForgetTime", this.timeLimit);
    }

    @Override
    public JsonObject saveToJson(JsonObject json) {
        json.addProperty("Limit", this.limit);
        if(this.enforceTimeLimit())
            json.addProperty("ForgetTime", this.timeLimit);
        return json;
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        if(compound.contains("Limit", NbtElement.INT_TYPE))
            this.limit = compound.getInt("Limit");
        if(compound.contains("Memory", NbtElement.LIST_TYPE))
        {
            this.memory.clear();
            NbtList memoryList = compound.getList("Memory", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < memoryList.size(); i++)
            {
                NbtCompound thisMemory = memoryList.getCompound(i);
                UUID id = null;
                List<Long> eventTimes = new ArrayList<>();
                if(thisMemory.contains("ID"))
                    id = thisMemory.getUuid("ID");
                if(thisMemory.contains("Times", NbtCompound.LONG_ARRAY_TYPE))
                {
                    for(long time : thisMemory.getLongArray("Times"))
                    {
                        eventTimes.add(time);
                    }
                }
                this.memory.put(id, eventTimes);
            }
        }
        if(compound.contains("ForgetTime", NbtElement.LONG_TYPE))
            this.timeLimit = compound.getLong("ForgetTime");
    }

    @Override
    public void handleUpdateMessage(NbtCompound updateInfo)
    {
        if(updateInfo.contains("Limit"))
        {
            this.limit = updateInfo.getInt("Limit");
        }
        else if(updateInfo.contains("TimeLimit"))
        {
            this.timeLimit = updateInfo.getLong("TimeLimit");
        }
        else if(updateInfo.contains("ClearMemory"))
        {
            this.resetMemory();
        }
    }

    @Override
    public NbtCompound savePersistentData() {
        NbtCompound data = new NbtCompound();
        NbtList memoryList = new NbtList();
        this.memory.forEach((id, eventTimes) ->{
            NbtCompound thisMemory = new NbtCompound();
            thisMemory.putUuid("ID", id);
            thisMemory.putLongArray("Times", eventTimes);
            memoryList.add(thisMemory);
        });
        data.put("Memory", memoryList);
        return data;
    }

    @Override
    public void loadPersistentData(NbtCompound data) {
        if(data.contains("Memory", NbtElement.LIST_TYPE))
        {
            this.memory.clear();
            NbtList memoryList = data.getList("Memory", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < memoryList.size(); i++)
            {
                NbtCompound thisMemory = memoryList.getCompound(i);
                UUID id = null;
                List<Long> eventTimes = new ArrayList<>();
                if(thisMemory.contains("ID"))
                    id = thisMemory.getUuid("ID");
                if(thisMemory.contains("Times", NbtElement.LONG_ARRAY_TYPE))
                {
                    for(long time : thisMemory.getLongArray("Times"))
                    {
                        eventTimes.add(time);
                    }
                }
                this.memory.put(id, eventTimes);
            }
        }
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if(json.has("Limit"))
            this.limit = json.get("Limit").getAsInt();
        if(json.has("ForgetTime"))
            this.timeLimit = json.get("ForgetTime").getAsLong();
    }

    @Override
    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_COUNT_PLAYER; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        private PlayerTradeLimit getRule()
        {
            if(getRuleRaw() instanceof PlayerTradeLimit)
                return (PlayerTradeLimit)getRuleRaw();
            return null;
        }

        GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            super(screen, rule);
        }

        TextFieldWidget limitInput;
        ButtonWidget buttonSetLimit;
        ButtonWidget buttonClearMemory;

        TimeInputWidget timeInput;

        @Override
        public void initTab() {

            this.limitInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, Text.empty()));
            this.limitInput.setMaxLength(3);
            this.limitInput.setText(Integer.toString(this.getRule().limit));

            this.buttonSetLimit = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, Text.translatable("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
            this.buttonClearMemory = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, Text.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));

            this.timeInput = this.addCustomRenderable(new TimeInputWidget(screen.guiLeft() + 48, screen.guiTop() + 87, 10, TimeUtil.TimeUnit.DAY, TimeUtil.TimeUnit.MINUTE, this::addCustomRenderable, this::onTimeSet));
            this.timeInput.setTime(this.getRule().timeLimit);

        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

            gui.drawText(this.screen.getFont(), Text.translatable("gui.button.lightmanscurrency.playerlimit.info", this.getRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF, false);

            Text text = this.getRule().timeLimit > 0 ? Text.translatable("gui.widget.lightmanscurrency.playerlimit.duration", new TimeUtil.TimeData(this.getRule().timeLimit).getShortString()) : Text.translatable("gui.widget.lightmanscurrency.playerlimit.noduration");
            TextRenderUtil.drawCenteredText(gui, text, this.screen.guiLeft() + this.screen.xSize / 2, this.screen.guiTop() + 75, 0xFFFFFF);

            if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.screen.getFont(), Text.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);

        }

        @Override
        public void onTabClose() {

            this.removeCustomWidget(this.limitInput);
            this.removeCustomWidget(this.buttonSetLimit);
            this.removeCustomWidget(this.buttonClearMemory);

            this.timeInput.removeChildren(this::removeCustomWidget);
            this.removeCustomWidget(this.timeInput);

        }

        @Override
        public void onScreenTick() {

            TextInputUtil.whitelistInteger(this.limitInput, 1, 100);

        }

        void PressSetLimitButton(ButtonWidget button)
        {
            int limit = MathUtil.clamp(TextInputUtil.getIntegerValue(this.limitInput), 1, 100);
            this.getRule().limit = limit;
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putInt("Limit", limit);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

        void PressClearMemoryButton(ButtonWidget button)
        {
            this.getRule().memory.clear();
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putBoolean("ClearMemory", true);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

        public void onTimeSet(TimeUtil.TimeData newTime) {
            long timeLimit = MathUtil.clamp(newTime.miliseconds, 0, Long.MAX_VALUE);
            this.getRule().timeLimit = timeLimit;
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putLong("TimeLimit", timeLimit);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

    }

}