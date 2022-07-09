package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.PermissionBoard;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Created 18.04.2022
 * @Author Nihar
 * @Description
 * This command is used to accept the data protection.
 */
public class CMD_DataProtection implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("DataProtection"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(PermissionBoard.of_getInstance().of_hasPermissions(ps, "Command.Permission.Dataprotection"))
                    {
                        //   If the player has already accepted the data protection.
                        if(main.SPIELERSERVICE.of_hasAlreadyAcceptedDataProtection(ps))
                        {
                            //  Send the player the defined textblock.
                            new CommandSet(new String[] {"TEXTBLOCK=txt_dataprotection_accepted"}, ps).of_executeAllCommands();
                            return true;
                        }

                        if(args.length == 0)
                        {
                            //   Send the player the message that he has to accept the data protection.
                            new CommandSet(new String[] {"TEXTBLOCK=txt_dataprotection"}, ps).of_executeAllCommands();
                            return true;
                        }

                        //  Accept the data protection.
                        if(args.length == 1)
                        {
                            if(args[0].equalsIgnoreCase("accept"))
                            {
                                //  Allow the player to move.
                                ps.of_setBlockedMoving(false);

                                // Add the necessary attributes to the InternList.
                                main.SPIELERSERVICE.of_addDataEntry4PlayerInternList(ps, "DataProtection", Sys.of_getTimeStamp(true));
                                main.SPIELERSERVICE.of_addDataEntry4PlayerInternList(ps, "IPLink", ps.of_getPlayerIPAsString());

                                //  Send the accept message...
                                new CommandSet(new String[] {"TEXTBLOCK=txt_dataprotection_accepted"}, ps).of_executeAllCommands();

                                // Check if the player has already played before.
                                main.SPIELERSERVICE.of_playerHasDoubleIPAddress(ps);
                            }
                        }

                        return true;
                    }
                    else
                    {
                        main.SPIELERSERVICE.of_sendNoPermissionsMessage(ps);
                    }
                }
            }
            else
            {
                sender.sendMessage("This command can only be used by players.");
            }

            return true;
        }

        return false;
    }

    /* ************************* */
    /* TAB COMPLETE */
    /* ************************* */

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        // List in with the completions will be stored.
        List<String> list = new ArrayList<>();
        Iterable<String> completions = null;

        //  All array arguments to lowercase
        for(int i = 0; i < args.length; i++)
        {
            args[i] = args[i].toLowerCase();
        }

        //  React to the different arguments.
        if(args.length == 1)
        {
            completions = Collections.singletonList("accept");
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
