package com.hypixel.hytale.server.spawning.suppression;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;

public class SpawnSuppressorEntry {
   public static final BuilderCodec<SpawnSuppressorEntry> CODEC = BuilderCodec.builder(SpawnSuppressorEntry.class, SpawnSuppressorEntry::new)
      .append(new KeyedCodec<>("Position", Vector3d.CODEC), (entry, v) -> entry.position = v, entry -> entry.position)
      .add()
      .append(new KeyedCodec<>("Suppression", Codec.STRING), (entry, s) -> entry.suppressionId = s, entry -> entry.suppressionId)
      .add()
      .build();
   private String suppressionId;
   private Vector3d position;

   public SpawnSuppressorEntry(String suppressionId, Vector3d position) {
      this.suppressionId = suppressionId;
      this.position = position;
   }

   private SpawnSuppressorEntry() {
   }

   public Vector3d getPosition() {
      return this.position;
   }

   public String getSuppressionId() {
      return this.suppressionId;
   }
}
