package com.hypixel.hytale.builtin.instances.page;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserConfig;
import com.hypixel.hytale.server.core.ui.browser.FileBrowserEventData;
import com.hypixel.hytale.server.core.ui.browser.ServerFileBrowser;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InstanceListPage extends InteractiveCustomUIPage<InstanceListPage.PageData> {
   @Nonnull
   private static final String ASSET_PACK_SUB_PATH = "Server/Instances";
   @Nullable
   private String selectedInstance;
   @Nonnull
   private final ServerFileBrowser browser;

   public InstanceListPage(@Nonnull PlayerRef playerRef) {
      super(playerRef, CustomPageLifetime.CanDismiss, InstanceListPage.PageData.CODEC);
      FileBrowserConfig config = FileBrowserConfig.builder()
         .listElementId("#List")
         .searchInputId("#SearchInput")
         .currentPathId("#CurrentPath")
         .enableRootSelector(false)
         .enableSearch(true)
         .enableDirectoryNav(true)
         .maxResults(50)
         .assetPackMode(true, "Server/Instances")
         .terminalDirectoryPredicate(path -> Files.exists(path.resolve("instance.bson")))
         .build();
      this.browser = new ServerFileBrowser(config);
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/InstanceListPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Spawn", EventData.of("Action", InstanceListPage.Action.Spawn.toString()));
      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#Load", EventData.of("Action", InstanceListPage.Action.Load.toString()));
      commandBuilder.set("#Load.Visible", !AssetModule.get().getBaseAssetPack().isImmutable());
      this.browser.buildCurrentPath(commandBuilder);
      this.browser.buildSearchInput(commandBuilder, eventBuilder);
      this.browser.buildFileList(commandBuilder, eventBuilder);
      commandBuilder.set("#Name.Text", this.selectedInstance != null ? this.selectedInstance : "");
      commandBuilder.set("#Name.Visible", this.selectedInstance != null);
      commandBuilder.set("#Spawn.Disabled", this.selectedInstance == null);
      commandBuilder.set("#Load.Disabled", this.selectedInstance == null);
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull InstanceListPage.PageData data) {
      if (data.searchQuery != null) {
         this.browser.setSearchQuery(data.searchQuery.trim().toLowerCase());
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.browser.buildCurrentPath(commandBuilder);
         this.browser.buildFileList(commandBuilder, eventBuilder);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else if (data.searchResult != null) {
         Path resolvedPath = this.browser.resolveAssetPackPath(data.searchResult);
         if (resolvedPath != null && this.isInstance(resolvedPath)) {
            this.updateSelection(data.searchResult);
         }
      } else if (data.file != null) {
         String fileName = data.file;
         if ("..".equals(fileName)) {
            this.browser.navigateUp();
            this.selectedInstance = null;
            UICommandBuilder commandBuilder = new UICommandBuilder();
            UIEventBuilder eventBuilder = new UIEventBuilder();
            this.browser.buildCurrentPath(commandBuilder);
            this.browser.buildFileList(commandBuilder, eventBuilder);
            commandBuilder.set("#Name.Text", "");
            commandBuilder.set("#Name.Visible", false);
            commandBuilder.set("#Spawn.Disabled", true);
            commandBuilder.set("#Load.Disabled", true);
            this.sendUpdate(commandBuilder, eventBuilder, false);
         } else {
            if (this.browser.handleEvent(FileBrowserEventData.file(fileName))) {
               this.selectedInstance = null;
               UICommandBuilder commandBuilder = new UICommandBuilder();
               UIEventBuilder eventBuilder = new UIEventBuilder();
               this.browser.buildCurrentPath(commandBuilder);
               this.browser.buildFileList(commandBuilder, eventBuilder);
               commandBuilder.set("#Name.Text", "");
               commandBuilder.set("#Name.Visible", false);
               commandBuilder.set("#Spawn.Disabled", true);
               commandBuilder.set("#Load.Disabled", true);
               this.sendUpdate(commandBuilder, eventBuilder, false);
            } else {
               String virtualPath = this.browser.getAssetPackCurrentPath().isEmpty() ? fileName : this.browser.getAssetPackCurrentPath() + "/" + fileName;
               Path resolvedPath = this.browser.resolveAssetPackPath(virtualPath);
               if (resolvedPath != null && this.isInstance(resolvedPath)) {
                  this.updateSelection(virtualPath);
               }
            }
         }
      } else {
         if (data.getAction() != null) {
            switch (data.getAction()) {
               case Load:
                  if (this.selectedInstance != null) {
                     this.load(ref, store);
                     this.close();
                  }
                  break;
               case Spawn:
                  if (this.selectedInstance != null) {
                     this.spawn(ref, store);
                     this.close();
                  }
            }
         }
      }
   }

   private boolean isInstance(@Nonnull Path path) {
      return Files.isDirectory(path) && Files.exists(path.resolve("instance.bson"));
   }

   private void load(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      String instanceName = this.getInstanceNameFromVirtualPath(this.selectedInstance);
      if (instanceName != null) {
         InstancesPlugin.get();
         InstancesPlugin.loadInstanceAssetForEdit(instanceName).thenAccept(world -> {
            Store<EntityStore> playerStore = ref.getStore();
            World playerWorld = playerStore.getExternalData().getWorld();
            playerWorld.execute(() -> {
               Transform spawnTransform = world.getWorldConfig().getSpawnProvider().getSpawnPoint(ref, playerStore);
               Teleport teleportComponent = Teleport.createForPlayer(world, spawnTransform);
               playerStore.addComponent(ref, Teleport.getComponentType(), teleportComponent);
            });
         }).exceptionally(ex -> {
            ex.printStackTrace();
            return null;
         });
      }
   }

   private void spawn(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      String instanceName = this.getInstanceNameFromVirtualPath(this.selectedInstance);
      if (instanceName != null) {
         World world = store.getExternalData().getWorld();
         world.execute(() -> {
            TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
            HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

            assert transformComponent != null;

            Vector3d position = transformComponent.getPosition();
            Transform returnLocation = new Transform(position.clone(), headRotationComponent.getRotation().clone());
            CompletableFuture<World> instanceWorld = InstancesPlugin.get().spawnInstance(instanceName, world, returnLocation);
            InstancesPlugin.teleportPlayerToLoadingInstance(ref, store, instanceWorld, null);
         });
      }
   }

   @Nullable
   private String getInstanceNameFromVirtualPath(@Nullable String virtualPath) {
      if (virtualPath != null && !virtualPath.isEmpty()) {
         String normalizedPath = virtualPath.replace('\\', '/');
         String[] parts = normalizedPath.split("/", 2);
         return parts.length > 1 ? parts[1] : normalizedPath;
      } else {
         return null;
      }
   }

   private void updateSelection(@Nonnull String virtualPath) {
      this.selectedInstance = virtualPath;
      UICommandBuilder commandBuilder = new UICommandBuilder();
      String displayName = virtualPath.contains("/") ? virtualPath.substring(virtualPath.lastIndexOf(47) + 1) : virtualPath;
      commandBuilder.set("#Name.Text", displayName);
      commandBuilder.set("#Name.Visible", true);
      commandBuilder.set("#Spawn.Disabled", false);
      commandBuilder.set("#Load.Disabled", false);
      this.sendUpdate(commandBuilder, false);
   }

   public static enum Action {
      Select,
      Load,
      Spawn;
   }

   public static class PageData {
      @Nonnull
      public static final String KEY_INSTANCE = "Instance";
      @Nonnull
      public static final String KEY_ACTION = "Action";
      @Nonnull
      public static final BuilderCodec<InstanceListPage.PageData> CODEC = BuilderCodec.builder(InstanceListPage.PageData.class, InstanceListPage.PageData::new)
         .addField(new KeyedCodec<>("Instance", BuilderCodec.STRING), (o, i) -> o.instance = i, o -> o.instance)
         .addField(new KeyedCodec<>("Action", new EnumCodec<>(InstanceListPage.Action.class)), (o, i) -> o.action = i, o -> o.action)
         .addField(new KeyedCodec<>("File", Codec.STRING), (o, s) -> o.file = s, o -> o.file)
         .addField(new KeyedCodec<>("@SearchQuery", Codec.STRING), (o, s) -> o.searchQuery = s, o -> o.searchQuery)
         .addField(new KeyedCodec<>("SearchResult", Codec.STRING), (o, s) -> o.searchResult = s, o -> o.searchResult)
         .build();
      private String instance;
      private InstanceListPage.Action action;
      @Nullable
      private String file;
      @Nullable
      private String searchQuery;
      @Nullable
      private String searchResult;

      public String getInstance() {
         return this.instance;
      }

      public InstanceListPage.Action getAction() {
         return this.action;
      }
   }
}
