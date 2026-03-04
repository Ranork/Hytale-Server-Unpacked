package com.hypixel.hytale.server.core.universe.world.commands.world;

import com.hypixel.fastutil.util.SneakyThrow;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncWorldCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.provider.RocksDbChunkStorageProvider;
import java.util.concurrent.CompletableFuture;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WorldRocksDbCommand extends AbstractCommandCollection {
   public WorldRocksDbCommand() {
      super("rocksdb", "server.commands.world.rocksdb");
      this.addSubCommand(new WorldRocksDbCommand.CompactCommand());
   }

   public static class CompactCommand extends AbstractAsyncWorldCommand {
      public CompactCommand() {
         super("compact", "server.commands.world.rocksdb.compact");
      }

      @NonNullDecl
      @Override
      protected CompletableFuture<Void> executeAsync(@NonNullDecl CommandContext context, @NonNullDecl World world) {
         if (world.getChunkStore().getStorageData() instanceof RocksDbChunkStorageProvider.RocksDbResource rocksDbResource) {
            context.sendMessage(Message.translation("server.commands.world.rocksdb.compact.start"));
            return CompletableFuture.runAsync(() -> {
               try {
                  rocksDbResource.db.compactRange(rocksDbResource.chunkColumn);
               } catch (Exception var3) {
                  throw SneakyThrow.sneakyThrow(var3);
               }

               context.sendMessage(Message.translation("server.commands.world.rocksdb.compact.end"));
            });
         } else {
            context.sendMessage(Message.translation("server.commands.world.rocksdb.compact.wrong"));
            return CompletableFuture.completedFuture(null);
         }
      }
   }
}
