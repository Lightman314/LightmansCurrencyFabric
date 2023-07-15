package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import io.github.lightman314.lightmanscurrency.common.blockentity.trader.FreezerTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class FreezerTraderBlockEntityRenderer implements BlockEntityRenderer<FreezerTraderBlockEntity> {

    public static final Item doorItem = ModItems.FREEZER_DOOR;

    public FreezerTraderBlockEntityRenderer(BlockEntityRendererFactory.Context context) { }

    @Override
    public void render(FreezerTraderBlockEntity tileEntity, float partialTicks, MatrixStack poseStack, VertexConsumerProvider buffer, int lightLevel, int id)
    {

        //Render the items using the default method
        ItemTraderBlockEntityRenderer.renderItems(tileEntity, partialTicks, poseStack, buffer, lightLevel, id);

        //Render the door
        poseStack.push();
        Vector3f corner = new Vector3f(0f,0f,0f);
        Vector3f right = new Vector3f(1f, 0f, 0f);
        Vector3f forward = new Vector3f(0f, 0f, 1f);
        Block freezerBlock = tileEntity.getCachedState().getBlock();
        Direction facing = Direction.SOUTH;
        if(freezerBlock instanceof IRotatableBlock block)
        {
            facing = block.getFacing(tileEntity.getCachedState());
            corner = IRotatableBlock.getOffsetVect(facing);
            right = IRotatableBlock.getRightVect(facing);
            forward = IRotatableBlock.getForwardVect(facing);
        }
        //Calculate the hinge position
        Vector3f hinge = MathUtil.VectorAdd(corner, MathUtil.VectorMult(right, 15.5f/16f), MathUtil.VectorMult(forward, 3.5f/16f));

        Quaternionf rotation = MathUtil.getRotationDegrees(facing.getHorizontal() * -90f + (90f * tileEntity.getDoorAngle(partialTicks)));

        poseStack.translate(hinge.x(), hinge.y(), hinge.z());
        poseStack.multiply(rotation);

        ItemStack stack = new ItemStack(doorItem);
        MinecraftClient.getInstance().getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED, lightLevel, OverlayTexture.DEFAULT_UV, poseStack, buffer, tileEntity.getWorld(), id);

        poseStack.pop();

    }

}