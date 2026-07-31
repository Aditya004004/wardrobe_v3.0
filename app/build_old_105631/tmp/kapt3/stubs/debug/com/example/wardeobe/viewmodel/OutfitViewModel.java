package com.example.wardeobe.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000r\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0010\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0010\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u00020\bH\u0002J\u0010\u00100\u001a\u0004\u0018\u00010\bH\u0082@\u00a2\u0006\u0002\u00101J \u00102\u001a\u0004\u0018\u00010\b2\u0006\u00103\u001a\u00020\b2\u0006\u00104\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u00105J\u0018\u00106\u001a\u0004\u0018\u00010\b2\u0006\u00107\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u00108J\u0014\u00109\u001a\u00020:2\f\u0010;\u001a\b\u0012\u0004\u0012\u00020=0<J \u0010>\u001a\u0004\u0018\u00010\b2\u0006\u0010?\u001a\u00020\b2\u0006\u0010@\u001a\u00020AH\u0082@\u00a2\u0006\u0002\u0010BJ\u0018\u0010C\u001a\u0004\u0018\u00010\b2\u0006\u0010D\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u00108J\u0006\u0010E\u001a\u00020.J\u0006\u0010F\u001a\u00020.J\u0018\u0010G\u001a\u00020.2\u0006\u0010H\u001a\u00020\b2\u0006\u00104\u001a\u00020\bH\u0002J\u0016\u0010I\u001a\u00020.2\u0006\u0010J\u001a\u00020K2\u0006\u0010L\u001a\u00020MJ\u000e\u0010N\u001a\u00020.2\u0006\u0010O\u001a\u00020\bJ\u000e\u0010P\u001a\u00020.2\u0006\u0010Q\u001a\u00020\bJ\u000e\u0010R\u001a\u00020.2\u0006\u0010S\u001a\u00020\bJ\u000e\u0010T\u001a\u00020.2\u0006\u0010U\u001a\u00020\bJ\u000e\u0010V\u001a\u00020.2\u0006\u0010W\u001a\u00020\bR\u000e\u0010\u0007\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082D\u00a2\u0006\u0002\n\u0000R\u0014\u0010\n\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\f0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0012\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\f0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\f0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\f0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001c\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001d\u0010\u001e\"\u0004\b\u001f\u0010 R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010!\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010\u001e\"\u0004\b#\u0010 R\u001a\u0010$\u001a\u00020\bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b%\u0010\u001e\"\u0004\b&\u0010 R\u0019\u0010\'\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010\u0019R\u0017\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00110\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u0019R\u0019\u0010+\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0017\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u0019\u00a8\u0006X"}, d2 = {"Lcom/example/wardeobe/viewmodel/OutfitViewModel;", "Landroidx/lifecycle/ViewModel;", "profileRepository", "Lcom/example/wardeobe/data/ProfileRepository;", "repository", "Lcom/example/wardeobe/data/WardrobeRepository;", "(Lcom/example/wardeobe/data/ProfileRepository;Lcom/example/wardeobe/data/WardrobeRepository;)V", "FREEPIK_API_KEY", "", "FREEPIK_ENDPOINT", "_hasProfilePicture", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_isGeneratingShoppingOutfit", "_isGeneratingVTO", "_shoppingImageUrl", "_userProfile", "Lcom/example/wardeobe/model/UserProfile;", "_vtoImageUrl", "cachedProfileUrl", "client", "Lokhttp3/OkHttpClient;", "hasProfilePicture", "Lkotlinx/coroutines/flow/StateFlow;", "getHasProfilePicture", "()Lkotlinx/coroutines/flow/StateFlow;", "isGeneratingShoppingOutfit", "isGeneratingVTO", "recommendationType", "getRecommendationType", "()Ljava/lang/String;", "setRecommendationType", "(Ljava/lang/String;)V", "selectedOccasion", "getSelectedOccasion", "setSelectedOccasion", "selectedStyle", "getSelectedStyle", "setSelectedStyle", "shoppingImageUrl", "getShoppingImageUrl", "userProfile", "getUserProfile", "vtoImageUrl", "getVtoImageUrl", "cleanUpTemporaryGarment", "", "publicId", "generateNewOutfitWithFreepik", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateVtoImage", "outfitUrl", "profileUrl", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateVtoImageFallback", "garmentUrl", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getRecommendedOutfit", "Lcom/example/wardeobe/model/RecommendedOutfit;", "fullWardrobe", "", "Lcom/example/wardeobe/model/ClothingItem;", "initiateFreepikGeneration", "prompt", "referenceImages", "Lorg/json/JSONArray;", "(Ljava/lang/String;Lorg/json/JSONArray;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "pollFreepikTask", "taskId", "resetShoppingOutfit", "startShoppingOutfitGeneration", "startVtoGeneration", "shopImageUrl", "startVtoLocalGeneration", "context", "Landroid/content/Context;", "garmentUri", "Landroid/net/Uri;", "updateAgeGroup", "ageGroup", "updateBodyType", "bodyType", "updateGender", "gender", "updateHeightGroup", "heightGroup", "updateSkinTone", "skinTone", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class OutfitViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.example.wardeobe.data.ProfileRepository profileRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.example.wardeobe.data.WardrobeRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String FREEPIK_API_KEY = "dummy_freepik_key";
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String FREEPIK_ENDPOINT = "https://api.magnific.com/v1/ai/gemini-2-5-flash-image-preview";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.example.wardeobe.model.UserProfile> _userProfile = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.example.wardeobe.model.UserProfile> userProfile = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String selectedOccasion = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String selectedStyle = "";
    @org.jetbrains.annotations.NotNull()
    private java.lang.String recommendationType = "personal";
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _shoppingImageUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> shoppingImageUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isGeneratingShoppingOutfit = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGeneratingShoppingOutfit = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _vtoImageUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> vtoImageUrl = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isGeneratingVTO = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGeneratingVTO = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _hasProfilePicture = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> hasProfilePicture = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String cachedProfileUrl;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    
    @javax.inject.Inject()
    public OutfitViewModel(@org.jetbrains.annotations.NotNull()
    com.example.wardeobe.data.ProfileRepository profileRepository, @org.jetbrains.annotations.NotNull()
    com.example.wardeobe.data.WardrobeRepository repository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.example.wardeobe.model.UserProfile> getUserProfile() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSelectedOccasion() {
        return null;
    }
    
    public final void setSelectedOccasion(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getSelectedStyle() {
        return null;
    }
    
    public final void setSelectedStyle(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRecommendationType() {
        return null;
    }
    
    public final void setRecommendationType(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getShoppingImageUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGeneratingShoppingOutfit() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getVtoImageUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isGeneratingVTO() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHasProfilePicture() {
        return null;
    }
    
    public final void updateGender(@org.jetbrains.annotations.NotNull()
    java.lang.String gender) {
    }
    
    public final void updateBodyType(@org.jetbrains.annotations.NotNull()
    java.lang.String bodyType) {
    }
    
    public final void updateSkinTone(@org.jetbrains.annotations.NotNull()
    java.lang.String skinTone) {
    }
    
    public final void updateAgeGroup(@org.jetbrains.annotations.NotNull()
    java.lang.String ageGroup) {
    }
    
    public final void updateHeightGroup(@org.jetbrains.annotations.NotNull()
    java.lang.String heightGroup) {
    }
    
    public final void resetShoppingOutfit() {
    }
    
    public final void startVtoLocalGeneration(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.net.Uri garmentUri) {
    }
    
    public final void startShoppingOutfitGeneration() {
    }
    
    private final void startVtoGeneration(java.lang.String shopImageUrl, java.lang.String profileUrl) {
    }
    
    private final java.lang.Object initiateFreepikGeneration(java.lang.String prompt, org.json.JSONArray referenceImages, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object generateVtoImage(java.lang.String outfitUrl, java.lang.String profileUrl, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object generateVtoImageFallback(java.lang.String garmentUrl, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object generateNewOutfitWithFreepik(kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final java.lang.Object pollFreepikTask(java.lang.String taskId, kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    private final void cleanUpTemporaryGarment(java.lang.String publicId) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.wardeobe.model.RecommendedOutfit getRecommendedOutfit(@org.jetbrains.annotations.NotNull()
    java.util.List<com.example.wardeobe.model.ClothingItem> fullWardrobe) {
        return null;
    }
}