package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.paygate;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.paygate.PaygateTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.PaygateTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.tradedata.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class PaygateTradeEditClientTab extends TraderStorageClientTab<PaygateTradeEditTab> implements InteractionConsumer {

    public PaygateTradeEditClientTab(TraderStorageScreen screen, PaygateTradeEditTab commonTab) {
        super(screen, commonTab);
    }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

    @Override
    public MutableText getTooltip() { return Text.empty(); }

    @Override
    public boolean tabButtonVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

    TradeButton tradeDisplay;
    CoinValueInput priceSelection;
    TextFieldWidget durationInput;

    @Override
    public void onOpen() {

        PaygateTradeData trade = this.commonTab.getTrade();

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
        this.tradeDisplay.setPosition(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18);
        this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + TraderScreen.WIDTH / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 55, Text.empty(), trade == null ? CoinValue.EMPTY : trade.getCost(), this.font, this::onValueChanged, this.screen::addRenderableTabWidget));
        this.priceSelection.drawBG = false;
        this.priceSelection.init();

        int labelWidth = this.font.getWidth(Text.translatable("gui.lightmanscurrency.duration"));
        int unitWidth = this.font.getWidth(Text.translatable("gui.lightmanscurrency.duration.unit"));
        this.durationInput = this.screen.addRenderableTabWidget(new TextFieldWidget(this.font, this.screen.getGuiLeft() + 15 + labelWidth, this.screen.getGuiTop() + 38, this.screen.getImageWidth() - 30 - labelWidth - unitWidth, 18, Text.empty()));
        this.durationInput.setText(String.valueOf(trade == null ? "" : trade.getDuration()));

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(this.commonTab.getTrade() == null)
            return;

        this.validateRenderables();

        gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.duration"), this.screen.getGuiLeft() + 13, this.screen.getGuiTop() + 42, 0x404040, false);
        int unitWidth = this.font.getWidth(Text.translatable("gui.lightmanscurrency.duration.unit"));
        gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.duration.unit"), this.screen.getGuiLeft() + this.screen.getImageWidth() - unitWidth - 13, this.screen.getGuiTop() + 42, 0x404040, false);


    }

    private void validateRenderables() {

        this.priceSelection.visible = !this.commonTab.getTrade().isTicketTrade();
        if(this.priceSelection.visible)
            this.priceSelection.tick();
        TextInputUtil.whitelistInteger(this.durationInput, PaygateTraderData.DURATION_MIN, PaygateTraderData.DURATION_MAX);
        int inputDuration = Math.max(TextInputUtil.getIntegerValue(this.durationInput, PaygateTraderData.DURATION_MIN), PaygateTraderData.DURATION_MIN);
        if(inputDuration != this.commonTab.getTrade().getDuration())
            this.commonTab.setDuration(inputDuration);
    }

    @Override
    public void tick() {
        this.durationInput.tick();
    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        this.tradeDisplay.renderTooltips(gui, this.font, mouseX, mouseY);

    }

    @Override
    public void receiveSelfMessage(NbtCompound message) {
        if(message.contains("TradeIndex"))
            this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
    }

    @Override
    public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
        if(trade instanceof PaygateTradeData t)
        {
            if(this.menu.getCursorStack().getItem() == ModItems.TICKET_MASTER)
            {
                UUID ticketID = TicketItem.GetTicketID(this.menu.getCursorStack());
                this.commonTab.setTicket(ticketID);
            }
            else if(t.isTicketTrade())
            {
                this.commonTab.setTicket(null);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
        return false;
    }

    @Override
    public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) { }

    @Override
    public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

    public void onValueChanged(CoinValue value) { this.commonTab.setPrice(value.copy()); }

}