package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageOpenStorage;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageOpenTrades;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TraderSettingsScreen extends Screen {

    public static final Identifier GUI_TEXTURE =  new Identifier(LightmansCurrency.MODID, "textures/gui/tradersettings.png");

    public PlayerEntity getPlayer() { return this.client.player; }
    private final Supplier<TraderData> trader;
    public TraderData getTrader() { return this.trader.get(); }

    public TextRenderer getFont() { return this.textRenderer; }

    public final int guiLeft() { return (this.width - this.xSize) / 2; }
    public final int guiTop() { return (this.height - this.ySize) / 2; }
    public final int xSize = 200;
    public final int ySize = 200;

    List<ClickableWidget> tabWidgets = Lists.newArrayList();
    List<Element> tabListeners = Lists.newArrayList();

    List<TabButton> tabButtons = Lists.newArrayList();

    List<SettingsTab> tabs;
    int currentTabIndex = 0;
    public SettingsTab currentTab()
    {
        return this.tabs.get(MathUtil.clamp(currentTabIndex, 0, this.tabs.size() - 1));
    }

    public TraderSettingsScreen(Supplier<TraderData> trader)
    {
        super(Text.empty());

        this.trader = trader;

        //Collect the Settings Tabs
        this.tabs = this.trader.get().getSettingsTabs();

        this.tabs.forEach(tab -> tab.setScreen(this));

    }

    @Override
    public void init()
    {
        //Initialize the back button
        this.addDrawableChild(new IconButton(this.guiLeft(), this.guiTop() - 20, this::OpenStorage, IconAndButtonUtil.ICON_BACK));
        //Initialize the tab buttons
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            TabButton button = this.addDrawableChild(new TabButton(this::clickedOnTab, this.textRenderer, this.tabs.get(i)));
            button.active = i != this.currentTabIndex;
            button.visible = this.tabs.get(i).canOpen();
            this.tabButtons.add(button);
        }
        this.positionTabButtons();

        //Initialize the starting tab
        try {
            this.currentTab().initTab();
        } catch(Throwable t) { LightmansCurrency.LogError("Error in Settings Tab init.", t); }


    }

    private int getTabPosX(int index)
    {
        if(index < 7)
            return this.guiLeft() + 20 + 25 * index;
        if(index < 15)
            return this.guiLeft() + this.xSize;
        if(index < 23)
            return this.guiLeft() + this.xSize - 25 * (index - 15);
        return this.guiLeft() - 25;
    }

    private int getTabPosY(int index)
    {
        if(index < 7)
            return this.guiTop() - 25;
        if(index < 15)
            return this.guiTop() + 25 * (index - 10);
        if(index < 23)
            return this.guiTop() + this.ySize;
        return this.guiTop() + this.ySize - 25 * (index - 23);
    }

    private int getTabRotation(int index)
    {
        if(index < 7)
            return 0;
        if(index < 15)
            return 1;
        if(index < 23)
            return 2;
        return 3;
    }

    private void positionTabButtons()
    {
        int index = 0;
        for (TabButton thisButton : this.tabButtons) {
            if (thisButton.visible) {
                thisButton.reposition(this.getTabPosX(index), this.getTabPosY(index), this.getTabRotation(index));
                index++;
            }
        }
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {

        this.renderBackground(gui);
        //Render the background
        this.setColor(gui, this.currentTab().getColor());
        gui.drawTexture(GUI_TEXTURE, this.guiLeft(), this.guiTop(), 0, 0, this.xSize, this.ySize);
        //Render the tab buttons
        super.render(gui, mouseX, mouseY, partialTicks);
        //Pre-render the tab
        try {
            this.currentTab().preRender(gui, mouseX, mouseY, partialTicks);
        } catch(Throwable t) { LightmansCurrency.LogError("Error in Settings Tab pre-render.", t); }
        //Render the renderables
        this.tabWidgets.forEach(widget -> widget.render(gui, mouseX, mouseY, partialTicks));
        //Post-render the tab
        try {
            this.currentTab().postRender(gui, mouseX, mouseY, partialTicks);
        } catch(Throwable t) { LightmansCurrency.LogError("Error in Settings Tab post-render.", t); }

        //Render the tab button tooltips
        for (TabButton tabButton : this.tabButtons) {
            if (tabButton.isMouseOver(mouseX, mouseY))
                gui.drawTooltip(this.textRenderer, tabButton.tab.getTooltip(), mouseX, mouseY);
        }

    }

    public void setColor(DrawContext gui, int color)
    {
        float r = (float)(color >> 16 & 255) / 255f;
        float g = (float)(color >> 8 & 255) / 255f;
        float b = (float)(color & 255) / 255f;

        gui.setShaderColor(r, g, b, 1f);
    }

    @Override
    public void tick()
    {
        if(this.client == null)
            return;
        if(this.getTrader() == null)
        {
            this.client.setScreen(null);
            return;
        }
        if(!this.hasPermission(Permissions.EDIT_SETTINGS))
        {
            this.client.setScreen(null);
            if(this.hasPermission(Permissions.OPEN_STORAGE))
                new CMessageOpenStorage(this.getTrader().getID()).sendToServer();
            else
                new CMessageOpenTrades(this.getTrader().getID()).sendToServer();
            return;
        }
        //Update the tabs visibility
        boolean updateTabs = false;
        for(int i = 0; i < this.tabs.size(); ++i)
        {
            boolean visible = this.tabs.get(i).canOpen();
            if(visible != this.tabButtons.get(i).visible)
            {
                updateTabs = true;
                this.tabButtons.get(i).visible = visible;
            }
        }
        if(updateTabs)
            this.positionTabButtons();

        if(!this.currentTab().canOpen())
        {
            this.clickedOnTab(this.tabButtons.get(0));
        }

        //Tick the current tab
        try {
            this.currentTab().tick();
        } catch(Throwable t) { LightmansCurrency.LogError("Error in Settings Tab tick.", t); }

    }

    public boolean hasPermission(String permission)
    {
        if(this.trader.get() != null)
            return this.trader.get().hasPermission(this.getPlayer(), permission);
        return false;
    }

    public int getPermissionLevel(String permission)
    {
        if(this.trader.get() != null)
            return this.trader.get().getPermissionLevel(this.getPlayer(), permission);
        return 0;
    }

    public boolean hasPermissions(List<String> permissions)
    {
        for (String permission : permissions) {
            if (!this.hasPermission(permission))
                return false;
        }
        return true;
    }

    public <T extends ClickableWidget> T addRenderableTabWidget(T widget)
    {
        this.tabWidgets.add(widget);
        return widget;
    }

    public void removeRenderableTabWidget(ClickableWidget widget)
    {
        this.tabWidgets.remove(widget);
    }

    public <T extends Element> T addTabListener(T listener)
    {
        this.tabListeners.add(listener);
        return listener;
    }

    public void removeTabListener(Element listener)
    {
        this.tabListeners.remove(listener);
    }

    private void clickedOnTab(ButtonWidget tab)
    {
        int tabIndex = this.tabButtons.indexOf(tab);
        if(tabIndex < 0)
            return;
        if(tabIndex != this.currentTabIndex)
        {
            //Close the old tab
            try {
                this.currentTab().closeTab();
            } catch(Throwable t) { LightmansCurrency.LogError("Error in Settings Tab close.", t); }

            this.tabButtons.get(this.currentTabIndex).active = true;
            this.currentTabIndex = tabIndex;
            this.tabButtons.get(this.currentTabIndex).active = false;

            //Clear the previous tabs widgets
            this.tabWidgets.clear();
            this.tabListeners.clear();

            //Initialize the new tab
            try {
                this.currentTab().initTab();
            } catch(Throwable t) { LightmansCurrency.LogError("Error in Settings Tab init.", t); }
        }
    }

    private void OpenStorage(ButtonWidget button)
    {
        new CMessageOpenStorage(this.getTrader().getID()).sendToServer();
    }

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
    public boolean shouldPause() { return false; }

}