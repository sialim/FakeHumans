package me.sialim.fakehumans.behaviors;

import net.citizensnpcs.api.ai.tree.BehaviorGoalAdapter;
import net.citizensnpcs.api.ai.tree.BehaviorStatus;
import net.citizensnpcs.api.npc.NPC;

public class SitBehavior extends BehaviorGoalAdapter {
    private final NPC npc;
    public boolean isSitting;

    public SitBehavior(NPC npc) {
        this.npc = npc;
        this.isSitting = false;
    }

    public void toggleSit() {
        isSitting = !isSitting;
        if (isSitting) {
            npc.getNavigator().cancelNavigation();
            npc.setSneaking(true);
        } else {
            npc.setSneaking(false);
        }
    }

    @Override
    public void reset() {
        isSitting = false;
        npc.setSneaking(false);
    }

    @Override
    public BehaviorStatus run() {
        return isSitting ? BehaviorStatus.RUNNING : BehaviorStatus.SUCCESS;
    }

    @Override
    public boolean shouldExecute() {
        return isSitting;
    }
}
