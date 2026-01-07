package com.lura.app;

import com.lura.domain.hardware.HardwareKeyManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<HardwareKeyManager> hardwareKeyManagerProvider;

  public MainActivity_MembersInjector(Provider<HardwareKeyManager> hardwareKeyManagerProvider) {
    this.hardwareKeyManagerProvider = hardwareKeyManagerProvider;
  }

  public static MembersInjector<MainActivity> create(
      Provider<HardwareKeyManager> hardwareKeyManagerProvider) {
    return new MainActivity_MembersInjector(hardwareKeyManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectHardwareKeyManager(instance, hardwareKeyManagerProvider.get());
  }

  @InjectedFieldSignature("com.lura.app.MainActivity.hardwareKeyManager")
  public static void injectHardwareKeyManager(MainActivity instance,
      HardwareKeyManager hardwareKeyManager) {
    instance.hardwareKeyManager = hardwareKeyManager;
  }
}
