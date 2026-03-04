package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.codec.Vector3iArrayCodec;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolHideAnchors;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabEditSession implements Resource<EntityStore> {
   @Nonnull
   public static final BuilderCodec<PrefabEditSession> CODEC = BuilderCodec.builder(PrefabEditSession.class, PrefabEditSession::new)
      .append(new KeyedCodec<>("WorldName", Codec.STRING, true), (o, worldName) -> o.worldName = worldName, o -> o.worldName)
      .add()
      .append(new KeyedCodec<>("WorldArrivedFrom", Codec.UUID_STRING, true), (o, worldName) -> o.worldArrivedFrom = worldName, o -> o.worldArrivedFrom)
      .add()
      .append(
         new KeyedCodec<>("TransformArrivedFrom", Transform.CODEC, true),
         (o, positionArrivedFrom) -> o.transformArrivedFrom = positionArrivedFrom,
         o -> o.transformArrivedFrom
      )
      .add()
      .append(new KeyedCodec<>("WorldCreatorUUID", Codec.UUID_STRING, true), (o, worldCreatorUuid) -> o.worldCreator = worldCreatorUuid, o -> o.worldCreator)
      .add()
      .append(new KeyedCodec<>("SpawnPoint", new Vector3iArrayCodec(), true), (o, spawnPoint) -> o.spawnPoint = spawnPoint, o -> o.spawnPoint)
      .add()
      .append(
         new KeyedCodec<>("LoadedPrefabMetadata", new ArrayCodec<>(PrefabEditingMetadata.CODEC, PrefabEditingMetadata[]::new), false),
         (editSession, prefabEditingMetadata) -> {
            for (PrefabEditingMetadata prefabEditMetadata : prefabEditingMetadata) {
               editSession.loadedPrefabMetadata.put(prefabEditMetadata.getUuid(), prefabEditMetadata);
            }
         },
         editSession -> editSession.loadedPrefabMetadata.values().toArray(PrefabEditingMetadata[]::new)
      )
      .add()
      .afterDecode(editSession -> {
         PrefabEditSessionManager prefabEditSessionManager = BuilderToolsPlugin.get().getPrefabEditSessionManager();
         prefabEditSessionManager.populateActiveEditSession(editSession.getWorldCreator(), editSession);

         for (PrefabEditingMetadata value : editSession.loadedPrefabMetadata.values()) {
            prefabEditSessionManager.populatePrefabsBeingEdited(value.getPrefabPath());
         }

         prefabEditSessionManager.scheduleAnchorEntityRecreation(editSession);
      })
      .build();
   private String worldName;
   private UUID worldArrivedFrom;
   @Nullable
   private Transform transformArrivedFrom;
   private UUID worldCreator;
   @Nonnull
   private final Map<UUID, PrefabEditingMetadata> loadedPrefabMetadata = new Object2ObjectOpenHashMap();
   @Nonnull
   private final Map<UUID, UUID> selectedPrefab = new Object2ObjectOpenHashMap();
   @Nonnull
   private Vector3i spawnPoint = new Vector3i(0, 0, 0);

   @Nonnull
   public static ResourceType<EntityStore, PrefabEditSession> getResourceType() {
      return BuilderToolsPlugin.get().getPrefabEditSessionResourceType();
   }

   private PrefabEditSession() {
   }

   public PrefabEditSession(@Nonnull String worldName, @Nonnull UUID worldCreator, @Nonnull UUID worldArrivedFrom, @Nonnull Transform transformArrivedFrom) {
      this.worldName = worldName;
      this.worldCreator = worldCreator;
      this.worldArrivedFrom = worldArrivedFrom;
      this.transformArrivedFrom = transformArrivedFrom;
   }

   public PrefabEditSession(@Nonnull PrefabEditSession other) {
      this.worldName = other.worldName;
      this.worldArrivedFrom = other.worldArrivedFrom;
      this.transformArrivedFrom = other.transformArrivedFrom;
      this.worldCreator = other.worldCreator;
      this.spawnPoint = other.spawnPoint;
   }

   public void addPrefab(
      @Nonnull Path prefabPath, @Nonnull Vector3i minPoint, @Nonnull Vector3i maxPoint, @Nonnull Vector3i anchorPoint, @Nonnull Vector3i pastePosition
   ) {
      if (this.loadedPrefabMetadata.isEmpty()) {
         this.spawnPoint.assign(maxPoint);
      }

      PrefabEditingMetadata prefabEditingMetadata = new PrefabEditingMetadata(
         prefabPath, minPoint, maxPoint, anchorPoint, pastePosition, Universe.get().getWorld(this.worldName)
      );
      this.loadedPrefabMetadata.put(prefabEditingMetadata.getUuid(), prefabEditingMetadata);
   }

   @Nullable
   public PrefabEditingMetadata updatePrefabBounds(@Nonnull UUID prefab, @Nonnull Vector3i newMin, @Nonnull Vector3i newMax) {
      PrefabEditingMetadata prefabEditingMetadata = this.loadedPrefabMetadata.get(prefab);
      if (prefabEditingMetadata == null) {
         return null;
      } else {
         prefabEditingMetadata.setMaxPoint(newMax);
         prefabEditingMetadata.setMinPoint(newMin);
         prefabEditingMetadata.setDirty(true);
         return prefabEditingMetadata;
      }
   }

   public void setSelectedPrefab(
      @Nonnull Ref<EntityStore> ref, @Nonnull PrefabEditingMetadata prefabEditingMetadata, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      UUID playerUUID = uuidComponent.getUuid();
      if (this.selectedPrefab.get(playerUUID) != null && this.selectedPrefab.get(playerUUID).equals(prefabEditingMetadata.getUuid())) {
         BlockSelection selection = BuilderToolsPlugin.getState(playerComponent, playerRefComponent).getSelection();
         if (selection != null
            && prefabEditingMetadata.getMinPoint().equals(selection.getSelectionMin())
            && prefabEditingMetadata.getMaxPoint().equals(selection.getSelectionMax())) {
            return;
         }
      }

      this.selectedPrefab.put(playerUUID, prefabEditingMetadata.getUuid());
      prefabEditingMetadata.sendAnchorHighlightingPacket(playerRefComponent.getPacketHandler());
      BuilderToolsPlugin.addToQueue(
         playerComponent,
         playerRefComponent,
         (r, s, compAccess) -> s.select(prefabEditingMetadata.getMinPoint(), prefabEditingMetadata.getMaxPoint(), null, compAccess)
      );
   }

   public void hidePrefabAnchors(@Nonnull PacketHandler packetHandler) {
      packetHandler.writeNoCache(new BuilderToolHideAnchors());
   }

   @Nullable
   public PrefabEditingMetadata getSelectedPrefab(@Nonnull UUID playerUuid) {
      UUID prefabUuid = this.selectedPrefab.get(playerUuid);
      return prefabUuid == null ? null : this.loadedPrefabMetadata.get(prefabUuid);
   }

   public boolean clearSelectedPrefab(@Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      UUIDComponent uuidComponent = componentAccessor.getComponent(ref, UUIDComponent.getComponentType());

      assert uuidComponent != null;

      UUID playerUUID = uuidComponent.getUuid();
      if (this.selectedPrefab.remove(playerUUID) == null) {
         return false;
      } else {
         PlayerRef playerRefComponent = componentAccessor.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         Player playerComponent = componentAccessor.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         this.hidePrefabAnchors(playerRefComponent.getPacketHandler());
         BuilderToolsPlugin.addToQueue(playerComponent, playerRefComponent, (r, s, compAccess) -> s.deselect(compAccess));
         return true;
      }
   }

   @Nonnull
   public String getWorldName() {
      return this.worldName;
   }

   public UUID getWorldArrivedFrom() {
      return this.worldArrivedFrom;
   }

   @Nullable
   public Transform getTransformArrivedFrom() {
      return this.transformArrivedFrom;
   }

   public UUID getWorldCreator() {
      return this.worldCreator;
   }

   @Nonnull
   public Vector3i getSpawnPoint() {
      return this.spawnPoint;
   }

   @Nonnull
   public Map<UUID, PrefabEditingMetadata> getLoadedPrefabMetadata() {
      return this.loadedPrefabMetadata;
   }

   public void markPrefabsDirtyAtPosition(@Nonnull Vector3i position) {
      for (PrefabEditingMetadata metadata : this.loadedPrefabMetadata.values()) {
         if (metadata.isLocationWithinPrefabBoundingBox(position)) {
            metadata.setDirty(true);
         }
      }
   }

   public void markPrefabsDirtyInBounds(@Nonnull Vector3i min, @Nonnull Vector3i max) {
      for (PrefabEditingMetadata metadata : this.loadedPrefabMetadata.values()) {
         if (boundsIntersect(metadata.getMinPoint(), metadata.getMaxPoint(), min, max)) {
            metadata.setDirty(true);
         }
      }
   }

   private static boolean boundsIntersect(@Nonnull Vector3i aMin, @Nonnull Vector3i aMax, @Nonnull Vector3i bMin, @Nonnull Vector3i bMax) {
      return aMin.x <= bMax.x && aMax.x >= bMin.x && aMin.y <= bMax.y && aMax.y >= bMin.y && aMin.z <= bMax.z && aMax.z >= bMin.z;
   }

   @Nonnull
   public MapMarker[] createPrefabMarkers() {
      return this.loadedPrefabMetadata.values().stream().map(PrefabEditSession::createPrefabMarker).toArray(MapMarker[]::new);
   }

   @Nonnull
   public static MapMarker createPrefabMarker(@Nonnull PrefabEditingMetadata metadata) {
      String fileName = metadata.getPrefabPath().getFileName().toString();
      String prefabName = fileName.replace(".prefab.json", "");
      Transform transform = new Transform(metadata.getAnchorEntityPosition());
      return new MapMarkerBuilder("prefab-" + metadata.getUuid(), "Prefab.png", transform).withCustomName(prefabName).build();
   }

   @Nonnull
   public PrefabEditSession clone() {
      return new PrefabEditSession(this);
   }
}
