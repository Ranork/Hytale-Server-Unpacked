package com.hypixel.hytale.server.core.modules.physics.component;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Velocity implements Component<EntityStore> {
   @Nonnull
   public static final BuilderCodec<Velocity> CODEC = BuilderCodec.builder(Velocity.class, Velocity::new)
      .append(new KeyedCodec<>("Velocity", Vector3d.CODEC), (entity, o) -> entity.velocity.assign(o), entity -> entity.velocity)
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
      this(other.velocity.clone());
   }

   public Velocity(@Nonnull Vector3d initialVelocity) {
      this.velocity.assign(initialVelocity);
   }

   public void setZero() {
      this.set(0.0, 0.0, 0.0);
   }

   public void addForce(@Nonnull Vector3d force) {
      this.velocity.add(force);
   }

   public void addForce(double x, double y, double z) {
      this.velocity.add(x, y, z);
   }

   public void set(@Nonnull Vector3d newVelocity) {
      this.set(newVelocity.getX(), newVelocity.getY(), newVelocity.getZ());
   }

   public void set(double x, double y, double z) {
      this.velocity.assign(x, y, z);
   }

   public void setClient(@Nonnull Vector3d newVelocity) {
      this.setClient(newVelocity.getX(), newVelocity.getY(), newVelocity.getZ());
   }

   public void setClient(double x, double y, double z) {
      this.clientVelocity.assign(x, y, z);
   }

   public void setX(double x) {
      this.velocity.setX(x);
   }

   public void setY(double y) {
      this.velocity.setY(y);
   }

   public void setZ(double z) {
      this.velocity.setZ(z);
   }

   public double getX() {
      return this.velocity.getX();
   }

   public double getY() {
      return this.velocity.getY();
   }

   public double getZ() {
      return this.velocity.getZ();
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
      return vector.assign(this.velocity);
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
