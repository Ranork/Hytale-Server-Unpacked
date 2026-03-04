package com.hypixel.hytale.builtin.teleport;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.function.BsonFunctionCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import java.time.Instant;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public class Warp {
   public static final Codec<Warp> CODEC = new BsonFunctionCodec<>(
      BuilderCodec.builder(Warp.class, Warp::new)
         .addField(new KeyedCodec<>("Id", Codec.STRING), (warp, s) -> warp.id = s, warp -> warp.id)
         .addField(new KeyedCodec<>("World", Codec.STRING), (warp, s) -> warp.world = s, warp -> warp.world)
         .addField(new KeyedCodec<>("Creator", Codec.STRING), (warp, s) -> warp.creator = s, warp -> warp.creator)
         .<Long>append(new KeyedCodec<>("Date", Codec.LONG), (warp, s) -> warp.creationDate = Instant.ofEpochMilli(s), warp -> null)
         .addValidator(Validators.deprecated())
         .add()
         .append(new KeyedCodec<>("CreationDate", Codec.INSTANT), (o, i) -> o.creationDate = i, o -> o.creationDate)
         .add()
         .build(),
      (warp, bsonValue) -> {
         warp.transform = Transform.CODEC.decode(bsonValue);
         return warp;
      },
      (bsonValue, warp) -> {
         bsonValue.asDocument().putAll(Transform.CODEC.encode(warp.transform).asDocument());
         return bsonValue;
      }
   );
   public static final ArrayCodec<Warp> ARRAY_CODEC = new ArrayCodec<>(CODEC, Warp[]::new);
   private String id;
   private String world;
   @Nullable
   private Transform transform;
   private String creator;
   private Instant creationDate;

   public Warp() {
   }

   public Warp(@Nonnull Transform transform, @Nonnull String id, @Nonnull World world, @Nonnull String creator, @Nonnull Instant creationDate) {
      this.id = id;
      this.world = world.getName();
      this.transform = transform;
      this.creator = creator;
      this.creationDate = creationDate;
   }

   public String getId() {
      return this.id;
   }

   public String getWorld() {
      return this.world;
   }

   @Nullable
   public Transform getTransform() {
      return this.transform;
   }

   public String getCreator() {
      return this.creator;
   }

   public Instant getCreationDate() {
      return this.creationDate;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Warp warp = (Warp)o;
         if (!Objects.equals(this.id, warp.id)) {
            return false;
         } else if (!Objects.equals(this.world, warp.world)) {
            return false;
         } else if (!Objects.equals(this.transform, warp.transform)) {
            return false;
         } else {
            return !Objects.equals(this.creator, warp.creator) ? false : Objects.equals(this.creationDate, warp.creationDate);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.id != null ? this.id.hashCode() : 0;
      result = 31 * result + (this.world != null ? this.world.hashCode() : 0);
      result = 31 * result + (this.transform != null ? this.transform.hashCode() : 0);
      result = 31 * result + (this.creator != null ? this.creator.hashCode() : 0);
      return 31 * result + (this.creationDate != null ? this.creationDate.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Warp{id='" + this.id + "', transform=" + this.transform + ", creator='" + this.creator + "', creationDate=" + this.creationDate + "}";
   }

   @Nullable
   public Teleport toTeleport() {
      World worldInstance = Universe.get().getWorld(this.world);
      return worldInstance == null ? null : Teleport.createForPlayer(worldInstance, this.transform);
   }
}
