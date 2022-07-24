package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.extern.DataStore;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.PermissionBoard;
import com.roleplay.manager.LabelManager;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Created 23.07.2022
 * @Author Nihar
 * @Description
 * This command has been implemented to
 * add categories and labels to the
 * label-system.
 */
public class CMD_Label extends CMDExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Label"))
        {
            if(sender instanceof Player)
            {
                Player p = (Player) sender;
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(p.getName());

                if(ps != null)
                {
                    if(PermissionBoard.of_getInstance().of_isAdmin(ps))
                    {
                        if(args.length == 2)
                        {
                            DataStore dataStore = null;
                            String listFrom = null;

                            if(args[0].equalsIgnoreCase("category"))
                            {
                                if(args[1].equalsIgnoreCase("list"))
                                {
                                    dataStore = LabelManager.of_getInstance().of_getDataStore4LabelEnums();
                                    listFrom = "Categories";
                                }
                            }
                            else if(args[0].equalsIgnoreCase("label"))
                            {
                                if(args[1].equalsIgnoreCase("list"))
                                {
                                    dataStore = LabelManager.of_getInstance().of_getDataStore4Labels();
                                    listFrom = "Labels";
                                }
                            }

                            if(dataStore != null)
                            {
                                if(dataStore.of_getRowCount() > 0)
                                {
                                    String[] columnEntries = dataStore.of_getColumnEntries("text");

                                    if(columnEntries != null && columnEntries.length > 0)
                                    {
                                        p.sendMessage("§7═════════════════════════");
                                        p.sendMessage("");
                                        p.sendMessage("§8[§4§lList of "+listFrom+"§8]");
                                        p.sendMessage("");
                                        p.sendMessage("§9Id - Text");
                                        for(int i = 1; i < columnEntries.length; i++)
                                        {
                                            p.sendMessage("§7"+ i + " §f-§a " + columnEntries[i].replace("&", "§"));
                                        }
                                        p.sendMessage("");
                                        p.sendMessage("§7═════════════════════════");
                                        return true;
                                    }
                                }

                                main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThere are no defined " + listFrom + ".");
                                return true;
                            }
                        }

                        if(args.length >= 3)
                        {
                            String first = args[0];
                            String second = args[1];

                            if(first.equalsIgnoreCase("create"))
                            {
                                if(second.equalsIgnoreCase("category"))
                                {
                                    StringBuilder buildCategory = new StringBuilder();

                                    for(int i = 2; i < args.length; i++)
                                    {
                                        buildCategory.append(args[i]).append(" ");
                                    }

                                    String labelCategory = buildCategory.toString().replace("&", "§");
                                    int rc = LabelManager.of_getInstance().of_createNewLabelEnum(labelCategory);

                                    if(rc == 1)
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe new label-category '§e"+labelCategory+"§f' has been added.");
                                    }
                                    //  The label-category already exists!
                                    else if(rc == 0)
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe label-category '§e"+labelCategory+"§f' already exist!");
                                    }
                                    //  If an error occurs.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThere was an error while adding the current label-category '§e"+labelCategory+"§c' to the categories!");
                                    }

                                    return true;
                                }
                                else if(second.equalsIgnoreCase("label") && args.length >= 4)
                                {
                                    int enumId = Sys.of_getString2Int(args[2]);

                                    if(enumId != -1)
                                    {
                                        StringBuilder labelBuilder = new StringBuilder();

                                        //  We need to subtract because this is the player-input!
                                        enumId--;

                                        for(int i = 3; i < args.length; i++)
                                        {
                                            labelBuilder.append(args[i]).append(" ");
                                        }

                                        String labelText = labelBuilder.toString().replace("&", "§");
                                        int rc = LabelManager.of_getInstance().of_createNewLabel(enumId, labelText);

                                        if(rc == 1)
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§fThe new label '§e"+labelText+"§f' has been added.");
                                        }
                                        //  The label-category does not exist!
                                        else if(rc == 0)
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe label-category with the id '§e" + args[2]+ "§c' does not exist!");
                                        }
                                        //  If an error occurs.
                                        else
                                        {
                                            main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThere was an error while adding the current label '§e"+labelText+"§c' to the labels!");
                                        }
                                    }
                                    //  Wrong input for the label-category-id.
                                    else
                                    {
                                        main.SPIELERSERVICE.of_sendPluginMessage2Player(ps, "§cThe label-category with the id '§e" + args[3]+ "§c' does not exist!");
                                    }

                                    return true;
                                }
                            }
                        }

                        //  Send the default help-text.
                        of_sendCMDHelperText(p);
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
        }

        return false;
    }

    /* ************************* */
    /* CMD-Text Helper */
    /* ************************* */

    @Override
    public void of_sendCMDHelperText(Player p)
    {
        p.sendMessage("§7═════════════════════════");
        p.sendMessage("");
        p.sendMessage("§8[§4§lLabel - Help§8]");
        p.sendMessage("");
        p.sendMessage("§fHello §d"+p.getName() + "§f,");
        p.sendMessage("§fyou can use the following commands:");
        p.sendMessage("");
        p.sendMessage("§7To show the help-text:");
        p.sendMessage("§c/Label");
        p.sendMessage("§7To get a list of all label-categories:");
        p.sendMessage("§c/Label category list");
        p.sendMessage("§7To create a new label-category:");
        p.sendMessage("§c/Label create category <name>");
        p.sendMessage("§7To delete a label-category:");
        p.sendMessage("§c/Label delete category <enumId>");
        p.sendMessage("§7To get a list of all labels:");
        p.sendMessage("§c/Label label list");
        p.sendMessage("§7To create a label for a category:");
        p.sendMessage("§c/Label create label <enumId> <label-text>");
        p.sendMessage("§7To delete a label from a category:");
        p.sendMessage("§c/Label delete label <labelId>");
        p.sendMessage("");
        p.sendMessage("§7═════════════════════════");
    }

    /* ************************* */
    /* TAB-COMPLETER */
    /* ************************* */

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        // List in with the completions will be stored.
        List<String> list = new ArrayList<>();
        Iterable<String> completions = null;

        //  All array arguments to lowercase
        for(int i = 0; i < args.length; i++)
        {
            args[i] = args[i].toLowerCase();
        }

        //  Handle the completions.
        if(args.length == 1)
        {
            completions = Arrays.asList("category", "label", "create", "delete");
        }
        else if(args.length == 2)
        {
            switch (args[0])
            {
                case "label":
                case "category":
                    completions = Collections.singletonList("list");
                    break;
                case "create":
                case "delete":
                    completions = Arrays.asList("category", "label");
                    break;
            }
        }
        else if(args.length == 3)
        {
            switch (args[1])
            {
                case "category":
                    if(args[0].equalsIgnoreCase("create"))
                    {
                        completions = Collections.singletonList("<name>");
                    }
                    else if(args[0].equalsIgnoreCase("delete"))
                    {
                        completions = Collections.singletonList("<enumId>");
                    }
                    break;
                case "label":
                    if(args[0].equalsIgnoreCase("create"))
                    {
                        completions = Collections.singletonList("<enumId>");
                    }
                    else if(args[0].equalsIgnoreCase("delete"))
                    {
                        completions = Collections.singletonList("<labelId>");
                    }
                    break;
            }
        }
        else if(args.length == 4)
        {
            if(args[1].equalsIgnoreCase("label") && args[2].equalsIgnoreCase("<enumId>"))
            {
                completions = Collections.singletonList("<label-text>");
            }
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
