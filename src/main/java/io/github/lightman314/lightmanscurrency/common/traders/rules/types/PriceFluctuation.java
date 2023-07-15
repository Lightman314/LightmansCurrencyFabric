package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PriceFluctuation extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "price_fluctuation");

    long duration = TimeUtil.DURATION_DAY;
    int fluctuation = 10;
    public int getFluctuation() { return this.fluctuation; }
    public void setFluctuation(int percent) { this.fluctuation = MathUtil.clamp(percent, 0, 100); }

    public PriceFluctuation() { super(TYPE); }

    private static final List<Long> debuggedSeeds = new ArrayList<>();
    private static final List<Long> debuggedTraderFactors = new ArrayList<>();

    private static void debugTraderFactor(long factor, long traderID, int tradeIndex)
    {
        if(debuggedTraderFactors.contains(factor))
            return;
        LightmansCurrency.LogDebug("Trader Seed Factor for trader with id '" + traderID + "' and trade index '" + tradeIndex + "' is " + factor);
        debuggedTraderFactors.add(factor);
    }

    private static void debugFlux(long seed, int maxFlux, int flux)
    {
        if(debuggedSeeds.contains(seed))
            return;
        LightmansCurrency.LogDebug("Price Fluctuation for trade with seed '" + (seed) + "' and max fluctuation of " + maxFlux + "% is " + flux + "%");
        debuggedSeeds.add(seed);
    }

    private long getTraderSeedFactor(TradeEvent.TradeCostEvent event) {
        long traderID = event.getTrader().getID();
        int tradeIndex = event.getTradeIndex();
        long factor = ((traderID + 1) << 32) + tradeIndex;
        debugTraderFactor(factor, traderID, tradeIndex);
        return factor;
    }

    private double randomizePriceMultiplier(long traderSeedFactor)
    {
        //Have the seed be constant during the given duration
        long seed = TimeUtil.getCurrentTime() / this.duration;
        int fluct = new Random(seed * traderSeedFactor).nextInt(-this.fluctuation, this.fluctuation + 1);
        debugFlux(seed * traderSeedFactor, this.fluctuation, fluct);

        return 1d + ((double)fluct/100d);
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event) {
        event.applyCostMultiplier(this.randomizePriceMultiplier(this.getTraderSeedFactor(event)));
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        compound.putLong("Duration", this.duration);
        compound.putInt("Fluctuation", this.fluctuation);

    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        this.duration = compound.getLong("Duration");
        if(this.duration <= 0)
            this.duration = TimeUtil.DURATION_DAY;

        this.fluctuation = compound.getInt("Fluctuation");

    }

    @Override
    public JsonObject saveToJson(JsonObject json) {

        json.addProperty("Duration", this.duration);
        json.addProperty("Fluctuation", this.fluctuation);

        return json;
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if(json.has("Duration"))
            this.duration = json.get("Duration").getAsLong();
        if(json.has("Fluctuation"))
            this.fluctuation = json.get("Fluctuation").getAsInt();
    }

    @Override
    public NbtCompound savePersistentData() { return null; }
    @Override
    public void loadPersistentData(NbtCompound data) {}

    @Override
    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_PRICE_FLUCTUATION; }

    @Override
    protected void handleUpdateMessage(NbtCompound updateInfo) {
        if(updateInfo.contains("Duration"))
            this.duration = updateInfo.getLong("Duration");
        if(updateInfo.contains("Fluctuation"))
            this.fluctuation = updateInfo.getInt("Fluctuation");
    }

    @Override
    @Environment(EnvType.CLIENT)
    public GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GuiHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GuiHandler extends TradeRule.GUIHandler
    {

        protected final PriceFluctuation getRule()
        {
            if(getRuleRaw() instanceof PriceFluctuation)
                return (PriceFluctuation)getRuleRaw();
            return null;
        }

        GuiHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { super(screen, rule); }

        TextFieldWidget fluctuationInput;
        ButtonWidget buttonSetFluctuation;

        TimeInputWidget durationInput;

        @Override
        public void initTab() {
            this.fluctuationInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, Text.empty()));
            this.fluctuationInput.setMaxLength(2);
            this.fluctuationInput.setText(Integer.toString(this.getRule().fluctuation));

            this.buttonSetFluctuation = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, Text.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetFluctuationButton));

            this.durationInput = this.addCustomRenderable(new TimeInputWidget(screen.guiLeft() + 48, screen.guiTop() + 75, 10, TimeUnit.DAY, TimeUnit.MINUTE, this::addCustomRenderable, this::onTimeSet));
            this.durationInput.setTime(this.getRule().duration);

        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
            if(getRule() == null)
                return;

            gui.drawText(this.screen.getFont(), Text.translatable("gui.lightmanscurrency.fluctuation.tooltip"), this.fluctuationInput.getX() + this.fluctuationInput.getWidth() + 4, this.fluctuationInput.getY() + 3, 0xFFFFFF, false);

            TextRenderUtil.drawCenteredMultilineText(gui, Text.translatable("gui.button.lightmanscurrency.price_fluctuation.info", this.getRule().fluctuation, new TimeData(this.getRule().duration).getShortString()), this.screen.guiLeft() + 10, this.screen.xSize - 20, this.screen.guiTop() + 35, 0xFFFFFF);

        }

        @Override
        public void onScreenTick() {
            TextInputUtil.whitelistInteger(this.fluctuationInput, 0, 99);
        }

        @Override
        public void onTabClose() {

            this.removeCustomWidget(this.fluctuationInput);
            this.removeCustomWidget(this.buttonSetFluctuation);
            this.durationInput.removeChildren(this::removeCustomWidget);
            this.removeCustomWidget(this.durationInput);

        }

        void PressSetFluctuationButton(ButtonWidget button)
        {
            int fluctuation = TextInputUtil.getIntegerValue(this.fluctuationInput, 1);
            this.getRule().fluctuation = fluctuation;
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putInt("Fluctuation", fluctuation);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

        public void onTimeSet(TimeData newTime)
        {
            this.getRule().duration = newTime.miliseconds;
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putLong("Duration", newTime.miliseconds);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

    }

}