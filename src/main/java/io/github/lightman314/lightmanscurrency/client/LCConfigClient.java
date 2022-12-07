package io.github.lightman314.lightmanscurrency.client;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.config.Config;
import io.github.lightman314.lightmanscurrency.config.options.BooleanOption;
import io.github.lightman314.lightmanscurrency.config.options.custom.ScreenPositionOption;
import io.github.lightman314.lightmanscurrency.config.options.IntegerOption;

public class LCConfigClient extends Config {

    public static final LCConfigClient INSTANCE = new LCConfigClient(ConfigBuilder.create());

    //Render Options
    public final IntegerOption itemRenderLimit;

    //Wallet Slot Options
    public final ScreenPositionOption walletSlot;
    public final ScreenPositionOption walletSlotCreative;
    public final ScreenPositionOption walletButtonOffset;

    //Inventory Button Options
    public final ScreenPositionOption buttonGroup;
    public final ScreenPositionOption buttonGroupCreative;

    //Notification Settings
    public final BooleanOption pushNotificationsToChat;

    //Sound Options
    public final BooleanOption moneyMendingClink;

    private LCConfigClient(ConfigBuilder builder) {
        super(LightmansCurrency.MODID + "/config-client", builder);

        builder.comment("Quality & Lag Prevention Settings").push("quality");

        this.itemRenderLimit = builder.comment("Maximum number of items each Item Trader can render (per-trade) as stock. Lower to decrease client-lag in trader-rich areas.",
                "Setting to 0 will disable item rendering entirely, so use with caution.")
                .option("itemTraderRenderLimit", IntegerOption.create(Integer.MAX_VALUE, 0, Integer.MAX_VALUE));

        builder.pop();

        builder.comment("Wallet Slot Settings").push("wallet_slot");

        this.walletSlot = builder.comment("The position in the menu where the wallet slot will be placed at in the players inventory.")
                .option("slotX", ScreenPositionOption.create(76, 43, -1000, 1000));

        this.walletSlotCreative = builder.comment("The position in the menu where the wallet slot will be placed at in the players creative inventory.")
                .option("creativeSlotX", ScreenPositionOption.create(126, 19, -1000, 1000));

        this.walletButtonOffset = builder.comment("The offset that the wallet button should be placed at relative to the wallet slot position.")
                .option("buttonX", ScreenPositionOption.create(8, -10, -1000, 1000));

        builder.pop();

        builder.comment("Inventory Button Settings").push("inventory_buttons");

        this.buttonGroup = builder.comment("The position on the menu that the notification & team manager buttons will be placed at in the players inventory.")
                .option("buttonX", ScreenPositionOption.create(152, 3, -1000, 1000));

        this.buttonGroupCreative = builder.comment("The position on the menu that the notification & team manager buttons will be placed at in the players creative inventory.")
                .option("buttonCreativeX", ScreenPositionOption.create(171, 3, -1000, 1000));

        builder.pop();

        builder.comment("Notification Settings").push("notification");

        this.pushNotificationsToChat = builder.comment("Whether notifications should be posted in your chat when you receive them.")
                .option("notificationsInChat", BooleanOption.create(true));

        builder.pop();

        builder.comment("Sound Settings").push("sounds");

        this.moneyMendingClink = builder.comment("Whether Money Mending should make a noise when triggered.")
                .option("moneyMendingClink", BooleanOption.create(true));

        builder.pop();


        this.lock();
    }
}
