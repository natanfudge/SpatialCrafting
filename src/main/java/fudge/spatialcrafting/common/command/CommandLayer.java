package fudge.spatialcrafting.common.command;

import com.google.common.collect.ImmutableList;
import fudge.spatialcrafting.common.MCConstants;
import fudge.spatialcrafting.common.tile.TileCrafter;
import fudge.spatialcrafting.common.util.CrafterUtil;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.List;

public class CommandLayer extends SCCommand {

    private static final List<String> ALIASES = ImmutableList.of("layer");

    @Override
    public String description() {
        return "commands.spatialcrafting.layer.description";
    }

    @Override
    public int minArgs() {
        return 1;
    }

    @Override
    public int maxArgs() {
        return 1;
    }

    @Override
    public List<String> getAliases() {
        return ALIASES;
    }

    @Override
    public String getName() {
        return "Set Active Hologram Layer";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sc layer <layer>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        TileCrafter crafter = CrafterUtil.getClosestMasterBlock(sender.getEntityWorld(), sender.getPosition());
        if (crafter != null) {

            int layer;
            try {
                layer = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.layer.not_a_number", args[1]));
                return;
            }

            if (layer < 1) {
                sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.layer.non_positive"));
                return;
            } else if (layer > crafter.size()) {
                sender.sendMessage(new TextComponentTranslation("commands.spatialcrafting.layer.too_small"));
                return;
            }

            crafter.setActiveHolograms(layer - 1);


        } else {
            sender.sendMessage(new TextComponentTranslation("jei.wrapper.helpButton.error.no_crafters"));
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
