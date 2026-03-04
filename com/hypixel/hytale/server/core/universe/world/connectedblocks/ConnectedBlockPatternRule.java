package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.buildertool.config.BlockTypeListAsset;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConnectedBlockPatternRule {
   public static final BuilderCodec<ConnectedBlockPatternRule> CODEC = BuilderCodec.builder(ConnectedBlockPatternRule.class, ConnectedBlockPatternRule::new)
      .append(new KeyedCodec<>("Position", Vector3i.CODEC, false), (o, relativePosition) -> o.relativePosition = relativePosition, o -> o.relativePosition)
      .add()
      .append(
         new KeyedCodec<>("IncludeOrExclude", new EnumCodec<>(ConnectedBlockPatternRule.IncludeOrExclude.class), true),
         (o, allowOrExclude) -> o.includeOrExclude = allowOrExclude,
         o -> o.includeOrExclude
      )
      .add()
      .append(
         new KeyedCodec<>(
            "PlacementNormals",
            new ArrayCodec<>(new EnumCodec<>(ConnectedBlockPatternRule.AdjacentSide.class), ConnectedBlockPatternRule.AdjacentSide[]::new),
            false
         ),
         (o, placementNormals) -> o.placementNormals = placementNormals,
         o -> o.placementNormals
      )
      .add()
      .documentation("Queries the face the block was placed against")
      .append(new KeyedCodec<>("FaceTags", ConnectedBlockFaceTags.CODEC, false), (o, faceTags) -> o.faceTags = faceTags, o -> o.faceTags)
      .add()
      .append(
         new KeyedCodec<>("Shapes", new SetCodec<>(BlockPattern.BlockEntry.CODEC, HashSet::new, true)),
         (o, blockTypesAllowed) -> o.shapeBlockTypeKeys = blockTypesAllowed,
         o -> o.shapeBlockTypeKeys
      )
      .add()
      .append(new KeyedCodec<>("BlockTypes", new ArrayCodec<>(Codec.STRING, String[]::new)), (o, blockTypesAllowed) -> {
         if (blockTypesAllowed != null) {
            Collections.addAll(o.blockTypes, blockTypesAllowed);
         }
      }, o -> o.blockTypes != null ? o.blockTypes.toArray(String[]::new) : null)
      .add()
      .append(new KeyedCodec<>("BlockTypeLists", Codec.STRING_ARRAY), (o, blockTypeListAssetsAllowed) -> {
         if (blockTypeListAssetsAllowed != null) {
            o.blockTypeListAssets = new BlockTypeListAsset[blockTypeListAssetsAllowed.length];

            for (int i = 0; i < blockTypeListAssetsAllowed.length; i++) {
               o.blockTypeListAssets[i] = BlockTypeListAsset.getAssetMap().getAsset(blockTypeListAssetsAllowed[i]);
               if (o.blockTypeListAssets[i] == null) {
                  System.out.println("BlockTypeListAsset with name: " + blockTypeListAssetsAllowed[i] + " does not exist");
               }
            }
         }
      }, o -> {
         if (o.blockTypeListAssets == null) {
            return null;
         } else {
            String[] assetIds = new String[o.blockTypeListAssets.length];

            for (int i = 0; i < o.blockTypeListAssets.length; i++) {
               assetIds[i] = o.blockTypeListAssets[i].getId();
            }

            return assetIds;
         }
      })
      .add()
      .build();
   private ConnectedBlockPatternRule.IncludeOrExclude includeOrExclude;
   private Vector3i relativePosition = Vector3i.ZERO;
   private final HashSet<String> blockTypes = new HashSet<>();
   @Nullable
   private BlockTypeListAsset[] blockTypeListAssets;
   private Set<BlockPattern.BlockEntry> shapeBlockTypeKeys = Collections.emptySet();
   private ConnectedBlockFaceTags faceTags = ConnectedBlockFaceTags.EMPTY;
   private ConnectedBlockPatternRule.AdjacentSide[] placementNormals;

   public Vector3i getRelativePosition() {
      return this.relativePosition;
   }

   @Nonnull
   public HashSet<String> getBlockTypes() {
      return this.blockTypes;
   }

   @Nonnull
   public Set<BlockPattern.BlockEntry> getShapeBlockTypeKeys() {
      return this.shapeBlockTypeKeys;
   }

   public ConnectedBlockFaceTags getFaceTags() {
      return this.faceTags;
   }

   @Nullable
   public BlockTypeListAsset[] getBlockTypeListAssets() {
      return this.blockTypeListAssets;
   }

   public ConnectedBlockPatternRule.AdjacentSide[] getPlacementNormals() {
      return this.placementNormals;
   }

   public boolean isInclude() {
      return this.includeOrExclude == ConnectedBlockPatternRule.IncludeOrExclude.INCLUDE;
   }

   public static enum AdjacentSide {
      Up(Vector3i.UP),
      Down(Vector3i.DOWN),
      North(Vector3i.NORTH),
      East(Vector3i.EAST),
      South(Vector3i.SOUTH),
      West(Vector3i.WEST);

      public final Vector3i relativePosition;

      private AdjacentSide(Vector3i side) {
         this.relativePosition = side;
      }
   }

   public static enum IncludeOrExclude {
      INCLUDE,
      EXCLUDE;
   }
}
