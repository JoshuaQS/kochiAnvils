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
import org.bukkit.event.inventory.InventoryCloseEvent;
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
        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        saveEnabledWorlds();
    }

    private void loadConfig() {
        enabledWorlds = new HashSet<>(getConfig().getStringList("enabledWorlds"));
    }

    private void saveEnabledWorlds() {
        getConfig().set("enabledWorlds", new ArrayList<>(enabledWorlds));
        super.saveConfig();
    }

    @EventHandler
    public void onAnvilUse(InventoryClickEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.RESULT) {
            AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
            World world = anvilInventory.getLocation().getWorld();

            if (world != null && enabledWorlds.contains(world.getName())) {
                // El yunque no se desgasta, pero permitimos que la reparación/encantamiento ocurra
                if (event.getCurrentItem() != null) {
                    // No cancelamos el evento, dejamos que el proceso se complete
                    // Guardamos temporalmente la ubicación del yunque para restaurar el tipo más adelante
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (anvilInventory.getLocation() != null) {
                            anvilInventory.getLocation().getBlock().setType(Material.ANVIL);
                        }
                    }, 1L); // 1 tick después para restaurar el tipo de yunque
                }
            }
        }
    }

    @EventHandler
    public void onAnvilClose(InventoryCloseEvent event) {
        if (event.getInventory().getType() == InventoryType.ANVIL) {
            AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
            World world = anvilInventory.getLocation().getWorld();

            // Restaurar el tipo de yunque cuando se cierra el inventario si está en un mundo habilitado
            if (world != null && enabledWorlds.contains(world.getName()) && anvilInventory.getLocation() != null) {
                Bukkit.getScheduler().runTaskLater(this, () -> {
                    anvilInventory.getLocation().getBlock().setType(Material.ANVIL);
                }, 1L);
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
                    saveEnabledWorlds();
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
