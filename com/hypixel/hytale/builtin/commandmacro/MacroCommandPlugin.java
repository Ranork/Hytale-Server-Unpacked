package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public class MacroCommandPlugin extends JavaPlugin {
   private static MacroCommandPlugin instance;
   @Nonnull
   private final Map<String, CommandRegistration> rootRegistrations = new Object2ObjectOpenHashMap();

   public static MacroCommandPlugin get() {
      return instance;
   }

   public MacroCommandPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      instance = this;
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        MacroCommandBuilder.class, new DefaultAssetMap()
                     )
                     .setPath("MacroCommands"))
                  .setKeyFunction(MacroCommandBuilder::getId))
               .setCodec(MacroCommandBuilder.CODEC))
            .build()
      );
      this.getEventRegistry().register(LoadedAssetsEvent.class, MacroCommandBuilder.class, this::loadCommandMacroAsset);
      this.getCommandRegistry().registerCommand(new WaitCommand());
      this.getCommandRegistry().registerCommand(new EchoCommand());
   }

   public void loadCommandMacroAsset(@Nonnull LoadedAssetsEvent<String, MacroCommandBuilder, DefaultAssetMap<String, MacroCommandBuilder>> event) {
      Collection<MacroCommandBuilder> allMacros = ((DefaultAssetMap)event.getAssetMap()).getAssetMap().values();
      Set<String> affectedRoots = new HashSet<>();

      for (MacroCommandBuilder changed : event.getLoadedAssets().values()) {
         String rootToken = MacroCommandTreeBuilder.rootTokenOf(changed);
         if (rootToken != null) {
            affectedRoots.add(rootToken);
         }
      }

      Set<String> currentRoots = new HashSet<>();

      for (MacroCommandBuilder macro : allMacros) {
         String rootToken = MacroCommandTreeBuilder.rootTokenOf(macro);
         if (rootToken != null) {
            currentRoots.add(rootToken);
         }
      }

      for (String existing : this.rootRegistrations.keySet()) {
         if (!currentRoots.contains(existing)) {
            affectedRoots.add(existing);
         }
      }

      for (String root : affectedRoots) {
         this.rebuildRoot(root, allMacros);
      }
   }

   private void rebuildRoot(@Nonnull String rootToken, @Nonnull Collection<MacroCommandBuilder> allMacros) {
      CommandRegistration existing = this.rootRegistrations.remove(rootToken);
      if (existing != null) {
         existing.unregister();
      }

      AbstractCommand rootCommand = MacroCommandTreeBuilder.build(rootToken, allMacros);
      if (rootCommand != null) {
         CommandRegistration registration = this.getCommandRegistry().registerCommand(rootCommand);
         if (registration != null) {
            this.rootRegistrations.put(rootToken, registration);
         }
      }
   }
}
