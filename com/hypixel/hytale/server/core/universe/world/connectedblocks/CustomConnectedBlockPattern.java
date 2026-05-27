package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.simple.BooleanCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFlipType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkFlag;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.Pair;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class CustomConnectedBlockPattern extends CustomTemplateConnectedBlockPattern {
   @Nonnull
   public static final BuilderCodec<CustomConnectedBlockPattern> CODEC = BuilderCodec.builder(
         CustomConnectedBlockPattern.class, CustomConnectedBlockPattern::new
      )
      .append(
         new KeyedCodec<>("TransformRulesToOrientation", Codec.BOOLEAN, false),
         (o, transformRulesToPlacedOrientation) -> o.transformRulesToOrientation = transformRulesToPlacedOrientation,
         o -> o.transformRulesToOrientation
      )
      .documentation("If the rules should be transformed to the current orientation of the block.")
      .add()
      .<Rotation>append(
         new KeyedCodec<>("YawToApplyAddReplacedBlockType", Rotation.CODEC, false),
         (o, yawToApplyAddReplacedBlockType) -> o.yawToApplyAddReplacedBlockType = yawToApplyAddReplacedBlockType,
         o -> o.yawToApplyAddReplacedBlockType
      )
      .documentation(
         "Apply an additional Yaw to the resulting BlockType represented by this shape. This allows your replacement to be offset from your original placement"
      )
      .add()
      .<Boolean>append(
         new KeyedCodec<>("RequireFaceTagsMatchingRoll", Codec.BOOLEAN, false),
         (o, requireFaceTagsMatchingRoll) -> o.requireFaceTagsMatchingRoll = requireFaceTagsMatchingRoll,
         o -> o.requireFaceTagsMatchingRoll
      )
      .documentation("Adds Roll comparison to face tag matching in patterns below")
      .add()
      .<PatternRotationDefinition>append(
         new KeyedCodec<>("AllowedPatternTransformations", PatternRotationDefinition.CODEC, false),
         (o, patternRotations) -> o.patternRotationDefinition = patternRotations,
         o -> o.patternRotationDefinition
      )
      .documentation(
         "Will create additional generated patterns that are variants of this pattern, but rotated/mirrored/flipped to achieve different results. A common example of this is the Fence, which should its resulting shape based on the rotation of its pattern (fence corner rotates depending on which two sides have the corner fence shape)"
      )
      .add()
      .<ConnectedBlockPatternRule[]>append(
         new KeyedCodec<>("RulesToMatch", new ArrayCodec<>(ConnectedBlockPatternRule.CODEC, ConnectedBlockPatternRule[]::new), true),
         (o, matchingPatterns) -> o.rulesToMatch = matchingPatterns,
         o -> o.rulesToMatch
      )
      .documentation("All rules must match in order for the pattern to match")
      .add()
      .<Boolean>append(
         new KeyedCodec<>("OnlyOnPlacement", new BooleanCodec(), false), (o, onlyOnPlacement) -> o.onlyOnPlacement = onlyOnPlacement, o -> o.onlyOnPlacement
      )
      .documentation("If true, this pattern will only be checked when the block is first placed.")
      .add()
      .<Boolean>append(new KeyedCodec<>("OnlyOnUpdate", new BooleanCodec(), false), (o, onlyOnUpdate) -> o.onlyOnUpdate = onlyOnUpdate, o -> o.onlyOnUpdate)
      .documentation("If true, this pattern will only be checked when the block is updated by neighboring block changes.")
      .add()
      .build();
   @Nonnull
   private static final Random random = new Random();
   private boolean transformRulesToOrientation = true;
   private PatternRotationDefinition patternRotationDefinition = PatternRotationDefinition.DEFAULT;
   private ConnectedBlockPatternRule[] rulesToMatch;
   private Rotation yawToApplyAddReplacedBlockType;
   private boolean requireFaceTagsMatchingRoll;
   private boolean onlyOnUpdate;
   private boolean onlyOnPlacement;

   private static boolean checkPatternRuleAgainstBlockType(
      @Nonnull CustomTemplateConnectedBlockRuleSet placedRuleset,
      @Nonnull CustomConnectedBlockTemplateAsset template,
      @Nonnull ConnectedBlockPatternRule rule,
      @Nonnull String blockToTest,
      @Nonnull RotationTuple rotationToCheckUnrotated,
      int fillerToCheckUnrotated
   ) {
      if (!rule.getFaceTags().getDirections().isEmpty()) {
         BlockType checkingBlockType = BlockType.getAssetMap().getAsset(blockToTest);
         if (!(checkingBlockType.getConnectedBlockRuleSet() instanceof CustomTemplateConnectedBlockRuleSet checkingConnectedBlockRuleSet)) {
            return !rule.isInclude();
         }

         int index = BlockType.getAssetMap().getIndex(blockToTest);
         Set blockTypeListAsset = checkingConnectedBlockRuleSet.getShapesForBlockType(index);
         CustomConnectedBlockTemplateAsset checkingTemplateAsset = checkingConnectedBlockRuleSet.getShapeTemplateAsset();
         if (checkingTemplateAsset == null) {
            return !rule.isInclude();
         }

         for (String shapeName : blockTypeListAsset) {
            if (template.connectsToOtherMaterials
               || placedRuleset.getShapeNameToBlockPatternMap().equals(checkingConnectedBlockRuleSet.getShapeNameToBlockPatternMap())) {
               ConnectedBlockShape blockToCheckConnectedBlockShape = checkingTemplateAsset.connectedBlockShapes.get(shapeName);
               Map<Vector3ic, HashSet<String>> ruleFaceTags = rule.getFaceTags().getBlockFaceTags();

               for (Entry<Vector3ic, HashSet<String>> ruleFaceTag : ruleFaceTags.entrySet()) {
                  Vector3i adjustedDirectionOfPattern = Rotation.rotate(
                     new Vector3i(ruleFaceTag.getKey()), Rotation.None.subtract(rotationToCheckUnrotated.yaw()), Rotation.None
                  );

                  for (String faceTag : ruleFaceTag.getValue()) {
                     boolean containsFaceTag = blockToCheckConnectedBlockShape.getFaceTags() != null
                        && blockToCheckConnectedBlockShape.getFaceTags().contains(adjustedDirectionOfPattern, faceTag);
                     if (containsFaceTag) {
                        return rule.isInclude();
                     }
                  }
               }
            }
         }
      }

      if (!rule.getShapeBlockTypeKeys().isEmpty()) {
         BlockType checkingBlockTypex = BlockType.getAssetMap().getAsset(blockToTest);
         if (!(checkingBlockTypex.getConnectedBlockRuleSet() instanceof CustomTemplateConnectedBlockRuleSet checkingConnectedBlockRuleSet)) {
            return !rule.isInclude();
         }

         int var28 = BlockType.getAssetMap().getIndex(blockToTest);

         for (String shapeNamex : checkingConnectedBlockRuleSet.getShapesForBlockType(var28)) {
            if ((
                  template.connectsToOtherMaterials
                     || placedRuleset.getShapeNameToBlockPatternMap().equals(checkingConnectedBlockRuleSet.getShapeNameToBlockPatternMap())
               )
               && rule.getShapeBlockTypeKeys().contains(new BlockPattern.BlockEntry(shapeNamex, rotationToCheckUnrotated.index(), fillerToCheckUnrotated))) {
               return rule.isInclude();
            }
         }
      }

      HashSet<String> ruleBlockTypes = rule.getBlockTypes();
      if (!ruleBlockTypes.isEmpty() && ruleBlockTypes.contains(blockToTest)) {
         return rule.isInclude();
      } else {
         if (rule.getBlockTypeListAssets() != null) {
            for (BlockTypeListAsset blockTypeListAsset : rule.getBlockTypeListAssets()) {
               if (blockTypeListAsset.getBlockTypeKeys().contains(blockToTest)) {
                  return rule.isInclude();
               }
            }
         }

         return !rule.isInclude();
      }
   }

   @Nonnull
   @Override
   public Optional<ConnectedBlocksUtil.ConnectedBlockResult> getConnectedBlockTypeKey(
      @Nonnull String shapeName,
      @Nonnull ChunkStore chunkStore,
      @Nonnull Vector3ic coordinate,
      @Nonnull CustomTemplateConnectedBlockRuleSet connectedBlockRuleset,
      @Nonnull BlockType blockType,
      int rotation,
      @Nonnull Vector3ic placementNormal,
      boolean isPlacement
   ) {
      if ((!isPlacement || !this.onlyOnUpdate) && (isPlacement || !this.onlyOnPlacement)) {
         CustomConnectedBlockTemplateAsset shapeTemplate = connectedBlockRuleset.getShapeTemplateAsset();
         if (shapeTemplate == null) {
            return Optional.empty();
         } else {
            Vector3i coordinateToTest = new Vector3i();
            Rotation3D totalRotation = new Rotation3D(Rotation.None, Rotation.None, Rotation.None);
            Rotation3D tempRotation = new Rotation3D(Rotation.None, Rotation.None, Rotation.None);
            List<Pair<Rotation, PatternRotationDefinition.MirrorAxis>> rotations = this.patternRotationDefinition.getRotations();
            int i = 0;

            label126:
            while (i < rotations.size()) {
               Pair<Rotation, PatternRotationDefinition.MirrorAxis> patternTransform = rotations.get(i);
               totalRotation.assign((Rotation)patternTransform.first(), Rotation.None, Rotation.None);
               if (this.transformRulesToOrientation) {
                  RotationTuple rotationTuple = RotationTuple.get(rotation);
                  tempRotation.assign(rotationTuple.yaw(), rotationTuple.pitch(), rotationTuple.roll());
                  totalRotation.add(tempRotation);
               }

               label124:
               for (ConnectedBlockPatternRule ruleToMatch : this.rulesToMatch) {
                  coordinateToTest.set(ruleToMatch.getRelativePosition());
                  switch ((PatternRotationDefinition.MirrorAxis)patternTransform.second()) {
                     case X:
                        coordinateToTest.x = -coordinateToTest.x();
                        break;
                     case Z:
                        coordinateToTest.z = -coordinateToTest.z();
                     case NONE:
                  }

                  if (ruleToMatch.getPlacementNormals() != null) {
                     for (ConnectedBlockPatternRule.AdjacentSide normal : ruleToMatch.getPlacementNormals()) {
                        if (normal.relativePosition.equals(placementNormal)) {
                           continue label124;
                        }
                     }

                     return Optional.empty();
                  } else {
                     coordinateToTest = Rotation.rotate(coordinateToTest, totalRotation.rotationYaw, totalRotation.rotationPitch, totalRotation.rotationRoll);
                     coordinateToTest.add(coordinate);
                     long chunkIndex = ChunkUtil.indexChunkFromBlock(coordinateToTest.x, coordinateToTest.z);
                     Ref<ChunkStore> chunkRef = chunkStore.getChunkReference(chunkIndex);
                     if (chunkRef == null || !chunkRef.isValid()) {
                        return Optional.empty();
                     }

                     Store<ChunkStore> store = chunkStore.getStore();
                     WorldChunk worldChunkComponent = store.getComponent(chunkRef, WorldChunk.getComponentType());
                     if (worldChunkComponent == null || !worldChunkComponent.is(ChunkFlag.TICKING)) {
                        return Optional.empty();
                     }

                     BlockChunk blockChunkComponent = store.getComponent(chunkRef, BlockChunk.getComponentType());
                     if (blockChunkComponent == null) {
                        return Optional.empty();
                     }

                     String blockToCheckUnrotated = worldChunkComponent.getBlockType(coordinateToTest).getId();
                     int testY = coordinateToTest.y;
                     RotationTuple rotationToCheckUnrotated;
                     int fillerToCheckUnrotated;
                     if (testY >= 0 && testY < 320) {
                        BlockSection section = blockChunkComponent.getSectionAtBlockY(testY);
                        rotationToCheckUnrotated = section.getRotation(coordinateToTest.x, testY, coordinateToTest.z);
                        fillerToCheckUnrotated = section.getFiller(coordinateToTest.x, testY, coordinateToTest.z);
                     } else {
                        rotationToCheckUnrotated = RotationTuple.NONE;
                        fillerToCheckUnrotated = 0;
                     }

                     tempRotation.assign(rotationToCheckUnrotated);
                     tempRotation.subtract(totalRotation);
                     fillerToCheckUnrotated = tempRotation.rotationPitch.subtract(rotationToCheckUnrotated.pitch()).rotateX(fillerToCheckUnrotated);
                     fillerToCheckUnrotated = tempRotation.rotationYaw.subtract(rotationToCheckUnrotated.yaw()).rotateY(fillerToCheckUnrotated);
                     fillerToCheckUnrotated = tempRotation.rotationRoll.subtract(rotationToCheckUnrotated.roll()).rotateY(fillerToCheckUnrotated);
                     rotationToCheckUnrotated = RotationTuple.of(tempRotation.rotationYaw, tempRotation.rotationPitch, tempRotation.rotationRoll);
                     BlockType blockTypeToCheckUnrotated = BlockType.getAssetMap().getAsset(blockToCheckUnrotated);
                     if (patternTransform.second() != PatternRotationDefinition.MirrorAxis.NONE) {
                        Rotation newYawMirrored = blockTypeToCheckUnrotated.getFlipType()
                           .flipYaw(rotationToCheckUnrotated.yaw(), patternTransform.second() == PatternRotationDefinition.MirrorAxis.X ? Axis.X : Axis.Z);
                        fillerToCheckUnrotated = newYawMirrored.subtract(rotationToCheckUnrotated.yaw()).rotateY(fillerToCheckUnrotated);
                        rotationToCheckUnrotated = RotationTuple.of(newYawMirrored, rotationToCheckUnrotated.pitch(), rotationToCheckUnrotated.roll());
                     }

                     boolean patternMatches = checkPatternRuleAgainstBlockType(
                        connectedBlockRuleset, shapeTemplate, ruleToMatch, blockToCheckUnrotated, rotationToCheckUnrotated, fillerToCheckUnrotated
                     );
                     if (!patternMatches) {
                        i++;
                        continue label126;
                     }
                  }
               }

               BlockPattern resultBlockPattern = connectedBlockRuleset.getShapeNameToBlockPatternMap().get(shapeName);
               if (resultBlockPattern == null) {
                  return Optional.empty();
               }

               random.setSeed(BlockUtil.pack(coordinate));
               BlockPattern.BlockEntry resultBlockTypeKey = resultBlockPattern.nextBlockTypeKey(random);
               if (resultBlockTypeKey == null) {
                  return Optional.empty();
               }

               BlockType baseBlockTypeForFlip = BlockType.getAssetMap().getAsset(resultBlockTypeKey.blockTypeKey());
               if (baseBlockTypeForFlip == null) {
                  return Optional.empty();
               }

               BlockFlipType flipType = baseBlockTypeForFlip.getFlipType();
               RotationTuple resultRotation = RotationTuple.get(resultBlockTypeKey.rotation());
               resultRotation = RotationTuple.of(resultRotation.yaw().add(this.yawToApplyAddReplacedBlockType), resultRotation.pitch(), resultRotation.roll());
               if (patternTransform.second() != PatternRotationDefinition.MirrorAxis.NONE) {
                  Rotation newYawMirrored = flipType.flipYaw(
                     resultRotation.yaw(), patternTransform.second() == PatternRotationDefinition.MirrorAxis.X ? Axis.X : Axis.Z
                  );
                  resultRotation = RotationTuple.of(newYawMirrored, resultRotation.pitch(), resultRotation.roll());
               }

               resultRotation = RotationTuple.of(
                  resultRotation.yaw().add(totalRotation.rotationYaw),
                  resultRotation.pitch().add(totalRotation.rotationPitch),
                  resultRotation.roll().add(totalRotation.rotationRoll)
               );
               if (resultRotation.pitch().equals(Rotation.OneEighty) && flipType.equals(BlockFlipType.ORTHOGONAL)) {
                  resultRotation = RotationTuple.of(resultRotation.yaw().subtract(Rotation.Ninety), resultRotation.pitch(), resultRotation.roll());
               }

               return Optional.of(new ConnectedBlocksUtil.ConnectedBlockResult(resultBlockTypeKey.blockTypeKey(), resultRotation.index()));
            }

            return Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }
}
