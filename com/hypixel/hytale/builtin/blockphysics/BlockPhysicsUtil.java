package com.hypixel.hytale.builtin.blockphysics;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFace;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFaceSupport;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RequiredBlockFaceSupport;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.blocktype.component.BlockPhysics;
import com.hypixel.hytale.server.core.modules.blockset.BlockSetModule;
import com.hypixel.hytale.server.core.modules.interaction.BlockHarvestUtils;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockPhysicsUtil {
   public static final int DOESNT_SATISFY = 0;
   public static final int IGNORE = -1;
   public static final int SATISFIES_SUPPORT = -2;
   public static final int WAITING_CHUNK = -3;

   @Nonnull
   public static BlockPhysicsUtil.Result applyBlockPhysics(
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      @Nonnull Ref<ChunkStore> chunkReference,
      @Nonnull BlockPhysicsSystems.CachedAccessor chunkAccessor,
      BlockSection blockSection,
      @Nonnull BlockPhysics blockPhysics,
      @Nonnull FluidSection fluidSection,
      int blockX,
      int blockY,
      int blockZ,
      @Nonnull BlockType blockType,
      int rotation,
      int filler
   ) {
      if (filler != 0) {
         return BlockPhysicsUtil.Result.VALID;
      } else {
         int supportDistance = -1;
         if (blockType.getHitboxTypeIndex() == 0) {
            supportDistance = testBlockPhysics(chunkAccessor, blockSection, blockPhysics, fluidSection, blockX, blockY, blockZ, blockType, rotation, filler);
         } else {
            BlockBoundingBoxes boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
            if (boundingBoxes != null && boundingBoxes.protrudesUnitBox()) {
               BlockBoundingBoxes.RotatedVariantBoxes rotatedBox = boundingBoxes.get(rotation);
               Box boundingBox = rotatedBox.getBoundingBox();
               int minX = (int)boundingBox.min.x;
               int minY = (int)boundingBox.min.y;
               int minZ = (int)boundingBox.min.z;
               if (minX - boundingBox.min.x > 0.0) {
                  minX--;
               }

               if (minY - boundingBox.min.y > 0.0) {
                  minY--;
               }

               if (minZ - boundingBox.min.z > 0.0) {
                  minZ--;
               }

               int maxX = (int)boundingBox.max.x;
               int maxY = (int)boundingBox.max.y;
               int maxZ = (int)boundingBox.max.z;
               if (boundingBox.max.x - maxX > 0.0) {
                  maxX++;
               }

               if (boundingBox.max.y - maxY > 0.0) {
                  maxY++;
               }

               if (boundingBox.max.z - maxZ > 0.0) {
                  maxZ++;
               }

               int blockWidth = Math.max(maxX - minX, 1);
               int blockHeight = Math.max(maxY - minY, 1);
               int blockDepth = Math.max(maxZ - minZ, 1);

               label136:
               for (int x = 0; x < blockWidth; x++) {
                  for (int y = 0; y < blockHeight; y++) {
                     for (int z = 0; z < blockDepth; z++) {
                        int fillerX = blockX + minX + x;
                        int fillerY = blockY + minY + y;
                        int fillerZ = blockZ + minZ + z;
                        BlockSection neighbourBlockSection;
                        FluidSection neighbourFluidSection;
                        BlockPhysics neighbourBlockPhysics;
                        if (ChunkUtil.isSameChunkSection(blockX, blockY, blockZ, fillerX, fillerY, fillerZ)) {
                           neighbourBlockSection = blockSection;
                           neighbourFluidSection = fluidSection;
                           neighbourBlockPhysics = blockPhysics;
                        } else {
                           int nx = ChunkUtil.chunkCoordinate(fillerX);
                           int ny = ChunkUtil.chunkCoordinate(fillerY);
                           int nz = ChunkUtil.chunkCoordinate(fillerZ);
                           neighbourBlockSection = chunkAccessor.getBlockSection(nx, ny, nz);
                           neighbourFluidSection = chunkAccessor.getFluidSection(nx, ny, nz);
                           neighbourBlockPhysics = chunkAccessor.getBlockPhysics(nx, ny, nz);
                        }

                        if (neighbourBlockSection == null || neighbourFluidSection == null) {
                           return BlockPhysicsUtil.Result.WAITING_CHUNK;
                        }

                        int neighbourFiller = FillerBlockUtil.pack(minX + x, minY + y, minZ + z);
                        int neighbourRotation = neighbourBlockSection.getRotationIndex(fillerX, fillerY, fillerZ);
                        int fillerSupportDistance = testBlockPhysics(
                           chunkAccessor,
                           neighbourBlockSection,
                           neighbourBlockPhysics,
                           neighbourFluidSection,
                           fillerX,
                           fillerY,
                           fillerZ,
                           blockType,
                           neighbourRotation,
                           neighbourFiller
                        );
                        if (fillerSupportDistance != -1) {
                           switch (blockType.getBlockSupportsRequiredFor()) {
                              case Any:
                                 if (fillerSupportDistance == -2) {
                                    supportDistance = -2;
                                    break label136;
                                 }

                                 if (fillerSupportDistance == 0) {
                                    supportDistance = 0;
                                 } else if (supportDistance < fillerSupportDistance) {
                                    supportDistance = fillerSupportDistance;
                                 }
                                 break;
                              case All:
                                 if (fillerSupportDistance == 0) {
                                    supportDistance = 0;
                                    break label136;
                                 }

                                 if (fillerSupportDistance == -2) {
                                    supportDistance = -2;
                                 } else if (supportDistance == -1 && supportDistance < fillerSupportDistance) {
                                    supportDistance = fillerSupportDistance;
                                 }
                           }
                        }
                     }
                  }
               }
            } else {
               supportDistance = testBlockPhysics(chunkAccessor, blockSection, blockPhysics, fluidSection, blockX, blockY, blockZ, blockType, rotation, filler);
            }
         }

         if (supportDistance == 0) {
            World world = componentAccessor.getExternalData().getWorld();
            Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
            switch (blockType.getSupportDropType()) {
               case BREAK:
                  BlockHarvestUtils.naturallyRemoveBlockByPhysics(
                     new Vector3i(blockX, blockY, blockZ), blockType, filler, 256, chunkReference, componentAccessor, chunkStore
                  );
                  break;
               case DESTROY:
                  BlockHarvestUtils.naturallyRemoveBlockByPhysics(
                     new Vector3i(blockX, blockY, blockZ), blockType, filler, 2304, chunkReference, componentAccessor, chunkStore
                  );
            }

            return BlockPhysicsUtil.Result.INVALID;
         } else if (supportDistance == -1) {
            return BlockPhysicsUtil.Result.VALID;
         } else if (supportDistance == -3) {
            return BlockPhysicsUtil.Result.WAITING_CHUNK;
         } else {
            int currentSupport = blockPhysics.get(blockX, blockY, blockZ);
            if (supportDistance == -2) {
               if (currentSupport != 0) {
                  blockPhysics.set(blockX, blockY, blockZ, 0);
                  chunkAccessor.performBlockUpdate(blockX, blockY, blockZ);
               }

               return BlockPhysicsUtil.Result.VALID;
            } else {
               if (currentSupport == supportDistance) {
                  chunkAccessor.performBlockUpdate(blockX, blockY, blockZ, supportDistance - 1);
               } else {
                  blockPhysics.set(blockX, blockY, blockZ, supportDistance);
                  chunkAccessor.performBlockUpdate(blockX, blockY, blockZ);
               }

               return BlockPhysicsUtil.Result.VALID;
            }
         }
      }
   }

   public static int testBlockPhysics(
      @Nonnull BlockPhysicsSystems.CachedAccessor chunkAccessor,
      BlockSection blockSection,
      @Nullable BlockPhysics blockPhysics,
      @Nonnull FluidSection fluidSection,
      int blockX,
      int blockY,
      int blockZ,
      @Nonnull BlockType blockType,
      int rotation,
      int filler
   ) {
      if (blockType.isUnknown()) {
         return -1;
      } else {
         Map<BlockFace, RequiredBlockFaceSupport[]> requiredBlockFaceSupportMap = blockType.getSupport(rotation);
         if (requiredBlockFaceSupportMap != null && !requiredBlockFaceSupportMap.isEmpty()) {
            Vector3i blockFillerOffset = new Vector3i(FillerBlockUtil.unpackX(filler), FillerBlockUtil.unpackY(filler), FillerBlockUtil.unpackZ(filler));
            Vector3i neighbourFillerOffset = new Vector3i();
            Fluid fluid = Fluid.getAssetMap().getAsset(fluidSection.getFluidId(blockX, blockY, blockZ));
            BlockBoundingBoxes hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
            Box boundingBox = hitbox.get(rotation).getBoundingBox();
            Vector3i origin = new Vector3i(
               blockX - FillerBlockUtil.unpackX(filler), blockY - FillerBlockUtil.unpackY(filler), blockZ - FillerBlockUtil.unpackZ(filler)
            );
            boolean hasTestedForSupport = false;
            int requiredSupportDistance = blockType.getMaxSupportDistance();
            int lowestSupportDistance = Integer.MAX_VALUE;

            for (BlockFace blockFace : BlockFace.VALUES) {
               RequiredBlockFaceSupport[] requiredBlockFaceSupports = requiredBlockFaceSupportMap.get(blockFace);
               if (requiredBlockFaceSupports != null && requiredBlockFaceSupports.length != 0) {
                  BlockFace[] connectingFaces = blockFace.getConnectingFaces();
                  Vector3i[] connectingFaceOffsets = blockFace.getConnectingFaceOffsets();

                  for (int i = 0; i < connectingFaces.length; i++) {
                     BlockFace neighbourBlockFace = connectingFaces[i];
                     Vector3i neighbourDirection = connectingFaceOffsets[i];
                     int neighbourX = blockX + neighbourDirection.x;
                     int neighbourY = blockY + neighbourDirection.y;
                     int neighbourZ = blockZ + neighbourDirection.z;
                     if (!boundingBox.containsBlock(origin, neighbourX, neighbourY, neighbourZ)) {
                        BlockSection neighbourBlockSection;
                        FluidSection neighbourFluidSection;
                        BlockPhysics neighbourBlockPhysics;
                        if (ChunkUtil.isSameChunkSection(blockX, blockY, blockZ, neighbourX, neighbourY, neighbourZ)) {
                           neighbourBlockSection = blockSection;
                           neighbourFluidSection = fluidSection;
                           neighbourBlockPhysics = blockPhysics;
                        } else {
                           int nx = ChunkUtil.chunkCoordinate(neighbourX);
                           int ny = ChunkUtil.chunkCoordinate(neighbourY);
                           int nz = ChunkUtil.chunkCoordinate(neighbourZ);
                           neighbourBlockSection = chunkAccessor.getBlockSection(nx, ny, nz);
                           neighbourFluidSection = chunkAccessor.getFluidSection(nx, ny, nz);
                           neighbourBlockPhysics = chunkAccessor.getBlockPhysics(nx, ny, nz);
                        }

                        if (neighbourFluidSection == null || neighbourBlockSection == null) {
                           return -3;
                        }

                        int neighbourFluidId = neighbourFluidSection.getFluidId(neighbourX, neighbourY, neighbourZ);
                        int neighbourBlockId = neighbourBlockSection.get(neighbourX, neighbourY, neighbourZ);
                        int neighbourFiller = neighbourBlockSection.getFiller(neighbourX, neighbourY, neighbourZ);
                        int neighbourRotation = neighbourBlockSection.getRotationIndex(neighbourX, neighbourY, neighbourZ);
                        BlockType neighbourBlockType = BlockType.getAssetMap().getAsset(neighbourBlockId);
                        Fluid neighbourFluid = Fluid.getAssetMap().getAsset(neighbourFluidId);
                        neighbourFillerOffset.assign(
                           FillerBlockUtil.unpackX(neighbourFiller), FillerBlockUtil.unpackY(neighbourFiller), FillerBlockUtil.unpackZ(neighbourFiller)
                        );
                        boolean doesSatisfySupport = false;
                        boolean failedSatisfySupport = false;

                        for (RequiredBlockFaceSupport requiredBlockFaceSupport : requiredBlockFaceSupports) {
                           if (requiredBlockFaceSupport.isAppliedToFiller(blockFillerOffset)) {
                              boolean doesSatisfyRequirements = doesSatisfyRequirements(
                                 blockType,
                                 blockFillerOffset,
                                 neighbourFillerOffset,
                                 blockFace,
                                 neighbourBlockFace,
                                 neighbourBlockId,
                                 neighbourBlockType,
                                 neighbourRotation,
                                 neighbourFluidId,
                                 neighbourFluid,
                                 requiredBlockFaceSupport
                              );
                              if (doesSatisfyRequirements && requiredSupportDistance > 0 && requiredBlockFaceSupport.allowsSupportPropagation()) {
                                 int supportDistance = neighbourBlockPhysics != null ? neighbourBlockPhysics.get(neighbourX, neighbourY, neighbourZ) : 0;
                                 if (supportDistance == 15) {
                                    lowestSupportDistance = 1;
                                 } else if (supportDistance < lowestSupportDistance) {
                                    lowestSupportDistance = supportDistance;
                                 }
                              }

                              switch (requiredBlockFaceSupport.getSupport()) {
                                 case IGNORED:
                                    break;
                                 case REQUIRED:
                                    if (doesSatisfyRequirements) {
                                       doesSatisfySupport = true;
                                    }

                                    hasTestedForSupport = true;
                                    break;
                                 case DISALLOWED:
                                    if (doesSatisfyRequirements) {
                                       failedSatisfySupport = true;
                                    }

                                    hasTestedForSupport = true;
                                    break;
                                 default:
                                    throw new IllegalArgumentException("Unknown Support Match type: " + requiredBlockFaceSupport.getMatchSelf());
                              }
                           }
                        }

                        if (!failedSatisfySupport && doesSatisfySupport) {
                           return -2;
                        }
                     }
                  }
               }
            }

            if (!hasTestedForSupport) {
               return -1;
            } else {
               if (lowestSupportDistance < Integer.MAX_VALUE && lowestSupportDistance >= 0) {
                  int supportDistance = lowestSupportDistance + 1;
                  if (requiredSupportDistance >= supportDistance) {
                     return supportDistance;
                  }
               }

               return 0;
            }
         } else {
            return -1;
         }
      }
   }

   public static boolean doesSatisfyRequirements(
      @Nonnull BlockType blockType,
      Vector3i blockFillerOffset,
      Vector3i neighbourFillerOffset,
      BlockFace blockFace,
      BlockFace neighbourBlockFace,
      int neighbourBlockId,
      @Nonnull BlockType neighbourBlockType,
      int neighbourRotation,
      int neighbourFluidId,
      @Nonnull Fluid neighbourFluid,
      @Nonnull RequiredBlockFaceSupport requiredBlockFaceSupport
   ) {
      String neighbourBlockTypeKey = neighbourBlockType.getId();
      boolean hasSupport = true;
      int blockSetId = requiredBlockFaceSupport.getBlockSetIndex();
      if (blockSetId >= 0 && !BlockSetModule.getInstance().blockInSet(blockSetId, neighbourBlockId)) {
         hasSupport = false;
      }

      String requiredBlockTypeId = requiredBlockFaceSupport.getBlockTypeId();
      if (hasSupport && requiredBlockTypeId != null && !requiredBlockTypeId.equals(neighbourBlockTypeKey)) {
         hasSupport = false;
      }

      String fluidId = requiredBlockFaceSupport.getFluidId();
      if (hasSupport
         && fluidId != null
         && (neighbourBlockType.getMaterial() != BlockMaterial.Empty || neighbourFluidId == 0 || !fluidId.equals(neighbourFluid.getId()))) {
         hasSupport = false;
      }

      int tagIndex = requiredBlockFaceSupport.getTagIndex();
      if (tagIndex >= 0 && !BlockType.getAssetMap().getKeysForTag(tagIndex).contains(neighbourBlockTypeKey)) {
         hasSupport = false;
      }

      if (hasSupport && requiredBlockFaceSupport.getFaceType() != null) {
         hasSupport = doesMatchFaceType(
            neighbourFillerOffset, requiredBlockFaceSupport.getFaceType(), neighbourBlockFace, neighbourBlockType.getSupporting(neighbourRotation)
         );
      }

      if (hasSupport && requiredBlockFaceSupport.getSelfFaceType() != null) {
         hasSupport = doesMatchFaceType(blockFillerOffset, requiredBlockFaceSupport.getSelfFaceType(), blockFace, blockType.getSupporting(neighbourRotation));
      }
      return switch (requiredBlockFaceSupport.getMatchSelf()) {
         case IGNORED -> {
         }
         case REQUIRED -> {
            if (hasSupport) {
               yield blockType.getId().equals(neighbourBlockTypeKey);
            }
         }
         case DISALLOWED -> {
            if (hasSupport) {
               yield !blockType.getId().equals(neighbourBlockTypeKey);
            }
         }
         default -> throw new IllegalArgumentException("Unknown MatchSelf type: " + requiredBlockFaceSupport.getMatchSelf());
      };
   }

   public static boolean doesMatchFaceType(
      Vector3i fillerOffset, @Nonnull String faceType, BlockFace blockFace, @Nonnull Map<BlockFace, BlockFaceSupport[]> supporting
   ) {
      boolean faceHasSupport = false;
      BlockFaceSupport[] blockFaceSupports = supporting.get(blockFace);
      if (blockFaceSupports != null) {
         for (BlockFaceSupport blockFaceSupport : blockFaceSupports) {
            if (blockFaceSupport.providesSupportFromFiller(fillerOffset) && faceType.equals(blockFaceSupport.getFaceType())) {
               faceHasSupport = true;
               break;
            }
         }
      }

      return faceHasSupport;
   }

   public static enum Result {
      INVALID,
      VALID,
      WAITING_CHUNK;
   }
}
