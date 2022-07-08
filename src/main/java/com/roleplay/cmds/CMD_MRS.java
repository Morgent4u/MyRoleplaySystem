package com.roleplay.cmds;

import com.basis.main.main;
import com.basis.sys.Sys;
import com.basis.utils.Settings;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Created 18.04.2022
 * @Author Nihar
 * @Description
 * This command is used to accept the data protection.
 */
public class CMD_MRS implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("MRS"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(main.PERMISSIONBOARD.of_hasPermissions(ps, "Command.Permission.MRS"))
                    {
                        if(args.length == 1)
                        {
                            //  First
                            String first = args[0];

                            if(first.equalsIgnoreCase("reload"))
                            {
                                //  Plugin will be reloaded.
                                sender.sendMessage("§8[§cReload-Service§8]§6 Warning: §c!§fThe plugin will be reloaded, but some-functions could be lost§c!");

                                //  We disable and enable the plugin...
                                Settings.of_getInstance().of_setUseProtocolLib(false);
                                main.PLUGIN.onDisable();
                                Settings.of_getInstance().of_initSystemServices();
                                Settings.of_getInstance().of_setUseProtocolLib(true);

                                sender.sendMessage("§8[§cReload-Service§8]§a Plugin has been reloaded.");
                                return true;
                            }
                            else if(first.equalsIgnoreCase("modules"))
                            {
                                p.sendMessage("§7═════════════════════════");
                                p.sendMessage("");
                                p.sendMessage("§8[§4§l"+Sys.of_getProgramVersion()+" - Modules§8]");
                                p.sendMessage("");
                                p.sendMessage("§5Current modules:");
                                p.sendMessage("§b[*]§f DeathCommandSet: "+( Settings.of_getInstance().of_isUsingModuleDeathCommandSet() ? "§aEnabled" : "§cDisabled" ));
                                p.sendMessage("");
                                p.sendMessage("§7═════════════════════════");
                                return true;
                            }
                        }

                        //  If no arguments has been found or something is incorrect, we send the help message.
                        of_sendCMDHelperText(ps);
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

    private void of_sendCMDHelperText(Spieler ps)
    {
        Player p = ps.of_getPlayer();

        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lMRS - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fVersion:");
        p.sendMessage("§e" + Sys.of_getProgramVersion());
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("");
        p.sendMessage("§7To show the help-text:");
        p.sendMessage("§c/MRS");
        p.sendMessage("§fCheck the current enabled modules:");
        p.sendMessage("§c/MRS modules");
        p.sendMessage("§7To reload the whole MRS:");
        p.sendMessage("§c/MRS reload");
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
            completions = Arrays.asList("reload", "modules");
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