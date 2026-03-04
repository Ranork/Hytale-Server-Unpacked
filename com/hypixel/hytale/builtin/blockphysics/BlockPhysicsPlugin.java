package com.hypixel.hytale.builtin.blockphysics;

import com.hypixel.hytale.common.util.FormatUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.ValidationOption;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class BlockPhysicsPlugin extends JavaPlugin {
   public BlockPhysicsPlugin(@Nonnull JavaPluginInit init) {
      super(init);
   }

   @Override
   protected void setup() {
      this.getEventRegistry().register(LoadAssetEvent.class, BlockPhysicsPlugin::validatePrefabs);
      this.getChunkStoreRegistry().registerSystem(new BlockPhysicsSystems.Ticking());
   }

   public static void validatePrefabs(@Nonnull LoadAssetEvent event) {
      if (Options.getOptionSet().has(Options.VALIDATE_PREFABS) && !event.isShouldShutdown()) {
         long start = System.nanoTime();
         List<ValidationOption> validatePrefabs = Options.getOptionSet().valuesOf(Options.VALIDATE_PREFABS);
         List<String> failedToValidatePrefabs = PrefabBufferValidator.validateAllPrefabs(validatePrefabs);
         if (!failedToValidatePrefabs.isEmpty()) {
            HytaleLogger.getLogger().at(Level.SEVERE).log("One or more prefabs failed to validate, Exiting!\n" + String.join("\n", failedToValidatePrefabs));
            event.failed(true, "failed to validate prefabs");
         }

         HytaleLogger.getLogger()
            .at(Level.INFO)
            .log(
               "Validate prefabs phase completed! Boot time %s, Took %s",
               FormatUtil.nanosToString(System.nanoTime() - event.getBootStart()),
               FormatUtil.nanosToString(System.nanoTime() - start)
            );
      }
   }
}
