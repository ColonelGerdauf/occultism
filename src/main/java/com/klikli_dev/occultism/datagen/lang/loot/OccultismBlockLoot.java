package com.klikli_dev.occultism.datagen.lang.loot;

import com.klikli_dev.occultism.Occultism;
import com.klikli_dev.occultism.common.block.crops.IReplantableCrops;
import com.klikli_dev.occultism.common.block.otherworld.IOtherworldBlock;
import com.klikli_dev.occultism.registry.OccultismBlocks;
import com.klikli_dev.occultism.registry.OccultismItems;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.BonusLevelTableCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Set;
import java.util.function.BiConsumer;

public class OccultismBlockLoot extends BlockLootSubProvider {

    //x2 vanilla rate
    protected static final float[] DEFAULT_SAPLING_DROP_RATES = new float[]{0.05F, 0.0625F, 0.083333336F, 0.1F};
    protected static final float[] INCREASED_SAPLING_DROP_RATES = new float[]{0.1F, 0.2F, 0.3F, 0.4F};

    public OccultismBlockLoot() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    public void generate(BiConsumer<ResourceLocation, LootTable.Builder> consumer) {
        this.generate();
        this.map.forEach(consumer::accept);
    }

    @Override
    protected void generate() {
        OccultismBlocks.BLOCKS.getEntries().stream()
                .map(RegistryObject::get)
                .forEach(block -> {
                    OccultismBlocks.BlockDataGenSettings settings = OccultismBlocks.BLOCK_DATA_GEN_SETTINGS
                            .get(ForgeRegistries.BLOCKS.getKey(block));
                    if (settings.lootTableType == OccultismBlocks.LootTableType.EMPTY)
                        this.registerDropNothingLootTable(block);
                    else if (settings.lootTableType == OccultismBlocks.LootTableType.REPLANTABLE_CROP) {
                        IReplantableCrops cropsBlock = (IReplantableCrops) block;
                        LootItemCondition.Builder lootCondition =
                                LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                                        StatePropertiesPredicate.Builder.properties()
                                                .hasProperty(CropBlock.AGE, 7));
                        this.add(block,
                                this.createCropDrops(block, cropsBlock.getCropsItem().asItem(),
                                        cropsBlock.getSeedsItem().asItem(), lootCondition));
                    } else if (settings.lootTableType == OccultismBlocks.LootTableType.DROP_SELF)
                        this.dropSelf(block);
                    else if (settings.lootTableType == OccultismBlocks.LootTableType.OTHERWORLD_BLOCK)
                        this.registerOtherworldBlockTable(block);
                });

        this.add(OccultismBlocks.OTHERWORLD_LEAVES.get(),
                (block) -> this.createLeavesDrops(block, OccultismBlocks.OTHERWORLD_SAPLING.get(),
                        DEFAULT_SAPLING_DROP_RATES));

        this.add(OccultismBlocks.OTHERWORLD_LEAVES_NATURAL.get(),
                (block) -> this.createOtherworldLeavesDrops(block, Blocks.OAK_SAPLING, OccultismBlocks.OTHERWORLD_SAPLING_NATURAL.get(),
                        INCREASED_SAPLING_DROP_RATES));

        this.add(OccultismBlocks.SILVER_ORE.get(), this.createOreDrop(OccultismBlocks.SILVER_ORE.get(), OccultismItems.RAW_SILVER.get()));
        this.add(OccultismBlocks.SILVER_ORE_DEEPSLATE.get(), this.createOreDrop(OccultismBlocks.SILVER_ORE_DEEPSLATE.get(), OccultismItems.RAW_SILVER.get()));
        this.add(OccultismBlocks.IESNIUM_ORE.get(), this.createOreDrop(OccultismBlocks.IESNIUM_ORE.get(), OccultismItems.RAW_IESNIUM.get()));
    }

    protected void registerOtherworldBlockTable(Block block) {
        if (block instanceof IOtherworldBlock)
            this.add(block, this.createOtherworldBlockTable(block));
        else
            Occultism.LOGGER.warn("Tried to register otherworld block loot table for non-otherworld block {}",
                    ForgeRegistries.BLOCKS.getKey(block));
    }

    protected LootTable.Builder createOtherworldBlockTable(Block block) {
        IOtherworldBlock otherworldBlock = (IOtherworldBlock) block;
        return this.createOtherworldBlockTable(block, otherworldBlock.getCoveredBlock(), otherworldBlock.getUncoveredBlock());
    }

    protected LootTable.Builder createOtherworldBlockTable(Block block, ItemLike coveredDrop, ItemLike uncoveredDrop) {
        LootItemCondition.Builder uncoveredCondition =
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(block).setProperties(
                        StatePropertiesPredicate.Builder.properties()
                                .hasProperty(IOtherworldBlock.UNCOVERED, true));
        LootPool.Builder builder = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .add(LootItem.lootTableItem(uncoveredDrop)
                        .when(uncoveredCondition)
                        .otherwise(LootItem.lootTableItem(coveredDrop))
                );
        return LootTable.lootTable().withPool(builder);
    }

    protected LootTable.Builder createOtherworldLeavesDrops(Block leavesBlock, Block coveredSapling,
                                                            Block uncoveredSapling,
                                                            float... chances) {
        LootItemCondition.Builder uncoveredCondition =
                LootItemBlockStatePropertyCondition.hasBlockStateProperties(leavesBlock).setProperties(
                        StatePropertiesPredicate.Builder.properties()
                                .hasProperty(IOtherworldBlock.UNCOVERED, true));

        var saplingLootItem = LootItem.lootTableItem(uncoveredSapling)
                .when(uncoveredCondition)
                .otherwise(LootItem.lootTableItem(coveredSapling));

        return createSilkTouchOrShearsDispatchTable(leavesBlock,
                this.applyExplosionCondition(leavesBlock, saplingLootItem)
                        .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, chances)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                        .add(this.applyExplosionDecay(leavesBlock, LootItem.lootTableItem(Items.STICK)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                                .when(BonusLevelTableCondition.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F))));
    }

    public void registerDropNothingLootTable(Block block) {
        this.add(block, LootTable.lootTable().withPool(LootPool.lootPool()));
    }
}
