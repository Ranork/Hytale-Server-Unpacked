package com.hypixel.hytale.builtin.adventure.objectives.markers.reachlocation;

import com.hypixel.hytale.builtin.adventure.objectives.ObjectivePlugin;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ReachLocationMarker implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<ReachLocationMarker> CODEC = BuilderCodec.builder(ReachLocationMarker.class, ReachLocationMarker::new)
      .append(
         new KeyedCodec<>("MarkerId", Codec.STRING),
         (reachLocationMarkerEntity, uuid) -> reachLocationMarkerEntity.markerId = uuid,
         reachLocationMarkerEntity -> reachLocationMarkerEntity.markerId
      )
      .addValidator(Validators.nonNull())
      .add()
      .build();
   private String markerId;
   @Nonnull
   private final Set<UUID> players = new HashSet<>();

   public static ComponentType<EntityStore, ReachLocationMarker> getComponentType() {
      return ObjectivePlugin.get().getReachLocationMarkerComponentType();
   }

   public ReachLocationMarker() {
   }

   public ReachLocationMarker(String markerId) {
      this.markerId = markerId;
   }

   public String getMarkerId() {
      return this.markerId;
   }

   @Nullable
   public String getLocationName() {
      ReachLocationMarkerAsset asset = ReachLocationMarkerAsset.getAssetMap().getAsset(this.markerId);
      return asset != null ? asset.getName() : null;
   }

   @Nonnull
   public Set<UUID> getPlayers() {
      return this.players;
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      ReachLocationMarker marker = new ReachLocationMarker(this.markerId);
      marker.players.addAll(this.players);
      return marker;
   }
}
