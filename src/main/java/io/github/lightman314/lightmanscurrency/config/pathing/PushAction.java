package io.github.lightman314.lightmanscurrency.config.pathing;

public class PushAction implements INeutralConfigAction {

    private final String path;
    private PushAction(String path) { this.path = path; }
    public static final PushAction of(String path) { return new PushAction(path); }

    @Override
    public void action(JsonStack json) { json.push(this.path); }


}
