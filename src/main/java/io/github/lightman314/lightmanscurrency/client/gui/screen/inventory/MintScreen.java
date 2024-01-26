package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.menu.MintMenu;

import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MintScreen extends MenuScreen<MintMenu>{

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/container/coinmint.png");

    public MintScreen(MintMenu container, PlayerInventory inventory, Text title)
    {
        super(container, inventory, title);
        this.backgroundHeight = 138;
        this.backgroundWidth = 176;
    }

    @Override
    protected void drawBackground(DrawContext gui, float partialTicks, int mouseX, int mouseY)
    {
        gui.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        gui.drawTexture(GUI_TEXTURE, this.x, this.y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        if(this.handler.coinMint == null)
            return;
        int blitWidth = MathUtil.clamp((int)(23 * this.handler.coinMint.getMintProgress()), 0, 22);
        gui.drawTexture(GUI_TEXTURE, this.x + 80, this.y + 21, 176, 0, blitWidth, 16);

    }

    @Override
    protected void drawForeground(DrawContext gui, int mouseX, int mouseY)
    {
        gui.drawText(this.textRenderer, this.title, 8, 6, 0x404040, false);
        gui.drawText( this.textRenderer, this.playerInventoryTitle, 8, this.backgroundHeight - 94, 0x404040, false);
    }

    @Override
    public void render(DrawContext gui, int mouseX, int mouseY, float delta) {
        this.renderBackground(gui);
        super.render(gui, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(gui, mouseX, mouseY);
    }
    
}