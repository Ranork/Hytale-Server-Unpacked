package com.hypixel.hytale.server.core.entity.entities.player.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.math.vector.Transform;
import javax.annotation.Nonnull;

public final class PlayerDeathPositionData {
   @Nonnull
   public static final BuilderCodec<PlayerDeathPositionData> CODEC = BuilderCodec.builder(PlayerDeathPositionData.class, PlayerDeathPositionData::new)
      .append(new KeyedCodec<>("MarkerId", Codec.STRING), (data, value) -> data.markerId = value, data -> data.markerId)
      .documentation("The unique ID of the associated map marker.")
      .add()
      .<Transform>append(new KeyedCodec<>("Transform", Transform.CODEC), (data, value) -> data.transform = value, data -> data.transform)
      .documentation("The transform of this death position.")
      .add()
      .<Integer>append(new KeyedCodec<>("Day", Codec.INTEGER), (data, value) -> data.day = value, data -> data.day)
      .documentation("The in-game day in which the player died.")
      .add()
      .build();
   @Nonnull
   public static final ArrayCodec<PlayerDeathPositionData> ARRAY_CODEC = new ArrayCodec<>(CODEC, PlayerDeathPositionData[]::new);
   private String markerId;
   private Transform transform;
   private int day;

   private PlayerDeathPositionData() {
   }

   public PlayerDeathPositionData(@Nonnull String markerId, @Nonnull Transform transform, int day) {
      this.markerId = markerId;
      this.transform = transform;
      this.day = day;
   }

   public String getMarkerId() {
      return this.markerId;
   }

   public Transform getTransform() {
      return this.transform;
   }

   public int getDay() {
      return this.day;
   }
}
