package me.sialim.fakehumans;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.sialim.calendar.Calendar;
import me.sialim.fakehumans.traits.FHHomunculusTrait;
import me.sialim.fakehumans.traits.FHOwnerTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.List;

public final class FakeHumans extends JavaPlugin implements TabExecutor {
    public ProtocolManager protocolManager;
    public NPCManager npcManager;
    public Calendar calendar;

    @Override
    public void onEnable() {
        protocolManager = ProtocolLibrary.getProtocolManager();
        npcManager = new NPCManager(this, new SkinManager(this));
        if (getServer().getPluginManager().getPlugin("Citizens") == null || !getServer().getPluginManager().getPlugin("Citizens").isEnabled()) {
            getLogger().log(Level.SEVERE, "Citizens 2.0 not found or not enabled");
        } else {
            try {
                getLogger().info("Registering FHOwnerTrait...");
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FHOwnerTrait.class));
                getLogger().info("Successfully registered FHOwnerTrait.");
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE, "Failed to register FHOwnerTrait: " + e.getMessage(), e);
            }

            try {
                getLogger().info("Registering FHHomunculusTrait...");
                CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(FHHomunculusTrait.class));
                getLogger().info("Successfully registered FHHomunculusTrait.");
            } catch (IllegalArgumentException e) {
                getLogger().log(Level.SEVERE, "Failed to register FHHomunculusTrait: " + e.getMessage(), e);
            }
        }

        getCommand("fakehumans").setExecutor(this);
        getCommand("fakehumans").setTabCompleter(this);
        getServer().getPluginManager().registerEvents(npcManager, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p && p.isOp()) {
            if (args.length >= 1) {
                switch (args[0]) {
                    case "create": {
                        Location loc = p.getLocation();
                        UUID ownerUUID = p.getUniqueId();
                        String npcName = "γ-";
                        if (args.length == 1) {
                            npcName += npcManager.generateRandomName(p.getName());
                        } else {
                            npcName += args[1];
                        }
                        NPC npc = npcManager.createNPC(npcName, loc, ownerUUID);
                        p.sendMessage(ChatColor.GREEN + "[FH] NPC " + npc.getId() + ": " + npcName + " created and following you.");
                        p.sendMessage();
                        return true;
                    }
                    case "erase": {
                        if (args.length == 1) {
                            p.sendMessage(ChatColor.RED + "[FH] Incorrect usage, do /fh erase <npcID>");
                            return false;
                        } else {
                            int npcID = Integer.parseInt(args[1]);
                            npcManager.removeNPC(npcID);
                            p.sendMessage(ChatColor.GREEN + "[FH] NPC erased and deleted from world.");
                        }
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "[FH] Incorrect usage, do /fh create");
                return false;
            }
        } else {
            sender.sendMessage("Only players are able to use this currently");
            return false;
        }
        return false;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                completions.addAll(Arrays.asList("create","erase"));
                //return completions;
            } else if (args.length == 2 && args[0].equalsIgnoreCase("erase")) {
                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                    completions.add(String.valueOf(npc.getId()));
                    //return completions;
                }
            }
            return completions;
        }
        return null;
    }

    public void setNPCSize(NPC npc, double size) {
        if (npc.isSpawned()) {
            Entity entity = npc.getEntity();
            ((LivingEntity) entity).getAttribute(Attribute.GENERIC_SCALE).setBaseValue(size);
        }
    }
}
