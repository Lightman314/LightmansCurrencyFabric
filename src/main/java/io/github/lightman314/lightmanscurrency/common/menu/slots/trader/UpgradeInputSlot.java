package io.github.lightman314.lightmanscurrency.common.menu.slots.trader;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.menu.slots.SimpleSlot;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;

public class UpgradeInputSlot extends SimpleSlot {

    public static final Identifier EMPTY_UPGRADE_SLOT = new Identifier(LightmansCurrency.MODID, "item/empty_upgrade_slot");

    private final UpgradeType.IUpgradeable machine;
    private final Runnable onModified;

    public UpgradeInputSlot(Inventory inventory, int index, int x, int y, UpgradeType.IUpgradeable machine) { this(inventory, index, x, y, machine, () -> {}); }

    public UpgradeInputSlot(Inventory inventory, int index, int x, int y, UpgradeType.IUpgradeable machine, Runnable onModified)
    {
        super(inventory, index, x, y);
        this.machine = machine;
        this.onModified = onModified;
    }

    @Override
    public boolean canInsert(ItemStack stack)
    {
        Item item = stack.getItem();
        if(item instanceof UpgradeItem)
            return machine.allowUpgrade((UpgradeItem)item);
        return false;
    }

    @Override
    public int getMaxItemCount() { return 1; }

    @Override
    public void markDirty() {
        super.markDirty();
        if(this.onModified != null)
            this.onModified.run();
    }

    @Override
    public Pair<Identifier,Identifier> getBackgroundSprite() { return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_UPGRADE_SLOT); }

}