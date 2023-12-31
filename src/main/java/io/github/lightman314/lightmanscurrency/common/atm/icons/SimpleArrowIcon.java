package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class SimpleArrowIcon extends ATMIconData {

    public static final Identifier TYPE_NAME = new Identifier(LightmansCurrency.MODID, "small_arrow");
    public static final IconType TYPE = IconType.create(TYPE_NAME, SimpleArrowIcon::new);

    public enum ArrowType{
        UP(0),
        DOWN(6),
        LEFT(12),
        RIGHT(18);
        public final int uOffset;
        ArrowType(int uOffset) { this.uOffset = uOffset; }
        static ArrowType parse(String value) {
            for(ArrowType type : ArrowType.values())
            {
                if(type.name().equals(value))
                    return type;
            }
            return ArrowType.RIGHT;
        }
    }

    private final ArrowType direction;

    public SimpleArrowIcon(JsonObject data) {
        super(data);

        if(data.has("direction"))
            this.direction = ArrowType.parse(data.get("direction").getAsString());
        else
        {
            LightmansCurrency.LogWarning("Simple Arrow icon has no defined direction. Will assume it's pointing right.");
            this.direction = ArrowType.RIGHT;
        }
    }

    public SimpleArrowIcon(int xPos, int yPos, ArrowType direction) {
        super(xPos, yPos);
        this.direction = direction;
    }

    @Override
    protected void saveAdditional(JsonObject data) {

        data.addProperty("direction", this.direction.name());

    }

    @Override
    protected Identifier getType() { return TYPE_NAME; }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(ATMExchangeButton button, DrawContext gui, boolean isHovered) {
        RenderSystem.setShaderTexture(0, ATMScreen.BUTTON_TEXTURE);
        gui.setShaderColor(1f,1f,1f,1f);
        gui.drawTexture(ATMScreen.BUTTON_TEXTURE, button.getX() + this.xPos, button.getY() + this.yPos, this.direction.uOffset, ATMExchangeButton.HEIGHT * 2, 6, 6);
    }

}