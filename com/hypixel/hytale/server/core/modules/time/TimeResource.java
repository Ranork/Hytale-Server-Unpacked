package com.hypixel.hytale.server.core.modules.time;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.TemporalUnit;
import javax.annotation.Nonnull;

public class TimeResource implements Resource<EntityStore> {
   @Nonnull
   public static final BuilderCodec<TimeResource> CODEC = BuilderCodec.builder(TimeResource.class, TimeResource::new)
      .append(new KeyedCodec<>("Now", Codec.INSTANT), (o, now) -> o.now = now, o -> o.now)
      .documentation("Now. The current instant of time.")
      .add()
      .build();
   @Nonnull
   private Instant now;
   private float timeDilationModifier = 1.0F;

   @Nonnull
   public static ResourceType<EntityStore, TimeResource> getResourceType() {
      return TimeModule.get().getTimeResourceType();
   }

   public TimeResource() {
      this(Instant.EPOCH);
   }

   public TimeResource(@Nonnull Instant now) {
      this.now = now;
   }

   public float getTimeDilationModifier() {
      return this.timeDilationModifier;
   }

   public void setTimeDilationModifier(float timeDilationModifier) {
      this.timeDilationModifier = timeDilationModifier;
   }

   @Nonnull
   public Instant getNow() {
      return this.now;
   }

   public void setNow(@Nonnull Instant now) {
      this.now = now;
   }

   public void add(@Nonnull Duration duration) {
      this.now = this.now.plus(duration);
   }

   public void add(long time, @Nonnull TemporalUnit unit) {
      this.now = this.now.plus(time, unit);
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new TimeResource(this.now);
   }

   @Nonnull
   @Override
   public String toString() {
      return "TimeResource{now=" + this.now + "}";
   }
}
