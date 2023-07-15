package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.atm;

import java.util.List;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMConversionButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.network.server.messages.bank.CMessageATMExchange;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ExchangeTab extends ATMTab{

    public ExchangeTab(ATMScreen screen) { super(screen); }

    @Override
    public void init() {

        List<ATMConversionButtonData> buttonData = ATMData.get().getConversionButtons();
        int left = this.screen.getGuiLeft();
        int top = this.screen.getGuiTop();
        for(ATMConversionButtonData data : buttonData)
        {
            this.screen.addRenderableTabWidget(new ATMExchangeButton(left, top, data, this::RunConversionCommand));
        }

    }

    @Override
    public void preRender(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        gui.drawText(this.screen.getFont(), this.getTooltip(), this.screen.getGuiLeft() + 8, this.screen.getGuiTop() + 6, 0x404040, false);
    }

    @Override
    public void postRender(DrawContext gui, int mouseX, int mouseY) { }

    @Override
    public void tick() { }

    @Override
    public void onClose() { }

    private void RunConversionCommand(String command) { new CMessageATMExchange(command).sendToServer(); }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModBlocks.MACHINE_ATM); }

    @Override
    public MutableText getTooltip() { return Text.translatable("tooltip.lightmanscurrency.atm.conversion"); }

}