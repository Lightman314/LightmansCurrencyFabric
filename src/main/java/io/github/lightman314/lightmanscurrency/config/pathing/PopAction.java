package io.github.lightman314.lightmanscurrency.config.pathing;

public class PopAction implements INeutralConfigAction {

    private PopAction() { }
    @Override
    public void action(JsonStack json) { json.pop(); }

    public static final PopAction of() { return new PopAction(); }

}
