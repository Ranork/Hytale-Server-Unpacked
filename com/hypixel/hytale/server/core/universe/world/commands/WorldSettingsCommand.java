package com.hypixel.hytale.server.core.universe.world.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box2D;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.storage.provider.IChunkStorageProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.provider.IWorldGenProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.IWorldMapProvider;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class WorldSettingsCommand extends AbstractCommandCollection {
   @Nonnull
   private final WorldConfig defaultWorldConfig = new WorldConfig();

   public WorldSettingsCommand() {
      super("settings", "server.commands.world.settings.desc");
      this.addAliases("ws");
      this.generateSubCommand(
         "worldgentype",
         "server.commands.world.settings.worldgentype.desc",
         "type",
         ArgTypes.STRING,
         "WorldGen Type",
         worldConfig -> IWorldGenProvider.CODEC.getIdFor((Class<? extends IWorldGenProvider>)worldConfig.getWorldGenProvider().getClass()),
         (worldConfig, path) -> worldConfig.setWorldGenProvider(IWorldGenProvider.CODEC.getCodecFor(path).getDefaultValue())
      );
      this.generateSubCommand(
         "worldmaptype",
         "server.commands.world.settings.worldmaptype.desc",
         "type",
         ArgTypes.STRING,
         "WorldMap Type",
         worldConfig -> IWorldMapProvider.CODEC.getIdFor((Class<? extends IWorldMapProvider>)worldConfig.getWorldMapProvider().getClass()),
         (worldConfig, path) -> worldConfig.setWorldMapProvider(IWorldMapProvider.CODEC.getCodecFor(path).getDefaultValue())
      );
      this.generateSubCommand(
         "chunkstoragetype",
         "server.commands.world.settings.chunkstoragetype.desc",
         "type",
         ArgTypes.STRING,
         "ChunkStorage Type",
         worldConfig -> IChunkStorageProvider.CODEC.getIdFor((Class<? extends IChunkStorageProvider<?>>)worldConfig.getChunkStorageProvider().getClass()),
         (worldConfig, path) -> worldConfig.setChunkStorageProvider((IChunkStorageProvider<?>)IChunkStorageProvider.CODEC.getCodecFor(path).getDefaultValue())
      );
      this.generateSubCommand(
         "ticking", "server.commands.world.settings.ticking.desc", "ticking", ArgTypes.BOOLEAN, "Ticking", WorldConfig::isTicking, WorldConfig::setTicking
      );
      this.generateSubCommand(
         "blockticking",
         "server.commands.world.settings.blockticking.desc",
         "blockticking",
         ArgTypes.BOOLEAN,
         "Block Ticking",
         WorldConfig::isBlockTicking,
         WorldConfig::setBlockTicking
      );
      this.generateSubCommand(
         "pvp", "server.commands.world.settings.pvp.desc", "pvp", ArgTypes.BOOLEAN, "PvP", WorldConfig::isPvpEnabled, WorldConfig::setPvpEnabled
      );
      this.generateSubCommand(
         "timepaused",
         "server.commands.world.settings.timepaused.desc",
         "timepaused",
         ArgTypes.BOOLEAN,
         "Time Paused",
         WorldConfig::isGameTimePaused,
         WorldConfig::setGameTimePaused
      );
      this.generateSubCommand(
         "spawningnpc",
         "server.commands.world.settings.spawningnpc.desc",
         "spawning",
         ArgTypes.BOOLEAN,
         "Spawning NPC's",
         WorldConfig::isSpawningNPC,
         WorldConfig::setSpawningNPC
      );
      this.generateSubCommand(
         "spawnmarkers",
         "server.commands.world.settings.spawnmarkers.desc",
         "enabled",
         ArgTypes.BOOLEAN,
         "Spawn markers enabled",
         WorldConfig::isSpawnMarkersEnabled,
         WorldConfig::setIsSpawnMarkersEnabled
      );
      this.generateSubCommand(
         "freezeallnpcs",
         "server.commands.world.settings.freezeallnpcs.desc",
         "enabled",
         ArgTypes.BOOLEAN,
         "NPCs will be frozen",
         WorldConfig::isAllNPCFrozen,
         WorldConfig::setIsAllNPCFrozen
      );
      this.generateSubCommand(
         "compassupdating",
         "server.commands.world.settings.compassupdating.desc",
         "updating",
         ArgTypes.BOOLEAN,
         "Compass Updating",
         World::isCompassUpdating,
         WorldConfig::isCompassUpdating,
         World::setCompassUpdating
      );
      this.generateSubCommand(
         "playersaving",
         "server.commands.world.settings.playersaving.desc",
         "enabled",
         ArgTypes.BOOLEAN,
         "Player Saving Enabled",
         WorldConfig::isSavingPlayers,
         WorldConfig::setSavingPlayers
      );
      this.generateSubCommand(
         "chunksaving",
         "server.commands.world.settings.chunksaving.desc",
         "enabled",
         ArgTypes.BOOLEAN,
         "Chunk Saving Enabled",
         WorldConfig::canSaveChunks,
         WorldConfig::setCanSaveChunks
      );
      this.generateSubCommand(
         "chunkunloading",
         "server.commands.world.settings.chunkunload.desc",
         "enabled",
         ArgTypes.BOOLEAN,
         "Chunk Unloading Enabled",
         WorldConfig::canUnloadChunks,
         WorldConfig::setCanUnloadChunks
      );
      this.generateSubCommand(
         "gamemode",
         "server.commands.world.settings.gamemode.desc",
         "gamemode",
         new EnumArgumentType<>("server.commands.parsing.argtype.gamemode.name", GameMode.class),
         "Default GameMode",
         WorldConfig::getGameMode,
         WorldConfig::setGameMode
      );
      this.generateSubCommand(
         "gameplayconfig",
         "server.commands.world.settings.gameplayconfig.desc",
         "id",
         ArgTypes.STRING,
         "GameplayConfigId",
         WorldConfig::getGameplayConfig,
         WorldConfig::setGameplayConfig
      );
      this.addSubCommand(
         new WorldSettingsCommand.WorldSettingsBox2DCommand(
            "pregenerate",
            "server.commands.world.settings.pregenerate.desc",
            "Pre-generate region",
            w -> w.getWorldConfig().getChunkConfig().getPregenerateRegion(),
            wc -> wc.getChunkConfig().getPregenerateRegion(),
            (w, v) -> {
               WorldConfig worldConfig = w.getWorldConfig();
               worldConfig.getChunkConfig().setPregenerateRegion(v);
               worldConfig.markChanged();
            }
         )
      );
      this.addSubCommand(
         new WorldSettingsCommand.WorldSettingsBox2DCommand(
            "keeploaded",
            "server.commands.world.settings.keeploaded.desc",
            "Keep loaded region",
            w -> w.getWorldConfig().getChunkConfig().getKeepLoadedRegion(),
            wc -> wc.getChunkConfig().getKeepLoadedRegion(),
            (w, v) -> {
               WorldConfig worldConfig = w.getWorldConfig();
               worldConfig.getChunkConfig().setKeepLoadedRegion(v);
               worldConfig.markChanged();
            }
         )
      );
   }

   private <T> void generateSubCommand(
      @Nonnull String command,
      @Nonnull String description,
      @Nonnull String arg,
      @Nonnull ArgumentType<T> argumentType,
      @Nonnull String display,
      @Nonnull Function<WorldConfig, T> getter,
      @Nonnull BiConsumer<WorldConfig, T> setter
   ) {
      this.generateSubCommand(
         command,
         description,
         arg,
         argumentType,
         display,
         world -> getter.apply(world.getWorldConfig()),
         getter,
         (world, v) -> setter.accept(world.getWorldConfig(), v)
      );
   }

   private <T> void generateSubCommand(
      @Nonnull String command,
      @Nonnull String description,
      @Nonnull String arg,
      ArgumentType<T> argumentType,
      @Nonnull String display,
      @Nonnull Function<World, T> getter,
      Function<WorldConfig, T> defaultGetter,
      @Nonnull BiConsumer<World, T> setter
   ) {
      this.addSubCommand(
         new WorldSettingsCommand.WorldSettingsSubCommand<>(
            command, description, arg, argumentType, display, getter, defaultGetter, setter, this.defaultWorldConfig
         )
      );
   }

   private static class WorldSettingsBox2DCommand extends AbstractWorldCommand {
      @Nonnull
      private final String display;
      @Nonnull
      private final Function<World, Box2D> getter;
      @Nonnull
      private final Function<WorldConfig, Box2D> defaultGetter;
      @Nonnull
      private final BiConsumer<World, Box2D> setter;
      @Nonnull
      private final WorldConfig defaultWorldConfig;

      public WorldSettingsBox2DCommand(
         String name,
         String description,
         @Nonnull String display,
         @Nonnull Function<World, Box2D> getter,
         @Nonnull Function<WorldConfig, Box2D> defaultGetter,
         @Nonnull BiConsumer<World, Box2D> setter
      ) {
         super(name, description);
         this.display = display;
         this.getter = getter;
         this.defaultGetter = defaultGetter;
         this.setter = setter;
         this.defaultWorldConfig = new WorldConfig();
         this.addSubCommand(new WorldSettingsCommand.WorldSettingsBox2DCommand.SetSubCommand());
         this.addSubCommand(new WorldSettingsCommand.WorldSettingsBox2DCommand.ResetSubCommand());
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         Box2D currentValue = this.getter.apply(world);
         context.sendMessage(
            Message.translation("server.commands.world.settings.currentValue")
               .param("display", this.display)
               .param("worldName", world.getName())
               .param("currentValue", Objects.toString(currentValue))
         );
      }

      private class ResetSubCommand extends AbstractWorldCommand {
         public ResetSubCommand() {
            super("reset", "server.commands.world.settings.reset.desc");
         }

         @Override
         protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
            Box2D currentValue = WorldSettingsBox2DCommand.this.getter.apply(world);
            Box2D newValue = WorldSettingsBox2DCommand.this.defaultGetter.apply(WorldSettingsBox2DCommand.this.defaultWorldConfig);
            WorldSettingsBox2DCommand.this.setter.accept(world, newValue);
            world.getWorldConfig().markChanged();
            context.sendMessage(
               Message.translation("server.commands.world.settings.displaySetDefault")
                  .param("display", WorldSettingsBox2DCommand.this.display)
                  .param("worldName", world.getName())
                  .param("newValue", Objects.toString(newValue))
                  .param("oldValue", Objects.toString(currentValue))
            );
         }
      }

      private class SetSubCommand extends AbstractWorldCommand {
         @Nonnull
         private final RequiredArg<Double> minXArg = this.withRequiredArg("minX", "server.commands.world.settings.box2d.minX.desc", ArgTypes.DOUBLE);
         @Nonnull
         private final RequiredArg<Double> minZArg = this.withRequiredArg("minZ", "server.commands.world.settings.box2d.minZ.desc", ArgTypes.DOUBLE);
         @Nonnull
         private final RequiredArg<Double> maxXArg = this.withRequiredArg("maxX", "server.commands.world.settings.box2d.maxX.desc", ArgTypes.DOUBLE);
         @Nonnull
         private final RequiredArg<Double> maxZArg = this.withRequiredArg("maxZ", "server.commands.world.settings.box2d.maxZ.desc", ArgTypes.DOUBLE);

         public SetSubCommand() {
            super("set", "server.commands.world.settings.set.desc");
         }

         @Override
         protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
            Box2D currentValue = WorldSettingsBox2DCommand.this.getter.apply(world);
            Box2D newValue = new Box2D(
               new Vector2d(context.get(this.minXArg), context.get(this.minZArg)), new Vector2d(context.get(this.maxXArg), context.get(this.maxZArg))
            );
            WorldSettingsBox2DCommand.this.setter.accept(world, newValue);
            world.getWorldConfig().markChanged();
            context.sendMessage(
               Message.translation("server.commands.world.settings.displaySet")
                  .param("display", WorldSettingsBox2DCommand.this.display)
                  .param("worldName", world.getName())
                  .param("newValue", Objects.toString(newValue))
                  .param("oldValue", Objects.toString(currentValue))
            );
         }
      }
   }

   private static class WorldSettingsSubCommand<T> extends AbstractWorldCommand {
      @Nonnull
      private final ArgumentType<T> argumentType;
      @Nonnull
      private final String display;
      @Nonnull
      private final Function<World, T> getter;
      private final Function<WorldConfig, T> defaultGetter;
      @Nonnull
      private final BiConsumer<World, T> setter;
      @Nonnull
      private final WorldConfig defaultWorldConfig;

      public WorldSettingsSubCommand(
         @Nonnull String name,
         @Nonnull String description,
         @Nonnull String arg,
         @Nonnull ArgumentType<T> argumentType,
         @Nonnull String display,
         @Nonnull Function<World, T> getter,
         @Nonnull Function<WorldConfig, T> defaultGetter,
         @Nonnull BiConsumer<World, T> setter,
         @Nonnull WorldConfig defaultWorldConfig
      ) {
         super(name, description);
         this.argumentType = argumentType;
         this.display = display;
         this.getter = getter;
         this.defaultGetter = defaultGetter;
         this.setter = setter;
         this.defaultWorldConfig = defaultWorldConfig;
         this.addSubCommand(new WorldSettingsCommand.WorldSettingsSubCommand.SetSubCommand());
         this.addSubCommand(new WorldSettingsCommand.WorldSettingsSubCommand.ResetSubCommand());
      }

      @Override
      protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
         T currentValue = this.getter.apply(world);
         context.sendMessage(
            Message.translation("server.commands.world.settings.currentValue")
               .param("display", this.display)
               .param("worldName", world.getName())
               .param("currentValue", currentValue.toString())
         );
      }

      private class ResetSubCommand extends AbstractWorldCommand {
         public ResetSubCommand() {
            super("reset", "server.commands.world.settings.reset.desc");
         }

         @Override
         protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
            T currentValue = WorldSettingsSubCommand.this.getter.apply(world);
            T newValue = WorldSettingsSubCommand.this.defaultGetter.apply(WorldSettingsSubCommand.this.defaultWorldConfig);
            WorldSettingsSubCommand.this.setter.accept(world, newValue);
            world.getWorldConfig().markChanged();
            context.sendMessage(
               Message.translation("server.commands.world.settings.displaySetDefault")
                  .param("display", WorldSettingsSubCommand.this.display)
                  .param("worldName", world.getName())
                  .param("newValue", newValue.toString())
                  .param("oldValue", currentValue.toString())
            );
         }
      }

      private class SetSubCommand extends AbstractWorldCommand {
         @Nonnull
         private final RequiredArg<T> valueArg = this.withRequiredArg(
            "value", "server.commands.world.settings.value.desc", WorldSettingsSubCommand.this.argumentType
         );

         public SetSubCommand() {
            super("set", "server.commands.world.settings.set.desc");
         }

         @Override
         protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
            T currentValue = WorldSettingsSubCommand.this.getter.apply(world);
            T newValue = context.get(this.valueArg);
            WorldSettingsSubCommand.this.setter.accept(world, newValue);
            world.getWorldConfig().markChanged();
            context.sendMessage(
               Message.translation("server.commands.world.settings.displaySet")
                  .param("display", WorldSettingsSubCommand.this.display)
                  .param("worldName", world.getName())
                  .param("newValue", newValue.toString())
                  .param("oldValue", currentValue.toString())
            );
         }
      }
   }
}
