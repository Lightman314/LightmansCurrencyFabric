package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.item;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ItemEditWidget.IItemEditListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.InteractionConsumer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.item.ItemTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ItemTradeEditClientTab extends TraderStorageClientTab<ItemTradeEditTab> implements InteractionConsumer, IItemEditListener {

    private static final int X_OFFSET = 13;
    private static final int Y_OFFSET = 71;
    private static final int COLUMNS = 10;
    private static final int ROWS = 2;

    public ItemTradeEditClientTab(TraderStorageScreen screen, ItemTradeEditTab commonTab) {
        super(screen, commonTab);
    }

    @Override
    public @NotNull IconData getIcon() { return IconData.BLANK; }

    @Override
    public MutableText getTooltip() { return Text.empty(); }

    @Override
    public boolean tabButtonVisible() { return false; }

    @Override
    public boolean blockInventoryClosing() { return true; }

    @Override
    public int getTradeRuleTradeIndex() { return this.commonTab.getTradeIndex(); }

    TradeButton tradeDisplay;
    CoinValueInput priceSelection;
    TextFieldWidget customNameInput;

    ItemEditWidget itemEdit;
    ScrollBarWidget itemEditScroll;

    ButtonWidget buttonToggleTradeType;

    private int selection = -1;
    private int itemEditScrollValue = -1;

    @Override
    public void onOpen() {

        ItemTradeData trade = this.getTrade();

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, this.commonTab::getTrade, button -> {}));
        this.tradeDisplay.setPosition(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 18);
        this.priceSelection = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + TraderScreen.WIDTH / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 40, Text.empty(), trade == null ? CoinValue.EMPTY : trade.getCost(), this.font, this::onValueChanged, this.screen::addRenderableTabWidget));
        this.priceSelection.drawBG = false;
        this.priceSelection.init();

        this.itemEdit = this.screen.addRenderableTabWidget(new ItemEditWidget(this.screen.getGuiLeft() + X_OFFSET, this.screen.getGuiTop() + Y_OFFSET, COLUMNS, ROWS, this));
        this.itemEdit.init(this.screen::addRenderableTabWidget, this.screen::addTabListener);
        if(this.itemEditScrollValue >= 0)
            this.itemEdit.setScroll(itemEditScrollValue);

        this.itemEditScroll = this.screen.addRenderableTabWidget(new ScrollBarWidget(this.screen.getGuiLeft() + X_OFFSET + 18 * COLUMNS, this.screen.getGuiTop() + Y_OFFSET, 18 * ROWS, this.itemEdit));
        this.itemEditScroll.smallKnob = true;

        int labelWidth = this.font.getWidth(Text.translatable("gui.lightmanscurrency.customname"));
        this.customNameInput = this.screen.addRenderableTabWidget(new TextFieldWidget(this.font, this.screen.getGuiLeft() + 15 + labelWidth, this.screen.getGuiTop() + 38, this.screen.getImageWidth() - 28 - labelWidth, 18, Text.empty()));
        if(this.selection >= 0 && this.selection < 2 && trade != null)
            this.customNameInput.setText(trade.getCustomName(this.selection));

        this.buttonToggleTradeType = this.screen.addRenderableTabWidget(new VanillaButton(this.screen.getGuiLeft() + 113, this.screen.getGuiTop() + 15, 80, 20, Text.empty(), this::ToggleTradeType));

    }

    @Override
    public void onClose() { this.selection = -1; this.itemEditScrollValue = -1; }

    @Override
    public void renderBG(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        if(this.getTrade() == null)
            return;

        this.validateRenderables();

        if(this.itemEditScroll.visible)
            this.itemEditScroll.beforeWidgetRender(mouseY);

        //Render a down arrow over the selected position
        gui.setShaderColor(1f, 1f, 1f, 1f);

        gui.drawTexture(TraderScreen.GUI_TEXTURE, this.getArrowPosition(), this.screen.getGuiTop() + 10, TraderScreen.WIDTH + 8, 18, 8, 6);

        if(this.customNameInput.visible)
            gui.drawText(this.font, Text.translatable("gui.lightmanscurrency.customname"), this.screen.getGuiLeft() + 13, this.screen.getGuiTop() + 42, 0x404040, false);

    }

    private int getArrowPosition() {

        ItemTradeData trade = this.getTrade();
        if(this.selection == -1)
        {
            if(trade.isSale())
                return this.screen.getGuiLeft() + 25;
            if(trade.isPurchase())
                return this.screen.getGuiLeft() + 81;
            else
                return -100;
        }
        else
        {
            if(this.selection >= 2 && !trade.isBarter())
                return -100;
            int horizSlot = this.selection;
            if(trade.isSale() || trade.isBarter())
                horizSlot += 2;
            int spacing = horizSlot % 4 >= 2 ? 20 : 0;
            return this.screen.getGuiLeft() + 16 + (18 * (horizSlot % 4)) + spacing;
        }
    }

    private void validateRenderables() {

        this.priceSelection.visible = this.selection < 0 && !this.getTrade().isBarter();
        if(this.priceSelection.visible)
            this.priceSelection.tick();
        this.itemEdit.visible = this.itemEditScroll.visible = (this.getTrade().isBarter() && this.selection >=2) || (this.getTrade().isPurchase() && this.selection >= 0);
        this.customNameInput.visible = this.selection >= 0 && this.selection < 2 && !this.getTrade().isPurchase();
        if(this.customNameInput.visible && !this.customNameInput.getText().contentEquals(this.getTrade().getCustomName(this.selection)))
            this.commonTab.setCustomName(this.selection, this.customNameInput.getText());
        this.buttonToggleTradeType.setMessage(Text.translatable("gui.button.lightmanscurrency.tradedirection." + this.getTrade().getTradeType().name().toLowerCase()));
    }

    @Override
    public void tick() {
        if(this.customNameInput.visible)
            this.customNameInput.tick();
        if(this.itemEdit.visible)
        {
            this.itemEdit.tick();
            this.itemEditScrollValue = this.itemEdit.currentScroll();
        }
    }

    @Override
    public void renderTooltips(DrawContext gui, int mouseX, int mouseY) {

        this.tradeDisplay.renderTooltips(gui, this.font, mouseX, mouseY);

        if(this.selection >= 0)
            this.itemEdit.renderTooltips(gui, this.font, mouseX, mouseY);

    }

    @Override
    public void receiveSelfMessage(NbtCompound message) {
        if(message.contains("TradeIndex"))
            this.commonTab.setTradeIndex(message.getInt("TradeIndex"));
        if(message.contains("StartingSlot"))
            this.selection = message.getInt("StartingSlot");
    }

    @Override
    public void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
        if(trade instanceof ItemTradeData t)
        {
            ItemStack heldItem = this.menu.getCursorStack();
            if(t.isSale())
                this.changeSelection(-1);
            else if(t.isPurchase())
            {
                if(this.selection != index && heldItem.isEmpty())
                    this.changeSelection(index);
                else
                    this.commonTab.defaultInteraction(index, heldItem, mouseButton);
            }
            else if(t.isBarter())
            {
                if(this.selection != (index + 2) && heldItem.isEmpty())
                    this.changeSelection(index + 2);
                else
                    this.commonTab.defaultInteraction(index + 2, heldItem, mouseButton);
            }

        }
    }

    @Override
    public void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton) {
        if(trade instanceof ItemTradeData t)
        {
            ItemStack heldItem = this.menu.getCursorStack();
            if(t.isSale() || t.isBarter())
            {
                if(this.selection != index && heldItem.isEmpty())
                    this.changeSelection(index);
                else
                    this.commonTab.defaultInteraction(index, heldItem, mouseButton);
            }
            else if(t.isPurchase())
                this.changeSelection(-1);
        }
    }

    private void changeSelection(int newSelection) {
        this.selection = newSelection;
        if(this.selection == -1)
            this.priceSelection.setCoinValue(this.getTrade().getCost());
        if(this.selection >= 0 && this.selection < 2)
            this.customNameInput.setText(this.commonTab.getTrade().getCustomName(this.selection));
        if(this.selection >= 2)
            this.itemEdit.refreshSearch();
    }

    @Override
    public void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton) { }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.tradeDisplay.onInteractionClick((int)mouseX, (int)mouseY, button, this);
        this.itemEditScroll.onMouseClicked(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.itemEditScroll.onMouseReleased(mouseX, mouseY, button);
        return false;
    }

    public void onValueChanged(CoinValue value) { this.commonTab.setPrice(value.copy()); }

    @Override
    public ItemTradeData getTrade() { return this.commonTab.getTrade(); }

    @Override
    public boolean restrictItemEditItems() { return this.selection < 2; }

    @Override
    public void onItemClicked(ItemStack item) { this.commonTab.setSelectedItem(this.selection, item); }

    private void ToggleTradeType(ButtonWidget button) {
        if(this.getTrade() != null)
            this.commonTab.setType(this.getTrade().getTradeType().next());
    }

}