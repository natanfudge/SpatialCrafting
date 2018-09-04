package fudge.spatialcrafting.common.command;

import com.google.common.collect.Lists;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.MCConstants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Commands extends CommandBase {
    private static List<SCCommand> commands = new ArrayList<>();

    public Commands() {
        commands.add(new CommandAddSRecipe());
        commands.add(new CommandHelp());
        commands.add(new CommandDebug());
        commands.add(new CommandLayer());
    }

    public static List<SCCommand> getCommands() {
        return commands;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] words) {

        // Look for the command in the map to see if we can execute it.
        boolean exists = false;
        for (SCCommand command : getCommands()) {
            List<String> aliases = command.getAliases();

            if (aliases.contains(words[0].toLowerCase())) {
                exists = true;
                if (command.checkPermission(server, sender)) {
                    try {
                        if (argAmountValid(words.length - 1, command, sender)) {
                            command.execute(server, sender, words);
                        } else {
                            sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.help.usage", command.getUsage(sender)));
                        }
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


    private boolean argAmountValid(int argAmount, SCCommand command, ICommandSender sender) {
        int minArgs = command.minArgs();
        int maxArgs = command.maxArgs();

        if (minArgs == maxArgs) {
            int requiredAmount = minArgs;
            if (argAmount != requiredAmount) {
                if (requiredAmount == 0) {
                    sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.no_args", requiredAmount));
                } else {
                    sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.arg_amount_wrong", requiredAmount));
                }
                return false;
            }
        } else {
            if (argAmount < minArgs) {
                sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.too_little_args", minArgs));
                return false;
            }

            if (argAmount > maxArgs) {
                sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.too_many_args", maxArgs));
                return false;
            }


        }


        return true;
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
        return MCConstants.LOWEST;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
