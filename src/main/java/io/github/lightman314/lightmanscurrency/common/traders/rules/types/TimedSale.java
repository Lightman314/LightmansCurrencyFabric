package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TimedSale extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "timed_sale");

    long startTime = 0;
    boolean timerActive() { return this.startTime != 0; }
    long duration = 0;
    int discount = 10;
    public int getDiscountPercent() { return this.discount; }
    public void setDiscountPercent(int percent) { this.discount = MathUtil.clamp(percent, 0, 100); }
    private double getDiscountMult() { return 1d - ((double)discount/100d); }
    private double getIncreaseMult() { return 1d + ((double)discount/100d); }

    public TimedSale() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event)
    {
        if(this.timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
        {
            switch (event.getTrade().getTradeDirection()) {
                case SALE ->
                        event.addHelpful(Text.translatable("traderule.lightmanscurrency.timed_sale.info.sale", this.discount, this.getTimeRemaining().getString()));
                case PURCHASE ->
                        event.addHelpful(Text.translatable("traderule.lightmanscurrency.timed_sale.info.purchase", this.discount, this.getTimeRemaining().getString()));
                default -> {
                } //Nothing if direction is NONE
            }
        }
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event)
    {
        if(timerActive() && TimeUtil.compareTime(this.duration, this.startTime))
        {
            switch (event.getTrade().getTradeDirection()) {
                case SALE -> event.applyCostMultiplier(this.getDiscountMult());
                case PURCHASE -> event.applyCostMultiplier(this.getIncreaseMult());
                default -> {
                } //Nothing if direction is NONE
            }
        }
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event)
    {
        if(confirmStillActive())
            event.markDirty();
    }

    private boolean confirmStillActive()
    {
        if(!timerActive())
            return false;
        else if(!TimeUtil.compareTime(this.duration, this.startTime))
        {
            this.startTime = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        //Write start time
        compound.putLong("StartTime", this.startTime);
        //Save sale duration
        compound.putLong("Duration", this.duration);
        //Save discount
        compound.putInt("Discount", this.discount);
    }

    @Override
    public JsonObject saveToJson(JsonObject json) {

        json.addProperty("Duration", this.duration);
        json.addProperty("Discount", this.discount);

        return json;
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        //Load start time
        if(compound.contains("StartTime", NbtElement.LONG_TYPE))
            this.startTime = compound.getLong("StartTime");
        //Load duration
        if(compound.contains("Duration", NbtElement.LONG_TYPE))
            this.duration = compound.getLong("Duration");
        //Load discount
        if(compound.contains("Discount", NbtElement.INT_TYPE))
            this.discount = compound.getInt("Discount");

    }

    @Override
    public void loadFromJson(JsonObject json) {
        if(json.has("duration"))
            this.duration = json.get("duration").getAsLong();
        if(json.has("discount"))
            this.discount = MathUtil.clamp(this.discount, 0, 100);
    }

    @Override
    public void handleUpdateMessage(NbtCompound updateInfo) {
        if(updateInfo.contains("Discount"))
        {
            this.discount = updateInfo.getInt("Discount");
        }
        else if(updateInfo.contains("Duration"))
        {
            this.duration = updateInfo.getLong("Duration");
        }
        else if(updateInfo.contains("StartSale"))
        {
            if(this.timerActive() == updateInfo.getBoolean("StartSale"))
                return;
            if(this.timerActive())
                this.startTime = 0;
            else
                this.startTime = TimeUtil.getCurrentTime();
        }

    }

    @Override
    public NbtCompound savePersistentData() {
        NbtCompound compound = new NbtCompound();
        compound.putLong("StartTime", this.startTime);
        return compound;
    }
    @Override
    public void loadPersistentData(NbtCompound data) {
        if(data.contains("StartTime", NbtElement.LONG_TYPE))
            this.startTime = data.getLong("StartTime");
    }

    public TimeData getTimeRemaining()
    {
        if(!timerActive())
            return new TimeData(0);
        else
        {
            return new TimeData(this.startTime + this.duration - TimeUtil.getCurrentTime());
        }
    }

    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_TIMED_SALE; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        protected final TimedSale getRule()
        {
            if(getRuleRaw() instanceof TimedSale)
                return (TimedSale)getRuleRaw();
            return null;
        }

        GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            super(screen, rule);
        }

        TextFieldWidget discountInput;

        ButtonWidget buttonSetDiscount;
        ButtonWidget buttonStartSale;

        TimeInputWidget durationInput;

        @Override
        public void initTab() {


            this.discountInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, Text.empty()));
            this.discountInput.setMaxLength(2);
            this.discountInput.setText(Integer.toString(this.getRule().discount));
            this.buttonSetDiscount = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, Text.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));

            this.buttonStartSale = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 45, 156, 20, this.getButtonText(), this::PressStartButton));

            this.durationInput = this.addCustomRenderable(new TimeInputWidget(screen.guiLeft() + 48, screen.guiTop() + 75, 10, TimeUnit.DAY, TimeUnit.MINUTE, this::addCustomRenderable, this::onTimeSet));
            this.durationInput.setTime(this.getRule().duration);

        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

            if(getRule() == null)
                return;

            gui.drawText(this.screen.getFont(), Text.translatable("gui.lightmanscurrency.discount.tooltip"), this.discountInput.getX() + this.discountInput.getWidth() + 4, this.discountInput.getY() + 3, 0xFFFFFF, false);

            Text infoText = Text.translatable("gui.button.lightmanscurrency.timed_sale.info.inactive", new TimeData(this.getRule().duration).getShortString());
            if(this.getRule().timerActive())
                infoText = Text.translatable("gui.button.lightmanscurrency.timed_sale.info.active", this.getRule().getTimeRemaining().getShortString(3));

            gui.drawText(this.screen.getFont(), infoText.getString(), screen.guiLeft() + 10, screen.guiTop() + 35, 0xFFFFFF, false);

            if(this.buttonStartSale.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.screen.getFont(), this.getButtonTooltip(), mouseX, mouseY);

        }

        @Override
        public void onScreenTick()
        {
            this.buttonStartSale.setMessage(getButtonText());
            this.buttonStartSale.active = this.getRule().timerActive() || (this.getRule().duration > 0 && this.getRule().isActive());
            TextInputUtil.whitelistInteger(this.discountInput, 0, 99);

        }

        private Text getButtonText()  { return Text.translatable("gui.button.lightmanscurrency.timed_sale." + (this.getRule().timerActive() ? "stop" : "start")); }

        private Text getButtonTooltip()  { return Text.translatable("gui.button.lightmanscurrency.timed_sale." + (this.getRule().timerActive() ? "stop" : "start") + ".tooltip"); }

        @Override
        public void onTabClose() {

            this.removeCustomWidget(this.discountInput);
            this.removeCustomWidget(this.buttonSetDiscount);
            this.removeCustomWidget(this.buttonStartSale);
            this.durationInput.removeChildren(this::removeCustomWidget);
            this.removeCustomWidget(this.durationInput);
        }

        void PressSetDiscountButton(ButtonWidget button)
        {
            int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
            this.getRule().discount = discount;
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putInt("Discount", discount);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

        void PressStartButton(ButtonWidget button)
        {
            boolean setActive = !this.getRule().timerActive();
            this.getRule().startTime = this.getRule().timerActive() ? 0 : TimeUtil.getCurrentTime();
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putBoolean("StartSale", setActive);
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