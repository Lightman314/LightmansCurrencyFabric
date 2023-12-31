package io.github.lightman314.lightmanscurrency.client.gui.screen.settings.core;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.network.server.messages.persistentdata.CMessageAddPersistentTrader;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageAddOrRemoveTrade;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MainTab extends SettingsTab{

    public static final MainTab INSTANCE = new MainTab();

    /**
     * Main Tab contains the Trader Name & Creative Settings
     */
    private MainTab() { }

    TextFieldWidget nameInput;
    ButtonWidget buttonSetName;
    ButtonWidget buttonResetName;

    PlainButton buttonToggleBankLink;

    IconButton buttonToggleCreative;
    ButtonWidget buttonAddTrade;
    ButtonWidget buttonRemoveTrade;

    ButtonWidget buttonSavePersistentTrader;
    TextFieldWidget persistentTraderIDInput;
    TextFieldWidget persistentTraderOwnerInput;

    @Override
    public boolean canOpen() { return true; }

    @Override
    public void initTab() {

        TraderSettingsScreen screen = this.getScreen();

        TraderData trader = this.getTrader();

        this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 25, 160, 20, Text.empty()));
        this.nameInput.setMaxLength(32);
        this.nameInput.setText(trader.getCustomName());

        this.buttonSetName = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 50, 74, 20, Text.translatable("gui.lightmanscurrency.changename"), this::SetName));
        this.buttonResetName = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 50, 74, 20, Text.translatable("gui.lightmanscurrency.resetname"), this::ResetName));

        //Creative Toggle
        this.buttonToggleCreative = screen.addRenderableTabWidget(IconAndButtonUtil.creativeToggleButton(screen.guiLeft() + 176, screen.guiTop() + screen.ySize - 30, this::ToggleCreative, () -> this.getTrader().isCreative()));
        this.buttonAddTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + screen.ySize - 30, 10, 10, this::AddTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 200));
        this.buttonRemoveTrade = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 166, screen.guiTop() + screen.ySize - 20, 10, 10, this::RemoveTrade, TraderSettingsScreen.GUI_TEXTURE, 0, 220));

        this.buttonToggleBankLink = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 20, screen.guiTop() + 100, 10, 10, this::ToggleBankLink, TraderSettingsScreen.GUI_TEXTURE, 10, trader.getLinkedToBank() ? 200 : 220));
        this.buttonToggleBankLink.visible = screen.hasPermission(Permissions.BANK_LINK);

        this.buttonSavePersistentTrader = screen.addRenderableTabWidget(new IconButton(screen.guiLeft() + 10, screen.guiTop() + screen.ySize - 30, this::SavePersistentTraderData, IconAndButtonUtil.ICON_PERSISTENT_DATA, IconAndButtonUtil.TOOLTIP_PERSISTENT_TRADER));
        this.buttonSavePersistentTrader.visible = CommandLCAdmin.isAdminPlayer(this.getPlayer());


        int idWidth = this.getFont().getWidth(Text.translatable("gui.lightmanscurrency.settings.persistent.id"));
        this.persistentTraderIDInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 37 + idWidth, screen.guiTop() + screen.ySize - 30, 108 - idWidth, 18, Text.empty()));

        int ownerWidth = this.getFont().getWidth(Text.translatable("gui.lightmanscurrency.settings.persistent.owner"));
        this.persistentTraderOwnerInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 12 + ownerWidth, screen.guiTop() + screen.ySize - 55, 178 - ownerWidth, 18, Text.empty()));

        this.tick();

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        TraderSettingsScreen screen = this.getScreen();
        TraderData trader = this.getScreen().getTrader();

        gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.customname"), screen.guiLeft() + 20, screen.guiTop() + 15, 0x404040, false);

        if(screen.hasPermission(Permissions.BANK_LINK))
            gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.settings.banklink"), screen.guiLeft() + 32, screen.guiTop() + 101, 0x404040, false);

        //Draw current trade count
        if(CommandLCAdmin.isAdminPlayer(this.getScreen().getPlayer()) && trader != null)
        {
            String count = String.valueOf(trader.getTradeCount());
            int width = this.getFont().getWidth(count);
            gui.drawText(this.getFont(), count, screen.guiLeft() + 164 - width, screen.guiTop() + screen.ySize - 25, 0x404040, false);

            if(this.persistentTraderIDInput != null)
            {
                //Draw ID input label
                gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.settings.persistent.id"), screen.guiLeft() + 35, screen.guiTop() + screen.ySize - 25, 0xFFFFFF, false);
                //Draw Owner input label
                gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.settings.persistent.owner"), screen.guiLeft() + 10, screen.guiTop() + screen.ySize - 50, 0xFFFFFF, false);

            }

        }

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        TraderSettingsScreen screen = this.getScreen();

        //IconAndButtonUtil.renderButtonTooltips(gui, this.getFont(), mouseX, mouseY, Lists.newArrayList(this.buttonToggleCreative, this.buttonSavePersistentTrader));

        //Render button tooltips
        if(this.buttonAddTrade.isMouseOver(mouseX, mouseY))
        {
            gui.drawTooltip(this.getFont(), Text.translatable("tooltip.lightmanscurrency.trader.creative.addTrade"), mouseX, mouseY);
        }
        else if(this.buttonRemoveTrade.isMouseOver(mouseX, mouseY))
        {
            gui.drawTooltip(this.getFont(), Text.translatable("tooltip.lightmanscurrency.trader.creative.removeTrade"), mouseX, mouseY);
        }

    }

    @Override
    public void tick() {

        boolean canChangeName = this.getScreen().hasPermission(Permissions.CHANGE_NAME);
        this.nameInput.setEditable(canChangeName);
        this.nameInput.tick();

        TraderData trader = this.getTrader();

        this.buttonSetName.active = !this.nameInput.getText().contentEquals(trader.getCustomName());
        this.buttonSetName.visible = canChangeName;
        this.buttonResetName.active = trader.hasCustomName();
        this.buttonResetName.visible = canChangeName;

        boolean isAdmin = CommandLCAdmin.isAdminPlayer(this.getPlayer());
        this.buttonToggleCreative.visible = isAdmin;
        if(this.buttonToggleCreative.visible)
        {
            this.buttonAddTrade.visible = true;
            this.buttonAddTrade.active = trader.getTradeCount() < TraderData.GLOBAL_TRADE_LIMIT;
            this.buttonRemoveTrade.visible = true;
            this.buttonRemoveTrade.active = trader.getTradeCount() > 1;
        }
        else
        {
            this.buttonAddTrade.visible = false;
            this.buttonRemoveTrade.visible = false;
        }

        boolean canLinkAccount = this.getScreen().hasPermission(Permissions.BANK_LINK);
        this.buttonToggleBankLink.visible = canLinkAccount;
        if(canLinkAccount)
        {
            this.buttonToggleBankLink.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, trader.getLinkedToBank() ? 200 : 220);
            this.buttonToggleBankLink.active = trader.canLinkBankAccount() || trader.getLinkedToBank();
        }


        if(this.buttonSavePersistentTrader != null)
        {
            this.buttonSavePersistentTrader.visible = isAdmin;
            this.buttonSavePersistentTrader.active = trader.hasValidTrade();
        }
        if(this.persistentTraderIDInput != null)
        {
            this.persistentTraderIDInput.visible = isAdmin;
            this.persistentTraderIDInput.tick();
        }
        if(this.persistentTraderOwnerInput != null)
        {
            this.persistentTraderOwnerInput.visible = isAdmin;
            this.persistentTraderOwnerInput.tick();
        }

    }

    @Override
    public void closeTab() { }

    @Override
    public int getColor() {
        return 0xFFFFFF;
    }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModItems.TRADING_CORE); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.settings.name"); }

    private void SetName(ButtonWidget button)
    {
        TraderData trader = this.getTrader();
        String customName = trader.getCustomName();
        if(!customName.contentEquals(this.nameInput.getText()))
        {
            NbtCompound message = new NbtCompound();
            message.putString("ChangeName", this.nameInput.getText());
            this.sendNetworkMessage(message);
            //LightmansCurrency.LogInfo("Sent 'Change Name' message with value:" + this.nameInput.getValue());
        }
    }

    private void ResetName(ButtonWidget button)
    {
        this.nameInput.setText("");
        this.SetName(button);
    }

    private void ToggleCreative(ButtonWidget button)
    {
        TraderData trader = this.getTrader();
        NbtCompound message = new NbtCompound();
        message.putBoolean("MakeCreative", !trader.isCreative());
        this.sendNetworkMessage(message);
    }

    private void ToggleBankLink(ButtonWidget button)
    {
        TraderData trader = this.getTrader();
        NbtCompound message = new NbtCompound();
        message.putBoolean("LinkToBankAccount", !trader.getLinkedToBank());
        this.sendNetworkMessage(message);
    }

    private void AddTrade(ButtonWidget button)
    {
        new CMessageAddOrRemoveTrade(this.getTrader().getID(), true).sendToServer();
    }

    private void RemoveTrade(ButtonWidget button)
    {
        new CMessageAddOrRemoveTrade(this.getTrader().getID(), false).sendToServer();
    }

    private void SavePersistentTraderData(ButtonWidget button)
    {
        TraderData trader = this.getScreen().getTrader();
        if(trader != null && trader.canMakePersistent())
            new CMessageAddPersistentTrader(trader.getID(), this.persistentTraderIDInput.getText(), this.persistentTraderOwnerInput.getText()).sendToServer();
    }

}