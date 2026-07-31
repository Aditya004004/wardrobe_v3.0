package com.example.wardeobe.viewmodel;

import com.example.wardeobe.data.WardrobeRepository;
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
public final class UploadViewModel_Factory implements Factory<UploadViewModel> {
  private final Provider<WardrobeRepository> repositoryProvider;

  public UploadViewModel_Factory(Provider<WardrobeRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public UploadViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static UploadViewModel_Factory create(Provider<WardrobeRepository> repositoryProvider) {
    return new UploadViewModel_Factory(repositoryProvider);
  }

  public static UploadViewModel newInstance(WardrobeRepository repository) {
    return new UploadViewModel(repository);
  }
}
