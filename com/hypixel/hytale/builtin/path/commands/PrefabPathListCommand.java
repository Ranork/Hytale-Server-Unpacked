package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.path.PathPlugin;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PrefabPathListCommand extends AbstractWorldCommand {
   public PrefabPathListCommand() {
      super("list", "server.commands.npcpath.list.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      WorldPathData worldPathDataResource = store.getResource(WorldPathData.getResourceType());
      List<IPrefabPath> paths = worldPathDataResource.getAllPrefabPaths();
      StringBuilder sb = new StringBuilder("Active prefab paths:\n");
      Message msg = Message.translation("server.npc.npcpath.list.prefabPaths");

      for (IPrefabPath path : paths) {
         sb.append(' ').append(path.getWorldGenId()).append('.').append(path.getId());
         sb.append(" (").append(path.getName()).append(')');
         sb.append(" [ Length: ").append(path.length());
         sb.append(", Loaded nodes: ").append(path.loadedWaypointCount()).append(" ]\n");
         msg.insert(
            Message.translation("server.npc.npcpath.list.details")
               .param("worldGenId", path.getWorldGenId())
               .param("pathId", path.getId().toString())
               .param("pathName", path.getName())
               .param("length", path.length())
               .param("count", path.loadedWaypointCount())
         );
      }

      PathPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
      context.sendMessage(msg);
   }
}
