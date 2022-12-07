package io.github.lightman314.lightmanscurrency.config.pathing;

public interface INeutralConfigAction extends IConfigAction{
    @Override
    default void writeAction(JsonStack json) { this.action(json); }
    @Override
    default void readAction(JsonStack json) { this.action(json); }
    public void action(JsonStack json);
}
