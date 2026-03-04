package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.path.PathPlugin;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.builtin.path.waypoint.IPrefabPathWaypoint;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PrefabPathNodesCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<Integer> worldgenIdArg = this.withRequiredArg("worldgenId", "server.commands.npcpath.nodes.worldgenId.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<UUID> pathArg = this.withRequiredArg("path", "server.commands.npcpath.nodes.path.desc", ArgTypes.UUID);

   public PrefabPathNodesCommand() {
      super("nodes", "server.commands.npcpath.nodes.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Integer worldgenId = this.worldgenIdArg.get(context);
      UUID uuid = this.pathArg.get(context);
      WorldPathData worldPathData = store.getResource(WorldPathData.getResourceType());
      IPrefabPath path = worldPathData.getPrefabPath(worldgenId, uuid, true);
      if (path == null) {
         context.sendMessage(Message.translation("server.npc.npcpath.noSuchPath").param("path", uuid.toString()).param("worldgenId", worldgenId));
      } else {
         StringBuilder sb = new StringBuilder("Path [ ");
         sb.append(path.getName()).append(" ]:");
         sb.append("\n Length: ").append(path.length());
         sb.append("\n Fully loaded: ").append(path.isFullyLoaded());
         sb.append("\n Waypoints: ");
         Message msg = Message.translation("server.npc.npcpath.nodes.pathDesc")
            .param("name", path.getName())
            .param("length", path.length())
            .param("isLoaded", path.isFullyLoaded());
         List<IPrefabPathWaypoint> waypoints = path.getPathWaypoints();
         int[] order = new int[]{0};
         waypoints.forEach(
            waypoint -> {
               if (waypoint == null) {
                  sb.append("\n  ").append('#').append(order[0]).append(" (Not loaded)");
                  msg.insert(Message.translation("server.npc.npcpath.nodes.waypointNotLoaded").param("index", order[0]));
                  order[0]++;
               } else {
                  Vector3d pos = waypoint.getWaypointPosition(store);
                  Vector3f rotation = waypoint.getWaypointRotation(store);
                  sb.append("\n  ").append('#').append(waypoint.getOrder());
                  sb.append(" (").append(pos.x).append(", ").append(pos.y).append(", ").append(pos.z).append(')');
                  sb.append("\n   ").append("Rotation: (").append(rotation.x).append(", ").append(rotation.y).append(", ").append(rotation.z).append(')');
                  sb.append("\n   ").append("Pause time: ").append(waypoint.getPauseTime()).append('s');
                  sb.append("\n   ").append(String.format("Observation angle: %.2f", waypoint.getObservationAngle() * (180.0F / (float)Math.PI)));
                  msg.insert(
                     Message.translation("server.npc.npcpath.nodes.node")
                        .param("index", waypoint.getOrder())
                        .param("posX", pos.x)
                        .param("posY", pos.y)
                        .param("posZ", pos.z)
                        .param("rotX", rotation.x)
                        .param("rotY", rotation.y)
                        .param("rotZ", rotation.z)
                        .param("time", waypoint.getPauseTime())
                        .param("angle", String.format("%.2f", waypoint.getObservationAngle() * (180.0F / (float)Math.PI)))
                  );
                  order[0]++;
               }
            }
         );
         PathPlugin.get().getLogger().at(Level.INFO).log(sb.toString());
         context.sendMessage(msg);
      }
   }
}
