package com.hypixel.hytale.server.npc.instructions.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.FeatureEvaluatorHelper;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.instructions.ActionList;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import com.hypixel.hytale.server.npc.instructions.HeadMotion;
import com.hypixel.hytale.server.npc.instructions.Instruction;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderInstruction extends BuilderBase<Instruction> {
   public static final String[] ANTECEDENT = new String[]{"TreeMode"};
   public static final String[] SUBSEQUENT = new String[]{"Continue"};
   protected final BuilderObjectReferenceHelper<Sensor> sensorBuilderObjectReferenceHelper = new BuilderObjectReferenceHelper<>(Sensor.class, this);
   protected final BuilderObjectReferenceHelper<BodyMotion> bodyMotionBuilderObjectReferenceHelper = new BuilderObjectReferenceHelper<>(BodyMotion.class, this);
   protected final BuilderObjectReferenceHelper<HeadMotion> headMotionBuilderObjectReferenceHelper = new BuilderObjectReferenceHelper<>(HeadMotion.class, this);
   protected final BuilderObjectReferenceHelper<ActionList> actionsBuilderObjectReferenceHelper = new BuilderObjectReferenceHelper<>(ActionList.class, this);
   protected final BuilderObjectListHelper<Instruction> steps = new BuilderObjectListHelper<>(Instruction.class, this);
   protected String name;
   protected String tag;
   protected boolean continueAfter;
   protected final DoubleHolder chance = new DoubleHolder();
   protected final BooleanHolder enabled = new BooleanHolder();
   protected boolean actionsBlocking;
   protected boolean actionsAtomic;
   protected boolean treeMode;
   protected final BooleanHolder invertTreeModeResult = new BooleanHolder();
   protected String currentStateName;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "An instruction with Sensor, and Motions and Actions, or a list of nested instructions.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "An instruction with Sensor, and Motions and Actions, or a list of nested instructions.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   @Override
   public Builder<Instruction> readConfig(@Nonnull JsonElement data) {
      FeatureEvaluatorHelper features = new FeatureEvaluatorHelper();
      BuilderValidationHelper helper = new BuilderValidationHelper(
         this.fileName,
         features,
         this.internalReferenceResolver,
         this.stateHelper,
         this.instructionContextHelper,
         this.extraInfo,
         this.evaluators,
         this.readErrors
      );
      if (this.requiresName()) {
         this.requireString(data, "Name", v -> this.name = v, null, BuilderDescriptorState.Stable, "Name for referencing", null);
      } else {
         this.getString(data, "Name", v -> this.name = v, null, null, BuilderDescriptorState.Stable, "Optional name for descriptor", null);
      }

      this.increaseDepth();
      this.getString(
         data,
         "Tag",
         v -> this.tag = v,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Experimental,
         "Internal identifier tag for debugging",
         null
      );
      this.getBoolean(data, "Enabled", this.enabled, true, BuilderDescriptorState.Stable, "Whether this instruction should be enabled on the NPC", null);
      this.getObject(
         data,
         "Sensor",
         this.sensorBuilderObjectReferenceHelper,
         BuilderDescriptorState.Stable,
         "Sensor for testing if instruction can be applied",
         "Sensor for testing if instruction can be applied. If not supplied, will always match",
         helper
      );
      features.lock();
      this.getObject(data, "BodyMotion", this.bodyMotionBuilderObjectReferenceHelper, BuilderDescriptorState.Stable, "Body motion to be executed", null, helper);
      this.getObject(data, "HeadMotion", this.headMotionBuilderObjectReferenceHelper, BuilderDescriptorState.Stable, "Head motion to be executed", null, helper);
      this.getObject(data, "Actions", this.actionsBuilderObjectReferenceHelper, BuilderDescriptorState.Stable, "Actions to be executed", null, helper);
      this.getBoolean(
         data,
         "ActionsBlocking",
         b -> this.actionsBlocking = b,
         false,
         BuilderDescriptorState.Stable,
         "Do not execute an action unless the previous action could execute",
         null
      );
      this.getBoolean(
         data, "ActionsAtomic", b -> this.actionsAtomic = b, false, BuilderDescriptorState.Stable, "Only execute actions if all actions can be executed", null
      );
      this.getArray(
         data,
         "Instructions",
         this.steps,
         null,
         BuilderDescriptorState.Stable,
         "Optional nested list of instructions",
         null,
         new BuilderValidationHelper(
            this.fileName,
            null,
            this.internalReferenceResolver,
            this.stateHelper,
            this.instructionContextHelper,
            this.extraInfo,
            this.evaluators,
            this.readErrors
         )
      );
      this.getBoolean(
         data, "Continue", v -> this.continueAfter = v, false, BuilderDescriptorState.WorkInProgress, "Continue after this instruction was executed", null
      );
      this.getDouble(
         data,
         "Weight",
         this.chance,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Weighted chance of picking this instruction in a random instruction",
         null
      );
      this.getBoolean(
         data,
         "TreeMode",
         v -> this.treeMode = v,
         false,
         BuilderDescriptorState.Stable,
         "Whether this instruction and its contents should be treated like a traditional behaviour tree.",
         "Whether this instruction and its contents should be treated like a traditional behaviour tree, i.e. will continue if all child instructions fail"
      );
      this.getBoolean(
         data,
         "InvertTreeModeResult",
         this.invertTreeModeResult,
         false,
         BuilderDescriptorState.Stable,
         "Whether or not to invert the result of TreeMode evaluation when passing up to parent TreeMode instructions",
         null
      );
      this.decreaseDepth();
      this.validateOneOrNonePresent("BodyMotion", this.bodyMotionBuilderObjectReferenceHelper, "Instructions", this.steps);
      this.validateOneOrNonePresent("HeadMotion", this.headMotionBuilderObjectReferenceHelper, "Instructions", this.steps);
      this.validateOneOrNonePresent("Actions", this.actionsBuilderObjectReferenceHelper, "Instructions", this.steps);
      this.validateBooleanImplicationAnyAntecedent(ANTECEDENT, new boolean[]{this.treeMode}, true, SUBSEQUENT, new boolean[]{this.continueAfter}, false);
      return this;
   }

   @Nullable
   public Instruction build(@Nonnull BuilderSupport builderSupport) {
      if (!this.enabled.get(builderSupport.getExecutionContext())) {
         return null;
      } else {
         Sensor sensor;
         if (this.sensorBuilderObjectReferenceHelper.isPresent()) {
            sensor = this.getSensor(builderSupport);
            if (sensor == null) {
               return null;
            }
         } else {
            sensor = Sensor.NULL;
         }

         if (this.currentStateName != null) {
            builderSupport.pushCurrentStateName(this.currentStateName);
         }

         Instruction[] instructionList = this.hasNestedInstructions() ? this.getSteps(builderSupport) : null;
         if (instructionList == null && !this.hasActions() && !this.hasBodyMotion() && !this.hasHeadMotion()) {
            if (this.currentStateName != null) {
               builderSupport.popCurrentStateName();
            }

            return null;
         } else {
            if (this.currentStateName != null) {
               builderSupport.popCurrentStateName();
            }

            return new Instruction(this, sensor, instructionList, builderSupport);
         }
      }
   }

   @Override
   public boolean validate(
      String configName,
      @Nonnull NPCLoadTimeValidationHelper validationHelper,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      if (!this.enabled.get(context)) {
         return true;
      } else {
         if (this.currentStateName != null) {
            validationHelper.pushCurrentStateName(this.currentStateName);
         }

         boolean result = this.sensorBuilderObjectReferenceHelper.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
            & this.bodyMotionBuilderObjectReferenceHelper.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
            & this.headMotionBuilderObjectReferenceHelper.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
            & this.actionsBuilderObjectReferenceHelper.validate(configName, validationHelper, this.builderManager, context, globalScope, errors)
            & this.steps.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
         if (this.currentStateName != null) {
            validationHelper.popCurrentStateName();
         }

         validationHelper.clearParentSensorOnce();
         return result;
      }
   }

   @Nonnull
   @Override
   public Class<Instruction> category() {
      return Instruction.class;
   }

   @Override
   public final boolean isEnabled(ExecutionContext context) {
      return this.enabled.get(context);
   }

   @Override
   public void setCurrentStateName(String name) {
      this.currentStateName = name;
   }

   public boolean hasActions() {
      return this.actionsBuilderObjectReferenceHelper.isPresent();
   }

   public boolean hasBodyMotion() {
      return this.bodyMotionBuilderObjectReferenceHelper.isPresent();
   }

   public boolean hasHeadMotion() {
      return this.headMotionBuilderObjectReferenceHelper.isPresent();
   }

   public boolean hasNestedInstructions() {
      return this.steps.isPresent() && !this.steps.hasNoElements();
   }

   @Nullable
   public Sensor getSensor(@Nonnull BuilderSupport builderSupport) {
      return this.sensorBuilderObjectReferenceHelper.build(builderSupport);
   }

   @Nullable
   public BodyMotion getBodyMotion(@Nonnull BuilderSupport builderSupport) {
      return this.bodyMotionBuilderObjectReferenceHelper.build(builderSupport);
   }

   @Nullable
   public HeadMotion getHeadMotion(@Nonnull BuilderSupport support) {
      return this.headMotionBuilderObjectReferenceHelper.build(support);
   }

   @Nonnull
   public ActionList getActionList(@Nonnull BuilderSupport builderSupport) {
      ActionList actions = this.actionsBuilderObjectReferenceHelper.build(builderSupport);
      if (actions == null) {
         return ActionList.EMPTY_ACTION_LIST;
      } else {
         if (actions != ActionList.EMPTY_ACTION_LIST) {
            actions.setAtomic(this.actionsAtomic);
            actions.setBlocking(this.actionsBlocking);
         }

         return actions;
      }
   }

   @Nonnull
   public Instruction[] getSteps(@Nonnull BuilderSupport support) {
      List<Instruction> stepList = this.steps.build(support);
      return stepList == null ? Instruction.EMPTY_ARRAY : stepList.toArray(Instruction[]::new);
   }

   @Nullable
   public String getName() {
      return this.name;
   }

   public String getTag() {
      return this.tag;
   }

   public boolean isContinueAfter() {
      return this.continueAfter;
   }

   public double getChance(@Nonnull BuilderSupport support) {
      return this.chance.get(support.getExecutionContext());
   }

   public boolean isTreeMode() {
      return this.treeMode;
   }

   public boolean isInvertTreeModeResult(@Nonnull BuilderSupport support) {
      return this.invertTreeModeResult.get(support.getExecutionContext());
   }

   protected boolean requiresName() {
      return false;
   }
}
