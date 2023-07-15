package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.base.TraderSelectTab;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import io.github.lightman314.lightmanscurrency.common.traders.terminal.filters.TraderSearchFilter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TraderSelectClientTab extends TraderInterfaceClientTab<TraderSelectTab> implements IScrollable{

    public TraderSelectClientTab(TraderInterfaceScreen screen, TraderSelectTab tab) { super(screen,tab); }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModBlocks.TERMINAL); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.interface.trader"); }

    @Override
    public boolean blockInventoryClosing() { return true; }

    TextFieldWidget searchField;

    ScrollBarWidget scrollBar;

    List<NetworkTraderButton> traderButtons;

    private int scroll;

    private List<TraderData> filteredTraderList = new ArrayList<>();

    private List<TraderData> traderList() {
        List<TraderData> traderList = this.filterTraders(TraderSaveData.GetAllTerminalTraders(true));
        traderList.sort(TradingTerminalScreen.TERMINAL_SORTER);
        return traderList;
    }

    private List<TraderData> filterTraders(List<TraderData> allTraders) {
        List<TraderData> traders = new ArrayList<>();
        TraderInterfaceBlockEntity be = this.menu.getTraderInterface();
        if(be == null)
            return traders;
        TraderInterfaceBlockEntity.InteractionType interaction = be.getInteractionType();
        for(TraderData trader : allTraders) {
            //Confirm that the trader is the trade type that our interface is compatible with.
            if(be.validTraderType(trader))
            {
                //Confirm that the trader either has a valid trade, or we have interaction permissions
                if((interaction.trades && trader.hasValidTrade()) || (interaction.requiresPermissions && be.hasTraderPermissions(trader)))
                    traders.add(trader);
            }
        }
        return traders;
    }

    @Override
    public void onOpen() {

        this.searchField = this.screen.addRenderableTabWidget(new TextFieldWidget(this.font, this.screen.getGuiLeft() + 43, this.screen.getGuiTop() + 6, 101, 9, Text.translatable("gui.lightmanscurrency.terminal.search")));
        this.searchField.setDrawsBackground(false);
        this.searchField.setMaxLength(32);
        this.searchField.setEditableColor(0xFFFFFF);

        this.initTraderButtons(this.screen.getGuiLeft(), this.screen.getGuiTop());

        this.scrollBar = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + 30 + NetworkTraderButton.WIDTH, this.screen.getGuiTop() + 18, NetworkTraderButton.HEIGHT * 4, this));

        this.tick();

        this.updateTraderList();

        //Automatically go to the page with the currently selected trader.
        TraderData selectedTrader = this.menu.getTraderInterface().getTrader();
        if(selectedTrader!= null)
        {
            this.scroll = this.scrollOf(selectedTrader);
            this.updateTraderButtons();
        }

        this.screen.addTabListener(new ScrollListener(0,0, this.screen.width, this.screen.height, this::onMouseScrolled));


    }

    private void initTraderButtons(int guiLeft, int guiTop)
    {
        this.traderButtons = new ArrayList<>();
        for(int y = 0; y < 4; ++y)
        {
            NetworkTraderButton newButton = this.screen.addRenderableTabWidget(new NetworkTraderButton(guiLeft + 30, guiTop + 18 + (y * NetworkTraderButton.HEIGHT), this::SelectTrader, this.font));
            this.traderButtons.add(newButton);
        }
    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        gui.setShaderColor(1f, 1f, 1f, 1f);
        gui.drawTexture(TraderInterfaceScreen.GUI_TEXTURE, this.screen.getGuiLeft() + 28, this.screen.getGuiTop() + 4, 0, TraderInterfaceScreen.HEIGHT, 117, 12);

        this.scrollBar.beforeWidgetRender(mouseY);

    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) { }

    @Override
    public void tick() {

        this.searchField.tick();

        for (NetworkTraderButton button : this.traderButtons) {
            button.selected = button.getData() != null && button.getData() == this.menu.getTraderInterface().getTrader();
        }
    }

    @Override
    public boolean charTyped(char c, int code)
    {
        String s = this.searchField.getText();
        if(this.searchField.charTyped(c, code))
        {
            if(!Objects.equals(s, this.searchField.getText()))
            {
                this.updateTraderList();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods)
    {
        String s = this.searchField.getText();
        if(this.searchField.keyPressed(key, scanCode, mods))
        {
            if(!Objects.equals(s,  this.searchField.getText()))
            {
                this.updateTraderList();
            }
            return true;
        }
        return false;
    }

    private void SelectTrader(ButtonWidget button) {
        int index = getTraderIndex(button);
        if(index >= 0 && index < this.filteredTraderList.size())
        {
            long traderID = this.filteredTraderList.get(index).getID();
            this.commonTab.setTrader(traderID);
        }
    }

    private int getTraderIndex(ButtonWidget button) {
        if(!traderButtons.contains(button))
            return -1;
        int index = traderButtons.indexOf(button);
        index += this.scroll;
        return index;
    }

    public int getMaxScroll() { return Math.max(this.filteredTraderList.size() - this.traderButtons.size(), 0); }

    private int scrollOf(TraderData trader) {
        if(this.filteredTraderList != null)
        {
            int index = this.filteredTraderList.indexOf(trader);
            if(index >= 0)
                return Math.min(index, this.getMaxScroll());
            return this.scroll;
        }
        return this.scroll;
    }

    private void updateTraderList()
    {
        //Filtering of results moved to the TradingOffice.filterTraders
        this.filteredTraderList = TraderSearchFilter.FilterTraders(this.traderList(), this.searchField.getText());
        this.updateTraderButtons();
        //Limit the page
        if(this.scroll > this.getMaxScroll())
            this.scroll = this.getMaxScroll();
    }

    private void updateTraderButtons()
    {
        int startIndex = this.scroll;
        for(int i = 0; i < this.traderButtons.size(); ++i)
        {
            if(startIndex + i < this.filteredTraderList.size())
                this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
            else
                this.traderButtons.get(i).SetData(null);
        }
    }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) {
        this.scroll = Math.min(newScroll, this.getMaxScroll());
        this.updateTraderButtons();
    }

    private boolean onMouseScrolled(double mouseX, double mouseY, double delta) {
        if(delta < 0)
        {
            if(this.scroll < this.getMaxScroll())
                this.setScroll(this.scroll + 1);
        }
        else if(delta > 0)
        {
            if(this.scroll > 0)
                this.setScroll(this.scroll - 1);
        }
        return false;
    }

}