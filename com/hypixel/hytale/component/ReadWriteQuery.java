package com.hypixel.hytale.component;

import com.hypixel.hytale.component.query.ReadWriteArchetypeQuery;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ReadWriteQuery<ECS_TYPE> implements ReadWriteArchetypeQuery<ECS_TYPE> {
   @Nonnull
   private final Archetype<ECS_TYPE> read;
   @Nonnull
   private final Archetype<ECS_TYPE> write;

   public ReadWriteQuery(@Nonnull Archetype<ECS_TYPE> read, @Nonnull Archetype<ECS_TYPE> write) {
      this.read = read;
      this.write = write;
      Objects.requireNonNull(read, "Read archetype for ReadWriteQuery cannot be null");
      Objects.requireNonNull(write, "Write archetype for ReadWriteQuery cannot be null");
   }

   @Override
   public Archetype<ECS_TYPE> getReadArchetype() {
      return this.read;
   }

   @Override
   public Archetype<ECS_TYPE> getWriteArchetype() {
      return this.write;
   }
}
