package com.example.wardeobe.viewmodel;

import com.example.wardeobe.data.ProfileRepository;
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
public final class OutfitViewModel_Factory implements Factory<OutfitViewModel> {
  private final Provider<ProfileRepository> profileRepositoryProvider;

  private final Provider<WardrobeRepository> repositoryProvider;

  public OutfitViewModel_Factory(Provider<ProfileRepository> profileRepositoryProvider,
      Provider<WardrobeRepository> repositoryProvider) {
    this.profileRepositoryProvider = profileRepositoryProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public OutfitViewModel get() {
    return newInstance(profileRepositoryProvider.get(), repositoryProvider.get());
  }

  public static OutfitViewModel_Factory create(
      Provider<ProfileRepository> profileRepositoryProvider,
      Provider<WardrobeRepository> repositoryProvider) {
    return new OutfitViewModel_Factory(profileRepositoryProvider, repositoryProvider);
  }

  public static OutfitViewModel newInstance(ProfileRepository profileRepository,
      WardrobeRepository repository) {
    return new OutfitViewModel(profileRepository, repository);
  }
}
