package com.hypixel.hytale.server.core.universe.world.worldmap.markers.user;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.PlacedByMarkerComponent;
import com.hypixel.hytale.protocol.packets.worldmap.TintComponent;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import java.util.UUID;
import javax.annotation.Nullable;

public class UserMapMarker {
   public static final BuilderCodec<UserMapMarker> CODEC = BuilderCodec.builder(UserMapMarker.class, UserMapMarker::new)
      .append(new KeyedCodec<>("Id", Codec.STRING), (marker, o) -> marker.id = o, marker -> marker.id)
      .add()
      .append(new KeyedCodec<>("X", Codec.FLOAT), (marker, o) -> marker.x = o, marker -> marker.x)
      .add()
      .append(new KeyedCodec<>("Z", Codec.FLOAT), (marker, o) -> marker.z = o, marker -> marker.z)
      .add()
      .append(new KeyedCodec<>("Name", Codec.STRING), (marker, o) -> marker.name = o, marker -> marker.name)
      .add()
      .append(new KeyedCodec<>("Icon", Codec.STRING), (marker, o) -> marker.icon = o, marker -> marker.icon)
      .add()
      .append(new KeyedCodec<>("ColorTint", ProtocolCodecs.COLOR), (marker, o) -> marker.colorTint = o, marker -> marker.colorTint)
      .add()
      .append(new KeyedCodec<>("CreatedByUuid", Codec.UUID_BINARY), (marker, o) -> marker.createdByUuid = o, marker -> marker.createdByUuid)
      .add()
      .append(new KeyedCodec<>("CreatedByName", Codec.STRING), (marker, o) -> marker.createdByName = o, marker -> marker.createdByName)
      .add()
      .build();
   public static final ArrayCodec<UserMapMarker> ARRAY_CODEC = new ArrayCodec<>(CODEC, UserMapMarker[]::new);
   private String id;
   private float x;
   private float z;
   @Nullable
   private String name;
   private String icon;
   private Color colorTint;
   @Nullable
   private UUID createdByUuid;
   @Nullable
   private String createdByName;
   @Nullable
   private MapMarker cachedProto;

   public String getId() {
      return this.id;
   }

   public void setId(String id) {
      this.id = id;
   }

   public float getX() {
      return this.x;
   }

   public float getZ() {
      return this.z;
   }

   public void setPosition(float blockX, float blockZ) {
      this.x = blockX;
      this.z = blockZ;
      this.invalidateCachedProto();
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   public void setName(@Nullable String name) {
      this.name = name;
      this.invalidateCachedProto();
   }

   public String getIcon() {
      return this.icon;
   }

   public void setIcon(String icon) {
      this.icon = icon;
      this.invalidateCachedProto();
   }

   public Color getColorTint() {
      return this.colorTint;
   }

   public void setColorTint(Color colorTint) {
      this.colorTint = colorTint;
      this.invalidateCachedProto();
   }

   @Nullable
   public UUID getCreatedByUuid() {
      return this.createdByUuid;
   }

   public UserMapMarker withCreatedByUuid(@Nullable UUID uuid) {
      this.createdByUuid = uuid;
      this.invalidateCachedProto();
      return this;
   }

   @Nullable
   public String getCreatedByName() {
      return this.createdByName;
   }

   public UserMapMarker withCreatedByName(@Nullable String name) {
      this.createdByName = name;
      this.invalidateCachedProto();
      return this;
   }

   public MapMarker toProtocolMarker() {
      if (this.cachedProto == null) {
         MapMarkerBuilder builder = new MapMarkerBuilder(this.id, this.icon, new Transform(this.x, 100.0, this.z));
         if (this.name != null) {
            builder.withCustomName(this.name);
         }

         if (this.colorTint != null) {
            builder.withComponent(new TintComponent(this.colorTint));
         }

         if (this.createdByName != null) {
            builder.withComponent(new PlacedByMarkerComponent(Message.raw(this.createdByName).getFormattedMessage(), this.createdByUuid));
         }

         this.cachedProto = builder.build();
      }

      return this.cachedProto;
   }

   private void invalidateCachedProto() {
      this.cachedProto = null;
   }
}
