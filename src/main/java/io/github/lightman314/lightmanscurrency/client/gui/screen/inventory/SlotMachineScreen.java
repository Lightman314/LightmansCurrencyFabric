package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.slot_machine.SlotMachineRenderer;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.slot_machine.SlotMachineEntryDisplayWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.util.LazyWidgetPositioner;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.SlotMachineMenu;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.SlotMachineTraderData;
import io.github.lightman314.lightmanscurrency.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageCollectCoins;
import io.github.lightman314.lightmanscurrency.network.server.messages.trader.CMessageOpenStorage;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SlotMachineScreen extends MenuScreen<SlotMachineMenu> implements ScrollListener.IScrollListener, ScrollBarWidget.IScrollable {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/slot_machine.png");

    public static final int WIDTH = 176;
    public static final int HEIGHT = 222;

    public static final int ENTRY_ROWS = 2;
    public static final int ENTRY_COLUMNS = 2;
    public static final int ENTRIES_PER_PAGE = ENTRY_ROWS * ENTRY_COLUMNS;

    private boolean interactMode = true;
    private int scroll = 0;
    public TextRenderer getFont() { return this.textRenderer; }

    IconButton buttonOpenStorage;
    IconButton buttonCollectCoins;
    private final Supplier<Tooltip> collectCoinsTooltip = new IconAndButtonUtil.AdditiveTooltip(IconAndButtonUtil.TOOLTIP_COLLECT_COINS, () -> {
        TraderData t = this.handler.getTrader();
        if(t != null)
            return new Object[] { t.getStoredMoney().getString() };
        return new Object[0];
    });

    IconButton buttonOpenTerminal;

    ButtonWidget buttonInteract;
    ButtonWidget buttonInteract5;
    ButtonWidget buttonInteract10;

    ButtonWidget buttonInfo;

    ScrollListener scrollListener;
    ScrollBarWidget scrollBar;

    List<SlotMachineEntryDisplayWidget> infoWidgets = new ArrayList<>();

    public SlotMachineScreen(SlotMachineMenu menu, PlayerInventory inventory, Text title)
    {
        super(menu, inventory, title);
        this.backgroundWidth = WIDTH;
        this.backgroundHeight = HEIGHT;
    }

    private final SlotMachineRenderer slotRenderer = new SlotMachineRenderer(this);

    public final ScreenPosition SM_INFO_WIDGET = ScreenPosition.of(WIDTH - 16, 8);



    @Override
    protected void init() {

        super.init();

        LazyWidgetPositioner leftEdgePositioner = LazyWidgetPositioner.create(this, LazyWidgetPositioner.MODE_BOTTOMUP, -20, HEIGHT - 20, 20);

        this.infoWidgets.clear();

        this.buttonOpenStorage = this.addDrawableChild(new IconButton(this.getGuiLeft() + TraderMenu.SLOT_OFFSET - 20, this.getGuiTop() + 118, this::OpenStorage, IconAndButtonUtil.ICON_STORAGE));
        this.buttonOpenStorage.setVisiblityCheck(() -> this.handler.getTrader() != null && this.handler.getTrader().hasPermission(this.handler.player, Permissions.OPEN_STORAGE));

        this.buttonCollectCoins = this.addDrawableChild(new IconButton(this.getGuiLeft() + TraderMenu.SLOT_OFFSET - 20, this.getGuiTop() + 138, this::CollectCoins, IconAndButtonUtil.ICON_COLLECT_COINS));
        this.buttonCollectCoins.setVisiblityCheck(() -> {
            TraderData trader = this.handler.getTrader();
            if(trader == null)
                return false;
            return trader.hasPermission(this.handler.player, Permissions.COLLECT_COINS) && !trader.hasBankAccount();
        });
        this.buttonCollectCoins.setActiveCheck(() -> {
            TraderData trader = this.handler.getTrader();
            if(trader == null)
                return false;
            return trader.getInternalStoredMoney().getRawValue() > 0;
        });

        this.buttonOpenTerminal = this.addDrawableChild(new IconButton(this.getGuiLeft() + TraderMenu.SLOT_OFFSET - 20, this.getGuiTop() + this.getImageHeight() - 20, this::OpenTerminal, IconAndButtonUtil.ICON_BACK));
        this.buttonOpenTerminal.setVisiblityCheck(this::showTerminalButton);

        leftEdgePositioner.clear();
        leftEdgePositioner.addWidgets(this.buttonOpenTerminal, this.buttonOpenStorage, this.buttonCollectCoins);
        leftEdgePositioner.reposition();

        this.buttonInteract = this.addDrawableChild(new PlainButton(this.getGuiLeft() + 52, this.getGuiTop() + 107, 18, 18, b -> this.ExecuteTrade(1),  GUI_TEXTURE, WIDTH, 0));
        this.buttonInteract5 = this.addDrawableChild(new PlainButton(this.getGuiLeft() + 29, this.getGuiTop() + 107, 18, 18, b -> this.ExecuteTrade(5), GUI_TEXTURE, WIDTH + 18, 0));
        this.buttonInteract10 = this.addDrawableChild(new PlainButton(this.getGuiLeft() + 7, this.getGuiTop() + 107, 18, 18, b -> this.ExecuteTrade(10), GUI_TEXTURE, WIDTH + 36, 0));

        this.buttonInfo = this.addDrawableChild(new PlainButton(this.getGuiLeft() + SM_INFO_WIDGET.x, this.getGuiTop() + SM_INFO_WIDGET.y, 10, 11, this::ToggleMode, GUI_TEXTURE, WIDTH, 36));

        this.scrollListener = this.addSelectableChild(new ScrollListener(this.getGuiLeft(), this.getGuiTop(), this.getImageWidth(), this.getImageHeight(), this));
        this.scrollListener.active = this.isInfoMode();

        for(int y = 0; y < ENTRY_ROWS; ++y)
        {
            for(int x = 0; x < ENTRY_COLUMNS; x++)
            {
                int displayIndex = (y * ENTRY_COLUMNS) + x;
                this.infoWidgets.add(this.addDrawableChild(new SlotMachineEntryDisplayWidget(ScreenPosition.of(this.getGuiLeft(), this.getGuiTop()).offset(19 + (x * SlotMachineEntryDisplayWidget.WIDTH), 10 + (y * SlotMachineEntryDisplayWidget.HEIGHT)), this.handler::getTrader, () -> this.getTrueIndex(displayIndex), this.getFont())));
            }
        }

        this.scrollBar = this.addDrawableChild(new ScrollBarWidget(this.getGuiLeft() + 8, this.getGuiTop() + 10, 2 * SlotMachineEntryDisplayWidget.HEIGHT, this));

    }

    private boolean isInteractMode() { return this.interactMode; }
    private boolean isInfoMode() { return !this.interactMode; }

    private void ToggleMode(ButtonWidget button) { if(this.handler.hasPendingReward()) return; this.interactMode = !this.interactMode; if(this.isInfoMode()) this.validateScroll(); }

    private boolean allowInteraction()
    {
        SlotMachineTraderData trader = this.handler.getTrader();
        return !this.handler.hasPendingReward() && trader != null && trader.hasStock() && trader.hasValidTrade();
    }

    private boolean showTerminalButton() {
        if(this.handler.getTrader() != null)
            return this.handler.getTrader().showOnTerminal();
        return false;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {

        //Background
        context.drawTexture(GUI_TEXTURE, this.getGuiLeft(), this.getGuiTop(), 0, 0, this.getImageWidth(), this.getImageHeight());

        //Slot Machine
        if(this.isInteractMode())
            this.slotRenderer.render(context, delta);

    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, this.playerInventoryTitle, 8, this.getImageHeight() - 94, 0x404040, false);

        String valueText = MoneyUtil.getStringOfValue(this.handler.getContext(null).getAvailableFunds());
        context.drawText(this.textRenderer, valueText, 170 - this.textRenderer.getWidth(valueText) - 10, this.getImageHeight() - 94, 0x404040, false);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {

        //Active and visibility checks
        try {
            this.buttonInteract.visible = this.buttonInteract5.visible = this.buttonInteract10.visible = this.isInteractMode();
            this.buttonInteract.active = this.buttonInteract5.active = this.buttonInteract10.active = this.allowInteraction();
            this.scrollListener.active = this.scrollBar.visible = this.isInfoMode();
            for(SlotMachineEntryDisplayWidget w : this.infoWidgets)
                w.visible = this.isInfoMode();
        } catch(NullPointerException ignored) {}

        this.renderBackground(context);

        super.render(context, mouseX, mouseY, delta);

        context.getMatrices().push();
        context.getMatrices().translate(0d,0d,250d);
        //Manually draw all tooltips on the front layer

        this.drawMouseoverTooltip(context, mouseX, mouseY);

        if(this.buttonInteract != null && this.buttonInteract.isMouseOver(mouseX, mouseY))
            context.drawTooltip(this.textRenderer, this.getInteractionTooltip(1), mouseX, mouseY);
        if(this.buttonInteract5 != null && this.buttonInteract5.isMouseOver(mouseX, mouseY))
            context.drawTooltip(this.textRenderer, this.getInteractionTooltip(5), mouseX, mouseY);
        if(this.buttonInteract10 != null && this.buttonInteract10.isMouseOver(mouseX, mouseY))
            context.drawTooltip(this.textRenderer, this.getInteractionTooltip(10), mouseX, mouseY);
        if(this.buttonInfo != null && this.buttonInfo.isMouseOver(mouseX, mouseY))
            context.drawTooltip(this.textRenderer, this.getInfoTooltip(), mouseX, mouseY);
        if(this.buttonOpenStorage != null && this.buttonOpenStorage.isMouseOver(mouseX,mouseY))
            context.drawOrderedTooltip(this.textRenderer, IconAndButtonUtil.TOOLTIP_STORAGE.get().getLines(this.client), mouseX, mouseY);
        if(this.buttonCollectCoins != null && this.buttonCollectCoins.isMouseOver(mouseX,mouseY))
            context.drawOrderedTooltip(this.textRenderer, this.collectCoinsTooltip.get().getLines(this.client), mouseX, mouseY);
        if(this.buttonOpenTerminal != null && this.buttonOpenTerminal.isMouseOver(mouseX,mouseY))
            context.drawOrderedTooltip(this.textRenderer, IconAndButtonUtil.TOOLTIP_BACK_TO_TERMINAL.get().getLines(this.client), mouseX, mouseY);
        for(SlotMachineEntryDisplayWidget w : this.infoWidgets)
            w.renderTooltips(context, mouseX, mouseY);


        context.getMatrices().pop();

    }

    @Nullable
    private List<Text> getInfoTooltip()
    {
        SlotMachineTraderData trader = this.handler.getTrader();
        if(trader != null)
        {
            List<Text> info = trader.getSlotMachineInfo();
            if(this.isInfoMode())
                info.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.to_interact"));
            else
                info.add(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.to_info"));
            return info;
        }
        return null;
    }

    private List<Text> getInteractionTooltip(int count) {
        SlotMachineTraderData trader = this.handler.getTrader();
        if(trader != null)
        {
            if(count == 1)
                return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.roll"), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.roll.cost", trader.getPrice().getString()));
            else
                return ImmutableList.of(EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls", count), EasyText.translatable("tooltip.lightmanscurrency.slot_machine.rolls.cost", trader.getPrice().getString()));
        }
        return ImmutableList.of();
    }

    private void ExecuteTrade(int count) {
        this.handler.SendMessageToServer(LazyPacketData.builder().setInt("ExecuteTrade", count));
    }

    private void OpenStorage(ButtonWidget button) {
        if(this.handler.getTrader() != null)
            new CMessageOpenStorage(this.handler.getTrader().getID()).sendToServer();
    }

    private void CollectCoins(ButtonWidget button) {
        if(this.handler.getTrader() != null)
            new CMessageCollectCoins().sendToServer();
    }

    private void OpenTerminal(ButtonWidget button) {
        if(this.showTerminalButton())
        {
            this.handler.closeMenu(this.handler.player);
            TradingTerminalScreen.open();
        }
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        this.scrollListener.active = this.isInfoMode();
        if(this.isInfoMode())
            this.validateScroll();
        this.slotRenderer.tick();
    }

    protected void validateScroll()
    {
        if(this.scroll > this.getMaxScroll() && this.scroll > 0)
            this.setScroll(Math.max(this.getMaxScroll(),0));
    }

    private List<SlotMachineEntry> getEntries()
    {
        SlotMachineTraderData trader = this.handler.getTrader();
        if(trader != null)
            return trader.getValidEntries();
        return new ArrayList<>();
    }

    private int getTrueIndex(int displayIndex) { return displayIndex + (this.scroll * ENTRY_COLUMNS); }

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) { this.scroll = newScroll; }

    @Override
    public int getMaxScroll() { return ScrollBarWidget.IScrollable.calculateMaxScroll(ENTRIES_PER_PAGE, ENTRY_COLUMNS, this.getEntries().size()); }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int scroll = this.currentScroll();
        if(delta < 0)
        {
            if(scroll < this.getMaxScroll())
            {
                this.setScroll(scroll + 1);
                return true;
            }
        }
        else if(delta > 0)
        {
            if(scroll > 0)
            {
                this.setScroll(scroll - 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.scrollBar.onMouseClicked(mouseX,mouseY,button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.scrollBar.onMouseReleased(mouseX,mouseY,button);
        return super.mouseReleased(mouseX, mouseY, button);
    }
}