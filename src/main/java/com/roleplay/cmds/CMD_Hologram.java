package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.PermissionBoard;
import com.roleplay.hologram.Hologram;
import com.roleplay.spieler.Spieler;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Created 11.05.2022
 * @Author Nihar
 * @Description
 * This command is used to create, edit or delete a hologram.
 * A hologram can only be created by an admin.
 */
public class CMD_Hologram extends CMDExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Hologram"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(PermissionBoard.of_getInstance().of_hasPermissions(ps, "Command.Permission.Hologram"))
                    {
                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                //  Get all current loaded holograms and send a list into the players chat.
                                Objekt[] objects = main.HOLOGRAMSERVICE._CONTEXT.of_getAllObjects();

                                if(objects != null && objects.length > 0)
                                {
                                    //  Messages
                                    p.sendMessage("§7═════════════════════════");
                                    p.sendMessage("");
                                    p.sendMessage("§8[§4§lHologram - List§8]");
                                    p.sendMessage("");
                                    p.sendMessage("§9TeleportId - DisplayName");
                                    for(Objekt objekt : objects)
                                    {
                                        if(objekt instanceof Hologram)
                                        {
                                            Hologram holo = (Hologram) objekt;
                                            p.sendMessage("§f" + holo.of_getObjectId() + " §8- §7" + holo.of_getHologramTitles().get(0).replace("&", "§"));
                                        }
                                    }
                                    p.sendMessage("");
                                    p.sendMessage("§7═════════════════════════");
                                }
                                //  If no hologram has been loaded.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo holograms are loaded.");
                                }

                                return true;
                            }
                        }

                        if(args.length == 2)
                        {
                            String first = args[0];
                            String second = args[1];

                            // Example: /hologram tp <id>
                            if(first.equalsIgnoreCase("tp"))
                            {
                                // First = tp
                                // Second = <id>
                                Hologram holo = of_getHologramByPlayerInput(ps, second);

                                if(holo != null)
                                {
                                    //  Teleport the player to the hologram.
                                    p.teleport(holo.of_getSpawnLocation());
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aTeleported to hologram with id: §f" + second);
                                }

                                return true;
                            }
                            // Example: /HD delete <id>
                            else if(first.equalsIgnoreCase("delete"))
                            {
                                // First = delete
                                // Second = <id>
                                Hologram holo = of_getHologramByPlayerInput(ps, second);

                                if(holo != null)
                                {
                                    //  Delete the hologram.
                                    int rc = main.HOLOGRAMSERVICE._CONTEXT.of_deleteObjectFromFile(holo);

                                    if(rc == 1)
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aHologram with the id §f" + second + "§a has been successfully deleted!");
                                    }
                                    // An error has occurred.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error has occurred.");
                                    }
                                }

                                return true;
                            }
                        }

                        if(args.length >= 3)
                        {
                            String first = args[0];
                            String second = args[1];
                            StringBuilder third = new StringBuilder(args[2]);

                            // Example: /hologram create <fileName> <Text>
                            if(first.equalsIgnoreCase("create"))
                            {
                                // First = create
                                // Second = fileName
                                // Third = Text

                                //  Create the text (first line) for the hologram.
                                for(int i = 3; i < args.length; i++)
                                {
                                    third.append(" ").append(args[i]);
                                }

                                //  Get the full-text and manipulate the location to get the display, above the player.
                                String text = third.toString().replace("&", "§");
                                Location loc = p.getLocation();
                                loc.setY(loc.getY() + 1.0);

                                //  Create the hologram.
                                Hologram holo = new Hologram(loc, 0.26);
                                holo = main.HOLOGRAMSERVICE.of_addLine2Hologram(holo, text);

                                if(holo != null)
                                {
                                    //  Save the hologram to the hologram file.
                                    int rc = main.HOLOGRAMSERVICE._CONTEXT.of_saveHologram2File(second, holo);

                                    //  Check for the result of the save.
                                    switch (rc)
                                    {
                                        case 1:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aHologram created.");
                                            break;
                                        case -1:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram could not be created.");
                                            break;
                                        case -2:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram already exists.");
                                            break;
                                    }

                                    // Unload the hologram.
                                    holo.of_unload();

                                    if(rc == 1)
                                    {
                                        //  Load the hologram by using the load system on the context.
                                        main.HOLOGRAMSERVICE._CONTEXT.of_loadObjectByFile(new File(holo.of_getFilePath()));
                                    }
                                }
                                // If the hologram could not be created.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram could not be created.");
                                }

                                return true;
                            }
                            else if(first.equalsIgnoreCase("add"))
                            {
                                // First = Add
                                // Id = HologramId
                                // Third = Text

                                Hologram holo = of_getHologramByPlayerInput(ps, second);

                                if(holo != null)
                                {
                                    //  Create the line which will be added to the hologram.
                                    for(int i = 3; i < args.length; i++)
                                    {
                                        third.append(" ").append(args[i]);
                                    }

                                    // The line which will be added.
                                    String text = third.toString().replace("&", "§");
                                    holo = main.HOLOGRAMSERVICE.of_addLine2Hologram(holo, text);

                                    //  Update the hologram to the file.
                                    int rc = main.HOLOGRAMSERVICE._CONTEXT.of_updateHologram2File(holo);

                                    switch (rc)
                                    {
                                        case 1:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aHologram §f"+second+" §aupdated (line added).");
                                            break;
                                        case -1:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram could not be updated.");
                                            holo.of_sendDebugInformation("CMD_Hologram: by "+ps.of_getPlayer().getName() + " > ADD > "+text);
                                            break;
                                        case -2:
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram does not exist.");
                                            break;
                                    }
                                }

                                return true;
                            }
                            else if(first.equalsIgnoreCase("remove"))
                            {
                                // First = Remove
                                // Id = HologramId
                                // Third = LineId

                                // Remove the hologram.
                                Hologram holo = of_getHologramByPlayerInput(ps, second);

                                if(holo != null)
                                {
                                    // The line which will be removed.
                                    int line = Sys.of_getString2Int(third.toString());

                                    // Remove the line from the hologram.
                                    if(line != -1)
                                    {
                                        holo = main.HOLOGRAMSERVICE.of_removeLineFromHologram(holo, line);

                                        if(holo != null)
                                        {
                                            int rc = main.HOLOGRAMSERVICE._CONTEXT.of_updateHologram2File(holo);

                                            //  If the hologram has been deleted in the of_removeLineFromHologram function the file does not exist.
                                            //  So we get the returnCode -2, and we can live with it (it is not necessary to specify a message).
                                            if(rc == 1 || rc == -2)
                                            {
                                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aHologram §f"+second+" §aupdated (line removed).");
                                            }
                                            //  If the file could not be updated (no permissions), we send a message.
                                            else
                                            {
                                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram could not be updated.");
                                            }
                                        }
                                        // If the line is not found.
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cLine does not exist.");
                                        }
                                    }
                                    // If the line does not exist.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cLine does not exist.");
                                    }
                                }

                                return true;
                            }
                        }

                        if(args.length >= 4)
                        {
                            String first = args[0];
                            String second = args[1];
                            StringBuilder third = new StringBuilder(args[2]);
                            StringBuilder fourth = new StringBuilder(args[3]);

                            if(first.equalsIgnoreCase("edit"))
                            {
                                // First = Edit
                                // Id = HologramId
                                // Second = LineId
                                // Third = Text

                                Hologram holo = of_getHologramByPlayerInput(ps, second);

                                if(holo != null)
                                {
                                    // The line which will be edited.
                                    int line = Sys.of_getString2Int(third.toString());

                                    // Edit the line in the hologram.
                                    if(line != -1)
                                    {
                                        // Build the text which will be replaced.
                                        for (int i = 4; i < args.length; i++)
                                        {
                                            fourth.append(" ").append(args[i]);
                                        }

                                        String text = fourth.toString();

                                        //  Edit the line.
                                        int rc = main.HOLOGRAMSERVICE.of_editHologramLine(holo, line, text);

                                        if(rc == 1)
                                        {
                                            rc = main.HOLOGRAMSERVICE._CONTEXT.of_updateHologram2File(holo);

                                            switch (rc)
                                            {
                                                case 1:
                                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aHologram §f"+second+" §aupdated (line edited).");
                                                    break;
                                                case -1:
                                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram could not be updated.");
                                                    break;
                                                case -2:
                                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cA general error occurred. Print a report to the console.");
                                                    holo.of_sendDebugInformation("This has been called by the hd-editing-process by the player " + ps.of_getPlayer().getName() + ".");
                                                    break;
                                            }

                                        }
                                        // When the line could not be edited.
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cLine could not be edited. Does the line exist?");
                                        }
                                    }
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cLine does not exist.");
                                    }
                                }

                                return true;
                            }
                        }

                        //  If no arguments are given send the default CMD-Help-Text.
                        of_sendCMDHelperText(ps.of_getPlayer());
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
    /* SEND CMD-HELPER */
    /* ************************* */

    @Override
    public void of_sendCMDHelperText(Player p)
    {
        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lHologram - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("§c/HD §7- Shows the help text.");
        p.sendMessage("§c/HD create <fileName> [...] §7- Create a new hologram.");
        p.sendMessage("§c/HD delete <id> §7- Delete a hologram.");
        p.sendMessage("§c/HD add <id> [...] §7- Add a line to a hologram.");
        p.sendMessage("§c/HD remove <id> <line> §7- Remove a line from a hologram.");
        p.sendMessage("§c/HD edit <id> <line> <text> §7- Edit a line in a hologram.");
        p.sendMessage("§c/HD list §7- Get a list of all created holograms.");
        p.sendMessage("§c/HD tp <id> §7- Teleport to the hologram.");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
    }

    /* ************************* */
    /* OBJECT-METHODS */
    /* ************************* */

    private Hologram of_getHologramByPlayerInput(Spieler ps, String holoStringId)
    {
        int id = Sys.of_getString2Int(holoStringId);

        if(id != -1)
        {
            Objekt object = main.HOLOGRAMSERVICE._CONTEXT.of_getObjectById(id);

            if(object instanceof Hologram)
            {
                return (Hologram) object;
            }

            //  If the hologram-object could not be found.
            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram with the id §f" + id + " §cis not found.");
        }
        // An invalid id has been entered.
        else
        {
            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cInvalid id.");
        }

        return null;
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

        if(args.length == 1)
        {
            completions = Arrays.asList("delete", "create", "add", "remove", "list", "tp");
        }
        else if(args.length == 2)
        {
            completions = Arrays.asList("<id>", "<fileName>");
        }
        else if(args.length == 3)
        {
            completions = Collections.singletonList("&aYour text here");
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
