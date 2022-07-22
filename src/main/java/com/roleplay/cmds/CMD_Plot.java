package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.main.main;
import com.basis.utils.Settings;
import com.roleplay.board.PermissionBoard;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

                        //  Send the default help-text.
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
