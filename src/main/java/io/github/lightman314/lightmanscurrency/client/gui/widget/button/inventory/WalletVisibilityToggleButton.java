package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.ClientEventListeners;
import io.github.lightman314.lightmanscurrency.client.LCConfigClient;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import io.github.lightman314.lightmanscurrency.config.options.custom.values.ScreenPosition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.NotNull;

public class WalletVisibilityToggleButton extends InventoryButton {

    public static final int U_OFFSET = 28;
    public static final int V_OFFSET = 0;

    public static final int SIZE = 6;

    public WalletVisibilityToggleButton(HandledScreen<?> parent) {
        super(parent, SIZE, SIZE, b -> ClientEventListeners.toggleWalletVisibility(), ClientEventListeners.WALLET_SLOT_TEXTURE, WalletVisibilityToggleButton::getIconPos);
    }

    private static boolean isWalletVisible() {
        return WalletHandler.getWallet(MinecraftClient.getInstance().player).visible();
    }

    private static Pair<Integer,Integer> getIconPos() { return Pair.of(isWalletVisible() ? U_OFFSET + SIZE : U_OFFSET, V_OFFSET); }

    @Override
    protected @NotNull ScreenPosition getScreenPosition(ScreenPosition screenCorner, boolean isParentCreative) {
        return screenCorner
                .withOffset(isParentCreative ? LCConfigClient.INSTANCE.walletSlotCreative.get() : LCConfigClient.INSTANCE.walletSlot.get());
    }

    @Override
    public void render(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
        //Change icon based on the current visibility state
        this.setResource(ClientEventListeners.WALLET_SLOT_TEXTURE, U_OFFSET + (isWalletVisible() ? SIZE : 0), V_OFFSET);
        super.render(pose, mouseX, mouseY, partialTicks);
    }



}