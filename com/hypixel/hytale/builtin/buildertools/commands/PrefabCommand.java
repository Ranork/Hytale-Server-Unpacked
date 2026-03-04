package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.prefablist.PrefabPage;
import com.hypixel.hytale.builtin.buildertools.prefablist.PrefabSavePage;
import com.hypixel.hytale.builtin.buildertools.utils.RecursivePrefabLoader;
import com.hypixel.hytale.common.util.PathUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.singleplayer.SingleplayerModule;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.io.FileUtil;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabCommand extends AbstractCommandCollection {
   public PrefabCommand() {
      super("prefab", "server.commands.prefab.desc");
      this.addAliases("p");
      this.setPermissionGroup(GameMode.Creative);
      this.addSubCommand(new PrefabCommand.PrefabSaveCommand());
      this.addSubCommand(new PrefabCommand.PrefabLoadCommand());
      this.addSubCommand(new PrefabCommand.PrefabDeleteCommand());
      this.addSubCommand(new PrefabCommand.PrefabListCommand());
   }

   private static class PrefabDeleteCommand extends CommandBase {
      @Nonnull
      private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.prefab.delete.name.desc", ArgTypes.STRING);

      public PrefabDeleteCommand() {
         super("delete", "server.commands.prefab.delete.desc", true);
         this.requirePermission("hytale.editor.prefab.manage");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String name = this.nameArg.get(context);
         if (!name.endsWith(".prefab.json")) {
            name = name + ".prefab.json";
         }

         PrefabStore module = PrefabStore.get();
         Path serverPrefabsPath = module.getServerPrefabsPath();
         Path resolve = serverPrefabsPath.resolve(name);

         try {
            Ref<EntityStore> ref = context.senderAsPlayerRef();
            boolean isOwner = false;
            if (ref != null && ref.isValid()) {
               Store<EntityStore> store = ref.getStore();
               PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
               if (playerRefComponent != null) {
                  isOwner = SingleplayerModule.isOwner(playerRefComponent);
               }
            }

            if (!PathUtil.isChildOf(serverPrefabsPath, resolve) && !isOwner) {
               context.sendMessage(Message.translation("server.builderTools.attemptedToSaveOutsidePrefabsDir"));
               return;
            }

            Path relativize = PathUtil.relativize(serverPrefabsPath, resolve);
            if (Files.isRegularFile(resolve)) {
               Files.delete(resolve);
               context.sendMessage(Message.translation("server.builderTools.prefab.deleted").param("name", relativize.toString()));
            } else {
               context.sendMessage(Message.translation("server.builderTools.prefab.prefabNotFound").param("name", relativize.toString()));
            }
         } catch (IOException var10) {
            context.sendMessage(Message.translation("server.builderTools.prefab.errorOccured").param("reason", var10.getMessage()));
         }
      }
   }

   private static class PrefabListCommand extends CommandBase {
      @Nonnull
      private final DefaultArg<String> storeTypeArg = this.withDefaultArg(
         "storeType", "server.commands.prefab.list.storeType.desc", ArgTypes.STRING, "asset", "server.commands.prefab.list.storeType.desc"
      );
      @Nonnull
      private final FlagArg textFlag = this.withFlagArg("text", "server.commands.prefab.list.text.desc");

      public PrefabListCommand() {
         super("list", "server.commands.prefab.list.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String storeType = this.storeTypeArg.get(context);

         final Path prefabStorePath = switch (storeType) {
            case "server" -> PrefabStore.get().getServerPrefabsPath();
            case "asset" -> {
               List<PrefabStore.AssetPackPrefabPath> assetPaths = PrefabStore.get().getAllAssetPrefabPaths();
               yield assetPaths.isEmpty() ? PrefabStore.get().getAssetPrefabsPath() : assetPaths.getFirst().prefabsPath();
            }
            case "worldgen" -> PrefabStore.get().getWorldGenPrefabsPath();
            default -> throw new IllegalStateException("Unexpected value: " + storeType);
         };
         Ref<EntityStore> ref = context.senderAsPlayerRef();
         if (ref != null && ref.isValid() && !this.textFlag.get(context)) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(() -> {
               Player playerComponent = store.getComponent(ref, Player.getComponentType());

               assert playerComponent != null;

               PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

               assert playerRefComponent != null;

               BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRefComponent);
               playerComponent.getPageManager().openCustomPage(ref, store, new PrefabPage(playerRefComponent, prefabStorePath, builderState));
            });
         } else {
            try {
               final List<Message> prefabFiles = new ObjectArrayList();
               if ("asset".equals(storeType)) {
                  for (PrefabStore.AssetPackPrefabPath packPath : PrefabStore.get().getAllAssetPrefabPaths()) {
                     final Path path = packPath.prefabsPath();
                     final String packPrefix = packPath.isBasePack() ? "" : "[" + packPath.getPackName() + "] ";
                     if (Files.isDirectory(path)) {
                        Files.walkFileTree(path, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                           @Nonnull
                           public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                              String fileName = file.getFileName().toString();
                              if (fileName.endsWith(".prefab.json")) {
                                 prefabFiles.add(Message.raw(packPrefix + PathUtil.relativize(path, file).toString()));
                              }

                              return FileVisitResult.CONTINUE;
                           }
                        });
                     }
                  }
               } else if (Files.isDirectory(prefabStorePath)) {
                  Files.walkFileTree(prefabStorePath, FileUtil.DEFAULT_WALK_TREE_OPTIONS_SET, Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                     @Nonnull
                     public FileVisitResult visitFile(@Nonnull Path file, @Nonnull BasicFileAttributes attrs) {
                        String fileName = file.getFileName().toString();
                        if (fileName.endsWith(".prefab.json")) {
                           prefabFiles.add(Message.raw(PathUtil.relativize(prefabStorePath, file).toString()));
                        }

                        return FileVisitResult.CONTINUE;
                     }
                  });
               }

               context.sendMessage(MessageFormat.list(Message.translation("server.commands.prefab.list.header"), prefabFiles));
            } catch (IOException var10) {
               context.sendMessage(Message.translation("server.builderTools.prefab.errorListingPrefabs").param("reason", var10.getMessage()));
            }
         }
      }
   }

   private static class PrefabLoadByNameCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.prefab.load.name.desc", ArgTypes.STRING);
      @Nonnull
      private final DefaultArg<String> storeTypeArg = this.withDefaultArg(
         "storeType", "server.commands.prefab.load.storeType.desc", ArgTypes.STRING, "asset", "server.commands.prefab.load.storeType.desc"
      );
      @Nonnull
      private final DefaultArg<String> storeNameArg = this.withDefaultArg("storeName", "server.commands.prefab.load.storeName.desc", ArgTypes.STRING, null, "");
      @Nonnull
      private final FlagArg childrenFlag = this.withFlagArg("children", "server.commands.prefab.load.children.desc");

      public PrefabLoadByNameCommand() {
         super("server.commands.prefab.load.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         String storeType = this.storeTypeArg.get(context);
         String storeName = this.storeNameArg.get(context);
         String name = this.nameArg.get(context);
         if (!name.endsWith(".prefab.json")) {
            name = name + ".prefab.json";
         }

         Path prefabStorePath = null;
         Path resolvedPrefabPath = null;
         String finalName = name;

         Function<String, BlockSelection> prefabGetter = switch (storeType) {
            case "server" -> {
               prefabStorePath = PrefabStore.get().getServerPrefabsPath();
               yield PrefabStore.get()::getServerPrefab;
            }
            case "asset" -> {
               Path foundPath = PrefabStore.get().findAssetPrefabPath(finalName);
               if (foundPath != null) {
                  resolvedPrefabPath = foundPath;
                  prefabStorePath = foundPath.getParent();
                  yield key -> PrefabStore.get().getPrefab(foundPath);
               } else {
                  prefabStorePath = PrefabStore.get().getAssetPrefabsPath();
                  yield PrefabStore.get()::getAssetPrefab;
               }
            }
            case "worldgen" -> {
               Path storePath = PrefabStore.get().getWorldGenPrefabsPath(storeName);
               prefabStorePath = PrefabStore.get().getWorldGenPrefabsPath(storeName);
               yield key -> PrefabStore.get().getWorldGenPrefab(storePath, key);
            }
            default -> {
               context.sendMessage(Message.translation("server.commands.prefab.invalidStoreType").param("storeType", storeType));
               yield null;
            }
         };
         if (prefabGetter != null) {
            BiFunction<String, Random, BlockSelection> loader;
            if (this.childrenFlag.get(context)) {
               loader = new RecursivePrefabLoader.BlockSelectionLoader(prefabStorePath, prefabGetter);
            } else {
               loader = (prefabFile, rand) -> prefabGetter.apply(prefabFile);
            }

            boolean prefabExists = resolvedPrefabPath != null && Files.isRegularFile(resolvedPrefabPath) || Files.isRegularFile(prefabStorePath.resolve(name));
            if (prefabExists) {
               BuilderToolsPlugin.addToQueue(
                  playerComponent, playerRef, (r, s, componentAccessor) -> s.load(finalName, loader.apply(finalName, s.getRandom()), componentAccessor)
               );
            } else {
               context.sendMessage(Message.translation("server.builderTools.prefab.prefabNotFound").param("name", name));
            }
         }
      }
   }

   private static class PrefabLoadCommand extends AbstractPlayerCommand {
      public PrefabLoadCommand() {
         super("load", "server.commands.prefab.load.desc");
         this.requirePermission("hytale.editor.prefab.use");
         this.addUsageVariant(new PrefabCommand.PrefabLoadByNameCommand());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         List<PrefabStore.AssetPackPrefabPath> assetPaths = PrefabStore.get().getAllAssetPrefabPaths();
         Path defaultRoot = assetPaths.isEmpty() ? PrefabStore.get().getServerPrefabsPath() : assetPaths.getFirst().prefabsPath();
         BuilderToolsPlugin.BuilderState builderState = BuilderToolsPlugin.getState(playerComponent, playerRef);
         playerComponent.getPageManager().openCustomPage(ref, store, new PrefabPage(playerRef, defaultRoot, builderState));
      }
   }

   private static class PrefabSaveCommand extends AbstractPlayerCommand {
      public PrefabSaveCommand() {
         super("save", "server.commands.prefab.save.desc");
         this.requirePermission("hytale.editor.prefab.manage");
         this.addUsageVariant(new PrefabCommand.PrefabSaveDirectCommand());
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new PrefabSavePage(playerRef));
      }
   }

   private static class PrefabSaveDirectCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.prefab.save.name.desc", ArgTypes.STRING);
      @Nonnull
      private final FlagArg overwriteFlag = this.withFlagArg("overwrite", "server.commands.prefab.save.overwrite.desc");
      @Nonnull
      private final FlagArg entitiesFlag = this.withFlagArg("entities", "server.commands.prefab.save.entities.desc");
      @Nonnull
      private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.prefab.save.empty.desc");
      @Nonnull
      private final FlagArg playerAnchorFlag = this.withFlagArg("playerAnchor", "server.commands.prefab.save.playerAnchor.desc");
      @Nonnull
      private final FlagArg clearSupportFlag = this.withFlagArg("clearSupport", "server.commands.editprefab.save.clearSupport.desc");

      public PrefabSaveDirectCommand() {
         super("server.commands.prefab.save.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         String name = this.nameArg.get(context);
         boolean overwrite = this.overwriteFlag.get(context);
         boolean entities = this.entitiesFlag.get(context);
         boolean empty = this.emptyFlag.get(context);
         boolean clearSupport = this.clearSupportFlag.get(context);
         Vector3i playerAnchor = this.getPlayerAnchor(ref, store, this.playerAnchorFlag.get(context));
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> s.saveFromSelection(r, name, true, overwrite, entities, empty, playerAnchor, clearSupport, componentAccessor)
         );
      }

      @Nullable
      private Vector3i getPlayerAnchor(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, boolean usePlayerAnchor) {
         if (!usePlayerAnchor) {
            return null;
         } else {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            if (transformComponent == null) {
               return null;
            } else {
               Vector3d position = transformComponent.getPosition();
               return new Vector3i(MathUtil.floor(position.getX()), MathUtil.floor(position.getY()), MathUtil.floor(position.getZ()));
            }
         }
      }
   }
}
