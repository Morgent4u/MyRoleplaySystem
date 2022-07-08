package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import com.roleplay.position.Position;
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
 * @Created 19.06.2022
 * @Author Nihar
 * @Description
 * This command is used to create a position.
 * A position is similar to a location but in this system
 * you will be able to create safe-zones.
 */
public class CMD_Position implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Position"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    if(main.PERMISSIONBOARD.of_isAdmin(ps))
                    {
                        Player p = ps.of_getPlayer();

                        if(!Settings.of_getInstance().of_isUsingPosition())
                        {
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe §aPosition-system§7 is currently §cdisabled§f.");
                            return true;
                        }

                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                //  TODO: Need to be an GUI-System.
                                Position[] positions = main.POSITIONSERVICE._CONTEXT.of_getAllPositions();

                                //  Validate the positions
                                if(positions != null && positions.length > 0)
                                {
                                    p.sendMessage("§7═════════════════════════");
                                    p.sendMessage("");
                                    p.sendMessage("§8[§4§lPosition - List§8]");
                                    p.sendMessage("");
                                    p.sendMessage("§9Id §f-§9 Name");
                                    for (Position pos : positions)
                                    {
                                        p.sendMessage("§a" + (pos.of_getObjectId()) + " §f- §a" + pos.of_getPositionName());
                                    }
                                    p.sendMessage("");
                                    p.sendMessage("§7═════════════════════════");
                                }
                                //  If no positions has been created.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo positions has been created yet.");
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
                                Position pos = of_getPositionByAnything(ps, second);

                                if(pos != null)
                                {
                                    int rc = main.POSITIONSERVICE._CONTEXT.of_deletePosition(pos);

                                    //  Handle the return code.
                                    switch (rc)
                                    {
                                        case 1:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aThe position has been deleted.");
                                            break;
                                        case -2:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe position does not exist.");
                                            break;
                                        default:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error has occurred. Position could not be deleted!");
                                            break;
                                    }
                                }

                                return true;
                            }
                            else if(first.equalsIgnoreCase("tp"))
                            {
                                Position pos = of_getPositionByAnything(ps, second);

                                if(pos != null)
                                {
                                    p.teleport(pos.of_getLocation());
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aYou have been teleported to the position:§e '"+pos.of_getPositionName()+"'§a.");
                                }

                                return true;
                            }
                        }

                        if(args.length == 4)
                        {
                            String first = args[0];
                            String second = args[1];
                            String third = args[2];
                            String fourth = args[3];

                            if(first.equalsIgnoreCase("create"))
                            {
                                //  First = Create
                                //  Second = Name
                                //  Third = Range
                                //  Fourth = CommandSet

                                int range = Sys.of_getString2Int(third);

                                if(range != -1)
                                {
                                    //  Create the position-object which will be stored into the file.
                                    Position pos = new Position(new String[]{fourth}, second, p.getLocation(), range);

                                    int rc = main.POSITIONSERVICE._CONTEXT.of_savePosition2File(pos);

                                    //  Handle the return code.
                                    switch (rc)
                                    {
                                        case 1:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aThe position has been created.");
                                            break;
                                        case -2:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe position already exists.");
                                            break;
                                        default:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error has occurred. Position could not be created!");
                                            break;
                                    }
                                }
                                //  The range is not a number.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe range is not a number.");
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
    /* METHODS */
    /* ************************* */

    /**
     * This function is used to get the Position-object by any player input.
     * @param ps The player-object.
     * @param userInput The user input.
     * @return The Position-object.
     */
    private Position of_getPositionByAnything(Spieler ps, String userInput)
    {
        Position pos = main.POSITIONSERVICE.of_getPositionByAnything(userInput);

        if(pos == null)
        {
            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo position found with the name/id §e" + userInput + "§c.");
        }

        return pos;
    }

    /* ************************* */
    /* SEND CMD-HELPER */
    /* ************************* */

    private void of_sendCMDHelperText(Spieler ps)
    {
        Player p = ps.of_getPlayer();

        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lPosition - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("");
        p.sendMessage("§7To show the help-text:");
        p.sendMessage("§c/Position");
        p.sendMessage("§7To get a list of all created Positions.");
        p.sendMessage("§c/Position list");
        p.sendMessage("§7To teleport to a Position.");
        p.sendMessage("§c/Position tp <id>");
        p.sendMessage("§7To create a new Position.");
        p.sendMessage("§c/Position create <name> <block-area-size> <commandSet>");
        p.sendMessage("§7To delete a Position.");
        p.sendMessage("§c/Position delete <id>");
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
            completions = Arrays.asList("<name>", "<id>");
        }
        else if(args.length == 3)
        {
            completions = Collections.singletonList("<block-area-size>");
        }
        else if(args.length == 4)
        {
            completions = Collections.singletonList("<commandSet>");
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