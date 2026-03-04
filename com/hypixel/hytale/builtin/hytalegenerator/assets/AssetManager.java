package com.hypixel.hytale.builtin.hytalegenerator.assets;

import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.event.LoadedAssetsEvent;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask.BlockMaskAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CeilingCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ClampCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.DistanceExponentialCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.DistanceSCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.FloorCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ImportedCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.InverterCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.MinCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.MultiplierCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.NotCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.SmoothCeilingCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.SmoothClampCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.SmoothFloorCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.SmoothMaxCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.SmoothMinCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.SumCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.manual.ManualCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AbsDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AmplitudeConstantAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AmplitudeDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AnchorDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AngleDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.AxisDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.BaseHeightDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.Cache2dDensityAsset_Deprecated;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CacheDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CeilingDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CellNoise2DDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CellNoise3DDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CellWallDistanceDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ClampDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CubeDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CuboidDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CurveMapperDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.CylinderDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DistanceDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DistanceToBiomeEdgeDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.EllipsoidDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ExportedDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.FastGradientWarpDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.FloorDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.GradientDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.GradientWarpDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ImportedDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.InverterDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MaxDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MinDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MixDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MultiMixDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.MultiplierDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.NormalizerDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.OffsetConstantAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.OffsetDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.PipelineDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.PlaneDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.PositionsPinchDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.PositionsTwistDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.PowDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.RotatorDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ScaleDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ShellDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SimplexNoise2dDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SimplexNoise3DDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SliderDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SmoothCeilingDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SmoothClampDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SmoothFloorDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SmoothMaxDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SmoothMinDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SqrtDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SumDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SwitchDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.SwitchStateDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.TerrainDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.VectorWarpDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.XOverrideDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.XValueDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.YOverrideDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.YSampledDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.YValueDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ZOverrideDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ZValueDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.Positions3DDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.PositionsCellNoiseDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.DistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.EuclideanDistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions.ManhattanDistanceFunctionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.CellValueReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.CurveReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.DensityReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.Distance2AddReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.Distance2DivReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.Distance2MulReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.Distance2ReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.Distance2SubReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.DistanceReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.ImportedReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.returntypes.ReturnTypeAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders.ConstantEnvironmentProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders.DensityDelimitedEnvironmentProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders.EnvironmentProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.DecimalConstantsFrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.FrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.framework.PositionsFrameworkAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.DownwardDepthMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.DownwardSpaceMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.FieldFunctionMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ImportedMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.QueueMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.SimpleHorizontalMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.SolidityMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.StripedMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.TerrainDensityMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.UpwardDepthMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.UpwardSpaceMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.WeightedMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.SpaceAndDepthMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.AlwaysTrueConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.AndConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.ConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.EqualsConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.GreaterThanConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.NotConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.OrConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.SmallerThanConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.ConstantThicknessLayerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.LayerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.NoiseThicknessAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.RangeThicknessAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.WeightedThicknessLayerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.noisegenerators.CellNoiseAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.noisegenerators.NoiseAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.noisegenerators.SimplexNoiseAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.AndPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.BlockSetPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.CeilingPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ConstantPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.CuboidPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.DensityPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.FloorPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.GapPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.ImportedPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.MaterialPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.NotPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.OffsetPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.OrPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.PatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.SurfacePatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.patterns.WallPatternAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators.MeshPointGeneratorAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.pointgenerators.PointGeneratorAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.AnchorPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.BaseHeightPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.BoundPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.CachedPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.FieldFunctionOccurrencePositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.FieldFunctionPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.FrameworkPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ImportedPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.ListPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.Mesh2DPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.Mesh3DPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.OffsetPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.SimpleHorizontalPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.UnionPositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.AssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.ConstantAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.FieldFunctionAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.ImportedAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.SandwichAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.propassignments.WeightedAssignmentsAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.BoxPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.ClusterPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.ColumnPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.DensityPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.ImportedPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.OffsetPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PondFillerPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.QueuePropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.UnionPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.WeightedPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.PrefabPropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.DirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.ImportedDirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.PatternDirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.RandomDirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality.StaticDirectionalityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.AreaScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ColumnLinearScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ColumnRandomScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ImportedScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.OriginScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.scanners.ScannerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.terrains.DensityTerrainAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.terrains.TerrainAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.tintproviders.ConstantTintProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.tintproviders.DensityDelimitedTintProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.tintproviders.TintProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.CacheVectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.ConstantVectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.DensityGradientVectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.ExportedVectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.ImportedVectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.VectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.basic.BasicWorldStructureAsset;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.event.EventRegistry;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class AssetManager {
   @Nonnull
   private final HashMap<String, DensityAsset> densityAssets;
   @Nonnull
   private final HashMap<String, AssignmentsAsset> assigmentAssets;
   @Nonnull
   private final HashMap<String, BiomeAsset> biomeAssets;
   @Nonnull
   private final HashMap<String, WorldStructureAsset> worldStructureAssets;
   @Nonnull
   private final HashMap<String, BlockMaskAsset> blockMaskAssets;
   private SettingsAsset settingsAsset;
   @Nonnull
   private final HytaleLogger logger;
   private List<Runnable> reloadListeners;

   public AssetManager(@Nonnull EventRegistry eventRegistry, @Nonnull HytaleLogger logger) {
      this.logger = logger;
      this.reloadListeners = new ArrayList<>(1);
      this.densityAssets = new HashMap<>(1);
      this.assigmentAssets = new HashMap<>(1);
      this.biomeAssets = new HashMap<>(1);
      this.worldStructureAssets = new HashMap<>(1);
      this.blockMaskAssets = new HashMap<>(1);
      eventRegistry.register(LoadedAssetsEvent.class, DensityAsset.class, this::loadDensityAssets);
      eventRegistry.register(LoadedAssetsEvent.class, AssignmentsAsset.class, this::loadAssignmentsAssets);
      eventRegistry.register(LoadedAssetsEvent.class, BiomeAsset.class, this::loadBiomeAssets);
      eventRegistry.register(LoadedAssetsEvent.class, WorldStructureAsset.class, this::loadWorldStructureAssets);
      eventRegistry.register(LoadedAssetsEvent.class, SettingsAsset.class, this::loadSettingsAssets);
      eventRegistry.register(LoadedAssetsEvent.class, BlockMaskAsset.class, this::loadBlockMaskAssets);
   }

   private void loadBlockMaskAssets(@Nonnull LoadedAssetsEvent<String, BlockMaskAsset, DefaultAssetMap<String, BlockMaskAsset>> event) {
      this.blockMaskAssets.clear();

      for (BlockMaskAsset value : event.getLoadedAssets().values()) {
         this.blockMaskAssets.put(value.getId(), value);
         this.logger.at(Level.FINE).log("Loaded BlockMask asset " + value);
      }

      this.triggerReloadListeners();
   }

   private void loadDensityAssets(@Nonnull LoadedAssetsEvent<String, DensityAsset, DefaultAssetMap<String, DensityAsset>> event) {
      this.densityAssets.clear();

      for (DensityAsset value : event.getLoadedAssets().values()) {
         this.densityAssets.put(value.getId(), value);
         this.logger.at(Level.FINE).log("Loaded Density asset " + value);
      }

      this.triggerReloadListeners();
   }

   private void loadAssignmentsAssets(@Nonnull LoadedAssetsEvent<String, AssignmentsAsset, DefaultAssetMap<String, AssignmentsAsset>> event) {
      this.assigmentAssets.clear();

      for (AssignmentsAsset value : event.getLoadedAssets().values()) {
         this.assigmentAssets.put(value.getId(), value);
      }

      this.triggerReloadListeners();
   }

   private void loadBiomeAssets(@Nonnull LoadedAssetsEvent<String, BiomeAsset, DefaultAssetMap<String, BiomeAsset>> event) {
      this.biomeAssets.clear();

      for (BiomeAsset value : event.getLoadedAssets().values()) {
         this.biomeAssets.put(value.getId(), value);
      }

      this.triggerReloadListeners();
   }

   private void loadWorldStructureAssets(@Nonnull LoadedAssetsEvent<String, WorldStructureAsset, DefaultAssetMap<String, WorldStructureAsset>> event) {
      this.biomeAssets.clear();

      for (WorldStructureAsset value : event.getLoadedAssets().values()) {
         this.worldStructureAssets.put(value.getId(), value);
      }

      this.triggerReloadListeners();
   }

   private void loadSettingsAssets(@Nonnull LoadedAssetsEvent<String, SettingsAsset, DefaultAssetMap<String, SettingsAsset>> event) {
      SettingsAsset asset = event.getLoadedAssets().get("Settings");
      if (asset != null) {
         this.settingsAsset = asset;
         this.logger.at(Level.INFO).log("Loaded Settings asset.");
         this.triggerReloadListeners();
      }
   }

   public SettingsAsset getSettingsAsset() {
      return this.settingsAsset;
   }

   public WorldStructureAsset getWorldStructureAsset(@Nonnull String id) {
      return this.worldStructureAssets.get(id);
   }

   public void registerReloadListener(@Nonnull Runnable l) {
      this.reloadListeners.add(l);
   }

   public void unregisterReloadListener(@Nonnull Runnable l) {
      this.reloadListeners.remove(l);
   }

   private void triggerReloadListeners() {
      for (Runnable l : this.reloadListeners) {
         try {
            l.run();
         } catch (Exception var5) {
            String msg = "Exception thrown by HytaleGenerator while executing a reload listener:\n";
            msg = msg + ExceptionUtil.toStringWithStack(var5);
            LoggerUtil.getLogger().severe(msg);
         }
      }
   }

   static {
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(BiomeAsset.class, new DefaultAssetMap())
                     .setPath("HytaleGenerator/Biomes"))
                  .setKeyFunction(BiomeAsset::getId))
               .setCodec(BiomeAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        WorldStructureAsset.class, new DefaultAssetMap()
                     )
                     .setPath("HytaleGenerator/WorldStructures"))
                  .setKeyFunction(WorldStructureAsset::getId))
               .setCodec(WorldStructureAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(DensityAsset.class, new DefaultAssetMap())
                     .setPath("HytaleGenerator/Density"))
                  .setKeyFunction(DensityAsset::getId))
               .setCodec(DensityAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(BlockMaskAsset.class, new DefaultAssetMap())
                     .setPath("HytaleGenerator/MaterialMasks"))
                  .setKeyFunction(BlockMaskAsset::getId))
               .setCodec(BlockMaskAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(
                        AssignmentsAsset.class, new DefaultAssetMap()
                     )
                     .setPath("HytaleGenerator/Assignments"))
                  .setKeyFunction(AssignmentsAsset::getId))
               .setCodec(AssignmentsAsset.CODEC))
            .build()
      );
      AssetRegistry.register(
         ((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)((HytaleAssetStore.Builder)HytaleAssetStore.builder(SettingsAsset.class, new DefaultAssetMap())
                     .setPath("HytaleGenerator/Settings"))
                  .setKeyFunction(SettingsAsset::getId))
               .setCodec(SettingsAsset.CODEC))
            .build()
      );
      DensityAsset.CODEC.register("SimplexNoise2D", SimplexNoise2dDensityAsset.class, SimplexNoise2dDensityAsset.CODEC);
      DensityAsset.CODEC.register("SimplexNoise3D", SimplexNoise3DDensityAsset.class, SimplexNoise3DDensityAsset.CODEC);
      DensityAsset.CODEC.register("Offset", OffsetDensityAsset.class, OffsetDensityAsset.CODEC);
      DensityAsset.CODEC.register("Sum", SumDensityAsset.class, SumDensityAsset.CODEC);
      DensityAsset.CODEC.register("Sqrt", SqrtDensityAsset.class, SqrtDensityAsset.CODEC);
      DensityAsset.CODEC.register("Pow", PowDensityAsset.class, PowDensityAsset.CODEC);
      DensityAsset.CODEC.register("Multiplier", MultiplierDensityAsset.class, MultiplierDensityAsset.CODEC);
      DensityAsset.CODEC.register("Amplitude", AmplitudeDensityAsset.class, AmplitudeDensityAsset.CODEC);
      DensityAsset.CODEC.register("Clamp", ClampDensityAsset.class, ClampDensityAsset.CODEC);
      DensityAsset.CODEC.register("SmoothClamp", SmoothClampDensityAsset.class, SmoothClampDensityAsset.CODEC);
      DensityAsset.CODEC.register("Max", MaxDensityAsset.class, MaxDensityAsset.CODEC);
      DensityAsset.CODEC.register("Min", MinDensityAsset.class, MinDensityAsset.CODEC);
      DensityAsset.CODEC.register("Floor", FloorDensityAsset.class, FloorDensityAsset.CODEC);
      DensityAsset.CODEC.register("Ceiling", CeilingDensityAsset.class, CeilingDensityAsset.CODEC);
      DensityAsset.CODEC.register("SmoothMax", SmoothMaxDensityAsset.class, SmoothMaxDensityAsset.CODEC);
      DensityAsset.CODEC.register("SmoothMin", SmoothMinDensityAsset.class, SmoothMinDensityAsset.CODEC);
      DensityAsset.CODEC.register("SmoothFloor", SmoothFloorDensityAsset.class, SmoothFloorDensityAsset.CODEC);
      DensityAsset.CODEC.register("SmoothCeiling", SmoothCeilingDensityAsset.class, SmoothCeilingDensityAsset.CODEC);
      DensityAsset.CODEC.register("Constant", ConstantDensityAsset.class, ConstantDensityAsset.CODEC);
      DensityAsset.CODEC.register("Abs", AbsDensityAsset.class, AbsDensityAsset.CODEC);
      DensityAsset.CODEC.register("Inverter", InverterDensityAsset.class, InverterDensityAsset.CODEC);
      DensityAsset.CODEC.register("AmplitudeConstant", AmplitudeConstantAsset.class, AmplitudeConstantAsset.CODEC);
      DensityAsset.CODEC.register("OffsetConstant", OffsetConstantAsset.class, OffsetConstantAsset.CODEC);
      DensityAsset.CODEC.register("Pipeline", PipelineDensityAsset.class, PipelineDensityAsset.CODEC);
      DensityAsset.CODEC.register("Normalizer", NormalizerDensityAsset.class, NormalizerDensityAsset.CODEC);
      DensityAsset.CODEC.register("Imported", ImportedDensityAsset.class, ImportedDensityAsset.CODEC);
      DensityAsset.CODEC.register("PositionsCellNoise", PositionsCellNoiseDensityAsset.class, PositionsCellNoiseDensityAsset.CODEC);
      DensityAsset.CODEC.register("Positions3D", Positions3DDensityAsset.class, Positions3DDensityAsset.CODEC);
      DensityAsset.CODEC.register("CellNoise2D", CellNoise2DDensityAsset.class, CellNoise2DDensityAsset.CODEC);
      DensityAsset.CODEC.register("CellNoise3D", CellNoise3DDensityAsset.class, CellNoise3DDensityAsset.CODEC);
      DensityAsset.CODEC.register("Gradient", GradientDensityAsset.class, GradientDensityAsset.CODEC);
      DensityAsset.CODEC.register("Scale", ScaleDensityAsset.class, ScaleDensityAsset.CODEC);
      DensityAsset.CODEC.register("Slider", SliderDensityAsset.class, SliderDensityAsset.CODEC);
      DensityAsset.CODEC.register("GradientWarp", GradientWarpDensityAsset.class, GradientWarpDensityAsset.CODEC);
      DensityAsset.CODEC.register("VectorWarp", VectorWarpDensityAsset.class, VectorWarpDensityAsset.CODEC);
      DensityAsset.CODEC.register("Cache2D", Cache2dDensityAsset_Deprecated.class, Cache2dDensityAsset_Deprecated.CODEC);
      DensityAsset.CODEC.register("Rotator", RotatorDensityAsset.class, RotatorDensityAsset.CODEC);
      DensityAsset.CODEC.register("PositionsPinch", PositionsPinchDensityAsset.class, PositionsPinchDensityAsset.CODEC);
      DensityAsset.CODEC.register("PositionsTwist", PositionsTwistDensityAsset.class, PositionsTwistDensityAsset.CODEC);
      DensityAsset.CODEC.register("BaseHeight", BaseHeightDensityAsset.class, BaseHeightDensityAsset.CODEC);
      DensityAsset.CODEC.register("CurveMapper", CurveMapperDensityAsset.class, CurveMapperDensityAsset.CODEC);
      DensityAsset.CODEC.register("Anchor", AnchorDensityAsset.class, AnchorDensityAsset.CODEC);
      DensityAsset.CODEC.register("Distance", DistanceDensityAsset.class, DistanceDensityAsset.CODEC);
      DensityAsset.CODEC.register("Shell", ShellDensityAsset.class, ShellDensityAsset.CODEC);
      DensityAsset.CODEC.register("Axis", AxisDensityAsset.class, AxisDensityAsset.CODEC);
      DensityAsset.CODEC.register("Plane", PlaneDensityAsset.class, PlaneDensityAsset.CODEC);
      DensityAsset.CODEC.register("Switch", SwitchDensityAsset.class, SwitchDensityAsset.CODEC);
      DensityAsset.CODEC.register("SwitchState", SwitchStateDensityAsset.class, SwitchStateDensityAsset.CODEC);
      DensityAsset.CODEC.register("Ellipsoid", EllipsoidDensityAsset.class, EllipsoidDensityAsset.CODEC);
      DensityAsset.CODEC.register("Cube", CubeDensityAsset.class, CubeDensityAsset.CODEC);
      DensityAsset.CODEC.register("Cuboid", CuboidDensityAsset.class, CuboidDensityAsset.CODEC);
      DensityAsset.CODEC.register("Cylinder", CylinderDensityAsset.class, CylinderDensityAsset.CODEC);
      DensityAsset.CODEC.register("CellWallDistance", CellWallDistanceDensityAsset.class, CellWallDistanceDensityAsset.CODEC);
      DensityAsset.CODEC.register("FastGradientWarp", FastGradientWarpDensityAsset.class, FastGradientWarpDensityAsset.CODEC);
      DensityAsset.CODEC.register("Mix", MixDensityAsset.class, MixDensityAsset.CODEC);
      DensityAsset.CODEC.register("MultiMix", MultiMixDensityAsset.class, MultiMixDensityAsset.CODEC);
      DensityAsset.CODEC.register("XValue", XValueDensityAsset.class, XValueDensityAsset.CODEC);
      DensityAsset.CODEC.register("YValue", YValueDensityAsset.class, YValueDensityAsset.CODEC);
      DensityAsset.CODEC.register("ZValue", ZValueDensityAsset.class, ZValueDensityAsset.CODEC);
      DensityAsset.CODEC.register("XOverride", XOverrideDensityAsset.class, XOverrideDensityAsset.CODEC);
      DensityAsset.CODEC.register("YOverride", YOverrideDensityAsset.class, YOverrideDensityAsset.CODEC);
      DensityAsset.CODEC.register("ZOverride", ZOverrideDensityAsset.class, ZOverrideDensityAsset.CODEC);
      DensityAsset.CODEC.register("Cache", CacheDensityAsset.class, CacheDensityAsset.CODEC);
      DensityAsset.CODEC.register("Angle", AngleDensityAsset.class, AngleDensityAsset.CODEC);
      DensityAsset.CODEC.register("Exported", ExportedDensityAsset.class, ExportedDensityAsset.CODEC);
      DensityAsset.CODEC.register("Terrain", TerrainDensityAsset.class, TerrainDensityAsset.CODEC);
      DensityAsset.CODEC.register("DistanceToBiomeEdge", DistanceToBiomeEdgeDensityAsset.class, DistanceToBiomeEdgeDensityAsset.CODEC);
      DensityAsset.CODEC.register("YSampled", YSampledDensityAsset.class, YSampledDensityAsset.CODEC);
      FrameworkAsset.CODEC.register("DecimalConstants", DecimalConstantsFrameworkAsset.class, DecimalConstantsFrameworkAsset.CODEC);
      FrameworkAsset.CODEC.register("Positions", PositionsFrameworkAsset.class, PositionsFrameworkAsset.CODEC);
      TerrainAsset.CODEC.register("DAOTerrain", DensityTerrainAsset.class, DensityTerrainAsset.CODEC);
      NoiseAsset.CODEC.register("Simplex", SimplexNoiseAsset.class, SimplexNoiseAsset.CODEC);
      NoiseAsset.CODEC.register("Cell", CellNoiseAsset.class, CellNoiseAsset.CODEC);
      WorldStructureAsset.CODEC.register("NoiseRange", BasicWorldStructureAsset.class, BasicWorldStructureAsset.CODEC);
      MaterialProviderAsset.CODEC.register("Constant", ConstantMaterialProviderAsset.class, ConstantMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("Solidity", SolidityMaterialProviderAsset.class, SolidityMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("DownwardDepth", DownwardDepthMaterialProviderAsset.class, DownwardDepthMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("DownwardSpace", DownwardSpaceMaterialProviderAsset.class, DownwardSpaceMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("UpwardDepth", UpwardDepthMaterialProviderAsset.class, UpwardDepthMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("UpwardSpace", UpwardSpaceMaterialProviderAsset.class, UpwardSpaceMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("Queue", QueueMaterialProviderAsset.class, QueueMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("SimpleHorizontal", SimpleHorizontalMaterialProviderAsset.class, SimpleHorizontalMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("Striped", StripedMaterialProviderAsset.class, StripedMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("FieldFunction", FieldFunctionMaterialProviderAsset.class, FieldFunctionMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("TerrainDensity", TerrainDensityMaterialProviderAsset.class, TerrainDensityMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("Weighted", WeightedMaterialProviderAsset.class, WeightedMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("SpaceAndDepth", SpaceAndDepthMaterialProviderAsset.class, SpaceAndDepthMaterialProviderAsset.CODEC);
      MaterialProviderAsset.CODEC.register("Imported", ImportedMaterialProviderAsset.class, ImportedMaterialProviderAsset.CODEC);
      LayerAsset.CODEC.register("ConstantThickness", ConstantThicknessLayerAsset.class, ConstantThicknessLayerAsset.CODEC);
      LayerAsset.CODEC.register("NoiseThickness", NoiseThicknessAsset.class, NoiseThicknessAsset.CODEC);
      LayerAsset.CODEC.register("RangeThickness", RangeThicknessAsset.class, RangeThicknessAsset.CODEC);
      LayerAsset.CODEC.register("WeightedThickness", WeightedThicknessLayerAsset.class, WeightedThicknessLayerAsset.CODEC);
      ConditionAsset.CODEC.register("AndCondition", AndConditionAsset.class, AndConditionAsset.CODEC);
      ConditionAsset.CODEC.register("EqualsCondition", EqualsConditionAsset.class, EqualsConditionAsset.CODEC);
      ConditionAsset.CODEC.register("GreaterThanCondition", GreaterThanConditionAsset.class, GreaterThanConditionAsset.CODEC);
      ConditionAsset.CODEC.register("NotCondition", NotConditionAsset.class, NotConditionAsset.CODEC);
      ConditionAsset.CODEC.register("OrCondition", OrConditionAsset.class, OrConditionAsset.CODEC);
      ConditionAsset.CODEC.register("SmallerThanCondition", SmallerThanConditionAsset.class, SmallerThanConditionAsset.CODEC);
      ConditionAsset.CODEC.register("AlwaysTrueCondition", AlwaysTrueConditionAsset.class, AlwaysTrueConditionAsset.CODEC);
      PositionProviderAsset.CODEC.register("List", ListPositionProviderAsset.class, ListPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Mesh2D", Mesh2DPositionProviderAsset.class, Mesh2DPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Mesh3D", Mesh3DPositionProviderAsset.class, Mesh3DPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("FieldFunction", FieldFunctionPositionProviderAsset.class, FieldFunctionPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC
         .register("Occurrence", FieldFunctionOccurrencePositionProviderAsset.class, FieldFunctionOccurrencePositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Offset", OffsetPositionProviderAsset.class, OffsetPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Union", UnionPositionProviderAsset.class, UnionPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("SimpleHorizontal", SimpleHorizontalPositionProviderAsset.class, SimpleHorizontalPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Cache", CachedPositionProviderAsset.class, CachedPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("BaseHeight", BaseHeightPositionProviderAsset.class, BaseHeightPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Imported", ImportedPositionProviderAsset.class, ImportedPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Anchor", AnchorPositionProviderAsset.class, AnchorPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Bound", BoundPositionProviderAsset.class, BoundPositionProviderAsset.CODEC);
      PositionProviderAsset.CODEC.register("Framework", FrameworkPositionProviderAsset.class, FrameworkPositionProviderAsset.CODEC);
      PointGeneratorAsset.CODEC.register("Mesh", MeshPointGeneratorAsset.class, MeshPointGeneratorAsset.CODEC);
      AssignmentsAsset.CODEC.register("FieldFunction", FieldFunctionAssignmentsAsset.class, FieldFunctionAssignmentsAsset.CODEC);
      AssignmentsAsset.CODEC.register("Sandwich", SandwichAssignmentsAsset.class, SandwichAssignmentsAsset.CODEC);
      AssignmentsAsset.CODEC.register("Weighted", WeightedAssignmentsAsset.class, WeightedAssignmentsAsset.CODEC);
      AssignmentsAsset.CODEC.register("Constant", ConstantAssignmentsAsset.class, ConstantAssignmentsAsset.CODEC);
      AssignmentsAsset.CODEC.register("Imported", ImportedAssignmentsAsset.class, ImportedAssignmentsAsset.CODEC);
      PropAsset.CODEC.register("Box", BoxPropAsset.class, BoxPropAsset.CODEC);
      PropAsset.CODEC.register("Imported", ImportedPropAsset.class, ImportedPropAsset.CODEC);
      PropAsset.CODEC.register("Union", UnionPropAsset.class, UnionPropAsset.CODEC);
      PropAsset.CODEC.register("Column", ColumnPropAsset.class, ColumnPropAsset.CODEC);
      PropAsset.CODEC.register("Cluster", ClusterPropAsset.class, ClusterPropAsset.CODEC);
      PropAsset.CODEC.register("Queue", QueuePropAsset.class, QueuePropAsset.CODEC);
      PropAsset.CODEC.register("Prefab", PrefabPropAsset.class, PrefabPropAsset.CODEC);
      PropAsset.CODEC.register("PondFiller", PondFillerPropAsset.class, PondFillerPropAsset.CODEC);
      PropAsset.CODEC.register("Density", DensityPropAsset.class, DensityPropAsset.CODEC);
      PropAsset.CODEC.register("Offset", OffsetPropAsset.class, OffsetPropAsset.CODEC);
      PropAsset.CODEC.register("Weighted", WeightedPropAsset.class, WeightedPropAsset.CODEC);
      DirectionalityAsset.CODEC.register("Imported", ImportedDirectionalityAsset.class, ImportedDirectionalityAsset.CODEC);
      DirectionalityAsset.CODEC.register("Static", StaticDirectionalityAsset.class, StaticDirectionalityAsset.CODEC);
      DirectionalityAsset.CODEC.register("Random", RandomDirectionalityAsset.class, RandomDirectionalityAsset.CODEC);
      DirectionalityAsset.CODEC.register("Pattern", PatternDirectionalityAsset.class, PatternDirectionalityAsset.CODEC);
      PatternAsset.CODEC.register("BlockType", MaterialPatternAsset.class, MaterialPatternAsset.CODEC);
      PatternAsset.CODEC.register("BlockSet", BlockSetPatternAsset.class, BlockSetPatternAsset.CODEC);
      PatternAsset.CODEC.register("Offset", OffsetPatternAsset.class, OffsetPatternAsset.CODEC);
      PatternAsset.CODEC.register("Floor", FloorPatternAsset.class, FloorPatternAsset.CODEC);
      PatternAsset.CODEC.register("Ceiling", CeilingPatternAsset.class, CeilingPatternAsset.CODEC);
      PatternAsset.CODEC.register("Wall", WallPatternAsset.class, WallPatternAsset.CODEC);
      PatternAsset.CODEC.register("Cuboid", CuboidPatternAsset.class, CuboidPatternAsset.CODEC);
      PatternAsset.CODEC.register("And", AndPatternAsset.class, AndPatternAsset.CODEC);
      PatternAsset.CODEC.register("Or", OrPatternAsset.class, OrPatternAsset.CODEC);
      PatternAsset.CODEC.register("Not", NotPatternAsset.class, NotPatternAsset.CODEC);
      PatternAsset.CODEC.register("Surface", SurfacePatternAsset.class, SurfacePatternAsset.CODEC);
      PatternAsset.CODEC.register("Gap", GapPatternAsset.class, GapPatternAsset.CODEC);
      PatternAsset.CODEC.register("FieldFunction", DensityPatternAsset.class, DensityPatternAsset.CODEC);
      PatternAsset.CODEC.register("Imported", ImportedPatternAsset.class, ImportedPatternAsset.CODEC);
      PatternAsset.CODEC.register("Constant", ConstantPatternAsset.class, ConstantPatternAsset.CODEC);
      ScannerAsset.CODEC.register("ColumnLinear", ColumnLinearScannerAsset.class, ColumnLinearScannerAsset.CODEC);
      ScannerAsset.CODEC.register("ColumnRandom", ColumnRandomScannerAsset.class, ColumnRandomScannerAsset.CODEC);
      ScannerAsset.CODEC.register("Origin", OriginScannerAsset.class, OriginScannerAsset.CODEC);
      ScannerAsset.CODEC.register("Area", AreaScannerAsset.class, AreaScannerAsset.CODEC);
      ScannerAsset.CODEC.register("Imported", ImportedScannerAsset.class, ImportedScannerAsset.CODEC);
      CurveAsset.CODEC.register("Imported", ImportedCurveAsset.class, ImportedCurveAsset.CODEC);
      CurveAsset.CODEC.register("Manual", ManualCurveAsset.class, ManualCurveAsset.CODEC);
      CurveAsset.CODEC.register("DistanceExponential", DistanceExponentialCurveAsset.class, DistanceExponentialCurveAsset.CODEC);
      CurveAsset.CODEC.register("DistanceS", DistanceSCurveAsset.class, DistanceSCurveAsset.CODEC);
      CurveAsset.CODEC.register("Not", NotCurveAsset.class, NotCurveAsset.CODEC);
      CurveAsset.CODEC.register("Multiplier", MultiplierCurveAsset.class, MultiplierCurveAsset.CODEC);
      CurveAsset.CODEC.register("Sum", SumCurveAsset.class, SumCurveAsset.CODEC);
      CurveAsset.CODEC.register("Inverter", InverterCurveAsset.class, InverterCurveAsset.CODEC);
      CurveAsset.CODEC.register("Clamp", ClampCurveAsset.class, ClampCurveAsset.CODEC);
      CurveAsset.CODEC.register("SmoothClamp", SmoothClampCurveAsset.class, SmoothClampCurveAsset.CODEC);
      CurveAsset.CODEC.register("Min", MinCurveAsset.class, MinCurveAsset.CODEC);
      CurveAsset.CODEC.register("Max", MinCurveAsset.class, MinCurveAsset.CODEC);
      CurveAsset.CODEC.register("SmoothMin", SmoothMinCurveAsset.class, SmoothMinCurveAsset.CODEC);
      CurveAsset.CODEC.register("SmoothMax", SmoothMaxCurveAsset.class, SmoothMaxCurveAsset.CODEC);
      CurveAsset.CODEC.register("SmoothFloor", SmoothFloorCurveAsset.class, SmoothFloorCurveAsset.CODEC);
      CurveAsset.CODEC.register("SmoothCeiling", SmoothCeilingCurveAsset.class, SmoothCeilingCurveAsset.CODEC);
      CurveAsset.CODEC.register("Floor", FloorCurveAsset.class, FloorCurveAsset.CODEC);
      CurveAsset.CODEC.register("Ceiling", CeilingCurveAsset.class, CeilingCurveAsset.CODEC);
      CurveAsset.CODEC.register("Constant", ConstantCurveAsset.class, ConstantCurveAsset.CODEC);
      ReturnTypeAsset.CODEC.register("CellValue", CellValueReturnTypeAsset.class, CellValueReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Curve", CurveReturnTypeAsset.class, CurveReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Distance", DistanceReturnTypeAsset.class, DistanceReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Distance2", Distance2ReturnTypeAsset.class, Distance2ReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Distance2Add", Distance2AddReturnTypeAsset.class, Distance2AddReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Distance2Sub", Distance2SubReturnTypeAsset.class, Distance2SubReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Distance2Mul", Distance2MulReturnTypeAsset.class, Distance2MulReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Distance2Div", Distance2DivReturnTypeAsset.class, Distance2DivReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Imported", ImportedReturnTypeAsset.class, ImportedReturnTypeAsset.CODEC);
      ReturnTypeAsset.CODEC.register("Density", DensityReturnTypeAsset.class, DensityReturnTypeAsset.CODEC);
      DistanceFunctionAsset.CODEC.register("Euclidean", EuclideanDistanceFunctionAsset.class, EuclideanDistanceFunctionAsset.CODEC);
      DistanceFunctionAsset.CODEC.register("Manhattan", ManhattanDistanceFunctionAsset.class, ManhattanDistanceFunctionAsset.CODEC);
      EnvironmentProviderAsset.CODEC.register("Constant", ConstantEnvironmentProviderAsset.class, ConstantEnvironmentProviderAsset.CODEC);
      EnvironmentProviderAsset.CODEC
         .register("DensityDelimited", DensityDelimitedEnvironmentProviderAsset.class, DensityDelimitedEnvironmentProviderAsset.CODEC);
      TintProviderAsset.CODEC.register("Constant", ConstantTintProviderAsset.class, ConstantTintProviderAsset.CODEC);
      TintProviderAsset.CODEC.register("DensityDelimited", DensityDelimitedTintProviderAsset.class, DensityDelimitedTintProviderAsset.CODEC);
      VectorProviderAsset.CODEC.register("Constant", ConstantVectorProviderAsset.class, ConstantVectorProviderAsset.CODEC);
      VectorProviderAsset.CODEC.register("DensityGradient", DensityGradientVectorProviderAsset.class, DensityGradientVectorProviderAsset.CODEC);
      VectorProviderAsset.CODEC.register("Cache", CacheVectorProviderAsset.class, CacheVectorProviderAsset.CODEC);
      VectorProviderAsset.CODEC.register("Exported", ExportedVectorProviderAsset.class, ExportedVectorProviderAsset.CODEC);
      VectorProviderAsset.CODEC.register("Imported", ImportedVectorProviderAsset.class, ImportedVectorProviderAsset.CODEC);
   }
}
