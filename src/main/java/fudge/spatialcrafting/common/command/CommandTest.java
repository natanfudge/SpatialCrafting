package fudge.spatialcrafting.common.command;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.common.util.MCConstants;
import fudge.spatialcrafting.debug.test.Test;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import java.util.List;

public class CommandTest extends SCCommand {

    private static final List<String> ALIASES = ImmutableList.of("test");

    @Override
    public String description() {
        return "";
    }

    @Override
    public boolean showInHelp() {
        return false;
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sc test";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!Test.INSTANCE.testInit()) sender.sendMessage(new TextComponentString("Test failed because of error!"));

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
