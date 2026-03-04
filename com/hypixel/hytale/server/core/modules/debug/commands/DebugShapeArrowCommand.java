package com.hypixel.hytale.server.core.modules.debug.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class DebugShapeArrowCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_DEBUG_SHAPE_ARROW_SUCCESS = Message.translation("server.commands.debug.shape.arrow.success");

   public DebugShapeArrowCommand() {
      super("arrow", "server.commands.debug.shape.arrow.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d pos = transformComponent.getPosition();
      ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());

      assert modelComponent != null;

      Model model = modelComponent.getModel();
      HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

      assert headRotationComponent != null;

      Vector3f headRotation = headRotationComponent.getRotation();
      float lookYaw = headRotation.getYaw();
      float lookPitch = headRotation.getPitch();
      Matrix4d tmp = new Matrix4d();
      float eyeHeight = model != null ? model.getEyeHeight(ref, store) : 0.0F;
      ThreadLocalRandom random = ThreadLocalRandom.current();
      Vector3f color = new Vector3f(random.nextFloat(), random.nextFloat(), random.nextFloat());
      Matrix4d matrix = new Matrix4d();
      matrix.identity();
      matrix.translate(pos.x, pos.y + eyeHeight, pos.z);
      matrix.rotateAxis(-lookYaw, 0.0, 1.0, 0.0, tmp);
      matrix.rotateAxis((Math.PI / 2) - lookPitch, 1.0, 0.0, 0.0, tmp);
      DebugUtils.addArrow(world, matrix, color, 1.0, 30.0F, true);
      context.sendMessage(MESSAGE_COMMANDS_DEBUG_SHAPE_ARROW_SUCCESS);
   }
}
