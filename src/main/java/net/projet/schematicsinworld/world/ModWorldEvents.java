package net.projet.schematicsinworld.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.projet.schematicsinworld.SchematicsInWorld;
import net.projet.schematicsinworld.world.structure.ModStructures;
import net.projet.schematicsinworld.world.gen.ModStructureGeneration;
import org.apache.logging.log4j.LogManager;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = SchematicsInWorld.MOD_ID)
public class ModWorldEvents {

    @SubscribeEvent
    public static void biomeLoadingEvent(final BiomeLoadingEvent event) {
        ModStructureGeneration.generateStructures(event);
    }

    @SubscribeEvent
    public static void addDimensionalSpacing(final WorldEvent.Load event) {
        if(event.getWorld() instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld) event.getWorld();

            try {
                Method GETCODEC_METHOD =
                        ObfuscationReflectionHelper.findMethod(ChunkGenerator.class, "func_230347_a_");
                ResourceLocation cgRL = Registry.CHUNK_GENERATOR_CODEC.getKey(
                        (Codec<? extends ChunkGenerator>)GETCODEC_METHOD.invoke(serverWorld.getChunkProvider().generator));

                if (cgRL != null && cgRL.getNamespace().equals("terraforged")) {
                    return;
                }
            } catch (Exception e) {
                LogManager.getLogger().error("Was unable to check if " + serverWorld.getDimensionKey().getLocation()
                        + " is using Terraforged's ChunkGenerator.");
            }

            // Prevent spawning our structure in Vanilla's superflat world
            if (serverWorld.getChunkProvider().generator instanceof FlatChunkGenerator &&
                    serverWorld.getDimensionKey().equals(World.OVERWORLD)) {
                return;
            }

            // Adding our Structure to the Map
            Map<Structure<?>, StructureSeparationSettings> tempMap =
                    new HashMap<>(serverWorld.getChunkProvider().generator.func_235957_b_().func_236195_a_());
            for (int i = 0; i < ModStructures.SIW_STRUCTURES_LIST.size(); i++) {
                tempMap.putIfAbsent(ModStructures.SIW_STRUCTURES_LIST.get(i).get(),
                        DimensionStructuresSettings.field_236191_b_.get(ModStructures.SIW_STRUCTURES_LIST.get(i).get()));
            }
            //tempMap.putIfAbsent(ModStructures.BRICK_PILLAR.get(),
            //        DimensionStructuresSettings.field_236191_b_.get(ModStructures.BRICK_PILLAR.get()));
            serverWorld.getChunkProvider().generator.func_235957_b_().field_236193_d_ = tempMap;
        }
    }
}
