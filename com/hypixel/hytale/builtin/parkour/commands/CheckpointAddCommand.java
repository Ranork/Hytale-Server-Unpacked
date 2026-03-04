package com.hypixel.hytale.builtin.parkour.commands;

import com.hypixel.hytale.builtin.parkour.ParkourCheckpoint;
import com.hypixel.hytale.builtin.parkour.ParkourPlugin;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.HiddenFromAdventurePlayers;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.UUID;
import javax.annotation.Nonnull;

public class CheckpointAddCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHECKPOINT_ADD_FAILED = Message.translation("server.commands.checkpoint.add.failed");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CHECKPOINT_ADD_SUCCESS = Message.translation("server.commands.checkpoint.add.success");
   @Nonnull
   private final RequiredArg<Integer> indexArg = this.withRequiredArg("index", "server.commands.checkpoint.add.index.desc", ArgTypes.INTEGER);

   public CheckpointAddCommand() {
      super("add", "server.commands.checkpoint.add.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Integer index = this.indexArg.get(context);
      Int2ObjectMap<UUID> checkpointUUIDMap = ParkourPlugin.get().getCheckpointUUIDMap();
      if (checkpointUUIDMap.containsKey(index)) {
         context.sendMessage(MESSAGE_COMMANDS_CHECKPOINT_ADD_FAILED);
      } else {
         TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         Vector3d position = transformComponent.getPosition();
         Vector3f rotation = transformComponent.getRotation();
         Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
         holder.addComponent(ParkourCheckpoint.getComponentType(), new ParkourCheckpoint(index));
         Model model = ParkourPlugin.get().getParkourCheckpointModel();
         holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
         holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
         holder.addComponent(Nameplate.getComponentType(), new Nameplate(Integer.toString(index)));
         TransformComponent transform = new TransformComponent(position, rotation);
         holder.addComponent(TransformComponent.getComponentType(), transform);
         holder.ensureComponent(UUIDComponent.getComponentType());
         holder.ensureComponent(Intangible.getComponentType());
         holder.ensureComponent(HiddenFromAdventurePlayers.getComponentType());
         store.addEntity(holder, AddReason.SPAWN);
         context.sendMessage(MESSAGE_COMMANDS_CHECKPOINT_ADD_SUCCESS);
      }
   }
}
