package me.sialim.fakehumans.behaviors;

import me.sialim.fakehumans.traits.FHHomunculusTrait;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;

public class WanderBehavior extends BehaviorGoalAdapter {
    private final NPC npc;

    public WanderBehavior(NPC npc) {
        this.npc = npc;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
    }

    @Override
    public BehaviorStatus run() {
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
            FHHomunculusTrait trait = npc.getTrait(FHHomunculusTrait.class);
            if (trait.getSitBehavior().isSitting) {
                return false;
            }
        }
        return true;
    }
}
