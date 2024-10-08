package me.sialim.fakehumans;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.sialim.fakehumans.traits.FHOwnerTrait;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SkinManager {
    private final FakeHumans plugin;

    public SkinManager(FakeHumans plugin) {
        this.plugin = plugin;
    }

    public void setSkinFromUsername(NPC npc, String username) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String uuidUrl = "https://api.mojang.com/users/profiles/minecraft/" + username;
                HttpURLConnection uuidConnection = (HttpURLConnection) new URL(uuidUrl).openConnection();
                uuidConnection.setRequestMethod("GET");

                if (uuidConnection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed to get UUID for username: " + username);
                }

                // Read UUID response correctly
                BufferedReader uuidReader = new BufferedReader(new InputStreamReader(uuidConnection.getInputStream()));
                StringBuilder uuidResponseBuilder = new StringBuilder();
                String line;
                while ((line = uuidReader.readLine()) != null) {
                    uuidResponseBuilder.append(line);
                }
                String uuidResponse = uuidResponseBuilder.toString();

                JsonObject uuidJson = JsonParser.parseString(uuidResponse).getAsJsonObject();
                String uuid = uuidJson.get("id").getAsString();

                String skinUrl = "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false";
                HttpURLConnection skinConnection = (HttpURLConnection) new URL(skinUrl).openConnection();
                skinConnection.setRequestMethod("GET");

                if (skinConnection.getResponseCode() != 200) {
                    throw new RuntimeException("Failed to get skin information for UUID: " + uuid);
                }

                // Read skin response correctly
                BufferedReader skinReader = new BufferedReader(new InputStreamReader(skinConnection.getInputStream()));
                StringBuilder skinResponseBuilder = new StringBuilder();
                while ((line = skinReader.readLine()) != null) {
                    skinResponseBuilder.append(line);
                }
                String skinResponse = skinResponseBuilder.toString();

                JsonObject skinJson = JsonParser.parseString(skinResponse).getAsJsonObject();
                JsonObject properties = skinJson.getAsJsonArray("properties").get(0).getAsJsonObject();

                String texture = properties.get("value").getAsString();
                String signature = properties.get("signature").getAsString();

                Bukkit.getScheduler().runTask(plugin, () -> {
                    SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                    skinTrait.setTexture(texture, signature);
                    skinTrait.setTexture("ewogICJ0aW1lc3RhbXAiIDogMTcyODQxMzY3ODg1MSwKICAicHJvZmlsZUlkIiA6ICI3YWRhMmY0ZjY0Nzk0NmEyOTFkODVhZDUyZDczN2NiZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJzbWFydDExMjU1MCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zMDM0ZThkOTA4NDU4NDg0ODc3MDU3MjQ5ZDFiYmY0MDFjMWQzNjEwNmU5ZDZjNTc0OTMzYjhiZDkyMzA3MmU5IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0",
                            "lI6GhkSWcWPXr7kxzxM/jwPJfBCTq9f8TJbwkKV2xpntKmg0hhEwWt9oN322e/P1SjAc7ovhZznySTl2ZjFow6b2r1kivQ6Xec5EOknz2VGMEL6lT+TIdp8XY8QoazCr8QHdyTJurDltLz2Mn1ifQpsAuG97jBIb/4fl1pYPb1uithdrbTrWgorUIYktCjQzbIe7ybYCbj2XXIzQJuVT/w0rOv8IF0TdJpRBQYTnv/uAX3wsq0daJeQAoAahvlENeVfFYKnaaFDrj+JY2Yq4zlWNrBiB8DuTp/mjhDi9LtoIJF6s0bBkzL+Hro7OdtAFnrVbhmQtwsvP/Dpr3Gw0n3zWus47E67/vqlP5l8pQs5z2edNrZ/2RACRX36O03ZOrd2CVGBa5vOgCmiq5k668kZjpufwvYiCxOjjwdbBmga0zmnUAfOcg32/V7tWPTJzXArcq7HTULB4HhgeCtAsgtikvy2E6mUj/YZPlVXBLtTr/N5Gq0vAWo1zNWZjbJ7nsQV3BhR5+PRF+3ByGCNWyo324j13o9wkdEfmlVaYDzkc1NZ3o3gFBSgZ6XPaXgqfL3LQiFtjpzniGExHDX2cxtgDyCE+RupOamjxVe/DVMJwxKzH+TMkZzk42JO7iMZ5umoCyfnIlntED3yNtNT26IoJDzXkcRgqSMkIUmqWo3A=");
                });
            } catch (Exception e) {
                Bukkit.getLogger().warning("Failed to set skin for " + username + ": " + e.getMessage());
                Bukkit.getScheduler().runTask(plugin, () -> {
                    SkinTrait skinTrait = npc.getOrAddTrait(SkinTrait.class);
                    skinTrait.setSkinPersistent(Bukkit.getPlayer(npc.getOrAddTrait(FHOwnerTrait.class).getOwner()));
                });
            }
        });
    }
}
