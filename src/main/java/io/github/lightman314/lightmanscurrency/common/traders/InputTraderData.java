package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.input.InputTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.common.ownership.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.BooleanPermission;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class InputTraderData extends TraderData {

    public static MutableText getFacingName(Direction side) { return Text.translatable("gui.lightmanscurrency.settings.side." + side.toString().toLowerCase()); }

    public final ImmutableList<Direction> ignoreSides;
    private final Map<Direction,Boolean> inputSides = new HashMap<>();
    private final Map<Direction,Boolean> outputSides = new HashMap<>();

    @Override
    protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) {
        defaultValues.put(Permissions.InputTrader.EXTERNAL_INPUTS, 1);
    }

    protected InputTraderData(Identifier type) { this(type, ImmutableList.of()); }
    protected InputTraderData(Identifier type, ImmutableList<Direction> ignoreSides) { super(type); this.ignoreSides = ignoreSides; }
    protected InputTraderData(Identifier type, World level, BlockPos pos) { this(type, level, pos, ImmutableList.of()); }
    protected InputTraderData(Identifier type, World level, BlockPos pos, ImmutableList<Direction> ignoreSides) {
        super(type, level, pos);
        this.ignoreSides = ignoreSides;
    }

    public boolean allowInputSide(Direction side) {
        if(this.ignoreSides.contains(side))
            return false;
        return this.inputSides.getOrDefault(side, false);
    }

    public final boolean hasInputSide() {
        for(Direction side : Direction.values())
        {
            if(this.allowInputSide(side))
                return true;
        }
        return false;
    }

    public boolean allowOutputSide(Direction side) {
        if(this.ignoreSides.contains(side))
            return false;
        return this.outputSides.getOrDefault(side, false);
    }

    public final boolean hasOutputSide() {
        for(Direction side : Direction.values())
        {
            if(this.allowOutputSide(side))
                return true;
        }
        return false;
    }

    public void setInputSide(PlayerEntity player, Direction side, boolean value) {
        if(this.hasPermission(player, Permissions.InputTrader.EXTERNAL_INPUTS) && value != this.allowInputSide(side))
        {
            if(this.ignoreSides.contains(side))
                return;
            this.inputSides.put(side, value);
            this.markDirty(this::saveInputSides);

            if(player != null)
                this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Input-" + getFacingName(side).getString(), String.valueOf(this.allowInputSide(side))));
        }
    }

    public void setOutputSide(PlayerEntity player, Direction side, boolean value) {
        if(this.hasPermission(player, Permissions.InputTrader.EXTERNAL_INPUTS) && value != this.allowOutputSide(side))
        {
            if(this.ignoreSides.contains(side))
                return;
            this.outputSides.put(side, value);
            this.markDirty(this::saveOutputSides);

            if(player != null)
                this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Output-" + getFacingName(side).getString(), String.valueOf(this.allowOutputSide(side))));
        }
    }

    @Override
    protected void saveAdditional(NbtCompound compound) {
        this.saveInputSides(compound);
        this.saveOutputSides(compound);
    }

    protected final void saveInputSides(NbtCompound compound) {
        NbtCompound tag = new NbtCompound();
        for(Direction side : Direction.values())
        {
            if(this.ignoreSides.contains(side))
                continue;
            tag.putBoolean(side.toString(), this.allowInputSide(side));
        }
        compound.put("InputSides", tag);
    }

    protected final void saveOutputSides(NbtCompound compound) {
        NbtCompound tag = new NbtCompound();
        for(Direction side : Direction.values())
        {
            if(this.ignoreSides.contains(side))
                continue;
            tag.putBoolean(side.toString(), this.allowOutputSide(side));
        }
        compound.put("OutputSides", tag);
    }

    @Override
    protected void loadAdditional(NbtCompound compound) {
        if(compound.contains("InputSides"))
        {
            this.inputSides.clear();
            NbtCompound tag = compound.getCompound("InputSides");
            for(Direction side : Direction.values())
            {
                if(this.ignoreSides.contains(side))
                    continue;
                if(tag.contains(side.toString()))
                    this.inputSides.put(side, tag.getBoolean(side.toString()));
            }
        }

        if(compound.contains("OutputSides"))
        {
            this.outputSides.clear();
            NbtCompound tag = compound.getCompound("OutputSides");
            for(Direction side : Direction.values())
            {
                if(this.ignoreSides.contains(side))
                    continue;
                if(tag.contains(side.toString()))
                    this.outputSides.put(side, tag.getBoolean(side.toString()));
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public abstract IconData inputSettingsTabIcon();
    @Environment(EnvType.CLIENT)
    public abstract MutableText inputSettingsTabTooltip();
    @Environment(EnvType.CLIENT)
    public abstract int inputSettingsTabColor();
    @Environment(EnvType.CLIENT)
    public abstract int inputSettingsTextColor();
    @Environment(EnvType.CLIENT)
    public List<InputTabAddon> inputSettingsAddons() { return ImmutableList.of(); }

    @Override
    public void receiveNetworkMessage(PlayerEntity player, NbtCompound message)
    {
        super.receiveNetworkMessage(player, message);

        if(message.contains("SetInputSide"))
        {
            boolean newValue = message.getBoolean("SetInputSide");
            Direction side = Direction.byId(message.getInt("Side"));
            this.setInputSide(player, side, newValue);
        }
        if(message.contains("SetOutputSide"))
        {
            boolean newValue = message.getBoolean("SetOutputSide");
            Direction side = Direction.byId(message.getInt("Side"));
            this.setOutputSide(player, side, newValue);
        }
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void addSettingsTabs(List<SettingsTab> tabs) { tabs.add(InputTab.INSTANCE); }

    @Override
    @Environment(EnvType.CLIENT)
    public void addPermissionOptions(List<PermissionOption> options) { options.add(BooleanPermission.of(Permissions.InputTrader.EXTERNAL_INPUTS)); }

}