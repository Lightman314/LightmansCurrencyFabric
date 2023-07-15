package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.ClientEventListeners;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.money.wallet.WalletHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
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
                .offset(isParentCreative ? LCConfig.CLIENT.walletSlotCreative.get() : LCConfig.CLIENT.walletSlot.get());
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        //Change icon based on the current visibility state
        this.setResource(ClientEventListeners.WALLET_SLOT_TEXTURE, U_OFFSET + (isWalletVisible() ? SIZE : 0), V_OFFSET);
        super.render(gui, mouseX, mouseY, partialTicks);
    }



}