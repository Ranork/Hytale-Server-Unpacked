package com.hypixel.hytale.server.core.entity.entities.player.data;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public final class PlayerRespawnPointData {
   @Nonnull
   public static final BuilderCodec<PlayerRespawnPointData> CODEC = BuilderCodec.builder(PlayerRespawnPointData.class, PlayerRespawnPointData::new)
      .append(
         new KeyedCodec<>("BlockPosition", Vector3i.CODEC),
         (respawnPointData, vector3i) -> respawnPointData.blockPosition = vector3i,
         respawnPointData -> respawnPointData.blockPosition
      )
      .documentation("The position of the respawn block.")
      .add()
      .<Vector3d>append(
         new KeyedCodec<>("RespawnPosition", Vector3d.CODEC),
         (respawnPointData, vector3f) -> respawnPointData.respawnPosition = vector3f,
         respawnPointData -> respawnPointData.respawnPosition
      )
      .documentation("The position at which the player will respawn.")
      .add()
      .<String>append(new KeyedCodec<>("Name", Codec.STRING), (respawnPointData, s) -> respawnPointData.name = s, respawnPointData -> respawnPointData.name)
      .documentation("The name of the respawn point.")
      .add()
      .build();
   private Vector3i blockPosition;
   private Vector3d respawnPosition;
   private String name;

   public PlayerRespawnPointData(@Nonnull Vector3i blockPosition, @Nonnull Vector3d respawnPosition, @Nonnull String name) {
      this.blockPosition = blockPosition;
      this.respawnPosition = respawnPosition;
      this.name = name;
   }

   private PlayerRespawnPointData() {
   }

   public Vector3i getBlockPosition() {
      return this.blockPosition;
   }

   public Vector3d getRespawnPosition() {
      return this.respawnPosition;
   }

   public String getName() {
      return this.name;
   }

   public void setName(@Nonnull String name) {
      this.name = name;
   }
}
