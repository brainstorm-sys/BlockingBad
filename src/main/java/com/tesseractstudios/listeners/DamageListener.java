package com.tesseractstudios.listeners;

import com.tesseractstudios.managers.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.http.WebSocket;
import java.util.UUID;

public class DamageListener implements Listener {


    private final JavaPlugin plugin;
    private final ItemManager itemManager;
    private final NamespacedKey itemKey;
    private final ItemStack meth1;

    public DamageListener(JavaPlugin plugin, ItemManager itemManager) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.meth1 = itemManager.create("n_methamphetamine");
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.itemKey = new NamespacedKey(plugin, "test");
    }




    @EventHandler
    public void onEatEvent (PlayerItemConsumeEvent e){
        Player p = e.getPlayer();
        ItemStack methItem = p.getInventory().getItemInMainHand();
        UUID PlayerId = p.getUniqueId();

        if (!(itemManager.getItemId(methItem).equals("n_methamphetamine"))) return;


    }


}
