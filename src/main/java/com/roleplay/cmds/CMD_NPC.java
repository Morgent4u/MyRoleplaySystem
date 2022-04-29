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
                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                //  TODO: List all NPCs.
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

                                NPC npc = new NPC(p.getLocation(), second, third);

                                //  Add a default CommandSet.
                                npc.of_setCommandSet(new String[] {"OPEN=inv_menu"});

                                int rc = main.NPCSERVICE._CONTEXT.of_saveNPC2File(npc);

                                //  If the NPC was saved successfully.
                                //  TODO: Switch? :)
                                if(rc == 1)
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aThe NPC was created successfully.");
                                }
                                //  If the NPC already exist.
                                else if(rc == -2)
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe NPC with the name §d" + second + "§f already exists.");
                                }
                                //  A general error occurred.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error occurred while creating the NPC.");
                                }

                                return true;
                            }
                            //  React to the teleport command.
                            else if(first.equalsIgnoreCase("tp"))
                            {
                                int objectId = Sys.of_getString2Int(second);

                                if(objectId != -1)
                                {
                                    //  TODO: Teleport to the NPC.
                                }
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "The given ID is not a valid number.");
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

    private void of_sendCMDHelperText(Player p)
    {
        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lNPC - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("§c/NPC §7- This command.");
        p.sendMessage("§c/NPC list §7- Get a list of all created NPCs.");
        p.sendMessage("§c/NPC create <DisplayName> <SkinName> §7- Create a new NPC.");
        p.sendMessage("§c/NPC tp <Id> §7- Teleport to the NPC.");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
    }

}
