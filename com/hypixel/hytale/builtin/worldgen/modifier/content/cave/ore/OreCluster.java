package com.hypixel.hytale.builtin.worldgen.modifier.content.cave.ore;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.builtin.worldgen.modifier.content.cave.CaveTypeContent;
import com.hypixel.hytale.builtin.worldgen.modifier.content.cave.CaveTypeGenerator;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.BlockEntry;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.HeightMask;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.FloatRange;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.cave.CaveBiomeMaskFlags;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import com.hypixel.hytale.server.worldgen.cave.shape.EmptyLineCaveNodeShape;
import com.hypixel.hytale.server.worldgen.cave.shape.PipeCaveNodeShape;
import javax.annotation.Nonnull;

public class OreCluster implements CaveTypeGenerator {
   public static final String ID = "OreCluster";
   public static final BuilderCodec<OreCluster> CODEC = BuilderCodec.builder(OreCluster.class, OreCluster::new)
      .documentation(
         "Ore cluster generator: at each grid point picks a random height, radius, length, for the cluster shape, and a random selection of blocks to place"
      )
      .<BlockEntry[]>append(new KeyedCodec<>("Blocks", BlockEntry.ARRAY_CODEC), (t, v) -> t.blocks = v, t -> t.blocks)
      .documentation("Weighted list of block types to randomly fill the ore cluster with")
      .add()
      .<IntRange>append(new KeyedCodec<>("Radius", Codecs.INT_RANGE), (t, v) -> t.radius = v, t -> t.radius)
      .documentation("The radius range of the ore cluster shape")
      .add()
      .<IntRange>append(new KeyedCodec<>("Length", Codecs.INT_RANGE), (t, v) -> t.length = v, t -> t.length)
      .documentation("The length range of the ore cluster shape")
      .add()
      .<IntRange>append(new KeyedCodec<>("Repetitions", Codecs.INT_RANGE), (t, v) -> t.repetitions = v, t -> t.repetitions)
      .documentation("The number of ore clusters to generate per grid point")
      .add()
      .build();
   protected static final FloatRange.Normal YAW = new FloatRange.Normal((float) -Math.PI, (float) Math.PI);
   protected static final FloatRange.Constant PITCH_UP = new FloatRange.Constant((float) Math.PI);
   protected static final FloatRange.Constant NODE_RECURSION_DEPTH = new FloatRange.Constant(2.0F);
   protected BlockEntry[] blocks = BlockEntry.EMPTY_ARRAY;
   protected IntRange radius = new IntRange();
   protected IntRange length = new IntRange();
   protected IntRange repetitions = new IntRange(1, 3);

   @Nonnull
   @Override
   public CaveType create(@Nonnull SeedString<SeedStringResource> seed, @Nonnull CaveTypeContent cave) {
      CaveNodeType clusterType = CaveTypeContent.Util.createNodeType(cave.name(), BlockEntry.build(this.blocks), this.buildClusterShape());
      CaveNodeType.CaveNodeChildEntry clusterNode = CaveTypeContent.Util.createChildEntry(clusterType, CaveTypeContent.Util.INT_RANGE_ONE, false);
      CaveNodeType startType = CaveTypeContent.Util.createNodeType("Start", CaveTypeContent.Util.EMPTY_FILLING, this.buildStartShape(cave), clusterNode);
      CaveNodeType.CaveNodeChildEntry startNode = CaveTypeContent.Util.createChildEntry(startType, this.repetitions, true);
      CaveNodeType entryNodeType = CaveTypeContent.Util.createNodeType("Entry", CaveTypeContent.Util.EMPTY_FILLING, CaveTypeContent.Util.EMPTY_SHAPE, startNode);
      return new CaveType(
         cave.name(),
         entryNodeType,
         YAW,
         PITCH_UP,
         NODE_RECURSION_DEPTH,
         HeightMask.DEFAULT_HEIGHT_THRESHOLD,
         cave.grid().build(seed),
         CaveBiomeMaskFlags.DEFAULT_ALLOW,
         cave.mask().getBlockMask(),
         DefaultCoordinateCondition.DEFAULT_TRUE,
         DefaultCoordinateCondition.DEFAULT_TRUE,
         CaveTypeContent.Util.FIXED_ENTRY_HEIGHT,
         (NoiseProperty)null,
         CaveType.FluidLevel.EMPTY,
         0,
         true,
         true,
         this.radius.getInclusiveMax()
      );
   }

   protected CaveNodeShapeEnum.CaveNodeShapeGenerator buildStartShape(@Nonnull CaveTypeContent config) {
      return new EmptyLineCaveNodeShape.EmptyLineCaveNodeShapeGenerator(
         new DoubleRange.Normal(config.height().getInclusiveMin(), config.height().getInclusiveMax())
      );
   }

   protected CaveNodeShapeEnum.CaveNodeShapeGenerator buildClusterShape() {
      return new PipeCaveNodeShape.PipeCaveNodeShapeGenerator(
         new DoubleRange.Normal(this.radius.getInclusiveMin(), this.radius.getInclusiveMax()),
         new DoubleRange.Normal(this.radius.getInclusiveMin(), this.radius.getInclusiveMax()),
         new DoubleRange.Normal(this.length.getInclusiveMin(), this.length.getInclusiveMax()),
         false
      );
   }
}
