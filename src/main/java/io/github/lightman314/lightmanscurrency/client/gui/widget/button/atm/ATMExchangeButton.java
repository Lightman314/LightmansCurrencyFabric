package io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ATMScreen;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMConversionButtonData;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ATMExchangeButton extends ButtonWidget {

    public static final int HEIGHT = 18;

    private final ATMConversionButtonData data;

    public ATMExchangeButton(int left, int top, ATMConversionButtonData data, Consumer<String> commandHandler) {
        super(left + data.xPos, top + data.yPos, data.width, HEIGHT, Text.empty(), b -> commandHandler.accept(data.command), DEFAULT_NARRATION_SUPPLIER);
        this.data = data;
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        //Render background to width
        int yOffset = this.hovered ? HEIGHT : 0;
        if(this.active)
            gui.setShaderColor(1f, 1f, 1f, 1f);
        else
            gui.setShaderColor(0.5f, 0.5f, 0.5f, 1f);
        //Draw the left edge
        gui.drawTexture(ATMScreen.BUTTON_TEXTURE, this.getX(), this.getY(), 0, yOffset, 2, HEIGHT);
        //Draw the middle portions
        int xPos = 2;
        while(xPos < this.width - 2)
        {
            int xSize = Math.min(this.width - 2 - xPos, 252);
            gui.drawTexture(ATMScreen.BUTTON_TEXTURE, this.getX() + xPos, this.getY(), 2, yOffset, xSize, HEIGHT);
            xPos += xSize;
        }
        //Draw the right edge
        gui.drawTexture(ATMScreen.BUTTON_TEXTURE, this.getX() + this.width - 2, this.getY(), 254, yOffset, 2, HEIGHT);

        //Draw the icons
        for(ATMIconData icon : this.data.getIcons())
        {
            try {
                icon.render(this, gui, this.hovered);
            } catch(Exception e) { LightmansCurrency.LogError("Error rendering ATM Conversion Button icon.", e); }
        }

        gui.setShaderColor(1f,1f,1f,1f);

    }

}