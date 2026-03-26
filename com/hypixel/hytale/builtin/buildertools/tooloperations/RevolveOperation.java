package com.hypixel.hytale.builtin.buildertools.tooloperations;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.buildertools.BuilderToolOnUseInteraction;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.EntityTrackerSystems;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.OverridableChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class RevolveOperation extends ToolOperation {
   private Vector3f center;
   private BlockSelection currentSelection;
   private RevolveOperation.Sampling samplingMode = RevolveOperation.Sampling.Neighbor;
   private int copyCount = 6;
   private double stepRadians;
   private double stepDegrees;
   private int bufferX;
   private int bufferZ;
   private Material[][][] materialBuffer;
   private static final double DISTANCE_TO_NEXT_BLOCK = 0.33;
   private static final int REVOLVE_COPY_LIMIT = 1000000;
   private static final int MAX_ENTITIES = 1000;
   private static final int MIN_COPY_FULL = 10;
   private static final int MAX_COPY_FULL = 2000;
   private static final int REVOLVE_DENSITY = 12;
   private static final int[][] NEIGHBOR_OFFSETS_XZ = new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {-1, 1}, {1, -1}, {-1, -1}};

   public RevolveOperation(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Player player,
      @Nonnull BuilderToolOnUseInteraction packet,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super(ref, packet, componentAccessor);
      PlayerRef playerRefComponent = componentAccessor.getComponent(this.playerRef, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      BuilderToolsPlugin.BuilderState state = BuilderToolsPlugin.getState(player, playerRefComponent);
      BlockSelection selection = state.getSelection();
      if (selection == null) {
         BuilderToolsPlugin.sendFeedback(Message.translation("server.builderTools.noSelection"), player, NotificationStyle.Warning, componentAccessor);
      } else if (!selection.hasSelectionBounds()) {
         BuilderToolsPlugin.sendFeedback(Message.translation("server.builderTools.noSelectionBounds"), player, NotificationStyle.Warning, componentAccessor);
      } else {
         OverridableChunkAccessor accessor = this.edit.getAccessor();
         String fullRevolve = (String)this.args.tool().getOrDefault("aSampling", "neighbor");
         switch (fullRevolve) {
            case "reverse":
               this.samplingMode = RevolveOperation.Sampling.Reverse;
               break;
            case "none":
               this.samplingMode = RevolveOperation.Sampling.Disabled;
               break;
            default:
               this.samplingMode = RevolveOperation.Sampling.Neighbor;
         }

         this.copyCount = (Integer)this.args.tool().getOrDefault("bCount", 6);
         boolean fullRevolve = (Boolean)this.args.tool().getOrDefault("cFullRevolve", false);
         boolean useDistance = (Boolean)this.args.tool().getOrDefault("eUseDistance", false);
         boolean copyEntities = (Boolean)this.args.tool().getOrDefault("eCopyEntities", false);
         String customDistance = (String)this.args.tool().getOrDefault("dCenter", "target");
         byte min = -1;
         switch (customDistance.hashCode()) {
            case -985752863:
               if (customDistance.equals("player")) {
                  min = 0;
               }
            default:
               switch (min) {
                  case 0:
                     this.center = playerRefComponent.getTransform().getPosition().toVector3f().floor().add(0.5F, 0.5F, 0.5F);
                     break;
                  default:
                     this.center = new Vector3f(this.x + 0.5F, this.y + this.originOffsetY, this.z + 0.5F);
               }

               int customDistancex = 0;
               if (useDistance) {
                  customDistancex = (Integer)this.args.tool().getOrDefault("fDistance", 20);
               }

               Vector3i minx = Vector3i.min(selection.getSelectionMin(), selection.getSelectionMax());
               Vector3i max = Vector3i.max(selection.getSelectionMin(), selection.getSelectionMax());
               int xMin = minx.getX();
               int xMax = max.getX();
               int yMin = minx.getY();
               int yMax = max.getY();
               int zMin = minx.getZ();
               int zMax = max.getZ();
               this.currentSelection = new BlockSelection();
               this.bufferX = xMax - xMin + 1;
               int bufferY = yMax - yMin + 1;
               this.bufferZ = zMax - zMin + 1;
               this.currentSelection.setPosition(xMin, yMin, zMin);
               if (this.samplingMode.equals(RevolveOperation.Sampling.Neighbor)) {
                  this.materialBuffer = new Material[this.bufferX][bufferY][this.bufferZ];
               }

               int chunkXMin = xMin >> 5;
               int chunkXMax = xMax >> 5;
               int chunkZMin = zMin >> 5;
               int chunkZMax = zMax >> 5;

               for (int chunkX = chunkXMin; chunkX <= chunkXMax; chunkX++) {
                  for (int chunkZ = chunkZMin; chunkZ <= chunkZMax; chunkZ++) {
                     BlockAccessor chunk = accessor.getChunk(ChunkUtil.indexChunk(chunkX, chunkZ));
                     if (chunk != null) {
                        int localXMin = Math.max(xMin, chunkX << 5);
                        int localXMax = Math.min(xMax, (chunkX << 5) + 31);
                        int localZMin = Math.max(zMin, chunkZ << 5);
                        int localZMax = Math.min(zMax, (chunkZ << 5) + 31);

                        for (int x = localXMin; x <= localXMax; x++) {
                           for (int z = localZMin; z <= localZMax; z++) {
                              for (int y = yMax; y >= yMin; y--) {
                                 int block = chunk.getBlock(x, y, z);
                                 int rotation = chunk.getRotationIndex(x, y, z);
                                 int support = chunk.getSupportValue(x, y, z);
                                 int filler = chunk.getFiller(x, y, z);
                                 int fluid = chunk.getFluidId(x, y, z);
                                 byte level = chunk.getFluidLevel(x, y, z);
                                 Holder<ChunkStore> holder = chunk.getBlockComponentHolder(x, y, z);
                                 if (block != 0
                                    && filler == 0
                                    && (this.edit.getBlockMask() == null || !this.edit.getBlockMask().isExcluded(accessor, x, y, z, minx, max, block, 0))) {
                                    this.currentSelection.addBlockAtWorldPos(x, y, z, block, rotation, filler, support, holder);
                                 }

                                 if (fluid != 0
                                    && (this.edit.getBlockMask() == null || !this.edit.getBlockMask().isExcluded(accessor, x, y, z, minx, max, 0, fluid))) {
                                    this.currentSelection.addFluidAtWorldPos(x, y, z, fluid, level);
                                 }

                                 if (this.samplingMode == RevolveOperation.Sampling.Neighbor) {
                                    this.materialBuffer[x - xMin][y - yMin][z - zMin] = Material.full(block, rotation, support, filler, holder, fluid, level);
                                 }

                                 this.edit.setMaterial(x, y, z, Material.EMPTY);
                              }
                           }
                        }
                     }
                  }
               }

               List<Ref<EntityStore>> entityRefs = new ReferenceArrayList();
               if (!fullRevolve && copyEntities) {
                  World world = componentAccessor.getExternalData().getWorld();
                  Store<EntityStore> entityStore = world.getEntityStore().getStore();
                  List<Ref<EntityStore>> entities = new ReferenceArrayList();
                  BuilderToolsPlugin.forEachCopyableInSelection(world, xMin, yMin, zMin, xMax - xMin, yMax - yMin, zMax - zMin, entities::add);
                  int totalEntities = entities.size() * this.copyCount;
                  if (totalEntities <= 1000) {
                     for (Ref<EntityStore> e : entities) {
                        Holder<EntityStore> holderx = entityStore.copyEntity(e);
                        this.currentSelection.addEntityFromWorld(holderx);
                        entityRefs.add(e);
                     }
                  } else {
                     BuilderToolsPlugin.sendFeedback(
                        Message.translation("server.builderTools.errorTooManyEntities").param("count", 1000),
                        player,
                        NotificationStyle.Warning,
                        componentAccessor
                     );
                  }
               }

               if (customDistancex > 0) {
                  double selCX = (xMin + xMax) / 2.0;
                  double selCZ = (zMin + zMax) / 2.0;
                  double pivotDX = selCX - this.center.x;
                  double pivotDZ = selCZ - this.center.z;
                  double naturalDist = Math.sqrt(pivotDX * pivotDX + pivotDZ * pivotDZ);
                  if (naturalDist > 0.0) {
                     double scale = customDistancex / naturalDist;
                     int newPosX = (int)Math.floor(this.center.x + pivotDX * scale) - (xMax - xMin) / 2;
                     int newPosZ = (int)Math.floor(this.center.z + pivotDZ * scale) - (zMax - zMin) / 2;
                     this.currentSelection.setPosition(newPosX, yMin, newPosZ);
                     int deltaX = newPosX - xMin;
                     int deltaZ = newPosZ - zMin;

                     for (Ref<EntityStore> entity : entityRefs) {
                        this.edit.trackMovedEntity(entity, componentAccessor);
                        TransformComponent transform = componentAccessor.getComponent(entity, TransformComponent.getComponentType());
                        if (transform != null) {
                           transform.getPosition().add(deltaX, 0.0, deltaZ);
                        }
                     }
                  }
               }

               if (fullRevolve) {
                  this.samplingMode = RevolveOperation.Sampling.Disabled;
                  double radiusXZ = computeRadiusXZ(xMin, xMax, zMin, zMax, this.center);
                  this.copyCount = MathUtil.clamp((int)Math.round(radiusXZ * 12.0), 10, 2000);
                  int selectionBFCount = this.currentSelection.getBlockCount() + this.currentSelection.getFluidCount();
                  if (selectionBFCount == 0) {
                     return;
                  }

                  int maxCount = MathUtil.clamp(1000000 / selectionBFCount, 10, 2000);
                  this.copyCount = Math.min(this.copyCount, maxCount);
               }

               this.stepRadians = Math.toRadians(360.0 / this.copyCount);
               this.stepDegrees = 360.0 / this.copyCount;
         }
      }
   }

   @Override
   public void execute(ComponentAccessor<EntityStore> componentAccessor) {
   }

   @Override
   public void executeAt(int posX, int posY, int posZ, ComponentAccessor<EntityStore> componentAccessor) {
      if (this.currentSelection != null) {
         double[] cos = new double[this.copyCount + 1];
         double[] sin = new double[this.copyCount + 1];
         double[] degrees = new double[this.copyCount + 1];

         for (int i = 0; i <= this.copyCount; i++) {
            double angle = this.stepRadians * i;
            cos[i] = Math.cos(angle);
            sin[i] = Math.sin(angle);
            degrees[i] = this.stepDegrees * i;
         }

         if (this.samplingMode.equals(RevolveOperation.Sampling.Reverse)) {
            this.reverseSample();
            this.rotateEntities(componentAccessor, sin, cos, degrees);
         } else {
            BlockSelection placeDelayed = new BlockSelection();
            BlockSelection alreadyPlaced = new BlockSelection();
            int[][] offsets = NEIGHBOR_OFFSETS_XZ;
            Vector3i rotVec = new Vector3i();
            this.currentSelection
               .forEachBlock(
                  (bx, by, bz, blockHolder) -> {
                     int neighborMask = this.checkNeighbours(offsets, blockHolder.blockId(), bx, by, bz, false);
                     int nextCount = Integer.bitCount(neighborMask);
                     double deltaX = bx + this.currentSelection.getX() + 0.5 - this.center.x;
                     double deltaZ = bz + this.currentSelection.getZ() + 0.5 - this.center.z;

                     for (int c = 1; c <= this.copyCount; c++) {
                        int newRotation = rotateBlock(blockHolder, degrees[c]);
                        if (this.samplingMode == RevolveOperation.Sampling.Neighbor && nextCount > 0) {
                           for (int d = 0; d < offsets.length; d++) {
                              if ((neighborMask & 1 << d) != 0) {
                                 double offsetDeltaX = offsets[d][0] * 0.33 + deltaX;
                                 double offsetDeltaZ = offsets[d][1] * 0.33 + deltaZ;
                                 this.rotate(rotVec, c, offsetDeltaX, by, offsetDeltaZ, sin, cos);
                                 placeDelayed.addBlockAtWorldPos(
                                    rotVec.x, rotVec.y, rotVec.z, blockHolder.blockId(), newRotation, blockHolder.filler(), blockHolder.supportValue()
                                 );
                              }
                           }
                        }

                        this.rotate(rotVec, c, deltaX, by, deltaZ, sin, cos);
                        if (nextCount > 0) {
                           placeDelayed.addBlockAtWorldPos(
                              rotVec.x, rotVec.y, rotVec.z, blockHolder.blockId(), newRotation, blockHolder.filler(), blockHolder.supportValue()
                           );
                        } else {
                           alreadyPlaced.addBlockAtWorldPos(
                              rotVec.x, rotVec.y, rotVec.z, blockHolder.blockId(), newRotation, blockHolder.filler(), blockHolder.supportValue()
                           );
                           this.edit
                              .setMaterial(
                                 rotVec.x,
                                 rotVec.y,
                                 rotVec.z,
                                 Material.full(blockHolder.blockId(), newRotation, blockHolder.supportValue(), blockHolder.filler(), blockHolder.holder())
                              );
                        }
                     }
                  }
               );
            this.currentSelection.forEachFluid((bx, by, bz, fluid, level) -> {
               int neighborMask = this.checkNeighbours(offsets, fluid, bx, by, bz, true);
               int nextCount = Integer.bitCount(neighborMask);
               double deltaX = bx + this.currentSelection.getX() + 0.5 - this.center.x;
               double deltaZ = bz + this.currentSelection.getZ() + 0.5 - this.center.z;

               for (int c = 1; c <= this.copyCount; c++) {
                  if (this.samplingMode == RevolveOperation.Sampling.Neighbor && nextCount > 0) {
                     for (int d = 0; d < offsets.length; d++) {
                        if ((neighborMask & 1 << d) != 0) {
                           double offsetDeltaX = offsets[d][0] * 0.33 + deltaX;
                           double offsetDeltaZ = offsets[d][1] * 0.33 + deltaZ;
                           this.rotate(rotVec, c, offsetDeltaX, by, offsetDeltaZ, sin, cos);
                           this.edit.setMaterial(rotVec.x, rotVec.y, rotVec.z, Material.fluid(fluid, level));
                        }
                     }
                  }

                  this.rotate(rotVec, c, deltaX, by, deltaZ, sin, cos);
                  this.edit.setMaterial(rotVec.x, rotVec.y, rotVec.z, Material.fluid(fluid, level));
               }
            });
            placeDelayed.forEachBlock(
               (bx, by, bz, blockHolder) -> {
                  if (!alreadyPlaced.hasBlockAtWorldPos(bx, by, bz)) {
                     this.edit
                        .setMaterial(
                           bx,
                           by,
                           bz,
                           Material.full(blockHolder.blockId(), blockHolder.rotation(), blockHolder.supportValue(), blockHolder.filler(), blockHolder.holder())
                        );
                  }
               }
            );
            this.rotateEntities(componentAccessor, sin, cos, degrees);
         }
      }
   }

   private void rotateEntities(ComponentAccessor<EntityStore> componentAccessor, double[] sin, double[] cos, double[] degrees) {
      World world = componentAccessor.getExternalData().getWorld();
      Store<EntityStore> entityStore = world.getEntityStore().getStore();
      this.currentSelection.forEachEntity(entityHolder -> {
         TransformComponent t = entityHolder.getComponent(TransformComponent.getComponentType());
         double x = t.getPosition().getX();
         double y = t.getPosition().getY();
         double z = t.getPosition().getZ();
         double deltaX = x + this.currentSelection.getX() - this.center.x;
         double deltaZ = z + this.currentSelection.getZ() - this.center.z;

         for (int c = 1; c < this.copyCount; c++) {
            double rotatedX = deltaX * cos[c] - deltaZ * sin[c];
            double rotatedZ = deltaX * sin[c] + deltaZ * cos[c];
            float yawRad = (float)Math.toRadians(-degrees[c]);
            Holder<EntityStore> copy = entityHolder.clone();
            TransformComponent transformComponent = copy.getComponent(TransformComponent.getComponentType());
            Vector3f bodyRot = transformComponent.getRotation();
            bodyRot.addYaw(yawRad);
            HeadRotation headRotComp = copy.getComponent(HeadRotation.getComponentType());
            if (headRotComp != null) {
               headRotComp.getRotation().addYaw(yawRad);
            }

            transformComponent.getPosition().assign(rotatedX + this.center.x, y + this.currentSelection.getY(), rotatedZ + this.center.z);
            copy.putComponent(UUIDComponent.getComponentType(), new UUIDComponent(UUID.randomUUID()));
            copy.removeComponent(EntityTrackerSystems.Visible.getComponentType());
            copy.removeComponent(NetworkId.getComponentType());
            Ref<EntityStore> entityRef = new Ref<>(entityStore);
            world.execute(() -> entityStore.addEntity(copy, entityRef, AddReason.LOAD));
            this.edit.trackSpawnedEntity(entityRef);
         }
      });
   }

   private static int rotateBlock(BlockSelection.BlockHolder blockHolder, double snapped) {
      Rotation snappedYaw = Rotation.ofDegrees((int)(Math.round(-snapped / 90.0) * 90L));
      RotationTuple blockRotation = RotationTuple.get(blockHolder.rotation());
      RotationTuple rotatedRotation = RotationTuple.of(blockRotation.yaw().add(snappedYaw), blockRotation.pitch(), blockRotation.roll());
      if (rotatedRotation == null) {
         rotatedRotation = blockRotation;
      }

      return rotatedRotation.index();
   }

   private int checkNeighbours(int[][] offsets, int blockID, int x, int y, int z, boolean fluid) {
      int nextToSame = 0;
      if (this.samplingMode == RevolveOperation.Sampling.Neighbor) {
         for (int d = 0; d < offsets.length; d++) {
            int neighborX = x + offsets[d][0];
            int neighborZ = z + offsets[d][1];
            if (neighborX >= 0 && neighborX < this.bufferX && neighborZ >= 0 && neighborZ < this.bufferZ) {
               boolean matches = !fluid
                  ? this.materialBuffer[neighborX][y][neighborZ].getBlockId() == blockID
                  : this.materialBuffer[neighborX][y][neighborZ].getFluidId() == blockID;
               if (matches) {
                  nextToSame |= 1 << d;
               }
            }
         }
      }

      return nextToSame;
   }

   private void rotate(Vector3i v, int c, double x, int y, double z, double[] sin, double[] cos) {
      double rx = x * cos[c] - z * sin[c];
      double rz = x * sin[c] + z * cos[c];
      v.assign((int)Math.floor(rx + this.center.x), y + this.currentSelection.getY(), (int)Math.floor(rz + this.center.z));
   }

   private void reverseSample() {
      this.currentSelection
         .setAnchor(
            (int)(this.center.x - this.currentSelection.getX()),
            (int)this.center.y - this.currentSelection.getY(),
            (int)(this.center.z - this.currentSelection.getZ())
         );

      for (int c = 1; c <= this.copyCount; c++) {
         BlockSelection calcSelection = this.currentSelection.cloneSelection().rotateArbitrary((float)this.stepDegrees * c, 0.0F, 0.0F);
         calcSelection.forEachBlock(
            (bx, by, bz, blockHolder) -> this.edit
               .setMaterial(
                  bx + this.currentSelection.getX(),
                  by + this.currentSelection.getY(),
                  bz + this.currentSelection.getZ(),
                  Material.full(blockHolder.blockId(), blockHolder.rotation(), blockHolder.supportValue(), blockHolder.filler(), blockHolder.holder())
               )
         );
         calcSelection.forEachFluid(
            (bx, by, bz, fluid, level) -> this.edit
               .setMaterial(
                  bx + this.currentSelection.getX(), by + this.currentSelection.getY(), bz + this.currentSelection.getZ(), Material.fluid(fluid, level)
               )
         );
      }
   }

   private static double computeRadiusXZ(int xMin, int xMax, int zMin, int zMax, Vector3f hit) {
      double maxR2 = 0.0;
      int[] xs = new int[]{xMin, xMax};
      int[] zs = new int[]{zMin, zMax};

      for (int cx : xs) {
         for (int cz : zs) {
            double deltaX = cx + 0.5 - hit.x;
            double deltaZ = cz + 0.5 - hit.z;
            double r2 = deltaX * deltaX + deltaZ * deltaZ;
            if (r2 > maxR2) {
               maxR2 = r2;
            }
         }
      }

      return Math.sqrt(maxR2);
   }

   @Override
   public boolean execute0(int x, int y, int z) {
      return false;
   }

   private static enum Sampling {
      Neighbor,
      Reverse,
      Disabled;
   }
}
