package com.hypixel.hytale.server.npc.validators;

import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.valuestore.ValueStoreValidator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPCLoadTimeValidationHelper {
   private final String fileName;
   private final Model spawnModel;
   private final boolean isAbstract;
   private final HashSet<String> evaluatedAnimations = new HashSet<>();
   private final Set<Class<? extends MotionController>> providedMotionControllers = new HashSet<>();
   private final Set<Class<? extends MotionController>> requiredMotionControllers = new HashSet<>();
   private final ArrayDeque<HashSet<String>> seenFilterStack = new ArrayDeque<>();
   private final ValueStoreValidator valueStoreValidator = new ValueStoreValidator();
   @Nullable
   private Set<String> prioritiserProvidedFilterTypes;
   private int inventorySize;
   private int hotbarSize;
   private int offHandSize;
   private boolean parentSensorOnce;
   private boolean isVariant;
   private final ArrayDeque<String> stateStack = new ArrayDeque<>();

   public NPCLoadTimeValidationHelper(String fileName, Model spawnModel, boolean isAbstract) {
      this.fileName = fileName;
      this.spawnModel = spawnModel;
      this.isAbstract = isAbstract;
   }

   public void setInventorySizes(int inventorySize, int hotbarSize, int offHandSize) {
      this.inventorySize = inventorySize;
      this.hotbarSize = hotbarSize;
      this.offHandSize = offHandSize;
   }

   public Model getSpawnModel() {
      return this.spawnModel;
   }

   public boolean isAbstract() {
      return this.isAbstract;
   }

   public boolean isParentSensorOnce() {
      return this.parentSensorOnce;
   }

   public void updateParentSensorOnce(boolean parentSensorOnce) {
      this.parentSensorOnce |= parentSensorOnce;
   }

   public void clearParentSensorOnce() {
      this.parentSensorOnce = false;
   }

   public void setIsVariant() {
      this.isVariant = true;
   }

   public boolean isVariant() {
      return this.isVariant;
   }

   @Nonnull
   public ValueStoreValidator getValueStoreValidator() {
      return this.valueStoreValidator;
   }

   @Nullable
   public String getCurrentStateName() {
      return this.stateStack.peek();
   }

   public void pushCurrentStateName(@Nonnull String currentStateName) {
      this.stateStack.push(currentStateName);
   }

   public void popCurrentStateName() {
      this.stateStack.pop();
   }

   public void validateAnimation(@Nullable String animation) {
      if (animation != null && !animation.isEmpty()) {
         if (this.evaluatedAnimations.add(animation)) {
            if (!this.spawnModel.getAnimationSetMap().containsKey(animation)) {
               NPCPlugin.get().getLogger().at(Level.WARNING).log("Animation %s does not exist for model %s!", animation, this.spawnModel.getModelAssetId());
            }
         }
      }
   }

   public void registerMotionControllerType(Class<? extends MotionController> clazz) {
      this.providedMotionControllers.add(clazz);
   }

   public void requireMotionControllerType(Class<? extends MotionController> clazz) {
      this.requiredMotionControllers.add(clazz);
   }

   public boolean validateMotionControllers(@Nonnull List<String> errors) {
      if (this.requiredMotionControllers.isEmpty()) {
         return true;
      } else {
         ObjectArrayList<Class<? extends MotionController>> providedMotionControllerList = new ObjectArrayList(this.providedMotionControllers);
         int validCount = 0;

         for (Class<? extends MotionController> requiredMotionController : this.requiredMotionControllers) {
            boolean missing = true;

            for (int i = 0; i < providedMotionControllerList.size(); i++) {
               if (((Class)providedMotionControllerList.get(i)).isAssignableFrom(requiredMotionController)) {
                  validCount++;
                  missing = false;
                  break;
               }
            }

            if (missing) {
               errors.add(String.format("%s: Missing required motion controller: %s", this.fileName, requiredMotionController.getSimpleName()));
            }
         }

         return this.requiredMotionControllers.size() == validCount;
      }
   }

   public boolean validateInventoryHasSlot(int slot, String context, @Nonnull List<String> errors) {
      if (slot < this.inventorySize) {
         return true;
      } else {
         errors.add(String.format("%s: Inventory too small for slot %d, requested by %s", this.fileName, slot, context));
         return false;
      }
   }

   public boolean validateHotbarHasSlot(int slot, String context, @Nonnull List<String> errors) {
      if (slot < 0) {
         errors.add(String.format("%s: Hotbar slot %s is not valid for parameter %s. Must be >= 0", this.fileName, slot, context));
         return false;
      } else if (slot < this.hotbarSize) {
         return true;
      } else {
         errors.add(String.format("%s: Hotbar too small for slot %d, requested by %s. Actual size is %d", this.fileName, slot, context, this.hotbarSize));
         return false;
      }
   }

   public boolean validateOffHandHasSlot(int slot, String context, @Nonnull List<String> errors) {
      if (slot < -1) {
         errors.add(String.format("%s: Off-hand slot %s is not valid for parameter %s. Must be -1 for empty, or >= 0", this.fileName, slot, context));
         return false;
      } else if (slot < this.offHandSize) {
         return true;
      } else {
         errors.add(
            String.format("%s: Off-hand inventory too small for slot %d, requested by %s. Actual size is %d", this.fileName, slot, context, this.offHandSize)
         );
         return false;
      }
   }

   public void pushFilterSet() {
      this.seenFilterStack.push(new HashSet<>());
   }

   public void popFilterSet() {
      this.seenFilterStack.pop();
   }

   public boolean hasSeenFilter(String filter) {
      HashSet<String> set = this.seenFilterStack.peek();
      Objects.requireNonNull(set, "A filter set must have been pushed before checking if a filter has been seen!");
      return !set.add(filter);
   }

   public void setPrioritiserProvidedFilterTypes(Set<String> prioritiserProvidedFilterTypes) {
      this.prioritiserProvidedFilterTypes = prioritiserProvidedFilterTypes;
   }

   public boolean isFilterExternallyProvided(String filter) {
      return this.prioritiserProvidedFilterTypes == null ? false : this.prioritiserProvidedFilterTypes.contains(filter);
   }

   public void clearPrioritiserProvidedFilterTypes() {
      this.prioritiserProvidedFilterTypes = null;
   }
}
