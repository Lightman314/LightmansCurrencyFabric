package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.google.common.base.Supplier;

import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.teams.Team;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TeamButton extends ButtonWidget {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/teambutton.png");


    public enum Size { WIDE(180, 0), NORMAL(156, 1), NARROW(90, 2);
        public final int width;
        public final int guiPos;
        Size(int width, int guiPos) {
            this.width = width;
            this.guiPos = guiPos * HEIGHT * 2;
        }
    }

    public static final int HEIGHT = 20;
    public static final int TEXT_COLOR = 0xFFFFFF;

    private final TextRenderer font;
    private final Size size;
    private final Supplier<Team> teamSource;
    public Team getTeam() { return this.teamSource.get(); }
    private final Supplier<Boolean> selectedSource;

    public TeamButton(int x, int y, Size size, PressAction press, TextRenderer font, @NotNull Supplier<Team> teamSource, @NotNull Supplier<Boolean> selectedSource)
    {
        super(x, y, size.width, HEIGHT, Text.empty(), press, DEFAULT_NARRATION_SUPPLIER);
        this.font = font;
        this.size = size;
        this.teamSource = teamSource;
        this.selectedSource = selectedSource;
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks)
    {
        if(this.getTeam() == null)
            return;

        //Render Background
        gui.setShaderColor(1f, 1f, 1f, 1f);

        gui.drawTexture(GUI_TEXTURE, this.getX(), this.getY(), 0, (selectedSource.get() ? HEIGHT : 0) + this.size.guiPos, this.size.width, HEIGHT);

        //Render Team Name
        gui.drawText(this.font, TextRenderUtil.fitString(this.getTeam().getName(), this.width - 4), this.getX() + 2, this.getY() + 2, TEXT_COLOR, false);
        //Render Owner Name
        gui.drawText(this.font, TextRenderUtil.fitString(Text.translatable("gui.button.lightmanscurrency.team.owner", this.getTeam().getOwner().getName(true)), this.width - 4), this.getX() + 2, this.getY() + 10, TEXT_COLOR, false);

        gui.setShaderColor(1f,1f,1f,1f);

    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        if(!this.visible || this.getTeam() == null)
            return;
        super.playDownSound(soundManager);
    }

}