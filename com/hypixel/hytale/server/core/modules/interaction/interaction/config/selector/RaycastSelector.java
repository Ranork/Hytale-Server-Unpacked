package com.hypixel.hytale.server.core.modules.interaction.interaction.config.selector;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.function.consumer.TriIntConsumer;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector2d;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.Vector4d;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.protocol.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.collision.CollisionMath;
import com.hypixel.hytale.server.core.modules.debug.DebugUtils;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.none.SelectInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RaycastSelector extends SelectorType {
   public static final BuilderCodec<RaycastSelector> CODEC = BuilderCodec.builder(RaycastSelector.class, RaycastSelector::new, BASE_CODEC)
      .appendInherited(new KeyedCodec<>("Offset", Vector3d.CODEC), (o, i) -> o.offset = i, o -> o.offset, (o, p) -> o.offset = p.offset)
      .documentation("The offset of the area to search for targets in.")
      .add()
      .<Integer>appendInherited(new KeyedCodec<>("Distance", Codec.INTEGER), (o, i) -> o.distance = i, o -> o.distance, (o, p) -> o.distance = p.distance)
      .documentation("The max search distance for the raycast")
      .add()
      .appendInherited(
         new KeyedCodec<>("IgnoreFluids", Codec.BOOLEAN), (o, i) -> o.ignoreFluids = i, o -> o.ignoreFluids, (o, p) -> o.ignoreFluids = p.ignoreFluids
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("IgnoreEmptyCollisionMaterial", Codec.BOOLEAN),
         (o, i) -> o.ignoreEmptyCollisionMaterial = i,
         o -> o.ignoreEmptyCollisionMaterial,
         (o, p) -> o.ignoreEmptyCollisionMaterial = p.ignoreEmptyCollisionMaterial
      )
      .add()
      .<String>appendInherited(new KeyedCodec<>("BlockTag", Codec.STRING), (o, i) -> o.blockTag = i, o -> o.blockTag, (o, p) -> o.blockTag = p.blockTag)
      .documentation("The required tag for the block to have to match for the raycast to hit them")
      .add()
      .afterDecode(o -> {
         if (o.blockTag != null) {
            o.blockTagIndex = AssetRegistry.getOrCreateTagIndex(o.blockTag);
         }
      })
      .build();
   protected Vector3d offset = Vector3d.ZERO;
   protected int distance = 30;
   protected boolean ignoreFluids = false;
   protected boolean ignoreEmptyCollisionMaterial = false;
   @Nullable
   protected String blockTag;
   protected int blockTagIndex = Integer.MIN_VALUE;

   @Nonnull
   @Override
   public Selector newSelector() {
      return new RaycastSelector.RuntimeSelector();
   }

   @Nonnull
   public Vector3f getOffset() {
      return new Vector3f((float)this.offset.x, (float)this.offset.y, (float)this.offset.z);
   }

   @Nonnull
   public Vector3d selectTargetPosition(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> attacker) {
      TransformComponent transformComponent = commandBuffer.getComponent(attacker, TransformComponent.getComponentType());
      Vector3d position = transformComponent.getPosition();
      if (this.offset.x != 0.0 || this.offset.y != 0.0 || this.offset.z != 0.0) {
         position = this.offset.clone();
         HeadRotation headRotation = commandBuffer.getComponent(attacker, HeadRotation.getComponentType());
         position.rotateY(headRotation.getRotation().getYaw());
         position.add(transformComponent.getPosition());
      }

      return position;
   }

   public com.hypixel.hytale.protocol.Selector toPacket() {
      return new com.hypixel.hytale.protocol.RaycastSelector(
         this.getOffset(), this.distance, this.blockTagIndex, this.ignoreFluids, this.ignoreEmptyCollisionMaterial
      );
   }

   private static class Result {
      public Ref<EntityStore> match;
      public double distance = Double.MAX_VALUE;
      @Nonnull
      public Vector4d hitPosition = new Vector4d();
   }

   private class RuntimeSelector implements Selector {
      private final RaycastSelector.Result bestMatch = new RaycastSelector.Result();
      private final Vector2d minMax = new Vector2d();
      @Nullable
      private Vector3i blockPosition;

      @Override
      public void tick(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> ref, float time, float runTime) {
         Vector3d position = RaycastSelector.this.selectTargetPosition(commandBuffer, ref);
         HeadRotation headRotation = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
         Vector3d direction = new Vector3d(headRotation.getRotation().getYaw(), headRotation.getRotation().getPitch());
         IntSet blockTags = RaycastSelector.this.blockTag == null ? null : BlockType.getAssetMap().getIndexesForTag(RaycastSelector.this.blockTagIndex);
         if (SelectInteraction.SHOW_VISUAL_DEBUG) {
            Vector3d dir = direction.clone().scale(RaycastSelector.this.distance);
            com.hypixel.hytale.math.vector.Vector3f color = new com.hypixel.hytale.math.vector.Vector3f(
               (float)HashUtil.random(ref.getIndex(), this.hashCode(), 10L),
               (float)HashUtil.random(ref.getIndex(), this.hashCode(), 11L),
               (float)HashUtil.random(ref.getIndex(), this.hashCode(), 12L)
            );
            DebugUtils.addArrow(commandBuffer.getExternalData().getWorld(), position, dir, color, 5.0F, true);
         }

         this.blockPosition = TargetUtil.getTargetBlock(commandBuffer.getExternalData().getWorld(), (id, fluidId) -> {
            if (id == 0) {
               return false;
            } else {
               if (RaycastSelector.this.ignoreFluids || RaycastSelector.this.ignoreEmptyCollisionMaterial) {
                  BlockType blockType = BlockType.getAssetMap().getAsset(id);
                  if (RaycastSelector.this.ignoreFluids && blockType.getMaterial() == BlockMaterial.Empty && fluidId != 0) {
                     return false;
                  }

                  if (RaycastSelector.this.ignoreEmptyCollisionMaterial && blockType.getMaterial() == BlockMaterial.Empty) {
                     return false;
                  }
               }

               return blockTags == null || blockTags.contains(id);
            }
         }, position.x, position.y, position.z, direction.x, direction.y, direction.z, RaycastSelector.this.distance);
         Vector3d searchPosition = new Vector3d(
            position.x + direction.x * RaycastSelector.this.distance * 0.5,
            position.y + direction.y * RaycastSelector.this.distance * 0.5,
            position.z + direction.z * RaycastSelector.this.distance * 0.5
         );
         Selector.selectNearbyEntities(commandBuffer, searchPosition, RaycastSelector.this.distance * 0.6, entityRef -> {
            BoundingBox boundingBox = commandBuffer.getComponent(entityRef, BoundingBox.getComponentType());
            if (boundingBox != null) {
               TransformComponent transform = commandBuffer.getComponent(entityRef, TransformComponent.getComponentType());
               Vector3d ePos = transform.getPosition();
               if (CollisionMath.intersectRayAABB(position, direction, ePos.getX(), ePos.getY(), ePos.getZ(), boundingBox.getBoundingBox(), this.minMax)) {
                  double hitPosX = position.x + direction.x * this.minMax.x;
                  double hitPosY = position.y + direction.y * this.minMax.x;
                  double hitPosZ = position.z + direction.z * this.minMax.x;
                  double matchDistance = position.distanceSquaredTo(hitPosX, hitPosY, hitPosZ);
                  if (!(matchDistance >= this.bestMatch.distance)) {
                     this.bestMatch.match = entityRef;
                     this.bestMatch.distance = matchDistance;
                     this.bestMatch.hitPosition.assign(hitPosX, hitPosY, hitPosZ, 0.0);
                  }
               }
            }
         }, e -> !e.equals(ref));
         if (this.bestMatch.match != null && this.blockPosition != null) {
            double blockDistance = position.distanceSquaredTo(this.blockPosition.x + 0.5, this.blockPosition.y + 0.5, this.blockPosition.z + 0.5);
            if (!(blockDistance < this.bestMatch.distance)) {
               this.blockPosition = null;
            }
         }
      }

      @Override
      public void selectTargetEntities(
         @Nonnull CommandBuffer<EntityStore> commandBuffer,
         @Nonnull Ref<EntityStore> attacker,
         @Nonnull BiConsumer<Ref<EntityStore>, Vector4d> consumer,
         Predicate<Ref<EntityStore>> filter
      ) {
         if (this.blockPosition == null && this.bestMatch.match != null) {
            if (this.bestMatch.match.isValid()) {
               consumer.accept(this.bestMatch.match, this.bestMatch.hitPosition);
            }
         }
      }

      @Override
      public void selectTargetBlocks(@Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Ref<EntityStore> attacker, @Nonnull TriIntConsumer consumer) {
         if (this.blockPosition != null) {
            consumer.accept(this.blockPosition.x, this.blockPosition.y, this.blockPosition.z);
         }
      }
   }
}
