package com.example.wardeobe.di;

import com.google.firebase.functions.FirebaseFunctions;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class RepositoryModule_ProvideFirebaseFunctionsFactory implements Factory<FirebaseFunctions> {
  @Override
  public FirebaseFunctions get() {
    return provideFirebaseFunctions();
  }

  public static RepositoryModule_ProvideFirebaseFunctionsFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static FirebaseFunctions provideFirebaseFunctions() {
    return Preconditions.checkNotNullFromProvides(RepositoryModule.INSTANCE.provideFirebaseFunctions());
  }

  private static final class InstanceHolder {
    private static final RepositoryModule_ProvideFirebaseFunctionsFactory INSTANCE = new RepositoryModule_ProvideFirebaseFunctionsFactory();
  }
}
