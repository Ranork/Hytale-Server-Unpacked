package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.DebugShape;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class DebugShapeCubeCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_DEBUG_SHAPE_CUBE_SUCCESS = Message.translation("server.commands.debug.shape.cube.success");
   @Nonnull
   private final FlagArg fadeFlag = this.withFlagArg("fade", "server.commands.debug.shape.flag.fade.desc");
   @Nonnull
   private final FlagArg noWireframeFlag = this.withFlagArg("no-wireframe", "server.commands.debug.shape.flag.noWireframe.desc");
   @Nonnull
   private final FlagArg noSolidFlag = this.withFlagArg("no-solid", "server.commands.debug.shape.flag.noSolid.desc");

   public DebugShapeCubeCommand() {
      super("cube", "server.commands.debug.shape.cube.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      ThreadLocalRandom random = ThreadLocalRandom.current();
      Vector3f color = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
      int flags = DebugShapeSubCommand.buildFlags(context, this.fadeFlag, this.noWireframeFlag, this.noSolidFlag);
      DebugUtils.add(world, DebugShape.Cube, DebugUtils.makeMatrix(position, 2.0), color, 30.0F, flags);
      context.sendMessage(MESSAGE_COMMANDS_DEBUG_SHAPE_CUBE_SUCCESS);
   }
}
