package io.github.lightman314.lightmanscurrency.config.pathing;

public class CommentAction implements IConfigAction {

    private final String path;
    private final String comment;
    private CommentAction(String path, String comment) { this.path = path; this.comment = comment; }

    public static final CommentAction of(String path, String comment) { return new CommentAction(path, comment); }

    @Override
    public void writeAction(JsonStack json) { json.get().addProperty(this.path, this.comment); }
    @Override
    public void readAction(JsonStack json) { }

}
