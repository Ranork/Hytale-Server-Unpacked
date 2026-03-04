package com.hypixel.hytale.builtin.mounts.interactions;

import com.hypixel.hytale.builtin.mounts.minecart.MinecartComponent;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.RailConfig;
import com.hypixel.hytale.protocol.RailPoint;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.Interactable;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentModel;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.Interactions;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpawnMinecartInteraction extends SimpleBlockInteraction {
   public static final BuilderCodec<SpawnMinecartInteraction> CODEC = BuilderCodec.builder(
         SpawnMinecartInteraction.class, SpawnMinecartInteraction::new, SimpleBlockInteraction.CODEC
      )
      .documentation("Spawns a minecart at the target block")
      .<String>appendInherited(new KeyedCodec<>("Model", Codec.STRING), (o, v) -> o.modelId = v, o -> o.modelId, (o, p) -> o.modelId = p.modelId)
      .addValidator(ModelAsset.VALIDATOR_CACHE.getValidator())
      .add()
      .<Map<InteractionType, String>>appendInherited(
         new KeyedCodec<>("CartInteractions", new EnumMapCodec<>(InteractionType.class, RootInteraction.CHILD_ASSET_CODEC)),
         (o, v) -> o.cartInteractions = v,
         o -> o.cartInteractions,
         (o, p) -> o.cartInteractions = p.cartInteractions
      )
      .addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getMapValueValidator().late())
      .add()
      .build();
   private String modelId;
   private Map<InteractionType, String> cartInteractions = Collections.emptyMap();

   @Override
   protected void interactWithBlock(
      @Nonnull World world,
      @Nonnull CommandBuffer<EntityStore> commandBuffer,
      @Nonnull InteractionType type,
      @Nonnull InteractionContext context,
      @Nullable ItemStack itemInHand,
      @Nonnull Vector3i targetBlock,
      @Nonnull CooldownHandler cooldownHandler
   ) {
      Ref<EntityStore> ref = context.getEntity();
      Holder<EntityStore> holder = EntityStore.REGISTRY.newHolder();
      Vector3d targetPosition = targetBlock.toVector3d();
      targetPosition.add(0.5, 0.5, 0.5);
      Vector3f rotation = new Vector3f();
      HeadRotation headRotation = commandBuffer.getComponent(ref, HeadRotation.getComponentType());
      if (headRotation != null) {
         rotation.setYaw(headRotation.getRotation().getYaw());
      }

      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
      if (chunk != null) {
         BlockType block = chunk.getBlockType(targetBlock);
         int blockRotation = chunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
         RailConfig railConfig = block.getRailConfig(blockRotation);
         if (railConfig != null) {
            alignToRail(targetBlock, targetPosition, rotation, rotation.getYaw(), railConfig);
         } else {
            BlockBoundingBoxes.RotatedVariantBoxes bounding = BlockBoundingBoxes.getAssetMap().getAsset(block.getHitboxTypeIndex()).get(blockRotation);
            targetPosition.add(0.0, bounding.getBoundingBox().max.y - 0.5, 0.0);
         }

         holder.addComponent(TransformComponent.getComponentType(), new TransformComponent(targetPosition, rotation));
         holder.ensureComponent(UUIDComponent.getComponentType());
         ModelAsset modelAsset = ModelAsset.getAssetMap().getAsset(this.modelId);
         if (modelAsset == null) {
            modelAsset = ModelAsset.DEBUG;
         }

         Model model = Model.createRandomScaleModel(modelAsset);
         holder.addComponent(PersistentModel.getComponentType(), new PersistentModel(model.toReference()));
         holder.addComponent(ModelComponent.getComponentType(), new ModelComponent(model));
         holder.addComponent(BoundingBox.getComponentType(), new BoundingBox(model.getBoundingBox()));
         holder.ensureComponent(Interactable.getComponentType());
         holder.addComponent(Interactions.getComponentType(), new Interactions(this.cartInteractions));
         holder.putComponent(
            MinecartComponent.getComponentType(), new MinecartComponent(context.getHeldItem() != null ? context.getHeldItem().getItemId() : null)
         );
         commandBuffer.addEntity(holder, AddReason.SPAWN);
      }
   }

   @Override
   protected void simulateInteractWithBlock(
      @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nullable ItemStack itemInHand, @Nonnull World world, @Nonnull Vector3i targetBlock
   ) {
   }

   private static void alignToRail(@Nonnull Vector3i targetBlock, @Nonnull Vector3d target, @Nonnull Vector3f rotation, float yaw, @Nonnull RailConfig config) {
      RailPoint[] points = config.points;
      double smallestDistance = Double.MAX_VALUE;
      double ox = target.x;
      double oy = target.y;
      double oz = target.z;
      Vector3d facingDir = new Vector3d();
      facingDir.assign(yaw, 0.0);

      for (int index = 0; index < points.length - 1; index++) {
         RailPoint p = points[index];
         RailPoint p2 = points[index + 1];
         Vector3d point = new Vector3d(targetBlock.x + p.point.x, targetBlock.y + p.point.y, targetBlock.z + p.point.z);
         Vector3d point2 = new Vector3d(targetBlock.x + p2.point.x, targetBlock.y + p2.point.y, targetBlock.z + p2.point.z);
         Vector3d dir = point2.clone().subtract(point);
         double maxLength = dir.length();
         dir.normalize();
         Vector3d toPoint = target.clone().subtract(point);
         double distance = dir.dot(toPoint);
         Vector3d pointOnLine = point.clone();
         pointOnLine.addScaled(dir, Math.min(maxLength, Math.max(0.0, distance)));
         double pointDist = pointOnLine.distanceSquaredTo(target);
         if (pointDist >= 0.0 && pointDist <= 0.8F && pointDist < smallestDistance) {
            ox = pointOnLine.x;
            oy = pointOnLine.y;
            oz = pointOnLine.z;
            smallestDistance = pointDist;
            if (facingDir.dot(dir) < 0.0) {
               dir.scale(-1.0);
            }

            float newYaw = (float)(Math.atan2(dir.x, dir.z) + Math.PI);
            float newPitch = (float)Math.asin(dir.y);
            rotation.setYaw(newYaw);
            rotation.setPitch(newPitch);
         }
      }

      if (!(smallestDistance >= Double.MAX_VALUE)) {
         target.assign(ox, oy, oz);
      }
   }
}
