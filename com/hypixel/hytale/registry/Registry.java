package com.hypixel.hytale.registry;

import com.hypixel.hytale.function.consumer.BooleanConsumer;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Registry<T extends Registration> {
   @Nonnull
   private final BooleanSupplier precondition;
   @Nullable
   private final String preconditionMessage;
   private final Registry.RegistrationWrapFunction<T> wrappingFunction;
   @Nonnull
   private final List<BooleanConsumer> registrations;
   @Nonnull
   private final List<BooleanConsumer> unmodifiableRegistrations;
   private boolean enabled = true;

   protected Registry(
      @Nonnull List<BooleanConsumer> registrations,
      @Nonnull BooleanSupplier precondition,
      @Nullable String preconditionMessage,
      @Nonnull Registry.RegistrationWrapFunction<T> wrappingFunction
   ) {
      this.registrations = registrations;
      this.unmodifiableRegistrations = Collections.unmodifiableList(registrations);
      this.precondition = precondition;
      this.preconditionMessage = preconditionMessage;
      this.wrappingFunction = wrappingFunction;
   }

   protected void checkPrecondition() {
      if (!this.precondition.getAsBoolean()) {
         throw new IllegalStateException(this.preconditionMessage);
      }
   }

   public boolean isEnabled() {
      return this.enabled;
   }

   public void enable() {
      this.enabled = true;
   }

   public void shutdown() {
      this.enabled = false;
   }

   public void shutdownAndCleanup(boolean shutdown) {
      this.enabled = false;

      for (int i = this.registrations.size() - 1; i >= 0; i--) {
         this.registrations.get(i).accept(shutdown);
      }

      this.registrations.clear();
   }

   public T register(@Nonnull T registration) {
      if (!this.enabled) {
         registration.unregister();
         throw new IllegalStateException("Registry is not enabled!");
      } else {
         BooleanConsumer reg = v -> registration.unregister();
         this.registrations.add(reg);
         return this.wrappingFunction.wrap(registration, () -> this.enabled || registration.isRegistered(), () -> {
            this.registrations.remove(reg);
            registration.unregister();
         });
      }
   }

   @Nonnull
   public List<BooleanConsumer> getRegistrations() {
      return this.unmodifiableRegistrations;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Registry{registrations.size()=" + this.registrations.size() + "}";
   }

   public interface RegistrationWrapFunction<T extends Registration> {
      T wrap(T var1, BooleanSupplier var2, Runnable var3);
   }
}
