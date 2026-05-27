package com.hypixel.hytale.builtin.fallingblocks;

import com.hypixel.hytale.codec.lookup.Priority;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.fallingblocks.FallingBlockImpact;
import com.hypixel.hytale.server.core.entity.entities.BlockEntity;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.PhysicsValues;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class FallingBlocksPlugin extends JavaPlugin {
   private static FallingBlocksPlugin instance;
   private ComponentType<EntityStore, FallingBlock> fallingBlockComponentType;

   public static FallingBlocksPlugin get() {
      return instance;
   }

   public FallingBlocksPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.fallingBlockComponentType = entityStoreRegistry.registerComponent(FallingBlock.class, "FallingBlock", FallingBlock.CODEC);
      ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();
      ComponentType<EntityStore, HeadRotation> headRotationComponentType = HeadRotation.getComponentType();
      ComponentType<EntityStore, Velocity> velocityComponentType = Velocity.getComponentType();
      ComponentType<EntityStore, PhysicsValues> physicsValuesComponentType = PhysicsValues.getComponentType();
      ComponentType<EntityStore, BoundingBox> boundingBoxComponentType = EntityModule.get().getBoundingBoxComponentType();
      ComponentType<EntityStore, BlockEntity> blockEntityComponentType = BlockEntity.getComponentType();
      entityStoreRegistry.registerSystem(
         new FallingBlockTickingSystem(
            this.fallingBlockComponentType,
            transformComponentType,
            headRotationComponentType,
            velocityComponentType,
            physicsValuesComponentType,
            boundingBoxComponentType,
            blockEntityComponentType
         )
      );
      FallingBlockImpact.CODEC.register(Priority.DEFAULT, "Place", PlaceFallingBlockImpact.class, PlaceFallingBlockImpact.CODEC);
      FallingBlockImpact.CODEC.register("Break", BreakFallingBlockImpact.class, BreakFallingBlockImpact.CODEC);
      FallingBlockImpact.CODEC.register("Explode", ExplodeFallingBlockImpact.class, ExplodeFallingBlockImpact.CODEC);
   }

   public ComponentType<EntityStore, FallingBlock> getFallingBlockComponentType() {
      return this.fallingBlockComponentType;
   }
}
