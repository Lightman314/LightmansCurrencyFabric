package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.common.traders.ITraderSource;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageExecuteTrade;
import net.minecraft.client.gui.DrawContext;

public class TraderInteractionTab extends TraderClientTab {

    public TraderInteractionTab(TraderScreen screen) { super(screen); }

    @Override
    public boolean blockInventoryClosing() { return false; }

    TradeButtonArea tradeDisplay;

    @Override
    public void onOpen() {
        //Trade Button Display
        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.traderSource, this.menu::getContext, this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getImageWidth() - 6, 100, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, this::OnButtonPress, TradeButtonArea.FILTER_VALID));
        this.tradeDisplay.init();
    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        this.tradeDisplay.renderTraderName(gui, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getImageWidth() - 16, false);
        this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);
    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {
        if(this.menu.getCursorStack().isEmpty())
            this.tradeDisplay.renderTooltips(gui, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getImageWidth() - 16, mouseX, mouseY);
    }

    @Override
    public void tick() {
        this.tradeDisplay.tick();
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

    private void OnButtonPress(TraderData trader, TradeData trade) {

        if(trader == null || trade == null)
            return;

        ITraderSource ts = this.menu.traderSource;
        if(ts == null)
        {
            this.menu.closeMenu(this.menu.player);
            return;
        }

        List<TraderData> traders = ts.getTraders();
        int ti = traders.indexOf(trader);
        if(ti < 0)
            return;

        TraderData t = traders.get(ti);
        if(t == null)
            return;

        int tradeIndex = t.getTradeData().indexOf(trade);
        if(tradeIndex < 0)
            return;

        new CMessageExecuteTrade(ti, tradeIndex).sendToServer();

    }

}