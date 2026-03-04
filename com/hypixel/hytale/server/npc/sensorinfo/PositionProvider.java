package com.hypixel.hytale.server.npc.sensorinfo;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.sensorinfo.parameterproviders.ParameterProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionProvider extends InfoProviderBase implements IPositionProvider {
   protected double x = 2.147483647E9;
   protected double y = 2.147483647E9;
   protected double z = 2.147483647E9;
   protected boolean isValid;

   public PositionProvider() {
   }

   public PositionProvider(ParameterProvider parameterProvider) {
      super(parameterProvider);
   }

   public PositionProvider(ParameterProvider parameterProvider, ExtraInfoProvider... providers) {
      super(parameterProvider, providers);
   }

   @Override
   public void clear() {
      this.x = 2.147483647E9;
      this.y = 2.147483647E9;
      this.z = 2.147483647E9;
      this.isValid = false;
   }

   @Nullable
   public Ref<EntityStore> setTarget(@Nullable Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      if (ref == null) {
         return null;
      } else if (ref.isValid() && !componentAccessor.getArchetype(ref).contains(DeathComponent.getComponentType())) {
         TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

         assert transformComponent != null;

         this.setTarget(transformComponent.getPosition());
         return ref;
      } else {
         this.clear();
         return null;
      }
   }

   public void setTarget(@Nonnull Vector3d pos) {
      this.setTarget(pos.x, pos.y, pos.z);
   }

   public void setTarget(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.isValid = true;
   }

   @Override
   public boolean providePosition(@Nonnull Vector3d result) {
      if (!this.hasPosition()) {
         return false;
      } else {
         result.assign(this.x, this.y, this.z);
         return true;
      }
   }

   @Override
   public double getX() {
      return this.x;
   }

   @Override
   public double getY() {
      return this.y;
   }

   @Override
   public double getZ() {
      return this.z;
   }

   @Nonnull
   @Override
   public IPositionProvider getPositionProvider() {
      return this;
   }

   @Override
   public boolean hasPosition() {
      return this.isValid;
   }

   @Nullable
   @Override
   public Ref<EntityStore> getTarget() {
      return null;
   }
}
