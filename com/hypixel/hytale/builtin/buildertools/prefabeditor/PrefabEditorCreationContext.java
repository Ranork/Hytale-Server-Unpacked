package com.hypixel.hytale.builtin.buildertools.prefabeditor;

import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabAlignment;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRootDirectory;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabRowSplitMode;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.PrefabStackingAxis;
import com.hypixel.hytale.builtin.buildertools.prefabeditor.enums.WorldGenType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import java.nio.file.Path;
import java.util.List;

public interface PrefabEditorCreationContext {
   Player getEditor();

   PlayerRef getEditorRef();

   List<Path> getPrefabPaths();

   int getBlocksBetweenEachPrefab();

   int getPasteLevelGoal();

   boolean loadChildPrefabs();

   boolean shouldLoadEntities();

   PrefabStackingAxis getStackingAxis();

   WorldGenType getWorldGenType();

   int getBlocksAboveSurface();

   PrefabAlignment getAlignment();

   PrefabRootDirectory getPrefabRootDirectory();

   boolean isWorldTickingEnabled();

   PrefabRowSplitMode getRowSplitMode();

   List<String> getUnprocessedPrefabPaths();

   String getEnvironment();

   String getGrassTint();
}
