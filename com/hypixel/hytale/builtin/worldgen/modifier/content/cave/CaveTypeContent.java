package com.hypixel.hytale.builtin.worldgen.modifier.content.cave;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Content;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.BlockMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.HeightMask;
import com.hypixel.hytale.builtin.worldgen.modifier.content.common.PointGrid;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvent;
import com.hypixel.hytale.builtin.worldgen.modifier.event.ModifyEvents;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.math.range.IntRange;
import com.hypixel.hytale.procedurallib.condition.DefaultCoordinateCondition;
import com.hypixel.hytale.procedurallib.supplier.DoubleRange;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.worldgen.cave.CaveNodeType;
import com.hypixel.hytale.server.worldgen.cave.CaveYawMode;
import com.hypixel.hytale.server.worldgen.cave.prefab.CavePrefabContainer;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeEnum;
import com.hypixel.hytale.server.worldgen.cave.shape.EmptyLineCaveNodeShape;
import com.hypixel.hytale.server.worldgen.util.BlockFluidEntry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class CaveTypeContent implements Content {
   public static final String ID = "CaveType";
   public static final BuilderCodec<CaveTypeContent> CODEC = BuilderCodec.builder(CaveTypeContent.class, CaveTypeContent::new)
      .append(new KeyedCodec<>("Name", Codec.STRING), (t, v) -> t.name = v, t -> t.name)
      .documentation("The name of the cave type")
      .add()
      .<PointGrid>append(new KeyedCodec<>("PointGrid", PointGrid.CODEC), (t, v) -> t.grid = v, t -> t.grid)
      .documentation("The point grid configuration controlling where the cave starting points are placed horizontally")
      .add()
      .<HeightMask>append(new KeyedCodec<>("HeightRange", HeightMask.CODEC), (t, v) -> t.height = v, t -> t.height)
      .documentation("The height range within which the cave type can be placed")
      .add()
      .<BlockMask>append(new KeyedCodec<>("BlockMask", BlockMask.CODEC), (t, v) -> t.blockMask = v, t -> t.blockMask)
      .documentation("The block and fluid types that this cave type can replace")
      .add()
      .<CaveTypeGenerator>append(new KeyedCodec<>("Generator", CaveTypeGenerator.TYPE_CODEC), (t, v) -> t.generator = v, t -> t.generator)
      .documentation("The generator configuration for this cave type")
      .add()
      .build();
   protected String name = "";
   protected PointGrid grid = new PointGrid();
   protected HeightMask height = HeightMask.DEFAULT;
   protected BlockMask blockMask = BlockMask.REPLACE_SOLID;
   @Nullable
   protected CaveTypeGenerator generator = null;

   public String name() {
      return this.name;
   }

   public PointGrid grid() {
      return this.grid;
   }

   public BlockMask mask() {
      return this.blockMask;
   }

   public IntRange height() {
      return this.height.range();
   }

   @Override
   public Content.Type type() {
      return Content.Type.CAVE_TYPE;
   }

   @Override
   public <T> void applyTo(ModifyEvent<T> event) throws Exception {
      if (this.generator != null) {
         if (event instanceof ModifyEvents.CaveTypes container) {
            container.entries().add(this.generator.create(event.seed(), this));
         }
      }
   }

   public interface Util {
      IntRange INT_RANGE_ONE = new IntRange(1, 1);
      DoubleRange.Constant FIXED_ENTRY_HEIGHT = DoubleRange.ZERO;
      CaveNodeType.CaveNodeChildEntry.OrientationModifier DIRECT = (angle, rand) -> angle;
      CaveNodeType.CaveNodeChildEntry.OrientationModifier RANDOM = (angle, rand) -> angle + rand.nextFloat() * (float) (Math.PI * 2);
      CaveNodeShapeEnum.CaveNodeShapeGenerator EMPTY_SHAPE = new EmptyLineCaveNodeShape.EmptyLineCaveNodeShapeGenerator(DoubleRange.ZERO);
      IWeightedMap<BlockFluidEntry> EMPTY_FILLING = WeightedMap.builder(BlockFluidEntry.EMPTY_ARRAY).put(BlockFluidEntry.EMPTY, 1.0).build();

      static CaveNodeType createNodeType(
         @Nonnull String name,
         @Nonnull IWeightedMap<BlockFluidEntry> filling,
         @Nonnull CaveNodeShapeEnum.CaveNodeShapeGenerator shape,
         @Nonnull CaveNodeType.CaveNodeChildEntry... children
      ) {
         CaveNodeType type = new CaveNodeType(
            name,
            (CavePrefabContainer)null,
            filling,
            shape,
            DefaultCoordinateCondition.DEFAULT_TRUE,
            (IDoubleRange)null,
            CaveNodeType.CaveNodeCoverEntry.EMPTY_ARRAY,
            6,
            0
         );
         type.setChildren(children);
         return type;
      }

      static CaveNodeType.CaveNodeChildEntry createChildEntry(@Nonnull CaveNodeType type, @Nonnull IntRange repetitions, boolean randomDir) {
         return new CaveNodeType.CaveNodeChildEntry(
            WeightedMap.builder(CaveNodeType.EMPTY_ARRAY).put(type, 1.0).build(),
            new Vector3d(0.0),
            new Vector3d(0.0),
            PrefabRotation.VALUES,
            (IDoubleRange)null,
            new DoubleRange.Normal(repetitions.getInclusiveMin(), repetitions.getInclusiveMax()),
            randomDir ? RANDOM : DIRECT,
            randomDir ? RANDOM : DIRECT,
            1.0,
            CaveYawMode.NODE
         );
      }
   }
}
