package io.github.lightman314.lightmanscurrency.common.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.LCConfigCommon;
import io.github.lightman314.lightmanscurrency.common.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.config.options.custom.IdentifierListOption;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.fabricmc.fabric.api.loot.v2.LootTableSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.KilledByPlayerLootCondition;
import net.minecraft.loot.condition.RandomChanceWithLootingLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.LootingEnchantLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LootManager {

    public static final float LOOTING_MODIFIER = 0.01f;

    public enum PoolLevel
    {
        COPPER(0, true),
        IRON(1, true),
        GOLD(2, true),
        EMERALD(3, true),
        DIAMOND(4, true),
        NETHERITE(5, true),
        BOSS_COPPER(6, false),
        BOSS_IRON(7, false),
        BOSS_GOLD(8, false),
        BOSS_EMERALD(9, false),
        BOSS_DIAMOND(10, false),
        BOSS_NETHERITE(11, false);

        public final int level;
        private final boolean requiresPlayerKill;
        public final boolean requiresPlayerKill() { return this.requiresPlayerKill; }

        private PoolLevel(int level, boolean requiresPlayerKill) { this.level = level; this.requiresPlayerKill = requiresPlayerKill; }

    };

    public static boolean isValidSpawnReason(String reasonString)
    {
        for(SpawnReason reason : SpawnReason.values())
        {
            if(reason.toString() == reasonString)
                return true;
        }
        return false;
    }

    public static SpawnReason deserializeSpawnReason(String reasonString)
    {
        return deserializeSpawnReason(reasonString, SpawnReason.NATURAL);
    }

    public static SpawnReason deserializeSpawnReason(String reasonString, SpawnReason defaultReason)
    {
        for(SpawnReason reason : SpawnReason.values())
        {
            if(reason.toString().contentEquals(reasonString))
                return reason;
        }
        LightmansCurrency.LogWarning("Reason string \"" + reasonString + "\" could not be properly deserialized. Returning the default spawn reason.");
        return defaultReason;
    }

    public static boolean containsReason(List<? extends String> reasonList, SpawnReason reason)
    {
        for(int i = 0; i < reasonList.size(); ++i)
        {
            if(reason.toString().contentEquals(reasonList.get(i)))
                return true;
        }
        return false;
    }

    public static String getSpawnReasonList()
    {
        StringBuffer output = new StringBuffer();
        for(SpawnReason reason : SpawnReason.values())
        {
            if(output.length() > 0)
                output.append(", ");
            output.append(reason);
        }
        return output.toString();
    }

    private static final String ENTITY = "minecraft:";
    private static final String CHEST = "minecraft:chests/";

    public static final List<Identifier> ENTITY_COPPER_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "slime"), new Identifier(ENTITY + "silverfish"));
    public static final List<Identifier> ENTITY_IRON_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "zombie"), new Identifier(ENTITY + "skeleton"), new Identifier(ENTITY + "creeper"), new Identifier(ENTITY + "spider"), new Identifier(ENTITY + "cave_spider"), new Identifier(ENTITY + "husk"), new Identifier(ENTITY + "stray"), new Identifier(ENTITY + "magma_cube"), new Identifier(ENTITY + "zombie_villager"), new Identifier(ENTITY + "drowned"));
    public static final List<Identifier> ENTITY_GOLD_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "guardian"), new Identifier(ENTITY + "elder_guardian"), new Identifier(ENTITY + "phantom"), new Identifier(ENTITY + "blaze"), new Identifier(ENTITY + "ghast"), new Identifier(ENTITY + "witch"), new Identifier(ENTITY + "hoglin"), new Identifier(ENTITY + "piglin_brute"), new Identifier(ENTITY + "piglin"), new Identifier(ENTITY + "zombified_piglin"));
    public static final List<Identifier> ENTITY_EMERALD_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "enderman"), new Identifier(ENTITY + "evoker"), new Identifier(ENTITY + "vindicator"), new Identifier(ENTITY + "pillager"), new Identifier(ENTITY + "ravager"), new Identifier(ENTITY + "shulker"));
    public static final List<Identifier> ENTITY_DIAMOND_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "wither_skeleton"));
    public static final List<Identifier> ENTITY_NETHERITE_DROPLIST = ImmutableList.of();

    public static final List<Identifier> ENTITY_BOSS_COPPER_DROPLIST = ImmutableList.of();
    public static final List<Identifier> ENTITY_BOSS_IRON_DROPLIST = ImmutableList.of();
    public static final List<Identifier> ENTITY_BOSS_GOLD_DROPLIST = ImmutableList.of();
    public static final List<Identifier> ENTITY_BOSS_EMERALD_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "warden"));
    public static final List<Identifier> ENTITY_BOSS_DIAMOND_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "ender_dragon"));
    public static final List<Identifier> ENTITY_BOSS_NETHERITE_DROPLIST = ImmutableList.of(new Identifier(ENTITY + "wither"));

    public static final List<Identifier> CHEST_COPPER_DROPLIST = ImmutableList.of(new Identifier(CHEST + "underwater_ruin_small"), new Identifier(CHEST + "underwater_ruin_big"));
    public static final List<Identifier> CHEST_IRON_DROPLIST = ImmutableList.of();
    public static final List<Identifier> CHEST_GOLD_DROPLIST = ImmutableList.of(new Identifier(CHEST + "jungle_temple"), new Identifier(CHEST + "nether_bridge"), new Identifier(CHEST + "simple_dungeon"), new Identifier(CHEST + "ruined_portal"));
    public static final List<Identifier> CHEST_EMERALD_DROPLIST = ImmutableList.of(new Identifier(CHEST + "stronghold_crossing"), new Identifier(CHEST + "stronghold_corridor"), new Identifier(CHEST + "stronghold_library"), new Identifier(CHEST + "ancient_city"));
    public static final List<Identifier> CHEST_DIAMOND_DROPLIST = ImmutableList.of(new Identifier(CHEST + "buried_treasure"), new Identifier(CHEST + "bastion_hoglin_stable"), new Identifier(CHEST + "bastion_bridge"), new Identifier(CHEST + "bastion_other"), new Identifier(CHEST + "bastion_treasure"), new Identifier(CHEST + "end_city_treasure"));
    public static final List<Identifier> CHEST_NETHERITE_DROPLIST = ImmutableList.of();

    private static final Map<Identifier,PoolLevel> EXTERNAL_ENTITY_ENTRIES = new HashMap<>();
    private static final Map<Identifier,PoolLevel> EXTERNAL_CHEST_ENTRIES = new HashMap<>();

    private static boolean lootTablesBuilt = false;

    //Normal entity loot
    private static LootPool.Builder ENTITY_LOOT_COPPER = null;
    private static LootPool.Builder ENTITY_LOOT_IRON = null;
    private static LootPool.Builder ENTITY_LOOT_GOLD = null;
    private static LootPool.Builder ENTITY_LOOT_EMERALD = null;
    private static LootPool.Builder ENTITY_LOOT_DIAMOND = null;
    private static LootPool.Builder ENTITY_LOOT_NETHERITE = null;

    //Boss loot
    private static List<LootPool.Builder> ENTITY_LOOT_BOSS_COPPER = null;
    private static List<LootPool.Builder> ENTITY_LOOT_BOSS_IRON = null;
    private static List<LootPool.Builder> ENTITY_LOOT_BOSS_GOLD = null;
    private static List<LootPool.Builder> ENTITY_LOOT_BOSS_EMERALD = null;
    private static List<LootPool.Builder> ENTITY_LOOT_BOSS_DIAMOND = null;
    private static List<LootPool.Builder> ENTITY_LOOT_BOSS_NETHERITE = null;

    //Chest loot
    private static LootPool.Builder CHEST_LOOT_COPPER = null;
    private static LootPool.Builder CHEST_LOOT_IRON = null;
    private static LootPool.Builder CHEST_LOOT_GOLD = null;
    private static LootPool.Builder CHEST_LOOT_EMERALD = null;
    private static LootPool.Builder CHEST_LOOT_DIAMOND = null;
    private static LootPool.Builder CHEST_LOOT_NETHERITE = null;

    private static void generateLootTables() {
        if(lootTablesBuilt)
            return;

        //Normal Loot
        ENTITY_LOOT_COPPER = GenerateEntityCoinPool(ModItems.COIN_COPPER, 1, 10, 0.75f, "lightmanscurrency:entityloot_copper", true);
        ENTITY_LOOT_IRON = GenerateEntityCoinPool(ModItems.COIN_IRON, 1, 5, 0.5f, "lightmanscurrency:entityloot_iron", true);
        ENTITY_LOOT_GOLD = GenerateEntityCoinPool(ModItems.COIN_GOLD, 1, 5, 0.25f, "lightmanscurrency:entityloot_gold", true);
        ENTITY_LOOT_EMERALD = GenerateEntityCoinPool(ModItems.COIN_EMERALD, 1, 3, 0.1f, "lightmanscurrency:entityloot_emerald", true);
        ENTITY_LOOT_DIAMOND = GenerateEntityCoinPool(ModItems.COIN_DIAMOND, 1, 3, 0.05f, "lightmanscurrency:entityloot_diamond", true);
        ENTITY_LOOT_NETHERITE = GenerateEntityCoinPool(ModItems.COIN_NETHERITE, 1, 3, 0.025F, "lightmanscurrency:entityloot_netherite", true);

        //Boss loot
        ENTITY_LOOT_BOSS_COPPER = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false));
        ENTITY_LOOT_BOSS_IRON = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false));
        ENTITY_LOOT_BOSS_GOLD = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false));
        ENTITY_LOOT_BOSS_EMERALD = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(ModItems.COIN_EMERALD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false));
        ENTITY_LOOT_BOSS_DIAMOND = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(ModItems.COIN_EMERALD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false),GenerateEntityCoinPool(ModItems.COIN_DIAMOND, 10, 30, 1.0f, "lightmanscurrency:coinloot_boss_diamond", false));
        ENTITY_LOOT_BOSS_NETHERITE = ImmutableList.of(GenerateEntityCoinPool(ModItems.COIN_COPPER, 10,30,1.0f,"lightmanscurrency:entityloot_boss_copper", false),GenerateEntityCoinPool(ModItems.COIN_IRON, 10,30,1.0f,"lightmanscurrency:coinloot_boss_iron", false), GenerateEntityCoinPool(ModItems.COIN_GOLD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_gold", false), GenerateEntityCoinPool(ModItems.COIN_EMERALD, 10,30,1.0f,"lightmanscurrency:coinloot_boss_emerald", false),GenerateEntityCoinPool(ModItems.COIN_DIAMOND, 10, 30, 1.0f, "lightmanscurrency:coinloot_boss_diamond", false),GenerateEntityCoinPool(ModItems.COIN_NETHERITE, 1, 5, 1.0f, "lightmanscurrency:coinloot_boss_netherite", false));

        //Chest loot
        CHEST_LOOT_COPPER = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER}, 1, 5, "lightmanscurrency:chestloot_copper");
        CHEST_LOOT_IRON = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON}, 1, 5, "lightmanscurrency:chestloot_iron");
        CHEST_LOOT_GOLD = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD}, 2, 6, "lightmanscurrency:chestloot_gold");
        CHEST_LOOT_EMERALD = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD}, 3, 6, "lightmanscurrency:chestloot_emerald");
        CHEST_LOOT_DIAMOND = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND}, 3, 6, "lightmanscurrency:chestloot_diamond");
        CHEST_LOOT_NETHERITE = GenerateChestCoinPool(new ChestLootEntryData[] {ChestLootEntryData.COPPER, ChestLootEntryData.IRON, ChestLootEntryData.GOLD, ChestLootEntryData.EMERALD, ChestLootEntryData.DIAMOND, ChestLootEntryData.NETHERITE}, 3, 6, "lightmanscurrency:chestloot_netherite");

        lootTablesBuilt = true;

    }

    private static String getValueList(IdentifierListOption option) {
        StringBuffer buffer = new StringBuffer();
        List<Identifier> list = option.get();
        for(Identifier value : list)
        {
            if(buffer.length() > 0)
                buffer.append(", ");
            buffer.append("\"").append(value).append("\"");
        }
        return buffer.toString();
    }

    public static void debugLootConfigs() {

        LightmansCurrency.LogDebug("Lightman's Currency common configs have been loaded. Coin loot values are as follows.");
        //Chests
        LightmansCurrency.LogDebug("Chest Copper: " + getValueList(LCConfigCommon.INSTANCE.copperChestDrops));
        LightmansCurrency.LogDebug("Chest Iron: " + getValueList(LCConfigCommon.INSTANCE.ironChestDrops));
        LightmansCurrency.LogDebug("Chest Gold: " + getValueList(LCConfigCommon.INSTANCE.goldChestDrops));
        LightmansCurrency.LogDebug("Chest Emerald: " + getValueList(LCConfigCommon.INSTANCE.emeraldChestDrops));
        LightmansCurrency.LogDebug("Chest Diamond: " + getValueList(LCConfigCommon.INSTANCE.diamondChestDrops));
        LightmansCurrency.LogDebug("Chest Netherite: " + getValueList(LCConfigCommon.INSTANCE.netheriteChestDrops));

        //Entity (normal)
        LightmansCurrency.LogDebug("Entity Copper (Normal): " + getValueList(LCConfigCommon.INSTANCE.copperEntityDrops));
        LightmansCurrency.LogDebug("Entity Iron (Normal): " + getValueList(LCConfigCommon.INSTANCE.ironEntityDrops));
        LightmansCurrency.LogDebug("Entity Gold (Normal): " + getValueList(LCConfigCommon.INSTANCE.goldEntityDrops));
        LightmansCurrency.LogDebug("Entity Emerald (Normal): " + getValueList(LCConfigCommon.INSTANCE.emeraldEntityDrops));
        LightmansCurrency.LogDebug("Entity Diamond (Normal): " + getValueList(LCConfigCommon.INSTANCE.diamondEntityDrops));
        LightmansCurrency.LogDebug("Entity Netherite (Normal): " + getValueList(LCConfigCommon.INSTANCE.netheriteEntityDrops));

        //Entity (boss)
        LightmansCurrency.LogDebug("Entity Copper (Boss): " + getValueList(LCConfigCommon.INSTANCE.bossCopperEntityDrops));
        LightmansCurrency.LogDebug("Entity Iron (Boss): " + getValueList(LCConfigCommon.INSTANCE.bossIronEntityDrops));
        LightmansCurrency.LogDebug("Entity Gold (Boss): " + getValueList(LCConfigCommon.INSTANCE.bossGoldEntityDrops));
        LightmansCurrency.LogDebug("Entity Emerald (Boss): " + getValueList(LCConfigCommon.INSTANCE.bossEmeraldEntityDrops));
        LightmansCurrency.LogDebug("Entity Diamond (Boss): " + getValueList(LCConfigCommon.INSTANCE.bossDiamondEntityDrops));
        LightmansCurrency.LogDebug("Entity Netherite (Boss): " + getValueList(LCConfigCommon.INSTANCE.bossNetheriteEntityDrops));

    }

    /**
     * Listens to LootTableEvents.MODIFY to modify the chest loot tables safely.
     */
    public static void onLootTableLoaded(ResourceManager resourceManager, net.minecraft.loot.LootManager lootManager, Identifier id, LootTable.Builder tableBuilder, LootTableSource source) {
        PoolLevel level = GetChestPoolLevel(id);
        if(level != null)
            AddChestLootToTable(tableBuilder, level);
    }


    //TODO check for spawn reason
    public static void onEntitySpawned(LivingEntity entity, SpawnReason reason)
    {
        if(entity instanceof PlayerEntity)
            return;

        //SpawnTrackerCapability.getSpawnerTracker(entity).ifPresent(spawnerTracker -> spawnerTracker.setSpawnReason(event.getSpawnReason()));
        //if(!SpawnTrackerCapability.getSpawnerTracker(entity).isPresent())
        //    LightmansCurrency.LogWarning(entity.getName().getString() + " does not have a ISpawnerTracker attached. Unable to flag it's SpawnReason.");
    }

    public static void entityDeath(LivingEntity entity, DamageSource damageSource)
    {
        //Check if this is the server
        if(entity.world.isClient)
            return;

        if(!LCConfigCommon.INSTANCE.enableSpawnerEntityDrops.get())
        {
            //Spawner drops aren't allowed. Check if the entity was spawner-spawned
            if(EntityLootBlocker.BlockEntityDrops(entity))
                return;
        }

        Identifier killedType = Registry.ENTITY_TYPE.getId(entity.getType());

        if(damageSource.getAttacker() instanceof PlayerEntity player)
        {

            if(LCConfigCommon.INSTANCE.copperEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.COPPER);
            }
            else if(LCConfigCommon.INSTANCE.ironEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.IRON);
            }
            else if(LCConfigCommon.INSTANCE.goldEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.GOLD);
            }
            else if(LCConfigCommon.INSTANCE.emeraldEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.EMERALD);
            }
            else if(LCConfigCommon.INSTANCE.diamondEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.DIAMOND);
            }
            else if(LCConfigCommon.INSTANCE.netheriteEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.NETHERITE);
            }
            else if(LCConfigCommon.INSTANCE.bossCopperEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.BOSS_COPPER);
            }
            else if(LCConfigCommon.INSTANCE.bossIronEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.BOSS_IRON);
            }
            else if(LCConfigCommon.INSTANCE.bossGoldEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.BOSS_GOLD);
            }
            else if(LCConfigCommon.INSTANCE.bossEmeraldEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.BOSS_EMERALD);
            }
            else if(LCConfigCommon.INSTANCE.bossDiamondEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.BOSS_DIAMOND);
            }
            else if(LCConfigCommon.INSTANCE.bossNetheriteEntityDrops.get().contains(killedType))
            {
                DropEntityLoot(entity, player, PoolLevel.BOSS_NETHERITE);
            }
            else
            {
                EXTERNAL_ENTITY_ENTRIES.forEach((e,level) -> {
                    if(e.equals(killedType))
                    {
                        DropEntityLoot(entity, player, level);
                        return;
                    }
                });
            }
            return;
        }
        //Boss deaths don't require a player kill to drop coins
        if(LCConfigCommon.INSTANCE.bossCopperEntityDrops.get().contains(killedType))
        {
            DropEntityLoot(entity, null, PoolLevel.BOSS_COPPER);
        }
        else if(LCConfigCommon.INSTANCE.bossIronEntityDrops.get().contains(killedType))
        {
            DropEntityLoot(entity, null, PoolLevel.BOSS_IRON);
        }
        else if(LCConfigCommon.INSTANCE.bossGoldEntityDrops.get().contains(killedType))
        {
            DropEntityLoot(entity, null, PoolLevel.BOSS_GOLD);
        }
        else if(LCConfigCommon.INSTANCE.bossEmeraldEntityDrops.get().contains(killedType))
        {
            DropEntityLoot(entity, null, PoolLevel.BOSS_EMERALD);
        }
        else if(LCConfigCommon.INSTANCE.bossDiamondEntityDrops.get().contains(killedType))
        {
            DropEntityLoot(entity, null, PoolLevel.BOSS_DIAMOND);
        }
        else if(LCConfigCommon.INSTANCE.bossNetheriteEntityDrops.get().contains(killedType))
        {
            DropEntityLoot(entity, null, PoolLevel.BOSS_NETHERITE);
        }
        else
        {
            EXTERNAL_ENTITY_ENTRIES.forEach((e,level) -> {
                if(e.equals(killedType) && !level.requiresPlayerKill)
                {
                    DropEntityLoot(entity, null, level);
                }
            });
        }
    }

    private static void DropEntityLoot(Entity entity, PlayerEntity player, PoolLevel coinPool)
    {

        if(!LCConfigCommon.INSTANCE.enableEntityDrops.get())
            return;

        generateLootTables();

        //LightmansCurrency.LOGGER.info("Dropping entity loot level " + coinPool);

        LootTable.Builder table = LootTable.builder();
        LootContext.Builder contextBuilder = new LootContext.Builder((ServerWorld) entity.world);
        //Add the KilledByPlayer condition to the Loot Context
        if(player != null)
            contextBuilder.parameter(LootContextParameters.KILLER_ENTITY, player)
                    .parameter(LootContextParameters.LAST_DAMAGE_PLAYER, player);

        LootContext context = contextBuilder.build(new LootContextType.Builder().allow(LootContextParameters.LAST_DAMAGE_PLAYER).allow(LootContextParameters.KILLER_ENTITY).build());

        try {

            //Boss loot done separately due to loops and exclusiveness.
            if(coinPool == PoolLevel.BOSS_COPPER)
            {
                //Drop copper boss loot
                for(LootPool.Builder builder : ENTITY_LOOT_BOSS_COPPER)
                {
                    table.pool(builder);
                }
                //Generate the loot
                SpawnLootDrops(entity, table.build().generateLoot(context));
                return;
            }
            else if(coinPool == PoolLevel.BOSS_IRON)
            {
                //Drop iron boss loot
                for(LootPool.Builder builder : ENTITY_LOOT_BOSS_IRON)
                {
                    table.pool(builder);
                }
                //Generate the loot
                SpawnLootDrops(entity, table.build().generateLoot(context));
                return;
            }
            else if(coinPool == PoolLevel.BOSS_GOLD)
            {
                //Drop gold boss loot
                for(LootPool.Builder builder : ENTITY_LOOT_BOSS_GOLD)
                {
                    table.pool(builder);
                }
                //Generate the loot
                SpawnLootDrops(entity, table.build().generateLoot(context));
                return;
            }
            else if(coinPool == PoolLevel.BOSS_EMERALD)
            {
                //Drop emerald boss loot
                for(LootPool.Builder builder : ENTITY_LOOT_BOSS_EMERALD)
                {
                    table.pool(builder);
                }
                //Generate the loot
                SpawnLootDrops(entity, table.build().generateLoot(context));
                return;
            }
            else if(coinPool == PoolLevel.BOSS_DIAMOND)
            {
                //Drop diamond boss loot
                for(LootPool.Builder builder : ENTITY_LOOT_BOSS_DIAMOND)
                {
                    table.pool(builder);
                }
                //Generate the loot
                SpawnLootDrops(entity, table.build().generateLoot(context));
                return;
            }
            else if(coinPool == PoolLevel.BOSS_NETHERITE)
            {
                //Drop netherite boss loot
                for(LootPool.Builder builder : ENTITY_LOOT_BOSS_NETHERITE)
                {
                    table.pool(builder);
                }
                //Generate the loot
                SpawnLootDrops(entity, table.build().generateLoot(context));
                return;
            }

            //LightmansCurrency.LOGGER.debug("Added " + coinPool + " level entity loot to the " + name + " loot entry.");
            table.pool(ENTITY_LOOT_COPPER.build());
            if(coinPool != PoolLevel.COPPER)
            {
                table.pool(ENTITY_LOOT_IRON.build());
                if(coinPool != PoolLevel.IRON)
                {
                    table.pool(ENTITY_LOOT_GOLD.build());
                    if(coinPool != PoolLevel.GOLD)
                    {
                        table.pool(ENTITY_LOOT_EMERALD.build());
                        if(coinPool != PoolLevel.EMERALD)
                        {
                            table.pool(ENTITY_LOOT_DIAMOND.build());
                            if(coinPool != PoolLevel.DIAMOND)
                                table.pool(ENTITY_LOOT_NETHERITE.build());
                        }
                    }
                }
            }

            SpawnLootDrops(entity, table.build().generateLoot(context));

        } catch(Exception e) { LightmansCurrency.LogError("Error spawning coin drops!", e); }

    }

    public static void AddChestLootToTable(LootTable.Builder builder, PoolLevel coinPool) {
        generateLootTables();
        if(coinPool == PoolLevel.COPPER)
            builder.pool(CHEST_LOOT_COPPER);
        else if(coinPool == PoolLevel.IRON)
            builder.pool(CHEST_LOOT_IRON);
        else if(coinPool == PoolLevel.GOLD)
            builder.pool(CHEST_LOOT_GOLD);
        else if(coinPool == PoolLevel.EMERALD)
            builder.pool(CHEST_LOOT_EMERALD);
        else if(coinPool == PoolLevel.DIAMOND)
            builder.pool(CHEST_LOOT_DIAMOND);
        else if(coinPool == PoolLevel.NETHERITE)
            builder.pool(CHEST_LOOT_NETHERITE);
    }

    public static List<ItemStack> GetRandomChestLoot(PoolLevel coinPool, LootContext context) {

        generateLootTables();

        try {

            if(coinPool == PoolLevel.COPPER)
            {
                LootTable.Builder table = LootTable.builder();
                table.pool(CHEST_LOOT_COPPER);
                return safelyGetResults(table, context);
            }
            else if(coinPool == PoolLevel.IRON)
            {
                LootTable.Builder table = LootTable.builder();
                table.pool(CHEST_LOOT_IRON.build());
                return safelyGetResults(table, context);
            }
            else if(coinPool == PoolLevel.GOLD)
            {
                LootTable.Builder table = LootTable.builder();
                table.pool(CHEST_LOOT_GOLD.build());
                return safelyGetResults(table, context);
            }
            else if(coinPool == PoolLevel.EMERALD)
            {
                LootTable.Builder table = LootTable.builder();
                table.pool(CHEST_LOOT_EMERALD.build());
                return safelyGetResults(table, context);
            }
            else if(coinPool == PoolLevel.DIAMOND)
            {
                LootTable.Builder table = LootTable.builder();
                table.pool(CHEST_LOOT_DIAMOND.build());
                return safelyGetResults(table, context);
            }
            else if(coinPool == PoolLevel.NETHERITE)
            {
                LootTable.Builder table = LootTable.builder();
                table.pool(CHEST_LOOT_NETHERITE.build());
                return safelyGetResults(table, context);
            }
            else
            {
                LightmansCurrency.LogError("Attempting to get random chest loot from an invalid chest pool level of '" + (coinPool == null ? "NULL" : coinPool.toString()) + "'");
                return new ArrayList<>();
            }

        } catch(Exception e) {
            LightmansCurrency.LogError("Error spawning chest coin drops!", e);
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("deprecation")
    private static List<ItemStack> safelyGetResults(LootTable.Builder table, LootContext context) {
        List<ItemStack> results = new ArrayList<>();
        //Call getRandomItems(LootContext,Consumer<ItemStack>) to keep it from being modified by the GLM's and getting stuck in an infinite loop.
        table.build().generateLoot(context, results::add);
        return results;
    }

    public static PoolLevel GetChestPoolLevel(Identifier lootTable) {
        if(LCConfigCommon.INSTANCE.copperChestDrops.get().contains(lootTable))
            return PoolLevel.COPPER;
        if(LCConfigCommon.INSTANCE.ironChestDrops.get().contains(lootTable))
            return PoolLevel.IRON;
        if(LCConfigCommon.INSTANCE.goldChestDrops.get().contains(lootTable))
            return PoolLevel.GOLD;
        if(LCConfigCommon.INSTANCE.emeraldChestDrops.get().contains(lootTable))
            return PoolLevel.EMERALD;
        if(LCConfigCommon.INSTANCE.diamondChestDrops.get().contains(lootTable))
            return PoolLevel.DIAMOND;
        if(LCConfigCommon.INSTANCE.netheriteChestDrops.get().contains(lootTable))
            return PoolLevel.NETHERITE;

        if(EXTERNAL_CHEST_ENTRIES.containsKey(lootTable))
            return EXTERNAL_CHEST_ENTRIES.get(lootTable);

        return null;
    }

    private static void SpawnLootDrops(Entity entity, List<ItemStack> lootDrops)
    {
        //LightmansCurrency.LOGGER.info("Spawning " + lootDrops.size() + " coin drops.");
        InventoryUtil.dumpContents(entity.world, entity.getBlockPos(), lootDrops);
    }

    /**
     * Adds the given entity's loot table to the list so that it will drop coins in addition to its already given loot.
     * @param resource String format of the loot tables ResourceLocation (e.g. "minecraft:entities/zombie"), or of the entities id (e.g. "minecraft:sheep")
     * @param coinPool The highest level coin that the entity should be allowed to drop.
     */
    public static void AddEntityCoinPoolToTable(Identifier resource, PoolLevel coinPool)
    {
        EXTERNAL_ENTITY_ENTRIES.put(resource, coinPool);
    }

    /**
     * Adds the given chest's loot table to the list so that it will spawn coins in addition to its already given loot.
     * @param resource String format of the loot tables ResourceLocation (e.g. "minecraft:chests/buried_treasure")
     * @param coinPool The highest level coin that the chest should spawn. Should not include the BOSS pool levels, as those are for entities only.
     */
    public static void AddChestCoinPoolToTable(Identifier resource, PoolLevel coinPool)
    {
        if(coinPool.level > PoolLevel.NETHERITE.level)
        {
            LightmansCurrency.LogError("Attempted to add a chest to the coin pool at level " + coinPool.name() + ", but that level is not valid for chests.");
            return;
        }
        EXTERNAL_CHEST_ENTRIES.put(resource, coinPool);
    }


    private static LootPool.Builder GenerateEntityCoinPool(ItemConvertible item, float min, float max, float chance, String name, boolean requirePlayerKill)
    {

        LootPool.Builder lootPoolBuilder = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .with(ItemEntry.builder(item).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max))).apply(LootingEnchantLootFunction.builder(UniformLootNumberProvider.create(0f, 1f))));

        //Require that the player killed it (usually only disabled for bosses)
        if(requirePlayerKill)
            lootPoolBuilder.conditionally(KilledByPlayerLootCondition.builder());
        //Add a random chance to the loot (if applicable, usually only disabled for bosses)
        if(chance < 1.0f)
            lootPoolBuilder.conditionally(RandomChanceWithLootingLootCondition.builder(chance, LOOTING_MODIFIER));

        return lootPoolBuilder;

    }

    private static LootPool.Builder GenerateChestCoinPool(ChestLootEntryData[] lootEntries, float minRolls, float maxRolls, String name)
    {

        LootPool.Builder lootPoolBuilder = LootPool.builder()
                .rolls(UniformLootNumberProvider.create(minRolls, maxRolls));

        //Add each loot entry
        for(ChestLootEntryData entry : lootEntries)
        {
            lootPoolBuilder.with(ItemEntry.builder(entry.item).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(entry.minCount, entry.maxCount))).weight(entry.weight));
        }

        return lootPoolBuilder;

    }

    private record ChestLootEntryData(Item item, float minCount, float maxCount, int weight) {

        public static ChestLootEntryData COPPER = new ChestLootEntryData(ModItems.COIN_COPPER, 1, 10, 1);
        public static ChestLootEntryData IRON = new ChestLootEntryData(ModItems.COIN_IRON, 1, 10, 2);
        public static ChestLootEntryData GOLD = new ChestLootEntryData(ModItems.COIN_GOLD, 1, 10, 3);
        public static ChestLootEntryData EMERALD = new ChestLootEntryData(ModItems.COIN_EMERALD, 1, 10, 4);
        public static ChestLootEntryData DIAMOND = new ChestLootEntryData(ModItems.COIN_DIAMOND, 1, 8, 5);
        public static ChestLootEntryData NETHERITE = new ChestLootEntryData(ModItems.COIN_NETHERITE, 1, 3, 6);

    }

}