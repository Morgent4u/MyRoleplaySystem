package com.roleplay.cmds;

import com.basis.main.main;
import com.roleplay.objects.TextBlock;
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
                Player p = (Player) sender;

                if(p.hasPermission("mrs.cmd.test"))
                {
                    if(args.length == 0)
                    {
                        Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getSpieler(p.getName());

                        if(ps != null)
                        {
                            TextBlock textBlock = new TextBlock("txt_test", ps);
                            textBlock.of_sendMessage2Player();
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
