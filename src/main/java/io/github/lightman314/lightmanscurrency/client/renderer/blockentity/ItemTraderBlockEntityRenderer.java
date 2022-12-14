package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.client.LCConfigClient;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3f;

public class ItemTraderBlockEntityRenderer implements BlockEntityRenderer<ItemTraderBlockEntity> {

    public ItemTraderBlockEntityRenderer(BlockEntityRendererFactory.Context dispatcher) { }

    @Override
    public void render(ItemTraderBlockEntity blockEntity, float partialTicks, MatrixStack pose, VertexConsumerProvider buffer, int lightLevel, int id)
    {
        renderItems(blockEntity, partialTicks, pose, buffer, lightLevel, id);
    }

    public static List<ItemStack> GetRenderItems(ItemTradeData trade) {
        List<ItemStack> result = new ArrayList<>();
        for(int i = 0; i < 2; ++i)
        {
            ItemStack item = trade.getSellItem(i);
            if(!item.isEmpty())
                result.add(item);
        }
        return result;
    }

    public static void renderItems(ItemTraderBlockEntity blockEntity, float partialTicks, MatrixStack pose, VertexConsumerProvider buffer, int lightLevel, int id)
    {
        ItemTraderData trader = blockEntity.getTraderData();
        if(trader == null)
            return;
        for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < blockEntity.maxRenderIndex(); tradeSlot++)
        {

            ItemTradeData trade = trader.getTrade(tradeSlot);
            List<ItemStack> renderItems = GetRenderItems(trade);
            if(renderItems.size() > 0)
            {

                ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

                //Get positions
                List<Vec3f> positions = blockEntity.GetStackRenderPos(tradeSlot, renderItems.size() > 1);

                //Get rotation
                List<Quaternion> rotation = blockEntity.GetStackRenderRot(tradeSlot, partialTicks);

                //Get scale
                float scale = blockEntity.GetStackRenderScale(tradeSlot);

                for(int pos = 0; pos < LCConfigClient.INSTANCE.itemRenderLimit.get() && pos < positions.size() && pos < trader.getTradeStock(tradeSlot); pos++)
                {

                    pose.push();

                    Vec3f position = positions.get(pos);

                    //Translate, rotate, and scale the matrix stack
                    pose.translate(position.getX(), position.getY(), position.getZ());
                    for(Quaternion rot : rotation)
                    {
                        pose.multiply(rot);
                    }
                    pose.scale(scale, scale, scale);

                    //Render the item
                    if(renderItems.size() > 1)
                    {
                        //Render first item
                        pose.push();

                        //Don't base translation off of scale, as we've already been scaled down.
                        pose.translate(0.25, 0.25, 0d);
                        pose.scale(0.5f, 0.5f, 0.5f);

                        itemRenderer.renderItem(renderItems.get(0), ModelTransformation.Mode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, pose, buffer, id);

                        pose.pop();

                        //Render second item
                        pose.push();

                        //Slightly offset in the Z to prevent z-fighting if there's an overlap
                        pose.translate(-0.25, -0.25, 0.001d);
                        pose.scale(0.5f, 0.5f, 0.5f);

                        itemRenderer.renderItem(renderItems.get(1), ModelTransformation.Mode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, pose, buffer, id);

                        pose.pop();
                    }
                    else
                        itemRenderer.renderItem(renderItems.get(0), ModelTransformation.Mode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, pose, buffer, id);

                    pose.pop();

                }

            }

        }
    }


}