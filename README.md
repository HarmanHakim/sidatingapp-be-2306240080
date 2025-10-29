*Pertanyaan 1*
*Apa itu Test Plan dan Thread Group pada JMeter?*

*Jawab*
Test Plan dapat dianggap sebagai skrip induk atau wadah utama yang menampung keseluruhan rancangan skenario pengujian Anda, termasuk semua langkah, variabel, dan logika kontrol alur. Di dalam Test Plan tersebut, Thread Group adalah komponen fundamental yang secara spesifik mengatur simulasi beban pengguna. Thread Group inilah yang menentukan konfigurasi inti seperti berapa banyak pengguna virtual (threads) yang akan digunakan, berapa lama waktu yang dibutuhkan untuk mengaktifkan semua pengguna tersebut (ramp-up period), dan berapa kali setiap pengguna akan mengulangi skenario pengujian (loop count). Jadi, secara sederhana, Test Plan adalah "apa" yang akan diuji, sedangkan Thread Group adalah "bagaimana" pengujian itu akan disimulasikan dengan beban pengguna untuk meniru kondisi nyata.

*Resource*: https://jmeter.apache.org/usermanual/test_plan.html 



*Pertanyaan 2*
*Pada saat membuat HTTP Header Manager, Anda menambahkan header dengan name “content-type” dan value “application/json”. Mengapa Anda melakukan langkah tersebut?*

*Jawab*
Penggunaan header content-type yang di-set ke application/json pada HTTP Header Manager sebetulnya bertujuan untuk memberi tahu server bahwa data yang kita kirimkan di dalam body request tersebut adalah data berformat JSON. Langkah ini sangat penting, terutama dalam konteks API modern yang sering menggunakan request POST atau PUT untuk mengirim payload data yang terstruktur. Server akan mengandalkan header ini sebagai instruksi untuk dapat menginterpretasikan atau me-parsing body request dengan benar. Kalau header ini tidak dicantumkan, server bisa saja salah mengartikan format data yang dikirim, misalnya menganggapnya sebagai plain text, yang pada akhirnya bisa menyebabkan error saat pemrosesan data atau membuat request kita ditolak.

*Resource*: https://www.geeksforgeeks.org/software-testing/how-to-use-jmeters-http-header-manager/



*Pertanyaan 3*
*Apa itu JSON Extractor? Sebutkan semua kegunaannya di Test Plan ini!*

*Jawab*
JSON Extractor adalah sebuah post-processor di JMeter, yang artinya komponen ini baru akan berjalan setelah sebuah request menerima respons. Fungsi utamanya adalah untuk "membaca" respons yang berformat JSON dan mengambil atau mengekstrak data spesifik dari dalamnya. Untuk menemukan data yang tepat, extractor ini menggunakan ekspresi yang disebut JSON Path, yang cara kerjanya mirip seperti penunjuk alamat untuk menemukan nilai tertentu di dalam struktur data JSON.

Dalam Test Plan ini, kita menggunakannya untuk mengambil nilai-nilai penting dari respons server—contoh paling umum adalah mengambil token autentikasi setelah berhasil login atau mendapatkan ID pengguna. Nilai yang sudah diekstrak ini kemudian disimpan ke dalam sebuah variabel JMeter. Variabel inilah yang akan kita pakai ulang di request-request selanjutnya, misalnya untuk dimasukkan ke header otorisasi. Proses menyambungkan request seperti ini (dynamic chaining) sangat penting agar skenario pengujian kita bisa berjalan otomatis dan realistis. Selain itu, JSON Extractor juga bisa dipakai untuk validasi, di mana kita mengekstrak data hanya untuk memastikan nilainya sudah sesuai dengan yang kita harapkan.



*Pertanyaan 4*
*Apa itu Assertions dalam JMeter? Sebutkan contoh 3 Assertions dan kegunaannya!*

*Jawab*
Di JMeter, Assertions pada dasarnya adalah aturan pengecekan atau "titik validasi" yang kita tambahkan ke request kita. Fungsi utamanya adalah untuk secara otomatis memverifikasi apakah respons yang kita terima dari server sudah sesuai dengan yang kita harapkan. Dengan kata lain, assertion inilah yang membandingkan hasil aktual dengan hasil ekspektasi kita untuk menentukan apakah sebuah request bisa dianggap "lulus" (Pass) atau "gagal" (Fail).
    Response Assertion Ini adalah assertion yang paling umum, digunakan untuk memeriksa konten dari respons. Kita bisa menggunakannya untuk memastikan sebuah teks atau pola tertentu ada (atau tidak ada) di dalam body respons, header, atau response code. Contohnya, kita bisa menambahkan ini untuk mengecek apakah response code adalah "200" atau apakah pesan "Login berhasil" muncul setelah login.

    Duration Assertion Assertion ini fokus pada performa. Kita menggunakannya untuk memastikan bahwa server memberikan respons dalam batas waktu yang kita tentukan. Misalnya, kita bisa menetapkan bahwa request harus selesai dalam waktu kurang dari 1000 milidetik (1 detik). Jika responsnya lebih lama dari itu, assertion ini akan gagal.

    Size Assertion Assertion ini digunakan untuk memvalidasi ukuran dari data respons (dalam byte). Kita bisa mengaturnya untuk mengecek apakah ukuran respons sama persis dengan, lebih besar dari, atau lebih kecil dari nilai yang kita harapkan. Ini berguna, misalnya, untuk memastikan bahwa respons yang kembali tidak kosong atau ukurannya tidak membengkak secara tidak wajar.


*Resource*: https://www.geeksforgeeks.org/java/assertions-in-jmeter/



*Pertanyaan 5*
*Apa itu Number of Threads dan Ramp-up Period? Apa hubungan antar keduanya?*

*Jawab*
Number of Threads (Jumlah Thread) pada dasarnya adalah jumlah total pengguna virtual yang ingin kita simulasikan. Sederhananya, ini adalah "berapa banyak" pengguna yang akan kita gunakan untuk menguji server.

Di sisi lain, Ramp-up Period adalah "berapa lama" waktu yang kita berikan kepada JMeter untuk mengaktifkan semua thread tersebut. Ini adalah durasi yang dibutuhkan untuk beralih dari 0 pengguna aktif hingga semua pengguna yang kita tentukan tadi benar-benar berjalan.

Hubungan keduanya penting dalam menentukan pola beban (load pattern) pengujian. Jika kita mengatur Ramp-up Period terlalu singkat (misalnya, 100 thread dalam 1 detik), maka ke-100 pengguna virtual itu akan mencoba mengakses server secara bersamaan. Ini akan menciptakan lonjakan beban (load spike) yang sangat tinggi dan mendadak, yang bisa langsung menyebabkan bottleneck dan tidak realistis.

Sebaliknya, jika kita mengatur Ramp-up Period yang lebih terencana (misalnya, 100 thread dalam 100 detik), JMeter akan mengaktifkan pengguna secara bertahap (rata-rata 1 thread baru setiap detiknya). Ini memberikan beban yang lebih merata dan jauh lebih realistis, mensimulasikan kondisi di mana pengguna datang ke aplikasi kita secara berangsur-angsur.

*Resource*: https://www.geeksforgeeks.org/software-testing/thread-group-in-jmeter/



*Pertanyaan 6*
*Gunakan angka 1000 untuk Number of Threads dan 100 untuk Ramp-up Period. Jalankan Test Plan dengan konfigurasi tersebut. Kemudian, perhatikan Summary Report, View Result Tree, Graph Result, dan Assertion Result. Buatlah penjelasan minimal 2 paragraf untuk menjelaskan temuan menarik Anda terhadap hasil-hasil tersebut. Sertakan screenshot dari keempat result tersebut. Sertakan juga info mengenai prosesor, RAM, dan penggunaan hardisk HDD atau SSD dari perangkat Anda. (Jika perangkat Anda tidak kuat dengan angka konfigurasi tersebut, silakan turunkan angkanya).*
![alt text](<Screenshot 2025-10-29 205454.png>)
![alt text](<Screenshot 2025-10-29 205501.png>)
![alt text](<Screenshot 2025-10-29 205505.png>)
![alt text](<Screenshot 2025-10-29 205515.png>)
*Jawab*
Temuan paling menarik dari hasil pengujian ini adalah tingginya tingkat error dan degradasi performa yang signifikan di bawah beban. Summary Report menunjukkan bahwa dari total 2500 sample yang dikirim, terdapat Error % keseluruhan sebesar 11.48%. Angka ini sebagian besar disumbang oleh dua sampler spesifik: "Random Request" yang gagal lebih dari separuh waktu (51.80% error) dan "Filter Posts by User Id" (5.60% error). View Results Tree mengonfirmasi hal ini secara visual, di mana banyak request ditandai dengan ikon 'X' merah, yang menandakan kegagalan request atau assertion. Kegagalan pada "Random Request" bisa jadi mengindikasikan endpoint yang tidak stabil atau mungkin masalah pada data dinamis yang digunakan.

Temuan penting kedua terlihat jelas pada Graph Results, yang menunjukkan bahwa performa aplikasi menurun seiring berjalannya waktu pengujian. Garis biru (Average) dan garis hijau (Deviation) terlihat menanjak secara drastis. Ini mengindikasikan bahwa seiring makin banyaknya thread (pengguna virtual) yang aktif selama periode ramp-up, waktu respons rata-rata menjadi semakin lambat dan semakin tidak konsisten (deviasi tinggi). Summary Report merinci ini lebih lanjut: sampler "Filter Posts by User Id" adalah bottleneck utama, dengan waktu rata-rata sangat tinggi (5003 md) dan waktu maksimum mencapai 63.122 md (lebih dari 1 menit). Ini sangat kontras dengan sampler "Create Post" (rata-rata 665 md) dan menunjukkan bahwa query atau proses untuk memfilter data kemungkinan besar sangat tidak efisien dan perlu optimasi segera.

Prosesor: 12th Gen Intel(R) Core(TM) i7-12700H (2.30 GHz)
RAM: 16 GB
Penyimpanan:943 GB


*Pertanyaan 8*
*Sembari menjalankan Test Plan pada nomor 6, perhatikan pergerakan grafik pada JConsole. Buatlah penjelasan minimal 2 paragraf untuk menjelaskan temuan menarik kalian terhadap hasil-hasil tersebut. Sertakan screenshot dari grafik-grafik tersebut.*

*Jawab*
![alt text](<Screenshot 2025-10-29 205211.png>)
Yang pertama adalah tepat saat pengujian dimulai, grafik Live threads melonjak secara instan dan kemudian stabil di angka 71. Ini menunjukkan bahwa server aplikasi (kemungkinan Tomcat atau server serupa) langsung membuat worker thread baru untuk menangani setiap permintaan dari pengguna virtual yang disimulasikan oleh JMeter. Pada saat yang sama, grafik CPU Usage juga melonjak dari 0.1% hingga puncaknya di atas 3%, yang membuktikan bahwa prosesor mulai bekerja aktif untuk memproses permintaan-permintaan tersebut.

Temuan menarik kedua terlihat pada grafik Heap Memory Usage. Selama pengujian berlangsung (dari ~20:49:30 hingga ~20:51:30), penggunaan memori tidak naik secara linear, melainkan menunjukkan pola "gigi gergaji" (sawtooth pattern) yang sangat jelas. Penggunaan memori (garis biru "Used") akan naik hingga mendekati 100 MB, kemudian tiba-tiba anjlok kembali ke sekitar 50 MB, dan proses ini berulang-ulang. Pola ini adalah visualisasi dari Garbage Collector (GC) Java yang sedang bekerja. Saat request dari JMeter masuk, objek-objek baru dibuat dan mengisi heap. Ketika heap mulai penuh, GC akan berjalan untuk membersihkan objek-objek yang sudah tidak terpakai (misalnya, request yang sudah selesai diproses), sehingga memori kembali tersedia. Ini adalah perilaku yang normal dan sehat, menunjukkan bahwa aplikasi secara aktif mengelola memorinya di bawah beban.


*Pertanyaan 9*
*Apa itu performance testing? Buatlah kesimpulan dari pengerjaan tutorial bagian JMeter & JConsole ini.*

*Jawab*
Performance Testing (Uji Performa) adalah sebuah metode pengujian software yang dirancang untuk mengevaluasi bagaimana kinerja sebuah sistem ketika dihadapkan pada beban kerja tertentu. Fokus utamanya adalah untuk melihat aspek-aspek seperti kecepatan (seberapa cepat sistem merespons), stabilitas (apakah sistem tetap berjalan normal tanpa crash), dan skalabilitas (seberapa baik sistem menangani peningkatan beban). Tujuan akhirnya adalah untuk menemukan dan memperbaiki titik kelemahan (bottleneck), mengukur kapasitas maksimal sistem, dan memastikan pengguna tetap mendapatkan pengalaman yang optimal saat aplikasi digunakan.

Adapun kesimpulan dari pengerjaan tutorial ini, kita bisa melihat bahwa JMeter dan JConsole memiliki peran yang saling melengkapi. JMeter bertugas sebagai alat untuk memberi beban (simulasi pengguna), di mana kita merancang skenario pengujiannya—mulai dari mengatur Thread Group, konfigurasi header, hingga mengekstrak data respons JSON. Sementara itu, JConsole bertindak sebagai alat pemantau (monitoring) sisi server. Saat JMeter menjalankan pengujian, JConsole memberikan kita data real-time mengenai dampaknya terhadap aplikasi, seperti penggunaan CPU, pemakaian heap memory, dan aktivitas thread.

*Resource*: https://www.geeksforgeeks.org/software-testing/performance-testing-software-testing/ 



*Pertanyaan 10*
*Bagaimana cara meningkatkan performa hasil performance testing pada aplikasi? Sebutkan minimal tiga strategi optimasi yang dapat dilakukan.*

*Jawab*
Untuk meningkatkan hasil dari performance testing, kita perlu melakukan optimasi pada berbagai lapisan aplikasi. Saat hasil pengujian menunjukkan adanya bottleneck atau waktu respons yang lambat, strategi perbaikannya:

    Optimasi Database Seringkali database menjadi biang keladi kelambatan. Cara paling umum untuk memperbaikinya adalah dengan menambahkan indeks pada kolom-kolom tabel yang sering digunakan untuk pencarian (misalnya, yang ada di klausa WHERE). Indeks bekerja seperti daftar isi di buku, mempercepat pencarian data. Selain itu, penting juga untuk meninjau dan menyederhanakan query yang kompleks (misalnya yang menggunakan banyak JOIN) agar tidak terlalu membebani database.

    Implementasi Caching dan CDN Strategi ini bertujuan mengurangi jumlah permintaan ke server utama atau database. Caching (baik di level aplikasi atau server) menyimpan salinan data yang sering diakses di memori sementara. Dengan begitu, saat ada permintaan untuk data yang sama, aplikasi bisa mengambilnya dari cache yang super cepat, tanpa perlu bolak-balik ke database. Sementara itu, CDN (Content Delivery Network) adalah cache khusus untuk aset statis (seperti gambar, file CSS, atau JavaScript) yang mendistribusikan file-file tersebut ke server di berbagai lokasi geografis, sehingga aset bisa dimuat lebih cepat oleh pengguna di mana pun mereka berada.

    Optimasi Kode dan Pemrosesan Asinkron Perbaikan ini menyentuh langsung logika di dalam aplikasi. Ini bisa berarti memperbaiki algoritma agar berjalan lebih efisien atau menggunakan parallel processing. Selain itu, menerapkan pemrosesan asinkron (asynchronous) sangat membantu. Untuk tugas-tugas yang memakan waktu (seperti mengirim email notifikasi atau memproses gambar), kita bisa menjalankannya di "latar belakang" (background). Dengan cara ini, aplikasi tidak perlu "menunggu" tugas itu selesai dan bisa langsung memberi respons ke pengguna, sehingga throughput dan responsivitas aplikasi secara keseluruhan meningkat.

*Resource*: https://dev.to/adityabhuyan/optimizing-application-performance-tools-techniques-and-best-practices-45nm 