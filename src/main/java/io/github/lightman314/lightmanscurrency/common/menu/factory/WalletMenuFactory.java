package io.github.lightman314.lightmanscurrency.common.menu.factory;

import io.github.lightman314.lightmanscurrency.common.menu.wallet.WalletMenu;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class WalletMenuFactory implements ExtendedScreenHandlerFactory {

    private final int walletSlot;
    public WalletMenuFactory(int walletSlot) { this.walletSlot = walletSlot; }

    @Nullable
    @Override
    public ScreenHandler createMenu(int windowID, PlayerInventory inventory, PlayerEntity player) { return new WalletMenu(windowID, inventory, this.walletSlot); }
    @Override
    public Text getDisplayName() { return Text.empty(); }
    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buffer) { buffer.writeInt(this.walletSlot); }

}
