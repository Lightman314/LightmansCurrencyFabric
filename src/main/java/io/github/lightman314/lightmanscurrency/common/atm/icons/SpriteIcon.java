package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class SpriteIcon extends ATMIconData {

    public static final Identifier TYPE_NAME = new Identifier(LightmansCurrency.MODID, "sprite");
    public static final IconType TYPE = IconType.create(TYPE_NAME, SpriteIcon::new);

    private final Identifier texture;
    private final int u;
    private final int v;
    private final int width;
    private final int height;

    public SpriteIcon(JsonObject data) {

        super(data);

        this.texture = new Identifier(data.get("texture").getAsString());
        this.u = data.get("u").getAsInt();
        this.v = data.get("v").getAsInt();
        this.width = data.get("width").getAsInt();
        this.height = data.get("height").getAsInt();

    }

    public SpriteIcon(int xPos, int yPos, Identifier texture, int u, int v, int width, int height) {
        super(xPos,yPos);
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void saveAdditional(JsonObject data) {

        data.addProperty("texture", this.texture.toString());
        data.addProperty("u", this.u);
        data.addProperty("v", this.v);
        data.addProperty("width", this.width);
        data.addProperty("height", this.height);

    }

    @Override
    protected Identifier getType() { return TYPE_NAME; }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(ATMExchangeButton button, DrawContext gui, boolean isHovered) {
        gui.setShaderColor(1f,1f,1f,1f);
        gui.drawTexture(this.texture, button.getX() + this.xPos, button.getY() + this.yPos, this.u, this.v, this.width, this.height);
    }

}