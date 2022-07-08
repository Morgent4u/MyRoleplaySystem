package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.utils.Settings;
import com.roleplay.iblock.IBlock;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.*;

/**
 * @Created 22.05.2022
 * @Author Nihar
 * @Description
 * This command is used to interact with IBlocks.
 * It also allows creating or delete IBlocks.
 */
public class CMD_IBlock implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("IBlock"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    if(main.PERMISSIONBOARD.of_isAdmin(ps))
                    {
                        Player p = ps.of_getPlayer();

                        if(!Settings.of_getInstance().of_isUsingIBlock())
                        {
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe §aIBlock-system§f is currently §cdisabled§f.");
                            return true;
                        }

                        if(main.IBLOCKSERVICE.of_isInSetup(ps))
                        {
                            //  Remove the player from the setup-mode.
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cYou have left the setup-mode.");
                            main.IBLOCKSERVICE.of_removePlayerFromSetup(ps);
                            return true;
                        }

                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                IBlock[] iblocks = main.IBLOCKSERVICE._CONTEXT.of_getAllIBlocks();

                                if(iblocks != null && iblocks.length > 0)
                                {
                                    p.sendMessage("§7═════════════════════════");
                                    p.sendMessage("");
                                    p.sendMessage("§8[§4§lIBlock - List§8]");
                                    p.sendMessage("");
                                    p.sendMessage("§9Id §f-§9 Name");
                                    for (IBlock iblock : iblocks)
                                    {
                                        p.sendMessage("§a" + iblock.of_getObjectId() + " §f- §a" + iblock.of_getInfo());
                                    }
                                    p.sendMessage("");
                                    p.sendMessage("§7═════════════════════════");
                                }
                                //  If no iBlocks has been found.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo IBlocks found.");
                                }

                                return true;
                            }
                        }

                        if(args.length == 2)
                        {
                            String first = args[0];
                            String second = args[1];

                            if(first.equalsIgnoreCase("delete"))
                            {
                                IBlock iblock = main.IBLOCKSERVICE.of_getIBlockByName(second);

                                if(iblock != null)
                                {
                                    int rc = main.IBLOCKSERVICE._CONTEXT.of_deleteIBlock(iblock);

                                    if(rc == 1)
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aDeleted IBlock §f" + iblock.of_getInfo() + "§a.");
                                    }
                                    //  If an error occurred.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error occurred while deleting the IBlock.");
                                    }
                                }
                                //  If not found.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo IBlock found with the name §e" + second + "§c.");
                                }

                                return true;
                            }
                            else if(first.equalsIgnoreCase("tp"))
                            {
                                IBlock iblock = main.IBLOCKSERVICE.of_getIBlockByName(second);

                                if(iblock != null)
                                {
                                    p.teleport(iblock.of_getLocation());
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aTeleported to IBlock §f" + iblock.of_getInfo() + "§a.");
                                }
                                //  If not found.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo IBlock found with the name §e" + second + "§c.");
                                }

                                return true;
                            }
                        }

                        if(args.length == 3)
                        {
                            String first = args[0];
                            String second = args[1];
                            String third = args[2];

                            if(first.equalsIgnoreCase("create"))
                            {
                                IBlock iblock = main.IBLOCKSERVICE.of_getIBlockByName(second);

                                if(iblock == null)
                                {
                                    //  Create a new instance with the name...
                                    iblock = new IBlock(null, new String[] {third}, null);
                                    iblock.of_setInfo(second);

                                    //  Set the IBlock as powerObject to the player...
                                    ps.of_setPowerObject(iblock);

                                    main.IBLOCKSERVICE.of_addPlayer2Setup(ps);
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fYou have defined your §acommand-set§f and §aIBlock-Name.");
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§eClick on a block to set your command-set to it!");
                                }
                                //  If the IBlock already exist.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn IBlock with the name §e" + second + "§c already exists.");
                                }

                                return true;
                            }
                        }

                        //  Send the default help-text.
                        of_sendCMDHelperText(ps);
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

            return true;
        }

        return false;
    }

    /* ************************* */
    /* SEND CMD-HELPER */
    /* ************************* */

    private void of_sendCMDHelperText(Spieler ps)
    {
        Player p = ps.of_getPlayer();

        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lIBlock - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("");
        p.sendMessage("§7To show the help-text:");
        p.sendMessage("§c/IBlock");
        p.sendMessage("§7To get a list of all created IBlocks.");
        p.sendMessage("§c/IBlock list");
        p.sendMessage("§7To teleport to an IBlock.");
        p.sendMessage("§c/IBlock tp <name>");
        p.sendMessage("§7To create a new IBlock.");
        p.sendMessage("§c/IBlock create <name> <command-set>");
        p.sendMessage("§7To delete an IBlock.");
        p.sendMessage("§c/IBlock delete <name>");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
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
            completions = Arrays.asList("list", "tp", "create", "delete");
        }
        else if(args.length == 2)
        {
            completions = Collections.singletonList("<name>");
        }
        else if(args.length == 3)
        {
            completions = Arrays.asList("OPEN=inv_name_example", "CMD=command_example", "TEXTBLOCK=example_text.yml", "DEBUG");
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
