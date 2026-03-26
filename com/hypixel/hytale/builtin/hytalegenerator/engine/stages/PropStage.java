package com.hypixel.hytale.builtin.hytalegenerator.engine.stages;

import com.hypixel.hytale.builtin.hytalegenerator.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.PropRuntime;
import com.hypixel.hytale.builtin.hytalegenerator.Registry;
import com.hypixel.hytale.builtin.hytalegenerator.biome.Biome;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.BufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.CountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.EntityBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.SimplePixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.VoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.BufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.bufferbundle.buffers.type.ParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.engine.views.EntityBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.engine.views.PixelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.engine.views.VoxelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.propdistributions.PropDistribution;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PropStage implements Stage {
   public static final double DEFAULT_BACKGROUND_DENSITY = 0.0;
   @Nonnull
   public static final Class<CountedPixelBuffer> biomeBufferClass = CountedPixelBuffer.class;
   @Nonnull
   public static final Class<Integer> biomeClass = Integer.class;
   @Nonnull
   public static final Class<SimplePixelBuffer> biomeDistanceBufferClass = SimplePixelBuffer.class;
   @Nonnull
   public static final Class<BiomeDistanceStage.BiomeDistanceEntries> biomeDistanceClass = BiomeDistanceStage.BiomeDistanceEntries.class;
   @Nonnull
   public static final Class<VoxelBuffer> materialBufferClass = VoxelBuffer.class;
   @Nonnull
   public static final Class<Material> materialClass = Material.class;
   @Nonnull
   public static final Class<EntityBuffer> entityBufferClass = EntityBuffer.class;
   @Nonnull
   private final ParametrizedBufferType biomeInputBufferType;
   @Nonnull
   private final ParametrizedBufferType biomeDistanceInputBufferType;
   @Nonnull
   private final ParametrizedBufferType materialInputBufferType;
   @Nullable
   private final BufferType entityInputBufferType;
   @Nonnull
   private final ParametrizedBufferType materialOutputBufferType;
   @Nonnull
   private final BufferType entityOutputBufferType;
   @Nonnull
   private final Bounds3i materialInputBounds_bufferGrid;
   @Nonnull
   private final Bounds3i materialInputBounds_voxelGrid;
   @Nonnull
   private final Bounds3i biomeInputBounds_bufferGrid;
   @Nonnull
   private final Bounds3i positionsBounds_voxelGrid;
   @Nonnull
   private final Bounds3i positionsBounds_bufferGrid;
   @Nonnull
   private final String stageName;
   @Nonnull
   private final MaterialCache materialCache;
   @Nonnull
   private final WorkerIndexer.Data<WorldStructure> worldStructure_workerData;
   private final int runtimeIndex;

   public PropStage(
      @Nonnull String stageName,
      @Nonnull ParametrizedBufferType biomeInputBufferType,
      @Nonnull ParametrizedBufferType biomeDistanceInputBufferType,
      @Nonnull ParametrizedBufferType materialInputBufferType,
      @Nullable BufferType entityInputBufferType,
      @Nonnull ParametrizedBufferType materialOutputBufferType,
      @Nonnull BufferType entityOutputBufferType,
      @Nonnull MaterialCache materialCache,
      @Nonnull WorkerIndexer.Data<WorldStructure> worldStructure_workerData,
      int runtimeIndex
   ) {
      assert biomeInputBufferType.isValidType(biomeBufferClass, biomeClass);

      assert biomeDistanceInputBufferType.isValidType(biomeDistanceBufferClass, biomeDistanceClass);

      assert materialInputBufferType.isValidType(materialBufferClass, materialClass);

      assert entityInputBufferType == null || entityInputBufferType.isValidType(entityBufferClass);

      assert materialOutputBufferType.isValidType(materialBufferClass, materialClass);

      assert entityOutputBufferType.isValidType(entityBufferClass);

      this.biomeInputBufferType = biomeInputBufferType;
      this.biomeDistanceInputBufferType = biomeDistanceInputBufferType;
      this.materialInputBufferType = materialInputBufferType;
      this.entityInputBufferType = entityInputBufferType;
      this.materialOutputBufferType = materialOutputBufferType;
      this.entityOutputBufferType = entityOutputBufferType;
      this.worldStructure_workerData = worldStructure_workerData;
      this.stageName = stageName;
      this.materialCache = materialCache;
      this.runtimeIndex = runtimeIndex;
      List<Biome> allBiomes = new ArrayList<>();
      this.worldStructure_workerData.get(WorkerIndexer.Id.MAIN).getBiomeRegistry().forEach((biomeId, biomex) -> allBiomes.add(biomex));
      this.materialInputBounds_voxelGrid = new Bounds3i();
      this.positionsBounds_voxelGrid = new Bounds3i();

      for (Biome biome : allBiomes) {
         for (PropRuntime propRuntime : biome.getPropRuntimes()) {
            if (propRuntime.getRuntimeIndex() == this.runtimeIndex) {
               propRuntime.getPropDistribution().forEachPossibleProp(prop -> {
                  Bounds3i readBounds_voxelGrid = prop.getReadBounds_voxelGrid();
                  Bounds3i writeBounds_voxelGrid = prop.getWriteBounds_voxelGrid();
                  Bounds3i propInputBounds_voxelGrid = toInputBounds_voxelGrid(readBounds_voxelGrid, writeBounds_voxelGrid);
                  this.materialInputBounds_voxelGrid.encompass(propInputBounds_voxelGrid);
                  Bounds3i positionsBounds_voxelGrid = toPositionsBounds_voxelGrid(writeBounds_voxelGrid);
                  this.positionsBounds_voxelGrid.encompass(positionsBounds_voxelGrid);
               });
            }
         }
      }

      this.materialInputBounds_voxelGrid.min.y = 0;
      this.materialInputBounds_voxelGrid.max.y = 320;
      this.materialInputBounds_bufferGrid = GridUtils.createBufferBoundsInclusive_fromVoxelBounds(this.materialInputBounds_voxelGrid);
      if (this.materialInputBounds_bufferGrid.isZeroVolume()) {
         this.materialInputBounds_bufferGrid.encompass(Vector3i.ZERO);
      }

      GridUtils.setBoundsYToWorldHeight_bufferGrid(this.materialInputBounds_bufferGrid);
      this.biomeInputBounds_bufferGrid = GridUtils.createBufferBoundsInclusive_fromVoxelBounds(this.positionsBounds_voxelGrid);
      GridUtils.setBoundsYToWorldHeight_bufferGrid(this.biomeInputBounds_bufferGrid);
      this.positionsBounds_bufferGrid = GridUtils.createBufferBoundsInclusive_fromVoxelBounds(this.positionsBounds_voxelGrid);
      GridUtils.setBoundsYToWorldHeight_bufferGrid(this.positionsBounds_bufferGrid);
   }

   @Nonnull
   private static Bounds3i toInputBounds_voxelGrid(@Nonnull Bounds3i readBounds_voxelGrid, @Nonnull Bounds3i writeBounds_voxelGrid) {
      if (readBounds_voxelGrid.isZeroVolume()) {
         return new Bounds3i();
      } else {
         Bounds3i out = writeBounds_voxelGrid.clone().flipOnOriginVoxel();
         out.stack(readBounds_voxelGrid);
         return out;
      }
   }

   @Nonnull
   private static Bounds3i toPositionsBounds_voxelGrid(@Nonnull Bounds3i writeBounds_voxelGrid) {
      return writeBounds_voxelGrid.clone().flipOnOriginVoxel();
   }

   @Override
   public void run(@Nonnull Stage.Context context) {
      BufferBundle.Access.View biomeAccess = context.bufferAccess.get(this.biomeInputBufferType);
      PixelBufferView<Integer> biomeInputSpace = new PixelBufferView<>(biomeAccess, biomeClass);
      BufferBundle.Access.View biomeDistanceAccess = context.bufferAccess.get(this.biomeDistanceInputBufferType);
      PixelBufferView<BiomeDistanceStage.BiomeDistanceEntries> biomeDistanceSpace = new PixelBufferView<>(biomeDistanceAccess, biomeDistanceClass);
      BufferBundle.Access.View materialInputAccess = context.bufferAccess.get(this.materialInputBufferType);
      VoxelBufferView<Material> materialInputSpace = new VoxelBufferView<>(materialInputAccess, materialClass);
      BufferBundle.Access.View materialOutputAccess = context.bufferAccess.get(this.materialOutputBufferType);
      VoxelBufferView<Material> materialOutputSpace = new VoxelBufferView<>(materialOutputAccess, materialClass);
      BufferBundle.Access.View entityOutputAccess = context.bufferAccess.get(this.entityOutputBufferType);
      EntityBufferView entityOutputSpace = new EntityBufferView(entityOutputAccess);
      Bounds3i localOutputBounds_voxelGrid = materialOutputSpace.getBounds();
      Bounds3i localMaterialInputBounds_voxelGrid = materialInputSpace.getBounds();
      Bounds3i localPositionsBounds_voxelGrid = localOutputBounds_voxelGrid.clone();
      localPositionsBounds_voxelGrid.stack(this.positionsBounds_voxelGrid.clone());
      localPositionsBounds_voxelGrid.min.y = 0;
      localPositionsBounds_voxelGrid.max.y = 320;
      materialOutputSpace.copyFrom(materialInputSpace);
      if (this.entityInputBufferType != null) {
         BufferBundle.Access.View entityInputAccess = context.bufferAccess.get(this.entityInputBufferType);
         EntityBufferView entityInputSpace = new EntityBufferView(entityInputAccess);
         entityOutputSpace.copyFrom(entityInputSpace);
      }

      Registry<Biome> biomeRegistry = this.worldStructure_workerData.get(context.workerId).getBiomeRegistry();
      HashSet<Integer> traversedBiomes = new HashSet<>();
      List<Biome> biomesInBuffer = new ArrayList<>();

      for (int x = localPositionsBounds_voxelGrid.min.x; x < localPositionsBounds_voxelGrid.max.x; x++) {
         for (int z = localPositionsBounds_voxelGrid.min.z; z < localPositionsBounds_voxelGrid.max.z; z++) {
            Integer biomeId = biomeInputSpace.get(x, 0, z);
            if (!traversedBiomes.contains(biomeId)) {
               traversedBiomes.add(biomeId);
               Biome biome = biomeRegistry.getObject(biomeId);
               biomesInBuffer.add(biome);
            }
         }
      }

      Map<PropRuntime, Biome> propRuntimeBiomeMap = new HashMap<>();

      for (Biome biome : biomesInBuffer) {
         biome.getRuntimesWithIndex(this.runtimeIndex, propRuntimex -> propRuntimeBiomeMap.put(propRuntimex, biome));
      }

      Prop.Context propContext = new Prop.Context(new Vector3i(), materialInputSpace, materialOutputSpace, entityOutputSpace, Double.MAX_VALUE);
      Bounds3i propReadBounds_voxelGrid = new Bounds3i();
      Bounds3i propWriteBounds_voxelGrid = new Bounds3i();
      Vector3i position2d_voxelGrid = new Vector3i();
      PropDistribution.Context distributionContext = new PropDistribution.Context(new Bounds3d(), Pipe.getEmptyTwo(), Double.MAX_VALUE);

      for (Entry<PropRuntime, Biome> entry : propRuntimeBiomeMap.entrySet()) {
         PropRuntime propRuntime = entry.getKey();
         Biome biome = entry.getValue();
         PropDistribution propDistribution = propRuntime.getPropDistribution();
         Pipe.Two<Vector3d, Prop> propDistributionPipe = (position_voxelGrid, prop, control) -> {
            int positionX_voxelGrid = (int)Math.floor(position_voxelGrid.x);
            int positionY_voxelGrid = (int)Math.floor(position_voxelGrid.y);
            int positionZ_voxelGrid = (int)Math.floor(position_voxelGrid.z);
            propWriteBounds_voxelGrid.assign(prop.getWriteBounds_voxelGrid());
            propWriteBounds_voxelGrid.offset(positionX_voxelGrid, positionY_voxelGrid, positionZ_voxelGrid);
            if (propWriteBounds_voxelGrid.intersects(localOutputBounds_voxelGrid)) {
               propReadBounds_voxelGrid.assign(prop.getReadBounds_voxelGrid());
               propReadBounds_voxelGrid.offset(positionX_voxelGrid, positionY_voxelGrid, positionZ_voxelGrid);
               if (propReadBounds_voxelGrid.isZeroVolume() || localMaterialInputBounds_voxelGrid.intersects(propReadBounds_voxelGrid)) {
                  Integer biomeIdAtPosition = biomeInputSpace.get(positionX_voxelGrid, 0, positionZ_voxelGrid);
                  Biome biomeAtPosition = biomeRegistry.getObject(biomeIdAtPosition);
                  if (biomeAtPosition == biome) {
                     position2d_voxelGrid.assign(positionX_voxelGrid, 0, positionZ_voxelGrid);
                     double distanceToBiomeEdge = biomeDistanceSpace.get(position2d_voxelGrid).distanceToClosestOtherBiome(biomeIdAtPosition);
                     propContext.distanceToBiomeEdge = distanceToBiomeEdge;
                     propContext.position.assign(positionX_voxelGrid, positionY_voxelGrid, positionZ_voxelGrid);
                     prop.generate(propContext);
                  }
               }
            }
         };
         distributionContext.bounds.assign(localPositionsBounds_voxelGrid);
         distributionContext.pipe = propDistributionPipe;
         propDistribution.distribute(distributionContext);
      }
   }

   @Nonnull
   @Override
   public Map<BufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      Map<BufferType, Bounds3i> map = new HashMap<>();
      map.put(this.biomeInputBufferType, this.biomeInputBounds_bufferGrid);
      map.put(this.biomeDistanceInputBufferType, this.positionsBounds_bufferGrid);
      map.put(this.materialInputBufferType, this.materialInputBounds_bufferGrid);
      if (this.entityInputBufferType != null) {
         map.put(this.entityInputBufferType, this.materialInputBounds_bufferGrid);
      }

      return map;
   }

   @Nonnull
   @Override
   public List<BufferType> getOutputTypes() {
      return List.of(this.materialOutputBufferType, this.entityOutputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return this.stageName;
   }
}
