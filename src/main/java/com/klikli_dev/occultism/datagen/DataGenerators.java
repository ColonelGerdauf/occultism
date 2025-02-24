/*
 * MIT License
 *
 * Copyright 2020 klikli-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT
 * OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.klikli_dev.occultism.datagen;

import com.klikli_dev.occultism.Occultism;
import com.klikli_dev.occultism.datagen.lang.ENUSProvider;
import com.klikli_dev.occultism.datagen.lang.FRFRProvider;
import com.klikli_dev.occultism.datagen.lang.PTBRProvider;
import com.klikli_dev.occultism.datagen.lang.loot.OccultismBlockLoot;
import com.klikli_dev.occultism.datagen.lang.loot.OccultismEntityLoot;
import com.klikli_dev.occultism.datagen.worldgen.OccultismRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.DatapackBuiltinEntriesProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGenerators {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        generator.addProvider(event.includeServer(),
                new LootTableProvider(generator.getPackOutput(), Set.of(), List.of(
                        new LootTableProvider.SubProviderEntry(OccultismBlockLoot::new, LootContextParamSets.BLOCK),
                        new LootTableProvider.SubProviderEntry(OccultismEntityLoot::new, LootContextParamSets.ENTITY)
                )));
        generator.addProvider(event.includeServer(), new PentacleProvider(generator));
        generator.addProvider(event.includeServer(), new OccultismAdvancementProvider(generator));
        generator.addProvider(event.includeServer(), new CrushingRecipeProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new MinerRecipeProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new ItemModelsGenerator(generator.getPackOutput(), event.getExistingFileHelper()));
        generator.addProvider(event.includeClient(), new StandardBlockStateProvider(generator.getPackOutput(), event.getExistingFileHelper()));


        var enUSProvider = new ENUSProvider(generator.getPackOutput());
        var frFRProvider = new FRFRProvider(generator.getPackOutput());
        var ptBRProvider = new PTBRProvider(generator.getPackOutput());
        generator.addProvider(event.includeServer(), new OccultismBookProvider(generator.getPackOutput(), Occultism.MODID, enUSProvider, frFRProvider, ptBRProvider));

        //Important: Lang provider (in this case enus) needs to be added after the book provider to process the texts added by the book provider
        generator.addProvider(event.includeClient(), enUSProvider);
        generator.addProvider(event.includeClient(), frFRProvider);
        generator.addProvider(event.includeClient(), ptBRProvider);

        event.getGenerator().addProvider(event.includeServer(),
                (DataProvider.Factory<DatapackBuiltinEntriesProvider>) output ->
                        new DatapackBuiltinEntriesProvider(output, event.getLookupProvider(), OccultismRegistries.BUILDER, Set.of(Occultism.MODID)));
    }

}