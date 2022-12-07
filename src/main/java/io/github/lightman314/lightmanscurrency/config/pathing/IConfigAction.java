package io.github.lightman314.lightmanscurrency.config.pathing;

public interface IConfigAction {

    void writeAction(JsonStack json);
    void readAction(JsonStack json);

}
