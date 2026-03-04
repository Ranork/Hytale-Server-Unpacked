package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDoublePosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.command.system.exceptions.GeneralCommandException;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.text.DecimalFormat;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldConfigSetSpawnCommand extends AbstractWorldCommand {
   @Nonnull
   private static final DecimalFormat DECIMAL = new DecimalFormat("#.###");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERROR_PROVIDE_POSITION = Message.translation("server.commands.errors.providePosition");
   @Nonnull
   private final OptionalArg<RelativeDoublePosition> positionArg = this.withOptionalArg(
      "position", "server.commands.world.config.setspawn.position.desc", ArgTypes.RELATIVE_POSITION
   );
   @Nonnull
   private final DefaultArg<Vector3f> rotationArg = this.withDefaultArg(
      "rotation",
      "server.commands.world.config.setspawn.rotation.desc",
      ArgTypes.ROTATION,
      Vector3f.FORWARD,
      "server.commands.world.config.setspawn.rotation.default.desc"
   );

   public WorldConfigSetSpawnCommand() {
      super("setspawn", "server.commands.world.config.setspawn.desc");
      this.addSubCommand(new WorldConfigSetSpawnDefaultCommand());
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Vector3d position;
      if (this.positionArg.provided(context)) {
         RelativeDoublePosition relativePosition = this.positionArg.get(context);
         position = relativePosition.getRelativePosition(context, world, store);
      } else {
         if (!context.isPlayer()) {
            throw new GeneralCommandException(MESSAGE_COMMANDS_ERROR_PROVIDE_POSITION);
         }

         Ref<EntityStore> playerRef = context.senderAsPlayerRef();
         if (playerRef == null || !playerRef.isValid()) {
            throw new GeneralCommandException(MESSAGE_COMMANDS_ERROR_PROVIDE_POSITION);
         }

         TransformComponent transformComponent = store.getComponent(playerRef, TransformComponent.getComponentType());

         assert transformComponent != null;

         position = transformComponent.getPosition().clone();
      }

      Vector3f rotation;
      if (this.rotationArg.provided(context)) {
         rotation = this.rotationArg.get(context);
      } else if (context.isPlayer()) {
         Ref<EntityStore> playerRefx = context.senderAsPlayerRef();
         if (playerRefx != null && playerRefx.isValid()) {
            HeadRotation headRotationComponent = store.getComponent(playerRefx, HeadRotation.getComponentType());

            assert headRotationComponent != null;

            rotation = headRotationComponent.getRotation();
         } else {
            rotation = this.rotationArg.get(context);
         }
      } else {
         rotation = this.rotationArg.get(context);
      }

      Transform transform = new Transform(position, rotation);
      WorldConfig worldConfig = world.getWorldConfig();
      worldConfig.setSpawnProvider(new GlobalSpawnProvider(transform));
      worldConfig.markChanged();
      world.getLogger().at(Level.INFO).log("Set spawn provider to: %s", worldConfig.getSpawnProvider());
      context.sendMessage(
         Message.translation("server.universe.setspawn.info")
            .param("posX", DECIMAL.format(position.getX()))
            .param("posY", DECIMAL.format(position.getY()))
            .param("posZ", DECIMAL.format(position.getZ()))
            .param("rotX", DECIMAL.format(rotation.getX()))
            .param("rotY", DECIMAL.format(rotation.getY()))
            .param("rotZ", DECIMAL.format(rotation.getZ()))
      );
   }
}
