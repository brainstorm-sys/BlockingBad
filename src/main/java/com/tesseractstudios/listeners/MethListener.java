package com.tesseractstudios.listeners;

import com.tesseractstudios.managers.ItemManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.FoodProperties;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;


public class MethListener implements Listener {
    private final JavaPlugin plugin;
    private final ItemManager itemManager;
    private final NamespacedKey itemKey;
    private final ItemStack meth1;

    public MethListener(JavaPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.meth1 = itemManager.create("n_methamphetamine");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.itemKey = new NamespacedKey(plugin, "test");
    }


    private final Map<UUID, String> uuidStore = new HashMap<>();
    private final Map<UUID, Double> damageStorage = new HashMap<>();
    private final Map<UUID, Double> playerSave = new HashMap<>();
    private final Map<UUID, Long> methSoundCooldwn = new HashMap<>();
    private final Map<UUID, Double> calculatedDamage = new HashMap<>();

    //exp

    private final Map<UUID, Integer> methCount = new HashMap<>();
    private final Map<UUID, Long> oldTime = new HashMap<>();
    private final Map<UUID, Long> damageCooldown = new HashMap<>();
    private final Map<UUID, BukkitTask> damageTask = new HashMap<>();
    private final Map<UUID, Long> cooldownCheck = new HashMap<>();


    // -- Custom Crafting Logic wa--


    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e){
        Bukkit.getLogger().info("Join event detected");
        Player p = e.getPlayer();
        UUID playerUUID = p.getUniqueId();
        Double getDoubleTest = playerSave.get(playerUUID);

        if (!(playerSave.containsKey(playerUUID))) return;

        new BukkitRunnable(){
            public void run(){
                Bukkit.getLogger().info("Join damage applied");
                p.damage(getDoubleTest);
                playerSave.remove(playerUUID);
            }
        }.runTaskLater(plugin, 20L);
    }

    @EventHandler
    public void onLeaveEvent(PlayerQuitEvent e){
        Bukkit.getLogger().info("Player Leave event detected");
        Player p = e.getPlayer();
        UUID playerID = p.getUniqueId();

        if(!(uuidStore.containsKey(playerID))) return;
        Bukkit.getLogger().info("Player uuid get done");
        playerSave.put(playerID, damageStorage.get(playerID));
    }

    @EventHandler
    public void onGrindStoneClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();


        // phenylacetic acid
        ItemStack pa = itemManager.create("phenylacetic_acid");
        ItemMeta pameta = pa.getItemMeta();


        pameta.lore(List.of(
                Component.text(""),
                MiniMessage.miniMessage().deserialize("<!i><color:#F35BF3>- ɪᴜᴘᴀᴄ ɴᴀᴍᴇ: 2-ᴘʜᴇɴʏʟᴀᴄᴇᴛɪᴄ ᴀᴄɪᴅ</color></!i>"),
                MiniMessage.miniMessage().deserialize("<!i><color:#F35BF3>- ᴍᴏʟᴇᴄᴜʟᴀʀ ꜰᴏʀᴍᴜʟᴀ: ᴄ8ʜ8ᴏ2</color></!i>"),
                MiniMessage.miniMessage().deserialize("<!i><color:#F35BF3>- ᴍᴏʟᴀʀ ᴍᴀѕѕ: 136.15 ɢ·ᴍᴏʟ⁻¹</color></!i>")
        ));

        pa.setItemMeta(pameta);

        // ---  ---  ---  ---


        if (e.getClickedBlock() != null) {

            if (e.getClickedBlock().getType() == Material.GRINDSTONE) {
                if (itemManager.getItemId(e.getItem()).equals("honeycocoapaste")) {
                    e.setCancelled(true);


                    var loc = e.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);

                    player.getWorld().playSound(
                            Sound.sound(Key.key("minecraft:block.grindstone.use"), Sound.Source.BLOCK, 1f, 1f),
                            loc.getX(), loc.getY(), loc.getZ()
                    );
                    item.setAmount(item.getAmount() - 1);
                    if (item.getAmount() <= 0) {
                        player.getInventory().setItemInMainHand(pa);
                    }

                    else {
                        player.give(pa);
                    }
                }
            }
        }
    }

    // --- fish logic ---

    @EventHandler
    public void onEat(PlayerItemConsumeEvent e){

        System.out.println("If this works then lammy's gay");
        Player player = e.getPlayer();
        ItemStack eatitem = player.getInventory().getItemInMainHand();

        // - idk dawg test -, ok works ez
        if(eatitem.getType() == Material.TROPICAL_FISH){
            e.setCancelled(true);
            eatitem.setAmount(eatitem.getAmount() -1);
            if (eatitem.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(itemManager.create("fish_bone"));
            }

            else {
                player.give(itemManager.create("fish_bone"));
            }
        }


    }

    // -- glass -> pure meth logic


    @EventHandler
    public void onAnvil(PlayerInteractEvent e){
        Player p = e.getPlayer();

        if (e.getClickedBlock() == null || e.getClickedBlock().getType() == Material.AIR) return;

        ItemStack mainhand = p.getInventory().getItemInMainHand();
        if (itemManager.getItemId(mainhand).equals("liquidmeth")){
            if (e.getClickedBlock().getType() == Material.ANVIL){
                e.setCancelled(true);

                //audio
                var loc = e.getClickedBlock().getLocation().add(0.5, 0.5, 0.5);

                p.getWorld().playSound(
                        Sound.sound(Key.key("minecraft:block.glass.break"), Sound.Source.BLOCK, 1f, 1f),
                        loc.getX(), loc.getY(), loc.getZ()
                );


                // Customisation

                ItemMeta meta = meth1.getItemMeta();


                meta.lore(List.of(
                        Component.text(""),
                        MiniMessage.miniMessage().deserialize("<gradient:#6179DD:#7754F4><!italic>ɪ ᴡ</!italic></gradient><gradient:#7754F4:#1FA4D7><!italic>ᴀѕ ɢᴏᴏᴅ </!italic></gradient><gradient:#1FA4D7:#5BE3EB><!italic>ᴀᴛ ɪᴛ.</!italic></gradient>"),
                        Component.text(""),
                        MiniMessage.miniMessage().deserialize("<!i><gradient:#6179DD:#7754F4><shadow:#181E37:1>-</shadow><shadow:#191D38:1>-</shadow><shadow:#1A1C39:1>-</shadow><shadow:#1A1B39:1>-</shadow></gradient><gradient:#7754F4:#1FA4D7><shadow:#1B1A3A:1>-</shadow><shadow:#1C183B:1>-</shadow><shadow:#1D173C:1>-</shadow><shadow:#1D163C:1>-</shadow><shadow:#1E153D:1>-</shadow><shadow:#1B183C:1>-</shadow><shadow:#181B3B:1>-</shadow><shadow:#151E3A:1>-</shadow><shadow:#112039:1>-</shadow><shadow:#0E2338:1>-</shadow><shadow:#0B2637:1>-</shadow></gradient><gradient:#1FA4D7:#5BE3EB><shadow:#082936:1>-</shadow><shadow:#0A2B37:1>-</shadow><shadow:#0C2E37:1>-</shadow><shadow:#0E3038:1>-</shadow><shadow:#113239:1>-</shadow><shadow:#13343A:1>-</shadow><shadow:#15373A:1>-</shadow><shadow:#17393B:1>-</shadow></gradient></!i>"),
                        MiniMessage.miniMessage().deserialize("<!i><color:#6179DD>* 70% ᴄʜᴀɴᴄᴇ ᴏꜰ ɴᴀᴜѕᴇᴀ</color></!i>"),
                        MiniMessage.miniMessage().deserialize("<!i><color:#6179DD>* 50% ᴄʜᴀɴᴄᴇ ᴏꜰ ᴅᴀʀᴋɴᴇѕѕ</color></!i>"),
                        MiniMessage.miniMessage().deserialize("<!i><color:#6179DD>* 20% ᴄʜᴀɴᴄᴇ ᴏꜰ ʙᴏᴛʜ</color></!i>"),
                        MiniMessage.miniMessage().deserialize("<!i><color:#6179DD>* 75% ᴄʜᴀɴᴄᴇ ᴏꜰ ᴘᴏɪѕᴏɴ</color></!i>"),
                        MiniMessage.miniMessage().deserialize("<!i><color:#6179DD>* ѕᴛʀᴇɴɢᴛʜ 2 + ѕᴘᴇᴇᴅ 1</color></!i>"),
                        Component.text(""),
                        MiniMessage.miniMessage().deserialize("<!i><color:#61BADD>ᴏꜰꜰᴇʀѕ ɪɴᴠɪɴᴄɪʙɪʟɪᴛʏ ꜰᴏʀ ᴀ ʙɪᴛ.</color></!i>")

                ));
                meth1.setItemMeta(meta);


                meth1.setData(DataComponentTypes.FOOD,
                        FoodProperties.food()
                                .canAlwaysEat(true)
                                .nutrition(0)
                                .saturation(0)
                                .build()
                        );

                //


                mainhand.setAmount(mainhand.getAmount() - 1);
                if(mainhand.getAmount() <= 0){
                    p.getInventory().setItemInMainHand(meth1);
                }
                else {
                    p.give(meth1);
                }

            }
        }

    }

    // -- custom crafting logic (where it replaces existing items in crafting grid)

    // my god i gotta figure out logic so it can replace items with empty bucket to make it realistic... :skull:
    // sry if it looks weird and janky, pls do lmk if there can be better logic
    // gonna be my first logic made all by myself without any ai / google help. I'm proud of it.

    @EventHandler
    public void OnCraft(CraftItemEvent e){
        Player p = (Player) e.getWhoClicked();

        ItemStack[] matrix = e.getInventory().getMatrix();

        if(!(p.getOpenInventory().getTopInventory() instanceof CraftingInventory)) return;

        // the holy grail that took almost 2 days to figure out
        for(int i=0; i < matrix.length; i++){
            if(matrix[i] != null){
                matrix[i] = matrix[i].clone();
            }
        }

        Bukkit.getScheduler().runTask(plugin, ()-> {
            if(!(p.getOpenInventory().getTopInventory() instanceof CraftingInventory)) return;

            CraftingInventory Inv = (CraftingInventory) p.getOpenInventory().getTopInventory();

            for(int i = 0; i < matrix.length; i++){
                ItemStack item = matrix[i];

                if(item == null || item.getType() == Material.AIR) continue;

                if(itemManager.getItemId(item).equals("mercury")){
                    ItemStack bucket = new ItemStack(Material.BUCKET);
                    ItemMeta meta = bucket.getItemMeta();

                    if(meta != null){
                        meta.getPersistentDataContainer().set(
                                itemKey,
                                PersistentDataType.STRING,
                                "buckethahanoob"
                        );

                        bucket.setItemMeta(meta);

                    }

                    int slot = i + 1;
                    Inv.setItem(slot, bucket);
                }

                else if (itemManager.getItemId(item).equals("phenylacetone") || itemManager.getItemId(item).equals("methylamine")){
                    ItemStack glass = new ItemStack(Material.GLASS_BOTTLE);
                    ItemMeta glassmeta = glass.getItemMeta();

                    if(glassmeta != null){
                        glassmeta.getPersistentDataContainer().set(
                                itemKey,
                                PersistentDataType.STRING,
                                "ezglassp"
                        );
                    }

                    glass.setItemMeta(glassmeta);

                    int slot = i + 1;
                    Inv.setItem(slot, glass);

                }
            }

            p.updateInventory();

        });

    }


    // IMP SHIT EXPERIMENTAL. (final part except dimension idea)

    @EventHandler
    public void onEatMeth(PlayerItemConsumeEvent e){

        Bukkit.getLogger().info("Cat 1: Consume Event Triggered!");
        Player p = e.getPlayer();
        ItemStack methItem = p.getInventory().getItemInMainHand();
        UUID playerId = p.getUniqueId();
        long currentTime = System.currentTimeMillis();


        // adding potion randomnessw

        Random methRandom = new Random();

        double roll = methRandom.nextDouble();

        if (methItem.getType() == Material.AIR) return;
        if (itemManager.getItemId(methItem).equals("n_methamphetamine")){


            /*// --- test logic for meth counting ---
            long fixedCooldown = 10000;
            Long previousTime = abilityCooldown.get(playerId);

            if (previousTime == null) {
                abilityCooldown.put(playerId, currentTime);
            }

            else if (currentTime - previousTime < 10) {
                if (methCount.get(playerId) == null){
                    methCount.put(playerId, 0);
                }
                else {
                    Integer previousCount = methCount.get(playerId);
                    Integer operationCount = previousCount + 1;
                    methCount.put(playerId, operationCount);
                }

                abilityCooldown.put(playerId, currentTime);
            } else {
                abilityCooldown.put(playerId, currentTime);
            }
*/
            // --- test

            p.setCooldown(itemManager.create("n_methamphetamine"), 40);

            // sound effects. I'm pretty bad at this actually

            p.playSound(Sound.sound(Key.key("blockingbad", "crystal_smell"), Sound.Source.MASTER, 1f, 1f));


            p.sendMessage(MiniMessage.miniMessage().deserialize("<!i><gradient:#6179DD:#7754F4><shadow:#181E37:1>ɴ</shadow><shadow:#191D38:1>ᴜ</shadow><shadow:#191C38:1>ᴍ</shadow><shadow:#1A1B39:1>ʙ</shadow><shadow:#1A1A39:1> </shadow></gradient><gradient:#7754F4:#1FA4D7><shadow:#1B1A3A:1>ᴛ</shadow><shadow:#1C193B:1>ᴏ</shadow><shadow:#1C183B:1> </shadow><shadow:#1D173C:1>ᴅ</shadow><shadow:#1D163C:1>ᴀ</shadow><shadow:#1E153D:1>ᴍ</shadow><shadow:#1C173C:1>ᴀ</shadow><shadow:#19193B:1>ɢ</shadow><shadow:#171C3B:1>ᴇ</shadow><shadow:#141E3A:1> </shadow><shadow:#122039:1>ꜰ</shadow><shadow:#0F2238:1>ᴏ</shadow><shadow:#0D2538:1>ʀ</shadow><shadow:#0A2737:1> </shadow><shadow:#082936:1>1</shadow></gradient><gradient:#1FA4D7:#5BE3EB><shadow:#0A2B37:1>0</shadow><shadow:#0B2C37:1> </shadow><shadow:#0D2E38:1>ѕ</shadow><shadow:#0E2F38:1>ᴇ</shadow><shadow:#103139:1>ᴄ</shadow><shadow:#113339:1>ᴏ</shadow><shadow:#13343A:1>ɴ</shadow><shadow:#14363A:1>ᴅ</shadow><shadow:#16373B:1>ѕ</shadow><shadow:#17393B:1>.</shadow></gradient></!i>"));

            Bukkit.getLogger().info("Cat 3: Meth Detected!");

            PotionEffect tuffNausea = new PotionEffect(PotionEffectType.NAUSEA, 400, 1);
            PotionEffect tuffBlindness = new PotionEffect(PotionEffectType.BLINDNESS, 400, 2);
            PotionEffect tuffPoison = new PotionEffect(PotionEffectType.POISON, 200, 1);
            PotionEffect speedHack = new PotionEffect(PotionEffectType.SPEED, 100, 1);
            PotionEffect strengthHack = new PotionEffect(PotionEffectType.STRENGTH, 100, 2);

            p.addPotionEffect(speedHack);
            p.addPotionEffect(strengthHack);

            if (roll < 0.20){
                p.addPotionEffect(tuffNausea);
                p.addPotionEffect(tuffBlindness);
            } else {
                if (methRandom.nextDouble() < 0.70){
                    p.addPotionEffect(tuffNausea);
                }
                else if (methRandom.nextDouble() < 0.50){
                    p.addPotionEffect(tuffBlindness);
                }
            }

            if (methRandom.nextDouble() < 0.70){
                p.addPotionEffect(tuffPoison);
            }

            uuidStore.put(p.getUniqueId(), "test");

        }


    }


    // IMP FUNCTIONALITY




    @EventHandler
    public void onDamageEvent(EntityDamageEvent e){

        if (!(e.getEntity() instanceof Player damagedPlayer)) return;
        UUID playerId = damagedPlayer.getUniqueId();
        Double damageValue = e.getDamage();

        if (uuidStore.containsKey(playerId)){


            if (methCount.get(playerId) == null || methCount.get(playerId) == 1 || methCount.get(playerId) == 2 || methCount.get(playerId) == 3) {
                e.setCancelled(true);

                if (calculatedDamage.get(playerId) == null){
                    calculatedDamage.put(playerId, damageValue);
                }
                else {
                    Double finalDamage = calculatedDamage.get(playerId) + damageValue;
                    calculatedDamage.put(playerId, finalDamage);

                }

                Bukkit.getLogger().info("Task 10: MethCount taken and damage updated.");
            }

            else if (methCount.get(playerId) == 4){
                Double newDamage = damageValue*20/100 ;
                Double finalDamage = calculatedDamage.get(playerId) + newDamage;
                calculatedDamage.put(playerId, 100 - finalDamage);
            }

            else if (methCount.get(playerId) == 5){
                Double newDamage = damageValue*50/100 ;
                Double finalDamage = calculatedDamage.get(playerId) + newDamage;
                calculatedDamage.put(playerId, 100 - finalDamage);
            }

            else if (methCount.get(playerId) == 6){
                Double newDamage = damageValue*80/100 ;
                Double finalDamage = calculatedDamage.get(playerId) + newDamage;
                calculatedDamage.put(playerId, 100 - finalDamage);
            }

            else {
                e.setCancelled(false);
            }


            // ok im gonna try some genius stuff, may look not neat afs


            /*Bukkit.getLogger().info("Anti Damage Started.");

            if (e.getCause() == EntityDamageEvent.DamageCause.CUSTOM) return;

            if (damageStorage.get(playerId) != null) {
                Double finalValue = damageValue + damageStorage.get(playerId);
                damageStorage.put(playerId ,finalValue);
            } else {

                damageStorage.put(playerId, damageValue);
                new BukkitRunnable(){
                    @Override
                    public void run(){
                        Bukkit.getLogger().info("Anti-Damage ended.");
                        uuidStore.remove(playerId);
                        damagedPlayer.damage(calculatedDamage.get(playerId));
                        damageStorage.remove(playerId);
                        this.cancel();
                    }
                }.runTaskLater(plugin, 200L);


            }

            Bukkit.getLogger().info("Got player ID and set damage.");*/

        }

    }

    // meth count , still on beta.

    public void methDamage (Player p){

        Bukkit.getLogger().info("Task3: Initiated");
        UUID playerId = p.getUniqueId();
        BukkitTask oldTask = damageTask.remove(playerId);

        if (oldTask != null) {
            oldTask.cancel();
        }

        BukkitTask newTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {

            Bukkit.getLogger().info("Task4: BukkitRunnable working");

            uuidStore.remove(playerId);
            methCount.remove(playerId);
            damageCooldown.remove(playerId);
            cooldownCheck.remove(playerId);
            oldTime.remove(playerId);
            damageTask.remove(playerId);

            p.damage(calculatedDamage.get(p.getUniqueId()));
            calculatedDamage.remove(playerId);
            methCount.put(p.getUniqueId(), null);



        }, damageCooldown.get(p.getUniqueId()));

        damageTask.put(playerId, newTask);

    }

    @EventHandler
    public void onConsumeMeth(PlayerItemConsumeEvent e){
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        Long currentTime = System.currentTimeMillis();
        long initialCooldown = 200;

        if(!(itemManager.getItemId(mainHandItem).equals("n_methamphetamine"))) return;

        if (methCount.get(playerId) == null){
            methCount.put(playerId, 1);
            oldTime.put(playerId, currentTime);
            damageCooldown.put(playerId, initialCooldown);
            cooldownCheck.put(playerId, initialCooldown);
            methDamage(p);

            Bukkit.getLogger().info("Task1: Meth Count: 1");

        }
        else {
            Bukkit.getLogger().info("Task2: Stacking Deteted");
            int getCount = methCount.get(playerId);
            methCount.put(playerId, getCount + 1);
            Long calcTime = currentTime - oldTime.get(playerId);

            if (calcTime/50 > cooldownCheck.get(playerId)) return;
            Long finalTime = calcTime/50 + initialCooldown;
            damageCooldown.put(playerId, finalTime);
            methDamage(p);
            cooldownCheck.put(playerId, finalTime);
            Bukkit.getLogger().info("Task3: Damage counter initiated");
        }

    }

    // --- block



// uhh fixing sound:

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        UUID playerID = p.getUniqueId();
        ItemStack methDetect = p.getInventory().getItemInMainHand();
        long cooldownMS = 2000;
        Long currentTime = System.currentTimeMillis();

        if (!(itemManager.getItemId(methDetect).equals("n_methamphetamine"))) return;

        if (p.hasCooldown(methDetect.getType())) return;

        p.playSound(Sound.sound(Key.key("blockingbad", "crystal_sniff"), Sound.Source.MASTER, 1, 1));
        methSoundCooldwn.put(playerID, currentTime);
    }




// jesus it works, tears in my fricking eyes


}
