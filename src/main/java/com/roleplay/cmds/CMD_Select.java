package com.roleplay.cmds;

import com.basis.ancestor.CMDExecutor;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.board.PermissionBoard;
import com.roleplay.spieler.Spieler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

/**
 * @Created 17.07.2022
 * @Author Nihar
 * @Description
 * This command has been implemented to
 * select data from the database in game or
 * by the console.
 */
public class CMD_Select extends CMDExecutor
{
    /* ************************* */
    /* COMMAND */
    /* ************************* */

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args)
    {
        if(cmd.getName().equalsIgnoreCase("Select"))
        {
            if(sender instanceof  Player)
            {
                Spieler ps = main.SPIELERSERVICE._CONTEXT.of_getPlayer(sender.getName());

                if(ps != null)
                {
                    //  Check if the given player is an admin.
                    if(PermissionBoard.of_getInstance().of_isAdmin(ps))
                    {
                        of_preCommand(sender, args);
                    }
                    //  No Permissions.
                    else
                    {
                        main.SPIELERSERVICE.of_sendNoPermissionsMessage(ps);
                    }
                }
            }
            //  Console-Command.
            else
            {
                int rc = -1;

                //  Establish a database connection if no players are online.
                if(main.SQL != null && !main.SQL.of_isConnected() && Bukkit.getOnlinePlayers().size() == 0)
                {
                    rc = main.SQL.of_createConnection();

                    if(rc == 1)
                    {
                        of_preCommand(sender, args);
                    }
                    //  Error while establishing a database connection.
                    else
                    {
                        of_sendMessage(sender, "There was an error while creating the database-connection!");
                    }
                }
                else if(main.SQL != null && main.SQL.of_isConnected())
                {
                    of_preCommand(sender, args);
                }

                //  If the connection has been established, we close it then.
                if(rc == 1 && Bukkit.getOnlinePlayers().size() == 0)
                {
                    main.SQL.of_closeConnection();
                }
            }

            return true;
        }

        return false;
    }

    /* ************************* */
    /* OBJECT - METHODS */
    /* ************************* */

    private void of_preCommand(CommandSender p, String[] args)
    {
        //  Add the SELECT-String to the Args-Array.
        args = Sys.of_addArrayValueToFirstField(args, "select");

        if(args.length >= 4)
        {
            String sqlStatement = of_buildSQLStatement(args);

            if(sqlStatement != null)
            {
                String errorMessage = of_validateSQL(sqlStatement);

                if(errorMessage == null)
                {
                    ArrayList<String> resultList = of_executeSQLStatement(sqlStatement);

                    //  List of the SQL-Rows.
                    if(resultList.isEmpty())
                    {
                        of_sendMessage(p, "§e0 results. Correct syntax?");
                    }
                    else
                    {
                        //  Iterate through all Result-Lines.
                        for (String row : resultList)
                        {
                            p.sendMessage(row);
                        }

                        //  We need to subtract the size because the column-header will be count as welL!
                        of_sendMessage(p, "§e" + ( resultList.size() -1 ) + "§f results.");
                    }
                }
                else
                {
                    of_sendMessage(p, errorMessage);
                }
            }
            else
            {
                of_sendMessage(p, "Error while validating the SQL-Select-Statement!");
            }
        }
        //  Own command-cmd-helper because the console should get the help-text as well!
        else
        {
            of_sendMessage(p, "§c/SELECT <Column1,[...]> FROM <Table1, [...]>  <OPT: WHERE <Table-join>>");
        }
    }

    /* ************************* */
    /* COMMAND - VALIDATION */
    /* ************************* */

    private String of_validateSQL(String sql)
    {
        if(sql.contains("from"))
        {
            String[] sqlFragments = sql.split("from");

            if(sqlFragments.length == 2)
            {
                //  Behind: FROM table1
                String sqlBehindFrom = sqlFragments[1];

                //  Check for: table1, table2 and no WHERE-clause
                if(!sqlBehindFrom.contains("where") && sqlBehindFrom.contains(","))
                {
                    return "§cYou need to define a 'WHERE'-clause in your SQL-Select!";
                }

                //  Behind: WHERE table2
                String[] sqlBehindWhereArray = sqlBehindFrom.split("where");

                if(sqlBehindWhereArray.length == 2)
                {
                    String sqlBeforeWhere = sqlBehindWhereArray[0];
                    String sqlBehindWhere = sqlBehindWhereArray[1];

                    if(sqlBeforeWhere.contains(",") && !sqlBehindWhere.contains("="))
                    {
                        return "§cYou need to define a 'JOIN'-part in your SQL-Select!";
                    }
                }
            }
            else
            {
                return "§cYou can only define 'ONE' From-Part in your SQL-Select!";
            }
        }
        else
        {
            return "§cYou need to define a 'FROM'-Part in your SQL-Select!";
        }

        return null;
    }

    /* ************************* */
    /* SQL - METHODS */
    /* ************************* */

    private ArrayList<String> of_executeSQLStatement(String sql)
    {
        //  Execute the sql-statement and fetch the result.
        ResultSet result = main.SQL.of_getResultSet_suppress(sql, false);
        ArrayList<String> resultList = new ArrayList<String>();

        try
        {
            if(result != null)
            {
                //  Iterate through each row!
                while(result.next())
                {
                    ResultSetMetaData resultMeta = result.getMetaData();
                    StringBuilder columnBuilder = new StringBuilder();
                    StringBuilder resultBuilder = new StringBuilder();
                    int columnCount = resultMeta.getColumnCount();

                    for (int i = 1; i <= columnCount; i++ )
                    {
                        String columName = resultMeta.getColumnName(i);

                        if(columName != null && !columName.isEmpty())
                        {
                            columnBuilder.append(" | ").append(columName);
                            resultBuilder.append(" §l§8|§f ").append(result.getString(columName));
                        }
                    }

                    //  We only add the columnBuilder (Table-header) only ONCE to the ResultList!
                    if(resultList.isEmpty())
                    {
                        resultList.add(columnBuilder.toString());
                    }

                    //  If some display/label-columns are selected we translate the result in color-codes!
                    resultList.add(resultBuilder.toString().replace("&", "§"));
                }
            }
        }
        catch (Exception ignored) { }

        return resultList;
    }

    private String of_buildSQLStatement(String[] args)
    {
        StringBuilder sqlBuilder = new StringBuilder();

        for(String sql : args)
        {
            sqlBuilder.append(" ").append(sql);
        }

        //  Build the SQL-Select-Statement.
        String sqlStatement = sqlBuilder.append(";").toString();
        String[] sqlFragments = sqlStatement.split(";");

        if(sqlFragments.length > 0)
        {
            sqlStatement = sqlFragments[0];
            return sqlStatement.toLowerCase();
        }

        return null;
    }

    /* ************************* */
    /* SEND COMMANDS/CMD-HELPER */
    /* ************************* */

    private void of_sendMessage(CommandSender p, String message)
    {
        p.sendMessage("§8[§cDB§8]§f " + message);
    }

    @Override
    public void of_sendCMDHelperText(Player p) { /* Do not override this method! */ }

    /* ************************* */
    /* TAB-COMPLETE */
    /* ************************* */

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args)
    {
        return null;
    }
}
