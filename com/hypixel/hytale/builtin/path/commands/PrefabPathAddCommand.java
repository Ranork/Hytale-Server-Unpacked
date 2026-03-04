package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.path.WorldPathData;
import com.hypixel.hytale.builtin.path.path.IPrefabPath;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PrefabPathAddCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_NPC_PATH_ADD_NO_ACTIVE_PATH = Message.translation("server.commands.npcpath.add.noActivePath");
   @Nonnull
   private final DefaultArg<Double> pauseTimeArg = this.withDefaultArg("pauseTime", "server.commands.npcpath.add.pauseTime.desc", ArgTypes.DOUBLE, 0.0, "0.0");
   @Nonnull
   private final DefaultArg<Float> observationAngleArg = this.withDefaultArg(
      "observationAngleDegrees", "server.commands.npcpath.add.observationAngleDegrees.desc", ArgTypes.FLOAT, 0.0F, "0.0"
   );
   @Nonnull
   private final DefaultArg<Integer> indexArg = this.withDefaultArg("index", "server.commands.npcpath.add.index.desc", ArgTypes.INTEGER, -1, "-1");

   public PrefabPathAddCommand() {
      super("add", "server.commands.npcpath.add.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      UUID path = BuilderToolsPlugin.getState(playerComponent, playerRef).getActivePrefabPath();
      if (path == null) {
         throw new GeneralCommandException(MESSAGE_COMMANDS_NPC_PATH_ADD_NO_ACTIVE_PATH);
      } else {
         Double pauseTime = this.pauseTimeArg.get(context);
         Float obsvAngle = this.observationAngleArg.get(context);
         short targetIndex = this.indexArg.get(context).shortValue();
         WorldPathData worldPathData = store.getResource(WorldPathData.getResourceType());
         IPrefabPath parentPath = worldPathData.getPrefabPath(0, path, false);
         if (parentPath != null && parentPath.isFullyLoaded()) {
            PrefabPathHelper.addMarker(store, ref, path, parentPath.getName(), pauseTime, obsvAngle, targetIndex, parentPath.getWorldGenId());
         } else {
            context.sendMessage(Message.translation("server.npc.npcpath.pathMustBeLoaded").param("path", path.toString()));
         }
      }
   }
}
