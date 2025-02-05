package net.projet.schematicsinworld.world.structure;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.projet.schematicsinworld.world.structures.generic.GenericStructurePool;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.projet.schematicsinworld.SchematicsInWorld;
import net.projet.schematicsinworld.world.structures.SiwStructureProvider;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ModStructures {
    public static final DeferredRegister<Structure<?>> STRUCTURES =
            DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, SchematicsInWorld.MOD_ID);

    // -------------------- On explore le dossier src/main/resources/data/siw/structures puis on stocke les noms des nbt présents
    public final static List<String> STRUCTURE_NAMES = new LinkedList<String>();
    public final static List<String> STRUCTURE_FILES = new LinkedList<String>();

    static {
        String start = System.getProperty("user.dir");
        start += "/../src/main/resources/data/" + SchematicsInWorld.MOD_ID + "/structures";

        System.out.println("putain\n");
        System.out.println(start);

        try (Stream<Path> stream = Files.walk(Paths.get(start), Integer.MAX_VALUE)) {
            List<String> collect = stream
                    .map(String::valueOf)
                    .sorted()
                    .collect(Collectors.toList());
            for (String str : collect) {
                File file = new File(str);
                if (file.isFile() && file.getName().endsWith(".nbt")) {
                    String r = StringUtils.removeEnd(file.getName(), ".nbt");
                    STRUCTURE_FILES.add(r);
                    r = r.substring(0, r.length() - 2);
                    if (!STRUCTURE_NAMES.contains(r)) {
                        STRUCTURE_NAMES.add(r);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(String str : STRUCTURE_FILES) {
            GenericStructurePool gsp = new GenericStructurePool(str);
        }
    }

    // --------------------
    private static final List<SiwStructureProvider> providerList = new LinkedList<SiwStructureProvider>();

    static {
        for(String str : STRUCTURE_NAMES) {
            providerList.add(new SiwStructureProvider(str));
        }
    }

    // Notre liste des RegistryObject.
    public static final List<RegistryObject<Structure<NoFeatureConfig>>> SIW_STRUCTURES_LIST =
            new LinkedList<RegistryObject<Structure<NoFeatureConfig>>>();

    // Pour chaque structure, on enregistre !
    static {
        for(SiwStructureProvider s : providerList) {
            SIW_STRUCTURES_LIST.add(STRUCTURES.register(s.name(), s::provide));
        }
    }
    //public static final RegistryObject<Structure<NoFeatureConfig>> BRICK_PILLAR =
    //                STRUCTURES.register(brick.name(), brick::provide);
         //   STRUCTURES.register("brick_pillar", BrickPillarStructure::new);

    /* average distance apart in chunks between spawn attempts */
    /* minimum distance apart in chunks between spawn attempts. MUST BE LESS THAN ABOVE VALUE*/
    /* this modifies the seed of the structure so no two structures always spawn over each-other.
    Make this large and unique. */
    public static void setupStructures() {
        for(int i = 0; i < providerList.size(); i++) {
            SiwStructureProvider p = providerList.get(i);
            setupMapSpacingAndLand(SIW_STRUCTURES_LIST.get(i).get(),
                    new StructureSeparationSettings(p.maxDist(), p.minDist(), p.randseed()),
                    true);
        }
        //setupMapSpacingAndLand(BRICK_PILLAR.get(),
        //        new StructureSeparationSettings(100, 50, 475658536),
        //        true);
    }

    public static void register(IEventBus eventBus) {
        STRUCTURES.register(eventBus);
    }

    /**
     * Adds the provided structure to the registry, and adds the separation settings.
     * The rarity of the structure is determined based on the values passed into
     * this method in the structureSeparationSettings argument.
     * This method is called by setupStructures above.
     **/
    public static <F extends Structure<?>> void setupMapSpacingAndLand(F structure, StructureSeparationSettings structureSeparationSettings,
                                                                       boolean transformSurroundingLand) {
        //add our structures into the map in Structure class
        Structure.NAME_STRUCTURE_BIMAP.put(structure.getRegistryName().toString(), structure);

        /*
         * Whether surrounding land will be modified automatically to conform to the bottom of the structure.
         * Basically, it adds land at the base of the structure like it does for Villages and Outposts.
         * Doesn't work well on structure that have pieces stacked vertically or change in heights.
         *
         */
        if (transformSurroundingLand) {
            Structure.field_236384_t_ = ImmutableList.<Structure<?>>builder()
                    .addAll(Structure.field_236384_t_)
                    .add(structure)
                    .build();
        }

        /*
         * This is the map that holds the default spacing of all structures.
         * Always add your structure to here so that other mods can utilize it if needed.
         *
         * However, while it does propagate the spacing to some correct dimensions from this map,
         * it seems it doesn't always work for code made dimensions as they read from this list beforehand.
         *
         * Instead, we will use the WorldEvent.Load event in ModWorldEvents to add the structure
         * spacing from this list into that dimension or to do dimension blacklisting properly.
         * We also use our entry in DimensionStructuresSettings.DEFAULTS in WorldEvent.Load as well.
         *
         * DEFAULTS requires AccessTransformer  (See resources/META-INF/accesstransformer.cfg)
         */

        /*
        *  public static ImmutableMap<Structure<?>, StructureSeparationSettings> field_236191_b_ contient de base l'intégralité des structures
        *  de VanillaMC
         *
        */


        DimensionStructuresSettings.field_236191_b_ =
                ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
                        .putAll(DimensionStructuresSettings.field_236191_b_)
                        .put(structure, structureSeparationSettings)
                        .build();

        /*
         * There are very few mods that relies on seeing your structure in the
         * noise settings registry before the world is made.
         *
         * You may see some mods add their spacings to DimensionSettings.BUILTIN_OVERWORLD instead of the
         * NOISE_GENERATOR_SETTINGS loop below but that field only applies for the default overworld and
         * won't add to other worldtypes or dimensions (like amplified or Nether).
         * So yeah, don't do DimensionSettings.BUILTIN_OVERWORLD. Use the NOISE_GENERATOR_SETTINGS loop
         * below instead if you must.
         */
        WorldGenRegistries.NOISE_SETTINGS.getEntries().forEach(settings -> {
            Map<Structure<?>, StructureSeparationSettings> structureMap =
                    settings.getValue().getStructures().func_236195_a_();
            /*
             * Pre-caution in case a mod makes the structure map immutable like datapacks do.
             * I take no chances myself. You never know what another mods does...
             *
             * structureConfig requires AccessTransformer  (See resources/META-INF/accesstransformer.cfg)
             */
            if (structureMap instanceof ImmutableMap) {
                Map<Structure<?>, StructureSeparationSettings> tempMap = new HashMap<>(structureMap);
                tempMap.put(structure, structureSeparationSettings);
                settings.getValue().getStructures().func_236195_a_();

            } else {
                structureMap.put(structure, structureSeparationSettings);
            }
        });
    }
}
