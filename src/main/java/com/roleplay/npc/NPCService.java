package com.roleplay.npc;

import com.basis.ancestor.Objekt;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.UUID;

public class NPCService extends Objekt
{
    /**
     * This function is used to create a GameProfile by using the SkinName.
     * If no SkinName is given it only creates the GameProfile.
     * @param skinName The SkinName of the GameProfile. This can be null.
     * @return The GameProfile.
     */
    public GameProfile of_createGameProfile(String skinName)
    {
        GameProfile gameProfile = new GameProfile(UUID.randomUUID(), "NPC");

        if(skinName != null)
        {
            try
            {
                //  Get information via. API for the GameProfile.
                HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", skinName)).openConnection();

                //  Check if the connection is open.
                if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK)
                {
                    ArrayList<String> lines = new ArrayList<>();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    reader.lines().forEach(lines::add);

                    //  Extract the properties.
                    String reply = String.join(" ",lines);
                    int indexOfValue = reply.indexOf("\"value\": \"");
                    int indexOfSignature = reply.indexOf("\"signature\": \"");
                    String skin = reply.substring(indexOfValue + 10, reply.indexOf("\"", indexOfValue + 10));
                    String signature = reply.substring(indexOfSignature + 14, reply.indexOf("\"", indexOfSignature + 14));

                    //  Set the skin.
                    gameProfile.getProperties().put("textures", new Property("textures", skin, signature));
                }

            } catch (IOException ignored) { }
        }

        return gameProfile;
    }
}
