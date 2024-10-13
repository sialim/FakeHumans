package me.sialim.fakehumans.traits;

import me.sialim.fakehumans.FakeHumans;
import me.sialim.fakehumans.NPCManager;
import me.sialim.fakehumans.ai.HomunculusAI;
import me.sialim.fakehumans.behaviors.SitBehavior;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import org.bukkit.Bukkit;
import org.bukkit.entity.NPC;
import org.bukkit.entity.Player;

@TraitName("FHHomunculusTrait") public class FHHomunculusTrait extends Trait {
    private SitBehavior sitBehavior;

    public FHHomunculusTrait() {
        super("FHHomunculusTrait");
    }

    @Override public void onSpawn() {
        if (npc != null) {
            sitBehavior = new SitBehavior(npc);
        }
    }

    public SitBehavior getSitBehavior() {
        return sitBehavior;
    }

    public Player getOwner() {
        return Bukkit.getPlayer(npc.getOrAddTrait(FHOwnerTrait.class).getOwner());
    }
}
