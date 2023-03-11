package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.walletbank.*;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletBankMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageOpenWalletMenu;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WalletBankScreen extends MenuScreen<WalletBankMenu>{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/wallet_bank.png");

    int currentTabIndex = 0;
    List<WalletBankTab> tabs = Lists.newArrayList(new InteractionTab(this), new SelectionTab(this));
    public List<WalletBankTab> getTabs() { return this.tabs; }
    public WalletBankTab currentTab() { return tabs.get(this.currentTabIndex); }

    List<ClickableWidget> tabWidgets = new ArrayList<>();
    List<Element> tabListeners = new ArrayList<>();

    List<TabButton> tabButtons = new ArrayList<>();

    boolean logError = true;

    ButtonWidget buttonOpenWallet;

    public WalletBankScreen(WalletBankMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init()
    {

        this.backgroundHeight = WalletBankMenu.BANK_WIDGET_SPACING + this.handler.getRowCount() * 18 + 7;
        this.backgroundWidth = 176;

        super.init();

        this.clearChildren();

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

        this.buttonOpenWallet = this.addDrawableChild(new IconButton(this.x, this.y - 20, this::PressOpenWalletButton, IconData.of(this.handler.getWallet())));

        this.currentTab().init();

    }

    @Override
    protected void drawBackground(MatrixStack pose, float partialTicks, int mouseX, int mouseY) {

        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        //Draw the top
        this.drawTexture(pose, this.x, this.y, 0, 0, this.backgroundWidth, WalletBankMenu.BANK_WIDGET_SPACING);
        //Draw the middle strips
        for(int y = 0; y < this.handler.getRowCount(); y++)
            this.drawTexture(pose, this.x, this.y + WalletBankMenu.BANK_WIDGET_SPACING + y * 18, 0, WalletBankMenu.BANK_WIDGET_SPACING, this.backgroundWidth, 18);

        //Draw the bottom
        this.drawTexture(pose, this.x, this.y + WalletBankMenu.BANK_WIDGET_SPACING + this.handler.getRowCount() * 18, 0, WalletBankMenu.BANK_WIDGET_SPACING + 18, this.backgroundWidth, 7);

        //Draw the slots
        for(int y = 0; y * 9 < this.handler.getSlotCount(); y++)
        {
            for(int x = 0; x < 9 && x + y * 9 < this.handler.getSlotCount(); x++)
            {
                this.drawTexture(pose, this.x + 7 + x * 18, this.y + WalletBankMenu.BANK_WIDGET_SPACING + y * 18, 0, WalletBankMenu.BANK_WIDGET_SPACING + 18 + 7, 18, 18);
            }
        }

        //Render Current Tab
        try {
            this.currentTab().preRender(pose, mouseX, mouseY, partialTicks);
            this.tabWidgets.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
        } catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }

    }

    private Text getWalletName() {
        ItemStack wallet = this.handler.getWallet();
        return wallet.isEmpty() ? EasyText.empty() : wallet.getName();
    }

    @Override
    protected void drawForeground(MatrixStack pose, int mouseX, int mouseY) {

        this.textRenderer.draw(pose, this.getWalletName(), 8.0f, WalletBankMenu.BANK_WIDGET_SPACING - 11, 0x404040);

    }


    @Override
    public void render(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTicks);

        //Render the current tab
        try {
            this.currentTab().postRender(pose, mouseX, mouseY);
        } catch(Exception e) { if(logError) { LightmansCurrency.LogError("Error rendering " + this.currentTab().getClass().getName() + " tab.", e); logError = false; } }

        this.drawMouseoverTooltip(pose, mouseX,  mouseY);

        if(this.buttonOpenWallet != null && this.buttonOpenWallet.isMouseOver(mouseX, mouseY))
            this.renderTooltip(pose, EasyText.translatable("tooltip.lightmanscurrency.wallet.openwallet"), mouseX, mouseY);

        //Render the tab button tooltips
        for (TabButton tabButton : this.tabButtons) {
            if (tabButton.isMouseOver(mouseX, mouseY))
                this.renderTooltip(pose, tabButton.tab.getTooltip(), mouseX, mouseY);
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
    public void handledScreenTick()
    {
        this.currentTab().tick();
    }

    public <T extends ClickableWidget> T addRenderableTabWidget(T widget)
    {
        this.tabWidgets.add(widget);
        return widget;
    }

    public void removeRenderableTabWidget(Drawable widget)
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

    public TextRenderer getFont() {
        return this.textRenderer;
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
    public boolean keyPressed(int keyCode, int scanCode, int modifier) {
        //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
        if (this.client.options.inventoryKey.matchesKey(keyCode, scanCode) && this.currentTab().blockInventoryClosing()) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifier);
    }

    private void PressOpenWalletButton(ButtonWidget button) {
        new CMessageOpenWalletMenu(this.handler.getWalletStackIndex()).sendToServer();
    }


}