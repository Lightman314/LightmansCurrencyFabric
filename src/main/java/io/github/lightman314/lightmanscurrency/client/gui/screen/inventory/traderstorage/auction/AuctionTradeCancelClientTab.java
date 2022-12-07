package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.auction.AuctionTradeCancelTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class AuctionTradeCancelClientTab extends TraderStorageClientTab<AuctionTradeCancelTab> {

    public AuctionTradeCancelClientTab(TraderStorageScreen screen, AuctionTradeCancelTab commonTab) { super(screen,commonTab); }

    @Override
    public @NotNull IconData getIcon() { return IconData.BLANK; }

    @Override
    public MutableText getTooltip() { return Text.empty(); }

    @Override
    public boolean tabButtonVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    TradeButton tradeDisplay;

    ButtonWidget buttonCancelPlayerGive;
    ButtonWidget buttonCancelStorageGive;

    @Override
    public void onOpen() {

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, b -> {}));
        this.tradeDisplay.move(this.screen.getGuiLeft() + (this.screen.getImageWidth() / 2) - 47, this.screen.getGuiTop() + 17);

        this.buttonCancelPlayerGive = this.screen.addRenderableTabWidget(new ButtonWidget(this.screen.getGuiLeft() + 40, this.screen.getGuiTop() + 60, this.screen.getImageWidth() - 80, 20, Text.translatable("button.lightmanscurrency.auction.cancel.self"), b -> this.commonTab.cancelAuction(true)));
        this.buttonCancelStorageGive = this.screen.addRenderableTabWidget(new ButtonWidget(this.screen.getGuiLeft() + 40, this.screen.getGuiTop() + 85, this.screen.getImageWidth() - 80, 20, Text.translatable("button.lightmanscurrency.auction.cancel.storage"), b -> this.commonTab.cancelAuction(false)));

    }

    @Override
    public void renderBG(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {

        TextRenderUtil.drawCenteredText(pose, Text.translatable("tooltip.lightmanscurrency.auction.cancel"), this.screen.getGuiLeft() + (this.screen.getImageWidth() / 2), this.screen.getGuiTop() + 50, 0x404040);

    }

    @Override
    public void renderTooltips(MatrixStack pose, int mouseX, int mouseY) {

        this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);

        if(this.buttonCancelPlayerGive.isMouseOver(mouseX, mouseY))
            this.screen.renderOrderedTooltip(pose, this.font.wrapLines(Text.translatable("tooltip.lightmanscurrency.auction.cancel.self"), 160), mouseX, mouseY);
        if(this.buttonCancelStorageGive.isMouseOver(mouseX, mouseY))
            this.screen.renderOrderedTooltip(pose, this.font.wrapLines(Text.translatable("tooltip.lightmanscurrency.auction.cancel.storage"), 160), mouseX, mouseY);
    }

    @Override
    public void tick() {
        //Reopen the default tab if the trade is null, or we're not allowed to edit it. (Or it's already been handled).
        AuctionTradeData trade = this.commonTab.getTrade();
        if(trade == null || !trade.isOwner(this.menu.player) || !trade.isValid())
            this.screen.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
    }

    @Override
    public void receiveSelfMessage(NbtCompound message) {
        if(message.contains("TradeIndex"))
            this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
    }

    @Override
    public void receiveServerMessage(NbtCompound message) {
        if(message.contains("CancelSuccess"))
            this.screen.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
    }

}