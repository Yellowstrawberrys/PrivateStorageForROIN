package me.yellowstrawberry.privatestorage;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class PrivateStorage extends JavaPlugin {

    DataManager dataManager;
    Map<String, Inventory> inv = new HashMap<>();

    int backupTaskId = -1;

    @Override
    public void onEnable() {
        dataManager = new DataManager();
        // Backup (Per 1 hours)
        backupTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for(Map.Entry<String, Inventory> entry : inv.entrySet()) {
                dataManager.saveInventory(entry.getValue(), entry.getKey());
            }
        }, 20*60*60, 20*60*60);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getScheduler().cancelTask(backupTaskId);
        System.out.println("Saving Inventories...");
        long before = System.currentTimeMillis();
        for(Map.Entry<String, Inventory> entry : inv.entrySet()) {
            entry.getValue().close();
            dataManager.saveInventory(entry.getValue(), entry.getKey());
        }
        System.out.printf("Done (%3.2f)!", ((float) (System.currentTimeMillis()-before)) / 1000f);
        dataManager.closeConnection();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof Player && command.getName().equals("openPrivateStorage")) {
            openStorage((Player) sender);
        }
        return super.onCommand(sender, command, label, args);
    }

    private void openStorage(Player p) {
        if(!inv.containsKey(p.getUniqueId().toString())) inv.put(p.getUniqueId().toString(), (dataManager.loadInventory(p.getUniqueId().toString()) != null ? dataManager.loadInventory(p.getUniqueId().toString()) : Bukkit.createInventory(null, 27)));
        p.openInventory(inv.get(p.getUniqueId().toString()));
    }
}
