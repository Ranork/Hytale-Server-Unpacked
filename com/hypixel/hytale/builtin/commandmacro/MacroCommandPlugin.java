package com.hypixel.hytale.builtin.commandmacro;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.command.system.CommandRegistration;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class MacroCommandPlugin extends JavaPlugin {
   private static MacroCommandPlugin instance;
   @Nonnull
   private final Map<String, CommandRegistration> macroCommandRegistrations = new Object2ObjectOpenHashMap();

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
      for (MacroCommandBuilder value : event.getLoadedAssets().values()) {
         if (this.macroCommandRegistrations.containsKey(value.getName())) {
            this.macroCommandRegistrations.get(value.getName()).unregister();
         }

         CommandRegistration commandRegistration = MacroCommandBuilder.createAndRegisterCommand(value);
         if (commandRegistration != null) {
            this.macroCommandRegistrations.put(value.getName(), commandRegistration);
         }
      }
   }
}
