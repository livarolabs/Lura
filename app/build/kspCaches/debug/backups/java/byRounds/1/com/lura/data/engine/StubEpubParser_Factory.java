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
public final class StubEpubParser_Factory implements Factory<StubEpubParser> {
  @Override
  public StubEpubParser get() {
    return newInstance();
  }

  public static StubEpubParser_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static StubEpubParser newInstance() {
    return new StubEpubParser();
  }

  private static final class InstanceHolder {
    private static final StubEpubParser_Factory INSTANCE = new StubEpubParser_Factory();
  }
}
