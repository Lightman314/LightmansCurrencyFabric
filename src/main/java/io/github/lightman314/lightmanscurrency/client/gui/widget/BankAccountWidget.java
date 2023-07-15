package io.github.lightman314.lightmanscurrency.client.gui.widget;


import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageBankInteraction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.inventory.Inventory;
import net.minecraft.text.Text;

public class BankAccountWidget {

    public static final int MIN_WIDTH = 100;
    public static final int HEIGHT = CoinValueInput.HEIGHT + 40;
    public static final int BUTTON_WIDTH = 70;

    private final IBankAccountWidget parent;

    private CoinValueInput amountSelection;
    public CoinValueInput getAmountSelection() { return this.amountSelection; }
    ButtonWidget buttonDeposit;
    ButtonWidget buttonWithdraw;

    int y;
    int spacing;

    public boolean allowEmptyDeposits = true;

    public BankAccountWidget(int y, IBankAccountWidget parent) { this(y, parent, 0); }

    public BankAccountWidget(int y, IBankAccountWidget parent, int spacing) {
        this.parent = parent;

        this.y = y;
        this.spacing = spacing;

        int screenMiddle = this.parent.getScreen().width / 2;

        this.amountSelection = this.parent.addCustomWidget(new CoinValueInput(screenMiddle - CoinValueInput.DISPLAY_WIDTH / 2, this.y, Text.translatable("gui.lightmanscurrency.bank.amounttip"), CoinValue.EMPTY, this.parent.getFont(), value -> {}, this.parent::addCustomWidget));
        this.amountSelection.allowFreeToggle = false;
        this.amountSelection.init();

        this.buttonDeposit = this.parent.addCustomWidget(new VanillaButton(screenMiddle - 5 - BUTTON_WIDTH, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, Text.translatable("gui.button.bank.deposit"), this::OnDeposit));
        this.buttonWithdraw = this.parent.addCustomWidget(new VanillaButton(screenMiddle + 5, this.y + CoinValueInput.HEIGHT + 5 + spacing, BUTTON_WIDTH, 20, Text.translatable("gui.button.bank.withdraw"), this::OnWithdraw));
        this.buttonDeposit.active = this.buttonWithdraw.active = false;

    }

    public void renderInfo(DrawContext gui) { this.renderInfo(gui, 0); }

    public void renderInfo(DrawContext gui, int yOffset)
    {

        int screenMiddle = this.parent.getScreen().width / 2;
        TextRenderer font = this.parent.getFont();
        Text balanceComponent = this.parent.getBankAccount() == null ? Text.translatable("gui.lightmanscurrency.bank.null") : Text.translatable("gui.lightmanscurrency.bank.balance", this.parent.getBankAccount().getCoinStorage().getString("0"));
        int offset = font.getWidth(balanceComponent.getString()) / 2;
        gui.drawText(this.parent.getFont(), balanceComponent, screenMiddle - offset, this.y + CoinValueInput.HEIGHT + 30 + spacing + yOffset, 0x404040, false);

    }

    public void tick()
    {
        this.amountSelection.tick();

        if(this.parent.getBankAccount() == null)
        {
            this.buttonDeposit.active = this.buttonWithdraw.active = false;
        }
        else
        {
            this.buttonDeposit.active = MoneyUtil.getValue(this.parent.getCoinAccess()) > 0 && (this.allowEmptyDeposits || this.amountSelection.getCoinValue().getRawValue() > 0);
            this.buttonWithdraw.active = this.amountSelection.getCoinValue().getRawValue() > 0;
        }

    }

    private void OnDeposit(ButtonWidget button)
    {
        new CMessageBankInteraction(true, this.amountSelection.getCoinValue()).sendToServer();;
        this.amountSelection.setCoinValue(CoinValue.EMPTY);
    }

    private void OnWithdraw(ButtonWidget button)
    {
        new CMessageBankInteraction(false, this.amountSelection.getCoinValue()).sendToServer();;
        this.amountSelection.setCoinValue(CoinValue.EMPTY);
    }

    public interface IBankAccountWidget
    {
        public <T extends Element & Drawable & Selectable> T addCustomWidget(T widget);
        public TextRenderer getFont();
        public Screen getScreen();
        public BankAccount getBankAccount();
        public Inventory getCoinAccess();
    }

}