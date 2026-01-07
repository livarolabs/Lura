package com.lura.di;

import com.lura.data.db.AppDatabase;
import com.lura.data.db.dao.BookDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class AppModule_ProvideBookDaoFactory implements Factory<BookDao> {
  private final Provider<AppDatabase> dbProvider;

  public AppModule_ProvideBookDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public BookDao get() {
    return provideBookDao(dbProvider.get());
  }

  public static AppModule_ProvideBookDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new AppModule_ProvideBookDaoFactory(dbProvider);
  }

  public static BookDao provideBookDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideBookDao(db));
  }
}
