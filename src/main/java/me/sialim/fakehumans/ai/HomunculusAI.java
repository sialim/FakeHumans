package me.sialim.fakehumans.ai;

import me.sialim.fakehumans.behaviors.CombatBehavior;
import me.sialim.fakehumans.behaviors.FollowOwnerBehavior;
import me.sialim.fakehumans.behaviors.WanderBehavior;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.tree.IfElse;
import net.citizensnpcs.api.ai.tree.Sequence;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.FallingExaminer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HomunculusAI implements Listener {
    private final NPC npc;
    private final Player owner;

    public HomunculusAI(NPC npc, Player owner) {
        this.npc = npc;
        this.owner = owner;
        setupBehaviorTree();
    }

    private void setupBehaviorTree() {
        NavigatorParameters navParams = npc.getNavigator().getDefaultParameters();
        //navParams.speedModifier(10.0f);
        navParams.range(10);
        navParams.examiner(new FallingExaminer(2));
        navParams.debug(false);
        navParams.useNewPathfinder(true);
        navParams.updatePathRate(20);
        navParams.attackRange(5);
        navParams.attackDelayTicks(10);
        npc.getDefaultGoalController().clear();


        npc.getDefaultGoalController().addGoal(Sequence.createSequence(
                new IfElse(() -> findClosestHostileMob() != null,
                        new CombatBehavior(npc, owner),
                        new IfElse(() -> owner.isOnline() && owner.getLocation().distance(npc.getEntity().getLocation()) < 20,
                                new FollowOwnerBehavior(npc, owner),
                                new WanderBehavior(npc, owner)
                        )
                )
        ), 1);
    }

    private LivingEntity findClosestHostileMob() {
        return npc.getEntity().getNearbyEntities(10, 10, 10).stream()
                .filter(entity -> entity instanceof Monster)
                .map(entity -> (LivingEntity) entity)
                .findFirst()
                .orElse(null);
    }

    @EventHandler public void onPlayerJoin(PlayerJoinEvent e) {
        if (e.getPlayer().equals(owner)) {
            setupBehaviorTree();
        }
    }
}
