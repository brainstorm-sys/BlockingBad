    package com.tesseractstudios;

    import com.github.retrooper.packetevents.PacketEvents;
    import com.tesseractstudios.items.ingredients.*;
    import com.tesseractstudios.listeners.MethListener;
    import com.tesseractstudios.managers.ItemManager;
    import com.tesseractstudios.recipes.MethRecipes;
    import org.bukkit.plugin.java.JavaPlugin;
    import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

    public final class BlockingBad extends JavaPlugin {


        public static BlockingBad instance;



        @Override

        public void onEnable() {

            PacketEvents.getAPI().init();

            instance = this;
            // Plugin startup logic

            // ------ MANAGERS ------
            ItemManager itemManager = new ItemManager(this);


            // -- INGREDIENTS --
            new Alluminum(itemManager).register();
            new AlluminumAmalgam(itemManager).register();
            new FishBone(itemManager).register();
            new Mercury(itemManager).register();
            new Methylamine(itemManager).register();
            new PhenylaceticAcid(itemManager).register();
            new NMethamphetamine(itemManager).register();
            new Phenylacetone(itemManager).register();
            new Sulfate(itemManager).register();
            new HoneyCocoaPaste(itemManager).register();
            new OrganicMercuryMaterial(itemManager).register();
            new ShadedFishBone(itemManager).register();
            new PhenylAceticAcidSolution(itemManager).register();
            new UnpureMeth(itemManager).register();
            new LiquidMeth(itemManager).register();
            new OrganicMix(itemManager).register();

            // -- RECIPE --
            new MethRecipes(this, itemManager).register();
            new MethListener(this, itemManager);


            // ------ COMMANDS ------


        }

        @Override
        public void onLoad(){
            PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
            PacketEvents.getAPI().load();
        }



        @Override
        public void onDisable() {
            // Plugin shutdown logic
            PacketEvents.getAPI().terminate();
            instance = null;
        }
    }
