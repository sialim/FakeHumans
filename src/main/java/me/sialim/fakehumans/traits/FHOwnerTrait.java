package me.sialim.fakehumans.traits;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

import java.util.UUID;

@TraitName("FHOwnerTrait") public class FHOwnerTrait extends Trait {
    private UUID ownerUUID;

    public FHOwnerTrait() {
        super("FHOwnerTrait");
    }

    public void setOwner(UUID ownerUUID) {
        this.ownerUUID = ownerUUID;
    }

    public UUID getOwner() {
        return ownerUUID;
    }
}
