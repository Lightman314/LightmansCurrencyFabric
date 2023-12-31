package io.github.lightman314.lightmanscurrency.client.gui.widget.notifications;

import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NotificationDisplayWidget extends ClickableWidget implements IScrollable {

    public static final Identifier GUI_TEXTURE =  new Identifier(LightmansCurrency.MODID, "textures/gui/notifications.png");

    public static final int HEIGHT_PER_ROW = 22;

    private final Supplier<List<Notification>> notificationSource;
    private final TextRenderer font;
    private final int rowCount;
    public boolean colorIfUnseen = false;
    public int backgroundColor = 0xFFC6C6C6;

    public static int CalculateHeight(int rowCount) { return rowCount * HEIGHT_PER_ROW; }

    private List<Notification> getNotifications() { return this.notificationSource.get(); }

    Text tooltip = null;

    public NotificationDisplayWidget(int x, int y, int width, int rowCount, TextRenderer font, Supplier<List<Notification>> notificationSource) {
        super(x, y, width, CalculateHeight(rowCount), Text.empty());
        this.notificationSource = notificationSource;
        this.font = font;
        this.rowCount = rowCount;
    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks) {
        this.validateScroll();

        this.tooltip = null;

        List<Notification> notifications = this.getNotifications();
        int index = this.scroll;

        gui.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, this.backgroundColor);

        for(int y = 0; y < this.rowCount && index < notifications.size(); ++y)
        {
            int yPos = this.getY() + y * HEIGHT_PER_ROW;
            Notification n = notifications.get(index++);

            //Draw the background
            gui.setShaderColor(1f, 1f, 1f, 1f);
            int vPos = n.wasSeen() && this.colorIfUnseen ? 222 : 200;
            gui.drawTexture(GUI_TEXTURE, this.getX(), yPos, 0, vPos, 2, HEIGHT_PER_ROW);
            int xPos = this.getX() + 2;
            while(xPos < this.getX() + this.width - 2)
            {
                int thisWidth = Math.min(166, this.getX() + this.width - 2 - xPos);
                gui.drawTexture(GUI_TEXTURE, xPos, yPos, 2, vPos, thisWidth, HEIGHT_PER_ROW);
                xPos += thisWidth;
            }
            gui.drawTexture(GUI_TEXTURE, this.getX() + this.width - 2, yPos, 168, 200, 2, HEIGHT_PER_ROW);

            //Draw the text
            int textXPos = this.getX() + 2;
            int textWidth = this.width - 4;
            int textColor = n.wasSeen() ? 0xFFFFFF : 0x000000;
            if(n.getCount() > 1)
            {
                //Render quantity text
                String countText = String.valueOf(n.getCount());
                int quantityWidth = this.font.getWidth(countText);
                gui.drawTexture(GUI_TEXTURE, this.getX() + 1 + quantityWidth, yPos, 170, vPos, 3, HEIGHT_PER_ROW);

                gui.drawText(this.font, countText, textXPos, yPos + (HEIGHT_PER_ROW / 2) - (this.font.fontHeight / 2), textColor, false);

                textXPos += quantityWidth + 2;
                textWidth -= quantityWidth + 2;
            }

            Text message = n.getMessage();
            List<OrderedText> lines = this.font.wrapLines(message, textWidth);
            if(lines.size() == 1)
            {
                gui.drawText(this.font, lines.get(0), textXPos, yPos + (HEIGHT_PER_ROW / 2) - (this.font.fontHeight / 2), textColor, false);
            }
            else
            {
                for(int l = 0; l < lines.size() && l < 2; ++l)
                    gui.drawText(this.font, lines.get(l), textXPos, yPos + 2 + l * 10, textColor, false);
                if(lines.size() > 2 && this.tooltip == null && mouseX >= this.getX() && mouseX < this.getX() + this.width && mouseY >= yPos && mouseY < yPos + HEIGHT_PER_ROW)
                    this.tooltip = message;
            }

        }

        gui.setShaderColor(1f,1f,1f,1f);

    }

    public void tryRenderTooltip(DrawContext gui, int mouseX, int mouseY)
    {
        if(this.tooltip != null)
        {
            gui.drawOrderedTooltip(this.font, this.font.wrapLines(this.tooltip, this.width), mouseX, mouseY);
            this.tooltip = null;
        }
    }

    private int scroll = 0;

    @Override
    public int currentScroll() { return this.scroll; }

    @Override
    public void setScroll(int newScroll) {
        this.scroll = newScroll;
        this.validateScroll();
    }

    private void validateScroll() { this.scroll = MathUtil.clamp(this.scroll, 0, this.getMaxScroll()); }

    @Override
    public int getMaxScroll() { return Math.max(0, this.getNotifications().size() - this.rowCount); }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) { }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if(scroll < 0)
            this.setScroll(this.scroll + 1);
        else if(scroll > 0)
            this.setScroll(this.scroll - 1);
        return true;
    }

    public void playDownSound(SoundManager manager) {}

}