package me.sialim.fakehumans.ai;

import me.sialim.fakehumans.FakeHumans;
import me.sialim.fakehumans.behaviors.CombatBehavior;
import me.sialim.fakehumans.behaviors.FollowOwnerBehavior;
import me.sialim.fakehumans.behaviors.WanderBehavior;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.ai.NavigatorParameters;
import net.citizensnpcs.api.ai.tree.IfElse;
import net.citizensnpcs.api.ai.tree.Sequence;
import net.citizensnpcs.api.astar.pathfinder.DoorExaminer;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.ai.FallingExaminer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginManager;

public class HomunculusAI implements Listener {
    private final NPC npc;
    private final Player owner;
    private final FakeHumans plugin;

    public HomunculusAI(NPC npc, Player owner, FakeHumans plugin) {
        this.npc = npc;
        this.owner = owner;
        this.plugin = plugin;
        setupBehaviorTree();
        registerListener();
    }

    private void setupBehaviorTree() {
        NavigatorParameters navParams = npc.getNavigator().getDefaultParameters();
        //navParams.speedModifier(10.0f);
        navParams.range(10);
        navParams.examiner(new FallingExaminer(2));
        navParams.examiner(new DoorExaminer());
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

    private void registerListener() {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.registerEvents(this, plugin);
    }

    public void unregisterListener() {
        PlayerJoinEvent.getHandlerList().unregister(this);
    }
}
