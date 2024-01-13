package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.auction;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.server.messages.auction.CMessageSubmitBid;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AuctionBidTab extends TraderClientTab {

    private final long auctionHouseID;
    private final int tradeIndex;

    private AuctionHouseTrader getAuctionHouse() {
        TraderData data = TraderSaveData.GetTrader(true, this.auctionHouseID);
        if(data instanceof AuctionHouseTrader)
            return (AuctionHouseTrader)data;
        return null;
    }

    private AuctionTradeData getTrade() {
        AuctionHouseTrader trader = this.getAuctionHouse();
        if(trader != null)
            return trader.getTrade(this.tradeIndex);
        return null;
    }

    public AuctionBidTab(TraderScreen screen, long auctionHouseID, int tradeIndex) { super(screen); this.auctionHouseID = auctionHouseID; this.tradeIndex = tradeIndex; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    //Auction Bid Display
    TradeButton tradeDisplay;

    //Bid Amount Input
    CoinValueInput bidAmount;

    //Bid Button
    ButtonWidget bidButton;

    ButtonWidget closeButton;

    @Override
    public void onOpen() {

        if(this.getTrade() == null)
            return;

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(() -> this.menu.getContext(this.getAuctionHouse()), this::getTrade, b -> {}));
        this.tradeDisplay.setPosition(this.screen.getGuiLeft() + this.screen.getImageWidth() / 2 - this.tradeDisplay.getWidth() / 2, this.screen.getGuiTop() + 5);

        this.bidAmount = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + this.screen.getImageWidth() / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 10 + this.tradeDisplay.getHeight(), Text.translatable("gui.lightmanscurrency.auction.bidamount"), this.getTrade().getMinNextBid(), this.font, v -> {}, this.screen::addRenderableTabWidget));
        this.bidAmount.init();
        this.bidAmount.allowFreeToggle = false;
        this.bidAmount.drawBG = false;

        this.bidButton = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + 22, this.screen.getGuiTop() + 119, 68, 20, Text.translatable("gui.lightmanscurrency.auction.bid"), this::SubmitBid));

        this.closeButton = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + this.screen.getImageWidth() - 25, this.screen.getGuiTop() + 5, 20, 20, Text.literal("X").formatted(Formatting.RED, Formatting.BOLD), this::close));

        this.tick();

    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        this.tradeDisplay.renderTooltips(gui, this.font, mouseX, mouseY);

    }

    @Override
    public void tick() {
        if(this.getTrade() == null)
        {
            this.screen.closeTab();
            return;
        }

        if(this.bidAmount != null)
        {
            long bidQuery = this.bidAmount.getCoinValue().getRawValue();
            CoinValue minBid = this.getTrade().getMinNextBid();
            if(bidQuery < minBid.getRawValue())
                this.bidAmount.setCoinValue(this.getTrade().getMinNextBid());
            this.bidButton.active = this.menu.getContext(this.getAuctionHouse()).getAvailableFunds() >= bidQuery;

            this.bidAmount.tick();
        }

    }

    private void SubmitBid(ButtonWidget button) {
        new CMessageSubmitBid(this.auctionHouseID, this.tradeIndex, this.bidAmount.getCoinValue()).sendToServer();
        this.screen.closeTab();
    }

    private void close(ButtonWidget button) { this.screen.closeTab(); }

}