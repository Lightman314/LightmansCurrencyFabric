package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionData;
import io.github.lightman314.lightmanscurrency.common.menu.TraderRecoveryMenu;
import io.github.lightman314.lightmanscurrency.network.server.messages.emergencyejection.CMessageChangeSelectedData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TraderRecoveryScreen extends MenuScreen<TraderRecoveryMenu> {

    public static final Identifier GUI_TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    public TraderRecoveryScreen(TraderRecoveryMenu menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, title);
        this.backgroundHeight = 222;
        this.backgroundWidth = 176;
    }

    ButtonWidget buttonLeft;
    ButtonWidget buttonRight;

    @Override
    protected void init() {
        super.init();

        this.buttonLeft = this.addDrawableChild(new IconButton(this.x - 20, this.y, b -> this.changeSelection(-1), IconAndButtonUtil.ICON_LEFT));
        this.buttonRight = this.addDrawableChild(new IconButton(this.x + this.backgroundWidth, this.y, b -> this.changeSelection(1), IconAndButtonUtil.ICON_RIGHT));

    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY) {

        gui.setShaderColor(1f, 1f, 1f, 1f);

        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

    }

    private MutableText getTraderTitle() {
        EjectionData data = this.handler.getSelectedData();
        if(data != null)
            return data.getTraderName();
        return Text.translatable("gui.lightmanscurrency.trader_recovery.nodata");
    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY) {
        gui.drawText(this.textRenderer, this.getTraderTitle(), this.titleX, this.titleY, 0x404040, false);
        gui.drawText(this.textRenderer, this.playerInventoryTitle, this.playerInventoryTitleX, this.backgroundHeight - 94, 0x404040, false);
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, partialTicks);
        this.drawMouseoverTooltip(gui, mouseX, mouseY);

    }

    @Override
    protected void handledScreenTick() {

        this.buttonLeft.active = this.handler.getSelectedIndex() > 0;
        this.buttonRight.active = this.handler.getSelectedIndex() < this.handler.getValidEjectionData().size() - 1;

    }

    private void changeSelection(int delta) {
        int newSelection = this.handler.getSelectedIndex() + delta;
        new CMessageChangeSelectedData(newSelection).sendToServer();
    }

}