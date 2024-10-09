package me.sialim.fakehumans.behaviors;

import me.sialim.fakehumans.traits.FHHomunculusTrait;
import net.citizensnpcs.api.ai.AttackStrategy;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.stream.Collectors;
import java.util.List;

public class CombatBehavior extends BehaviorGoalAdapter {
    private final NPC npc;
    private final Player owner;

    public CombatBehavior(NPC npc, Player owner) {
        this.npc = npc;
        this.owner = owner;
    }

    @Override
    public void reset() {

    }

    @Override
    public BehaviorStatus run() {
        LivingEntity target = findClosestHostileMob();
        if (target != null) {
            Location fleeLocation = npc.getEntity().getLocation().clone()
                    .add(npc.getEntity().getLocation().toVector().subtract(target.getLocation().toVector()).normalize().multiply(5));
            npc.getNavigator().setTarget(fleeLocation);
            return BehaviorStatus.RUNNING;
        } else {
            return BehaviorStatus.FAILURE;
        }
    }

    private boolean hasWeapon() {
        if (npc.getEntity() instanceof LivingEntity) {
            ItemStack heldItem = (((LivingEntity) npc.getEntity())).getEquipment().getItemInMainHand();
            return heldItem != null && (heldItem.getType().name().contains("SWORD") || heldItem.getType().name().contains("AXE"));
        }
        return false;
    }

    @Override
    public boolean shouldExecute() {
        if (npc.hasTrait(FHHomunculusTrait.class)) {
            FHHomunculusTrait trait = npc.getOrAddTrait(FHHomunculusTrait.class);
            if (trait.getSitBehavior().isSitting) {
                return false;
            }
        }

        List<Entity> nearbyMobs = npc.getEntity().getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof Monster)
                .toList();
        return !nearbyMobs.isEmpty();
    }

    private LivingEntity findClosestHostileMob() {
        return npc.getEntity().getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof Monster)
                .map(entity -> (LivingEntity) entity)
                .findFirst()
                .orElse(null);
    }
}
