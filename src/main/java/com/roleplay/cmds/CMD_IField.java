package com.roleplay.cmds;

import com.basis.main.main;
import com.roleplay.ifield.IField;
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
 * This command is used to interact with IFields.
 * It also allows creating or delete IFields.
 */
public class CMD_IField implements CommandExecutor, TabCompleter
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("IField"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    if(main.PERMISSIONBOARD.of_isAdmin(ps))
                    {
                        Player p = ps.of_getPlayer();

                        if(!main.SETTINGS.of_isUsingIField())
                        {
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "The §aIField-system§7 is currently §cdisabled§7.");
                            return true;
                        }

                        if(main.IFIELDSERVICE.of_isInSetup(ps))
                        {
                            //  Remove the player from the setup-mode.
                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cYou have left the setup-mode.");
                            main.IFIELDSERVICE.of_removePlayerFromSetup(ps);
                            return true;
                        }

                        if(args.length == 1)
                        {
                            String first = args[0];

                            if(first.equalsIgnoreCase("list"))
                            {
                                IField[] ifields = main.IFIELDSERVICE._CONTEXT.of_getAllIFields();

                                if(ifields != null && ifields.length > 0)
                                {
                                    p.sendMessage("§7═════════════════════════");
                                    p.sendMessage("");
                                    p.sendMessage("§8[§4§lIField - List§8]");
                                    p.sendMessage("");
                                    p.sendMessage("§9Id §f-§9 Name");
                                    for (IField ifield : ifields)
                                    {
                                        p.sendMessage("§a" + (ifield.of_getObjectId()) + " §f- §a" + ifield.of_getInfo());
                                    }
                                    p.sendMessage("");
                                    p.sendMessage("§7═════════════════════════");
                                }
                                //  If no IFields has been found.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo IFields found.");
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
                                IField ifield = main.IFIELDSERVICE.of_getIFieldByName(second);

                                if(ifield != null)
                                {
                                    int rc = main.IFIELDSERVICE._CONTEXT.of_deleteIField(ifield);

                                    if(rc == 1)
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aDeleted IField §f" + ifield.of_getInfo() + "§a.");
                                    }
                                    //  If an error occurred.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn error occurred while deleting the IField.");
                                    }
                                }
                                //  If not found.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo IField found with the name §e" + second + "§c.");
                                }

                                return true;
                            }
                            else if(first.equalsIgnoreCase("tp"))
                            {
                                IField ifield = main.IFIELDSERVICE.of_getIFieldByName(second);

                                if(ifield != null)
                                {
                                    p.teleport(ifield.of_getLocation());
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§aTeleported to IField §f" + ifield.of_getInfo() + "§a.");
                                }
                                //  If not found.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cNo IField found with the name §e" + second + "§c.");
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
                                IField ifield = main.IFIELDSERVICE.of_getIFieldByName(second);

                                if(ifield == null)
                                {
                                    //  Create a new instance with the name...
                                    ifield = new IField(null, new String[] {third}, null);
                                    ifield.of_setInfo(second);

                                    //  Set the iField as powerObject to the player...
                                    ps.of_setPowerObject(ifield);

                                    main.IFIELDSERVICE.of_addPlayer2Setup(ps);
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fYou have defined your §acommand-set§f and §aIField-Name.");
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§eClick on a block to set your command-set to it!");
                                }
                                //  If the IField already exist.
                                else
                                {
                                    main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cAn IField with the name §e" + second + "§c already exists.");
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
        p.sendMessage("§8[§4§lIField - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("");
        p.sendMessage("§7To show the help-text:");
        p.sendMessage("§c/IField");
        p.sendMessage("§7To get a list of all created IFields.");
        p.sendMessage("§c/IField list");
        p.sendMessage("§7To teleport to an IField.");
        p.sendMessage("§c/IField tp <name>");
        p.sendMessage("§7To create a new IField.");
        p.sendMessage("§c/IField create <name> <command-set>");
        p.sendMessage("§7To delete an IField.");
        p.sendMessage("§c/IField delete <name>");
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
