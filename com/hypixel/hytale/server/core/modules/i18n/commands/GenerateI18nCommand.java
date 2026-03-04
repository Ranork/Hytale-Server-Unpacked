package com.hypixel.hytale.server.core.modules.i18n.commands;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.modules.i18n.event.GenerateDefaultLanguageEvent;
import com.hypixel.hytale.server.core.modules.i18n.generator.TranslationMap;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class GenerateI18nCommand extends AbstractAsyncCommand {
   public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   protected final FlagArg cleanArg = this.withFlagArg("clean", "server.commands.i18n.gen.clean.desc");

   public GenerateI18nCommand() {
      super("gen", "server.commands.i18n.gen.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      CommandSender commandSender = context.sender();
      AssetPack baseAssetPack = AssetModule.get().getBaseAssetPack();
      if (baseAssetPack.isImmutable()) {
         commandSender.sendMessage(Message.translation("server.commands.i18n.gen.immutable"));
         return CompletableFuture.completedFuture(null);
      } else {
         Path baseAssetPackRoot = baseAssetPack.getRoot();
         boolean cleanOldKeys = this.cleanArg.get(context);
         ConcurrentHashMap<String, TranslationMap> translationFiles = new ConcurrentHashMap<>();
         HytaleServer.get()
            .getEventBus()
            .<Void, GenerateDefaultLanguageEvent>dispatchFor(GenerateDefaultLanguageEvent.class)
            .dispatch(new GenerateDefaultLanguageEvent(translationFiles));
         return CompletableFuture.runAsync(() -> {
            try {
               for (Entry<String, TranslationMap> entry : translationFiles.entrySet()) {
                  String filename = entry.getKey();
                  TranslationMap generatedMap = entry.getValue();
                  Path path = baseAssetPackRoot.resolve(I18nModule.DEFAULT_GENERATED_PATH).resolve(filename + ".lang");
                  TranslationMap mergedMap = this.mergei18nWithOnDisk(path, generatedMap, cleanOldKeys);
                  mergedMap.sortByKeyBeforeFirstDot();
                  this.writeTranslationMap(path, mergedMap);
                  LOGGER.at(Level.INFO).log("Wrote %s translation(s) to %s", mergedMap.size(), path.toAbsolutePath());
               }

               LOGGER.at(Level.INFO).log("Wrote %s generated translation file(s)", translationFiles.size());
               commandSender.sendMessage(Message.translation(cleanOldKeys ? "server.commands.i18n.gen.cleaned" : "server.commands.i18n.gen.done"));
            } catch (Throwable var11) {
               throw new RuntimeException("Error writing generated translation file(s)", var11);
            }
         });
      }
   }

   @Nonnull
   private TranslationMap mergei18nWithOnDisk(@Nonnull Path path, @Nonnull TranslationMap generated, boolean cleanOldKeys) throws Exception {
      TranslationMap mergedMap = new TranslationMap();
      if (Files.exists(path)) {
         Properties diskAsProperties = new Properties();
         diskAsProperties.load(new FileInputStream(path.toFile()));
         TranslationMap diskTranslationMap = new TranslationMap(diskAsProperties);
         if (cleanOldKeys) {
            Set<String> extraneousDiskKeys = difference(diskTranslationMap.asMap().keySet(), generated.asMap().keySet());
            diskTranslationMap.removeKeys(extraneousDiskKeys);
         }

         mergedMap.putAbsentKeys(diskTranslationMap);
      }

      mergedMap.putAbsentKeys(generated);
      return mergedMap;
   }

   private void writeTranslationMap(@Nonnull Path path, @Nonnull TranslationMap translationMap) throws Exception {
      Files.createDirectories(path.getParent());
      Map<String, String> map = translationMap.asMap();

      try (BufferedWriter writer = Files.newBufferedWriter(path)) {
         for (Entry<String, String> e : map.entrySet()) {
            String k = e.getKey();
            String v = e.getValue();
            writer.write(k);
            writer.write(" = ");
            writer.write(v);
            writer.write(System.lineSeparator());
         }
      }
   }

   @Nonnull
   private static <T> Set<T> difference(@Nonnull Set<T> a, @Nonnull Set<T> b) {
      Set<T> difference = new HashSet<>(a);
      difference.removeAll(b);
      return difference;
   }
}
