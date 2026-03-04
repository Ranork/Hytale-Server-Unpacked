package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.codec.Vector3iArrayCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolShowAnchor;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.modules.entity.component.EntityScaleComponent;
import com.hypixel.hytale.server.core.modules.entity.component.Intangible;
import com.hypixel.hytale.server.core.modules.time.TimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabEditingMetadata {
   private static final float PREFAB_ANCHOR_ENTITY_SCALE = 2.1F;
   @Nonnull
   public static final BuilderCodec<PrefabEditingMetadata> CODEC = BuilderCodec.builder(PrefabEditingMetadata.class, PrefabEditingMetadata::new)
      .append(new KeyedCodec<>("Path", Codec.PATH), (o, path) -> o.prefabPath = path, o -> o.prefabPath)
      .add()
      .append(new KeyedCodec<>("MinPoint", new Vector3iArrayCodec()), (o, minPoint) -> o.minPoint = minPoint, o -> o.minPoint)
      .add()
      .append(new KeyedCodec<>("MaxPoint", new Vector3iArrayCodec()), (o, maxPoint) -> o.maxPoint = maxPoint, o -> o.maxPoint)
      .add()
      .append(new KeyedCodec<>("AnchorPoint", new Vector3iArrayCodec()), (o, anchorPoint) -> o.anchorPoint = anchorPoint, o -> o.anchorPoint)
      .add()
      .append(new KeyedCodec<>("PastePosition", new Vector3iArrayCodec()), (o, pastePosition) -> o.pastePosition = pastePosition, o -> o.pastePosition)
      .add()
      .append(
         new KeyedCodec<>("AnchorEntityUuid", Codec.UUID_STRING, false),
         (o, anchorEntityUuid) -> o.anchorEntityUuid = anchorEntityUuid,
         o -> o.anchorEntityUuid
      )
      .add()
      .append(
         new KeyedCodec<>("AnchorEntityPosition", new Vector3iArrayCodec(), false),
         (o, anchorEntityPosition) -> o.anchorEntityPosition = anchorEntityPosition,
         o -> o.anchorEntityPosition
      )
      .add()
      .append(new KeyedCodec<>("Uuid", Codec.UUID_STRING), (o, uuid) -> o.uuid = uuid, o -> o.uuid)
      .add()
      .append(new KeyedCodec<>("Dirty", Codec.BOOLEAN, true), (o, dirty) -> o.dirty = dirty, o -> o.dirty)
      .add()
      .build();
   private UUID uuid;
   private Path prefabPath;
   private Vector3i minPoint;
   private Vector3i maxPoint;
   private Vector3i anchorPoint;
   private Vector3i pastePosition;
   @Nullable
   private UUID anchorEntityUuid;
   private Vector3i anchorEntityPosition;
   private Vector3i originalFileAnchor;
   private boolean dirty = false;

   private PrefabEditingMetadata() {
   }

   public PrefabEditingMetadata(
      @Nonnull Path prefabPath,
      @Nonnull Vector3i minPoint,
      @Nonnull Vector3i maxPoint,
      @Nonnull Vector3i anchorPoint,
      @Nonnull Vector3i pastePosition,
      @Nonnull World world
   ) {
      this.prefabPath = prefabPath;
      this.minPoint = minPoint;
      this.maxPoint = maxPoint;
      if (minPoint.x > maxPoint.x) {
         throw new IllegalStateException("minX must be less than or equal to maxX: " + prefabPath);
      } else if (minPoint.y > maxPoint.y) {
         throw new IllegalStateException("minY must be less than or equal to maxY: " + prefabPath);
      } else if (minPoint.z > maxPoint.z) {
         throw new IllegalStateException("minZ must be less than or equal to maxZ: " + prefabPath);
      } else {
         this.uuid = UUID.randomUUID();
         this.anchorPoint = anchorPoint;
         this.pastePosition = pastePosition;
         this.originalFileAnchor = new Vector3i(anchorPoint.x - pastePosition.x, anchorPoint.y - pastePosition.y, anchorPoint.z - pastePosition.z);
         this.createAnchorEntityAt(pastePosition, world);
      }
   }

   private void createAnchorEntityAt(@Nonnull Vector3i position, @Nonnull World world) {
      this.anchorEntityPosition = position.clone();
      Store<EntityStore> store = world.getEntityStore().getStore();
      if (this.anchorEntityUuid != null) {
         Ref<EntityStore> entityReference = store.getExternalData().getRefFromUUID(this.anchorEntityUuid);
         if (entityReference != null && entityReference.isValid()) {
            world.execute(() -> store.removeEntity(entityReference, RemoveReason.REMOVE));
         }
      }

      TimeResource timeResource = store.getResource(TimeResource.getResourceType());
      Holder<EntityStore> blockEntityHolder = BlockEntity.assembleDefaultBlockEntity(timeResource, "Editor_Anchor", position.toVector3d().add(0.5, 0.0, 0.5));
      blockEntityHolder.addComponent(Intangible.getComponentType(), Intangible.INSTANCE);
      blockEntityHolder.addComponent(PrefabAnchor.getComponentType(), PrefabAnchor.INSTANCE);
      blockEntityHolder.addComponent(EntityScaleComponent.getComponentType(), new EntityScaleComponent(2.1F));
      this.anchorEntityUuid = blockEntityHolder.ensureAndGetComponent(UUIDComponent.getComponentType()).getUuid();
      world.execute(() -> store.addEntity(blockEntityHolder, AddReason.SPAWN));
   }

   public void setPrefabPath(@Nonnull Path prefabPath) {
      this.prefabPath = prefabPath;
   }

   public void setAnchorPoint(@Nonnull Vector3i newEntityPosition, @Nonnull World world) {
      int deltaX = newEntityPosition.x - this.anchorEntityPosition.x;
      int deltaY = newEntityPosition.y - this.anchorEntityPosition.y;
      int deltaZ = newEntityPosition.z - this.anchorEntityPosition.z;
      this.anchorPoint = new Vector3i(this.anchorPoint.x + deltaX, this.anchorPoint.y + deltaY, this.anchorPoint.z + deltaZ);
      this.createAnchorEntityAt(newEntityPosition, world);
   }

   public void recreateAnchorEntity(@Nonnull World world) {
      if (this.anchorEntityPosition != null) {
         this.createAnchorEntityAt(this.anchorEntityPosition, world);
      }
   }

   public void sendAnchorHighlightingPacket(@Nonnull PacketHandler displayTo) {
      displayTo.writeNoCache(new BuilderToolShowAnchor(this.anchorEntityPosition.x, this.anchorEntityPosition.y, this.anchorEntityPosition.z));
   }

   public boolean isLocationWithinPrefabBoundingBox(@Nonnull Vector3i location) {
      return location.x >= this.getMinPoint().x
         && location.x <= this.getMaxPoint().x
         && location.y >= this.getMinPoint().y
         && location.y <= this.getMaxPoint().y
         && location.z >= this.getMinPoint().z
         && location.z <= this.getMaxPoint().z;
   }

   void setMinPoint(Vector3i minPoint) {
      this.minPoint = minPoint;
   }

   void setMaxPoint(Vector3i maxPoint) {
      this.maxPoint = maxPoint;
   }

   public Vector3i getAnchorPoint() {
      return this.anchorPoint;
   }

   public Vector3i getPastePosition() {
      return this.pastePosition;
   }

   public Vector3i getOriginalFileAnchor() {
      return this.originalFileAnchor;
   }

   public Path getPrefabPath() {
      return this.prefabPath;
   }

   public Vector3i getMinPoint() {
      return this.minPoint;
   }

   public Vector3i getMaxPoint() {
      return this.maxPoint;
   }

   @Nullable
   public UUID getAnchorEntityUuid() {
      return this.anchorEntityUuid;
   }

   public Vector3i getAnchorEntityPosition() {
      return this.anchorEntityPosition;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public boolean isDirty() {
      return this.dirty;
   }

   public void setDirty(boolean dirty) {
      this.dirty = dirty;
   }

   public boolean isReadOnly() {
      return this.prefabPath != null && this.prefabPath.getFileSystem() != FileSystems.getDefault();
   }
}
