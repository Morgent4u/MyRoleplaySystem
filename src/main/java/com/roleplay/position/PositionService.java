package com.roleplay.position;

import com.basis.ancestor.Objekt;
import com.basis.sys.Sys;

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
        _CONTEXT = new PositionContext("PositionContext", Sys.of_getMainFilePath() + "Positions//");
        return _CONTEXT.of_load();
    }

    /* ************************************* */
    /* DEBUG-CENTER */
    /* ************************************* */

    @Override
    public void of_sendDebugDetailInformation()
    {
        Sys.of_sendMessage("Loaded Positions: " + _CONTEXT.of_getLoadedObjects());
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
            Objekt object = _CONTEXT.of_getObjectById(posId);

            if(object instanceof Position)
            {
                pos = (Position) object;
            }
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
        Objekt[] objects = _CONTEXT.of_getAllObjects();

        if(objects != null && objects.length > 0)
        {
            for(Objekt objekt : objects)
            {
                if(objekt instanceof Position)
                {
                    Position pos = (Position) objekt;

                    if(pos.of_getPositionName().equalsIgnoreCase(positionName))
                    {
                        return pos;
                    }
                }
            }
        }

        return null;
    }

    public Position of_getPositionByFileName(String fileName)
    {
        Objekt[] objects = _CONTEXT.of_getAllObjects();
        fileName = fileName.toLowerCase().replace(".yml", "");

        if(objects != null && objects.length > 0)
        {
            for(Objekt objekt : objects)
            {
                if(objekt instanceof Position)
                {
                    Position pos = (Position) objekt;

                    if(pos.of_getInfo().equalsIgnoreCase(fileName))
                    {
                        return pos;
                    }
                }
            }
        }

        return null;
    }
}
