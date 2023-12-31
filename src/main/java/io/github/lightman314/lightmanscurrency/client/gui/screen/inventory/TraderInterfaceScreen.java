package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menu.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menu.traderinterface.TraderInterfaceTab;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TraderInterfaceScreen extends MenuScreen<TraderInterfaceMenu> implements TraderInterfaceMenu.IClientMessage {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/trader_interface.png");

    public static final int WIDTH = 206;
    public static final int HEIGHT = 236;

    Map<Integer, TraderInterfaceClientTab<?>> availableTabs = new HashMap<>();
    public TraderInterfaceClientTab<?> currentTab() { return this.availableTabs.get(this.handler.getCurrentTabIndex()); }

    Map<Integer, TabButton> tabButtons = new HashMap<>();

    List<ClickableWidget> tabRenderables = new ArrayList<>();
    List<Element> tabListeners = new ArrayList<>();

    IconButton modeToggle;

    IconButton onlineModeToggle;

    public TraderInterfaceScreen(TraderInterfaceMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
        this.handler.getAllTabs().forEach((key,tab) -> this.availableTabs.put(key, tab.createClientTab(this)));
        this.backgroundWidth = WIDTH;
        this.backgroundHeight = HEIGHT;
    }

    @Override
    public void init() {

        super.init();

        this.tabRenderables.clear();
        this.tabListeners.clear();

        //Create the tab buttons
        this.tabButtons.clear();
        this.availableTabs.forEach((key,tab) ->{
            TabButton newButton = this.addDrawableChild(new TabButton(button -> this.changeTab(key), this.textRenderer, tab));
            if(key == this.handler.getCurrentTabIndex())
                newButton.active = false;
            this.tabButtons.put(key, newButton);
        });

        this.modeToggle = this.addDrawableChild(new IconButton(this.x + this.backgroundWidth, this.y, this::ToggleMode, () -> IconAndButtonUtil.GetIcon(this.handler.getTraderInterface().getMode()), new IconAndButtonUtil.SuppliedTooltip(() -> this.getMode().getDisplayText())));

        this.onlineModeToggle = this.addDrawableChild(new IconButton(this.x + this.backgroundWidth, this.y + 20, this::ToggleOnlineMode, () -> this.handler.getTraderInterface().isOnlineMode() ? IconAndButtonUtil.ICON_ONLINEMODE_TRUE : IconAndButtonUtil.ICON_ONLINEMODE_FALSE, new IconAndButtonUtil.SuppliedTooltip(() -> Text.translatable("gui.lightmanscurrency.interface.onlinemode." + this.handler.getTraderInterface().isOnlineMode()))));

        //Initialize the current tab
        this.currentTab().onOpen();

        this.handledScreenTick();

    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY) {

        gui.setShaderColor(1f, 1f, 1f, 1f);

        //Main BG
        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        //Current tab
        try {
            this.currentTab().renderBG(gui, mouseX, mouseY, partialTicks);
            this.tabRenderables.forEach(widget -> widget.render(gui, mouseX, mouseY, partialTicks));
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), e); }


    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY) {

        gui.drawText(this.textRenderer, this.playerInventoryTitle, TraderInterfaceMenu.SLOT_OFFSET + 8, this.backgroundHeight - 94, 0x404040, false);

    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(gui, mouseX, mouseY);

        try {
            this.currentTab().renderTooltips(gui, mouseX, mouseY);
        } catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab tooltips " + this.currentTab().getClass().getName(), e); }

        //IconAndButtonUtil.renderButtonTooltips(gui, this.textRenderer, mouseX, mouseY, this.children());

        this.tabButtons.forEach((key, button) -> {
            if(button.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.textRenderer, button.tab.getTooltip(), mouseX, mouseY);
        });

    }

    @Override
    protected void handledScreenTick()
    {

        if(!this.currentTab().commonTab.canOpen(this.handler.player))
            this.changeTab(TraderInterfaceTab.TAB_INFO);

        this.updateTabs();

        this.currentTab().tick();

    }

    private TraderInterfaceBlockEntity.ActiveMode getMode() {
        if(this.handler.getTraderInterface() != null)
            return this.handler.getTraderInterface().getMode();
        return TraderInterfaceBlockEntity.ActiveMode.DISABLED;
    }

    private void ToggleMode(ButtonWidget button) { this.handler.changeMode(this.getMode().getNext()); }

    private void ToggleOnlineMode(ButtonWidget button) { this.handler.setOnlineMode(!this.handler.getTraderInterface().isOnlineMode()); }

    private void updateTabs() {
        //Position the tab buttons
        int yPos = this.y - TabButton.SIZE;
        AtomicInteger index = new AtomicInteger(0);
        this.tabButtons.forEach((key,button) -> {
            TraderInterfaceClientTab<?> tab = this.availableTabs.get(key);
            button.visible = tab.tabButtonVisible();
            if(button.visible)
            {
                int xPos = this.x + TabButton.SIZE * index.get();
                button.reposition(xPos, yPos, 0);
                index.set(index.get() + 1);
            }
        });
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods) {
        if(this.currentTab().keyPressed(key, scanCode, mods))
            return true;
        if(this.client == null)
            return super.keyPressed(key, scanCode, mods);
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.client.options.inventoryKey.matchesKey(key, scanCode) && this.currentTab().blockInventoryClosing()) {
            return true;
        }
        return super.keyPressed(key, scanCode, mods);
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
        this.handler.changeTab(newTab, new NbtCompound());

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
            this.handler.sendMessage(this.handler.createTabChangeMessage(newTab, null));

    }

    @Override
    public void selfMessage(NbtCompound message) {
        //LightmansCurrency.LogInfo("Received self-message:\n" + message.getAsString());
        if(message.contains("ChangeTab", NbtElement.INT_TYPE))
            this.changeTab(message.getInt("ChangeTab"), false, message);
        else
            this.currentTab().receiveSelfMessage(message);
    }

    public <T extends ClickableWidget> T addRenderableTabWidget(T widget) {
        this.tabRenderables.add(widget);
        return widget;
    }

    public <T extends ClickableWidget> void removeRenderableTabWidget(T widget) {
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
        if(this.currentTab().mouseClicked(mouseX, mouseY, button))
            return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if(this.currentTab().mouseReleased(mouseX, mouseY, button))
            return true;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char c, int code) {
        if(this.currentTab().charTyped(c, code))
            return true;
        return super.charTyped(c, code);
    }

}