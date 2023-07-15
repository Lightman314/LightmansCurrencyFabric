package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.TradeSelectTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TradeSelectClientTab extends TraderInterfaceClientTab<TradeSelectTab> {

    public TradeSelectClientTab(TraderInterfaceScreen screen, TradeSelectTab commonTab) { super(screen, commonTab); }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.interface.trade"); }

    @Override
    public boolean blockInventoryClosing() { return false; }

    @Override
    public boolean tabButtonVisible() { return this.commonTab.canOpen(this.menu.player); }

    TradeButtonArea tradeDisplay;

    @Override
    public void onOpen() {

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.getTraderInterface().getTrader(), trader -> this.menu.getTraderInterface().getTradeContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getImageWidth() - 6, 100, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, this::SelectTrade, TradeButtonArea.FILTER_VALID));
        this.tradeDisplay.init();
        this.tradeDisplay.setSelectionDefinition(this::isTradeSelected);

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.tradeDisplay.tick();

        this.tradeDisplay.renderTraderName(gui, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getImageWidth() - 16, true);

        this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);

    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        this.tradeDisplay.renderTooltips(gui, 0, 0, 0, mouseX, mouseY);

    }

    @Override
    public void tick() {
        if(!this.commonTab.canOpen(this.menu.player))
            this.screen.changeTab(TraderInterfaceTab.TAB_INFO);
    }

    private boolean isTradeSelected(TraderData trader, TradeData trade) {
        return this.menu.getTraderInterface().getTrueTrade() == trade;
    }

    private int getTradeIndex(TraderData trader, TradeData trade) {
        List<? extends TradeData> trades = trader.getTradeData();
        if(trades != null)
            return trades.indexOf(trade);
        return -1;
    }

    private void SelectTrade(TraderData trader, TradeData trade) {

        this.commonTab.setTradeIndex(this.getTradeIndex(trader, trade));

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.getScrollBar().onMouseClicked(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.tradeDisplay.getScrollBar().onMouseReleased(mouseX, mouseY, button);
        return false;
    }

}