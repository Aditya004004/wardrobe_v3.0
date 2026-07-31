package com.example.wardeobe.data;

import com.google.firebase.firestore.FirebaseFirestore;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class ProfileRepository_Factory implements Factory<ProfileRepository> {
  private final Provider<FirebaseFirestore> firestoreProvider;

  public ProfileRepository_Factory(Provider<FirebaseFirestore> firestoreProvider) {
    this.firestoreProvider = firestoreProvider;
  }

  @Override
  public ProfileRepository get() {
    return newInstance(firestoreProvider.get());
  }

  public static ProfileRepository_Factory create(Provider<FirebaseFirestore> firestoreProvider) {
    return new ProfileRepository_Factory(firestoreProvider);
  }

  public static ProfileRepository newInstance(FirebaseFirestore firestore) {
    return new ProfileRepository(firestore);
  }
}
