package io.github.lightman314.lightmanscurrency.integration.rei.coin_mint;

import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;
import io.github.lightman314.lightmanscurrency.network.util.BlockEntityUtil;
import me.shedaniel.rei.api.client.registry.transfer.TransferHandler;
import me.shedaniel.rei.api.client.registry.transfer.simple.SimpleTransferHandler;

public class CoinMintTransferHandler implements TransferHandler {

    public static final TransferHandler INSTANCE = new CoinMintTransferHandler(SimpleTransferHandler.create(
            MintMenu.class,
            CoinMintCategory.ID,
            new SimpleTransferHandler.IntRange(0,1)
    ));

    private final TransferHandler parent;

    protected CoinMintTransferHandler(TransferHandler parent) { this.parent = parent; }

    @Override
    public Result handle(Context context) {

        Result result = this.parent.handle(context);

        //Force the client to re-check the recipes so that it can display the progress correctly.
        if(context.getMenu() instanceof MintMenu menu && menu.coinMint != null)
            menu.coinMint.checkRecipes();

        return result;
    }

    @Override
    public ApplicabilityResult checkApplicable(Context context) {
        ApplicabilityResult result = this.parent.checkApplicable(context);
        //Disable for recipes that are visible but disabled in the config
        if(result.isApplicable() && context.getDisplay() instanceof CoinMintDisplay display && display.recipe.allowed())
            return result;
        else
            return ApplicabilityResult.createNotApplicable();
    }

}
