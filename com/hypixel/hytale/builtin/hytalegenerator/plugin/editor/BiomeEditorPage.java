package com.hypixel.hytale.builtin.hytalegenerator.plugin.editor;

import com.hypixel.hytale.builtin.hytalegenerator.plugin.HytaleGenerator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.ui.DropdownEntryInfo;
import com.hypixel.hytale.server.core.ui.LocalizableString;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BiomeEditorPage extends InteractiveCustomUIPage<BiomeEditor.Config> {
   protected static final String KEY_NAME = "@Name";
   protected static final String KEY_PACK = "@Pack";
   protected static final String KEY_GROUP = "@Group";
   protected static final String KEY_BIOME_TEMPLATE = "@BiomeTemplate";
   protected static final String KEY_WORLD_STRUCTURE_TEMPLATE = "@WorldStructureTemplate";
   protected static final String SELECTED_NONE = "(None)";
   protected static final String DEFAULT_BIOME_NAME = "MyBiome";
   @Nonnull
   protected final BiomeEditor.Defaults defaults;
   public static final BuilderCodec<BiomeEditor.Config> CONFIG_CODEC = BuilderCodec.builder(BiomeEditor.Config.class, BiomeEditor.Config::new)
      .append(new KeyedCodec<>("@Name", Codec.STRING), (self, val) -> self.name = val, self -> self.name)
      .documentation("The name of the biome to create and edit")
      .add()
      .<String>append(new KeyedCodec<>("@Pack", Codec.STRING), (self, val) -> self.pack = val, self -> self.pack)
      .documentation("The name of the AssetPack to create the biome under")
      .add()
      .<String>append(new KeyedCodec<>("@Group", Codec.STRING), (self, val) -> self.group = val, self -> self.group)
      .documentation("The group-name of the AssetPack to create the biome under")
      .add()
      .<String>append(new KeyedCodec<>("@BiomeTemplate", Codec.STRING), (self, val) -> self.biomeTemplate = val, self -> self.biomeTemplate)
      .documentation("The existing Biome asset to use as a template to create new Biomes from")
      .add()
      .<String>append(
         new KeyedCodec<>("@WorldStructureTemplate", Codec.STRING), (self, val) -> self.worldStructureTemplate = val, self -> self.worldStructureTemplate
      )
      .documentation("The existing WorldStructure asset to use as a template to create new WorldStructures from")
      .add()
      .build();

   public BiomeEditorPage(@Nonnull PlayerRef playerRef, @Nonnull BiomeEditor.Defaults defaults) {
      super(playerRef, CustomPageLifetime.CanDismiss, CONFIG_CODEC);
      this.defaults = defaults;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/BiomeEditorPage.ui");
      commandBuilder.set("#Name #Input.Value", "MyBiome");
      commandBuilder.set("#Pack #Input.Value", this.defaults.pack);
      commandBuilder.set("#Group #Input.Value", this.defaults.group);
      commandBuilder.set("#BiomeTemplate #Input.Entries", createDropdownList(this.defaults.biomeAssets));
      commandBuilder.set("#BiomeTemplate #Input.Value", getSelectedValue(this.defaults.biome, this.defaults.biomeAssets));
      commandBuilder.set("#WorldStructureTemplate #Input.Entries", createDropdownList(this.defaults.worldStructureAssets));
      commandBuilder.set("#WorldStructureTemplate #Input.Value", getSelectedValue(this.defaults.worldStructure, this.defaults.worldStructureAssets));
      eventBuilder.addEventBinding(
         CustomUIEventBindingType.Activating,
         "#Content #LaunchButton",
         new EventData()
            .append("@Name", "#Name #Input.Value")
            .append("@Pack", "#Pack #Input.Value")
            .append("@Group", "#Group #Input.Value")
            .append("@BiomeTemplate", "#BiomeTemplate #Input.Value")
            .append("@WorldStructureTemplate", "#WorldStructureTemplate #Input.Value")
      );
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BiomeEditor.Config config) {
      if ("(None)".equals(config.biomeTemplate)) {
         config.biomeTemplate = null;
      }

      if ("(None)".equals(config.worldStructureTemplate)) {
         config.worldStructureTemplate = null;
      }

      Executor syncExecutor = store.getExternalData().getWorld();
      config.viewport = this.defaults.viewportBounds;
      config.enablePersistence = this.defaults.enablePersistence;
      BiomeEditor.setupViewport(config, ref).whenCompleteAsync((ignored, error) -> {
         if (error != null) {
            ((HytaleLogger.Api)((HytaleLogger.Api)BiomeEditor.LOGGER.atSevere()).withCause(error)).log("Error occurred whilst setting up BiomeEditor");
            if (ref.isValid()) {
               PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
               if (playerRef != null) {
                  playerRef.sendMessage(Message.translation("server.customUI.biomeEditor.error"));
               }
            }
         }
      }, syncExecutor);
      this.close();
   }

   protected static String getSelectedValue(@Nullable String value, @Nonnull String[] assets) {
      if (value == null) {
         return "(None)";
      } else {
         return ArrayUtil.indexOf(assets, value) == -1 ? "(None)" : value;
      }
   }

   protected static ObjectList<DropdownEntryInfo> createDropdownList(@Nonnull String[] assets) {
      ObjectArrayList<DropdownEntryInfo> options = new ObjectArrayList(assets.length + 1);
      options.add(new DropdownEntryInfo(LocalizableString.fromMessageId("server.customUI.biomeEditor.template.none"), "(None)"));

      for (String asset : assets) {
         options.add(new DropdownEntryInfo(LocalizableString.fromString(asset), asset));
      }

      return options;
   }

   public static CompletableFuture<Void> open(@Nonnull Ref<EntityStore> user) {
      World syncExecutor = user.getStore().getExternalData().getWorld();
      return HytaleGenerator.get().biomeEditorConfig.load().thenAcceptBothAsync(CompletableFuture.completedFuture(user), (defaults, ref) -> {
         if (ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            Player player = store.getComponent((Ref<EntityStore>)ref, Player.getComponentType());
            PlayerRef playerRef = store.getComponent((Ref<EntityStore>)ref, PlayerRef.getComponentType());
            if (player != null && playerRef != null) {
               player.getPageManager().openCustomPage((Ref<EntityStore>)ref, store, new BiomeEditorPage(playerRef, defaults));
            }
         }
      }, syncExecutor);
   }
}
