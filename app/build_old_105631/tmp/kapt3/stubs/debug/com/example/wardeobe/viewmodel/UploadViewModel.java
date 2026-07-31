package com.example.wardeobe.viewmodel;

/**
 * Handles image upload, AI processing (Freepik Gemini), and Cloudinary upload.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\\\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0018\u0010\u0013\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0014\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0015J\u0018\u0010\u0016\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0017\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0015J\u0016\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010\u0015J,\u0010\u001b\u001a\u00020\u00192\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020\u00062\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00190\"J(\u0010#\u001a\f\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0018\u00010$2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0086@\u00a2\u0006\u0002\u0010%J(\u0010&\u001a\f\u0012\u0002\b\u0003\u0012\u0002\b\u0003\u0018\u00010$2\u0006\u0010\'\u001a\u00020\u00062\u0006\u0010 \u001a\u00020\u0006H\u0082@\u00a2\u0006\u0002\u0010(J \u0010)\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0082@\u00a2\u0006\u0002\u0010%R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u0014\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012\u00a8\u0006*"}, d2 = {"Lcom/example/wardeobe/viewmodel/UploadViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/example/wardeobe/data/WardrobeRepository;", "(Lcom/example/wardeobe/data/WardrobeRepository;)V", "FREEPIK_API_KEY", "", "FREEPIK_ENDPOINT", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/example/wardeobe/viewmodel/UploadUiState;", "client", "Lokhttp3/OkHttpClient;", "cloudinary", "Lcom/cloudinary/Cloudinary;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "generateWithFreepik", "base64Image", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pollFreepikTask", "taskId", "showError", "", "msg", "uploadImageWithAI", "context", "Landroid/content/Context;", "uri", "Landroid/net/Uri;", "category", "onUploadComplete", "Lkotlin/Function0;", "uploadTemporaryUri", "", "(Landroid/content/Context;Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uploadToCloudinary", "imageUrl", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "uriToBase64", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class UploadViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.wardeobe.data.WardrobeRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String FREEPIK_API_KEY = "dummy_freepik_key";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String FREEPIK_ENDPOINT = "https://api.magnific.com/v1/ai/gemini-2-5-flash-image-preview";
    @org.jetbrains.annotations.NotNull()
    private final com.cloudinary.Cloudinary cloudinary = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.wardeobe.viewmodel.UploadUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.wardeobe.viewmodel.UploadUiState> uiState = null;
    
    @javax.inject.Inject()
    public UploadViewModel(@org.jetbrains.annotations.NotNull()
    com.example.wardeobe.data.WardrobeRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.wardeobe.viewmodel.UploadUiState> getUiState() {
        return null;
    }
    
    /**
     * Initiates the full upload and AI generation process (For permanent wardrobe items).
     * 🌟 Updated to accept category.
     */
    public final void uploadImageWithAI(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    java.lang.String category, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUploadComplete) {
    }
    
    private final java.lang.Object uriToBase64(android.content.Context context, android.net.Uri uri, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object generateWithFreepik(java.lang.String base64Image, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object pollFreepikTask(java.lang.String taskId, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object uploadToCloudinary(java.lang.String imageUrl, java.lang.String category, kotlin.coroutines.Continuation<? super java.util.Map<?, ?>> $completion) {
        return null;
    }
    
    /**
     * Uploads the local image URI to Cloudinary temporarily to get a public URL for the VTO AI.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object uploadTemporaryUri(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri uri, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.Map<?, ?>> $completion) {
        return null;
    }
    
    private final java.lang.Object showError(java.lang.String msg, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
}