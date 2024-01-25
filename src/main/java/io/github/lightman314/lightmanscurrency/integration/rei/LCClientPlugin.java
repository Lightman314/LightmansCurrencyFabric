package io.github.lightman314.lightmanscurrency.integration.rei;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.crafting.CoinMintRecipe;
import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;
import io.github.lightman314.lightmanscurrency.integration.rei.coin_mint.CoinMintCategory;
import io.github.lightman314.lightmanscurrency.integration.rei.coin_mint.CoinMintClickArea;
import io.github.lightman314.lightmanscurrency.integration.rei.coin_mint.CoinMintDisplay;
import io.github.lightman314.lightmanscurrency.integration.rei.coin_mint.CoinMintTransferHandler;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandlerRegistry;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;
import me.shedaniel.rei.api.common.util.EntryStacks;

public class LCClientPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(CoinMintCategory.INSTANCE);
        registry.addWorkstations(CoinMintCategory.ID, EntryStacks.of(ModBlocks.MACHINE_MINT));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) { registry.registerFiller(CoinMintRecipe.class, CoinMintRecipe::shouldShowInREI, CoinMintDisplay::new); }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        registry.registerClickArea(CoinMintClickArea.INSTANCE, MintScreen.class, CoinMintCategory.ID);
    }

    @Override
    public void registerTransferHandlers(TransferHandlerRegistry registry) {
        registry.register(CoinMintTransferHandler.INSTANCE);
    }
}
