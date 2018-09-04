package fudge.spatialcrafting.common.command;


import net.minecraft.command.CommandBase;

public abstract class SCCommand extends CommandBase{
    abstract String description();
    abstract int minArgs();
    abstract int maxArgs();
}
