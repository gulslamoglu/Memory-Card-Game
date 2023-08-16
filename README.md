# Harry Potter Temalı Hafıza Kartı Oyunu

![Ekran görüntüsü](https://github.com/gulslamoglu/Memory-Card-Game/blob/master/memory%20fame%202.png)

Bu proje, Harry Potter evreninde geçen eğlenceli bir hafıza kartı oyununu Android platformunda uygulayan bir mobil uygulamadır.

## Özellikler

- Kolay, Orta ve Zor olmak üzere üç farklı zorluk seviyesi seçeneği.
- Özelleştirilebilir oyun alanı boyutları ile kendi oyun tahtanızı oluşturma seçeneği.
- Kartları çevirirken ses efektleri.
- Firebase Authentication ile kullanıcı girişi.
- Başarı ekranında tebrik ses efekti.

## Nasıl Çalışır?

1. Oyun başladığında, kullanıcıya zorluk seviyesi seçenekleri sunulur.
2. Kullanıcı, istediği zorluk seviyesini seçebilir veya kendi oyun tahtasını oluşturabilir.
3. Oyun tahtası oluşturulduktan sonra, kartlar karıştırılır ve kapalı olarak görüntülenir.
4. Kullanıcı kartlara tıklayarak onları çevirir ve eşleşen kartları bulmaya çalışır.
5. Tüm kartlar eşleştirildiğinde veya zaman dolduğunda, oyun sona erer.
6. Kullanıcı, oyun sonu ekranında başarısını görür ve istediği zaman yeni bir oyun başlatabilir.

## Kurulum

1. Bu projeyi klonlayın veya indirin.
2. Android Studio'da projeyi açın.
3. Firebase Console'da yeni bir proje oluşturun ve projenin google-services.json dosyasını projenizin "app" klasörüne ekleyin.
4. Uygulamayı cihazınıza veya emülatörünüze yükleyin ve çalıştırın.

## Kullanılan Teknolojiler ve Kaynaklar

- Kotlin programlama dili
- Android Studio
- Firebase Authentication
- RecyclerView ve ConstraintLayout
- Ses efektleri: "prologue.mp3", "congratulations.mp3", "wheels.mp3", "finish.mp3"

## Katkıda Bulunma

Bu proje açık kaynaklıdır ve katkıda bulunmaktan mutluluk duyarız. Eğer projeye katkı sağlamak isterseniz:

1. Bu projeyi forklayın.
2. Yeni bir dal (branch) oluşturun: `git checkout -b yeni-ozellik`
3. Yaptığınız değişiklikleri commit'leyin: `git commit -m 'Yeni özellik: ...'`
4. Değişikliklerinizi uzak sunucuya gönderin: `git push origin yeni-ozellik`
5. Bir Pull Talebi (Pull Request) oluşturun.

## Lisans

Bu proje [MIT Lisansı](/LICENSE) altında lisanslanmıştır.

---

Bu README dosyası, projenizi anlamak ve kullanmak isteyenlere rehberlik etmek için tasarlanmıştır. Herhangi bir sorunuz veya geri bildiriminiz varsa, lütfen iletişime geçmekten çekinmeyin.

