package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.main.main;
import com.roleplay.board.PermissionBoard;
import com.roleplay.spieler.Spieler;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @Created 16.04.2022
 * @Author Nihar
 * @Description
 * This command is used to test some functions of the plugin.
 */
public class CMD_Test extends CMDExecutor
{
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Test"))
        {
            if(sender instanceof Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    Player p = ps.of_getPlayer();

                    if(PermissionBoard.of_getInstance().of_isAdmin(ps))
                    {
                        if(args.length == 0)
                        {
                            /*
                            //create the book
                            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
                            BookMeta bookMeta = (BookMeta) book.getItemMeta();

                            //create a page
                            BaseComponent[] page = new ComponentBuilder("Click me")
                                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/time set day"))
                                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Go to the spigot website!").create()))
                                    .create();

                            //add the page to the meta
                            assert bookMeta != null;
                            bookMeta.spigot().addPage(page);

                            //set the title and author of this book
                            bookMeta.setTitle("Interactive Book");
                            bookMeta.setAuthor("gigosaurus");

                            //update the ItemStack with this new meta
                            book.setItemMeta(bookMeta);
                            p.openBook(book);
                            */

                            // Do not change this!
                            p.sendMessage("§cCommand has been executed!");
                            return true;
                        }

                        String first = args[0];

                        if(args.length == 1)
                        {
                            // Do not change this!
                            p.sendMessage("§cCommand has been executed! Args: 1");
                            return true;
                        }

                        String second = args[1];

                        if(args.length == 2)
                        {
                            // Do not change this!
                            p.sendMessage("§cCommand has been executed! Args: 2");
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

    @Override
    public void of_sendCMDHelperText(Player p)
    {
        p.sendMessage("§c/Test");
    }
}
