package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class InstanceEditNewCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_SERVER_COMMANDS_INSTANCES_EDIT_ASSETS_IMMUTABLE = Message.translation("server.commands.instances.edit.assetsImmutable");
   @Nonnull
   private final RequiredArg<String> instanceNameArg = this.withRequiredArg("instanceName", "server.commands.instances.edit.arg.name", ArgTypes.STRING);
   @Nonnull
   private final OptionalArg<String> packName = this.withOptionalArg("pack", "server.commands.instances.edit.arg.packName", ArgTypes.STRING);

   public InstanceEditNewCommand() {
      super("new", "server.commands.instances.edit.new.desc");
   }

   @Nonnull
   @Override
   public CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (AssetModule.get().getBaseAssetPack().isImmutable()) {
         context.sendMessage(MESSAGE_SERVER_COMMANDS_INSTANCES_EDIT_ASSETS_IMMUTABLE);
         return CompletableFuture.completedFuture(null);
      } else {
         String packId = this.packName.get(context);
         AssetPack pack;
         if (packId != null) {
            pack = AssetModule.get().getAssetPack(packId);
            if (pack == null) {
               throw new IllegalArgumentException("Unknown asset pack: " + packId);
            }
         } else {
            pack = AssetModule.get().getBaseAssetPack();
         }

         Path path = PathUtil.resolveName(pack.getRoot().resolve("Server").resolve("Instances"), this.instanceNameArg.get(context));
         if (path == null) {
            context.sendMessage(Message.translation("server.commands.instances.edit.new.invalidPath"));
            return CompletableFuture.completedFuture(null);
         } else {
            try {
               Files.createDirectories(path);
            } catch (IOException var6) {
               context.sendMessage(Message.translation("server.commands.instances.createDirectory.failed").param("errormsg", var6.getMessage()));
               return CompletableFuture.completedFuture(null);
            }

            return WorldConfig.save(path.resolve("instance.bson"), new WorldConfig())
               .thenRun(
                  () -> context.sendMessage(
                     Message.translation("server.commands.instances.createdInstanceAssetConfig").param("name", path.getFileName().toString())
                  )
               );
         }
      }
   }
}
