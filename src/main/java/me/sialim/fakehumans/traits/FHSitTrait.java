package me.sialim.fakehumans.traits;

import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitName;

@TraitName("FHSitTrait") public class FHSitTrait extends Trait {
    private boolean isSitting;

    public FHSitTrait() {
        super("FHSitTrait");
        this.isSitting = false;
    }

    public void toggleSit() {
        isSitting = !isSitting;
        if (isSitting) {
            getNPC().setSneaking(true);
        } else {
            getNPC().setSneaking(false);
        }
    }

    public boolean isSitting() {
        return isSitting;
    }
}
