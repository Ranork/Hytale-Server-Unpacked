package com.hypixel.hytale.server.core.asset.type.item.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.protocol.BenchRequirement;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.inventory.MaterialQuantity;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class CraftingRecipe implements JsonAssetWithMap<String, DefaultAssetMap<String, CraftingRecipe>> {
   public static final String FIELDCRAFT_REQUIREMENT = "Fieldcraft";
   public static final AssetBuilderCodec<String, CraftingRecipe> CODEC = AssetBuilderCodec.builder(
         CraftingRecipe.class,
         CraftingRecipe::new,
         Codec.STRING,
         (recipe, blockTypeKey) -> recipe.id = blockTypeKey,
         recipe -> recipe.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(
         new KeyedCodec<>("Input", new ArrayCodec<>(MaterialQuantity.CODEC, MaterialQuantity[]::new)),
         (craftingRecipe, objects) -> craftingRecipe.input = objects,
         craftingRecipe -> craftingRecipe.input
      )
      .addValidator(Validators.nonNull())
      .add()
      .append(
         new KeyedCodec<>("Output", new ArrayCodec<>(MaterialQuantity.CODEC, MaterialQuantity[]::new)),
         (craftingRecipe, objects) -> craftingRecipe.outputs = objects,
         craftingRecipe -> craftingRecipe.outputs
      )
      .add()
      .append(
         new KeyedCodec<>("PrimaryOutput", MaterialQuantity.CODEC),
         (craftingRecipe, objects) -> craftingRecipe.primaryOutput = objects,
         craftingRecipe -> craftingRecipe.primaryOutput
      )
      .add()
      .append(
         new KeyedCodec<>("OutputQuantity", Codec.INTEGER),
         (craftingRecipe, quantity) -> craftingRecipe.primaryOutputQuantity = quantity,
         craftingRecipe -> craftingRecipe.primaryOutputQuantity
      )
      .add()
      .append(
         new KeyedCodec<>(
            "BenchRequirement",
            new ArrayCodec<>(
               BuilderCodec.builder(BenchRequirement.class, BenchRequirement::new)
                  .append(
                     new KeyedCodec<>("Type", new EnumCodec<>(BenchType.class)),
                     (benchRequirement, benchType) -> benchRequirement.type = benchType,
                     benchRequirement -> benchRequirement.type
                  )
                  .addValidator(Validators.nonNull())
                  .add()
                  .<String>append(
                     new KeyedCodec<>("Id", Codec.STRING), (benchRequirement, s) -> benchRequirement.id = s, benchRequirement -> benchRequirement.id
                  )
                  .addValidator(Validators.nonNull())
                  .add()
                  .append(
                     new KeyedCodec<>("Categories", Codec.STRING_ARRAY),
                     (benchRequirement, s) -> benchRequirement.categories = s,
                     benchRequirement -> benchRequirement.categories
                  )
                  .add()
                  .appendInherited(
                     new KeyedCodec<>("RequiredTierLevel", Codec.INTEGER),
                     (benchRequirement, s) -> benchRequirement.requiredTierLevel = s,
                     benchRequirement -> benchRequirement.requiredTierLevel,
                     (benchRequirement, parent) -> benchRequirement.requiredTierLevel = parent.requiredTierLevel
                  )
                  .add()
                  .build(),
               BenchRequirement[]::new
            )
         ),
         (craftingRecipe, objects) -> craftingRecipe.benchRequirement = objects,
         craftingRecipe -> craftingRecipe.benchRequirement
      )
      .add()
      .<Double>append(
         new KeyedCodec<>("TimeSeconds", Codec.DOUBLE),
         (craftingRecipe, d) -> craftingRecipe.timeSeconds = d.floatValue(),
         craftingRecipe -> (double)craftingRecipe.timeSeconds
      )
      .addValidator(Validators.min(0.0))
      .add()
      .append(
         new KeyedCodec<>("KnowledgeRequired", Codec.BOOLEAN),
         (craftingRecipe, b) -> craftingRecipe.knowledgeRequired = b,
         craftingRecipe -> craftingRecipe.knowledgeRequired
      )
      .add()
      .<Integer>append(
         new KeyedCodec<>("RequiredMemoriesLevel", Codec.INTEGER),
         (craftingRecipe, integer) -> craftingRecipe.requiredMemoriesLevel = integer,
         craftingRecipe -> craftingRecipe.requiredMemoriesLevel
      )
      .documentation("The level of Memories starts from 1, meaning a recipe with a RequiredMemoriesLevel set at 1 will always be available to players.")
      .addValidator(Validators.greaterThanOrEqual(1))
      .add()
      .validator((craftingRecipe, results) -> {
         BenchRequirement[] benchRequirements = craftingRecipe.getBenchRequirement();
         if (benchRequirements != null) {
            for (BenchRequirement benchRequirement : benchRequirements) {
               if (craftingRecipe.isKnowledgeRequired() && benchRequirement.type != BenchType.Crafting && benchRequirement.type != BenchType.DiagramCrafting) {
                  results.fail("KnowledgeRequired in recipe can't be set for non crafting recipes");
               }

               if (benchRequirement.type == BenchType.DiagramCrafting && craftingRecipe.getOutputs() != null && craftingRecipe.getOutputs().length > 1) {
                  results.fail("DiagramCrafting in recipe can only have 1 output");
               }

               if ("Fieldcraft".equals(benchRequirement.id) && craftingRecipe.getTimeSeconds() > 0.0F) {
                  results.warn(String.format("Bench Requirement in recipe for '%s' should not have a delay (TimeSeconds) set!", "Fieldcraft"));
               }
            }
         }
      })
      .afterDecode(CraftingRecipe::processConfig)
      .build();
   private static final MaterialQuantity[] EMPTY_OUTPUT = new MaterialQuantity[0];
   private static AssetStore<String, CraftingRecipe, DefaultAssetMap<String, CraftingRecipe>> ASSET_STORE;
   private AssetExtraInfo.Data data;
   protected String id;
   protected MaterialQuantity[] input;
   protected MaterialQuantity[] outputs = EMPTY_OUTPUT;
   protected MaterialQuantity primaryOutput;
   protected int primaryOutputQuantity = 1;
   protected BenchRequirement[] benchRequirement;
   protected float timeSeconds;
   protected boolean knowledgeRequired;
   protected int requiredMemoriesLevel = 1;

   public static AssetStore<String, CraftingRecipe, DefaultAssetMap<String, CraftingRecipe>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(CraftingRecipe.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, CraftingRecipe> getAssetMap() {
      return (DefaultAssetMap<String, CraftingRecipe>)getAssetStore().getAssetMap();
   }

   public CraftingRecipe(
      MaterialQuantity[] input,
      MaterialQuantity primaryOutput,
      MaterialQuantity[] outputs,
      int outputQuantity,
      BenchRequirement[] benchRequirement,
      float timeSeconds,
      boolean knowledgeRequired,
      int requiredMemoriesLevel
   ) {
      this.input = input;
      this.primaryOutput = primaryOutput;
      this.outputs = outputs;
      this.primaryOutputQuantity = outputQuantity;
      this.benchRequirement = benchRequirement;
      this.timeSeconds = timeSeconds;
      this.knowledgeRequired = knowledgeRequired;
      this.requiredMemoriesLevel = requiredMemoriesLevel;
   }

   public CraftingRecipe(CraftingRecipe other) {
      this.input = other.input;
      this.primaryOutput = other.primaryOutput;
      this.outputs = other.outputs;
      this.primaryOutputQuantity = other.primaryOutputQuantity;
      this.benchRequirement = other.benchRequirement;
      this.timeSeconds = other.timeSeconds;
      this.knowledgeRequired = other.knowledgeRequired;
      this.requiredMemoriesLevel = other.requiredMemoriesLevel;
   }

   protected CraftingRecipe() {
   }

   public static String generateIdFromItemRecipe(Item item, int i) {
      return item.id + "_Recipe_Generated_" + i;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.CraftingRecipe toPacket(String id) {
      com.hypixel.hytale.protocol.CraftingRecipe packet = new com.hypixel.hytale.protocol.CraftingRecipe();
      packet.id = id;
      if (this.input != null && this.input.length > 0) {
         packet.inputs = ArrayUtil.copyAndMutate(this.input, MaterialQuantity::toPacket, com.hypixel.hytale.protocol.MaterialQuantity[]::new);
      }

      packet.primaryOutput = this.primaryOutput.toPacket();
      if (this.outputs != null && this.outputs.length > 0) {
         packet.outputs = ArrayUtil.copyAndMutate(this.outputs, MaterialQuantity::toPacket, com.hypixel.hytale.protocol.MaterialQuantity[]::new);
      }

      if (this.benchRequirement != null && this.benchRequirement.length > 0) {
         packet.benchRequirement = this.benchRequirement;
      }

      packet.knowledgeRequired = this.knowledgeRequired;
      packet.timeSeconds = this.timeSeconds;
      packet.requiredMemoriesLevel = this.requiredMemoriesLevel;
      return packet;
   }

   private void processConfig() {
      if ((this.outputs == null || this.outputs.length == 0) && this.primaryOutput != null) {
         this.outputs = new MaterialQuantity[]{this.primaryOutput};
      }
   }

   public MaterialQuantity[] getInput() {
      return this.input;
   }

   public MaterialQuantity[] getOutputs() {
      return this.outputs;
   }

   public BenchRequirement[] getBenchRequirement() {
      return this.benchRequirement;
   }

   public float getTimeSeconds() {
      return this.timeSeconds;
   }

   public boolean isKnowledgeRequired() {
      return this.knowledgeRequired;
   }

   public int getRequiredMemoriesLevel() {
      return this.requiredMemoriesLevel;
   }

   public MaterialQuantity getPrimaryOutput() {
      return this.primaryOutput;
   }

   public boolean isRestrictedByBenchTierLevel(String benchId, int tierLevel) {
      if (this.benchRequirement == null) {
         return false;
      } else {
         for (BenchRequirement b : this.benchRequirement) {
            if (benchId.equals(b.id) && tierLevel < b.requiredTierLevel) {
               return true;
            }
         }

         return false;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "CraftingRecipe{input="
         + Arrays.toString((Object[])this.input)
         + ", extraOutputs="
         + Arrays.toString((Object[])this.outputs)
         + ", primaryOutput="
         + this.primaryOutput
         + ", outputQuantity="
         + this.primaryOutputQuantity
         + ", benchRequirement="
         + Arrays.toString((Object[])this.benchRequirement)
         + ", timeSeconds="
         + this.timeSeconds
         + ", knowledgeRequired="
         + this.knowledgeRequired
         + ", requiredMemoriesLevel="
         + this.requiredMemoriesLevel
         + "}";
   }

   public String getId() {
      return this.id;
   }
}
