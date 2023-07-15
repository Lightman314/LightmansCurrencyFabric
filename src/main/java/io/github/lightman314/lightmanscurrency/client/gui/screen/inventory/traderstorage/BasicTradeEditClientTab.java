package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TradeButtonArea;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.trades_basic.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class BasicTradeEditClientTab<T extends BasicTradeEditTab> extends TraderStorageClientTab<T> implements InteractionConsumer {

    public BasicTradeEditClientTab(TraderStorageScreen screen, T commonTab) { super(screen, commonTab); this.commonTab.setClientHandler(screen);}

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_TRADELIST; }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.trader.edit_trades"); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    TradeButtonArea tradeDisplay;

    ButtonWidget buttonAddTrade;
    ButtonWidget buttonRemoveTrade;

    @Override
    public void onOpen() {

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButtonArea(this.menu.getTrader(), t -> this.menu.getContext(), this.screen.getGuiLeft() + 3, this.screen.getGuiTop() + 17, this.screen.getImageWidth() - 6, 100, this.screen::addRenderableTabWidget, this.screen::removeRenderableTabWidget, (t1,t2) -> {}, this.menu.getTrader() == null ? TradeButtonArea.FILTER_ANY : this.menu.getTrader().getStorageDisplayFilter(this.menu)));
        this.tradeDisplay.init();
        this.tradeDisplay.setInteractionConsumer(this);

        this.buttonAddTrade = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getImageWidth() - 25, this.screen.getGuiTop() + 4, 10, 10, this::AddTrade, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 20));
        this.buttonRemoveTrade = this.screen.addRenderableTabWidget(new PlainButton(this.screen.getGuiLeft() + this.screen.getImageWidth() - 14, this.screen.getGuiTop() + 4, 10, 10, this::RemoveTrade, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 20));

        this.tick();

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.tradeDisplay.tick();

        this.tradeDisplay.renderTraderName(gui, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getImageWidth() - (this.renderAddRemoveButtons() ? 32 : 16), true);

        this.tradeDisplay.getScrollBar().beforeWidgetRender(mouseY);

    }

    private boolean renderAddRemoveButtons() {
        if(this.menu.getTrader() != null)
            return this.menu.getTrader().canEditTradeCount();
        return false;
    }

    @Override
    public void tick() {
        super.tick();

        TraderData trader = this.menu.getTrader();
        if(trader != null)
        {
            this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = trader.canEditTradeCount();
            this.buttonAddTrade.active = trader.getTradeCount() < trader.getMaxTradeCount();
            this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
        }
        else
            this.buttonAddTrade.visible = this.buttonRemoveTrade.visible = false;
    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        if(this.menu.getCursorStack().isEmpty())
            this.tradeDisplay.renderTooltips(gui, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, this.screen.getImageWidth() - (this.renderAddRemoveButtons() ? 27 : 16), mouseX, mouseY);

    }

    @Override
    public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
        trade.onInputDisplayInteraction(this.commonTab, this.screen, index, mouseButton, this.menu.getCursorStack());
    }

    @Override
    public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
        trade.onOutputDisplayInteraction(this.commonTab, this.screen, index, mouseButton, this.menu.getCursorStack());
    }

    @Override
    public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) {
        trade.onInteraction(this.commonTab, this.screen, localMouseX, localMouseY, mouseButton, this.menu.getCursorStack());
    }

    private void AddTrade(ButtonWidget button) { this.commonTab.addTrade(); }

    private void RemoveTrade(ButtonWidget button) { this.commonTab.removeTrade(); }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.getScrollBar().onMouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.tradeDisplay.getScrollBar().onMouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

}