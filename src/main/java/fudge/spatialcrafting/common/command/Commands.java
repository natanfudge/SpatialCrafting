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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Commands extends CommandBase {
    private Map<List<String>, CommandBase> commands = new HashMap<>();

    public Commands() {
        CommandAddSRecipe commandAddSRecipe = new CommandAddSRecipe();
        commands.put(commandAddSRecipe.getAliases(), new CommandAddSRecipe());
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] words) {

        // Look for the command in the map to see if we can execute it.
        boolean exists = false;
        List<String> aliases;
        CommandBase command;
        for (Map.Entry<List<String>, CommandBase> entry : commands.entrySet()) {
            aliases = entry.getKey();
            command = entry.getValue();

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
