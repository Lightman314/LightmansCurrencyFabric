package io.github.lightman314.lightmanscurrency.integration.rei.coin_mint;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.MintScreen;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.screen.SimpleClickArea;

public class CoinMintClickArea implements SimpleClickArea<MintScreen> {

    public static final CoinMintClickArea INSTANCE = new CoinMintClickArea();

    private CoinMintClickArea() {}

    @Override
    public Rectangle provide(MintScreen screen) {
        return new Rectangle(screen.getGuiLeft() + 80, screen.getGuiTop() + 21, 22, 16);
    }
}
