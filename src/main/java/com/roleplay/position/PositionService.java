package com.roleplay.position;

import com.basis.ancestor.Objekt;
import com.basis.main.main;
import com.basis.sys.Sys;
import com.roleplay.spieler.Spieler;

/**
 * @Created 19.06.2022
 * @Author Nihar
 * @Description
 * This service-class is used to interact with the Position-
 * or Position-context-object.
 */
public class PositionService  extends Objekt
{
    //  Attributes:
    public PositionContext _CONTEXT;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        _CONTEXT = new PositionContext(Sys.of_getMainFilePath() + "Positions//");
        return _CONTEXT.of_load();
    }

    /* ************************************* */
    /* DEBUG-CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Loaded Positions: " + _CONTEXT.of_getLoadedPositions());
    }

    /* ************************************* */
    /* GETTER */
    /* ************************************* */

    /**
     * This function is used to get the Position-object by any player input.
     * @param userInput The user input.
     * @return The Position-object.
     */
    public Position of_getPositionByAnything(String userInput)
    {
        Position pos = null;
        int posId = Sys.of_getString2Int(userInput);

        if(posId != -1)
        {
            //  Get the position by the position-id.
            pos = _CONTEXT.of_getPositionByObjectId(posId);
        }
        else
        {
            //  Try to get the position by the Position-Name or the file-name.
            pos = of_getPositionByPositionName(userInput);

            if(pos == null)
            {
                pos = of_getPositionByFileName(userInput);
            }
        }

        return pos;
    }

    public Position of_getPositionByPositionName(String positionName)
    {
        Position[] positions = _CONTEXT.of_getAllPositions();

        for(Position pos : positions)
        {
            if(pos.of_getPositionName().equalsIgnoreCase(positionName))
            {
                return pos;
            }
        }

        return null;
    }

    public Position of_getPositionByFileName(String fileName)
    {
        Position[] positions = _CONTEXT.of_getAllPositions();
        fileName = fileName.toLowerCase().replace(".yml", "");

        for(Position pos : positions)
        {
            if(pos.of_getInfo().equalsIgnoreCase(fileName))
            {
                return pos;
            }
        }

        return null;
    }
}
