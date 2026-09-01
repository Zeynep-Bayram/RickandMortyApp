\# Rick and Morty App



\*\*Jetpack Compose\*\* ve \*\*Kotlin\*\* ile geliştirilmiş, Rick and Morty API üzerinden karakter verilerini listeleyen, arayan ve interaktif 3D flip kart animasyonuyla detaylandıran bir Android uygulaması.



Uygulama; REST API üzerinden asenkron veri çekme, MVVM mimarisi, state-driven UI tasarımı ve Jetpack Compose animasyonları gibi modern Android geliştirme pratiklerini bir araya getirir.



<p align="center">

&#x20; <a href="#-özellikler">Özellikler</a> •

&#x20; <a href="#-ekran-görüntüleri">Ekran Görüntüleri</a> •

&#x20; <a href="#-kullanılan-teknolojiler">Teknolojiler</a> •

&#x20; <a href="#-mimari">Mimari</a> •

&#x20; <a href="#-projeyi-çalıştırma">Çalıştırma</a>

</p>



\---



\## Özellikler



\-  \*\*Karakter Listeleme\*\* — Rick and Morty API üzerinden karakter verilerinin çekilip listelenmesi.

\-  \*\*Karakter Arama\*\* — Girilen isme göre API üzerinden anlık arama yapılabilmesi.

\-  \*\*3D Flip Kart Animasyonu\*\* — Karakter kartına dokunulduğunda kartın 180° dönerek arka yüzündeki detayları göstermesi.

\-  \*\*Detaylı Karakter Bilgisi\*\* — Durum (status), tür (species), cinsiyet, köken ve mevcut konum bilgilerinin görüntülenmesi.

\-  \*\*State Yönetimi\*\* — Loading, Success ve Error durumlarının ayrı ayrı ve tutarlı biçimde yönetilmesi.

\-  \*\*Hata Yönetimi ve Tekrar Deneme\*\* — Ağ/API hatasında kullanıcıya açık mesaj ve "Tekrar Dene" seçeneği sunulması.

\-  \*\*Uzaktan Görsel Yükleme\*\* — Karakter görsellerinin Coil ile API üzerinden verimli biçimde yüklenmesi.



\---



\##  Ekran Görüntüleri



> Aşağıdaki görselleri projenizin kök dizininde bir `screenshots/` klasörü oluşturarak ekleyin ve dosya adlarını kendi görüntülerinizle eşleştirin.



| Ana Ekran | Karakter Arama | Kart Detayı (Flip) |

|:---:|:---:|:---:|

| !\[Ana Ekran](screenshots/home.png) | !\[Arama](screenshots/search.png) | !\[Detay](screenshots/flip.png) |



\---



\##  Kullanılan Teknolojiler



| Katman | Teknoloji | Açıklama |

|---|---|---|

| Dil | \*\*Kotlin\*\* | Android için resmi ve modern programlama dili |

| UI | \*\*Jetpack Compose\*\* | Deklaratif ve modern Android UI araç seti |

| UI Bileşenleri | \*\*Material 3\*\* | Tasarım sistemi ve hazır bileşenler |

| Asenkronluk | \*\*Kotlin Coroutines\*\* | Non-blocking, asenkron API çağrıları |

| Ağ Katmanı | \*\*Retrofit\*\* | Tip güvenli (type-safe) HTTP istemcisi |

| Serileştirme | \*\*Gson\*\* | JSON ↔ Kotlin model dönüşümü |

| Görsel Yükleme | \*\*Coil\*\* | Compose ile uyumlu, coroutine tabanlı görsel yükleme kütüphanesi |

| State Yönetimi | \*\*ViewModel\*\* | Konfigürasyon değişikliklerine dayanıklı UI state yönetimi |

| Mimari | \*\*MVVM\*\* | Model-View-ViewModel katmanlı mimari |



\---



\##  Mimari



Proje \*\*MVVM (Model-View-ViewModel)\*\* mimari desenini takip eder. Bu yapı; sorumlulukların katmanlar arasında net biçimde ayrılmasını, test edilebilirliği ve sürdürülebilirliği sağlar.



\### Veri Akışı



```

Rick and Morty API

&#x20;       ↓

&#x20;    Retrofit

&#x20;       ↓

&#x20;  CharacterApi

&#x20;       ↓

&#x20;CharacterViewModel

&#x20;       ↓

&#x20;CharacterUiState

&#x20;       ↓

&#x20;CharacterListScreen (Jetpack Compose)

```



\### Katmanlar



\*\*Model\*\*

API'den gelen verileri temsil eden veri sınıflarını içerir.

\- `Character` — Tekil karakter modeli

\- `CharacterResponse` — API'den dönen liste/response modeli

\- `CharacterApi` — Retrofit servis arayüzü



\*\*ViewModel\*\*

`CharacterViewModel`, API'den veri çekme işlemini coroutine'ler üzerinden asenkron olarak yürütür ve sonucu `CharacterUiState` (Loading / Success / Error) üzerinden View katmanına iletir. Arama sorgularının yönetimi de bu katmanda gerçekleşir.



\*\*View\*\*

Jetpack Compose ile oluşturulan `CharacterListScreen`, ViewModel'deki state'i gözlemler ve buna göre:

\- Loading durumunda `CircularProgressIndicator` gösterir,

\- Success durumunda karakterleri `LazyColumn` içinde listeler,

\- Error durumunda hata mesajı ve tekrar deneme butonu sunar.



Kart etkileşimleri ve flip animasyonu da bu katmanda yönetilir.



\###  Karakter Arama



Arama işlemi istemci tarafında filtreleme yerine doğrudan API üzerinden gerçekleştirilir. Kullanıcının girdiği metin `@Query("name")` parametresiyle isteğe eklenir ve dönen sonuç yeniden `CharacterUiState` üzerinden ekrana yansıtılır.



\###  3D Kart Animasyonu



Karakter kartına dokunulduğunda, kartın arka yüzündeki detay bilgilerini göstermek amacıyla 3D flip animasyonu uygulanmıştır.



```

0° ──────────────────→ 180°

Ön Yüz (Görsel/İsim)     Arka Yüz (Detaylar)

```



Animasyon `animateFloatAsState` ve `graphicsLayer` (`rotationY`, `cameraDistance`) kullanılarak oluşturulmuştur. Kartın arka yüzünde şu bilgiler yer alır:



\- Status (Durum)

\- Species (Tür)

\- Gender (Cinsiyet)

\- Type (Alt Tür)

\- Origin (Köken)

\- Location (Konum)



\---



\##  Öne Çıkan Teknik Konular



Bu projede özellikle aşağıdaki konseptler üzerinde çalışılmıştır:



\- REST API entegrasyonu ve veri alışverişi

\- Kotlin Coroutines ile asenkron ağ işlemleri

\- MVVM mimarisi ve katmanlar arası veri akışı

\- State-driven UI tasarımı

\- Jetpack Compose ve recomposition mekanizması

\- `remember` / `rememberSaveable` ile state saklama

\- `LaunchedEffect` ile side-effect yönetimi

\- `LazyColumn` ile performanslı listeleme

\- Coil ile uzaktan görsel (remote image) yükleme

\- Compose animasyonları ve `graphicsLayer` ile 3D dönüşümler



\---



\## Proje Yapısı



```

com.example.rickandmortyapp

│

├── api

│   ├── CharacterApi.kt

│   └── RetrofitInstance.kt

│

├── model

│   ├── Character.kt

│   └── CharacterResponse.kt

│

├── ui

│   └── CharacterListScreen.kt

│

├── viewmodel

│   └── CharacterViewModel.kt

│

└── MainActivity.kt

```



\---



\##  Projeyi Çalıştırma



\### Gereksinimler



\- Android Studio (güncel sürüm önerilir)

\- JDK 11 veya üzeri

\- Android SDK

\- İnternet bağlantısı (API istekleri için)



\### Kurulum



1\. Repository'yi klonlayın:

&#x20;  ```sh

&#x20;  git clone https://github.com/Zeynep-Bayram/RickAndMortyApp.git

&#x20;  ```

2\. Projeyi Android Studio ile açın.

3\. Gradle senkronizasyonunun tamamlanmasını bekleyin.

4\. Uygulamayı bir emülatör veya fiziksel Android cihaz üzerinde çalıştırın.



> Uygulamanın karakter verilerini API üzerinden alabilmesi için cihazın internete bağlı olması gerekir.



\---



<p align="center">Made with ❤️ for the Android Community</p>

