package com.lura.data.repository;

import android.content.Context;
import com.lura.data.db.dao.BookDao;
import com.lura.data.db.dao.HighlightDao;
import com.lura.domain.engine.EpubParser;
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
public final class LibraryRepositoryImpl_Factory implements Factory<LibraryRepositoryImpl> {
  private final Provider<BookDao> bookDaoProvider;

  private final Provider<HighlightDao> highlightDaoProvider;

  private final Provider<Context> contextProvider;

  private final Provider<EpubParser> epubParserProvider;

  public LibraryRepositoryImpl_Factory(Provider<BookDao> bookDaoProvider,
      Provider<HighlightDao> highlightDaoProvider, Provider<Context> contextProvider,
      Provider<EpubParser> epubParserProvider) {
    this.bookDaoProvider = bookDaoProvider;
    this.highlightDaoProvider = highlightDaoProvider;
    this.contextProvider = contextProvider;
    this.epubParserProvider = epubParserProvider;
  }

  @Override
  public LibraryRepositoryImpl get() {
    return newInstance(bookDaoProvider.get(), highlightDaoProvider.get(), contextProvider.get(), epubParserProvider.get());
  }

  public static LibraryRepositoryImpl_Factory create(Provider<BookDao> bookDaoProvider,
      Provider<HighlightDao> highlightDaoProvider, Provider<Context> contextProvider,
      Provider<EpubParser> epubParserProvider) {
    return new LibraryRepositoryImpl_Factory(bookDaoProvider, highlightDaoProvider, contextProvider, epubParserProvider);
  }

  public static LibraryRepositoryImpl newInstance(BookDao bookDao, HighlightDao highlightDao,
      Context context, EpubParser epubParser) {
    return new LibraryRepositoryImpl(bookDao, highlightDao, context, epubParser);
  }
}
