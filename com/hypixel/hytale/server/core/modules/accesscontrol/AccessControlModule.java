package com.hypixel.hytale.server.core.modules.accesscontrol;

import com.google.gson.JsonObject;
import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.event.events.player.PlayerSetupConnectEvent;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.Ban;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.BanParser;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.InfiniteBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.TimedBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.commands.BanCommand;
import com.hypixel.hytale.server.core.modules.accesscontrol.commands.UnbanCommand;
import com.hypixel.hytale.server.core.modules.accesscontrol.commands.WhitelistCommand;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.AccessProvider;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleWhitelistProvider;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.annotation.Nonnull;

public class AccessControlModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(AccessControlModule.class).build();
   private static AccessControlModule instance;
   private final HytaleWhitelistProvider whitelistProvider = new HytaleWhitelistProvider();
   private final HytaleBanProvider banProvider = new HytaleBanProvider();
   private final List<AccessProvider> providerRegistry = new CopyOnWriteArrayList<AccessProvider>() {
      {
         this.add(AccessControlModule.this.whitelistProvider);
         this.add(AccessControlModule.this.banProvider);
      }
   };
   private final Map<String, BanParser> parsers = new ConcurrentHashMap<>();

   public static AccessControlModule get() {
      return instance;
   }

   public AccessControlModule(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      this.getCommandRegistry().registerCommand(new BanCommand(this.banProvider));
      this.getCommandRegistry().registerCommand(new UnbanCommand(this.banProvider));
      this.getCommandRegistry().registerCommand(new WhitelistCommand(this.whitelistProvider));
      this.registerBanParser("timed", TimedBan::fromJsonObject);
      this.registerBanParser("infinite", InfiniteBan::fromJsonObject);
      this.getEventRegistry().register(PlayerSetupConnectEvent.class, event -> {
         CompletableFuture<Optional<String>> completableFuture = this.getDisconnectReason(event.getUuid());
         Optional<String> disconnectReason = completableFuture.join();
         if (disconnectReason.isPresent()) {
            event.setReason(disconnectReason.get());
            event.setCancelled(true);
         }
      });
   }

   @Override
   protected void start() {
      this.whitelistProvider.syncLoad();
      this.banProvider.syncLoad();
   }

   @Override
   protected void shutdown() {
      this.whitelistProvider.syncSave();
      this.banProvider.syncSave();
   }

   public void registerBanParser(String type, BanParser banParser) {
      BanParser currentParser = this.parsers.get(type);
      if (currentParser != null) {
         throw new IllegalArgumentException("Type \"" + type + "\" is already registered by " + currentParser.getClass());
      } else {
         this.parsers.put(type, banParser);
      }
   }

   public void registerAccessProvider(AccessProvider provider) {
      this.providerRegistry.add(provider);
   }

   public Ban parseBan(String type, JsonObject object) {
      BanParser parser = this.parsers.get(type);
      if (parser == null) {
         throw new IllegalArgumentException("No BanParser for type: " + type);
      } else {
         return parser.parse(object);
      }
   }

   @Nonnull
   private CompletableFuture<Optional<String>> getDisconnectReason(UUID uuid) {
      return this.providerRegistry
         .stream()
         .map(p -> p.getDisconnectReason(uuid))
         .reduce(CompletableFuture.completedFuture(Optional.empty()), (a, b) -> a.thenCombine(b, (aMessage, bMessage) -> {
            if (aMessage.isPresent()) {
               return (Optional)aMessage;
            } else {
               return (Optional)(bMessage.isPresent() ? bMessage : Optional.empty());
            }
         }));
   }
}
