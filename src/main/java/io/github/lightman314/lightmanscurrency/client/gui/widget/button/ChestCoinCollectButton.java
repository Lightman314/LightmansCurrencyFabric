package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CPacketChestQuickCollect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class ChestCoinCollectButton extends IconButton {

    private final GenericContainerScreen screen;

    public ChestCoinCollectButton(GenericContainerScreen screen) {
        super(0,0,b -> CPacketChestQuickCollect.send(), ChestCoinCollectButton::getIcon, new IconAndButtonUtil.SimpleTooltip(EasyText.translatable("tooltip.button.chest.coin_collection")));
        this.screen = screen;
        //Position in the top-right corner
        ScreenArea area = ScreenUtil.getScreenArea(this.screen);
        this.setPosition(area.x + area.width - this.width, area.y - this.height);
    }

    private static IconData getIcon() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc != null)
            return IconData.of(WalletHandler.getWallet(mc.player).getWallet());
        return IconData.BLANK;
    }

    private boolean shouldBeVisible()
    {
        if(!LCConfig.CLIENT.chestButtonVisible.get())
            return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc != null)
        {
            ItemStack wallet = WalletHandler.getWallet(mc.player).getWallet();
            if(WalletItem.isWallet(wallet))
            {
                final boolean allowHidden = LCConfig.CLIENT.chestButtonAllowSideChains.get();
                //Check menu inventory for coins
                Inventory container = this.screen.getScreenHandler().getInventory();
                for(int i = 0; i < container.size(); ++i)
                {
                    if(MoneyUtil.isCoin(container.getStack(i), allowHidden))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        this.visible = this.shouldBeVisible();
        super.render(gui, mouseX, mouseY, partialTicks);
    }
}
