package io.github.lightman314.lightmanscurrency.client.model;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;


public class ModelWallet<T extends LivingEntity> extends BipedEntityModel<T> {

    public final ModelPart wallet;

    public ModelWallet(ModelPart part)
    {
        super(part);
        this.wallet = part.getChild("wallet");
    }

    @Override
    protected Iterable<ModelPart> getHeadParts()
    {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> getBodyParts()
    {
        return ImmutableList.of(this.wallet);
    }

    @Override
    public void setAngles(@NotNull T t, float v, float v1, float v2, float v3, float v4)
    {

    }

}