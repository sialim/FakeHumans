package me.sialim.fakehumans;

import me.sialim.fakehumans.ai.HomunculusAI;
import me.sialim.fakehumans.traits.FHHomunculusTrait;
import me.sialim.fakehumans.traits.FHOwnerTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.trait.AttributeTrait;
import net.citizensnpcs.trait.Controllable;
import net.citizensnpcs.trait.GameModeTrait;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;
import java.util.UUID;

public class NPCManager implements Listener {
    private final FakeHumans plugin;
    private final NPCRegistry registry;
    private final SkinManager skinManager;

    public NPCManager (FakeHumans plugin, SkinManager skinManager) {
        this.plugin = plugin;
        this.registry = CitizensAPI.getNPCRegistry();
        this.skinManager = skinManager;
    }

    public NPC createNPC (String name, Location location, UUID ownerUUID) {
        NPC npc = registry.createNPC(EntityType.PLAYER, name);
        //npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(Bukkit.getPlayer(ownerUUID));
        npc.getOrAddTrait(GameModeTrait.class).setGameMode(GameMode.SURVIVAL);
        npc.getOrAddTrait(AttributeTrait.class).setAttributeValue(Attribute.GENERIC_SCALE, 0.65);
        npc.data().setPersistent(NPC.Metadata.FLYABLE, false);
        npc.data().setPersistent(NPC.Metadata.PICKUP_ITEMS, true);
        npc.data().setPersistent(NPC.Metadata.COLLIDABLE, true);
        npc.data().setPersistent(NPC.Metadata.PATHFINDER_OPEN_DOORS, true);
        npc.data().setPersistent(NPC.Metadata.FLUID_PUSHABLE, true);
        npc.data().setPersistent(NPC.Metadata.TARGETABLE, true);
        npc.data().setPersistent(NPC.Metadata.HURT_SOUND, "ENTITY_ALLAY_HURT");
        npc.data().setPersistent(NPC.Metadata.LEASH_PROTECTED, false);
        npc.data().setPersistent(NPC.Metadata.SWIM, true);
        npc.data().setPersistent(NPC.Metadata.SHOULD_SAVE, true);
        npc.data().setPersistent(NPC.Metadata.PATHFINDER_FALL_DISTANCE, 2);
        npc.data().setPersistent(NPC.Metadata.REMOVE_FROM_PLAYERLIST, false);
        npc.data().setPersistent(NPC.Metadata.REMOVE_FROM_TABLIST, true);
        npc.data().setPersistent(NPC.Metadata.KNOCKBACK, true);


        FHOwnerTrait ownerTrait = npc.getOrAddTrait(FHOwnerTrait.class);
        if (ownerTrait != null) {
            ownerTrait.setOwner(ownerUUID);
        } else {
            Bukkit.getLogger().severe("Failed to add OwnerTrait to NPC.");
        }

        Player owner = Bukkit.getPlayer(ownerUUID);
        if (owner != null) {
            new HomunculusAI(npc, owner);
        } else {
            Bukkit.getLogger().severe("Owner player offline or doesn't exist.");
        }

        npc.setName(name);
        skinManager.setSkinFromUsername(npc, "smart112550");
        npc.spawn(location);


        return npc;
    }

    public void removeNPC(int npcID) {
        NPC npc = registry.getById(npcID);
        if (npc != null) {
            npc.despawn();
            registry.deregister(npc);
        }
    }

    public void followOwner(NPC npc, Player owner) {
        if (npc.hasTrait(FHOwnerTrait.class) && npc.getOrAddTrait(FHOwnerTrait.class).getOwner().equals(owner.getUniqueId())) {
            npc.getNavigator().getDefaultParameters().range(10);
            npc.getNavigator().setTarget(owner, true);
        }
    }

    public String generateRandomName(String ownerUsername) {
        Random random = new Random();
        String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        String baseName = ownerUsername.length() > 3 ? ownerUsername.substring(0, 3) : ownerUsername;
        StringBuilder randomName = new StringBuilder(baseName);
        for (int i = 0; i < 5; i++) {
            randomName.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }

        return randomName.toString();
    }

    public boolean areHostileMobsNearby(NPC npc, int radius) {
        Location npcLocation = npc.getEntity().getLocation();

        for (Entity entity : npcLocation.getWorld().getNearbyEntities(npcLocation, radius, radius, radius)) {
            if (entity instanceof Monster) {
                return true;
            }
        }
        return false;
    }

    public boolean areOtherPlayersNearby(NPC npc, Player owner, int radius) {
        Location npcLocation = npc.getEntity().getLocation();

        for (Entity entity : npcLocation.getWorld().getNearbyEntities(npcLocation, radius, radius, radius)) {
            if (entity instanceof Player && !entity.getUniqueId().equals(owner.getUniqueId())) {
                return true;
            }
        }
        return false;
    }

    public void patrolRandomly(NPC npc, int patrolRadius) {
        Location currentLocation = npc.getEntity().getLocation();

        double randomX = currentLocation.getX() + (Math.random() * patrolRadius * 2 - patrolRadius);
        double randomZ = currentLocation.getZ() + (Math.random() * patrolRadius * 2 - patrolRadius);
        Location patrolLocation = new Location(currentLocation.getWorld(), randomX, currentLocation.getY(), randomZ);

        npc.getNavigator().setTarget(patrolLocation);
    }


    @Deprecated public void handleNPCBehavior(NPC npc, Player owner) {
        int followRange = 10;
        int fleeRadius = 15;
        int patrolRadius = 20;
        int patrolInterval = 20;
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!npc.isSpawned() || !owner.isOnline()) return;
            Location npcLocation = npc.getEntity().getLocation();
            Location ownerLocation = owner.getLocation();
            double distanceToOwner = npcLocation.distance(ownerLocation);
            if (areHostileMobsNearby(npc, fleeRadius)) {
                Location fleeLocation = npcLocation.clone().add(npcLocation.toVector().subtract(ownerLocation.toVector()).normalize().multiply(5));
                npc.getNavigator().setTarget(fleeLocation);
            } else if (distanceToOwner <= followRange) {
                npc.getNavigator().setTarget(owner, true);
            } else if (areOtherPlayersNearby(npc, owner, patrolRadius)) {
                patrolRandomly(npc, patrolRadius);
            } else {
                npc.getNavigator().cancelNavigation();
            }
        }, 0L, patrolInterval);
    }

    @EventHandler public void onPlayerRightClickNPC(NPCRightClickEvent e) {
        NPC clickedNPC = e.getNPC();
        Player p = e.getClicker();

        Bukkit.getLogger().info("Player right-clicked NPC: " + clickedNPC.getId());

        if (clickedNPC.hasTrait(FHHomunculusTrait.class)) {
            FHHomunculusTrait trait = clickedNPC.getOrAddTrait(FHHomunculusTrait.class);
            Bukkit.getLogger().info("NPC has Homunculus trait");
            if (trait.getOwner().equals(p)) {
                trait.getSitBehavior().toggleSit();
                Bukkit.getLogger().info("Player toggled Homunculus");
            }
        }
    }
}