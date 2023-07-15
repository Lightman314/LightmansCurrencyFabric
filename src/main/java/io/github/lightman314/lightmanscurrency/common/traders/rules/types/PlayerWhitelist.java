package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.VanillaButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PlayerWhitelist extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "whitelist");

    List<PlayerReference> whitelistedPlayers = new ArrayList<>();

    public PlayerWhitelist() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {

        if(!this.isWhitelisted(event.getPlayerReference()))
            event.addDenial(Text.translatable("traderule.lightmanscurrency.whitelist.denial"));
        else
            event.addHelpful(Text.translatable("traderule.lightmanscurrency.whitelist.allowed"));

    }

    public boolean isWhitelisted(PlayerReference player)
    {
        for (PlayerReference whitelistedPlayer : this.whitelistedPlayers) {
            if (whitelistedPlayer.is(player))
                return true;
        }
        return false;
    }

    public boolean addToWhitelist(PlayerEntity player)
    {
        PlayerReference pr = PlayerReference.of(player);
        if(!this.isWhitelisted(pr))
        {
            this.whitelistedPlayers.add(pr);
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        //Save player names
        NbtList playerNameList = new NbtList();
        for (PlayerReference whitelistedPlayer : this.whitelistedPlayers) {
            playerNameList.add(whitelistedPlayer.save());
        }
        compound.put("WhitelistedPlayers", playerNameList);
    }

    @Override
    public JsonObject saveToJson(JsonObject json) { return json; }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        //Load whitelisted players
        if(compound.contains("WhitelistedPlayers", NbtElement.LIST_TYPE))
        {
            this.whitelistedPlayers.clear();
            NbtList playerList = compound.getList("WhitelistedPlayers", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < playerList.size(); ++i)
            {
                PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
                if(reference != null)
                    this.whitelistedPlayers.add(reference);
            }
        }

    }

    @Override
    public void loadFromJson(JsonObject json) {}

    @Override
    public void handleUpdateMessage(NbtCompound updateInfo)
    {
        boolean add = updateInfo.getBoolean("Add");
        String name = updateInfo.getString("Name");
        PlayerReference player = PlayerReference.of(false, name);
        if(player == null)
            return;
        if(add && !this.isWhitelisted(player))
        {
            this.whitelistedPlayers.add(player);
        }
        else if(!add && this.isWhitelisted(player))
        {
            PlayerReference.removeFromList(this.whitelistedPlayers, player);
        }
    }

    @Override
    public NbtCompound savePersistentData() {
        NbtCompound compound = new NbtCompound();
        this.saveAdditional(compound);
        return compound;
    }
    @Override
    public void loadPersistentData(NbtCompound data) { this.loadAdditional(data); }

    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_WHITELIST; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        protected final PlayerWhitelist getWhitelistRule()
        {
            if(getRuleRaw() instanceof PlayerWhitelist)
                return (PlayerWhitelist)getRuleRaw();
            return null;
        }

        GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
        {
            super(screen, rule);
        }

        TextFieldWidget nameInput;

        ButtonWidget buttonAddPlayer;
        ButtonWidget buttonRemovePlayer;

        ScrollTextDisplay playerDisplay;

        @Override
        public void initTab() {

            this.nameInput = this.addCustomRenderable(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, screen.xSize - 20, 20, Text.empty()));

            this.buttonAddPlayer = this.screen.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, Text.translatable("gui.button.lightmanscurrency.whitelist.add"), this::PressWhitelistButton));
            this.buttonRemovePlayer = this.screen.addCustomRenderable(new VanillaButton(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, Text.translatable("gui.button.lightmanscurrency.whitelist.remove"), this::PressForgetButton));

            //Player list display
            this.playerDisplay = this.screen.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getWhitelistedPlayers));
            this.playerDisplay.setColumnCount(2);

        }

        private List<Text> getWhitelistedPlayers()
        {
            List<Text> playerList = Lists.newArrayList();
            if(getWhitelistRule() == null)
                return playerList;
            for(PlayerReference player : getWhitelistRule().whitelistedPlayers)
                playerList.add(player.getNameComponent(true));
            return playerList;
        }

        @Override
        public void renderTab(DrawContext gui, int mouseX, int mouseY, float partialTicks) { }

        @Override
        public void onTabClose() {

            this.removeCustomWidget(this.nameInput);
            this.removeCustomWidget(this.buttonAddPlayer);
            this.removeCustomWidget(this.buttonRemovePlayer);
            this.removeCustomWidget(this.playerDisplay);

        }

        void PressWhitelistButton(ButtonWidget button)
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

    }

}