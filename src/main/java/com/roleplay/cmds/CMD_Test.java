package com.roleplay.cmds;

import com.basis.main.main;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @Created 16.04.2022
 * @Author Nihar
 * @Description
 * This command is used to test some functions of the plugin.
 */
public class CMD_Test implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Test"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(main.PERMISSIONBOARD.of_isAdmin(ps))
                    {
                        if(args.length == 0)
                        {
                            if(ps.of_getPowerObject() == null)
                            {
                                p.sendMessage("§aNPC created...");
                            }
                            else
                            {
                                p.sendMessage("§aNPC Destroyed.");
                            }

                            // Do not change this!
                            p.sendMessage("§cCommand has been executed!");
                            return true;
                        }

                        String first = args[0];

                        if(args.length == 1)
                        {
                            // Do not change this!
                            p.sendMessage("§cCommand has been executed! Args: 1");
                            return true;
                        }

                        String second = args[1];

                        if(args.length == 2)
                        {


                            // Do not change this!
                            p.sendMessage("§cCommand has been executed! Args: 2");
                            return true;
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
}
