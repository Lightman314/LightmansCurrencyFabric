package io.github.lightman314.lightmanscurrency.common.notifications.categories;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.traders.auction.AuctionHouseTrader;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class AuctionHouseCategory extends NotificationCategory {

    public static final Identifier TYPE = new Identifier(LightmansCurrency.MODID,"auction_house");

    public static final AuctionHouseCategory INSTANCE = new AuctionHouseCategory();

    private AuctionHouseCategory() { }

    @Override
    public @NotNull IconData getIcon() { return AuctionHouseTrader.ICON; }

    @Override
    public MutableText getName() { return Text.translatable("gui.lightmanscurrency.universaltrader.auction"); }

    @Override
    public Identifier getType() { return TYPE; }

    @Override
    public boolean matches(NotificationCategory other) { return other == INSTANCE; }

    public void saveAdditional(NbtCompound compound) { }


}