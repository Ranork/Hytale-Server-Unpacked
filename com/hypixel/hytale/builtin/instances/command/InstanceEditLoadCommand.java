package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.builtin.instances.InstanceValidator;
import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class InstanceEditLoadCommand extends AbstractAsyncCommand {
   private final RequiredArg<String> instanceNameArg = this.withRequiredArg("instanceName", "server.commands.instances.edit.arg.name", ArgTypes.STRING)
      .addValidator(new InstanceValidator());

   public InstanceEditLoadCommand() {
      super("load", "server.commands.instances.edit.load.desc");
   }

   @Nonnull
   @Override
   public CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (AssetModule.get().getBaseAssetPack().isImmutable()) {
         context.sendMessage(Message.translation("server.commands.instances.edit.assetsImmutable"));
         return CompletableFuture.completedFuture(null);
      } else {
         String name = this.instanceNameArg.get(context);
         context.sendMessage(Message.translation("server.commands.instances.beginLoading").param("name", name));
         InstancesPlugin.get();
         return InstancesPlugin.loadInstanceAssetForEdit(name).thenAccept(world -> {
            context.sendMessage(Message.translation("server.commands.instances.doneLoading").param("world", world.getName()));
            if (context.isPlayer()) {
               Ref<EntityStore> ref = context.senderAsPlayerRef();
               if (ref == null || !ref.isValid()) {
                  return;
               }

               Store<EntityStore> playerStore = ref.getStore();
               World playerWorld = playerStore.getExternalData().getWorld();
               playerWorld.execute(() -> {
                  Transform spawnTransform = world.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, playerStore);
                  Teleport teleportComponent = Teleport.createForPlayer(world, spawnTransform);
                  playerStore.addComponent(ref, Teleport.getComponentType(), teleportComponent);
               });
            }
         });
      }
   }
}
