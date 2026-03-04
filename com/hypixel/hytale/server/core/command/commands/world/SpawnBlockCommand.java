package com.hypixel.hytale.server.core.command.commands.world;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDoublePosition;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SpawnBlockCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<String> blockArg = this.withRequiredArg("block", "server.commands.spawnblock.arg.block.desc", ArgTypes.BLOCK_TYPE_KEY);
   @Nonnull
   private final RequiredArg<RelativeDoublePosition> positionArg = this.withRequiredArg(
      "position", "server.commands.spawnblock.arg.position.desc", ArgTypes.RELATIVE_POSITION
   );
   @Nonnull
   private final DefaultArg<Vector3f> rotationArg = this.withDefaultArg(
      "rotation", "server.commands.spawnblock.arg.rotation.desc", ArgTypes.ROTATION, Vector3f.FORWARD, "server.commands.spawnblock.arg.rotation.desc"
   );

   public SpawnBlockCommand() {
      super("spawnblock", "server.commands.spawnblock.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      String blockTypeKey = context.get(this.blockArg);
      Vector3d position = context.get(this.positionArg).getRelativePosition(context, world, store);
      Vector3f rotation = this.rotationArg.get(context);
      TimeResource timeResource = world.getEntityStore().getStore().getResource(TimeResource.getResourceType());
      Holder<EntityStore> blockEntityHolder = BlockEntity.assembleDefaultBlockEntity(timeResource, blockTypeKey, position);
      TransformComponent transformComponent = blockEntityHolder.ensureAndGetComponent(TransformComponent.getComponentType());
      transformComponent.setPosition(position);
      transformComponent.setRotation(rotation);
      UUIDComponent uuidComponent = blockEntityHolder.getComponent(UUIDComponent.getComponentType());
      String entityIdString = uuidComponent == null ? "None" : uuidComponent.getUuid().toString();
      world.getEntityStore().getStore().addEntity(blockEntityHolder, AddReason.SPAWN);
      context.sendMessage(Message.translation("server.commands.spawnblock.success").param("id", entityIdString));
   }
}
