package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PlayerDiscounts extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "discount_list");

    List<PlayerReference> playerList = new ArrayList<>();
    int discount = 10;
    public int getDiscountPercent() { return this.discount; }
    public void setDiscountPercent(int percent) { this.discount = MathUtil.clamp(percent, 0, 99); }
    private double getDiscountMult() { return 1d - ((double)discount/100d); }
    private double getIncreaseMult() { return 1d + ((double)discount/100d); }

    public PlayerDiscounts() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event)
    {
        if(this.isOnList(event.getPlayerReference()))
        {
            switch (event.getTrade().getTradeDirection()) {
                case SALE ->
                        event.addHelpful(Text.translatable("traderule.lightmanscurrency.discount_list.info.sale", this.discount));
                case PURCHASE ->
                        event.addHelpful(Text.translatable("traderule.lightmanscurrency.discount_list.info.purchase", this.discount));
                default -> {
                } //Nothing by default
            }
        }
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event)
    {
        if(this.isOnList(event.getPlayerReference()))
        {
            switch (event.getTrade().getTradeDirection()) {
                case SALE -> event.applyCostMultiplier(this.getDiscountMult());
                case PURCHASE -> event.applyCostMultiplier(this.getIncreaseMult());
                default -> {
                } //Nothing by default
            }
        }
    }

    public boolean isOnList(PlayerReference player)
    {
        for (PlayerReference playerReference : this.playerList) {
            if (playerReference.is(player))
                return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        //Save player names
        NbtList playerNameList = new NbtList();
        for (PlayerReference playerReference : playerList) {
            playerNameList.add(playerReference.save());
        }
        compound.put("Players", playerNameList);
        //Save discount
        compound.putInt("Discount", this.discount);
    }

    @Override
    public JsonObject saveToJson(JsonObject json) {
        JsonArray playerList = new JsonArray();
        for (PlayerReference playerReference : this.playerList) {
            playerList.add(playerReference.saveAsJson());
        }
        json.add("Players", playerList);
        json.addProperty("Discount", this.discount);
        return json;
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        //Load player names
        if(compound.contains("Players", NbtElement.LIST_TYPE))
        {
            this.playerList.clear();
            NbtList playerNameList = compound.getList("Players", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < playerNameList.size(); i++)
            {
                NbtCompound thisCompound = playerNameList.getCompound(i);
                PlayerReference reference = PlayerReference.load(thisCompound);
                if(reference != null)
                    this.playerList.add(reference);
            }
        }
        //Load discount
        if(compound.contains("Discount", NbtElement.INT_TYPE))
            this.discount = compound.getInt("Discount");

    }

    @Override
    public void loadFromJson(JsonObject json) {
        if(json.has("Players"))
        {
            this.playerList.clear();
            JsonArray playerList = json.get("Players").getAsJsonArray();
            for(int i = 0; i < playerList.size(); ++i) {
                PlayerReference reference = PlayerReference.load(playerList.get(i));
                if(reference != null && !this.isOnList(reference))
                    this.playerList.add(reference);
            }
        }
        if(json.has("Discount"))
            this.discount = json.get("Discount").getAsInt();
    }

    @Override
    protected void handleUpdateMessage(NbtCompound updateInfo)
    {
        if(updateInfo.contains("Discount"))
        {
            this.discount = updateInfo.getInt("Discount");
        }
        else
        {
            boolean add = updateInfo.getBoolean("Add");
            String name = updateInfo.getString("Name");
            PlayerReference player = PlayerReference.of(false, name);
            if(add && !this.isOnList(player))
            {
                this.playerList.add(player);
            }
            else if(!add && this.isOnList(player))
            {
                PlayerReference.removeFromList(this.playerList, player);
            }
        }
    }

    @Override
    public NbtCompound savePersistentData() { return null; }
    @Override
    public void loadPersistentData(NbtCompound data) { }

    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_DISCOUNT_LIST; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        protected final PlayerDiscounts getRule()
        {
            if(getRuleRaw() instanceof PlayerDiscounts)
                return (PlayerDiscounts)getRuleRaw();
            return null;
        }

        GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            super(screen, rule);
        }

        TextFieldWidget nameInput;
        TextFieldWidget discountInput;

        ButtonWidget buttonAddPlayer;
        ButtonWidget buttonRemovePlayer;

        ButtonWidget buttonSetDiscount;

        ScrollTextDisplay playerList;

        @Override
        public void initTab() {

            this.nameInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 34, screen.xSize - 20, 20, Text.empty()));

            this.buttonAddPlayer = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 55, 78, 20, Text.translatable("gui.button.lightmanscurrency.discount.add"), this::PressAddButton));
            this.buttonRemovePlayer = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 55, 78, 20, Text.translatable("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton));

            this.discountInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, Text.empty()));
            this.discountInput.setMaxLength(2);
            this.discountInput.setText(Integer.toString(this.getRule().discount));
            this.buttonSetDiscount = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, Text.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));

            this.playerList = this.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 78, screen.xSize - 14, 91, this.screen.getFont(), this::getPlayerList));
            this.playerList.setColumnCount(2);

        }

        private List<Text> getPlayerList()
        {
            List<Text> playerList = Lists.newArrayList();
            if(getRule() == null)
                return playerList;
            for(PlayerReference player : getRule().playerList)
                playerList.add(player.getNameComponent(true));
            return playerList;
        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

            if(getRule() == null)
                return;

            gui.fill(this.screen.guiLeft() + 7, this.screen.guiTop() + 78, this.screen.guiLeft() + this.screen.width - 7, this.screen.guiTop() + 78 + 91, 0x000000FF);

            gui.drawText(this.screen.getFont(), Text.translatable("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.getX() + this.discountInput.getWidth() + 4, this.discountInput.getY() + 3, 0xFFFFFF, false);

        }

        @Override
        public void onScreenTick()
        {
            TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
        }


        @Override
        public void onTabClose() {

            this.removeCustomWidget(this.nameInput);
            this.removeCustomWidget(this.buttonAddPlayer);
            this.removeCustomWidget(this.buttonRemovePlayer);
            this.removeCustomWidget(this.discountInput);
            this.removeCustomWidget(this.buttonSetDiscount);
            this.removeCustomWidget(this.playerList);

        }

        void PressAddButton(ButtonWidget button)
        {
            String name = nameInput.getText();
            if(!name.isBlank())
            {
                nameInput.setText("");
                NbtCompound updateInfo = new NbtCompound();
                updateInfo.putBoolean("Add", true);
                updateInfo.putString("Name", name);
                this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
            }
        }

        void PressForgetButton(ButtonWidget button)
        {
            String name = nameInput.getText();
            if(!name.isBlank())
            {
                nameInput.setText("");
                NbtCompound updateInfo = new NbtCompound();
                updateInfo.putBoolean("Add", false);
                updateInfo.putString("Name", name);
                this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
            }

        }

        void PressSetDiscountButton(ButtonWidget button)
        {
            int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
            this.getRule().discount = discount;
            NbtCompound updateInfo = new NbtCompound();
            updateInfo.putInt("Discount", discount);
            this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
        }

    }

}