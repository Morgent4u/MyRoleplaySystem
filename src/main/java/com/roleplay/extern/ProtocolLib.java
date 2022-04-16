package com.roleplay.extern;

import com.basis.ancestor.Objekt;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;

/**
 * @Created 16.04.2022
 * @Author Nihar
 * @Description
 * This class is used to represent protocolLib-objects.
 */
public class ProtocolLib extends Objekt
{
    public ProtocolManager PROTOCOLMANAGER;

    /* ************************************* */
    /* LOADER */
    /* ************************************* */

    @Override
    public int of_load()
    {
        try
        {
            PROTOCOLMANAGER = ProtocolLibrary.getProtocolManager();
            return 1;
        }
        catch (Exception e)
        {
            of_sendErrorMessage(e, "of_load();", "Error while registering the ProtocolLib-service to this plugin.");
        }

        return -1;
    }
}
