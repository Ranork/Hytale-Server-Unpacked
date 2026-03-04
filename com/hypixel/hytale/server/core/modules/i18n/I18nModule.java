package com.hypixel.hytale.server.core.modules.i18n;

import com.hypixel.hytale.assetstore.AssetPack;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.protocol.UpdateType;
import com.hypixel.hytale.protocol.packets.assets.UpdateTranslations;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.HytaleServerConfig;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.AssetPackRegisterEvent;
import com.hypixel.hytale.server.core.asset.AssetPackUnregisterEvent;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitor;
import com.hypixel.hytale.server.core.asset.monitor.AssetMonitorHandler;
import com.hypixel.hytale.server.core.asset.monitor.EventKind;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.Bench;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.bench.CraftingBench;
import com.hypixel.hytale.server.core.asset.type.item.config.FieldcraftCategory;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.i18n.commands.EnableTmpTagsCommand;
import com.hypixel.hytale.server.core.modules.i18n.commands.InternationalizationCommands;
import com.hypixel.hytale.server.core.modules.i18n.event.MessagesUpdated;
import com.hypixel.hytale.server.core.modules.i18n.parser.LangFileParser;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class I18nModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(I18nModule.class).depends(AssetModule.class).build();
   public static final String DEFAULT_LANGUAGE = "en-US";
   public static final Path FALLBACK_LANG_PATH = Paths.get("fallback.lang");
   public static final String FILE_EXTENSION = ".lang";
   public static final String SERVER_ASSETS = "Server";
   public static final String LANGUAGE_ASSETS = "Languages";
   public static final Path DEFAULT_GENERATED_PATH = Path.of("Server", "Languages", "en-US");
   private static I18nModule instance;
   private final Map<String, String> fallbacks = new ConcurrentHashMap<>();
   private final Map<String, Map<String, String>> languages = new ConcurrentHashMap<>();
   private final Map<String, Map<String, String>> cachedLanguages = new ConcurrentHashMap<>();

   public static I18nModule get() {
      return instance;
   }

   public I18nModule(@Nonnull JavaPluginInit parent) {
      super(parent);
      instance = this;
   }

   @Override
   protected void setup() {
      this.getEventRegistry().register(LoadAssetEvent.class, event -> {
         for (AssetPack pack : AssetModule.get().getAssetPacks()) {
            this.loadMessagesFromPack(pack);
         }
      });
      this.getEventRegistry().register(AssetPackRegisterEvent.class, event -> this.loadMessagesFromPack(event.getAssetPack()));
      this.getEventRegistry().register(AssetPackUnregisterEvent.class, event -> {});
      this.getEventRegistry()
         .register(
            LoadedAssetsEvent.class,
            BlockType.class,
            event -> {
               Map<String, String> addedMessages = new Object2ObjectOpenHashMap();
               event.getLoadedAssets()
                  .values()
                  .forEach(
                     item -> {
                        Bench bench = item.getBench();
                        if (bench != null) {
                           String id = item.getId();
                           if (bench instanceof CraftingBench craftingBench) {
                              for (CraftingBench.BenchCategory category : craftingBench.getCategories()) {
                                 addedMessages.put("server.items." + id + ".bench.categories." + category.getId() + ".name", category.getName());
                                 if (category.getItemCategories() != null) {
                                    for (CraftingBench.BenchItemCategory itemCategory : category.getItemCategories()) {
                                       addedMessages.put(
                                          "server.items." + id + ".bench.categories." + category.getId() + ".itemCategories." + itemCategory.getId() + ".name",
                                          itemCategory.getName()
                                       );
                                    }
                                 }
                              }
                           }

                           if (bench.getDescriptiveLabel() != null) {
                              addedMessages.put("server.items." + id + ".bench.descriptiveLabel", bench.getDescriptiveLabel());
                           }
                        }
                     }
                  );
               this.addDefaultMessages(addedMessages, event.isInitial());
            }
         );
      this.getEventRegistry().register(LoadedAssetsEvent.class, FieldcraftCategory.class, event -> {
         Map<String, String> addedMessages = new Object2ObjectOpenHashMap();
         event.getLoadedAssets().values().forEach(category -> {
            if (category.getName() != null) {
               addedMessages.put("fieldcraftCategories." + category.getId() + ".name", category.getName());
            }
         });
         this.addDefaultMessages(addedMessages, event.isInitial());
      });
   }

   @Override
   protected void start() {
      this.getCommandRegistry().registerCommand(new InternationalizationCommands());
      this.getCommandRegistry().registerCommand(new EnableTmpTagsCommand());
   }

   private void loadMessagesFromPack(AssetPack pack) {
      Path languagesPath = pack.getRoot().resolve("Server").resolve("Languages");
      if (Files.isDirectory(languagesPath)) {
         AssetMonitor assetMonitor = AssetModule.get().getAssetMonitor();
         if (assetMonitor != null && !pack.isImmutable()) {
            assetMonitor.monitorDirectoryFiles(languagesPath, new I18nModule.I18nAssetMonitorHandler(languagesPath));
         }

         try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(languagesPath, x$0 -> Files.isDirectory(x$0))) {
            for (Path path : directoryStream) {
               try {
                  String languageKey = path.getFileName().toString();
                  int entriesCount = this.loadMessages(languageKey, path);
                  this.getLogger().at(Level.INFO).log("Loaded %d entries for '%s' from %s", entriesCount, languageKey, languagesPath);
               } catch (IOException var10) {
                  ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var10)).log("Failed to load messages from: %s", path);
               }
            }
         } catch (IOException var13) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var13)).log("Failed to load languages from: %s", languagesPath);
         }
      }

      Path fallbackPath = languagesPath.resolve("fallback.lang");
      if (Files.exists(fallbackPath)) {
         Properties properties = new Properties();

         try {
            properties.load(Files.newInputStream(fallbackPath));

            for (Entry<Object, Object> entry : properties.entrySet()) {
               this.fallbacks.put((String)entry.getKey(), (String)entry.getValue());
            }

            if (!properties.isEmpty()) {
               this.getLogger().at(Level.INFO).log("Loaded %d entries from %s", properties.size(), fallbackPath);
            }
         } catch (IOException var11) {
            ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(var11)).log("Failed to load fallback languages from: %s", fallbackPath);
         }
      }
   }

   @Nonnull
   public UpdateTranslations[] getUpdatePacketsForChanges(
      String languageKey, @Nonnull Map<String, Map<String, String>> changed, @Nonnull Map<String, Map<String, String>> removed
   ) {
      Map<String, String> removedMessages = this.getMessages(removed, languageKey);
      Map<String, String> changedMessages = this.getMessages(changed, languageKey);
      int size = (removedMessages.isEmpty() ? 0 : 1) + (changedMessages.isEmpty() ? 0 : 1);
      UpdateTranslations[] packets = new UpdateTranslations[size];
      int index = 0;
      if (!removedMessages.isEmpty()) {
         packets[index++] = new UpdateTranslations(UpdateType.Remove, removedMessages);
      }

      if (!changedMessages.isEmpty()) {
         packets[index] = new UpdateTranslations(UpdateType.AddOrUpdate, changedMessages);
      }

      return packets;
   }

   private void addDefaultMessages(@Nonnull Map<String, String> messages, boolean isInitial) {
      Map<String, String> languageMap = this.languages.computeIfAbsent("en-US", k -> new ConcurrentHashMap<>());

      for (Entry<String, String> entry : messages.entrySet()) {
         if (entry.getKey() != null && entry.getValue() != null) {
            languageMap.put(entry.getKey(), entry.getValue());
         } else {
            this.getLogger().at(Level.WARNING).log("Attempted to add invalid default translation message: %s=%s", entry.getKey(), entry.getValue());
         }
      }

      if (!isInitial) {
         UpdateTranslations packet = new UpdateTranslations(UpdateType.AddOrUpdate, messages);
         Universe.get().broadcastPacketNoCache(packet);
         IEventDispatcher<MessagesUpdated, MessagesUpdated> dispatch = HytaleServer.get().getEventBus().dispatchFor(MessagesUpdated.class);
         if (dispatch.hasListener()) {
            Object2ObjectOpenHashMap<String, Map<String, String>> languageMapping = new Object2ObjectOpenHashMap();
            languageMapping.put("en-US", messages);
            dispatch.dispatch(new MessagesUpdated(languageMapping, new Object2ObjectOpenHashMap()));
         }
      }
   }

   private int loadMessages(String languageKey, @Nonnull Path languagePath) throws IOException {
      Map<String, String> messages = this.languages.computeIfAbsent(languageKey, k -> new ConcurrentHashMap<>());

      int var5;
      try (Stream<Path> stream = Files.find(
            languagePath,
            Integer.MAX_VALUE,
            (path, attr) -> path.toString().endsWith(".lang") && attr.isRegularFile(),
            FileUtil.DEFAULT_WALK_TREE_OPTIONS_ARRAY
         )) {
         var5 = stream.mapToInt(path -> {
            String prefix = this.getPrefix(languagePath, path);
            return this.loadMessagesFrom(messages, prefix, path);
         }).sum();
      }

      return var5;
   }

   private int loadMessagesFrom(@Nonnull Map<String, String> messages, String prefix, @Nonnull Path path) {
      Map<String, String> properties;
      try (BufferedReader inputStream = Files.newBufferedReader(path)) {
         properties = LangFileParser.parse(inputStream);
      } catch (Exception var12) {
         ((HytaleLogger.Api)this.getLogger().at(Level.SEVERE).withCause(new SkipSentryException(var12)))
            .log("Error parsing language file: %s", path.toString());
         return 0;
      }

      for (Entry<String, String> entry : properties.entrySet()) {
         String key = prefix + "." + entry.getKey();
         String value = entry.getValue();
         String prev = messages.get(key);
         if (prev != null) {
            if (!prev.equals(value)) {
               this.getLogger().at(Level.WARNING).log("'%s' has multiple definitions: `%s` and `%s`", key, prev, value);
            }
         } else {
            messages.put(key, value);
         }
      }

      return properties.size();
   }

   @Nonnull
   private String getPrefix(@Nonnull Path languagePath, @Nonnull Path path) {
      String prefix = "";
      Path directory = path.getParent();
      if (!languagePath.equals(directory)) {
         Path relativePath = languagePath.relativize(directory);
         prefix = prefix + relativePath.toString().replace(File.separatorChar, '.') + ".";
      }

      String name = path.getFileName().toString();
      return prefix + name.substring(0, name.length() - ".lang".length());
   }

   @Nonnull
   public Map<String, String> getMessages(String language) {
      return this.cachedLanguages.computeIfAbsent(language, key -> Collections.unmodifiableMap(this.getMessages(this.languages, key)));
   }

   public Map<String, String> getMessages(@Nonnull Map<String, Map<String, String>> languageMap, @Nullable String language) {
      if (language == null) {
         return this.getMessages(languageMap, "en-US");
      } else {
         Map<String, String> messages = languageMap.get(language);
         if ("en-US".equals(language)) {
            return messages != null ? messages : Collections.emptyMap();
         } else {
            String fallback = this.fallbacks.getOrDefault(language, "en-US");
            Map<String, String> fallbackMessages = this.getMessages(languageMap, fallback);
            if (fallbackMessages == null) {
               return messages != null ? messages : Collections.emptyMap();
            } else if (messages == null) {
               return fallbackMessages;
            } else {
               Object2ObjectOpenHashMap<String, String> map = new Object2ObjectOpenHashMap(fallbackMessages);
               map.putAll(messages);
               return map;
            }
         }
      }
   }

   public void sendTranslations(@Nonnull PacketHandler packetHandler, String language) {
      if (!this.isDisabled()) {
         packetHandler.writeNoCache(new UpdateTranslations(UpdateType.Init, this.getMessages(language)));
      }
   }

   @Nullable
   public String getMessage(String language, @Nonnull String key) {
      HytaleServerConfig config = HytaleServer.get().getConfig();
      if (config != null && config.isDisplayTmpTagsInStrings()) {
         return this.getMessages(language).get(key);
      } else {
         String translatedString = this.getMessages(language).get(key);
         return translatedString != null ? translatedString.replace("[TMP] ", "").replace("[TMP]", "") : null;
      }
   }

   private class I18nAssetMonitorHandler implements AssetMonitorHandler {
      private final Path languagesPath;

      public I18nAssetMonitorHandler(Path languagesPath) {
         this.languagesPath = languagesPath;
      }

      @Override
      public Object getKey() {
         return I18nModule.this;
      }

      public boolean test(Path path, EventKind eventKind) {
         return !Files.isDirectory(path) && path.getFileName().toString().endsWith(".lang") && I18nModule.this.isEnabled();
      }

      public void accept(Map<Path, EventKind> map) {
         Map<String, Map<String, String>> removed = new Object2ObjectOpenHashMap();
         Map<String, Map<String, String>> changed = new Object2ObjectOpenHashMap();

         for (Entry<Path, EventKind> entry : map.entrySet()) {
            Path path = entry.getKey();
            EventKind eventKind = entry.getValue();
            Path normalized = path.toAbsolutePath().normalize();
            Path relativized = this.languagesPath.relativize(normalized);
            if (I18nModule.FALLBACK_LANG_PATH.equals(relativized)) {
               Properties properties = new Properties();

               try {
                  properties.load(Files.newInputStream(path));
               } catch (IOException var18) {
                  ((HytaleLogger.Api)I18nModule.this.getLogger().at(Level.SEVERE).withCause(var18)).log("Failed to load fallback languages from: %s", path);
                  continue;
               }

               I18nModule.this.fallbacks.clear();
               properties.forEach((keyx, value) -> I18nModule.this.fallbacks.put((String)keyx, value));
            } else {
               String languageKey = relativized.getName(0).toString();
               Path langPath = this.languagesPath.resolve(languageKey).toAbsolutePath().normalize();
               String prefix = I18nModule.this.getPrefix(langPath, normalized);
               switch (eventKind) {
                  case ENTRY_MODIFY:
                  case ENTRY_DELETE:
                     String prefixWithDot = prefix + ".";
                     Map<String, String> removedMessages = removed.computeIfAbsent(languageKey, k -> new Object2ObjectOpenHashMap());
                     Map<String, String> messages = I18nModule.this.languages.computeIfAbsent(languageKey, k -> new ConcurrentHashMap<>());
                     Iterator<String> iterator = messages.keySet().iterator();

                     while (iterator.hasNext()) {
                        String key = iterator.next();
                        if (key.startsWith(prefixWithDot)) {
                           removedMessages.put(key, "");
                           iterator.remove();
                        }
                     }

                     if (eventKind == EventKind.ENTRY_DELETE) {
                        break;
                     }
                  case ENTRY_CREATE:
                     Map<String, String> changedMessages = changed.computeIfAbsent(languageKey, k -> new Object2ObjectOpenHashMap());
                     I18nModule.this.loadMessagesFrom(changedMessages, prefix, path);
               }
            }
         }

         if (!removed.isEmpty() || !changed.isEmpty()) {
            for (Entry<String, Map<String, String>> changedLang : changed.entrySet()) {
               Map<String, String> messages = I18nModule.this.languages.computeIfAbsent(changedLang.getKey(), k -> new ConcurrentHashMap<>());
               messages.putAll(changedLang.getValue());
               I18nModule.this.cachedLanguages.remove(changedLang.getKey());
            }

            for (Entry<String, Map<String, String>> removedLang : removed.entrySet()) {
               I18nModule.this.cachedLanguages.remove(removedLang.getKey());
               Map<String, String> orig = I18nModule.this.getMessages(removedLang.getKey());
               Map<String, String> changedMessages = changed.computeIfAbsent(removedLang.getKey(), k -> new Object2ObjectOpenHashMap());
               Iterator<String> iterator = removedLang.getValue().keySet().iterator();

               while (iterator.hasNext()) {
                  String removedKey = iterator.next();
                  if (changedMessages.containsKey(removedKey)) {
                     iterator.remove();
                  } else {
                     String fallback = orig.get(removedKey);
                     if (fallback != null) {
                        iterator.remove();
                        changedMessages.put(removedKey, fallback);
                     }
                  }
               }
            }

            List<PlayerRef> players = Universe.get().getPlayers();
            Map<String, UpdateTranslations[]> updatePackets = new Object2ObjectOpenHashMap();

            for (PlayerRef playerRef : players) {
               PacketHandler handler = playerRef.getPacketHandler();
               String languageKey = playerRef.getLanguage();
               UpdateTranslations[] packets = updatePackets.get(languageKey);
               if (packets == null) {
                  packets = I18nModule.this.getUpdatePacketsForChanges(languageKey, changed, removed);
                  updatePackets.put(languageKey, packets);
               }

               if (packets.length != 0) {
                  handler.write(packets);
               }
            }

            IEventDispatcher<MessagesUpdated, MessagesUpdated> dispatch = HytaleServer.get().getEventBus().dispatchFor(MessagesUpdated.class);
            if (dispatch.hasListener()) {
               dispatch.dispatch(new MessagesUpdated(changed, removed));
            }

            I18nModule.this.getLogger().at(Level.INFO).log("Handled language changes for: %s", changed.keySet());
         }
      }
   }
}
