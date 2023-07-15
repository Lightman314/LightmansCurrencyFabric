package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.common.base.Supplier;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TradeLimit extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "trade_limit");

    private int limit = 1;
    public int getLimit() { return this.limit; }
    public void setLimit(int newLimit) { this.limit = newLimit; }

    int count = 0;
    public void resetCount() { this.count = 0; }

    public TradeLimit() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {

        if(this.count >= this.limit)
        {
            event.addDenial(Text.translatable("traderule.lightmanscurrency.tradelimit2.denial", this.count));
            event.addDenial(Text.translatable("traderule.lightmanscurrency.tradelimit.denial.limit", this.limit));
        }
        else
            event.addHelpful(Text.translatable("traderule.lightmanscurrency.tradelimit2.info", this.count, this.limit));
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event) {

        this.count++;

        event.markDirty();

    }

    @Override
    protected void saveAdditional(NbtCompound compound) {

        compound.putInt("Limit", this.limit);
        compound.putInt("Count", this.count);

    }

    @Override
    public JsonObject saveToJson(JsonObject json) {
        json.addProperty("Limit", this.limit);
        return json;
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        if(compound.contains("Limit", NbtCompound.INT_TYPE))
            this.limit = compound.getInt("Limit");
        if(compound.contains("Count", NbtCompound.INT_TYPE))
            this.count = compound.getInt("Count");

    }

    @Override
    public void loadFromJson(JsonObject json) {
        if(json.has("Limit"))
            this.limit = json.get("Limit").getAsInt();
    }

    @Override
    public void handleUpdateMessage(NbtCompound updateInfo)
    {
        if(updateInfo.contains("Limit"))
        {
            this.limit = updateInfo.getInt("Limit");
        }
        else if(updateInfo.contains("ClearMemory"))
        {
            this.count = 0;
        }
    }

    @Override
    public NbtCompound savePersistentData() {
        NbtCompound data = new NbtCompound();
        data.putInt("Count", this.count);
        return data;
    }
    @Override
    public void loadPersistentData(NbtCompound data) {
        if(data.contains("Count", NbtElement.INT_TYPE))
            this.count = data.getInt("Count");
    }

    @Override
    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_COUNT; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        private TradeLimit getRule()
        {
            if(getRuleRaw() instanceof TradeLimit)
                return (TradeLimit)getRuleRaw();
            return null;
        }

        GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            super(screen, rule);
        }

        TextFieldWidget limitInput;
        ButtonWidget buttonSetLimit;
        ButtonWidget buttonClearMemory;

        @Override
        public void initTab() {

            this.limitInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 19, 30, 20, Text.empty()));
            this.limitInput.setMaxLength(3);
            this.limitInput.setText(Integer.toString(this.getRule().limit));

            this.buttonSetLimit = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 41, screen.guiTop() + 19, 40, 20, Text.translatable("gui.button.lightmanscurrency.playerlimit.setlimit"), this::PressSetLimitButton));
            this.buttonClearMemory = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 50, screen.xSize - 20, 20, Text.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory"), this::PressClearMemoryButton));
        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

            gui.drawText(this.screen.getFont(), Text.translatable("gui.button.lightmanscurrency.playerlimit.info", this.getRule().limit).getString(), screen.guiLeft() + 10, screen.guiTop() + 9, 0xFFFFFF, false);

            if(this.buttonClearMemory.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.screen.getFont(), Text.translatable("gui.button.lightmanscurrency.playerlimit.clearmemory.tooltip"), mouseX, mouseY);

        }

        @Override
        public void onTabClose() {

            this.removeCustomWidget(this.limitInput);
            this.removeCustomWidget(this.buttonSetLimit);
            this.removeCustomWidget(this.buttonClearMemory);

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
            this.getRule().resetCount();
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putBoolean("ClearMemory", true);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

    }

}