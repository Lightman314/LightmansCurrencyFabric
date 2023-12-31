package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.notifications.NotificationDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.IScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menu.TraderStorageMenu;
import io.github.lightman314.lightmanscurrency.common.menu.slots.CoinSlot;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageOpenTrades;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageStoreCoins;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;

public class TraderStorageScreen extends MenuScreen<TraderStorageMenu> implements TraderStorageMenu.IClientMessage, IScreen {

    Map<Integer, TraderStorageClientTab<?>> availableTabs = new HashMap<>();
    public TraderStorageClientTab<?> currentTab() { return this.availableTabs.get(this.handler.getCurrentTabIndex()); }

    Map<Integer, TabButton> tabButtons = new HashMap<>();

    ButtonWidget buttonShowTrades;
    ButtonWidget buttonCollectMoney;

    ButtonWidget buttonOpenSettings;

    ButtonWidget buttonStoreMoney;

    ButtonWidget buttonShowLog;

    NotificationDisplayWidget logWindow;

    ButtonWidget buttonTradeRules;

    List<ClickableWidget> tabRenderables = new ArrayList<>();
    List<Element> tabListeners = new ArrayList<>();

    private final List<Runnable> tickListeners = new ArrayList<>();

    public TraderStorageScreen(TraderStorageMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
        this.handler.getAllTabs().forEach((key,tab) -> this.availableTabs.put(key, tab.createClientTab(this)));
        this.backgroundWidth = TraderScreen.WIDTH;
        this.backgroundHeight = TraderScreen.HEIGHT;
        menu.addMessageListener(this::serverMessage);
    }

    @Override
    public void init() {

        super.init();

        this.tabRenderables.clear();
        this.tabListeners.clear();

        //Create the tab buttons
        this.tabButtons.clear();
        this.availableTabs.forEach((key,tab) ->{
            if(tab.tabButtonVisible()) {
                TabButton newButton = this.addDrawableChild(new TabButton(button -> this.changeTab(key), this.textRenderer, tab));
                if(key == this.handler.getCurrentTabIndex())
                    newButton.active = false;
                this.tabButtons.put(key, newButton);
            }
        });
        //Position the tab buttons
        int xPos = this.x - TabButton.SIZE;
        AtomicInteger index = new AtomicInteger(0);
        this.tabButtons.forEach((key,button) -> {
            int yPos = this.y + TabButton.SIZE * index.get();
            button.reposition(xPos, yPos, 3);
            index.set(index.get() + 1);
        });

        //Other buttons
        this.buttonShowTrades = this.addDrawableChild(IconAndButtonUtil.traderButton(this.x + TraderStorageMenu.SLOT_OFFSET - 20, this.y + 118, this::PressTradesButton));

        this.buttonCollectMoney = this.addDrawableChild(IconAndButtonUtil.collectCoinButton(this.x + TraderStorageMenu.SLOT_OFFSET - 20, this.y + 138, this::PressCollectionButton, this.handler.player, this.handler::getTrader));
        this.buttonCollectMoney.visible = this.handler.hasPermission(Permissions.COLLECT_COINS) && !this.handler.getTrader().hasBankAccount();

        this.buttonStoreMoney = this.addDrawableChild(IconAndButtonUtil.storeCoinButton(this.x + TraderStorageMenu.SLOT_OFFSET + 176, this.y + 158, this::PressStoreCoinsButton));
        this.buttonStoreMoney.visible = false;

        this.buttonOpenSettings = this.addDrawableChild(IconAndButtonUtil.openSettingsButton(this.x + TraderStorageMenu.SLOT_OFFSET + 176, this.y + 118, this::PressSettingsButton));
        this.buttonOpenSettings.visible = this.handler.hasPermission(Permissions.EDIT_SETTINGS);

        this.buttonTradeRules = this.addDrawableChild(IconAndButtonUtil.tradeRuleButton(this.x + TraderStorageMenu.SLOT_OFFSET + 176, this.y + 138, this::PressTradeRulesButton, () -> this.currentTab().getTradeRuleTradeIndex() >= 0));
        this.buttonTradeRules.visible = this.handler.hasPermission(Permissions.EDIT_TRADE_RULES);

        this.buttonShowLog = this.addDrawableChild(IconAndButtonUtil.showLoggerButton(this.x + TraderStorageMenu.SLOT_OFFSET - 20, this.y + 158, this::PressLogButton, () -> false));

        this.logWindow = this.addDrawableChild(new NotificationDisplayWidget(this.x + TraderStorageMenu.SLOT_OFFSET, this.y, this.backgroundWidth - (2 * TraderStorageMenu.SLOT_OFFSET), this.backgroundHeight / NotificationDisplayWidget.HEIGHT_PER_ROW, this.textRenderer, () -> { TraderData trader = this.handler.getTrader(); return trader != null ? trader.getNotifications() : new ArrayList<>(); }));
        this.logWindow.visible = false;

        //Left side auto-position
        LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, TraderMenu.SLOT_OFFSET - 20, 118, 20, this.buttonShowTrades, this.buttonCollectMoney, this.buttonShowLog);
        //Right side auto-position
        LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_TOPDOWN, TraderStorageMenu.SLOT_OFFSET + 176, 118, 20, this.buttonStoreMoney, this.buttonOpenSettings, this.buttonTradeRules);

        //Initialize the current tab
        this.currentTab().onOpen();

        this.handledScreenTick();

    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY) {

        gui.setShaderColor(1f, 1f, 1f, 1f);

        //Main BG
        gui.drawTexture(TraderScreen.GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        //Coin Slots
        for(CoinSlot slot : this.handler.getCoinSlots())
        {
            if(slot.isEnabled())
                gui.drawTexture(TraderScreen.GUI_TEXTURE, this.x + slot.x - 1, this.y + slot.y - 1, this.backgroundWidth, 0, 18, 18);
        }

        //Current tab
        try {
            this.currentTab().renderBG(gui, mouseX, mouseY, partialTicks);
            this.tabRenderables.forEach(widget -> widget.render(gui, mouseX, mouseY, partialTicks));
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), e); }


    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY) {

        gui.drawText(this.textRenderer, this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.backgroundHeight - 94, 0x404040, false);

    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        if(this.handler.getTrader() == null)
        {
            this.handler.closeMenu(this.handler.player);
            return;
        }

        this.renderBackground(gui);
        if(this.logWindow != null && this.logWindow.visible)
        {
            this.logWindow.render(gui, mouseX, mouseY, partialTicks);
            this.buttonShowLog.render(gui, mouseX, mouseY, partialTicks);
            //IconAndButtonUtil.renderButtonTooltips(gui, this.textRenderer, mouseX, mouseY, Lists.newArrayList(this.buttonShowLog));
            this.logWindow.tryRenderTooltip(gui, mouseX, mouseY);
            return;
        }
        super.render(gui, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(gui, mouseX, mouseY);

        try {
            this.currentTab().renderTooltips(gui, mouseX, mouseY);
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab tooltips " + this.currentTab().getClass().getName(), e); }

        //IconAndButtonUtil.renderButtonTooltips(gui, this.textRenderer, mouseX, mouseY, this.children());

        this.tabButtons.forEach((key, button) ->{
            if(button.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.textRenderer, button.tab.getTooltip(), mouseX, mouseY);
        });

    }

    @Override
    protected void handledScreenTick()
    {
        if(this.handler.getTrader() == null)
        {
            this.handler.closeMenu(this.handler.player);
            return;
        }

        this.handler.validateCoinSlots();

        if(!this.handler.hasPermission(Permissions.OPEN_STORAGE))
        {
            this.handler.closeMenu(this.handler.player);
            new CMessageOpenTrades(this.handler.getTrader().getID()).sendToServer();
            return;
        }

        this.buttonOpenSettings.visible = this.handler.hasPermission(Permissions.EDIT_SETTINGS);
        this.buttonTradeRules.visible = this.handler.hasPermission(Permissions.EDIT_TRADE_RULES);

        this.buttonStoreMoney.visible = this.handler.HasCoinsToAdd() && this.handler.hasPermission(Permissions.STORE_COINS);
        this.buttonShowLog.visible = this.handler.hasPermission(Permissions.VIEW_LOGS);

        this.currentTab().tick();

        for(Runnable r : this.tickListeners) r.run();

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.currentTab().blockInventoryClosing() && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) { return true; }
        return super.keyPressed(keyCode, scanCode, modifier);
    }

    private TabButton getTabButton(int key) {
        if(this.tabButtons.containsKey(key))
            return this.tabButtons.get(key);
        return null;
    }

    public void changeTab(int newTab) { this.changeTab(newTab, true, null); }

    public void changeTab(int newTab, boolean sendMessage, NbtCompound selfMessage) {

        if(newTab == this.handler.getCurrentTabIndex())
            return;

        //Close the old tab
        int oldTab = this.handler.getCurrentTabIndex();
        this.currentTab().onClose();

        //Make the old tabs button active again
        TabButton button = this.getTabButton(this.handler.getCurrentTabIndex());
        if(button != null)
            button.active = true;

        //Clear the renderables & listeners
        this.tabRenderables.clear();
        this.tabListeners.clear();

        //Change the tab officially
        this.handler.changeTab(newTab);

        //Make the tab button for the current tab inactive
        button = this.getTabButton(this.handler.getCurrentTabIndex());
        if(button != null)
            button.active = false;

        //Open the new tab
        if(selfMessage != null)
            this.currentTab().receiveSelfMessage(selfMessage);
        this.currentTab().onOpen();

        //Inform the server that the tab has been changed
        if(oldTab != this.handler.getCurrentTabIndex() && sendMessage)
            this.handler.sendMessage(this.handler.createTabChangeMessage(newTab, selfMessage));

    }

    @Override
    public void selfMessage(NbtCompound message) {
        //LightmansCurrency.LogInfo("Received self-message:\n" + message.getAsString());
        if(message.contains("ChangeTab", NbtElement.INT_TYPE))
            this.changeTab(message.getInt("ChangeTab"), false, message);
        else
            this.currentTab().receiveSelfMessage(message);
    }

    public void serverMessage(NbtCompound message) {
        this.currentTab().receiveServerMessage(message);
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
            if(this.currentTab().mouseClicked(mouseX, mouseY, button))
                return true;
        } catch(Throwable t) {}
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        try {
            if(this.currentTab().mouseReleased(mouseX, mouseY, button))
                return true;
        } catch(Throwable t) {}
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void PressTradesButton(ButtonWidget button)
    {
        new CMessageOpenTrades(this.handler.getTrader().getID()).sendToServer();
    }

    private void PressCollectionButton(ButtonWidget button)
    {
        //Open the container screen
        if(this.handler.hasPermission(Permissions.COLLECT_COINS))
        {
            //CurrencyMod.LOGGER.info("Owner attempted to collect the stored money.");
            new CMessageCollectCoins().sendToServer();
        }
        else
            Permissions.PermissionWarning(this.handler.player, "collect stored coins", Permissions.COLLECT_COINS);
    }

    private void PressStoreCoinsButton(ButtonWidget button)
    {
        if(this.handler.hasPermission(Permissions.STORE_COINS))
        {
            new CMessageStoreCoins().sendToServer();
        }
        else
            Permissions.PermissionWarning(this.handler.player, "store coins", Permissions.STORE_COINS);
    }

    private void PressLogButton(ButtonWidget button)
    {
        this.logWindow.visible = !this.logWindow.visible;
    }

    private void PressTradeRulesButton(ButtonWidget button)
    {
        this.handler.closeMenu(this.handler.player);
        MinecraftClient.getInstance().setScreen(new TradeRuleScreen(this.handler.getTrader().getID(), this.currentTab().getTradeRuleTradeIndex()));
    }

    private void PressSettingsButton(ButtonWidget button)
    {
        this.handler.closeMenu(this.handler.player);
        MinecraftClient.getInstance().setScreen(new TraderSettingsScreen(this.handler.traderSource));
    }

    @Override
    public void addTickListener(Runnable r) {
        this.tickListeners.add(r);
    }

}