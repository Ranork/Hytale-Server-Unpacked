package com.hypixel.hytale.builtin.hytalegenerator.newsystem.stages;

import com.hypixel.hytale.builtin.hytalegenerator.PropField;
import com.hypixel.hytale.builtin.hytalegenerator.Registry;
import com.hypixel.hytale.builtin.hytalegenerator.biome.Biome;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3d;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NCountedPixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NEntityBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NSimplePixelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NVoxelBuffer;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.type.NParametrizedBufferType;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NEntityBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NPixelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.NVoxelBufferView;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPropStage implements NStage {
   public static final double DEFAULT_BACKGROUND_DENSITY = 0.0;
   @Nonnull
   public static final Class<NCountedPixelBuffer> biomeBufferClass = NCountedPixelBuffer.class;
   @Nonnull
   public static final Class<Integer> biomeClass = Integer.class;
   @Nonnull
   public static final Class<NSimplePixelBuffer> biomeDistanceBufferClass = NSimplePixelBuffer.class;
   @Nonnull
   public static final Class<NBiomeDistanceStage.BiomeDistanceEntries> biomeDistanceClass = NBiomeDistanceStage.BiomeDistanceEntries.class;
   @Nonnull
   public static final Class<NVoxelBuffer> materialBufferClass = NVoxelBuffer.class;
   @Nonnull
   public static final Class<Material> materialClass = Material.class;
   @Nonnull
   public static final Class<NEntityBuffer> entityBufferClass = NEntityBuffer.class;
   @Nonnull
   private final NParametrizedBufferType biomeInputBufferType;
   @Nonnull
   private final NParametrizedBufferType biomeDistanceInputBufferType;
   @Nonnull
   private final NParametrizedBufferType materialInputBufferType;
   @Nullable
   private final NBufferType entityInputBufferType;
   @Nonnull
   private final NParametrizedBufferType materialOutputBufferType;
   @Nonnull
   private final NBufferType entityOutputBufferType;
   @Nonnull
   private final Bounds3i inputBounds_bufferGrid;
   @Nonnull
   private final Bounds3i inputBounds_voxelGrid;
   @Nonnull
   private final String stageName;
   @Nonnull
   private final MaterialCache materialCache;
   @Nonnull
   private final WorkerIndexer.Data<WorldStructure> worldStructure_workerData;
   private final int runtimeIndex;

   public NPropStage(
      @Nonnull String stageName,
      @Nonnull NParametrizedBufferType biomeInputBufferType,
      @Nonnull NParametrizedBufferType biomeDistanceInputBufferType,
      @Nonnull NParametrizedBufferType materialInputBufferType,
      @Nullable NBufferType entityInputBufferType,
      @Nonnull NParametrizedBufferType materialOutputBufferType,
      @Nonnull NBufferType entityOutputBufferType,
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
      this.worldStructure_workerData
         .forEach((workerId, worldStructure) -> worldStructure.getBiomeRegistry().forEach((biomeId, biomex) -> allBiomes.add(biomex)));
      this.inputBounds_voxelGrid = new Bounds3i();
      Vector3i range = new Vector3i();

      for (Biome biome : allBiomes) {
         for (PropField propField : biome.getPropFields()) {
            if (propField.getRuntime() == this.runtimeIndex) {
               for (Prop prop : propField.getPropDistribution().getAllPossibleProps()) {
                  Vector3i readRange_voxelGrid = prop.getContextDependency().getReadRange();
                  Vector3i writeRange_voxelGrid = prop.getContextDependency().getWriteRange();
                  range.x = readRange_voxelGrid.x + writeRange_voxelGrid.x;
                  range.y = readRange_voxelGrid.y + writeRange_voxelGrid.y;
                  range.z = readRange_voxelGrid.z + writeRange_voxelGrid.z;
                  this.inputBounds_voxelGrid.encompass(range.clone().add(Vector3i.ALL_ONES));
                  range.scale(-1);
                  this.inputBounds_voxelGrid.encompass(range);
               }
            }
         }
      }

      this.inputBounds_voxelGrid.min.y = 0;
      this.inputBounds_voxelGrid.max.y = 320;
      this.inputBounds_bufferGrid = GridUtils.createBufferBoundsInclusive_fromVoxelBounds(this.inputBounds_voxelGrid);
      GridUtils.setBoundsYToWorldHeight_bufferGrid(this.inputBounds_bufferGrid);
   }

   @Override
   public void run(@Nonnull NStage.Context context) {
      NBufferBundle.Access.View biomeAccess = context.bufferAccess.get(this.biomeInputBufferType);
      NPixelBufferView<Integer> biomeInputSpace = new NPixelBufferView<>(biomeAccess, biomeClass);
      NBufferBundle.Access.View biomeDistanceAccess = context.bufferAccess.get(this.biomeDistanceInputBufferType);
      NPixelBufferView<NBiomeDistanceStage.BiomeDistanceEntries> biomeDistanceSpace = new NPixelBufferView<>(biomeDistanceAccess, biomeDistanceClass);
      NBufferBundle.Access.View materialInputAccess = context.bufferAccess.get(this.materialInputBufferType);
      NVoxelBufferView<Material> materialInputSpace = new NVoxelBufferView<>(materialInputAccess, materialClass);
      NBufferBundle.Access.View materialOutputAccess = context.bufferAccess.get(this.materialOutputBufferType);
      NVoxelBufferView<Material> materialOutputSpace = new NVoxelBufferView<>(materialOutputAccess, materialClass);
      NBufferBundle.Access.View entityOutputAccess = context.bufferAccess.get(this.entityOutputBufferType);
      NEntityBufferView entityOutputSpace = new NEntityBufferView(entityOutputAccess);
      Bounds3i localOutputBounds_voxelGrid = materialOutputSpace.getBounds();
      Bounds3i localInputBounds_voxelGrid = this.inputBounds_voxelGrid.clone();
      Bounds3i absoluteOutputBounds_voxelGrid = localOutputBounds_voxelGrid.clone();
      absoluteOutputBounds_voxelGrid.offset(localOutputBounds_voxelGrid.min.clone().scale(-1));
      localInputBounds_voxelGrid.stack(absoluteOutputBounds_voxelGrid);
      localInputBounds_voxelGrid.offset(localOutputBounds_voxelGrid.min);
      localInputBounds_voxelGrid.min.y = 0;
      localInputBounds_voxelGrid.max.y = 320;
      Bounds3d localInputBoundsDouble_voxelGrid = localInputBounds_voxelGrid.toBounds3d();
      materialOutputSpace.copyFrom(materialInputSpace);
      if (this.entityInputBufferType != null) {
         NBufferBundle.Access.View entityInputAccess = context.bufferAccess.get(this.entityInputBufferType);
         NEntityBufferView entityInputSpace = new NEntityBufferView(entityInputAccess);
         entityOutputSpace.copyFrom(entityInputSpace);
      }

      Registry<Biome> biomeRegistry = this.worldStructure_workerData.get(context.workerId).getBiomeRegistry();
      HashSet<Biome> biomesInBuffer = new HashSet<>();

      for (int x = localInputBounds_voxelGrid.min.x; x < localInputBounds_voxelGrid.max.x; x++) {
         for (int z = localInputBounds_voxelGrid.min.z; z < localInputBounds_voxelGrid.max.z; z++) {
            Integer biomeId = biomeInputSpace.getContent(x, 0, z);
            Biome biome = biomeRegistry.getObject(biomeId);
            biomesInBuffer.add(biome);
         }
      }

      Map<PropField, Biome> propFieldBiomeMap = new HashMap<>();

      for (Biome biome : biomesInBuffer) {
         for (PropField propField : biome.getPropFields()) {
            if (propField.getRuntime() == this.runtimeIndex) {
               propFieldBiomeMap.put(propField, biome);
            }
         }
      }

      for (Entry<PropField, Biome> entry : propFieldBiomeMap.entrySet()) {
         PropField propFieldx = entry.getKey();
         Biome biome = entry.getValue();
         PositionProvider positionProvider = propFieldx.getPositionProvider();
         Consumer<Vector3d> positionsConsumer = position -> {
            if (localInputBoundsDouble_voxelGrid.contains(position)) {
               Vector3i positionInt_voxelGrid = position.toVector3i();
               Integer biomeIdAtPosition = biomeInputSpace.getContent(positionInt_voxelGrid.x, 0, positionInt_voxelGrid.z);
               Biome biomeAtPosition = biomeRegistry.getObject(biomeIdAtPosition);
               if (biomeAtPosition == biome) {
                  Vector3i position2d_voxelGrid = positionInt_voxelGrid.clone();
                  position2d_voxelGrid.setY(0);
                  double distanceToBiomeEdge = biomeDistanceSpace.getContent(position2d_voxelGrid).distanceToClosestOtherBiome(biomeIdAtPosition);
                  Prop prop = propField.getPropDistribution().propAt(position, context.workerId, distanceToBiomeEdge);
                  Bounds3i propWriteBounds = prop.getWriteBounds_voxelGrid().clone();
                  propWriteBounds.offset(positionInt_voxelGrid);
                  if (propWriteBounds.intersects(localOutputBounds_voxelGrid)) {
                     ScanResult scanResult = prop.scan(positionInt_voxelGrid, materialInputSpace, context.workerId);
                     Prop.Context propContext = new Prop.Context(scanResult, materialOutputSpace, entityOutputSpace, context.workerId, distanceToBiomeEdge);
                     prop.place(propContext);
                  }
               }
            }
         };
         PositionProvider.Context positionsContext = new PositionProvider.Context(
            localInputBoundsDouble_voxelGrid.min, localInputBoundsDouble_voxelGrid.max, positionsConsumer, null
         );
         positionProvider.positionsIn(positionsContext);
      }
   }

   @Nonnull
   @Override
   public Map<NBufferType, Bounds3i> getInputTypesAndBounds_bufferGrid() {
      Map<NBufferType, Bounds3i> map = new HashMap<>();
      map.put(this.biomeInputBufferType, this.inputBounds_bufferGrid);
      map.put(this.biomeDistanceInputBufferType, this.inputBounds_bufferGrid);
      map.put(this.materialInputBufferType, this.inputBounds_bufferGrid);
      if (this.entityInputBufferType != null) {
         map.put(this.entityInputBufferType, this.inputBounds_bufferGrid);
      }

      return map;
   }

   @Nonnull
   @Override
   public List<NBufferType> getOutputTypes() {
      return List.of(this.materialOutputBufferType, this.entityOutputBufferType);
   }

   @Nonnull
   @Override
   public String getName() {
      return this.stageName;
   }
}
