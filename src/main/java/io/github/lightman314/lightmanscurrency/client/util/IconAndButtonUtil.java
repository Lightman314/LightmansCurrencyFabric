package io.github.lightman314.lightmanscurrency.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.traderinterface.TraderInterfaceBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget.PressAction;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class IconAndButtonUtil {

    public static final Identifier ICON_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/icons.png");

    public static final IconData ICON_TRADER = IconData.of(ModItems.TRADING_CORE);
    //TODO add alt trader icon
    public static final IconData ICON_TRADER_ALT = IconData.of(ICON_TEXTURE, 80, 0);
    public static final IconData ICON_STORAGE = IconData.of(Items.CHEST);

    public static final IconData ICON_COLLECT_COINS = IconData.of(ICON_TEXTURE, 0, 0);
    public static final IconData ICON_STORE_COINS = IconData.of(ICON_TEXTURE, 16, 0);
    public static final IconData ICON_TRADE_RULES = IconData.of(Items.BOOK);
    public static final IconData ICON_SETTINGS = IconData.of(ICON_TEXTURE, 32, 0);

    public static final IconData ICON_BACK = IconData.of(ICON_TEXTURE, 0, 16);
    public static final IconData ICON_LEFT = IconData.of(ICON_TEXTURE, 16, 16);
    public static final IconData ICON_RIGHT = IconData.of(ICON_TEXTURE, 32, 16);

    public static final IconData ICON_SHOW_LOGGER = IconData.of(Items.WRITABLE_BOOK);
    public static final IconData ICON_CLEAR_LOGGER = IconData.of(PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER));

    public static Function<IconButton,IconData> ICON_CREATIVE(Supplier<Boolean> isCreative) {
        return b -> {
            boolean creative = b.isNarratable() ? !isCreative.get() : isCreative.get();
            return creative ? ICON_CREATIVE_ON : ICON_CREATIVE_OFF;
        };
    }
    private static final IconData ICON_CREATIVE_ON = IconData.of(ICON_TEXTURE, 48, 16);
    private static final IconData ICON_CREATIVE_OFF = IconData.of(ICON_TEXTURE, 64, 16);

    public static final IconData ICON_PERSISTENT_DATA = IconData.of(ICON_TEXTURE, 80, 16);

    public static final IconData ICON_TICKET = IconData.of(ModItems.TICKET_MASTER);
    public static final IconData ICON_PAYGATE_ACTIVATE = IconData.of(Items.REDSTONE);

    public static Supplier<IconData> ICON_INTERFACE_ACTIVE(Supplier<Boolean> isActive) {
        return () -> isActive.get() ? ICON_INTERFACE_ON : ICON_INTERFACE_OFF;
    }
    private static final IconData ICON_INTERFACE_ON = IconData.of(Items.REDSTONE_TORCH);
    private static final IconData ICON_INTERFACE_OFF = IconData.of(Items.TORCH);

    public static final IconData ICON_PLUS = IconData.of(ICON_TEXTURE, 0, 32);
    public static final IconData ICON_X = IconData.of(ICON_TEXTURE, 16, 32);

    public static final IconData ICON_WHITELIST = IconData.of(ICON_TEXTURE, 32, 32);
    public static final IconData ICON_BLACKLIST = IconData.of(ICON_TEXTURE, 48, 32);
    public static final IconData ICON_COUNT = IconData.of(ICON_TEXTURE, 64, 32);
    public static final IconData ICON_COUNT_PLAYER = IconData.of(ICON_TEXTURE, 80, 32);
    public static final IconData ICON_TIMED_SALE = IconData.of(Items.CLOCK);
    public static final IconData ICON_DISCOUNT_LIST = IconData.of(ICON_TEXTURE, 96, 32);
    public static final IconData ICON_FREE_SAMPLE = IconData.of(ICON_TEXTURE, 112, 32);
    public static final IconData ICON_PRICE_FLUCTUATION = IconData.of(ICON_TEXTURE, 128, 32);

    public static final IconData ICON_TRADELIST = IconData.of(ICON_TEXTURE, 48, 0);

    public static final IconData ICON_MODE_DISABLED = IconData.of(Items.BARRIER);
    public static final IconData ICON_MODE_REDSTONE_OFF = IconData.of(ICON_TEXTURE, 64, 0);
    public static final IconData ICON_MODE_REDSTONE = IconData.of(Items.REDSTONE_TORCH);
    public static final IconData ICON_MODE_ALWAYS_ON = IconData.of(Items.REDSTONE_BLOCK);

    public static IconData GetIcon(TraderInterfaceBlockEntity.ActiveMode mode) {
        switch(mode) {
            case DISABLED:
                return ICON_MODE_DISABLED;
            case REDSTONE_OFF:
                return ICON_MODE_REDSTONE_OFF;
            case REDSTONE_ONLY:
                return ICON_MODE_REDSTONE;
            case ALWAYS_ON:
                return ICON_MODE_ALWAYS_ON;
            default:
                return IconData.of(Items.ROTTEN_FLESH);
        }
    }

    public static final IconData ICON_CHECKMARK = IconData.of(ICON_TEXTURE, 0, 48);

    public static final IconData ICON_ONLINEMODE_TRUE = IconData.of(Items.PLAYER_HEAD);
    public static final IconData ICON_ONLINEMODE_FALSE = ICON_CHECKMARK;

    public static final SimpleTooltip TOOLTIP_TRADER = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.opentrades"));
    public static final SimpleTooltip TOOLTIP_STORAGE = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.openstorage"));

    public static final String TOOLTIP_COLLECT_COINS = "tooltip.lightmanscurrency.trader.collectcoins";
    public static final SimpleTooltip TOOLTIP_STORE_COINS = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.storecoins"));

    public static final SimpleTooltip TOOLTIP_BACK_TO_TERMINAL = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.universaltrader.back"));

    public static final Text TOOLTIP_SHOW_LOGGER = Text.translatable("tooltip.lightmanscurrency.trader.log.show");
    public static final Text TOOLTIP_HIDE_LOGGER = Text.translatable("tooltip.lightmanscurrency.trader.log.hide");
    public static final SimpleTooltip TOOLTIP_CLEAR_LOGGER = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.log.clear"));

    public static final SimpleTooltip TOOLTIP_TRADE_RULES = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.traderules"));
    public static final Text TOOLTIP_TRADE_RULES_TRADER = Text.translatable("tooltip.lightmanscurrency.trader.traderules.trader");
    public static final Text TOOLTIP_TRADE_RULES_TRADE = Text.translatable("tooltip.lightmanscurrency.trader.traderules.trade");
    public static final SimpleTooltip TOOLTIP_OPEN_SETTINGS = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.trader.settings"));

    public static final Text TOOLTIP_CREATIVE_ENABLE = Text.translatable("tooltip.lightmanscurrency.trader.creative.enable");
    public static final Text TOOLTIP_CREATIVE_DISABLE = Text.translatable("tooltip.lightmanscurrency.trader.creative.disable");

    public static final Text TOOLTIP_INTERFACE_ENABLE = Text.translatable("tooltip.lightmanscurrency.interface.enable");
    public static final Text TOOLTIP_INTERFACE_DISABLE = Text.translatable("tooltip.lightmanscurrency.interface.disable");

    public static final SimpleTooltip TOOLTIP_PERSISTENT_TRADER = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.persistent.add.trader"));
    public static final SimpleTooltip TOOLTIP_PERSISTENT_AUCTION = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.persistent.add.auction"));

    public static final SimpleTooltip TOOLTIP_PAIR_TICKET = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.paygate.setticket"));

    public static final SimpleTooltip TOOLTIP_PAYGATE_ACTIVATE = new SimpleTooltip(Text.translatable("tooltip.lightmanscurrency.paygate.paybutton"));

    public static IconButton traderButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_TRADER, TOOLTIP_TRADER); }
    public static IconButton storageButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_STORAGE, TOOLTIP_STORAGE); }
    public static IconButton storageButton(int x, int y, PressAction pressable, Supplier<Boolean> visiblityCheck) {
        IconButton button = storageButton(x,y,pressable);
        button.setVisiblityCheck(visiblityCheck);
        return button;
    }

    public static IconButton collectCoinButton(int x, int y, PressAction pressable, PlayerEntity player, Supplier<TraderData> traderSource) {
        IconButton button = new IconButton(x, y, pressable, ICON_COLLECT_COINS, new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> new Object[] { traderSource.get().getStoredMoney().getString() }));
        button.setVisiblityCheck(() -> {
            TraderData trader = traderSource.get();
            if(trader == null)
                return false;
            return trader.hasPermission(player, Permissions.COLLECT_COINS) && !trader.hasBankAccount();
        });
        button.setActiveCheck(() -> {
            TraderData trader = traderSource.get();
            if(trader == null)
                return false;
            return trader.getInternalStoredMoney().getRawValue() > 0;
        });
        return button;
    }
    public static IconButton collectCoinButtonAlt(int x, int y, PressAction pressable, Supplier<Object> storedCoinTextSource) { return new IconButton(x, y, pressable, ICON_COLLECT_COINS, new AdditiveTooltip(TOOLTIP_COLLECT_COINS, () -> new Object[] { storedCoinTextSource.get() })); }
    public static IconButton storeCoinButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_STORE_COINS, TOOLTIP_STORE_COINS); }

    public static IconButton leftButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_LEFT); }
    public static IconButton rightButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_RIGHT); }

    public static IconButton backToTerminalButton(int x, int y, PressAction pressable) { return new IconButton(x,y, pressable, ICON_BACK, TOOLTIP_BACK_TO_TERMINAL); }
    public static IconButton backToTerminalButton(int x, int y, PressAction pressable, Supplier<Boolean> visibilityCheck) {
        IconButton button = new IconButton(x,y, pressable, ICON_BACK, TOOLTIP_BACK_TO_TERMINAL);
        button.setVisiblityCheck(visibilityCheck);
        return button;
    }

    public static IconButton showLoggerButton(int x, int y, PressAction pressable, Supplier<Boolean> isLoggerVisible) { return new IconButton(x,y,pressable, ICON_SHOW_LOGGER, new ToggleTooltip(isLoggerVisible, TOOLTIP_HIDE_LOGGER, TOOLTIP_SHOW_LOGGER)); }
    public static IconButton clearLoggerButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_CLEAR_LOGGER, TOOLTIP_CLEAR_LOGGER); }

    public static IconButton tradeRuleButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_TRADE_RULES, TOOLTIP_TRADE_RULES); }
    public static IconButton tradeRuleButton(int x, int y, PressAction pressable, Supplier<Boolean> isTradeSelected) { return new IconButton(x, y, pressable, ICON_TRADE_RULES, new ToggleTooltip(isTradeSelected, TOOLTIP_TRADE_RULES_TRADE, TOOLTIP_TRADE_RULES_TRADER)); }
    public static IconButton openSettingsButton(int x, int y, PressAction pressable) { return new IconButton(x, y, pressable, ICON_SETTINGS, TOOLTIP_OPEN_SETTINGS); }

    public static IconButton creativeToggleButton(int x, int y, PressAction pressable, Supplier<Boolean> isCreative) { return new IconButton(x, y, pressable, ICON_CREATIVE(isCreative), new ToggleTooltip(isCreative, TOOLTIP_CREATIVE_DISABLE, TOOLTIP_CREATIVE_ENABLE)); }

    public static IconButton interfaceActiveToggleButton(int x, int y, PressAction pressable, Supplier<Boolean> isActive) { return new IconButton(x, y, pressable, ICON_INTERFACE_ACTIVE(isActive), new ToggleTooltip(isActive, TOOLTIP_INTERFACE_DISABLE, TOOLTIP_INTERFACE_ENABLE)); }

    public static PlainButton quickInsertButton(int x, int y, PressAction pressable) { return new PlainButton(x, y, 10, 10, pressable, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 18, 0); }
    public static PlainButton quickExtractButton(int x, int y, PressAction pressable) { return new PlainButton(x, y, 10, 10, pressable, TraderScreen.GUI_TEXTURE, TraderScreen.WIDTH + 28, 0); }

    public static PlainButton checkmarkButton(int x, int y, PressAction pressable, Supplier<Boolean> isActive) {
        return new PlainButton(x, y, 10, 10, pressable, TraderSettingsScreen.GUI_TEXTURE, () -> Pair.of(10, isActive.get() ? 200 : 220));
    }

    //public static TextLogWindow traderLogWindow(AbstractContainerScreen<?> screen, Supplier<ILoggerSupport<?>> loggerSource) { return new TextLogWindow(screen, () -> loggerSource.get().getLogger()); }

    public static DropdownWidget interactionTypeDropdown(int x, int y, int width, TextRenderer font, TraderInterfaceBlockEntity.InteractionType currentlySelected, Consumer<Integer> onSelect, Function<ButtonWidget,ButtonWidget> addButton, List<TraderInterfaceBlockEntity.InteractionType> blacklist) {
        List<Text> options = new ArrayList<>();
        for(int i = 0; i < TraderInterfaceBlockEntity.InteractionType.size(); ++i)
            options.add(TraderInterfaceBlockEntity.InteractionType.fromIndex(i).getDisplayText());
        return new DropdownWidget(x, y, width, font, currentlySelected.index, onSelect, (index) ->  !blacklist.contains(TraderInterfaceBlockEntity.InteractionType.fromIndex(index)), addButton, options);
    }


    public static void renderButtonTooltips(DrawContext gui, TextRenderer font, int mouseX, int mouseY, List<? extends Object> widgets)
    {
        //Nothing to do, as all tooltip-relevant buttons use the vanilla tooltip system.
        for(Object w : widgets)
        {
            if(w instanceof ButtonWidget button && button.isMouseOver(mouseX, mouseY))
            {
                Tooltip t = button.getTooltip();
                if(t != null)
                    gui.drawOrderedTooltip(font, t.getLines(MinecraftClient.getInstance()), mouseX, mouseY);
            }
        }
    }

    private static abstract class BaseTooltip implements Supplier<Tooltip>
    {

        protected abstract Text getTooltip();

        @Override
        public Tooltip get() { return Tooltip.of(this.getTooltip()); }
    }

    public static class SimpleTooltip extends BaseTooltip
    {
        private final Text tooltip;
        public SimpleTooltip(Text tooltip) { this.tooltip = tooltip; }
        @Override
        protected Text getTooltip() { return this.tooltip; }
    }

    public static class SuppliedTooltip extends BaseTooltip
    {
        private final Supplier<Text> tooltipSource;
        public SuppliedTooltip(Supplier<Text> tooltipSource) { this.tooltipSource = tooltipSource; }
        @Override
        protected Text getTooltip() { return this.tooltipSource.get(); }
    }

    public static class AdditiveTooltip extends BaseTooltip
    {
        private final String translationKey;
        private final Supplier<Object[]> inputSource;
        public AdditiveTooltip(String translationKey, Supplier<Object[]> inputSource) { this.translationKey = translationKey; this.inputSource = inputSource; }
        @Override
        protected Text getTooltip() { return Text.translatable(translationKey, inputSource.get()); }
    }

    public static class ToggleTooltip extends BaseTooltip
    {
        private final Supplier<Boolean> toggleSource;
        private final Text trueTooltip;
        private final Text falseTooltip;
        public ToggleTooltip(Supplier<Boolean> toggleSource, Text trueTooltip, Text falseTooltip) {
            this.toggleSource = toggleSource;
            this.trueTooltip = trueTooltip;
            this.falseTooltip = falseTooltip;
        }
        @Override
        protected Text getTooltip() { return this.toggleSource.get() ? this.trueTooltip : this.falseTooltip; }
    }

    public static class ChangingTooltip extends BaseTooltip
    {
        private final Supplier<Integer> indicator;
        private final List<Text> tooltips;
        public ChangingTooltip(Supplier<Integer> indicator, Text... tooltips)
        {
            this.indicator = indicator;
            this.tooltips = Lists.newArrayList(tooltips);
        }

        @Override
        protected Text getTooltip() {
            int index = this.indicator.get();
            return this.tooltips.get(MathUtil.clamp(index, 0, this.tooltips.size() - 1));
        }

    }
}