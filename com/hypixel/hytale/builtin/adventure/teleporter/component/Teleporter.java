package com.hypixel.hytale.builtin.adventure.teleporter.component;

import com.hypixel.hytale.builtin.adventure.teleporter.TeleporterPlugin;
import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.wordlist.WordList;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Teleporter implements Component<ChunkStore> {
   @Nonnull
   public static final BuilderCodec<Teleporter> CODEC = BuilderCodec.builder(Teleporter.class, Teleporter::new)
      .append(new KeyedCodec<>("World", Codec.UUID_BINARY), (teleporter, uuid) -> teleporter.worldUuid = uuid, teleporter -> teleporter.worldUuid)
      .add()
      .append(new KeyedCodec<>("Transform", Transform.CODEC), (teleporter, transform) -> teleporter.transform = transform, teleporter -> teleporter.transform)
      .add()
      .append(new KeyedCodec<>("Relative", Codec.BYTE), (teleporter, b) -> teleporter.relativeMask = b, teleporter -> teleporter.relativeMask)
      .add()
      .append(new KeyedCodec<>("Warp", Codec.STRING), (teleporter, s) -> teleporter.warp = s, teleporter -> teleporter.warp)
      .add()
      .append(new KeyedCodec<>("OwnedWarp", Codec.STRING), (teleporter, s) -> teleporter.ownedWarp = s, teleporter -> teleporter.ownedWarp)
      .add()
      .append(new KeyedCodec<>("IsCustomName", Codec.BOOLEAN), (teleporter, s) -> teleporter.isCustomName = s, teleporter -> teleporter.isCustomName)
      .add()
      .<String>append(
         new KeyedCodec<>("WarpNameWordList", Codec.STRING),
         (teleporter, s) -> teleporter.warpNameWordListKey = s,
         teleporter -> teleporter.warpNameWordListKey
      )
      .documentation("The ID of the Word list to select default warp names from")
      .add()
      .build();
   public static final String ACTIVATE_STATE = "Active";
   public static final String INACTIVE_STATE = "default";
   @Nullable
   private UUID worldUuid;
   @Nullable
   private Transform transform;
   private byte relativeMask = 0;
   @Nullable
   private String warp;
   @Deprecated
   @Nullable
   private String ownedWarp;
   private boolean isCustomName;
   private String warpNameWordListKey;

   public static ComponentType<ChunkStore, Teleporter> getComponentType() {
      return TeleporterPlugin.get().getTeleporterComponentType();
   }

   @Nullable
   public UUID getWorldUuid() {
      return this.worldUuid;
   }

   public void setWorldUuid(@Nullable UUID worldUuid) {
      this.worldUuid = worldUuid;
   }

   @Nullable
   public Transform getTransform() {
      return this.transform;
   }

   public void setTransform(@Nullable Transform transform) {
      this.transform = transform;
   }

   public byte getRelativeMask() {
      return this.relativeMask;
   }

   public void setRelativeMask(byte relativeMask) {
      this.relativeMask = relativeMask;
   }

   @Nullable
   public String getWarp() {
      return this.warp;
   }

   public void setWarp(@Nullable String warp) {
      this.warp = warp != null && !warp.isEmpty() ? warp : null;
   }

   @Nullable
   public String getOwnedWarp() {
      return this.ownedWarp;
   }

   public void setOwnedWarp(@Nullable String ownedWarp) {
      this.ownedWarp = ownedWarp;
   }

   public boolean hasOwnedWarp() {
      return this.ownedWarp != null && !this.ownedWarp.isEmpty();
   }

   public void setWarpNameWordListKey(String warpNameWordListKey) {
      this.warpNameWordListKey = warpNameWordListKey;
   }

   public boolean isCustomName() {
      return this.isCustomName;
   }

   public void setIsCustomName(boolean customName) {
      this.isCustomName = customName;
   }

   @Nullable
   public String getWarpNameWordListKey() {
      return this.warpNameWordListKey;
   }

   @Nullable
   public WordList getWarpNameWordList() {
      return WordList.getWordList(this.warpNameWordListKey);
   }

   public boolean isValid() {
      if (this.warp != null && !this.warp.isEmpty()) {
         return TeleportPlugin.get().getWarps().get(this.warp.toLowerCase()) != null;
      } else if (this.transform != null) {
         return this.worldUuid != null ? Universe.get().getWorld(this.worldUuid) != null : true;
      } else {
         return false;
      }
   }

   @Nonnull
   @Override
   public Component<ChunkStore> clone() {
      Teleporter teleporter = new Teleporter();
      teleporter.worldUuid = this.worldUuid;
      teleporter.transform = this.transform != null ? this.transform.clone() : null;
      teleporter.relativeMask = this.relativeMask;
      teleporter.warp = this.warp;
      teleporter.ownedWarp = this.ownedWarp;
      teleporter.isCustomName = this.isCustomName;
      teleporter.warpNameWordListKey = this.warpNameWordListKey;
      return teleporter;
   }

   @Nullable
   public Teleport toTeleport(@Nonnull Vector3d currentPosition, @Nonnull Vector3f currentRotation, @Nonnull Vector3i blockPosition) {
      if (this.warp != null && !this.warp.isEmpty()) {
         Warp targetWarp = TeleportPlugin.get().getWarps().get(this.warp.toLowerCase());
         return targetWarp != null ? targetWarp.toTeleport() : null;
      } else if (this.transform != null) {
         if (this.worldUuid != null) {
            World world = Universe.get().getWorld(this.worldUuid);
            if (world != null) {
               if (this.relativeMask != 0) {
                  Transform teleportTransform = this.transform.clone();
                  Transform.applyMaskedRelativeTransform(teleportTransform, this.relativeMask, currentPosition, currentRotation, blockPosition);
                  return Teleport.createForPlayer(world, teleportTransform);
               }

               return Teleport.createForPlayer(world, this.transform);
            }
         }

         if (this.relativeMask != 0) {
            Transform teleportTransform = this.transform.clone();
            Transform.applyMaskedRelativeTransform(teleportTransform, this.relativeMask, currentPosition, currentRotation, blockPosition);
            return Teleport.createForPlayer(teleportTransform);
         } else {
            return Teleport.createForPlayer(this.transform);
         }
      } else {
         return null;
      }
   }
}
