package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import com.roleplay.board.PermissionBoard;
import com.roleplay.manager.LabelManager;
import com.roleplay.plot.Plot;
import com.roleplay.spieler.Spieler;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;

/**
 * @Created 21.07.2022
 * @Author Nihar
 * @Description
 * This command is used to declare defined world-guard regions as
 * a plot to the MRS. You will be able to create categories in
 * where you sort the created plots.
 */
public class CMD_Plot extends CMDExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Plot"))
        {
            if(sender instanceof Player)
            {
                Player p = (Player) sender;
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(p.getName());

                if(ps != null)
                {
                    if(PermissionBoard.of_getInstance().of_isAdmin(ps))
                    {
                        if(!Settings.of_getInstance().of_isUsingWorldGuard())
                        {
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe §aPlot-system§f is currently §cdisabled§f.");
                            return true;
                        }

                        if(args.length == 0)
                        {
                            of_sendCMDHelperText(p);
                            return true;
                        }

                        //  Check for the create-word!
                        if(args[0].equalsIgnoreCase("create"))
                        {
                            //  Build the plot-name.
                            StringBuilder nameBuilder = new StringBuilder();

                            for(int i = 1; i < args.length; i++)
                            {
                                nameBuilder.append(args[i]).append(" ");
                            }

                            //  Create a plot-object and set the info-attribute as plot-name.
                            Plot plot = new Plot();
                            plot.of_setInfo(nameBuilder.toString());
                            ps.of_setPowerObject(plot);

                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fSuccessfully created a §csetup-plot§f with the name §f'§a"+nameBuilder+"§f'.");
                            return true;
                        }

                        //  Check if the player has a plot.
                        Plot plot = of_getPlotObject(ps);

                        if(plot == null)
                        {
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cYou need to setup a plot first. Use: §7/plot create <plot-name [...]>");
                            return true;
                        }

                        //  Save the current object.
                        if(args[0].equalsIgnoreCase("save"))
                        {
                            String errorMessage = plot.of_validate();

                            //  All ok :)
                            if(errorMessage == null)
                            {
                                int rc = main.PLOTSERVICE._CONTEXT.of_createNewPlot(plot);

                                switch (rc)
                                {
                                    case 1:
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fSuccessfully created a the §cmrs-plot §f'§a"+plot.of_getInfo()+"§f'!");
                                        ps.of_setPowerObject(null);
                                        break;
                                    case -1:
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThere was an error while saving your plot-setup for '§e"+plot.of_getInfo()+"§c'!");
                                        break;
                                    case -2:
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThere was a validation-error for your plot-setup for '§e"+plot.of_getInfo()+"§c'!");
                                        break;
                                }
                            }
                            //  Validation error!
                            else
                            {
                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§c"+errorMessage);
                            }

                            return true;
                        }

                        //  Set the spawn-point.
                        if(args.length == 2)
                        {
                            String first = args[0];
                            String second = args[1];

                            if(first.equalsIgnoreCase("set") && second.equalsIgnoreCase("spawn"))
                            {
                                plot.of_setLocation(p.getLocation());
                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fSuccessfully added a §aspawn-point§f to this plot.");
                                return true;
                            }
                        }

                        //  Set other important attributes (WorldGuard-Region, Category etc.)
                        if(args.length == 3)
                        {
                            String first = args[0];
                            String second = args[1].toLowerCase();
                            String third = args[2];

                            if(first.equalsIgnoreCase("set"))
                            {
                                switch (second)
                                {
                                    case "category":
                                        int labelId = Sys.of_getString2Int(third);

                                        if(labelId != -1)
                                        {
                                            if(LabelManager.of_getInstance().of_checkLabel4GivenLabelEnumFlag(labelId, "plotsystem"))
                                            {
                                                String labelName = LabelManager.of_getInstance().of_getLabelById(labelId);
                                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fSuccessfully set the category to '§a"+labelName+"§f'!");
                                                plot.of_setLabelEnum(labelId);
                                            }
                                            //  The given label-id is not set for the plot-system category.
                                            else
                                            {
                                                String categoryName = LabelManager.of_getInstance().of_getLabelEnumTextByFlag("plotsystem");
                                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe given category/label is not set for the label-enum: '§e"+categoryName+"§c'.");
                                            }
                                        }
                                        //  Invalid category-Id.
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cYou need to enter a valid label/plot-category!");
                                        }
                                        break;
                                    case "region":
                                        try
                                        {
                                            RegionContainer regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                            World world = BukkitAdapter.adapt(Objects.requireNonNull(p.getLocation().getWorld()));
                                            RegionManager regions = regionContainer.get(world);

                                            if(regions != null && regions.getRegion(third) != null)
                                            {
                                                plot.of_setWorldGuardRegion(third);
                                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fSuccessfully set the WorldGuard-Region '§a"+third+"§f' to this plot.");
                                                return true;
                                            }
                                        }
                                        catch (Exception ignored) { }

                                        //  If an error occurred or the world-guard region does not exist.
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe defined WorldGuard-Region: '§e"+third+"§c' does not exist!");
                                        break;
                                    case "price":
                                        if(Sys.of_getString2Int(third) != -1)
                                        {
                                            plot.of_setPrice(Sys.of_getRoundedDouble(Double.parseDouble(third), 2));
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fSuccessfully set the WorldGuard-Region '§a"+third+"§f' to this plot.");
                                        }
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cYou need to enter a valid price!");
                                        }
                                        break;
                                }

                                return true;
                            }
                        }

                        //  Send CMD-Helper text.
                        of_sendCMDHelperText(p);
                    }
                    //  No permission
                    else
                    {
                        main.SPIELERSERVICE.of_sendNoPermissionsMessage(ps);
                    }
                }
            }
            //  If no player executes the command, the command is executed by the console.
            else
            {
                sender.sendMessage("This command can only be used by players.");
            }
        }

        return false;
    }

    /* ************************* */
    /* COMMAND METHODS */
    /* ************************* */

    private Plot of_getPlotObject(Spieler ps)
    {
        try
        {
            return (Plot) ps.of_getPowerObject();
        }
        catch (Exception ignored) { }

        return null;
    }

    /* ************************* */
    /* SEND CMD-HELPER */
    /* ************************* */

    @Override
    public void of_sendCMDHelperText(Player p)
    {
        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lPlot - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("");
        p.sendMessage("§7To show the help-text:");
        p.sendMessage("§c/Plot");
        p.sendMessage("§7To create a plot:");
        p.sendMessage("§c/Plot create <plot-name>");
        p.sendMessage("§7To add the current plot to a category:");
        p.sendMessage("§c/Plot set category <category-id>");
        p.sendMessage("§7To set a defined world-guard region to the plot:");
        p.sendMessage("§c/Plot set region <world-guard-region>");
        p.sendMessage("§7To set a sell price:");
        p.sendMessage("§c/Plot set price <price>");
        p.sendMessage("§7To set a plot-spawn use:");
        p.sendMessage("§c/Plot set spawn");
        p.sendMessage("§7To get a list of all plots:");
        p.sendMessage("§c/Plot list");
        p.sendMessage("§7To get a list of categories:");
        p.sendMessage("§c/Plot category list");
        p.sendMessage("§7To delete a defined plot:");
        p.sendMessage("§c/Plot delete <plot-id>");
        p.sendMessage("§7To edit a plot use:");
        p.sendMessage("§c/Plot edit <plot-id>");
        p.sendMessage("§7To teleport to the given plot:");
        p.sendMessage("§c/Plot tp <plot-id>");
        p.sendMessage("§7To save the current plot:");
        p.sendMessage("§c/Plot save");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
    }

    /* ************************* */
    /* TAB-COMPLETER */
    /* ************************* */

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        // List in with the completions will be stored.
        List<String> list = new ArrayList<>();
        Iterable<String> completions = null;

        //  All array arguments to lowercase
        for(int i = 0; i < args.length; i++)
        {
            args[i] = args[i].toLowerCase();
        }

        //  Handle the completions.
        if(args.length == 1)
        {
            completions = Arrays.asList("create", "set", "list", "delete", "edit", "tp", "save", "category");
        }
        else if(args.length == 2)
        {
            switch (args[0])
            {
                case "create":
                    completions = Collections.singletonList("<plot-name>");
                    break;
                case "set":
                    completions = Arrays.asList("category", "region", "price", "spawn");
                    break;
                case "category":
                    completions = Collections.singletonList("list");
                    break;
                case "delete":
                case "edit":
                case "tp":
                    completions = Collections.singletonList("<plot-id>");
                    break;
            }
        }
        else if(args.length == 3)
        {
            switch (args[1])
            {
                case "category":
                    completions = Collections.singletonList("<category-id>");
                    break;
                case "region":
                    completions = Collections.singletonList("<world-guard-region>");
                    break;
                case "price":
                    completions = Collections.singletonList("<price>");
                    break;
            }
        }

        // Suggest the player the completions.
        if(completions != null)
        {
            //  Copy the completions to the list.
            StringUtil.copyPartialMatches(args[args.length - 1], completions, list);
        }

        return list;
    }
}
