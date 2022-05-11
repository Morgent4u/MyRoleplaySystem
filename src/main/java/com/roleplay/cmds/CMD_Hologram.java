package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.extended.LocationDatei;
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
public class CMD_Hologram implements CommandExecutor, TabCompleter
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

                    if(main.PERMISSIONBOARD.of_hasPermissions(ps, "Command.Permission.Hologram"))
                    {
                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                //  Get all current loaded holograms and send a list into the players chat.
                                Hologram[] holos = main.HOLOGRAMSERVICE._CONTEXT.of_getAllHolograms();
                                int size = holos.length;

                                if(size > 0)
                                {
                                    //  Messages
                                    p.sendMessage("§7═════════════════════════");
                                    p.sendMessage("");
                                    p.sendMessage("§8[§4§lHologram - List§8]");
                                    p.sendMessage("");
                                    p.sendMessage("§9TeleportId - DisplayName");
                                    for(int i = 0; i < size; i++)
                                    {
                                        p.sendMessage("§f" + (i + 1) + " §8- §7" + holos[i].of_getHologramTitles().get(0).replace("&", "§"));
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
                                int id = Sys.of_getString2Int(second);

                                if(id != -1)
                                {
                                    //  Get the hologram with the id.
                                    Hologram hologram = main.HOLOGRAMSERVICE._CONTEXT.of_getHologramById(id);

                                    if(hologram != null)
                                    {
                                        //  Teleport the player to the hologram.
                                        p.teleport(hologram.of_getSpawnLocation());
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aTeleported to hologram with id: §f" + id);
                                    }
                                    // If the hologram with the id is not found.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram with the id §f" + id + " §cis not found.");
                                    }
                                }
                                // An invalid id has been entered.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cInvalid id.");
                                }

                                return true;
                            }
                            // Example: /HD delete <id>
                            else if(first.equalsIgnoreCase("delete"))
                            {
                                // First = delete
                                // Second = <id>
                                int id = Sys.of_getString2Int(second);

                                if(id != -1)
                                {
                                    //  Get the hologram with the id.
                                    Hologram hologram = main.HOLOGRAMSERVICE._CONTEXT.of_getHologramById(id);

                                    if(hologram != null)
                                    {
                                        //  Delete the hologram.
                                        int rc = main.HOLOGRAMSERVICE._CONTEXT.of_deleteHologram(hologram);

                                        if(rc == 1)
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aHologram with the id §f" + id + "§a has been successfully deleted!");
                                        }
                                        // An error has occurred.
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error has occurred.");
                                        }
                                    }
                                }
                                // An invalid id has been entered.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cInvalid id.");
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

                                //  Create a hologram.
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
                                holo = main.HOLOGRAMSERVICE.of_addHologramLine(holo, text);

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
                                        main.HOLOGRAMSERVICE._CONTEXT.of_loadHologramFromFile(new LocationDatei(new File(holo.of_getFilePath())));
                                    }
                                }
                                // If the hologram could not be created.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cHologram could not be created.");
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

    //  Send the default CMD-HelperText if some command arguments are wrong.
    private void of_sendCMDHelperText(Player p)
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
        p.sendMessage("§c/HD remove <id> §7- Remove a line from a hologram.");
        p.sendMessage("§c/HD list §7- Get a list of all created holograms.");
        p.sendMessage("§c/HD tp <id> §7- Teleport to the hologram.");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
    }

    /* ************************* */
    /* TAB COMPLETE */
    /* ************************* */

    // Attributes:
    private static final Iterable<String> firstCompleteAttributes = Arrays.asList("delete", "create", "add", "remove", "list", "tp");
    private static final Iterable<String> secondCompleteAttributes = Arrays.asList("<id>", "<fileName>");

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args)
    {
        List<String> list = new ArrayList<>();

        //  We react when the second-arguments are needed!
        if(args.length == 1)
        {
            // Check for the start letter of the arguments.
            StringUtil.copyPartialMatches(args[0], firstCompleteAttributes, list);
        }
        else if(args.length == 2)
        {
            StringUtil.copyPartialMatches(args[1], secondCompleteAttributes, list);
        }

        // Sort the elements in the list.
        Collections.sort(list);

        return list;
    }
}
