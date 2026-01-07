package com.lura.domain.hardware;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class HardwareKeyManager_Factory implements Factory<HardwareKeyManager> {
  @Override
  public HardwareKeyManager get() {
    return newInstance();
  }

  public static HardwareKeyManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static HardwareKeyManager newInstance() {
    return new HardwareKeyManager();
  }

  private static final class InstanceHolder {
    private static final HardwareKeyManager_Factory INSTANCE = new HardwareKeyManager_Factory();
  }
}
