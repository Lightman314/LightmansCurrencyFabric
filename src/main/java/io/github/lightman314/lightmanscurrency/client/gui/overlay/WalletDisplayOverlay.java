package io.github.lightman314.lightmanscurrency.client.gui.overlay;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class WalletDisplayOverlay {

    public static final WalletDisplayOverlay INSTANCE = new WalletDisplayOverlay();
    private WalletDisplayOverlay() {}

    private boolean sendError = true;

    public enum DisplayType { ITEMS_WIDE, ITEMS_NARROW, TEXT }

    public static void setup() { HudRenderCallback.EVENT.register(INSTANCE::render); }

    private void render(DrawContext gui, float partialTick)
    {
        if(!LCConfig.CLIENT.walletOverlayEnabled.get())
            return;

        try {

            ScreenCorner corner = LCConfig.CLIENT.walletOverlayCorner.get();
            ScreenPosition offset = LCConfig.CLIENT.walletOverlayPosition.get();

            int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

            ScreenPosition currentPosition = corner.getCorner(screenWidth, screenHeight).offset(offset);
            if(corner.isRightSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(-16,0));
            if(corner.isBottomSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(0, -16));

            //Draw the wallet
            WalletHandler walletHandler = WalletHandler.getWallet(MinecraftClient.getInstance().player);
            ItemStack wallet = walletHandler.getWallet();
            if(WalletItem.isWallet(wallet))
            {
                //Draw the wallet
                ItemRenderUtil.drawItemStack(gui, null, wallet, currentPosition.x, currentPosition.y);
                if(corner.isRightSide)
                    currentPosition = currentPosition.offset(ScreenPosition.of(-17,0));
                else
                    currentPosition = currentPosition.offset(ScreenPosition.of(17,0));

                CoinValue contents = MoneyUtil.getCoinValue(WalletItem.getWalletInventory(wallet));

                if(!contents.hasAny())
                    return;

                //Draw the stored money
                switch(LCConfig.CLIENT.walletOverlayType.get())
                {
                    case ITEMS_NARROW,ITEMS_WIDE -> {
                        int offsetAmount = LCConfig.CLIENT.walletOverlayType.get() == DisplayType.ITEMS_WIDE ? 17 : 9;
                        List<ItemStack> walletContents = MoneyUtil.getCoinsOfValue(contents);
                        for(ItemStack coin : walletContents)
                        {
                            ItemRenderUtil.drawItemStack(gui, null, coin, currentPosition.x, currentPosition.y);
                            if(corner.isRightSide)
                                currentPosition = currentPosition.offset(ScreenPosition.of(-offsetAmount,0));
                            else
                                currentPosition = currentPosition.offset(ScreenPosition.of(offsetAmount,0));
                        }
                    }
                    case TEXT -> {
                        TextRenderer font = MinecraftClient.getInstance().textRenderer;
                        Text walletText = contents.getComponent();
                        ScreenPosition pos;
                        if(corner.isRightSide)
                            pos = currentPosition.offset(-1 * font.getWidth(walletText), 3);
                        else
                            pos = currentPosition.offset(0,3);
                        gui.drawText(font, walletText, pos.x, pos.y, 0xFFFFFF, false);
                    }
                }

            }
        } catch (Throwable t) {
            if(this.sendError)
            {
                this.sendError = false;
                LightmansCurrency.LogError("Error occurred while rendering wallet overlay!", t);
            }
        }
    }


}
