# Rick and Morty App

**Jetpack Compose** ve **Kotlin** ile geliştirilmiş, Rick and Morty API üzerinden karakter verilerini listeleyen, arayan ve interaktif 3D flip kart animasyonuyla karakter detaylarını gösteren bir Android uygulamasıdır.

Uygulama; REST API üzerinden asenkron veri çekme, MVVM mimarisi, state-driven UI tasarımı ve Jetpack Compose animasyonları gibi modern Android geliştirme pratiklerini bir araya getirir.

<p align="center">
  <a href="#özellikler">Özellikler</a> •
  <a href="#ekran-görüntüleri">Ekran Görüntüleri</a> •
  <a href="#kullanılan-teknolojiler">Teknolojiler</a> •
  <a href="#mimari">Mimari</a> •
  <a href="#öne-çıkan-teknik-konular">Teknik Konular</a> •
  <a href="#proje-yapısı">Proje Yapısı</a> •
  <a href="#projeyi-çalıştırma">Çalıştırma</a>
</p>

---

## Özellikler

- **Karakter Listeleme** — Rick and Morty API üzerinden karakter verilerinin çekilip listelenmesi.
- **Karakter Arama** — Girilen isme göre API üzerinden karakter araması yapılabilmesi.
- **3D Flip Kart Animasyonu** — Karakter kartına dokunulduğunda kartın 180° dönerek arka yüzündeki detayları göstermesi.
- **Detaylı Karakter Bilgisi** — Karakterin durumu, türü, cinsiyeti, kökeni ve mevcut konumu gibi bilgilerin görüntülenmesi.
- **State Yönetimi** — Loading, Success ve Error durumlarının ayrı ayrı ve tutarlı biçimde yönetilmesi.
- **Hata Yönetimi ve Tekrar Deneme** — Ağ veya API hatalarında kullanıcıya hata mesajı ve tekrar deneme seçeneği sunulması.
- **Uzaktan Görsel Yükleme** — Karakter görsellerinin Coil kullanılarak API üzerinden yüklenmesi.

---

## Ekran Görüntüleri

### Ana Ekran

Uygulamanın açılış ekranında karakterler API üzerinden alınarak listelenir.

<p align="center">
  <img src="screenshots/homepage.png" width="300">
</p>

### Karakter Arama

Arama alanına karakter adı girilerek API üzerinden arama yapılabilir.

<p align="center">
  <img src="screenshots/search.png" width="300">
</p>

### Arama Sonuçları

Girilen arama sorgusuna karşılık gelen karakterler listelenir.

<p align="center">
  <img src="screenshots/searchresult.png" width="300">
</p>

### Kart Detayı

Karakter kartına dokunulduğunda 3D flip animasyonu ile kartın arka yüzü görüntülenir. Arka yüzde karakterin detay bilgileri yer alır.

<p align="center">
  <img src="screenshots/details.png" width="300">
</p>

---

## Kullanılan Teknolojiler

| Katman | Teknoloji | Açıklama |
|---|---|---|
| Dil | **Kotlin** | Android uygulama geliştirme dili |
| UI | **Jetpack Compose** | Deklaratif ve modern Android UI toolkit |
| UI Bileşenleri | **Material 3** | Modern Android tasarım bileşenleri |
| Asenkronluk | **Kotlin Coroutines** | Asenkron ve non-blocking işlemler |
| Ağ Katmanı | **Retrofit** | REST API iletişimi için type-safe HTTP istemcisi |
| Serileştirme | **Gson** | JSON verilerinin Kotlin modellerine dönüştürülmesi |
| Görsel Yükleme | **Coil** | Uzaktan görsellerin Compose içerisinde yüklenmesi |
| State Yönetimi | **ViewModel** | UI state ve veri akışının yönetilmesi |
| Mimari | **MVVM** | Model-View-ViewModel mimari yaklaşımı |

---

## Mimari

Proje **MVVM (Model-View-ViewModel)** mimari desenini takip eder.

Bu yapı sayesinde API iletişimi, veri modelleri, UI state yönetimi ve kullanıcı arayüzü sorumlulukları birbirinden ayrılmıştır.

### Veri Akışı

```text
Rick and Morty API
        ↓
     Retrofit
        ↓
   CharacterApi
        ↓
 CharacterViewModel
        ↓
 CharacterUiState
        ↓
 CharacterListScreen
   (Jetpack Compose)
