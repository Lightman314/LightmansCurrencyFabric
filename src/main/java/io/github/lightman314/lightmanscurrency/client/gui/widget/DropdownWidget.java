package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.DropdownButton;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class DropdownWidget extends ClickableWidget {

    public static final Identifier GUI_TEXTURE = new Identifier(LightmansCurrency.MODID, "textures/gui/dropdown.png");

    public static final int HEIGHT = 12;

    boolean open = false;

    int currentlySelected;

    private final TextRenderer font;
    private final List<Text> options;
    private final Consumer<Integer> onSelect;
    private final Function<Integer,Boolean> optionActive;

    List<ButtonWidget> optionButtons = new ArrayList<>();

    public DropdownWidget(int x, int y, int width, TextRenderer font, int selected, Consumer<Integer> onSelect, Function<ButtonWidget,ButtonWidget> addButton, Text... options) {
        this(x, y, width, font, selected, onSelect, (index) -> true, addButton, options);
    }

    public DropdownWidget(int x, int y, int width, TextRenderer font, int selected, Consumer<Integer> onSelect, Function<ButtonWidget,ButtonWidget> addButton, List<Text> options) {
        this(x, y, width, font, selected, onSelect, (index) -> true, addButton, options);
    }

    public DropdownWidget(int x, int y, int width, TextRenderer font, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, Function<ButtonWidget,ButtonWidget> addButton, Text... options) {
        this(x, y, width, font, selected, onSelect, optionActive, addButton, Lists.newArrayList(options));
    }

    public DropdownWidget(int x, int y, int width, TextRenderer font, int selected, Consumer<Integer> onSelect, Function<Integer,Boolean> optionActive, Function<ButtonWidget,ButtonWidget> addButton, List<Text> options) {
        super(x, y, width, HEIGHT, Text.empty());
        this.font = font;
        this.options = options;
        this.currentlySelected = MathUtil.clamp(selected, 0, this.options.size() - 1);
        this.onSelect = onSelect;
        this.optionActive = optionActive;
        //Init the buttons before this, so that they get pressed before this closes them on an offset click
        this.init(addButton);
    }

    private void init(Function<ButtonWidget,ButtonWidget> addButton) {
        this.optionButtons = new ArrayList<>();

        for(int i = 0; i < this.options.size(); ++i)
        {
            int yPos = this.getY() + HEIGHT + (i * HEIGHT);
            this.optionButtons.add(addButton.apply(new DropdownButton(this.getX(), yPos, this.width, this.font, this.options.get(i), this::OnSelect)));
            this.optionButtons.get(i).visible = this.open;
        }

    }

    @Override
    public void renderButton(DrawContext gui, int mouseX, int mouseY, float partialTicks) {

        //Draw the background
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);
        gui.setShaderColor(1f, 1f, 1f, 1f);
        int offset = this.hovered ? this.height : 0;
        if(!this.active)
            gui.setShaderColor(0.5F, 0.5F, 0.5F, 1.0F);
        gui.drawTexture(GUI_TEXTURE, this.getX(), this.getY(), 0, offset, 2, DropdownWidget.HEIGHT);
        int xOffset = 0;
        while(xOffset < this.width - 14)
        {
            int xPart = Math.min(this.width - 14 - xOffset, 244);
            gui.drawTexture(GUI_TEXTURE, this.getX() + 2 + xOffset, this.getY(), 2, offset, xPart, DropdownWidget.HEIGHT);
            xOffset += xPart;
        }
        gui.drawTexture(GUI_TEXTURE, this.getX() + this.width - 12, this.getY(), 244, offset, 12, DropdownWidget.HEIGHT);

        //Draw the option text
        gui.drawText(this.font, this.fitString(this.options.get(this.currentlySelected).getString()), this.getX() + 2, this.getY() + 2, 0x404040, false);

        //Confirm the option buttons active state
        if(this.open)
        {
            for(int i = 0; i < this.optionButtons.size(); ++i)
                this.optionButtons.get(i).active = this.optionActive.apply(i) && i != this.currentlySelected;
        }
        gui.setShaderColor(1f,1f,1f,1f);


    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int click) {
        if (this.active && this.visible) {
            if (this.clicked(mouseX, mouseY) && this.isValidClickButton(click)) {
                this.playDownSound(MinecraftClient.getInstance().getSoundManager());
                this.open = !this.open;
                this.optionButtons.forEach(button -> button.visible = this.open);
                return true;
            }
            else if(this.open)
            {
                this.open = false;
                this.optionButtons.forEach(button -> button.visible = false);
            }
            return false;
        } else {
            return false;
        }
    }

    private void OnSelect(ButtonWidget button) {
        int index = this.optionButtons.indexOf(button);
        if(index < 0)
            return;
        this.currentlySelected = index;
        this.onSelect.accept(index);
        this.open = false;
        this.optionButtons.forEach(b -> b.visible = false);
    }

    @Override
    public void appendClickableNarrations(NarrationMessageBuilder builder) { }

    private String fitString(String text) {
        if(this.font.getWidth(text) <= this.width - 14)
            return text;
        while(this.font.getWidth(text + "...") > this.width - 14 && text.length() > 0)
        {
            text = text.substring(0, text.length() - 1);
        }
        return text + "...";
    }

}