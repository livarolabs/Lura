package com.lura.data.engine;

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
public final class RealEpubParser_Factory implements Factory<RealEpubParser> {
  @Override
  public RealEpubParser get() {
    return newInstance();
  }

  public static RealEpubParser_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RealEpubParser newInstance() {
    return new RealEpubParser();
  }

  private static final class InstanceHolder {
    private static final RealEpubParser_Factory INSTANCE = new RealEpubParser_Factory();
  }
}
