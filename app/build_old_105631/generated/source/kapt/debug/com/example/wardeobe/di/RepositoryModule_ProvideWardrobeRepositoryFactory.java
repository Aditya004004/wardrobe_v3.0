package com.example.wardeobe.di;

import com.example.wardeobe.data.WardrobeRepository;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
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
public final class RepositoryModule_ProvideWardrobeRepositoryFactory implements Factory<WardrobeRepository> {
  private final Provider<FirebaseFunctions> functionsProvider;

  private final Provider<FirebaseFirestore> firestoreProvider;

  public RepositoryModule_ProvideWardrobeRepositoryFactory(
      Provider<FirebaseFunctions> functionsProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    this.functionsProvider = functionsProvider;
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public WardrobeRepository get() {
    return provideWardrobeRepository(functionsProvider.get(), firestoreProvider.get());
  }

  public static RepositoryModule_ProvideWardrobeRepositoryFactory create(
      Provider<FirebaseFunctions> functionsProvider,
      Provider<FirebaseFirestore> firestoreProvider) {
    return new RepositoryModule_ProvideWardrobeRepositoryFactory(functionsProvider, firestoreProvider);
  }

  public static WardrobeRepository provideWardrobeRepository(FirebaseFunctions functions,
      FirebaseFirestore firestore) {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideWardrobeRepository(functions, firestore));
  }
}
