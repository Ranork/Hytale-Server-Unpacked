package com.hypixel.hytale.codec.validation;

import com.hypixel.hytale.codec.ExtraInfo;

public interface LateValidator<T> extends Validator<T> {
   void acceptLate(T var1, ValidationResults var2, ExtraInfo var3);
}
