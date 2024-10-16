package me.sialim.fakehumans.behaviors;

import me.sialim.fakehumans.traits.FHHomunculusTrait;
import me.sialim.fakehumans.traits.FHSitTrait;
import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
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
        if (!owner.isOnline()) {
            return BehaviorStatus.SUCCESS;
        }
        //Bukkit.getLogger().info("FollowOwnerBehavior running");
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
            if (npc.hasTrait(FHSitTrait.class)) {
                if (npc.getOrAddTrait(FHSitTrait.class).isSitting()) {
                    return false;
                }
            }
        }
        return owner.isOnline() && owner.getLocation().distance(npc.getEntity().getLocation()) < 20;
    }
}
