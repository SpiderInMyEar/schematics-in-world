package net.projet.schematicsinworld.world.structures;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.structure.AbstractVillagePiece;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.structure.VillageConfig;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.projet.schematicsinworld.SchematicsInWorld;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

/**
 * This class' purpose is to provide all information relevant to generating its own generic structure.
 * This is done through a local class depending on each instance of this class.
 *
 * It must be Immutable.
 */
public class SiwStructureProvider {

    // ATTRIBUTS
    private final String struct_name;
    private final int distMax = 32;
    private final int distMin = 8;

    // CONSTRUCTORs

    public SiwStructureProvider(String name) {
        struct_name = name;
    }

    // REQUETES
    // The name of the structure. Used for /locate, notably
    public String name() {
        return struct_name;
    }

    // Distance max entre structure.
    public int maxDist(){
        return distMax;
    }

    // Distance min entre structure.
    public int minDist(){
        return distMin;
    }

    // The random seed associated with this provider.
    // Linked to the name.
    public int randseed(){
        return struct_name.hashCode();
    }

    // The main method. It provides instances of its Structure.
    // You main consider it a form of "new Structure...".
    public Structure<NoFeatureConfig> provide(){
        return new SiwStructure();
    }

    // The Structure class template for the provider. All provided structures will extends this class.
    private class SiwStructure extends Structure<NoFeatureConfig> {

        public SiwStructure() {
            super(NoFeatureConfig.CODEC);
        }

        /*
            On génére la structure au moment de génération des structure de surface,
            AVANT les plantes et minerais.
        */
        @Override
        public GenerationStage.Decoration getDecorationStage() {
            return GenerationStage.Decoration.SURFACE_STRUCTURES;
        }


        @Override
        public IStartFactory<NoFeatureConfig> getStartFactory() {
            return Start::new;
        }

        public class Start extends StructureStart<NoFeatureConfig> {

            public Start(Structure<NoFeatureConfig> structureIn, int chunkX, int chunkZ,
                         MutableBoundingBox mutableBoundingBox,
                         int referenceIn, long seedIn) {
                super(structureIn, chunkX, chunkZ, mutableBoundingBox, referenceIn, seedIn);
            }

            @Override // GeneratePieces
            public void func_230364_a_(DynamicRegistries dynamicRegistryManager, ChunkGenerator chunkGenerator,
                                       TemplateManager templateManagerIn, int chunkX, int chunkZ, Biome biomeIn,
                                       NoFeatureConfig config) {
                int x = (chunkX << 4) + 7;
                int z = (chunkZ << 4) + 7;
                BlockPos blockpos = new BlockPos(x, 0, z);

                //addpieces()

                /*
                JigsawManager.func_242837_a(dynamicRegistryManager,
                        new VillageConfig(() -> dynamicRegistryManager.getRegistry(Registry.JIGSAW_POOL_KEY)
                                .getOrDefault(new ResourceLocation(SchematicsInWorld.MOD_ID, "brick_pillar/start_pool")),
                                10), AbstractVillagePiece::new, chunkGenerator, templateManagerIn,
                        blockpos, this.components, this.rand, false, true);
                 */

                JigsawManager.func_242837_a(dynamicRegistryManager,
                        new VillageConfig(() -> dynamicRegistryManager.getRegistry(Registry.JIGSAW_POOL_KEY)
                                .getOrDefault(new ResourceLocation(SchematicsInWorld.MOD_ID,
                                        name() + "/" + struct_name + "_start_pool")),
                                10), AbstractVillagePiece::new, chunkGenerator, templateManagerIn,
                        blockpos, this.components, this.rand, false, true);


                this.components.forEach(piece -> piece.offset(0, 1, 0));
                this.components.forEach(piece -> piece.getBoundingBox().minY -= 1);

                this.recalculateStructureSize();

                LogManager.getLogger().log(Level.DEBUG, name() +" at " +
                        this.components.get(0).getBoundingBox().minX + " " +
                        this.components.get(0).getBoundingBox().minY + " " +
                        this.components.get(0).getBoundingBox().minZ);
            }
        }
    }
}
