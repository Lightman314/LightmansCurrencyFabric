package io.github.lightman314.lightmanscurrency.client.gui.screen.team;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import io.github.lightman314.lightmanscurrency.network.server.messages.team.CMessageRenameTeam;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class TeamNameTab extends TeamTab {

    public static final TeamNameTab INSTANCE = new TeamNameTab();

    private TeamNameTab() { }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(Text.translatable("gui.button.lightmanscurrency.changename")); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.team.name"); }

    @Override
    public boolean allowViewing(PlayerEntity player, Team team) { return team != null && team.isAdmin(player); }

    TextFieldWidget nameInput;
    ButtonWidget buttonChangeName;

    @Override
    public void initTab() {

        TeamManagerScreen screen = this.getScreen();

        this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(this.getFont(), screen.guiLeft() + 20, screen.guiTop() + 20, 160, 20, Text.empty()));
        this.nameInput.setMaxLength(Team.MAX_NAME_LENGTH);
        this.nameInput.setText(this.getActiveTeam().getName());

        this.buttonChangeName = screen.addRenderableTabWidget(new VanillaButton(screen.guiLeft() + 20, screen.guiTop() + 45, 160, 20, Text.translatable("gui.button.lightmanscurrency.team.rename"), this::changeName));
        this.buttonChangeName.active = false;
    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        TeamManagerScreen screen = this.getScreen();

        String currentName = "NULL";
        if(this.getActiveTeam() != null)
            currentName = this.getActiveTeam().getName();
        gui.drawText(this.getFont(), Text.translatable("gui.lightmanscurrency.team.name.current", currentName), screen.guiLeft() + 20, screen.guiTop() + 10, 0x404040, false);

    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void tick() {

        this.nameInput.tick();
        this.buttonChangeName.active = !this.nameInput.getText().isBlank() && !this.nameInput.getText().contentEquals(this.getActiveTeam().getName());

    }

    @Override
    public void closeTab() {

    }

    private void changeName(ButtonWidget button)
    {
        if(this.nameInput.getText().isBlank() || this.getActiveTeam() == null)
            return;

        this.getActiveTeam().changeName(this.getPlayer(), this.nameInput.getText());
        new CMessageRenameTeam(this.getActiveTeam().getID(), this.nameInput.getText()).sendToServer();

    }

}