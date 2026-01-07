package com.lura.ui.reader;

import androidx.lifecycle.SavedStateHandle;
import com.lura.domain.engine.EpubParser;
import com.lura.domain.engine.RsvpEngine;
import com.lura.domain.hardware.HardwareKeyManager;
import com.lura.domain.repository.LibraryRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ReaderViewModel_Factory implements Factory<ReaderViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<LibraryRepository> repositoryProvider;

  private final Provider<EpubParser> epubParserProvider;

  private final Provider<RsvpEngine> rsvpEngineProvider;

  private final Provider<HardwareKeyManager> hardwareKeyManagerProvider;

  public ReaderViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<LibraryRepository> repositoryProvider, Provider<EpubParser> epubParserProvider,
      Provider<RsvpEngine> rsvpEngineProvider,
      Provider<HardwareKeyManager> hardwareKeyManagerProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.repositoryProvider = repositoryProvider;
    this.epubParserProvider = epubParserProvider;
    this.rsvpEngineProvider = rsvpEngineProvider;
    this.hardwareKeyManagerProvider = hardwareKeyManagerProvider;
  }

  @Override
  public ReaderViewModel get() {
    return newInstance(savedStateHandleProvider.get(), repositoryProvider.get(), epubParserProvider.get(), rsvpEngineProvider.get(), hardwareKeyManagerProvider.get());
  }

  public static ReaderViewModel_Factory create(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<LibraryRepository> repositoryProvider, Provider<EpubParser> epubParserProvider,
      Provider<RsvpEngine> rsvpEngineProvider,
      Provider<HardwareKeyManager> hardwareKeyManagerProvider) {
    return new ReaderViewModel_Factory(savedStateHandleProvider, repositoryProvider, epubParserProvider, rsvpEngineProvider, hardwareKeyManagerProvider);
  }

  public static ReaderViewModel newInstance(SavedStateHandle savedStateHandle,
      LibraryRepository repository, EpubParser epubParser, RsvpEngine rsvpEngine,
      HardwareKeyManager hardwareKeyManager) {
    return new ReaderViewModel(savedStateHandle, repository, epubParser, rsvpEngine, hardwareKeyManager);
  }
}
