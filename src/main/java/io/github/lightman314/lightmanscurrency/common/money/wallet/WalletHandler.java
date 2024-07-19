package io.github.lightman314.lightmanscurrency.common.money.wallet;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.WalletSlot;
import io.github.lightman314.lightmanscurrency.integration.trinketsapi.LCTrinketsAPI;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import org.jetbrains.annotations.NotNull;

public class WalletHandler{

    private boolean invalidated = false;
    private boolean wasInvalid = false;

    //Wallet Storage
    private ItemStack wallet = ItemStack.EMPTY;
    private ItemStack oldWallet = ItemStack.EMPTY;

    //Wallet Visibility
    private boolean visible = true;
    private boolean wasVisible = true;
    private PlayerEntity latestPlayer = null;

    public WalletHandler updatePlayer(@NotNull PlayerEntity player) { this.latestPlayer = player; return this; }

    /**
     * The currently equipped wallet on the player.
     */
    public ItemStack getWallet() {
        if(LCTrinketsAPI.isValid(this.latestPlayer))
            return LCTrinketsAPI.getWallet(this.latestPlayer);
        return this.wallet;
    }

    /**
     * Sets the currently equipped wallet on the player.
     */
    public void setWallet(ItemStack walletStack) {
        if(LCTrinketsAPI.isValid(this.latestPlayer) && LCTrinketsAPI.setWallet(this.latestPlayer,walletStack))
            return;
        //Flag as no-longer invalid if trinkets cannot be found
        this.invalidated = false;
        this.wallet = walletStack;
        if(!(walletStack.getItem() instanceof WalletItem) && !walletStack.isEmpty())
            LightmansCurrency.LogWarning("Equipped a non-wallet to the players wallet slot.");
    }

    /**
     * Whether the wallet should be rendered
     */
    public boolean visible() {
        if(LCTrinketsAPI.isValid(this.latestPlayer))
            return true;
        return this.visible;
    }

    /**
     *
     */
    public void setVisible(boolean visible) { this.visible = visible; }

    /**
     * Returns true if the wallet has been changed, and needs to send an update packet
     */
    public boolean isDirty() { return this.wasInvalid != this.invalidated || this.visible != this.wasVisible || !InventoryUtil.ItemMatches(this.wallet, this.oldWallet) || this.wallet.getCount() != this.oldWallet.getCount(); }

    /**
     * Removes the dirty flag, called when an update packet is sent.
     */
    public void clean() { this.wasInvalid = this.invalidated; this.wasVisible = this.visible; this.oldWallet = this.wallet.copy(); }

    /**
     * Run every server tick.
     */
    public void tick() {
        if(this.invalidated || this.latestPlayer == null)
            return;
        if(!this.wallet.isEmpty() && LCTrinketsAPI.isValid(this.latestPlayer))
        {
            if(LCTrinketsAPI.setWallet(this.latestPlayer, this.wallet))
            {
                this.wallet = ItemStack.EMPTY;
                this.invalidated = true;
            }
        }
    }

    /**
     * Save the nbt data to file
     */
    public NbtCompound save() {
        NbtCompound compound = new NbtCompound();
        if(this.invalidated)
        {
            compound.putBoolean("Invalidated", true);
            return compound;
        }
        NbtCompound walletTag = this.wallet.writeNbt(new NbtCompound());
        compound.put("Wallet", walletTag);
        compound.putBoolean("Visible", this.visible);
        return compound;
    }

    /**
     * Load the nbt data from file
     */
    public void load(NbtCompound compound)
    {
        if(compound.contains("Invalidated") && compound.getBoolean("Invalidated"))
        {
            this.invalidated = true;
            this.wallet = ItemStack.EMPTY;
            this.visible = false;
            return;
        }
        this.wallet = ItemStack.fromNbt(compound.getCompound("Wallet"));
        this.visible = compound.getBoolean("Visible");
        this.clean();
    }

    @NotNull
    public static WalletHandler getWallet(@NotNull PlayerEntity player) { return WalletSaveData.GetPlayerWallet(player); }

    public static void WalletSlotInteraction(PlayerEntity player, int clickedSlot, boolean heldShift, ItemStack heldItem)
    {

        LightmansCurrency.LogDebug("Wallet Slot interaction for slot " + clickedSlot + " (shift " + (heldShift ? "held" : "not held") + ") on the " + DebugUtil.getSideText(player));
        ScreenHandler menu = player.currentScreenHandler;
        if(menu == null)
            return;
        boolean creative = player.isCreative() && !player.getWorld().isClient;
        if(!creative)
            heldItem = menu.getCursorStack();
        WalletHandler walletHandler = getWallet(player);
        if(clickedSlot < 0)
        {
            //Wallet slot clicked
            ItemStack wallet = walletHandler.getWallet();
            if(heldShift)
            {
                //Quick-move the wallet to the players inventory
                if(wallet.isEmpty())
                    return;
                //If we were able to move the wallet into the players inventory, empty the wallet slot
                if(player.getInventory().getEmptySlot() >= 0)
                {
                    if(!creative)
                        player.getInventory().insertStack(wallet);
                    walletHandler.setWallet(ItemStack.EMPTY);
                    //LightmansCurrency.LogInfo("Successfully moved the wallet into the players inventory on the " + DebugUtil.getSideText(player));
                }
            }
            else
            {
                //Swap the held item with the wallet item
                if(wallet.isEmpty() && heldItem.isEmpty())
                    return;
                if(WalletSlot.isValidWallet(heldItem) || heldItem.isEmpty())
                {
                    walletHandler.setWallet(heldItem);
                    if(!creative)
                        menu.setCursorStack(wallet);
                }
            }
        }
        else if(heldShift)
        {
            PlayerInventory inventory = player.getInventory();
            //Try to shift-click the hovered slot into the wallet slot
            if(clickedSlot >= inventory.size())
            {
                //LightmansCurrency.LogWarning("Clicked on slot " + clickedSlot + " of " + player.getInventory().size() + " on the " + DebugUtil.getSideText(player));
                return;
            }
            ItemStack slotItem = inventory.getStack(clickedSlot);
            if(WalletSlot.isValidWallet(slotItem) && walletHandler.getWallet().isEmpty())
            {
                //Remove the item from inventory
                if(!creative)
                {
                    if(slotItem.getCount() > 1)
                        inventory.removeStack(clickedSlot, 1);
                    else
                        inventory.setStack(clickedSlot, ItemStack.EMPTY);
                }
                //Move the wallet into the wallet slot
                ItemStack newWallet = slotItem.copy();
                newWallet.setCount(1);
                walletHandler.setWallet(newWallet);
            }
        }

    }

}