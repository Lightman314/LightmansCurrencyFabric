package io.github.lightman314.lightmanscurrency.common.money;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;

public interface DefaultMoneyDataCollection {

    public final Event<DefaultMoneyDataCollection> EVENT = EventFactory.createArrayBacked(DefaultMoneyDataCollection.class,
            (listeners) -> (dataCollector) -> {
                for(DefaultMoneyDataCollection listener : listeners) {
                    listener.appendMoneyData(dataCollector);
                }
            });

    public void appendMoneyData(MoneyData.CoinDataCollector dataCollector);

}