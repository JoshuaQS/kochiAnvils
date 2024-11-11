package net.punchpvp.kochianvils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class kochiAnvils extends JavaPlugin implements Listener {

    private Set<String> enabledWorlds;

    @Override
    public void onEnable() {
        // Cargar configuración y mundos habilitados
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Guardar configuración
        saveConfig();
    }

    private void loadConfig() {
        enabledWorlds = new HashSet<>(getConfig().getStringList("enabledWorlds"));
    }

    public void saveConfig() {
        getConfig().set("enabledWorlds", new ArrayList<>(enabledWorlds));
        saveConfig();
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        // Verificar que el inventario es un yunque y el slot es el de resultado
        if (event.getInventory().getType() == InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.RESULT) {
            AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
            World world = anvilInventory.getLocation().getWorld();

            // Verificar si el mundo tiene yunques irrompibles habilitados
            if (world != null && enabledWorlds.contains(world.getName())) {
                // Asegurarse de que hay un elemento en la ranura de resultado
                if (event.getCurrentItem() != null) {
                    anvilInventory.getLocation().getBlock().setType(Material.ANVIL);
                    event.setCancelled(true);  // Cancelar para evitar desgaste
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("kochiAnvil")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("toggle") && sender instanceof Player) {
                    Player player = (Player) sender;
                    World world = player.getWorld();
                    String worldName = world.getName();

                    if (enabledWorlds.contains(worldName)) {
                        enabledWorlds.remove(worldName);
                        player.sendMessage("§cYunques irrompibles desactivados en el mundo: " + worldName);
                    } else {
                        enabledWorlds.add(worldName);
                        player.sendMessage("§aYunques irrompibles activados en el mundo: " + worldName);
                    }
                    saveConfig();
                    return true;

                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                        reloadConfig();
                        loadConfig();
                        sender.sendMessage("§aConfiguración de KochiAnvils recargada.");
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
