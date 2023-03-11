package io.github.lightman314.lightmanscurrency.common.traders;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public interface ITraderSource {

    ITraderSource CLIENT_TRADER_SOURCE = new NetworkTraderSource(true);
    ITraderSource SERVER_TRADER_SOURCE = new NetworkTraderSource(false);

    ITraderSource NULL = new NullTraderBlockSource();

    @NotNull
    List<TraderData> getTraders();
    boolean isSingleTrader();
    default TraderData getSingleTrader() { return this.isSingleTrader() && this.getTraders().size() == 1 ? this.getTraders().get(0) : null; }

    static ITraderSource getSafeSource(Supplier<ITraderSource> source) { return new SafeTraderSource(source); }

    static ITraderSource UniversalTraderSource(boolean isClient) { return isClient ? CLIENT_TRADER_SOURCE : SERVER_TRADER_SOURCE; }



    class NetworkTraderSource implements ITraderSource
    {

        private final boolean isClient;
        private NetworkTraderSource(boolean isClient) { this.isClient = isClient; }

        @Override
        public @NotNull List<TraderData> getTraders() { return TraderSaveData.GetAllTerminalTraders(this.isClient); }
        @Override
        public boolean isSingleTrader() { return false; }

    }

    class SafeTraderSource implements ITraderSource
    {
        private final Supplier<ITraderSource> traderSource;
        private SafeTraderSource(Supplier<ITraderSource> traderSource) { this.traderSource = traderSource; }
        private ITraderSource getSource() {
            ITraderSource source = this.traderSource.get();
            return source == null ? NULL : source;
        }
        @Override
        public @NotNull List<TraderData> getTraders() { return this.getSource().getTraders(); }
        @Override
        public boolean isSingleTrader() { return this.getSource().isSingleTrader(); }
        @Override
        public TraderData getSingleTrader() { return this.getSource().getSingleTrader(); }
    }

    class NullTraderBlockSource implements ITraderSource
    {
        private NullTraderBlockSource() {}
        @Override
        public @NotNull List<TraderData> getTraders() { return new ArrayList<>(); }
        @Override
        public boolean isSingleTrader() { return false; }
    }

}