package fudge.spatialcrafting.common.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.util.MCConstants;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class Commands extends CommandBase {
    private static List<SCCommand> commandList = ImmutableList.of(new CommandAddSRecipe(),
            new CommandAddShapeless(),
            new CommandDebug(),
            new CommandHelp(),
            new CommandLayer(),
            new CommandTest());

    @Nullable
    public static SCCommand getCommand(String alias) {
        for (SCCommand command : commandList) {
            if (command.getAliases().contains(alias)) return command;
        }

        return null;
    }

    public static List<SCCommand> getCommandList() {
        return commandList;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] words) {

        SCCommand command = getCommand(words[0]);

        if (command != null) {
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
        } else {
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
