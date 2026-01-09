package com.lura.data.engine;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class CoverExtractor_Factory implements Factory<CoverExtractor> {
  private final Provider<Context> contextProvider;

  public CoverExtractor_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public CoverExtractor get() {
    return newInstance(contextProvider.get());
  }

  public static CoverExtractor_Factory create(Provider<Context> contextProvider) {
    return new CoverExtractor_Factory(contextProvider);
  }

  public static CoverExtractor newInstance(Context context) {
    return new CoverExtractor(context);
  }
}
