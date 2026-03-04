package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.prefab.selection.buffer.BsonPrefabBufferDeserializer;
import com.hypixel.hytale.server.core.util.BsonUtil;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nonnull;

public class ValidateCPBCommand extends AbstractAsyncCommand {
   private static final String UNABLE_TO_LOAD_MODEL = "Unable to load entity with model ";
   private static final String FAILED_TO_FIND_BLOCK = "Failed to find block ";
   @Nonnull
   private final OptionalArg<String> pathArg = this.withOptionalArg("path", "server.commands.validatecpb.path.desc", ArgTypes.STRING);

   public ValidateCPBCommand() {
      super("validatecpb", "server.commands.validatecpb.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (this.pathArg.provided(context)) {
         Path assetPath = Path.of(this.pathArg.get(context));
         if (!PathUtil.isInTrustedRoot(assetPath)) {
            context.sendMessage(Message.translation("server.commands.validatecpb.invalidPath"));
            return CompletableFuture.completedFuture(null);
         } else {
            return CompletableFuture.runAsync(() -> convertPrefabs(context, assetPath));
         }
      } else {
         return CompletableFuture.runAsync(() -> {
            for (AssetPack pack : AssetModule.get().getAssetPacks()) {
               convertPrefabs(context, pack.getRoot());
            }
         });
      }
   }

   private static void convertPrefabs(@Nonnull CommandContext context, @Nonnull Path assetPath) {
      List<String> failed = new ObjectArrayList();

      try (Stream<Path> stream = Files.walk(assetPath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY)) {
         CompletableFuture[] futures = stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".prefab.json"))
            .map(path -> BsonUtil.readDocument(path, false).thenAccept(document -> {
               BsonPrefabBufferDeserializer.INSTANCE.deserialize(path, document);
               context.sendMessage(Message.translation("server.general.loadedPrefab").param("name", path.toString()));
            }).exceptionally(throwable -> {
               String message = throwable.getCause().getMessage();
               if (message != null) {
                  if (message.contains("Failed to find block ")) {
                     failed.add("Failed to load " + path + " because " + message);
                     return null;
                  }

                  if (message.contains("Unable to load entity with model ")) {
                     failed.add("Failed to load " + path + " because " + message);
                     return null;
                  }
               }

               failed.add("Failed to load " + path + " because " + (message != null ? message : throwable.getCause().getClass()));
               new Exception("Failed to load " + path, throwable.getCause()).printStackTrace();
               return null;
            }))
            .toArray(CompletableFuture[]::new);
         CompletableFuture.allOf(futures).join();
      } catch (IOException var8) {
         throw SneakyThrow.sneakyThrow(var8);
      }

      if (!failed.isEmpty()) {
         context.sendMessage(Message.translation("server.commands.validatecpb.failed").param("failed", failed.toString()));
      }

      context.sendMessage(Message.translation("server.commands.prefabConvertionDone").param("path", assetPath.toString()));
   }
}
