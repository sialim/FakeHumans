package me.sialim.fakehumans.traits;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Owner;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@TraitName("FHHomunculusTrait") public class FHHomunculusTrait extends Trait {

    public FHHomunculusTrait() {
        super("FHHomunculusTrait");
    }

    public Player getOwner() {
        return Bukkit.getPlayer(npc.getOrAddTrait(Owner.class).getOwnerId());
    }
}
