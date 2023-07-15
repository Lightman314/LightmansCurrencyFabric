package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.TradeData;

public interface InteractionConsumer {
    void onTradeButtonInputInteraction(TraderData trader, TradeData trade, int index, int mouseButton);
    void onTradeButtonOutputInteraction(TraderData trader, TradeData trade, int index, int mouseButton);
    void onTradeButtonInteraction(TraderData trader, TradeData trade, int localMouseX, int localMouseY, int mouseButton);
}
