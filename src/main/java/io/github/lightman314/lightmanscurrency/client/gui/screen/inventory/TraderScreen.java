package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common.TraderInteractionTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageOpenStorage;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TraderScreen extends MenuScreen<TraderMenu> {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/trader.png");

    public static final int WIDTH = 206;
    public static final int HEIGHT = 236;

    IconButton buttonOpenStorage;
    IconButton buttonCollectCoins;

    ButtonWidget buttonOpenTerminal;

    List<ClickableWidget> tabRenderables = new ArrayList<>();
    List<Element> tabListeners = new ArrayList<>();

    TraderClientTab currentTab = new TraderInteractionTab(this);
    public void setTab(@NotNull TraderClientTab tab) {
        //Close the old tab
        this.currentTab.onClose();
        this.tabRenderables.clear();
        this.tabListeners.clear();
        //Set the new tab
        this.currentTab = tab;
        this.currentTab.onOpen();
    }
    public void closeTab() { this.setTab(new TraderInteractionTab(this)); }

    protected boolean forceShowTerminalButton() { return false; }

    public TraderScreen(TraderMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
        this.backgroundWidth = WIDTH;
        this.backgroundHeight = HEIGHT;
    }

    @Override
    public void init() {

        super.init();

        this.tabRenderables.clear();
        this.tabListeners.clear();

        this.buttonOpenStorage = this.addDrawableChild(IconAndButtonUtil.storageButton(this.x + TraderMenu.SLOT_OFFSET - 20, this.y + 118, this::OpenStorage, () -> this.handler.isSingleTrader() && this.handler.getSingleTrader().hasPermission(this.handler.player, Permissions.OPEN_STORAGE)));
        this.buttonCollectCoins = this.addDrawableChild(IconAndButtonUtil.collectCoinButton(this.x + TraderMenu.SLOT_OFFSET - 20, this.y + 138, this::CollectCoins, this.handler.player, this.handler::getSingleTrader));
        this.buttonOpenTerminal = this.addDrawableChild(IconAndButtonUtil.backToTerminalButton(this.x + TraderMenu.SLOT_OFFSET - 20, this.y + this.backgroundHeight - 20, this::OpenTerminal, this::showTerminalButton));

        LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, TraderMenu.SLOT_OFFSET - 20, 118, 20, this.buttonOpenStorage, this.buttonCollectCoins);

        //Initialize the current tab
        this.currentTab.onOpen();

        this.handledScreenTick();

    }

    private boolean showTerminalButton() {
        return this.forceShowTerminalButton() || (this.handler.isSingleTrader() && this.handler.getSingleTrader().showOnTerminal());
    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY) {

        gui.setShaderColor(1f, 1f, 1f, 1f);

        //Main BG
        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        //Coin Slots
        for(Slot slot : this.handler.getCoinSlots())
        {
            gui.drawTexture(GUI_TEXTURE, this.x + slot.x - 1, this.y + slot.y - 1, this.backgroundWidth, 0, 18, 18);
        }

        //Interaction Slot BG
        if(this.handler.getInteractionSlot().isEnabled())
            gui.drawTexture(GUI_TEXTURE, this.x + this.handler.getInteractionSlot().x - 1, this.y + this.handler.getInteractionSlot().y - 1, this.backgroundWidth, 0, 18, 18);

        try {
            this.currentTab.renderBG(gui, mouseX, mouseY, partialTicks);
            this.tabRenderables.forEach(widget -> widget.render(gui, mouseX, mouseY, partialTicks));
        } catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader tab " + this.currentTab.getClass().getName(), t); }

    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY) {

        gui.drawText(this.textRenderer, this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.backgroundHeight - 94, 0x404040, false);

        //Moved to underneath the coin slots
        String valueText = MoneyUtil.getStringOfValue(this.handler.getContext(null).getAvailableFunds());
        gui.drawText(this.textRenderer, valueText, TraderMenu.SLOT_OFFSET + 170 - this.textRenderer.getWidth(valueText), this.backgroundHeight - 94, 0x404040, false);

    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(gui, mouseX, mouseY);

        try {
            this.currentTab.renderTooltips(gui, mouseX, mouseY);
        } catch (Throwable t) { LightmansCurrency.LogError("Error rendering trader tab tooltips " + this.currentTab.getClass().getName(), t); }

        //IconAndButtonUtil.renderButtonTooltips(gui, this.textRenderer, mouseX, mouseY, this.children());

    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        this.currentTab.tick();
    }

    private void OpenStorage(ButtonWidget button) {
        if(this.handler.isSingleTrader())
            new CMessageOpenStorage(this.handler.getSingleTrader().getID()).sendToServer();
    }

    private void CollectCoins(ButtonWidget button) {
        if(this.handler.isSingleTrader())
            new CMessageCollectCoins().sendToServer();
    }

    private void OpenTerminal(ButtonWidget button) {
        if(this.showTerminalButton())
        {
            this.handler.closeMenu(this.handler.player);
            TradingTerminalScreen.open();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && this.currentTab.blockInventoryClosing()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifier);
    }

    public <T extends ClickableWidget> T addRenderableTabWidget(T widget) {
        this.tabRenderables.add(widget);
        return widget;
    }

    public <T extends Drawable> void removeRenderableTabWidget(T widget) {
        this.tabRenderables.remove(widget);
    }

    public <T extends Element> T addTabListener(T listener) {
        this.tabListeners.add(listener);
        return listener;
    }

    public <T extends Element> void removeTabListener(T listener) {
        this.tabListeners.remove(listener);
    }

    @Override
    public List<? extends Element> children()
    {
        List<? extends Element> coreListeners = super.children();
        List<Element> listeners = Lists.newArrayList();
        listeners.addAll(coreListeners);
        listeners.addAll(this.tabRenderables);
        listeners.addAll(this.tabListeners);
        return listeners;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        try {
            if(this.currentTab.mouseClicked(mouseX, mouseY, button))
                return true;
        } catch(Throwable t) {}
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        try {
            if(this.currentTab.mouseReleased(mouseX, mouseY, button))
                return true;
        } catch(Throwable t) {}
        return super.mouseReleased(mouseX, mouseY, button);
    }

}