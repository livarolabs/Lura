package com.lura.domain.engine;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class RsvpEngine_Factory implements Factory<RsvpEngine> {
  @Override
  public RsvpEngine get() {
    return newInstance();
  }

  public static RsvpEngine_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RsvpEngine newInstance() {
    return new RsvpEngine();
  }

  private static final class InstanceHolder {
    private static final RsvpEngine_Factory INSTANCE = new RsvpEngine_Factory();
  }
}
