package net.projet.schematicsinworld;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.projet.schematicsinworld.parser.SchematicsParser;
import net.projet.schematicsinworld.world.structure.ModStructures;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SchematicsInWorld.MOD_ID)
public class SchematicsInWorld {
    public static final String MOD_ID = "siw";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public SchematicsInWorld() {
        // Register the setup method for modloading
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // new SchematicsParser("E:\\Jordan\\Modding\\projet_annuel\\schem_tests\\maison.schem");
        // new SchematicsParser("C:\\Users\\utilisateur\\Desktop\\Minecraft Modding\\schematicsInWorld\\schem_tests\\maison.schem");

        /* Test de la classe TagFloat
        byte buffer[] = new byte[] {64, 73, -103, -102};
        String s = "pain";
        byte[] name = s.getBytes(StandardCharsets.UTF_8);
        byte[] len = new byte[] {0, (byte)name.length};//BigInteger.valueOf(name.length).toByteArray();

        byte[] all = new byte[len.length + name.length + buffer.length + 10];
        all[0] = len[0];
        all[1] = len[1];

        for (int i = 0; i < name.length; ++i) {
            all[i + 2] = name[i];
        }
        for (int i = 0; i < buffer.length; ++i) {
            all[i + 2 + name.length] = buffer[i];
        }
        BytesStream bs = new BytesStream(all);
        TagFloat tf = new TagFloat(bs);
        LOGGER.info(tf.getKey() + " " + tf.getValue());
        */

        ModStructures.register(modEventBus);

        modEventBus.addListener(this::setup);
        // Register the enqueueIMC method for modloading
        modEventBus.addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        modEventBus.addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        modEventBus.addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        event.enqueueWork(() -> {
           ModStructures.setupStructures();
        });
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
            // register a new block here
            LOGGER.info("HELLO from Register Block");
        }
    }
}
