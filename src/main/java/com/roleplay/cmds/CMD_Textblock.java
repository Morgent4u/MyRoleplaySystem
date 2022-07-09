package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.PermissionBoard;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @Created 22.04.2022
 * @Author Nihar
 * @Description
 * This command is used to get a preview of the defined textblock-file.
 */
public class CMD_Textblock implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Textblock"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(PermissionBoard.of_getInstance().of_hasPermissions(ps, "Command.Permission.Textblock"))
                    {
                        if(args.length == 0)
                        {
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§c/Textblock list§a - Get a list of the textblock-files.");
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§c/Textblock <filename>§a - Get a preview of the defined textblock-file.");
                            return true;
                        }

                        String first = args[0];

                        //  Get the text or the list.
                        if(args.length == 1)
                        {
                            if(first.equalsIgnoreCase("list"))
                            {
                                File directory = new File(Sys.of_getMainFilePath() + "//TextBlocks//");
                                File[] files = directory.listFiles();

                                if(files == null || files.length == 0)
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThere are no textblock-files.");
                                    return true;
                                }

                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe following textblock-files are available:");

                                for(File file : files)
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§f-§a " + file.getName().replace(".yml", ""));
                                }

                                return true;
                            }

                            //  Get the preview. Because the player wants to see the textblock.
                            try
                            {
                                new CommandSet(new String[] {"TEXTBLOCK=" + first}, ps).of_executeAllCommands();
                            }
                            //  Use this try-catch to handle any error of the CommandSet/TextBlock.
                            catch(Exception ignored)
                            {
                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe textblock-file could not be found or the content is invalid.");
                            }

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
