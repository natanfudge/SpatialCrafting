package fudge.spatialcrafting.common.command;

import com.google.common.collect.Lists;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.SCConstants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Commands extends CommandBase {
    //private static Map<List<String>, CommandBase> commands = new HashMap<>();
    private static List<SCCommand> commands = new ArrayList<>();

    public Commands() {
        //commands.put(commandAddSRecipe.getAliases(), commandAddSRecipe);
        commands.add(new CommandAddSRecipe());
        commands.add(new CommandHelp());
    }

    public static List<SCCommand> getCommands() {
        return commands;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] words) {

        // Look for the command in the map to see if we can execute it.
        boolean exists = false;
        for (CommandBase command : getCommands()) {
            List<String> aliases = command.getAliases();

            if (aliases.contains(words[0].toLowerCase())) {
                exists = true;
                if (command.checkPermission(server, sender)) {
                    try {
                        command.execute(server, sender, words);
                    } catch (CommandException e) {
                        SpatialCrafting.LOGGER.error(e);
                    }
                } else {
                    sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.no_permission", 0));
                }
            }
        }
        if (!exists) {
            sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.no_such_command", 0));
        }

    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return Lists.newArrayList("sc");
    }

    @Override
    @Nonnull
    public String getName() {
        return "SCCommands";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "sc <command>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return SCConstants.LOWEST;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
