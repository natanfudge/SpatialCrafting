package fudge.spatialcrafting.common.command;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.SpatialCrafting;
import fudge.spatialcrafting.common.MCConstants;
import fudge.spatialcrafting.debug.Debug;
import fudge.spatialcrafting.network.PacketHandler;
import fudge.spatialcrafting.network.client.PacketDebugPrint;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class CommandDebug extends SCCommand {

    private static final List<String> ALIASES = ImmutableList.of("debug");

    @Override
    public String description() {
        return "commands.spatialcrafting.debug.description";
    }


    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public String getName() {
        return "Debug";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sc debug";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        World serverWorld = sender.getEntityWorld();

        String message = Debug.getAllCrafterData(serverWorld, false);
        SpatialCrafting.LOGGER.info(message);
        try {
            PacketHandler.getNetwork().sendTo(new PacketDebugPrint(), getCommandSenderAsPlayer(sender));
        } catch (PlayerNotFoundException ignored) {
        }


        sender.sendMessage(new TextComponentString(message));

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
