package pl.pcraft.specjalnykompas;

import net.md_5.babi.api.ChatColor; // Klasyczny kolor lub fabryka komponentów Paper
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;

public final class SpecjalnyKompas extends JavaPlugin implements CommandExecutor {

    private Location celNode = null;
    private final String NickAdmina = "Pcraft600";

    @Override
    public void onEnable() {
        this.getCommand("gra").setExecutor(this);
        startOdliczanieDystansu();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equals("1")) {
            if (sender instanceof Player) {
                Player admin = (Player) sender;
                // Ustawiamy cel w miejscu, gdzie stoi Pcraft600 (lub ktokolwiek z uprawnieniem, kto wpisze)
                celNode = admin.getLocation();
                celNode.getBlock().setType(Material.BARRIER);
            }

            // Ekwipunek dla wszystkich graczy
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.getInventory().clear();

                // Diamentowy set + tarcza + miecz
                p.getInventory().setHelmet(new ItemStack(Material.DIAMOND_HELMET));
                p.getInventory().setChestplate(new ItemStack(Material.DIAMOND_CHESTPLATE));
                p.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                p.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                p.getInventory().setItemInOffHand(new ItemStack(Material.SHIELD));
                p.getInventory().addItem(new ItemStack(Material.DIAMOND_MIECZ));

                // Specjalny kompas kierujący na cel
                ItemStack kompas = new ItemStack(Material.COMPASS);
                CompassMeta meta = (CompassMeta) kompas.getItemMeta();
                if (meta != null && celNode != null) {
                    meta.setLodestoneTracked(false);
                    meta.setLodestone(celNode);
                    meta.setDisplayName("§6Kompas Lokacyjny");
                    kompas.setItemMeta(meta);
                }
                p.getInventory().addItem(kompas);
                p.sendMessage("§aGra się rozpoczęła! Szukaj celu!");
            }

            // Dodatkowa nagroda/blok dla Ciebie (Pcraft600)
            Player pcraft = Bukkit.getPlayer(NickAdmina);
            if (pcraft != null) {
                ItemStack specjalnaBariera = new ItemStack(Material.BARRIER);
                ItemMeta bMeta = specjalnaBariera.getItemMeta();
                if (bMeta != null) {
                    bMeta.setDisplayName("§c§lBlok Celu Gry");
                    bMeta.setLore(Collections.singletonList("§7Postawienie tego bloku wyznacza punkt."));
                    specjalnaBariera.setItemMeta(bMeta);
                }
                pcraft.getInventory().addItem(specjalnaBariera);
            }
            return true;
        }
        return false;
    }

    // Licznik działający w tle co 5 ticków (1/4 sekundy)
    private void startOdliczanieDystansu() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (celNode == null) return;

                for (Player p : Bukkit.getOnlinePlayers()) {
                    ItemStack mainHand = p.getInventory().getItemInMainHand();
                    ItemStack offHand = p.getInventory().getItemInOffHand();

                    // Sprawdzamy czy trzyma kompas
                    if (mainHand.getType() == Material.COMPASS || offHand.getType() == Material.COMPASS) {
                        double dystans = p.getLocation().distance(celNode);
                        
                        // Generowanie prostej strzałki kierunku (lewo/prawo/prosto na bazie yaw)
                        String strzalka = getKierunekStrzalki(p, celNode);

                        // Wyświetlanie nad paskiem ekwipunku (Actionbar) – czysty i nie migający sposób
                        p.sendActionBar("§eCel: " + strzalka + " §b" + (int) dystans + " m §edopo celu");
                    }
                }
            }
        }.runTaskTimer(this, 0L, 5L);
    }

    private String getKierunekStrzalki(Player p, Location target) {
        // Obliczanie kąta między graczem a celem
        double dx = target.getX() - p.getLocation().getX();
        double dz = target.getZ() - p.getLocation().getZ();
        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        angle = (angle < 0) ? angle + 360 : angle;

        double yaw = p.getLocation().getYaw();
        yaw = (yaw < 0) ? yaw + 360 : yaw;

        double diff = angle - yaw;
        if (diff < 0) diff += 360;

        // Zwracanie strzałki na podstawie obrotu głowy gracza
        if (diff >= 337.5 || diff < 22.5) return "▲";
        if (diff >= 22.5 && diff < 67.5) return "◥";
        if (diff >= 67.5 && diff < 112.5) return "▶";
        if (diff >= 112.5 && diff < 157.5) return "◢";
        if (diff >= 157.5 && diff < 202.5) return "▼";
        if (diff >= 202.5 && diff < 247.5) return "◣";
        if (diff >= 247.5 && diff < 292.5) return "◀";
        return "◤";
    }
}
