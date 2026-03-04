package com.hypixel.hytale.server.core.command.commands.debug;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.common.CommonAsset;
import com.hypixel.hytale.server.core.asset.common.CommonAssetRegistry;
import com.hypixel.hytale.server.core.asset.common.asset.FileCommonAsset;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import com.hypixel.hytale.sneakythrow.SneakyThrow;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class AssetsDuplicatesCommand extends AbstractAsyncCommand {
   @Nonnull
   private final FlagArg reverseFlag = this.withFlagArg("reverse", "server.commands.assets.duplicates.reverse.desc");

   public AssetsDuplicatesCommand() {
      super("duplicates", "server.commands.assets.duplicates.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      boolean reverse = this.reverseFlag.get(context);
      List<CompletableFuture<Void>> futures = new ObjectArrayList();
      List<AssetsDuplicatesCommand.DuplicatedAssetInfo> duplicates = new ObjectArrayList();

      for (Entry<String, List<CommonAssetRegistry.PackAsset>> entry : CommonAssetRegistry.getDuplicatedAssets().entrySet()) {
         AssetsDuplicatesCommand.DuplicatedAssetInfo duplicateInfo = new AssetsDuplicatesCommand.DuplicatedAssetInfo(entry.getKey(), entry.getValue());
         duplicates.add(duplicateInfo);
         futures.add(duplicateInfo.calculateTotalSize());
      }

      return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
         .thenAccept(
            aVoid -> {
               duplicates.sort(
                  reverse ? AssetsDuplicatesCommand.DuplicatedAssetInfo.COMPARATOR_REVERSE : AssetsDuplicatesCommand.DuplicatedAssetInfo.COMPARATOR
               );
               long totalWastedSpace = 0L;

               for (AssetsDuplicatesCommand.DuplicatedAssetInfo duplicateInfox : duplicates) {
                  Message header = Message.translation("server.commands.assets.duplicates.header")
                     .param("hash", duplicateInfox.hash)
                     .param("wastedBytes", FormatUtil.bytesToString(duplicateInfox.wastedSpace));
                  Set<Message> duplicateAssets = duplicateInfox.assets
                     .stream()
                     .map(a -> a.pack() + ":" + a.asset().getName())
                     .map(Message::raw)
                     .collect(Collectors.toSet());
                  context.sendMessage(MessageFormat.list(header, duplicateAssets));
                  totalWastedSpace += duplicateInfox.wastedSpace;
               }

               context.sendMessage(
                  Message.translation("server.commands.assets.duplicates.total").param("wastedBytes", FormatUtil.bytesToString(totalWastedSpace))
               );
            }
         );
   }

   public static class DuplicatedAssetInfo {
      @Nonnull
      public static final Comparator<AssetsDuplicatesCommand.DuplicatedAssetInfo> COMPARATOR = Comparator.comparingLong(o -> o.wastedSpace);
      @Nonnull
      public static final Comparator<AssetsDuplicatesCommand.DuplicatedAssetInfo> COMPARATOR_REVERSE = Collections.reverseOrder(COMPARATOR);
      @Nonnull
      final String hash;
      @Nonnull
      final List<CommonAssetRegistry.PackAsset> assets;
      long wastedSpace;

      public DuplicatedAssetInfo(@Nonnull String hash, @Nonnull List<CommonAssetRegistry.PackAsset> assets) {
         this.hash = hash;
         this.assets = assets;
      }

      @Nonnull
      public CompletableFuture<Void> calculateTotalSize() {
         CommonAsset commonAsset = this.assets.getFirst().asset();
         if (commonAsset instanceof FileCommonAsset fileCommonAsset) {
            Path path = fileCommonAsset.getFile();
            return CompletableFuture.runAsync(SneakyThrow.sneakyRunnable(() -> this.wastedSpace = Files.size(path) * (this.assets.size() - 1)));
         } else {
            return commonAsset.getBlob().thenAccept(bytes -> this.wastedSpace = (long)bytes.length * (this.assets.size() - 1));
         }
      }
   }
}
