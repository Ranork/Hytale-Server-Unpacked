package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.builtin.instances.InstanceValidator;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDoublePosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class InstanceSpawnCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> instanceNameArg = this.withRequiredArg("instanceName", "server.commands.instances.spawn.arg.name", ArgTypes.STRING)
      .addValidator(new InstanceValidator());
   @Nonnull
   private final OptionalArg<RelativeDoublePosition> positionArg = this.withOptionalArg(
      "position", "server.commands.instances.spawn.arg.position", ArgTypes.RELATIVE_POSITION
   );
   @Nonnull
   private final DefaultArg<Vector3f> rotationArg = this.withDefaultArg(
      "rotation", "server.commands.instances.spawn.arg.rotation", ArgTypes.ROTATION, Vector3f.FORWARD, "server.commands.instances.spawn.arg.rotation.default"
   );

   public InstanceSpawnCommand() {
      super("spawn", "server.commands.instances.spawn.desc");
      this.addAliases("sp");
   }

   protected Vector3f getSpawnRotation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull CommandContext context,
      @Nonnull DefaultArg<Vector3f> rotationArg,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!rotationArg.provided(context) && context.isPlayer()) {
         TransformComponent headRotationComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert headRotationComponent != null;

         return headRotationComponent.getRotation().clone();
      } else {
         return rotationArg.get(context);
      }
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Vector3d position;
      if (!this.positionArg.provided(context)) {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         position = transformComponent.getPosition();
      } else {
         position = this.positionArg.get(context).getRelativePosition(context, world, store);
      }

      Transform returnLocation = new Transform(position.clone(), this.getSpawnRotation(ref, context, this.rotationArg, store).clone());
      String instanceName = this.instanceNameArg.get(context);
      CompletableFuture<World> instanceWorld = InstancesPlugin.get().spawnInstance(instanceName, world, returnLocation);
      InstancesPlugin.teleportPlayerToLoadingInstance(ref, store, instanceWorld, null);
   }
}
