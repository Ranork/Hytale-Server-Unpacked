package com.hypixel.hytale.builtin.buildertools.prefabeditor.ui;

import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditSession;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.PrefabEditingMetadata;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.StringCompareUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public class PrefabTeleportPage extends InteractiveCustomUIPage<PrefabTeleportPage.PageData> {
   private static final Value<String> BUTTON_HIGHLIGHTED = Value.ref("Pages/BasicTextButton.ui", "SelectedLabelStyle");
   @Nonnull
   private final PrefabEditSession prefabEditSession;
   @Nonnull
   private String searchQuery = "";

   public PrefabTeleportPage(@Nonnull PlayerRef playerRef, @Nonnull PrefabEditSession prefabEditSession) {
      super(playerRef, CustomPageLifetime.CanDismiss, PrefabTeleportPage.PageData.CODEC);
      this.prefabEditSession = prefabEditSession;
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/PrefabTeleportPage.ui");
      eventBuilder.addEventBinding(CustomUIEventBindingType.ValueChanged, "#SearchInput", EventData.of("@SearchQuery", "#SearchInput.Value"), false);
      this.buildPrefabList(commandBuilder, eventBuilder);
   }

   private void buildPrefabList(@Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder) {
      commandBuilder.clear("#FileList");
      Map<UUID, PrefabEditingMetadata> loadedPrefabs = this.prefabEditSession.getLoadedPrefabMetadata();
      if (!loadedPrefabs.isEmpty()) {
         List<PrefabEditingMetadata> prefabsToDisplay;
         if (!this.searchQuery.isEmpty()) {
            Object2IntMap<PrefabEditingMetadata> matchScores = new Object2IntOpenHashMap(loadedPrefabs.size());

            for (PrefabEditingMetadata metadata : loadedPrefabs.values()) {
               String fileName = metadata.getPrefabPath().getFileName().toString();
               String baseName = fileName.endsWith(".prefab.json") ? fileName.substring(0, fileName.length() - ".prefab.json".length()) : fileName;
               int fuzzyDistance = StringCompareUtil.getFuzzyDistance(baseName, this.searchQuery, Locale.ENGLISH);
               if (fuzzyDistance > 0) {
                  matchScores.put(metadata, fuzzyDistance);
               }
            }

            prefabsToDisplay = matchScores.keySet().stream().sorted(Comparator.comparingInt(matchScores::getInt).reversed()).collect(Collectors.toList());
         } else {
            prefabsToDisplay = loadedPrefabs.values()
               .stream()
               .sorted(Comparator.comparing(m -> m.getPrefabPath().getFileName().toString().toLowerCase()))
               .collect(Collectors.toList());
         }

         int buttonIndex = 0;

         for (PrefabEditingMetadata metadatax : prefabsToDisplay) {
            Path prefabPath = metadatax.getPrefabPath();
            String fileName = prefabPath.getFileName().toString();
            String displayName = fileName.endsWith(".prefab.json") ? fileName.substring(0, fileName.length() - ".prefab.json".length()) : fileName;
            commandBuilder.append("#FileList", "Pages/BasicTextButton.ui");
            commandBuilder.set("#FileList[" + buttonIndex + "].Text", displayName);
            commandBuilder.set("#FileList[" + buttonIndex + "].Style", BUTTON_HIGHLIGHTED);
            commandBuilder.set("#FileList[" + buttonIndex + "].TooltipText", prefabPath.toString());
            eventBuilder.addEventBinding(
               CustomUIEventBindingType.Activating, "#FileList[" + buttonIndex + "]", EventData.of("PrefabUuid", metadatax.getUuid().toString())
            );
            buttonIndex++;
         }
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PrefabTeleportPage.PageData data) {
      if (data.getSearchQuery() != null) {
         this.searchQuery = data.getSearchQuery().trim().toLowerCase();
         UICommandBuilder commandBuilder = new UICommandBuilder();
         UIEventBuilder eventBuilder = new UIEventBuilder();
         this.buildPrefabList(commandBuilder, eventBuilder);
         this.sendUpdate(commandBuilder, eventBuilder, false);
      } else {
         if (data.getPrefabUuid() != null) {
            try {
               UUID prefabUuid = UUID.fromString(data.getPrefabUuid());
               PrefabEditingMetadata metadata = this.prefabEditSession.getLoadedPrefabMetadata().get(prefabUuid);
               if (metadata != null) {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());

                  assert playerComponent != null;

                  Vector3i minPoint = metadata.getMinPoint();
                  Vector3i maxPoint = metadata.getMaxPoint();
                  int centerX = (minPoint.x + maxPoint.x) / 2;
                  int centerZ = (minPoint.z + maxPoint.z) / 2;
                  World world = store.getExternalData().getWorld();
                  WorldChunk worldChunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(centerX, centerZ));
                  int teleportY = worldChunk != null ? worldChunk.getHeight(centerX, centerZ) + 8 : maxPoint.y + 8;
                  Vector3d teleportPosition = new Vector3d(centerX + 0.5, teleportY, centerZ + 0.5);
                  store.addComponent(ref, Teleport.getComponentType(), Teleport.createForPlayer(teleportPosition, new Vector3f()));
                  playerComponent.getPageManager().setPage(ref, store, Page.None);
               }
            } catch (IllegalArgumentException var15) {
            }
         }
      }
   }

   public static class PageData {
      static final String KEY_SEARCH_QUERY = "@SearchQuery";
      static final String KEY_PREFAB_UUID = "PrefabUuid";
      public static final BuilderCodec<PrefabTeleportPage.PageData> CODEC = BuilderCodec.builder(
            PrefabTeleportPage.PageData.class, PrefabTeleportPage.PageData::new
         )
         .addField(new KeyedCodec<>("@SearchQuery", Codec.STRING), (entry, s) -> entry.searchQuery = s, entry -> entry.searchQuery)
         .addField(new KeyedCodec<>("PrefabUuid", Codec.STRING), (entry, s) -> entry.prefabUuid = s, entry -> entry.prefabUuid)
         .build();
      private String searchQuery;
      private String prefabUuid;

      public String getSearchQuery() {
         return this.searchQuery;
      }

      public String getPrefabUuid() {
         return this.prefabUuid;
      }
   }
}
