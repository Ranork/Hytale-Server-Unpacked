package com.hypixel.hytale.server.core;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.common.CommonAssetModule;
import com.hypixel.hytale.server.core.blocktype.BlockTypeModule;
import com.hypixel.hytale.server.core.console.ConsoleModule;
import com.hypixel.hytale.server.core.cosmetics.CosmeticsModule;
import com.hypixel.hytale.server.core.io.ServerManager;
import com.hypixel.hytale.server.core.modules.LegacyModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.anchoraction.AnchorActionModule;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.blockhealth.BlockHealthModule;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.camera.FlyCameraModule;
import com.hypixel.hytale.server.core.modules.collision.CollisionModule;
import com.hypixel.hytale.server.core.modules.debug.DebugPlugin;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageModule;
import com.hypixel.hytale.server.core.modules.entity.stamina.StaminaModule;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatsModule;
import com.hypixel.hytale.server.core.modules.entityui.EntityUIModule;
import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.migrations.MigrationModule;
import com.hypixel.hytale.server.core.modules.prefabspawner.PrefabSpawnerModule;
import com.hypixel.hytale.server.core.modules.projectile.ProjectileModule;
import com.hypixel.hytale.server.core.modules.serverplayerlist.ServerPlayerListModule;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.modules.splitvelocity.SplitVelocity;
import com.hypixel.hytale.server.core.modules.time.TimeModule;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.connectedblocks.ConnectedBlocksModule;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.update.UpdateModule;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.annotation.Nonnull;
import joptsimple.OptionSet;

public final class Constants {
   private static final OptionSet OPTION_SET = Options.getOptionSet();
   public static final boolean DEBUG = true;
   public static final boolean SINGLEPLAYER = OPTION_SET.has(Options.SINGLEPLAYER);
   public static final boolean ALLOWS_SELF_OP_COMMAND = OPTION_SET.has(Options.ALLOW_SELF_OP_COMMAND);
   public static final boolean FRESH_UNIVERSE = checkFreshUniverse();
   public static final boolean FORCE_NETWORK_FLUSH = (Boolean)OPTION_SET.valueOf(Options.FORCE_NETWORK_FLUSH);
   public static final Path UNIVERSE_PATH = getUniversePath();
   @Nonnull
   public static final PluginManifest[] CORE_PLUGINS = new PluginManifest[]{
      ConsoleModule.MANIFEST,
      PermissionsModule.MANIFEST,
      UpdateModule.MANIFEST,
      FlyCameraModule.MANIFEST,
      AssetModule.MANIFEST,
      CommonAssetModule.MANIFEST,
      CosmeticsModule.MANIFEST,
      ServerManager.MANIFEST,
      I18nModule.MANIFEST,
      ItemModule.MANIFEST,
      BlockTypeModule.MANIFEST,
      LegacyModule.MANIFEST,
      BlockModule.MANIFEST,
      BlockStateModule.MANIFEST,
      CollisionModule.MANIFEST,
      BlockSetModule.MANIFEST,
      MigrationModule.MANIFEST,
      BlockHealthModule.MANIFEST,
      PrefabSpawnerModule.MANIFEST,
      TimeModule.MANIFEST,
      AnchorActionModule.MANIFEST,
      InteractionModule.MANIFEST,
      EntityModule.MANIFEST,
      EntityStatsModule.MANIFEST,
      EntityUIModule.MANIFEST,
      DamageModule.MANIFEST,
      SplitVelocity.MANIFEST,
      StaminaModule.MANIFEST,
      DebugPlugin.MANIFEST,
      ProjectileModule.MANIFEST,
      ServerPlayerListModule.MANIFEST,
      AccessControlModule.MANIFEST,
      SingleplayerModule.MANIFEST,
      Universe.MANIFEST,
      ConnectedBlocksModule.MANIFEST
   };

   public static void init() {
   }

   private static boolean checkFreshUniverse() {
      Path universePath = getUniversePath();
      if (!Files.exists(universePath)) {
         return true;
      } else if (!Files.exists(universePath.resolve("players"))) {
         return true;
      } else {
         Path worlds = universePath.resolve("worlds");
         return !Files.exists(worlds);
      }
   }

   private static Path getUniversePath() {
      return OPTION_SET.has(Options.UNIVERSE) ? (Path)OPTION_SET.valueOf(Options.UNIVERSE) : Path.of("universe");
   }

   public static boolean shouldSkipModValidation() {
      return OPTION_SET.has(Options.SKIP_MOD_VALIDATION) || HytaleServer.get().getConfig().shouldSkipModValidation();
   }
}
