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
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
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

public class PlayerBlacklist extends TradeRule {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID, "blacklist");

    List<PlayerReference> bannedPlayers = new ArrayList<>();

    public PlayerBlacklist() { super(TYPE); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {

        if(this.isBlacklisted(event.getPlayerReference()))
            event.addDenial(Text.translatable("traderule.lightmanscurrency.blacklist.denial"));
    }

    public boolean isBlacklisted(PlayerReference player)
    {
        for (PlayerReference bannedPlayer : this.bannedPlayers) {
            if (bannedPlayer.is(player))
                return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        //Save player
        NbtList playerNameList = new NbtList();
        for (PlayerReference bannedPlayer : this.bannedPlayers) {
            playerNameList.add(bannedPlayer.save());
        }
        compound.put("BannedPlayers", playerNameList);
    }

    @Override
    public JsonObject saveToJson(JsonObject json) {
        JsonArray blacklist = new JsonArray();
        for (PlayerReference bannedPlayer : this.bannedPlayers) {
            blacklist.add(bannedPlayer.saveAsJson());
        }
        json.add("BannedPlayers", blacklist);
        return json;
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {

        //Load blacklisted players
        if(compound.contains("BannedPlayers", NbtElement.LIST_TYPE))
        {
            this.bannedPlayers.clear();
            NbtList playerList = compound.getList("BannedPlayers", NbtElement.COMPOUND_TYPE);
            for(int i = 0; i < playerList.size(); ++i)
            {
                PlayerReference reference = PlayerReference.load(playerList.getCompound(i));
                if(reference != null)
                    this.bannedPlayers.add(reference);
            }
        }

    }

    @Override
    public void handleUpdateMessage(NbtCompound updateInfo) {
        boolean add = updateInfo.getBoolean("Add");
        String name = updateInfo.getString("Name");
        PlayerReference player = PlayerReference.of(false, name);
        if(player == null)
            return;
        if(add && !this.isBlacklisted(player))
        {
            this.bannedPlayers.add(player);
        }
        else if(!add && this.isBlacklisted(player))
        {
            PlayerReference.removeFromList(this.bannedPlayers, player);
        }
    }

    @Override
    public void loadFromJson(JsonObject json) {
        if(json.has("BannedPlayers"))
        {
            this.bannedPlayers.clear();
            JsonArray blacklist = json.get("BannedPlayers").getAsJsonArray();
            for(int i = 0; i < blacklist.size(); ++i) {
                PlayerReference reference = PlayerReference.load(blacklist.get(i).getAsJsonObject());
                if(reference != null && !this.isBlacklisted(reference))
                    this.bannedPlayers.add(reference);
            }
        }
    }

    @Override
    public NbtCompound savePersistentData() { return null; }
    @Override
    public void loadPersistentData(NbtCompound data) { }

    public IconData getButtonIcon() { return IconAndButtonUtil.ICON_BLACKLIST; }

    @Override
    @Environment(EnvType.CLIENT)
    public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule) { return new GUIHandler(screen, rule); }

    @Environment(EnvType.CLIENT)
    private static class GUIHandler extends TradeRule.GUIHandler
    {

        protected final PlayerBlacklist getBlacklistRule()
        {
            if(getRuleRaw() instanceof PlayerBlacklist)
                return (PlayerBlacklist)getRuleRaw();
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

            this.buttonAddPlayer = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + 10, screen.guiTop() + 30, 78, 20, Text.translatable("gui.button.lightmanscurrency.blacklist.add"), this::PressBlacklistButton));
            this.buttonRemovePlayer = this.addCustomRenderable(new VanillaButton(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 30, 78, 20, Text.translatable("gui.button.lightmanscurrency.blacklist.remove"), this::PressForgiveButton));

            this.playerDisplay = this.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 55, this.screen.xSize - 14, 114, this.screen.getFont(), this::getBlacklistedPlayers));
            this.playerDisplay.setColumnCount(2);

        }

        private List<Text> getBlacklistedPlayers()
        {
            List<Text> playerList = Lists.newArrayList();
            if(getBlacklistRule() == null)
                return playerList;
            for(PlayerReference player : getBlacklistRule().bannedPlayers)
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

        void PressBlacklistButton(ButtonWidget button)
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

        void PressForgiveButton(ButtonWidget button)
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