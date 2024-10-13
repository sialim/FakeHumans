package me.sialim.fakehumans.behaviors;

import me.sialim.fakehumans.traits.FHHomunculusTrait;
import me.sialim.fakehumans.traits.FHSitTrait;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WanderBehavior extends BehaviorGoalAdapter {
    private final NPC npc;
    private final Player owner;

    public WanderBehavior(NPC npc, Player owner) {
        this.npc = npc;
        this.owner = owner;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
    }

    @Override
    public BehaviorStatus run() {
        //Bukkit.getLogger().info("WanderBehavior running");
        Location randomLocation = npc.getEntity().getLocation().add(
                Math.random() * 10 - 5,
                0,
                Math.random() * 10 - 5
        );
        npc.getNavigator().setTarget(randomLocation);
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if (npc.hasTrait(FHHomunculusTrait.class)) {
            if (npc.hasTrait(FHSitTrait.class)) {
                if (npc.getOrAddTrait(FHSitTrait.class).isSitting()) {
                    return false;
                }
            }
        }
        return !owner.isOnline() || owner.getLocation().distance(npc.getEntity().getLocation()) >= 20;
    }
}
