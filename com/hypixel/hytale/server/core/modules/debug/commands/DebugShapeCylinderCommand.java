package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class DebugShapeCylinderCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_DEBUG_SHAPE_CYLINDER_SUCCESS = Message.translation("server.commands.debug.shape.cylinder.success");

   public DebugShapeCylinderCommand() {
      super("cylinder", "server.commands.debug.shape.cylinder.desc");
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
      DebugUtils.addCylinder(world, position, color, 2.0, 30.0F);
      context.sendMessage(MESSAGE_COMMANDS_DEBUG_SHAPE_CYLINDER_SUCCESS);
   }
}
