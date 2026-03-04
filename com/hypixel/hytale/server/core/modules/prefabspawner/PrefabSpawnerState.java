package com.hypixel.hytale.server.core.modules.prefabspawner;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.prefab.PrefabWeights;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabSpawnerState extends BlockState {
   public static final String PREFAB_SPAWNER_TYPE = "prefabspawner";
   public static final KeyedCodec<Boolean> FIT_HEIGHTMAP_CODEC = new KeyedCodec<>("FitHeightmap", Codec.BOOLEAN);
   public static final KeyedCodec<Boolean> INHERIT_SEED_CODEC = new KeyedCodec<>("InheritSeed", Codec.BOOLEAN);
   public static final KeyedCodec<Boolean> INHERIT_HEIGHT_CONDITION_CODEC = new KeyedCodec<>("InheritHeightCondition", Codec.BOOLEAN);
   public static final KeyedCodec<PrefabWeights> PREFAB_WEIGHTS_CODEC = new KeyedCodec<>("PrefabWeights", PrefabWeights.CODEC);
   @Nonnull
   public static final Codec<PrefabSpawnerState> CODEC = BuilderCodec.builder(PrefabSpawnerState.class, PrefabSpawnerState::new, BlockState.BASE_CODEC)
      .append(new KeyedCodec<>("PrefabPath", Codec.STRING), (state, s) -> state.prefabPath = s, state -> state.prefabPath)
      .documentation("The prefab path where the prefab is located. This uses the dot-notation. 'folder.folder.folder.filename'")
      .add()
      .<Boolean>append(FIT_HEIGHTMAP_CODEC, (state, s) -> state.fitHeightmap = s, state -> state.fitHeightmap)
      .documentation("Determines if the child prefab should follow the heightmap during generation in worldgen.")
      .add()
      .<Boolean>append(INHERIT_SEED_CODEC, (state, s) -> state.inheritSeed = s, state -> state.inheritSeed)
      .documentation("Determines if the child prefab should inherit the worldgen-id from the parent. This allows child prefabs to have independent markers.")
      .add()
      .<Boolean>append(INHERIT_HEIGHT_CONDITION_CODEC, (state, s) -> state.inheritHeightCondition = s, state -> state.inheritHeightCondition)
      .documentation(
         "Determines if the child prefab should inherit the HeightCondition from the parent. Setting to false allows child prefabs to bypass the height condition check."
      )
      .add()
      .append(PREFAB_WEIGHTS_CODEC, PrefabSpawnerState::setPrefabWeights, PrefabSpawnerState::getPrefabWeightsNullable)
      .documentation(
         "Determines the probability of each individual prefab file being selected to generate when the PrefabPath points to a folder containing multiple prefabs."
      )
      .add()
      .build();
   private String prefabPath;
   private boolean fitHeightmap = false;
   private boolean inheritSeed = true;
   private boolean inheritHeightCondition = true;
   private PrefabWeights prefabWeights = PrefabWeights.NONE;

   public String getPrefabPath() {
      return this.prefabPath;
   }

   public void setPrefabPath(String prefabPath) {
      this.prefabPath = prefabPath;
   }

   public boolean isFitHeightmap() {
      return this.fitHeightmap;
   }

   public void setFitHeightmap(boolean fitHeightmap) {
      this.fitHeightmap = fitHeightmap;
   }

   public boolean isInheritSeed() {
      return this.inheritSeed;
   }

   public void setInheritSeed(boolean inheritSeed) {
      this.inheritSeed = inheritSeed;
   }

   public boolean isInheritHeightCondition() {
      return this.inheritHeightCondition;
   }

   public void setInheritHeightCondition(boolean inheritHeightCondition) {
      this.inheritHeightCondition = inheritHeightCondition;
   }

   public PrefabWeights getPrefabWeights() {
      return this.prefabWeights;
   }

   public void setPrefabWeights(PrefabWeights prefabWeights) {
      this.prefabWeights = prefabWeights;
   }

   @Nullable
   private PrefabWeights getPrefabWeightsNullable() {
      return this.prefabWeights.size() == 0 ? null : this.prefabWeights;
   }

   public static class PrefabSpawnerSettingsPage extends InteractiveCustomUIPage<PrefabSpawnerState.PrefabSpawnerSettingsPageEventData> {
      private final PrefabSpawnerState state;

      public PrefabSpawnerSettingsPage(@Nonnull PlayerRef playerRef, PrefabSpawnerState state, @Nonnull CustomPageLifetime lifetime) {
         super(playerRef, lifetime, PrefabSpawnerState.PrefabSpawnerSettingsPageEventData.CODEC);
         this.state = state;
      }

      @Override
      public void build(
         @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
      ) {
         commandBuilder.append("Pages/PrefabSpawnerSettingsPage.ui");
         commandBuilder.set("#PrefabPath.Value", Objects.requireNonNullElse(this.state.prefabPath, ""));
         commandBuilder.set("#FitHeightmap #CheckBox.Value", this.state.fitHeightmap);
         commandBuilder.set("#InheritSeed #CheckBox.Value", this.state.inheritSeed);
         commandBuilder.set("#InheritHeightCondition #CheckBox.Value", this.state.inheritHeightCondition);
         commandBuilder.set("#DefaultWeight.Value", this.state.getPrefabWeights().getDefaultWeight());
         commandBuilder.set("#PrefabWeights.Value", this.state.getPrefabWeights().getMappingString());
         eventBuilder.addEventBinding(
            CustomUIEventBindingType.Activating,
            "#SaveButton",
            new EventData()
               .append("@PrefabPath", "#PrefabPath.Value")
               .append("@FitHeightmap", "#FitHeightmap #CheckBox.Value")
               .append("@InheritSeed", "#InheritSeed #CheckBox.Value")
               .append("@InheritHeightCondition", "#InheritHeightCondition #CheckBox.Value")
               .append("@DefaultWeight", "#DefaultWeight.Value")
               .append("@PrefabWeights", "#PrefabWeights.Value")
         );
      }

      public void handleDataEvent(
         @Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PrefabSpawnerState.PrefabSpawnerSettingsPageEventData data
      ) {
         this.state.prefabPath = data.prefabPath;
         this.state.fitHeightmap = data.fitHeightmap;
         this.state.inheritSeed = data.inheritSeed;
         this.state.inheritHeightCondition = data.inheritHeightCondition;
         this.state.prefabWeights = PrefabWeights.parse(data.prefabWeights);
         this.state.prefabWeights.setDefaultWeight(data.defaultWeight);
         this.state.markNeedsSave();
         Player playerComponent = store.getComponent(ref, Player.getComponentType());
         playerComponent.getPageManager().setPage(ref, store, Page.None);
      }
   }

   public static class PrefabSpawnerSettingsPageEventData {
      public static final String KEY_PREFAB_PATH = "@PrefabPath";
      public static final String KEY_FIT_HEIGHTMAP = "@FitHeightmap";
      public static final String KEY_INHERIT_SEED = "@InheritSeed";
      public static final String KEY_INHERIT_HEIGHT_CONDITION = "@InheritHeightCondition";
      public static final String KEY_DEFAULT_WEIGHT = "@DefaultWeight";
      public static final String KEY_PREFAB_WEIGHTS = "@PrefabWeights";
      public static final BuilderCodec<PrefabSpawnerState.PrefabSpawnerSettingsPageEventData> CODEC = BuilderCodec.builder(
            PrefabSpawnerState.PrefabSpawnerSettingsPageEventData.class, PrefabSpawnerState.PrefabSpawnerSettingsPageEventData::new
         )
         .append(new KeyedCodec<>("@PrefabPath", Codec.STRING), (entry, s) -> entry.prefabPath = s, entry -> entry.prefabPath)
         .add()
         .append(new KeyedCodec<>("@FitHeightmap", Codec.BOOLEAN), (entry, s) -> entry.fitHeightmap = s, entry -> entry.fitHeightmap)
         .add()
         .append(new KeyedCodec<>("@InheritSeed", Codec.BOOLEAN), (entry, s) -> entry.inheritSeed = s, entry -> entry.inheritSeed)
         .add()
         .append(
            new KeyedCodec<>("@InheritHeightCondition", Codec.BOOLEAN), (entry, s) -> entry.inheritHeightCondition = s, entry -> entry.inheritHeightCondition
         )
         .add()
         .<Double>append(new KeyedCodec<>("@DefaultWeight", Codec.DOUBLE), (entry, s) -> entry.defaultWeight = s, entry -> entry.defaultWeight)
         .addValidator(Validators.greaterThanOrEqual(0.0))
         .add()
         .append(new KeyedCodec<>("@PrefabWeights", Codec.STRING), (entry, s) -> entry.prefabWeights = s, entry -> entry.prefabWeights)
         .add()
         .build();
      private String prefabPath;
      private boolean fitHeightmap;
      private boolean inheritSeed;
      private boolean inheritHeightCondition;
      private double defaultWeight;
      private String prefabWeights;
   }
}
