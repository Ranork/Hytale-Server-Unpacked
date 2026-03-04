package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class PrefabPathNewCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> pathNameArg = this.withRequiredArg("pathName", "server.commands.npcpath.new.pathName.desc", ArgTypes.STRING);
   @Nonnull
   private final DefaultArg<Double> pauseTimeArg = this.withDefaultArg("pauseTime", "server.commands.npcpath.new.pauseTime.desc", ArgTypes.DOUBLE, 0.0, "0.0");
   @Nonnull
   private final DefaultArg<Float> observationAngleArg = this.withDefaultArg(
      "observationAngleDegrees", "server.commands.npcpath.new.observationAngleDegrees.desc", ArgTypes.FLOAT, 0.0F, "0.0"
   );

   public PrefabPathNewCommand() {
      super("new", "server.commands.npcpath.new.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String pathName = this.pathNameArg.get(context);
      Double pauseTime = this.pauseTimeArg.get(context);
      Float obsvAngle = this.observationAngleArg.get(context);
      UUID uuid = UUID.randomUUID();
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PrefabPathHelper.addMarker(store, ref, uuid, pathName, pauseTime, obsvAngle, (short)-1, 0);
      BuilderToolsPlugin.getState(playerComponent, playerRef).setActivePrefabPath(uuid);
      context.sendMessage(Message.translation("server.npc.npcpath.editingPath").param("path", pathName));
   }
}
