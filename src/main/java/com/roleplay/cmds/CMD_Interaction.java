package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.main.main;
import com.roleplay.board.PermissionBoard;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @Created 17.04.2022
 * @Author Nihar
 * @Description
 * This command is used to translate the attributes from the command into
 * a CommandSet which can be executed.
 */
public class CMD_Interaction extends CMDExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Interaction"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(PermissionBoard.of_getInstance().of_hasPermissions(ps, "Command.Permission.Interaction"))
                    {
                        if(args.length >= 2)
                        {
                            //  Check if the CommandSet comes from the TextBlock-Object.
                            if(args[0].equalsIgnoreCase("textblock"))
                            {
                                //  Check if the given TextBlock-File is the same which is stored as the textBlock attribute from the player.
                                if(ps.of_getTextBlockAttribute().equals(args[1]))
                                {
                                    // Check if commands are defined.
                                    // Build the commands until the ';' is found.
                                    StringBuilder command = new StringBuilder();

                                    for (int i = 2; i < args.length; i++)
                                    {
                                        if(i == args.length - 1)
                                        {
                                            command.append(args[i]);
                                        }
                                        else
                                        {
                                            command.append(args[i]).append(" ");
                                        }
                                    }

                                    // Execute all given commands.
                                    new CommandSet(command.toString().split(";"), ps).of_executeAllCommands();
                                    return true;
                                }
                            }
                        }
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

    @Override
    public void of_sendCMDHelperText(Player p){ /* Do not implement code in here! */ }
}
