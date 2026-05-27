package com.hypixel.hytale.server.core.modules.physics.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class Velocity implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<Velocity> CODEC = BuilderCodec.builder(Velocity.class, Velocity::new)
      .append(new KeyedCodec<>("Velocity", Vector3dUtil.CODEC), (entity, o) -> entity.velocity.set(o), entity -> entity.velocity)
      .add()
      .build();
   @Nonnull
   protected final List<Velocity.Instruction> instructions = new ObjectArrayList();
   @Nonnull
   protected final Vector3d velocity = new Vector3d();
   @Nonnull
   protected final Vector3d clientVelocity = new Vector3d();

   @Nonnull
   public static ComponentType<EntityStore, Velocity> getComponentType() {
      return EntityModule.get().getVelocityComponentType();
   }

   public Velocity() {
   }

   public Velocity(@Nonnull Velocity other) {
      this(new Vector3d(other.velocity));
   }

   public Velocity(@Nonnull Vector3d initialVelocity) {
      this.velocity.set(initialVelocity);
   }

   public void setZero() {
      this.set(0.0, 0.0, 0.0);
   }

   public void addVelocity(@Nonnull Vector3d velocityDelta) {
      this.velocity.add(velocityDelta);
   }

   public void addVelocity(double x, double y, double z) {
      this.velocity.add(x, y, z);
   }

   public void set(@Nonnull Vector3d newVelocity) {
      this.set(newVelocity.x(), newVelocity.y(), newVelocity.z());
   }

   public void set(double x, double y, double z) {
      this.velocity.set(x, y, z);
   }

   public void setClient(@Nonnull Vector3d newVelocity) {
      this.setClient(newVelocity.x(), newVelocity.y(), newVelocity.z());
   }

   public void setClient(double x, double y, double z) {
      this.clientVelocity.set(x, y, z);
   }

   public void setX(double x) {
      this.velocity.x = x;
   }

   public void setY(double y) {
      this.velocity.y = y;
   }

   public void setZ(double z) {
      this.velocity.z = z;
   }

   public double getX() {
      return this.velocity.x();
   }

   public double getY() {
      return this.velocity.y();
   }

   public double getZ() {
      return this.velocity.z();
   }

   public double getSpeed() {
      return this.velocity.length();
   }

   public void addInstruction(@Nonnull Vector3d velocity, @Nullable VelocityConfig config, @Nonnull ChangeVelocityType type) {
      this.instructions.add(new Velocity.Instruction(velocity, config, type));
   }

   @Nonnull
   public List<Velocity.Instruction> getInstructions() {
      return this.instructions;
   }

   @Nonnull
   public Vector3d getVelocity() {
      return this.velocity;
   }

   @Nonnull
   public Vector3d getClientVelocity() {
      return this.clientVelocity;
   }

   @Nonnull
   public Vector3d assignVelocityTo(@Nonnull Vector3d vector) {
      return vector.set(this.velocity);
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new Velocity(this);
   }

   public static final class Instruction {
      @Nonnull
      private final Vector3d velocity;
      @Nullable
      private final VelocityConfig config;
      @Nonnull
      private final ChangeVelocityType type;

      public Instruction(@Nonnull Vector3d velocity, @Nullable VelocityConfig config, @Nonnull ChangeVelocityType type) {
         this.velocity = velocity;
         this.config = config;
         this.type = type;
      }

      @Nonnull
      public Vector3d getVelocity() {
         return this.velocity;
      }

      @Nullable
      public VelocityConfig getConfig() {
         return this.config;
      }

      @Nonnull
      public ChangeVelocityType getType() {
         return this.type;
      }
   }
}
