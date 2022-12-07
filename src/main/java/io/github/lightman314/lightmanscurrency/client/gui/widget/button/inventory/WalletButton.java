package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.client.ClientEventListeners;
import io.github.lightman314.lightmanscurrency.client.LCConfigClient;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import io.github.lightman314.lightmanscurrency.network.server.messages.wallet.CMessageOpenWalletMenu;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

public class WalletButton extends InventoryButton {

    public WalletButton(HandledScreen<?> parent) {
        super(parent, 10, 10, b -> new CMessageOpenWalletMenu(-1).sendToServer(), ClientEventListeners.WALLET_SLOT_TEXTURE, 18, 0);
    }

    @Override
    protected @NotNull ScreenPosition getScreenPosition(ScreenPosition screenCorner, boolean isParentCreative) {
        return screenCorner
                .withOffset(isParentCreative ? LCConfigClient.INSTANCE.walletSlotCreative.get() : LCConfigClient.INSTANCE.walletSlot.get())
                .withOffset(LCConfigClient.INSTANCE.walletButtonOffset.get());
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTick) {
        if(shouldHide())
            return;
        super.render(matrixStack, mouseX, mouseY, partialTick);
    }

    private static boolean shouldHide() {
        MinecraftClient mc = MinecraftClient.getInstance();
        WalletHandler walletHandler = WalletHandler.getWallet(mc.player);
        return walletHandler.getWallet().isEmpty();
    }


}