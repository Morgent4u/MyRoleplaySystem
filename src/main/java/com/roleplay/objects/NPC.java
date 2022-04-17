package com.roleplay.objects;

import com.basis.ancestor.Objekt;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.network.syncher.DataWatcherRegistry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.server.network.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.CraftServer;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * @Created 17.04.2022
 * @Author Nihar
 * @Description
 * This class is used to represent an NPC.
 * The NPC can be created and destroyed.
 *
 * Sources:
 * https://www.spigotmc.org/threads/how-to-create-npc.537833/
 * https://www.spigotmc.org/threads/how-to-create-and-modify-npcs.400753/
 *
 */
public class NPC extends Objekt
{
    //  Attributes:
    EntityPlayer entityPlayer;
    GameProfile gameProfile;
    Player p;
    float locYaw;

    /* ************************************* */
    /* CONSTRUCTOR */
    /* ************************************* */

    public NPC(Player p)
    {
        this.p = p;
        this.locYaw = p.getLocation().getYaw();
    }

    /* ************************************* */
    /* OBJECT METHODS */
    /* ************************************* */

    /**
     * Create the NPC.
     */
    public void of_createNPC()
    {
        //  Create an NPC.
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        Location loc = p.getLocation();
        WorldServer world = ((CraftWorld) Objects.requireNonNull(loc.getWorld())).getHandle();

        //  Set the GameProfile and the EntityPlayer.
        gameProfile = new GameProfile(UUID.randomUUID(), "NPC001");
        entityPlayer = new EntityPlayer(server, world, gameProfile);

        //  Set the location.
        entityPlayer.b(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());

        //  Add the NPC to the world / for the player.
        of_addNPCPacket();
    }

    /**
     * Set a skin by the username to the NPC.
     * @param userName The username of the skin.
     */
    public void of_setSkin(String userName)
    {
        of_destroyNPC();

        try
        {
            //  Get information via. API for the GameProfile.
            HttpsURLConnection connection = (HttpsURLConnection) new URL(String.format("https://api.ashcon.app/mojang/v2/user/%s", userName)).openConnection();

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
        }
        catch (IOException e)
        {
            of_sendErrorMessage(e, "NPC.of_setSkin();", "Could not set the skin. Please check your internet connection.");
            return;
        }

        // The client settings.
        DataWatcher watcher = new DataWatcher(entityPlayer);
        watcher.a(new DataWatcherObject<>(16, DataWatcherRegistry.a), (byte)127);

        //  Create the connection and the packet.
        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;
        connection.a(new PacketPlayOutEntityMetadata(entityPlayer.getBukkitEntity().getEntityId(), watcher, true));

        of_addNPCPacket();
    }

    /**
     * Add the NPC to the world / for the player.
     */
    private void of_addNPCPacket()
    {
        //  Create the connection and get the location.
        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;

        //  Send packet...
        connection.a(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.a, entityPlayer));
        connection.a(new PacketPlayOutNamedEntitySpawn(entityPlayer));
        connection.a(new PacketPlayOutEntityHeadRotation(entityPlayer, (byte) (locYaw * 256 / 360)));
    }

    /**
     * Destroy the NPC.
     */
    public void of_destroyNPC()
    {
        //  Create the connection.
        PlayerConnection connection = ((CraftPlayer) p).getHandle().b;

        //  Send a packet to destroy the NPC.
        connection.a(new PacketPlayOutEntityDestroy(entityPlayer.ae()));
    }
}
