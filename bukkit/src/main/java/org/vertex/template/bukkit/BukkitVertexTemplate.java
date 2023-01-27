package org.vertex.template.bukkit;

import org.vertex.bukkit.VertexBukkitPlugin;

public class BukkitVertexTemplate extends VertexBukkitPlugin {

    private static BukkitVertexTemplate instance;

    @Override
    public void init() {

    }

    public static BukkitVertexTemplate get() {
        return instance;
    }
}
