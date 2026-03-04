package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import java.util.Objects;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldAddCommand extends CommandBase {
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.addworld.arg.name.desc", ArgTypes.STRING);
   @Nonnull
   private final DefaultArg<String> genArg = this.withDefaultArg("gen", "server.commands.addworld.arg.gen.desc", ArgTypes.STRING, "default", "");
   @Nonnull
   private final DefaultArg<String> storageArg = this.withDefaultArg("storage", "server.commands.addworld.arg.gen.desc", ArgTypes.STRING, "default", "");

   public WorldAddCommand() {
      super("add", "server.commands.addworld.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      CommandSender sender = context.sender();
      String name = context.get(this.nameArg);
      if (Universe.get().getWorld(name) != null) {
         sender.sendMessage(Message.translation("server.universe.addWorld.alreadyExists").param("worldName", name));
      } else if (Universe.get().isWorldLoadable(name)) {
         sender.sendMessage(Message.translation("server.universe.addWorld.alreadyExistsDisk").param("worldName", name));
      } else {
         String generatorType = context.get(this.genArg);
         String chunkStorageType = context.get(this.storageArg);
         if (generatorType != null && !"default".equals(generatorType)) {
            BuilderCodec<? extends IWorldGenProvider> providerCodec = IWorldGenProvider.CODEC.getCodecFor(generatorType);
            if (providerCodec == null) {
               throw new IllegalArgumentException("Unknown generatorType '" + generatorType + "'");
            }
         }

         CompletableFutureUtil._catch(
            Universe.get()
               .addWorld(name, generatorType, chunkStorageType)
               .thenRun(
                  () -> sender.sendMessage(
                     Message.translation("server.universe.addWorld.worldCreated")
                        .param("worldName", name)
                        .param("generator", Objects.requireNonNullElse(generatorType, "default"))
                        .param("storage", Objects.requireNonNullElse(chunkStorageType, "default"))
                  )
               )
               .exceptionally(
                  throwable -> {
                     ((HytaleLogger.Api)LOGGER.at(Level.SEVERE).withCause(throwable)).log("Failed to add world '%s'", name);
                     sender.sendMessage(
                        Message.translation("server.universe.addWorld.failed")
                           .param("worldName", name)
                           .param("error", throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage())
                     );
                     return null;
                  }
               )
         );
      }
   }
}
