package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class FreeSample extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "free_sample");

    List<UUID> memory = new ArrayList<>();
    public void resetMemory() { this.memory.clear(); }

    public FreeSample() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event)
    {
        if(this.giveDiscount(event))
            event.addHelpful(Text.translatable("traderule.lightmanscurrency.free_sample.alert"));
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event) {
        if(this.giveDiscount(event))
            event.applyCostMultiplier(0d);
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event) {
        if(this.giveDiscount(event))
        {
            this.addToMemory(event.getPlayerReference().id);
            event.markDirty();
        }
    }

    private boolean giveDiscount(TradeEvent event) {
        return this.giveDiscount(event.getPlayerReference().id) && event.getTrade().getTradeDirection() != TradeData.TradeDirection.PURCHASE;
    }

    private void addToMemory(UUID playerID) {
        if(!this.memory.contains(playerID))
            this.memory.add(playerID);
    }

    public boolean giveDiscount(UUID playerID) {
        return !this.givenFreeSample(playerID);
    }

    private boolean givenFreeSample(UUID playerID) {
        return this.memory.contains(playerID);
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        NbtList memoryList = new NbtList();
        for(UUID entry : this.memory)
        {
            NbtCompound tag = new NbtCompound();
            tag.putUuid("ID", entry);
            memoryList.add(tag);
        }
        compound.put("Memory", memoryList);
    }

    @Override
    public JsonObject saveToJson(JsonObject json) { return json; }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        if(compound.contains("Memory", NbtElement.LIST_TYPE))
        {
            this.memory.clear();
            NbtList memoryList = compound.getList("Memory", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < memoryList.size(); i++)
            {
                NbtCompound tag = memoryList.getCompound(i);
                if(tag.contains("ID"))
                    this.memory.add(tag.getUuid("ID"));
                else if(tag.contains("id"))
                    this.memory.add(tag.getUuid("id"));
            }
        }
    }

    @Override
    public NbtCompound savePersistentData() { NbtCompound data = new NbtCompound(); this.saveAdditional(data); return data; }

    @Override
    public void loadPersistentData(NbtCompound data) { this.loadAdditional(data); }

    @Override
    public void loadFromJson(JsonObject json) { }

    @Override
    protected void handleUpdateMessage(NbtCompound updateInfo) {
        if(updateInfo.contains("ClearData"))
            this.resetMemory();
    }

    @Override
    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_FREE_SAMPLE; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        private FreeSample getRule()
        {
            if(getRuleRaw() instanceof FreeSample)
                return (FreeSample)getRuleRaw();
            return null;
        }

        GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            super(screen, rule);
        }

        ButtonWidget buttonClearMemory;

        @Override
        public void initTab() {

            this.buttonClearMemory = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, Text.translatable("gui.button.lightmanscurrency.free_sample.reset"), this::PressClearMemoryButton));

        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

            if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.screen.getFont(), Text.translatable("gui.button.lightmanscurrency.free_sample.reset.tooltip"), mouseX, mouseY);

        }

        @Override
        public void onTabClose() {
            this.removeCustomWidget(this.buttonClearMemory);
        }

        @Override
        public void onScreenTick() { }

        void PressClearMemoryButton(ButtonWidget button)
        {
            this.getRule().memory.clear();
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putBoolean("ClearData", true);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

    }

}