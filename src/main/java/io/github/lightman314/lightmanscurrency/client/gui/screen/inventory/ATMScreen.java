package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.ATMMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ATMScreen extends MenuScreen<ATMMenu>{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/atm.png");

    public static final Identifier BUTTON_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/atm_buttons.png");

    int currentTabIndex = 0;
    List<ATMTab> tabs = Lists.newArrayList(new ExchangeTab(this), new SelectionTab(this), new InteractionTab(this), new NotificationTab(this), new LogTab(this), new TransferTab(this));
    public List<ATMTab> getTabs() { return this.tabs; }
    public ATMTab currentTab() { return tabs.get(this.currentTabIndex); }

    List<ClickableWidget> tabWidgets = new ArrayList<>();
    List<Element> tabListeners = new ArrayList<>();

    List<TabButton> tabButtons = new ArrayList<>();

    boolean logError = true;

    public ATMScreen(ATMMenu container, PlayerInventory inventory, Text title)
    {
        super(container, inventory, title);
        this.backgroundHeight = 243;
        this.backgroundWidth = 176;
    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY)
    {
        gui.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        try {
            this.currentTab().preRender(gui, mouseX, mouseY, partialTicks);
            this.tabWidgets.forEach(widget -> widget.render(gui, mouseX, mouseY, partialTicks));
        } catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }

    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY)
    {
        gui.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 94, 0x404040, false);
    }

    @Override
    protected void init()
    {
        super.init();

        this.tabWidgets.clear();
        this.tabListeners.clear();

        this.tabButtons = new ArrayList<>();
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            TabButton button = this.addDrawableChild(new TabButton(this::clickedOnTab, this.textRenderer, this.tabs.get(i)));
            button.reposition(this.x - TabButton.SIZE, this.y + i * TabButton.SIZE, 3);
            button.active = i != this.currentTabIndex;
            this.tabButtons.add(button);
        }

        this.currentTab().init();

    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        this.renderBackground(gui);

        //Render the tab buttons & background
        super.render(gui, mouseX, mouseY, partialTicks);

        //Render the current tab
        try {
            //this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
            //this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
            this.currentTab().postRender(gui, mouseX, mouseY);
        } catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }

        this.drawMouseoverTooltip(gui, mouseX,  mouseY);

        //Render the tab button tooltips
        for(int i = 0; i < this.tabButtons.size(); ++i)
        {
            if(this.tabButtons.get(i).isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.textRenderer, this.tabButtons.get(i).tab.getTooltip(), mouseX, mouseY);
        }

    }

    public void changeTab(int tabIndex)
    {

        //Close the old tab
        this.currentTab().onClose();
        this.tabButtons.get(this.currentTabIndex).active = true;
        this.currentTabIndex = MathUtil.clamp(tabIndex, 0, this.tabs.size() - 1);
        this.tabButtons.get(this.currentTabIndex).active = false;

        //Clear the previous tabs widgets
        this.tabWidgets.clear();
        this.tabListeners.clear();

        //Initialize the new tab
        this.currentTab().init();

        this.logError = true;
    }

    private void clickedOnTab(ButtonWidget tab)
    {
        int tabIndex = this.tabButtons.indexOf(tab);
        if(tabIndex < 0)
            return;
        this.changeTab(tabIndex);
    }

    @Override
    public void handledScreenTick() {
        super.handledScreenTick();
        this.currentTab().tick();
    }

    public <T extends ClickableWidget> T addRenderableTabWidget(T widget)
    {
        this.tabWidgets.add(widget);
        return widget;
    }

    public void removeRenderableTabWidget(ClickableWidget widget)
    {
        if(this.tabWidgets.contains(widget))
            this.tabWidgets.remove(widget);
    }

    public <T extends Element> T addTabListener(T listener)
    {
        this.tabListeners.add(listener);
        return listener;
    }

    public void removeTabListener(Element listener)
    {
        if(this.tabListeners.contains(listener))
            this.tabListeners.remove(listener);
    }

    public TextRenderer getFont() { return this.textRenderer; }

    @Override
    public List<? extends Element> children()
    {
        List<? extends Element> coreListeners = super.children();
        List<Element> listeners = Lists.newArrayList();
        listeners.addAll(coreListeners);
        listeners.addAll(this.tabWidgets);
        listeners.addAll(this.tabListeners);
        return listeners;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && this.currentTab().blockInventoryClosing()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}