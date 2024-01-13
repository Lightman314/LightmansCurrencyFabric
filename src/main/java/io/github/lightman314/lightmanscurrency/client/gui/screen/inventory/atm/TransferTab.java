package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TeamSelectWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TeamButton.Size;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.bank.BankAccount;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.common.teams.TeamSaveData;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageBankTransferPlayer;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageBankTransferTeam;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TransferTab extends ATMTab {

    public TransferTab(ATMScreen screen) { super(screen); }

    //Response should be 100 ticks or 5 seconds
    public static final int RESPONSE_DURATION = 100;

    private int responseTimer = 0;

    CoinValueInput amountWidget;

    TextFieldWidget playerInput;
    TeamSelectWidget teamSelection;

    IconButton buttonToggleMode;
    ButtonWidget buttonTransfer;

    long selectedTeam = -1;

    boolean playerMode = true;

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_STORE_COINS; }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.transfer"); }

    @Override
    public void init() {

        SimpleSlot.SetInactive(this.screen.getScreenHandler());

        this.responseTimer = 0;
        this.screen.getScreenHandler().clearMessage();

        this.amountWidget = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft(), this.screen.getGuiTop(), Text.translatable("gui.lightmanscurrency.bank.transfertip"), CoinValue.EMPTY, this.screen.getFont(), value -> {}, this.screen::addRenderableTabWidget));
        this.amountWidget.init();
        this.amountWidget.allowFreeToggle = false;
        this.amountWidget.drawBG = false;

        this.buttonToggleMode = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getImageWidth() - 30, this.screen.getGuiTop() + 64, this::ToggleMode, this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()), new IconAndButtonUtil.ToggleTooltip(() -> this.playerMode, Text.translatable("tooltip.lightmanscurrency.atm.transfer.mode.team"), Text.translatable("tooltip.lightmanscurrency.atm.transfer.mode.player"))));

        this.playerInput = this.screen.addRenderableTabWidget(new TextFieldWidget(this.screen.getFont(), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 104, this.screen.getImageWidth() - 20, 20, Text.empty()));
        this.playerInput.visible = this.playerMode;

        this.teamSelection = this.screen.addRenderableTabWidget(new TeamSelectWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 84, 2, Size.NORMAL, this::getTeamList, this::selectedTeam, this::SelectTeam));
        this.teamSelection.init(this.screen::addRenderableTabWidget, this.screen.getFont());
        this.teamSelection.visible = !this.playerMode;

        this.buttonTransfer = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 126, this.screen.getImageWidth() - 20, 20, Text.translatable(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"), this::PressTransfer));
        this.buttonTransfer.active = false;

    }

    private List<Team> getTeamList()
    {
        List<Team> results = Lists.newArrayList();
        BankAccount.AccountReference source = this.screen.getScreenHandler().getBankAccountReference();
        Team blockTeam = null;
        if(source != null && source.accountType == BankAccount.AccountType.Team)
            blockTeam = TeamSaveData.GetTeam(true, source.teamID);
        for(Team team : TeamSaveData.GetAllTeams(true))
        {
            if(team.hasBankAccount() && team != blockTeam)
                results.add(team);
        }
        return results;
    }

    public Team selectedTeam()
    {
        if(this.selectedTeam >= 0)
            return TeamSaveData.GetTeam(true, this.selectedTeam);
        return null;
    }

    public void SelectTeam(int teamIndex)
    {
        try {
            Team team = this.getTeamList().get(teamIndex);
            if(team.getID() == this.selectedTeam)
                return;
            this.selectedTeam = team.getID();
        } catch(Exception ignored) { }
    }

    private void PressTransfer(ButtonWidget button)
    {
        if(this.playerMode)
        {
            new CMessageBankTransferPlayer(this.playerInput.getText(), this.amountWidget.getCoinValue()).sendToServer();
            this.playerInput.setText("");
            this.amountWidget.setCoinValue(CoinValue.EMPTY);
        }
        else if(this.selectedTeam >= 0)
        {
            new CMessageBankTransferTeam(this.selectedTeam, this.amountWidget.getCoinValue()).sendToServer();
            this.amountWidget.setCoinValue(CoinValue.EMPTY);
        }
    }

    private void ToggleMode(ButtonWidget button) {
        this.playerMode = !this.playerMode;
        this.buttonTransfer.setMessage(Text.translatable(this.playerMode ? "gui.button.bank.transfer.player" : "gui.button.bank.transfer.team"));
        this.teamSelection.visible = !this.playerMode;
        this.playerInput.visible = this.playerMode;
        this.buttonToggleMode.setIcon(this.playerMode ? IconData.of(Items.PLAYER_HEAD) : IconData.of(ItemRenderUtil.getAlexHead()));
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.hideCoinSlots(gui);

        //this.screen.getFont().draw(pose, this.getTooltip(), this.screen.getGuiLeft() + 8f, this.screen.getGuiTop() + 6f, 0x404040);
        Text balance = this.screen.getScreenHandler().getBankAccount() == null ? Text.translatable("gui.lightmanscurrency.bank.null") : Text.translatable("gui.lightmanscurrency.bank.balance", this.screen.getScreenHandler().getBankAccount().getCoinStorage().getString("0"));
        gui.drawText(this.screen.getFont(), balance, this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 72, 0x404040, false);

        if(this.hasMessage())
        {
            //Draw a message background
            TextRenderUtil.drawCenteredMultilineText(gui, this.getMessage(), this.screen.getGuiLeft() + 2, this.screen.getImageWidth() - 4, this.screen.getGuiTop() + 5, 0x404040);
            this.amountWidget.visible = false;
        }
        else
            this.amountWidget.visible = true;
    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) {
        //IconAndButtonUtil.renderButtonTooltips(gui, this.screen.getFont(), mouseX, mouseY, Lists.newArrayList(this.buttonToggleMode));
    }

    @Override
    public void tick() {

        this.amountWidget.tick();

        if(this.playerMode)
        {
            this.playerInput.tick();
            this.buttonTransfer.active = !this.playerInput.getText().isBlank() && this.amountWidget.getCoinValue().isValid();
        }
        else
        {
            Team team = this.selectedTeam();
            this.buttonTransfer.active = team != null && team.hasBankAccount() && this.amountWidget.getCoinValue().isValid();
        }


        if(this.hasMessage())
        {
            this.responseTimer++;
            if(this.responseTimer >= RESPONSE_DURATION)
            {
                this.responseTimer = 0;
                this.screen.getScreenHandler().clearMessage();
            }
        }
    }

    private boolean hasMessage() { return this.screen.getScreenHandler().hasTransferMessage(); }

    private Text getMessage() { return this.screen.getScreenHandler().getTransferMessage(); }

    @Override
    public void onClose() {
        SimpleSlot.SetActive(this.screen.getScreenHandler());
        this.responseTimer = 0;
        this.screen.getScreenHandler().clearMessage();
    }

    @Override
    public boolean blockInventoryClosing() { return this.playerMode; }

}