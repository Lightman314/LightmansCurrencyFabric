package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.auction;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderStorageScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.TraderStorageClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.CoinValueInput;
import io.github.lightman314.lightmanscurrency.client.gui.widget.TimeInputWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade.TradeButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.commands.CommandLCAdmin;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menu.TraderMenu;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.TraderStorageTab;
import io.github.lightman314.lightmanscurrency.common.menu.traderstorage.auction.AuctionCreateTab;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.network.server.messages.persistentdata.CMessageAddPersistentAuction;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeData;
import io.github.lightman314.lightmanscurrency.util.TimeUtil.TimeUnit;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

public class AuctionCreateClientTab extends TraderStorageClientTab<AuctionCreateTab> {

    public static final long CLOSE_DELAY = TimeUtil.DURATION_SECOND * 5;

    public AuctionCreateClientTab(TraderStorageScreen screen, AuctionCreateTab commonTab) { super(screen, commonTab); }

    @Override
    public @NotNull IconData getIcon() { return IconAndButtonUtil.ICON_PLUS; }

    @Override
    public MutableText getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.auction.create"); }

    @Override
    public boolean tabButtonVisible() { return true; }

    @Override
    public boolean blockInventoryClosing() { return CommandLCAdmin.isAdminPlayer(this.menu.player); }

    AuctionTradeData pendingAuction;

    TradeButton tradeDisplay;

    CoinValueInput priceSelect;
    ButtonWidget buttonTogglePriceMode;
    boolean startingBidMode = true;

    ButtonWidget buttonSubmitAuction;

    boolean locked = false;
    long successTime = 0;

    ButtonWidget buttonSubmitPersistentAuction;
    TextFieldWidget persistentAuctionIDInput;

    TimeInputWidget timeInput;

    @Override
    public void onOpen() {

        this.pendingAuction = new AuctionTradeData(this.menu.player);
        this.locked = false;
        this.successTime = 0;
        this.startingBidMode = true;

        this.tradeDisplay = this.screen.addRenderableTabWidget(new TradeButton(this.menu::getContext, () -> this.pendingAuction, b -> {}));
        this.tradeDisplay.move(this.screen.getGuiLeft() + 15, this.screen.getGuiTop() + 5);

        this.priceSelect = this.screen.addRenderableTabWidget(new CoinValueInput(this.screen.getGuiLeft() + this.screen.getImageWidth() / 2 - CoinValueInput.DISPLAY_WIDTH / 2, this.screen.getGuiTop() + 34, EasyText.empty(), CoinValue.EMPTY, this.font, this::onPriceChanged, this.screen::addRenderableTabWidget));
        this.priceSelect.init();
        this.priceSelect.drawBG = this.priceSelect.allowFreeToggle = false;

        this.buttonTogglePriceMode = this.screen.addRenderableTabWidget(new ButtonWidget(this.screen.getGuiLeft() + 114, this.screen.getGuiTop() + 5, this.screen.getImageWidth() - 119, 20, EasyText.translatable("button.lightmanscurrency.auction.toggleprice.startingbid"), b -> this.TogglePriceTarget()));

        this.commonTab.getAuctionItems().addListener(c -> this.UpdateAuctionItems());

        //Duration Input
        this.timeInput = this.screen.addRenderableTabWidget(new TimeInputWidget(this.screen.getGuiLeft() + 80, this.screen.getGuiTop() + 112, 10, TimeUnit.DAY, TimeUnit.HOUR, this.screen::addRenderableTabWidget, this::updateDuration));
        this.timeInput.minDuration = Math.max(LCConfig.SERVER.auctionHouseDurationMin.get() * TimeUtil.DURATION_DAY, TimeUtil.DURATION_HOUR);
        this.timeInput.maxDuration = Math.max(LCConfig.SERVER.auctionHouseDurationMax.get(), LCConfig.SERVER.auctionHouseDurationMin.get()) * TimeUtil.DURATION_DAY;
        this.timeInput.setTime(this.timeInput.minDuration);

        //Submit Button
        this.buttonSubmitAuction = this.screen.addRenderableTabWidget(new ButtonWidget(this.screen.getGuiLeft() + 40, this.screen.getGuiTop() - 20, this.screen.getImageWidth() - 80, 20, EasyText.translatable("button.lightmanscurrency.auction.create"), b -> this.submitAuction()));
        this.buttonSubmitAuction.active = false;

        this.buttonSubmitPersistentAuction = this.screen.addRenderableTabWidget(new IconButton(this.screen.getGuiLeft() + this.screen.getImageWidth() - 20, this.screen.getGuiTop() - 20, this::submitPersistentAuction, IconAndButtonUtil.ICON_PERSISTENT_DATA, IconAndButtonUtil.TOOLTIP_PERSISTENT_AUCTION));
        this.buttonSubmitPersistentAuction.visible = CommandLCAdmin.isAdminPlayer(this.screen.getScreenHandler().player);
        this.buttonSubmitPersistentAuction.active = false;

        int idWidth = this.font.getWidth(EasyText.translatable("gui.lightmanscurrency.settings.persistent.id"));
        this.persistentAuctionIDInput = this.screen.addRenderableTabWidget(new TextFieldWidget(this.font, this.screen.getGuiLeft() + idWidth + 2, this.screen.getGuiTop() - 40, this.screen.getImageWidth() - idWidth - 2, 18, EasyText.empty()));
        this.persistentAuctionIDInput.visible = CommandLCAdmin.isAdminPlayer(this.screen.getScreenHandler().player);

    }

    @Override
    public void onClose() {
        this.commonTab.getAuctionItems().removeListener(c -> this.UpdateAuctionItems());
    }

    @Override
    public void renderBG(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {

        RenderSystem.setShaderTexture(0, TraderScreen.GUI_TEXTURE);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        for(SimpleSlot slot : this.commonTab.getSlots())
        {
            //Render Slot BG's
            this.screen.drawTexture(pose, this.screen.getGuiLeft() + slot.x - 1, this.screen.getGuiTop() + slot.y - 1, TraderScreen.WIDTH, 0, 18, 18);
        }

        //Item Slot label
        this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.auction.auctionitems"), this.screen.getGuiLeft() + TraderMenu.SLOT_OFFSET + 7, this.screen.getGuiTop() + 112, 0x404040);

        if(this.locked && this.successTime != 0)
            TextRenderUtil.drawCenteredText(pose, EasyText.translatable("gui.lightmanscurrency.auction.create.success").formatted(Formatting.BOLD), this.screen.getGuiLeft() + this.screen.getImageWidth() / 2, 34, 0x404040);

        if(CommandLCAdmin.isAdminPlayer(this.screen.getScreenHandler().player))
        {
            this.font.draw(pose, EasyText.translatable("gui.lightmanscurrency.settings.persistent.id"), this.screen.getGuiLeft(), this.screen.getGuiTop() - 35, 0xFFFFFF);
        }

    }

    @Override
    public void renderTooltips(MatrixStack pose, int mouseX, int mouseY) {

        this.tradeDisplay.renderTooltips(pose, mouseX, mouseY);

        IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, Lists.newArrayList(this.buttonSubmitPersistentAuction));

    }

    @Override
    public void tick() {
        this.priceSelect.locked = this.locked;
        this.priceSelect.tick();
        if(this.locked && this.successTime != 0)
        {
            if(TimeUtil.compareTime(CLOSE_DELAY, this.successTime))
            {
                this.screen.changeTab(TraderStorageTab.TAB_TRADE_BASIC);
                return;
            }
        }
        if(this.locked)
        {
            this.buttonTogglePriceMode.active = this.buttonSubmitAuction.active = false;
        }
        else
        {
            this.buttonTogglePriceMode.active = true;

            this.buttonSubmitAuction.active = this.pendingAuction.isValid();
        }

        if(CommandLCAdmin.isAdminPlayer(this.screen.getScreenHandler().player))
        {
            this.buttonSubmitPersistentAuction.visible = this.persistentAuctionIDInput.visible = !this.locked;
            this.buttonSubmitPersistentAuction.active = this.pendingAuction.isValid();
            this.persistentAuctionIDInput.tick();
        }
        else
            this.buttonSubmitPersistentAuction.visible = this.persistentAuctionIDInput.visible = false;

    }

    private void UpdateAuctionItems() {
        this.pendingAuction.setAuctionItems(this.commonTab.getAuctionItems());
    }

    private void onPriceChanged(CoinValue newPrice) {
        if(this.startingBidMode)
            this.pendingAuction.setStartingBid(newPrice);
        else
            this.pendingAuction.setMinBidDifferent(newPrice);
    }

    private void TogglePriceTarget() {
        this.startingBidMode = !this.startingBidMode;
        this.buttonTogglePriceMode.setMessage(EasyText.translatable(this.startingBidMode ? "button.lightmanscurrency.auction.toggleprice.startingbid" : "button.lightmanscurrency.auction.toggleprice.mindeltabid"));
        if(this.startingBidMode)
            this.priceSelect.setCoinValue(this.pendingAuction.getLastBidAmount());
        else
            this.priceSelect.setCoinValue(this.pendingAuction.getMinBidDifference());
    }



    private void updateDuration(TimeData newTime) {
        this.pendingAuction.setDuration(newTime.miliseconds);
    }

    private void submitAuction() {
        //LightmansCurrency.LogInfo("Sending Auction to the server!\n" + this.pendingAuction.getAsNBT().getAsString());
        this.commonTab.createAuction(this.pendingAuction);
        this.locked = true;
        for(SimpleSlot slot : this.commonTab.getSlots())
            slot.locked = true;
    }

    private void submitPersistentAuction(ButtonWidget button) {
        new CMessageAddPersistentAuction(this.pendingAuction.getAsNBT(), this.persistentAuctionIDInput.getText()).sendToServer();
    }

    @Override
    public void receiveServerMessage(NbtCompound message) {
        if(message.contains("AuctionCreated"))
        {
            //LightmansCurrency.LogInfo("Received create response message from the server.\nAuction Created: " + message.getBoolean("AuctionCreated"));
            if(message.getBoolean("AuctionCreated"))
                this.successTime = TimeUtil.getCurrentTime();
            else
            {
                this.locked = false;
                for(SimpleSlot slot : this.commonTab.getSlots())
                    slot.locked = false;
            }
        }
    }

}