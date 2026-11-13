package com.tesseractstudios.listeners;



import aQute.bnd.annotation.metatype.Meta;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateHealth;
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
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import javax.swing.*;
import java.util.*;

import static com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes.INT;


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
    private final Map<UUID, Long> methSoundCooldwn = new HashMap<>();
    private final Map<UUID, Double> calculatedDamage = new HashMap<>();

    //exp

    private final Map<UUID, Integer> methCount = new HashMap<>();
    private final Map<UUID, Long> oldTime = new HashMap<>();
    private final Map<UUID, Long> damageCooldown = new HashMap<>();
    private final Map<UUID, BukkitTask> damageTask = new HashMap<>();
    private final Map<UUID, Long> cooldownCheck = new HashMap<>();
    private final Map<UUID, BukkitTask> breathEffectTask = new HashMap<>();

    private final Map<UUID, BukkitTask> vignetteTask = new HashMap<>();

    private static final double BORDER_HALF_SIZE = 50_000;
    private static final double MAX_INTENSITY = 0.9;
    private static final long FADE_TICKS = 200;


    // -- Custom Crafting Logic wa--


    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e){
        // Bukkit.getLogger().info("Join event detected");
        Player p = e.getPlayer();
        UUID playerUUID = p.getUniqueId();

        if (calculatedDamage.get(playerUUID) == null) return;
        methDamage(p);

    }

    @EventHandler
    public void onLeaveEvent(PlayerQuitEvent e){
        //Bukkit.getLogger().info("Player Leave event detected");
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if(!(uuidStore.containsKey(playerId))) return;
        //Bukkit.getLogger().info("Player uuid get done");

        calculatedDamage.put(playerId, calculatedDamage.get(playerId));
        BukkitTask damageRemove = damageTask.remove(playerId);
        damageCooldown.put(playerId, 40L);
        damageRemove.cancel();
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

        //System.out.println("If this works then lammy's gay");
        Player player = e.getPlayer();
        ItemStack eatitem = player.getInventory().getItemInMainHand();

        // - idk dawg test -, ok works ez
        if(eatitem.getType() == Material.TROPICAL_FISH || eatitem.getType() == Material.COOKED_COD || eatitem.getType() == Material.SALMON || eatitem.getType() == Material.COOKED_SALMON || eatitem.getType() == Material.COD){
            e.setCancelled(true);
            eatitem.setAmount(eatitem.getAmount() -1);

            // nutrition logic

            ItemMeta foodMeta = eatitem.getItemMeta();
            float foodSaturation = foodMeta.getFood().getSaturation();
            int foodHunger = foodMeta.getFood().getNutrition();

            player.setFoodLevel(foodHunger);
            player.setSaturation(foodSaturation);


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
        if (itemManager.getItemId(mainhand) == null) return;
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
                        MiniMessage.miniMessage().deserialize("<!i><color:#6179DD>* ѕᴛʀᴇɴɢᴛʜ 2 + ѕᴘᴇᴇᴅ 1</color></!i>"),
                        Component.text(""),
                        MiniMessage.miniMessage().deserialize("<!i><color:#61BADD>ɴᴜᴍʙ ᴛᴏ ᴅᴀᴍᴀɢᴇ ꜰᴏʀ 10 ѕᴇᴄᴏɴᴅѕ. (ᴇxᴛᴇɴᴅᴀʙʟᴇ)</color></!i>")

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

        //Bukkit.getLogger().info("Cat 1: Consume Event Triggered!");
        Player p = e.getPlayer();
        ItemStack methItem = p.getInventory().getItemInMainHand();
        ItemStack offHandItem = p.getInventory().getItemInOffHand();

        if (itemManager.getItemId(offHandItem) != null) {
            if (itemManager.getItemId(offHandItem).equals("n_methamphetamine")){
                methItem = itemManager.create("n_methamphetamine");
            }
        }


        // adding potion randomnessw

        Random methRandom = new Random();

        double roll = methRandom.nextDouble();

        if (methItem.getType() == Material.AIR) return;
        if (itemManager.getItemId(methItem).equals("n_methamphetamine")){

            p.setCooldown(itemManager.create("n_methamphetamine"), 40);

            // sound effects. I'm pretty bad at this actually

            p.getWorld().playSound(Sound.sound(Key.key("blockingbad:crystal_smell"), Sound.Source.MASTER, 1f, 1f));


            //p.sendMessage(MiniMessage.miniMessage().deserialize("<!i><gradient:#6179DD:#7754F4><shadow:#181E37:1>ɴ</shadow><shadow:#191D38:1>ᴜ</shadow><shadow:#191C38:1>ᴍ</shadow><shadow:#1A1B39:1>ʙ</shadow><shadow:#1A1A39:1> </shadow></gradient><gradient:#7754F4:#1FA4D7><shadow:#1B1A3A:1>ᴛ</shadow><shadow:#1C193B:1>ᴏ</shadow><shadow:#1C183B:1> </shadow><shadow:#1D173C:1>ᴅ</shadow><shadow:#1D163C:1>ᴀ</shadow><shadow:#1E153D:1>ᴍ</shadow><shadow:#1C173C:1>ᴀ</shadow><shadow:#19193B:1>ɢ</shadow><shadow:#171C3B:1>ᴇ</shadow><shadow:#141E3A:1> </shadow><shadow:#122039:1>ꜰ</shadow><shadow:#0F2238:1>ᴏ</shadow><shadow:#0D2538:1>ʀ</shadow><shadow:#0A2737:1> </shadow><shadow:#082936:1>1</shadow></gradient><gradient:#1FA4D7:#5BE3EB><shadow:#0A2B37:1>0</shadow><shadow:#0B2C37:1> </shadow><shadow:#0D2E38:1>ѕ</shadow><shadow:#0E2F38:1>ᴇ</shadow><shadow:#103139:1>ᴄ</shadow><shadow:#113339:1>ᴏ</shadow><shadow:#13343A:1>ɴ</shadow><shadow:#14363A:1>ᴅ</shadow><shadow:#16373B:1>ѕ</shadow><shadow:#17393B:1>.</shadow></gradient></!i>"));

            //Bukkit.getLogger().info("Cat 3: Meth Detected!");

            PotionEffect tuffNausea = new PotionEffect(PotionEffectType.NAUSEA, 400, 1);
            PotionEffect tuffBlindness = new PotionEffect(PotionEffectType.BLINDNESS, 400, 2);
            PotionEffect speedHack = new PotionEffect(PotionEffectType.SPEED, 100, 1);
            PotionEffect strengthHack = new PotionEffect(PotionEffectType.STRENGTH, 100, 2);
            PotionEffect waterHack = new PotionEffect(PotionEffectType.WATER_BREATHING, 999, 1);

            p.addPotionEffect(speedHack);
            p.addPotionEffect(strengthHack);
            p.addPotionEffect(waterHack);

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

            uuidStore.put(p.getUniqueId(), "test");

        }


    }


    // IMP FUNCTIONALITY




    @EventHandler
    public void ongeEvent(EntityDamageEvent e){

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

                //Bukkit.getLogger().info("Task 10: MethCount taken and damage updated.");
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

        }

    }

    // meth count , still on beta.

    public void methDamage (Player p){

        //Bukkit.getLogger().info("Task3: Initiated");
        UUID playerId = p.getUniqueId();
        BukkitTask oldTask = damageTask.remove(playerId);

        if (oldTask != null) {
            oldTask.cancel();
        }


        BukkitTask newTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {

            //Bukkit.getLogger().info("Task4: BukkitRunnable working");

            uuidStore.remove(playerId);
            methCount.remove(playerId);
            damageCooldown.remove(playerId);
            cooldownCheck.remove(playerId);
            oldTime.remove(playerId);
            damageTask.remove(playerId);
            p.removePotionEffect(PotionEffectType.WATER_BREATHING);

            // test
            // wow works perfectly fine! Better solution than the one mentioned in JavaDocs..
            BukkitTask oldBreathTask = breathEffectTask.remove(playerId);

            if (oldBreathTask != null){
                oldBreathTask.cancel();
            }

            // for underwater
            Block getEyeBlock = p.getEyeLocation().getBlock();
            if (getEyeBlock.getType() == Material.WATER){
                p.setRemainingAir(0);
            } else {
                sendAir(p);
            }

            p.damage(calculatedDamage.get(p.getUniqueId()));
            methVignette(p);

            calculatedDamage.remove(playerId);
            methCount.put(p.getUniqueId(), null);



        }, damageCooldown.get(p.getUniqueId()));

        damageTask.put(playerId, newTask);

    }


    // wait bruh why did i make two of itemconsumeevents
    @EventHandler
    public void onConsumeMeth(PlayerItemConsumeEvent e){
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();
        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        ItemStack offHandItem = p.getInventory().getItemInOffHand();
        Long currentTime = System.currentTimeMillis();
        long initialCooldown = 200;

        if (itemManager.getItemId(offHandItem) != null) {
            if (itemManager.getItemId(offHandItem).equals("n_methamphetamine")){
                mainHandItem = itemManager.create("n_methamphetamine");
            }
        }

        if(itemManager.getItemId(mainHandItem) == null) return;
        if(!(itemManager.getItemId(mainHandItem).equals("n_methamphetamine"))) return;

        if (methCount.get(playerId) == null){
            methCount.put(playerId, 1);
            oldTime.put(playerId, currentTime);
            damageCooldown.put(playerId, initialCooldown);
            cooldownCheck.put(playerId, currentTime);
            methDamage(p);
            breathEffect(p);


            //Bukkit.getLogger().info("Task1: Meth Count: 1");

        }
        else {
            //Bukkit.getLogger().info("Task2: Stacking Deteted");
            int getCount = methCount.get(playerId);
            methCount.put(playerId, getCount + 1);
            Long calcTime = currentTime - oldTime.get(playerId);

            if (calcTime/50 > cooldownCheck.get(playerId)) return;
            Long finalTime = calcTime/50 + initialCooldown;
            damageCooldown.put(playerId, finalTime);
            methDamage(p);
            breathEffect(p);
            cooldownCheck.put(playerId, currentTime);
            //Bukkit.getLogger().info("Task3: Damage counter initiated");
        }

    }

    // --- block



// uhh fixing sound:

    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        UUID playerID = p.getUniqueId();
        ItemStack methDetect = p.getInventory().getItemInMainHand();
        ItemStack methDetectOffhand = p.getInventory().getItemInOffHand();

        Long currentTime = System.currentTimeMillis();
        if (p.hasCooldown(methDetect.getType())) return;
        Action  rightClickCheck = e.getAction();
        if (rightClickCheck == Action.LEFT_CLICK_AIR || rightClickCheck == Action.LEFT_CLICK_BLOCK) return;

        // im not sure && would still cause null pointer exception, this is the first time i've used it, will cleanup code in future if it doesn't cause npe

        if (itemManager.getItemId(methDetectOffhand) != null && itemManager.getItemId(methDetectOffhand).equals("n_methamphetamine")){
            methDetect = methDetectOffhand;
        }

        if (itemManager.getItemId(methDetect) == null) return;
        if (!(itemManager.getItemId(methDetect).equals("n_methamphetamine"))) return;



        p.getWorld().playSound(Sound.sound(Key.key("blockingbad:crystal_sniff"), Sound.Source.MASTER, 1f, 1f));
        methSoundCooldwn.put(playerID, currentTime);
    }

    @EventHandler
    public void onDeath (PlayerDeathEvent e){
        Player p = e.getPlayer();
        UUID playerId = p.getUniqueId();

        if (calculatedDamage.get(playerId) == null) return;

        BukkitTask damageEvent = damageTask.remove(playerId);
        if (damageEvent != null) {
            damageEvent.cancel();
        }
        BukkitTask breathTask = breathEffectTask.remove(playerId);
        if (breathTask != null){
            breathTask.cancel();
        }
        BukkitTask vignete = vignetteTask.remove(playerId);
        if (vignete != null){
            vignete.cancel();
        }

        //clearing stuffs

        uuidStore.remove(playerId);
        methCount.remove(playerId);
        damageCooldown.remove(playerId);
        cooldownCheck.remove(playerId);
        oldTime.remove(playerId);
        damageTask.remove(playerId);
        calculatedDamage.remove(playerId);
    }



    // ---- packets!! I've never worked on it so the code can be buggy.. ----



    public void sendAir (Player p){

        EntityData<?> airData = new EntityData<>(
                1,
                INT,
                300
        );

        WrapperPlayServerEntityMetadata airPacket = new WrapperPlayServerEntityMetadata(
                p.getEntityId(),
                Collections.singletonList(airData)
        );

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(p, airPacket);

    }

    // IT WORKS

    public void breathEffect(Player p) {

        UUID playerId = p.getUniqueId();
        BukkitTask oldBreathTask = breathEffectTask.remove(playerId);

        if (oldBreathTask != null){
            oldBreathTask.cancel();
        }


        BukkitTask breathTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            //Bukkit.getLogger().info("Effect running!");


            long currenTime = System.currentTimeMillis();
            Long test = cooldownCheck.get(playerId);
            if (test == null) return;
            long value = currenTime - test;
            int convertedValue = (int) value / 50;

            if (uuidStore.get(playerId) == null) return;

            long total = damageCooldown.get(playerId);
            long cooldownGet = total - convertedValue;


            int calculatedRemainingAir = (int) Math.max(0, 300L * cooldownGet / total);
            if (calculatedRemainingAir >= 300) calculatedRemainingAir = 299;



            EntityData<?> data = new EntityData<>(
                    1,
                    INT,
                    calculatedRemainingAir
            );

            WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(
                    p.getEntityId(),
                    Collections.singletonList(data)
            );

            PacketEvents.getAPI()
                    .getPlayerManager()
                    .sendPacket(p, packet);

        }, 0, 20);

        breathEffectTask.put(playerId, breathTask);

        // jesus it works, tears in my fricking eyes test
    }

    // FINAL THING GUYS


    public void methVignette(Player p) {
        UUID playerId = p.getUniqueId();
        BukkitTask old = vignetteTask.remove(playerId);
        if (old != null) old.cancel();

        WorldBorder border = Bukkit.createWorldBorder();
        border.setCenter(p.getLocation());
        border.setSize(BORDER_HALF_SIZE * 2);
        border.setWarningDistance((int) (BORDER_HALF_SIZE / (1 - MAX_INTENSITY)));
        p.setWorldBorder(border);

        p.playSound(Sound.sound(Key.key("blockingbad", "heartbeat"), Sound.Source.MASTER, 1, 1));



        BukkitTask fade = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int elapsed = 0;
            public void run() {
                elapsed += 5;
                if (!p.isOnline() || elapsed >= FADE_TICKS) {
                    if (p.isOnline()) p.setWorldBorder(null);
                    BukkitTask self = vignetteTask.remove(playerId);
                    if (self != null) self.cancel();
                    return;
                }


                double progress  = (double) elapsed / FADE_TICKS;
                double intensity = MAX_INTENSITY * Math.pow(1 - progress, 2);
                border.setWarningDistance((int) (BORDER_HALF_SIZE / (1 - Math.max(0.02, intensity))));
            }
        }, 5L, 5L);
        vignetteTask.put(playerId, fade);
    }


}
