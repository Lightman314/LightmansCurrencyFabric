package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ItemIcon extends ATMIconData {

    public static final Identifier TYPE_NAME = new Identifier(LightmansCurrency.MODID, "item");
    public static final IconType TYPE = IconType.create(TYPE_NAME, ItemIcon::new);

    private final boolean simpleItem;
    private final ItemStack item;

    public ItemIcon(JsonObject data) throws RuntimeException {
        super(data);

        JsonElement itemData = data.get("item");
        if(itemData.isJsonPrimitive())
        {
            this.item = new ItemStack(Registry.ITEM.get(new Identifier(itemData.getAsString())));
            simpleItem = true;
        }
        else
        {
            try {
                this.item = FileUtil.parseItemStack(itemData.getAsJsonObject());
            } catch(Exception e) { throw new RuntimeException(e); }

            //Always force quantity 1
            this.item.setCount(1);
            simpleItem = false;
        }
    }

    public ItemIcon(int xPos, int yPos, ItemConvertible item) {
        super(xPos, yPos);
        this.item = new ItemStack(item);
        this.simpleItem = true;
    }


    @Override
    protected void saveAdditional(JsonObject data) {

        if(this.simpleItem)
            data.addProperty("item", Registry.ITEM.getId(this.item.getItem()).toString());
        else
            data.add("item", FileUtil.convertItemStack(this.item));
    }

    @Override
    protected Identifier getType() { return TYPE_NAME; }

    @Override
    @Environment(EnvType.CLIENT)
    public void render(ATMExchangeButton button, MatrixStack pose, boolean isHovered) {
        ItemRenderUtil.drawItemStack(button, null, this.item, button.x + this.xPos, button.y + this.yPos, "");
    }

}