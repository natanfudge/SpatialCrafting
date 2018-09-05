package fudge.spatialcrafting.common.command;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class CommandHelp extends SCCommand {


    private static void newLine(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(""));
    }

    @Override
    public List<String> getAliases() {
        return ImmutableList.of("help", "h");
    }

    @Override
    public String description() {
        return "commands.spatialcrafting.help.description";
    }

    @Override
    public String getName() {
        return "Help";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sc help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {

        TextComponentTranslation optional = new TextComponentTranslation("commands.spatialcrafting.help.optional");
        optional.getStyle().setColor(TextFormatting.GOLD);
        sender.sendMessage(optional);
        newLine(sender);


        Commands.getCommandList().forEach(command -> {
            TextComponentString name = new TextComponentString(command.getName());
            name.getStyle().setBold(true);
            name.getStyle().setUnderlined(true);
            sender.sendMessage(name);
            newLine(sender);

            sender.sendMessage(new TextComponentTranslation(command.description()));
            sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.help.usage",
                    new TextComponentTranslation(command.getUsage(sender))));
            newLine(sender);
        });
    }
}
