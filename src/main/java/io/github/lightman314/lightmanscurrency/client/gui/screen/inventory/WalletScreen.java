package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageOpenWalletBankMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageWalletExchangeCoins;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageWalletQuickCollect;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageWalletToggleAutoExchange;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class WalletScreen extends MenuScreen<WalletMenu> {

    private final int BASEHEIGHT = 114;

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/wallet.png");

    IconButton buttonToggleAutoConvert;
    ButtonWidget buttonConvert;
    boolean autoConvert = false;

    ButtonWidget buttonOpenBank;

    ButtonWidget buttonQuickCollect;

    public WalletScreen(WalletMenu container, PlayerInventory inventory, Text title)
    {
        super(container, inventory, title);
    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY)
    {

        gui.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        //Draw the top
        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, 17);
        //Draw the middle strips
        for(int y = 0; y < this.handler.getRowCount(); y++)
        {
            gui.drawTexture(GUI_TEXTURE, this.x, this.y + 17 + y * 18, 0, 17, this.backgroundWidth, 18);
        }
        //Draw the bottom
        gui.drawTexture(GUI_TEXTURE, this.x, this.y + 17 + this.handler.getRowCount() * 18, 0, 35, this.backgroundWidth, BASEHEIGHT - 17);

        //Draw the slots
        for(int y = 0; y * 9 < this.handler.getSlotCount(); y++)
        {
            for(int x = 0; x < 9 && x + y * 9 < this.handler.getSlotCount(); x++)
            {
                gui.drawTexture(GUI_TEXTURE, this.x + 7 + x * 18, this.y + 17 + y * 18, 0, BASEHEIGHT + 18, 18, 18);
            }
        }

    }

    private Text getWalletName() {
        ItemStack wallet = this.handler.getWallet();
        return wallet.isEmpty() ? Text.empty() : wallet.getName();
    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY)
    {
        gui.drawText(this.textRenderer, this.getWalletName(), 8, 6, 0x404040, false);
        gui.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 94, 0x404040, false);
    }

    @Override
    protected void init()
    {

        this.backgroundHeight = BASEHEIGHT + this.handler.getRowCount() * 18;
        this.backgroundWidth = 176;

        super.init();

        this.clearChildren();
        this.buttonConvert = null;
        this.buttonToggleAutoConvert = null;

        int buttonPosition = this.y;

        if(this.handler.canConvert())
        {
            //Create the buttons
            this.buttonConvert = this.addDrawableChild(new IconButton(this.x - 20, buttonPosition, this::PressConvertButton, IconData.of(GUI_TEXTURE, this.backgroundWidth, 0)));
            buttonPosition += 20;

            if(this.handler.canPickup())
            {
                this.buttonToggleAutoConvert = this.addDrawableChild(new IconButton(this.x - 20, buttonPosition, this::PressAutoConvertToggleButton, IconData.of(GUI_TEXTURE, this.backgroundWidth, 16)));
                buttonPosition += 20;
                this.updateToggleButton();
            }
        }

        if(this.handler.hasBankAccess())
        {
            this.buttonOpenBank = this.addDrawableChild(new IconButton(this.x - 20, buttonPosition, this::PressOpenBankButton, IconData.of(ModBlocks.MACHINE_ATM)));
        }

        this.buttonQuickCollect = this.addDrawableChild(new PlainButton(this.x + 159, this.y + this.backgroundHeight - 95, 10, 10, this::PressQuickCollectButton, GUI_TEXTURE, this.backgroundWidth + 16, 0));

    }

    @Override
    protected void handledScreenTick()
    {

        if(this.buttonToggleAutoConvert != null)
        {
            //CurrencyMod.LOGGER.info("Local AC: " + this.autoConvert + " Stack AC: " + this.container.getAutoConvert());
            if(this.handler.getAutoConvert() != this.autoConvert)
                this.updateToggleButton();
        }

    }

    private void updateToggleButton()
    {
        //CurrencyMod.LOGGER.info("Updating AutoConvert Button");
        this.autoConvert = this.handler.getAutoConvert();
        this.buttonToggleAutoConvert.setIcon(IconData.of(GUI_TEXTURE, this.backgroundWidth, this.autoConvert ? 16 : 32));
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {

        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(gui, mouseX,  mouseY);

        if(this.buttonConvert != null && this.buttonConvert.isMouseOver(mouseX, mouseY))
        {
            gui.drawTooltip(this.textRenderer, Text.translatable("tooltip.lightmanscurrency.wallet.convert"), mouseX, mouseY);
        }
        else if(this.buttonToggleAutoConvert != null && this.buttonToggleAutoConvert.isMouseOver(mouseX, mouseY))
        {
            if(this.autoConvert)
                gui.drawTooltip(this.textRenderer, Text.translatable("tooltip.lightmanscurrency.wallet.autoconvert.disable"), mouseX, mouseY);
            else
                gui.drawTooltip(this.textRenderer, Text.translatable("tooltip.lightmanscurrency.wallet.autoconvert.enable"), mouseX, mouseY);
        }
        else if(this.buttonOpenBank != null && this.buttonOpenBank.isMouseOver(mouseX, mouseY))
        {
            gui.drawTooltip(this.textRenderer, Text.translatable("tooltip.lightmanscurrency.wallet.openbank"), mouseX, mouseY);
        }
    }

    private void PressConvertButton(ButtonWidget button)
    {
        new CMessageWalletExchangeCoins().sendToServer();
    }

    private void PressAutoConvertToggleButton(ButtonWidget button)
    {
        this.handler.ToggleAutoExchange();
        new CMessageWalletToggleAutoExchange().sendToServer();
    }

    private void PressOpenBankButton(ButtonWidget button)
    {
        new CMessageOpenWalletBankMenu(this.handler.getWalletStackIndex()).sendToServer();
    }

    private void PressQuickCollectButton(ButtonWidget button)
    {
        new CMessageWalletQuickCollect().sendToServer();
    }

}