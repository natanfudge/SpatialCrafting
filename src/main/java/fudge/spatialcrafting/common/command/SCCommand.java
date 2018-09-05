package fudge.spatialcrafting.common.command;


import net.minecraft.command.CommandBase;

public abstract class SCCommand extends CommandBase{
    abstract String description();
    public int minArgs(){return 0;}
    public int maxArgs(){return 0;}
}
