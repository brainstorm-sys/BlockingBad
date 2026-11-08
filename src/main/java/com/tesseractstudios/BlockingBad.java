    package com.tesseractstudios;

    import com.tesseractstudios.items.ingredients.*;
    import com.tesseractstudios.listeners.DamageListener;
    import com.tesseractstudios.listeners.MethListener;
    import com.tesseractstudios.managers.ItemManager;
    import com.tesseractstudios.recipes.MethRecipes;
    import dev.jorel.commandapi.CommandAPI;
    import dev.jorel.commandapi.CommandAPIPaperConfig;
    import org.bukkit.plugin.java.JavaPlugin;

    public final class BlockingBad extends JavaPlugin {


        public static BlockingBad instance;


        @Override
        public void onLoad() {
            CommandAPI.onLoad(
                    new CommandAPIPaperConfig(this)
                            .silentLogs(true)
                            .fallbackToLatestNMS(false)
                            .missingExecutorImplementationMessage("An error occured")
            );
        }

        @Override

        public void onEnable() {

            instance = this;
            // Plugin startup logic
            CommandAPI.onEnable();

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
            new DamageListener(this, itemManager);


            // ------ COMMANDS ------


        }



        @Override
        public void onDisable() {
            // Plugin shutdown logic
            CommandAPI.onDisable();
            instance = null;
        }
    }
