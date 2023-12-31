package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.InfoTab;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TradeContext.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData.TradeComparisonResult;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class InfoClientTab extends TraderInterfaceClientTab<InfoTab> {

    public InfoClientTab(TraderInterfaceScreen screen, InfoTab tab) { super(screen, tab); }

    TradeButton tradeDisplay;
    TradeButton newTradeDisplay;

    ScrollTextDisplay changesDisplay;

    DropdownWidget interactionDropdown;

    ButtonWidget acceptChangesButton;

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Items.PAPER); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.interface.info"); }

    @Override
    public boolean blockInventoryClosing() { return false; }

    @Override
    public void onOpen() {

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getTradeContext, this.menu.getTraderInterface()::getReferencedTrade, TradeButton.NULL_PRESS));
        this.tradeDisplay.setPosition(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 20);
        this.tradeDisplay.displayOnly = true;
        this.newTradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getTradeContext, this.menu.getTraderInterface()::getTrueTrade, TradeButton.NULL_PRESS));
        this.newTradeDisplay.visible = false;
        this.newTradeDisplay.displayOnly = true;

        this.interactionDropdown = this.screen.addRenderableTabWidget(IconAndButtonUtil.interactionTypeDropdown(this.screen.getGuiLeft() + 104, this.screen.getGuiTop() + 20, 97, this.font, this.menu.getTraderInterface().getInteractionType(), this::onInteractionSelect, this.screen::addRenderableTabWidget, this.menu.getTraderInterface().getBlacklistedInteractions()));

        this.changesDisplay = this.screen.addRenderableTabWidget(new ScrollTextDisplay(this.screen.getGuiLeft() + 104, this.screen.getGuiTop() + 36, 97, 73, this.font, this::getMessages));
        //Set background color to clear.
        this.changesDisplay.backgroundColor = 0x00000000;

        this.acceptChangesButton = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getImageWidth(), this.screen.getGuiTop() + 40, this::AcceptTradeChanges, IconAndButtonUtil.ICON_CHECKMARK, new IconAndButtonUtil.SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.interface.info.acceptchanges"))));
        this.acceptChangesButton.visible = false;

    }

    private List<Text> getMessages() {
        if(this.menu.getTraderInterface() == null)
            return new ArrayList<>();

        //Get last result
        List<Text> list = new ArrayList<>();
        TradeResult result = this.menu.getTraderInterface().mostRecentTradeResult();
        if(result.failMessage != null)
            list.add(result.failMessage);

        if(this.menu.getTraderInterface().getInteractionType().trades)
        {
            TradeData referencedTrade = this.menu.getTraderInterface().getReferencedTrade();
            TradeData trueTrade = this.menu.getTraderInterface().getTrueTrade();
            if(referencedTrade == null)
                return new ArrayList<>();
            if(trueTrade == null)
            {
                list.add(Text.translatable("gui.lightmanscurrency.interface.difference.missing").formatted(Formatting.RED));
                return list;
            }
            TradeComparisonResult differences = referencedTrade.compare(trueTrade);
            //Type check
            if(!differences.TypeMatches())
            {
                list.add(Text.translatable("gui.lightmanscurrency.interface.difference.type").formatted(Formatting.RED));
                return list;
            }
            //Trade-specific checks
            list.addAll(referencedTrade.GetDifferenceWarnings(differences));
            return list;
        }
        else if(this.menu.getTraderInterface().getInteractionType().requiresPermissions)
        {
            TraderData trader = this.menu.getTraderInterface().getTrader();
            if(trader != null && !trader.hasPermission(this.menu.getTraderInterface().getReferencedPlayer(), Permissions.INTERACTION_LINK))
            {
                list.add(Text.translatable("gui.lightmanscurrency.interface.info.trader.permissions").formatted(Formatting.RED));
            }
        }
        return list;
    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(this.menu.getTraderInterface() == null)
            return;

        //Trader name
        TraderData trader = this.menu.getTraderInterface().getTrader();
        Text infoText;
        if(trader != null)
            infoText = trader.getTitle();
        else
        {
            if(this.menu.getTraderInterface().hasTrader())
                infoText = Text.translatable("gui.lightmanscurrency.interface.info.trader.removed").formatted(Formatting.RED);
            else
                infoText = Text.translatable("gui.lightmanscurrency.interface.info.trader.null");

        }
        gui.drawText(this.font, TextRenderUtil.fitString(infoText, this.screen.getImageWidth() - 16), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);

        this.tradeDisplay.visible = this.menu.getTraderInterface().getInteractionType().trades;
        this.newTradeDisplay.visible = this.tradeDisplay.visible && this.changeInTrades();
        this.acceptChangesButton.visible = this.newTradeDisplay.visible;

        if(this.tradeDisplay.visible)
        {
            //If no defined trade, give "No Trade Selected" message.
            if(this.menu.getTraderInterface().getReferencedTrade() == null)
                gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.interface.info.trade.notdefined"), this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 20, 0x404040, false);
        }
        if(this.newTradeDisplay.visible)
        {
            //Reposition the new trade button, as we now know it's height.
            this.newTradeDisplay.setPosition(this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 109 - this.newTradeDisplay.getHeight());
            //Render the down arrow
            gui.setShaderColor(1f, 1f, 1f, 1f);
            gui.drawTexture(TraderInterfaceScreen.GUI_TEXTURE, this.screen.getGuiLeft() - 2 + (this.tradeDisplay.getWidth() / 2), this.screen.getGuiTop() + 54, TraderInterfaceScreen.WIDTH, 18, 16, 22);

            //If no found trade, give "Trade No Longer Exists" message.
            if(this.menu.getTraderInterface().getTrueTrade() == null)
                gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.interface.info.trade.missing").formatted(Formatting.RED), this.screen.getGuiLeft() + 6, this.screen.getGuiTop() + 109 - this.font.fontHeight, 0x404040, false);

        }

        BankAccount account = this.menu.getTraderInterface().getBankAccount();
        if(account != null && this.menu.getTraderInterface().getInteractionType().trades)
        {
            Text accountName = TextRenderUtil.fitString(account.getName(), 160);
            gui.drawText(this.font, accountName, this.screen.getGuiLeft() + TraderInterfaceMenu.SLOT_OFFSET + 88 - (this.font.getWidth(accountName) / 2), this.screen.getGuiTop() + 120, 0x404040, false);
            Text balanceText = Text.translatable("gui.lightmanscurrency.bank.balance", account.getCoinStorage().getString("0"));
            gui.drawText(this.font, balanceText, this.screen.getGuiLeft() + TraderInterfaceMenu.SLOT_OFFSET + 88 - (this.font.getWidth(balanceText) / 2), this.screen.getGuiTop() + 130, 0x404040, false);
        }

    }

    public boolean changeInTrades() {
        TradeData referencedTrade = this.menu.getTraderInterface().getReferencedTrade();
        TradeData trueTrade = this.menu.getTraderInterface().getTrueTrade();
        if(referencedTrade == null)
            return false;
        if(trueTrade == null)
            return true;
        return !referencedTrade.compare(trueTrade).Identical();
    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        if(this.menu.getTraderInterface() == null)
            return;

        //Render the currently referenced trade's tooltips (no stock or other misc stuff, just the item tooltips & original name)
        this.tradeDisplay.renderTooltips(gui, this.font, mouseX, mouseY);
        this.newTradeDisplay.renderTooltips(gui, this.font, mouseX, mouseY);

        //IconAndButtonUtil.renderButtonTooltips(gui, this.font, mouseX, mouseY, List.of(this.acceptChangesButton));

    }

    @Override
    public void tick() { }

    @Override
    public void onClose() { }

    private void onInteractionSelect(int newTypeIndex) {
        TraderInterfaceBlockEntity.InteractionType newType = TraderInterfaceBlockEntity.InteractionType.fromIndex(newTypeIndex);
        this.commonTab.changeInteractionType(newType);
    }

    private void AcceptTradeChanges(ButtonWidget button) {
        this.commonTab.acceptTradeChanges();
    }

}