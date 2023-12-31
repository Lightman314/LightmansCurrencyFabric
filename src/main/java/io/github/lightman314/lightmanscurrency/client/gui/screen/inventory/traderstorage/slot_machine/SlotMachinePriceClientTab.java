package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.slot_machine;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.slot_machine.SlotMachinePriceTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import org.jetbrains.annotations.NotNull;

public class SlotMachinePriceClientTab extends TraderStorageClientTab<SlotMachinePriceTab> {

    public SlotMachinePriceClientTab(TraderStorageScreen screen, SlotMachinePriceTab commonTab) { super(screen, commonTab); }

    @Override
    public @NotNull IconData getIcon() { return IconData.of(ModItems.COIN_GOLD); }

    @Override
    public MutableText getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.trader.slot_machine.edit_price"); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return false; }

    private CoinValueInput priceInput;

    @Override
    public void onOpen() {
        CoinValue startingPrice = CoinValue.EMPTY;
        if(this.menu.getTrader() instanceof SlotMachineTraderData trader)
            startingPrice = trader.getPrice();
        this.priceInput = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + (this.screen.getImageWidth() / 2) - (CoinValueInput.DISPLAY_WIDTH / 2), this.screen.getGuiTop() + 12, EasyText.empty(), startingPrice, this.font, this::ChangePrice, this.screen::addRenderableTabWidget));
        this.priceInput.init();
        this.priceInput.drawBG = false;
    }

    @Override
    public void tick() {
        this.priceInput.tick();
    }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) { }

    private void ChangePrice(CoinValue newPrice) { this.commonTab.SetPrice(newPrice); }

}