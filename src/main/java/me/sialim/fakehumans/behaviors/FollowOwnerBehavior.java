package me.sialim.fakehumans.behaviors;

import me.sialim.fakehumans.traits.FHHomunculusTrait;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class FollowOwnerBehavior extends BehaviorGoalAdapter {
    private final NPC npc;
    private final Player owner;

    public FollowOwnerBehavior(NPC npc, Player owner) {
        this.npc = npc;
        this.owner = owner;
    }

    @Override
    public void reset() {
        npc.getNavigator().cancelNavigation();
    }

    @Override
    public BehaviorStatus run() {
        double distanceToOwner = npc.getEntity().getLocation().distance(owner.getLocation());
        double minFollowDistance = 2.0;
        double maxFollowDistance = 40.0;
        Location npcLocation = npc.getEntity().getLocation();

        if (!npc.getNavigator().isNavigating()) {
            if (distanceToOwner > minFollowDistance && distanceToOwner < maxFollowDistance) {
                npc.getNavigator().setTarget(owner, false);
            } else if (distanceToOwner <= minFollowDistance) {
                npc.getNavigator().cancelNavigation();
            }
        }
        return BehaviorStatus.RUNNING;
    }

    @Override
    public boolean shouldExecute() {
        if (npc.hasTrait(FHHomunculusTrait.class)) {
            FHHomunculusTrait trait = npc.getOrAddTrait(FHHomunculusTrait.class);
            if (trait.getSitBehavior().isSitting) {
                return false;
            }
        }
        return owner.isOnline() && owner.getLocation().distance(npc.getEntity().getLocation()) < 20;
    }
}
