package fudge.spatialcrafting.common.data;

import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;

public abstract class CraftersEnergy extends EnergyStorage {
    public CraftersEnergy(int capacity) {
        super(capacity);
    }

    public CraftersEnergy(int capacity, int maxTransfer) {
        super(capacity, maxTransfer);
    }

    public CraftersEnergy(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    public CraftersEnergy(int capacity, int maxReceive, int maxExtract, int energy) {
        super(capacity, maxReceive, maxExtract, energy);
    }

    public abstract void onEnergyChanged();

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        onEnergyChanged();
        return super.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        onEnergyChanged();
        return super.extractEnergy(maxExtract, simulate);
    }


}
