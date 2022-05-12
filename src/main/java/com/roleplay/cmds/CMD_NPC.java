package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.npc.NPC;
import com.roleplay.objects.CommandSet;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.io.File;

/**
 * @Created 29.04.2022
 * @Author Nihar
 * @Description
 * This command is used to create a NPC.
 */
public class CMD_NPC implements CommandExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("NPC"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(main.PERMISSIONBOARD.of_hasPermissions(ps, "Command.Permission.NPC"))
                    {
                        //  Check if the NPCService is valid!
                        if(main.NPCSERVICE == null)
                        {
                            main.SPIELERSERVICE.of_sendErrorMessage(ps, "§cThe NPC service is not enabled because ProtocolLib is not used.");
                            return true;
                        }

                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                //  Get all current loaded NPCs and send a list into the players chat.
                                NPC[] npcs = main.NPCSERVICE._CONTEXT.of_getLoadedNPCs();
                                int size = npcs.length;

                                if(size > 0)
                                {
                                    //  Messages
                                    p.sendMessage("§7═════════════════════════");
                                    p.sendMessage("");
                                    p.sendMessage("§8[§4§lNPC - List§8]");
                                    p.sendMessage("");
                                    p.sendMessage("§9TeleportId - DisplayName");
                                    for(int i = 0; i < size; i++)
                                    {
                                        p.sendMessage("§f" + (i + 1) + " §8- §7" + npcs[i].of_getInfo());
                                    }
                                    p.sendMessage("");
                                    p.sendMessage("§7═════════════════════════");
                                }
                                //  If no NPCs has been loaded.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo NPCs are loaded.");
                                }

                                return true;
                            }
                        }

                        if(args.length >= 2)
                        {
                            String first = args[0];
                            String second = args[1];

                            //  React to the create command.
                            if(first.equalsIgnoreCase("create"))
                            {
                                String third = null;

                                //  If the SkinName is given.
                                if(args.length == 3)
                                {
                                    third = args[2];
                                }

                                //  Set the INFO-Attribute it's used as the fileName!
                                NPC npc = new NPC(p.getLocation(), third);
                                npc.of_setInfo(second);

                                //  Add a default CommandSet.
                                npc.of_setCommandSet(new String[] {"OPEN=inv_menu"});

                                //  Save the new NPC.
                                int rc = main.NPCSERVICE._CONTEXT.of_saveNPC2File(npc, ps);

                                //  Check the result of the save-function and send a specified message.
                                switch (rc)
                                {
                                    case 1:
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aThe NPC was created successfully.");
                                        break;
                                    case -2:
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe NPC with the name §d" + second + "§f already exists.");
                                        break;
                                    default:
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error occurred while creating the NPC.");
                                        break;
                                }

                                return true;
                            }
                            //  React to the teleport command.
                            else if(first.equalsIgnoreCase("tp"))
                            {
                                int objectId = Sys.of_getString2Int(second);

                                if(objectId != -1)
                                {
                                    // Get all loaded NPCs.
                                    NPC[] npcs = main.NPCSERVICE._CONTEXT.of_getLoadedNPCs();
                                    int size = npcs.length;

                                    if(size > 0)
                                    {
                                        //  The objectId is the index value which is entered by the user.
                                        if(objectId > 0 && objectId <= size)
                                        {
                                            NPC npc = npcs[objectId - 1];
                                            p.teleport(npc.of_getLocation());
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aYou have been teleported to the NPC §d" + npc.of_getInfo() + "§a.");
                                        }
                                        // If the given ID is out of range.
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe given ID §d" + second + "§f is out of range.");
                                        }
                                    }
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo NPCs are loaded.");
                                    }
                                }
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe given ID is not a valid number.");
                                }

                                return true;
                            }
                        }

                        //  If no arguments are given send the default CMD-Help-Text.
                        of_sendCMDHelperText(ps.of_getPlayer());
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
        p.sendMessage("§8[§4§lNPC - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("§c/NPC §7- Shows the help text.");
        p.sendMessage("§c/NPC list §7- Get a list of all created NPCs.");
        p.sendMessage("§c/NPC create <DisplayName> <SkinName> §7- Create a new NPC.");
        p.sendMessage("§c/NPC tp <Id> §7- Teleport to the NPC.");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
    }
}
