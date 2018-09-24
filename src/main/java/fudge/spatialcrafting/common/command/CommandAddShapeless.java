package fudge.spatialcrafting.common.command;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.util.MCConstants;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;
import java.util.List;

public class CommandAddShapeless extends SCCommand {

    private static final List<String> ALIASES = ImmutableList.of("addshapeless",
            "as",
            "asl",
            "addshapelessrecipe",
            "addrecipeshapeless",
            "asr",
            "aslr");

    @Override
    public String description() {
        return "commands.spatialcrafting.add_shapeless.description";
    }

    @Override
    public boolean showInHelp() {
        return true;
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public String getName() {
        return "Add Shapeless Recipe";
    }

    @Override
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/sc addshapeless [exact/wildcard/oredict] [craftTime]";
    }


    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        SCCommand command = Commands.getCommand("addrecipe");
        if (command instanceof CommandAddSRecipe) {
            ((CommandAddSRecipe) command).AddSpatialRecipe(server, sender, args, false);
        } else {
            SpatialCrafting.LOGGER.error("Couldn't find add recipe command!", new NullPointerException());
        }

    }

    @Override
    public int getRequiredPermissionLevel() {
        return MCConstants.HIGHEST;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
    }
}
