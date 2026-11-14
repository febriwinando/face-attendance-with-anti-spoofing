package go.pemkott.appsandroidmobiletebingtinggi.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MotivationalQuotesLibrary {

    public static class Quote {
        private final int id;
        private final String quote;
        private final String author;

        public Quote(int id, String quote, String author) {
            this.id = id;
            this.quote = quote;
            this.author = author;
        }

        public int getId() { return id; }
        public String getQuote() { return quote; }
        public String getAuthor() { return author; }
    }

    private static final String DEFAULT_AUTHOR = "AKU";
    private static final List<Quote> QUOTES = new ArrayList<>();

    static {
        QUOTES.add(new Quote(1, "Cara terbaik untuk memulai adalah dengan berhenti berbicara dan mulai melakukan.", "Walt Disney"));
        QUOTES.add(new Quote(2, "Orang pesimis melihat kesulitan di setiap kesempatan. Orang optimis melihat kesempatan dalam setiap kesulitan.", "Winston Churchill"));
        QUOTES.add(new Quote(3, "Jangan biarkan hari kemarin menyita terlalu banyak hari ini.", "Will Rogers"));
        QUOTES.add(new Quote(4, "Kita belajar dari kegagalan jauh lebih banyak daripada dari kesuksesan. Jangan biarkan kegagalan menghentikanmu.", "Unknown"));
        QUOTES.add(new Quote(5, "Bukan tentang apakah kamu jatuh, tapi apakah kamu akan bangkit kembali.", "Vince Lombardi"));
        QUOTES.add(new Quote(6, "Jika kamu bekerja pada sesuatu yang sangat kamu pedulikan, kamu tidak perlu didorong. Visi itu sendiri yang menarikmu.", "Steve Jobs"));
        QUOTES.add(new Quote(7, "Orang yang cukup gila untuk berpikir bahwa mereka dapat mengubah dunia adalah orang-orang yang benar-benar melakukannya.", "Steve Jobs"));
        QUOTES.add(new Quote(8, "Kegagalan tidak pernah menyusulku jika tekadku untuk sukses cukup kuat.", "Og Mandino"));
        QUOTES.add(new Quote(9, "Para pengusaha pandai menangani ketidakpastian dan juga sangat baik dalam meminimalkan risiko. Itulah pengusaha sejati.", "Mohnish Pabrai"));
        QUOTES.add(new Quote(10, "Kita mungkin menghadapi banyak kekalahan, tetapi kita tidak boleh dikalahkan.", "Maya Angelou"));
        QUOTES.add(new Quote(11, "Pengetahuan adalah kekuatan, tetapi informasi tanpa tindakan adalah tidak berguna.", "B.J. Habibie"));
        QUOTES.add(new Quote(12, "Keberhasilan itu bukan milik orang pintar, melainkan milik orang yang selalu berusaha.", "B.J. Habibie"));
        QUOTES.add(new Quote(13, "Gantungkan cita-citamu setinggi langit! Jika engkau jatuh, engkau akan jatuh di antara bintang-bintang.", "Ir. Soekarno"));
        QUOTES.add(new Quote(14, "Beri aku 1.000 orang tua, niscaya akan kucabut Semeru dari akarnya. Beri aku 10 pemuda niscaya akan kuguncangkan dunia.", "Ir. Soekarno"));
        QUOTES.add(new Quote(15, "Pendidikan adalah senjata paling mematikan di dunia, karena dengan pendidikan Anda dapat mengubah dunia.", "Nelson Mandela"));
        QUOTES.add(new Quote(16, "Kelihatannya selalu tidak mungkin sampai sesuatu itu selesai dilakukan.", "Nelson Mandela"));
        QUOTES.add(new Quote(17, "Satu-satunya cara untuk melakukan pekerjaan besar adalah dengan mencintai apa yang Anda lakukan.", "Steve Jobs"));
        QUOTES.add(new Quote(18, "Waktu Anda terbatas, jadi jangan buang-buang waktu dengan menjalani hidup orang lain.", "Steve Jobs"));
        QUOTES.add(new Quote(19, "Imajinasi lebih penting daripada pengetahuan. Pengetahuan terbatas, sedangkan imajinasi mencakup seluruh dunia.", "Albert Einstein"));
        QUOTES.add(new Quote(20, "Seseorang yang tidak pernah membuat kesalahan tidak pernah mencoba sesuatu yang baru.", "Albert Einstein"));
        QUOTES.add(new Quote(21, "Usaha dan keberanian tidak cukup tanpa adanya tujuan dan arah yang jelas.", "John F. Kennedy"));
        QUOTES.add(new Quote(22, "Jangan tanyakan apa yang negara dapat lakukan untukmu, tanyakan apa yang dapat kamu lakukan untuk negara.", "John F. Kennedy"));
        QUOTES.add(new Quote(23, "Jika Anda menginginkan sesuatu yang belum pernah Anda miliki, Anda harus bersedia melakukan sesuatu yang belum pernah Anda lakukan.", "Thomas Jefferson"));
        QUOTES.add(new Quote(24, "Sukses bukanlah kunci kebahagiaan. Kebahagiaan adalah kunci kesuksesan. Jika Anda mencintai apa yang Anda lakukan, Anda akan sukses.", "Albert Schweitzer"));
        QUOTES.add(new Quote(25, "Hanya mereka yang berani gagal parah yang bisa meraih keberhasilan besar.", "Robert F. Kennedy"));
        QUOTES.add(new Quote(26, "Jatuh tujuh kali, berdiri delapan kali.", "Pepatah Jepang"));
        QUOTES.add(new Quote(27, "Masa depan milik mereka yang percaya pada keindahan mimpi-mimpi mereka.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(28, "Lakukan satu hal setiap hari yang membuatmu takut.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(29, "Satu-satunya hal yang harus kita takuti adalah rasa takut itu sendiri.", "Franklin D. Roosevelt"));
        QUOTES.add(new Quote(30, "Masa depan tergantung pada apa yang Anda lakukan hari ini.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(31, "Hiduplah seolah-olah kamu akan mati besok. Belajarlah seolah-olah kamu akan hidup selamanya.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(32, "Jadilah perubahan yang ingin Anda lihat di dunia.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(33, "Pena lebih tajam daripada pedang.", "Edward Bulwer-Lytton"));
        QUOTES.add(new Quote(34, "Kelemahan terbesar kita terletak pada menyerah. Cara paling pasti untuk sukses adalah selalu mencoba sekali lagi.", "Thomas A. Edison"));
        QUOTES.add(new Quote(35, "Saya tidak gagal. Saya hanya menemukan 10.000 cara yang tidak berhasil.", "Thomas A. Edison"));
        QUOTES.add(new Quote(36, "Genius adalah satu persen inspirasi dan sembilan puluh sembilan persen keringat.", "Thomas A. Edison"));
        QUOTES.add(new Quote(37, "Kesuksesan adalah kemampuan untuk berpindah dari satu kegagalan ke kegagalan lain tanpa kehilangan antusiasme.", "Winston Churchill"));
        QUOTES.add(new Quote(38, "Jika Anda melewati neraka, teruslah berjalan.", "Winston Churchill"));
        QUOTES.add(new Quote(39, "Dua hari terpenting dalam hidup Anda adalah hari Anda lahir dan hari Anda mengetahui alasannya.", "Mark Twain"));
        QUOTES.add(new Quote(40, "Rahasia untuk maju adalah memulai.", "Mark Twain"));
        QUOTES.add(new Quote(41, "Kebaikan adalah bahasa yang bisa didengar oleh orang tuli dan dilihat oleh orang buta.", "Mark Twain"));
        QUOTES.add(new Quote(42, "Bukan ukuran anjing dalam pertarungan, melainkan ukuran pertarungan dalam diri anjing.", "Mark Twain"));
        QUOTES.add(new Quote(43, "Tempat terbaik untuk berhasil adalah tempat Anda berada saat ini dengan apa yang Anda miliki.", "Charles Schwab"));
        QUOTES.add(new Quote(44, "Jika Anda melakukan apa yang selalu Anda lakukan, Anda akan mendapatkan apa yang selalu Anda dapatkan.", "Henry Ford"));
        QUOTES.add(new Quote(45, "Apakah Anda berpikir Anda bisa atau tidak bisa, Anda benar.", "Henry Ford"));
        QUOTES.add(new Quote(46, "Kegagalan hanyalah kesempatan untuk memulai lagi, kali ini dengan lebih cerdas.", "Henry Ford"));
        QUOTES.add(new Quote(47, "Kebersamaan adalah permulaan; menjaga kebersamaan adalah kemajuan; bekerja sama adalah keberhasilan.", "Henry Ford"));
        QUOTES.add(new Quote(48, "Teman terbaik adalah orang yang mengeluarkan yang terbaik dalam diri saya.", "Henry Ford"));
        QUOTES.add(new Quote(49, "Jangan takut untuk melepaskan yang baik demi yang terbaik.", "John D. Rockefeller"));
        QUOTES.add(new Quote(50, "Cobalah untuk tidak menjadi orang yang sukses, melainkan orang yang bernilai.", "Albert Einstein"));
        QUOTES.add(new Quote(51, "Di tengah kesulitan terletak kesempatan.", "Albert Einstein"));
        QUOTES.add(new Quote(52, "Hidup itu seperti mengendarai sepeda. Untuk menjaga keseimbangan, Anda harus terus bergerak.", "Albert Einstein"));
        QUOTES.add(new Quote(53, "Lakukan yang terbaik yang kamu bisa sampai kamu tahu caranya. Lalu ketika kamu tahu caranya, lakukan dengan lebih baik.", "Maya Angelou"));
        QUOTES.add(new Quote(54, "Orang akan melupakan apa yang kamu katakan dan lakukan, tapi tidak akan melupakan bagaimana kamu membuat mereka merasa.", "Maya Angelou"));
        QUOTES.add(new Quote(55, "Jika kamu tidak menyukai sesuatu, ubahlah. Jika kamu tidak bisa mengubahnya, ubah sikapmu.", "Maya Angelou"));
        QUOTES.add(new Quote(56, "Pendidikan bukanlah persiapan untuk hidup; pendidikan adalah hidup itu sendiri.", "John Dewey"));
        QUOTES.add(new Quote(57, "Bukan bahaya itu sendiri yang menakutkan, melainkan rasa takut akan bahaya tersebut.", "Seneca"));
        QUOTES.add(new Quote(58, "Kita lebih sering menderita dalam imajinasi daripada dalam kenyataan.", "Seneca"));
        QUOTES.add(new Quote(59, "Setiap kesulitan dalam hidup adalah kesempatan untuk melatih karakter.", "Marcus Aurelius"));
        QUOTES.add(new Quote(60, "Kamu memiliki kekuasaan atas pikiranmu, bukan kejadian di luar. Sadarilah ini, dan kamu akan menemukan kekuatan.", "Marcus Aurelius"));
        QUOTES.add(new Quote(61, "Kebahagiaan hidupmu tergantung pada kualitas pikiranmu.", "Marcus Aurelius"));
        QUOTES.add(new Quote(62, "Pembalasan terbaik adalah tidak menjadi seperti musuhmu.", "Marcus Aurelius"));
        QUOTES.add(new Quote(63, "Bukan apa yang terjadi padamu, tapi bagaimana kamu bereaksi terhadapnya yang penting.", "Epictetus"));
        QUOTES.add(new Quote(64, "Seseorang tidak bisa belajar apa yang dia pikir sudah dia ketahui.", "Epictetus"));
        QUOTES.add(new Quote(65, "Pikiran yang tenang membawa kekuatan batin dan kepercayaan diri.", "Dalai Lama"));
        QUOTES.add(new Quote(66, "Tujuan utama kita dalam hidup ini adalah untuk membantu orang lain. Dan jika kamu tidak bisa membantu mereka, setidaknya jangan sakiti mereka.", "Dalai Lama"));
        QUOTES.add(new Quote(67, "Ingatlah bahwa tidak mendapatkan apa yang kamu inginkan terkadang merupakan keberuntungan yang luar biasa.", "Dalai Lama"));
        QUOTES.add(new Quote(68, "Bila ingin cepat, jalanlah sendiri. Bila ingin jauh, jalanlah bersama-sama.", "Pepatah Afrika"));
        QUOTES.add(new Quote(69, "Pohon yang berakar kuat tidak pernah takut pada badai.", "Pepatah Cina"));
        QUOTES.add(new Quote(70, "Perjalanan ribuan mil dimulai dengan satu langkah.", "Lao Tzu"));
        QUOTES.add(new Quote(71, "Ketika saya membiarkan diri saya menjadi apa adanya, saya menjadi apa yang mungkin bagi saya.", "Lao Tzu"));
        QUOTES.add(new Quote(72, "Menguasai orang lain adalah kekuatan. Menguasai diri sendiri adalah keutamaan sejati.", "Lao Tzu"));
        QUOTES.add(new Quote(73, "Pikirkan tentang keindahan yang masih tersisa di sekitarmu dan berbahagialah.", "Anne Frank"));
        QUOTES.add(new Quote(74, "Betapa indahnya bahwa tidak ada seorang pun yang perlu menunggu satu momen pun sebelum mulai memperbaiki dunia.", "Anne Frank"));
        QUOTES.add(new Quote(75, "Hanya orang-orang yang tidak mau bekerja keras yang mencari jalan pintas.", "Buya Hamka"));
        QUOTES.add(new Quote(76, "Kecantikan yang abadi terletak pada eloknya akhlak dan tingginya ilmu seseorang, bukan pada wajah dan pakaiannya.", "Buya Hamka"));
        QUOTES.add(new Quote(77, "Jangan takut jatuh, karena yang tidak pernah memanjatlah yang tidak pernah jatuh.", "Buya Hamka"));
        QUOTES.add(new Quote(78, "Tugas kita bukanlah untuk berhasil. Tugas kita adalah untuk mencoba, karena di dalam mencoba itulah kita menemukan peluang.", "Buya Hamka"));
        QUOTES.add(new Quote(79, "Ing Ngarsa Sung Tuladha, Ing Madya Mangun Karsa, Tut Wuri Handayani.", "Ki Hajar Dewantara"));
        QUOTES.add(new Quote(80, "Setiap orang menjadi guru, setiap rumah menjadi sekolah.", "Ki Hajar Dewantara"));
        QUOTES.add(new Quote(81, "Habis Gelap Terbitlah Terang.", "R.A. Kartini"));
        QUOTES.add(new Quote(82, "Banyak hal yang bisa menjatuhkanmu, tapi satu-satunya hal yang benar-benar dapat menjatuhkanmu adalah sikapmu sendiri.", "R.A. Kartini"));
        QUOTES.add(new Quote(83, "Bangsa yang besar adalah bangsa yang menghormati jasa pahlawannya.", "Ir. Soekarno"));
        QUOTES.add(new Quote(84, "Jangan pernah melupakan sejarah (JASMERAH).", "Ir. Soekarno"));
        QUOTES.add(new Quote(85, "Kurang cerdas dapat diperbaiki dengan belajar. Kurang cakap dapat dihilangkan dengan pengalaman. Namun kejujuran tidak akan pernah ada jika tidak dibiasakan.", "Sutan Sjahrir"));
        QUOTES.add(new Quote(86, "Pahlawan yang setia itu berkorban bukan untuk dikenal namanya, tetapi semata-mata membela cita-cita.", "Mohammad Hatta"));
        QUOTES.add(new Quote(87, "Jatuh bangunnya negara ini, sangat tergantung dari bangsa ini sendiri.", "Mohammad Hatta"));
        QUOTES.add(new Quote(88, "Hanya ada satu negara yang pantas menjadi negaraku. Ia tumbuh dengan perbuatan dan perbuatan itu adalah perbuatanku.", "Mohammad Hatta"));
        QUOTES.add(new Quote(89, "Seribu orang tua bisa bermimpi, satu orang pemuda bisa mengubah dunia.", "Ir. Soekarno"));
        QUOTES.add(new Quote(90, "Hidup yang tidak dipertaruhkan tidak akan pernah dimenangkan.", "Sutan Sjahrir"));
        QUOTES.add(new Quote(91, "Cinta itu seperti angin, kamu tidak bisa melihatnya tapi kamu bisa merasakannya.", "Nicholas Sparks"));
        QUOTES.add(new Quote(92, "Hal terbaik dan paling indah di dunia tidak dapat dilihat atau disentuh, mereka harus dirasakan dengan hati.", "Helen Keller"));
        QUOTES.add(new Quote(93, "Optimisme adalah keyakinan yang mengarah pada pencapaian. Tidak ada yang bisa dilakukan tanpa harapan dan percaya diri.", "Helen Keller"));
        QUOTES.add(new Quote(94, "Meskipun dunia ini penuh dengan penderitaan, dunia ini juga penuh dengan pembuktian atas penanggulangannya.", "Helen Keller"));
        QUOTES.add(new Quote(95, "Satu-satunya hal yang lebih buruk daripada menjadi buta adalah memiliki penglihatan tetapi tidak memiliki visi.", "Helen Keller"));
        QUOTES.add(new Quote(96, "Tersenyumlah, karena senyummu adalah awal dari kebaikan.", "Bunda Teresa"));
        QUOTES.add(new Quote(97, "Jika Anda menghakimi orang lain, Anda tidak memiliki waktu untuk mencintai mereka.", "Bunda Teresa"));
        QUOTES.add(new Quote(98, "Kita sendiri merasa bahwa apa yang kita lakukan hanyalah setetes air di lautan. Tetapi lautan akan berkurang jika setetes air itu tidak ada.", "Bunda Teresa"));
        QUOTES.add(new Quote(99, "Bukan berapa banyak yang kita berikan, tetapi berapa banyak cinta yang kita berikan saat memberi.", "Bunda Teresa"));
        QUOTES.add(new Quote(100, "Petualangan terbesar yang bisa Anda jalani adalah mewujudkan hidup impian Anda.", "Oprah Winfrey"));
        QUOTES.add(new Quote(101, "Ubah luka Anda menjadi kebijaksanaan.", "Oprah Winfrey"));
        QUOTES.add(new Quote(102, "Semakin Anda memuji dan merayakan hidup Anda, semakin banyak hal dalam hidup yang dapat dirayakan.", "Oprah Winfrey"));
        QUOTES.add(new Quote(103, "Kelilingilah diri Anda hanya dengan orang-orang yang akan mengangkat Anda lebih tinggi.", "Oprah Winfrey"));
        QUOTES.add(new Quote(104, "Memahami tujuan hidup adalah langkah pertama menuju kesuksesan.", "Oprah Winfrey"));
        QUOTES.add(new Quote(105, "Keberuntungan adalah pertemuan antara persiapan dan kesempatan.", "Oprah Winfrey"));
        QUOTES.add(new Quote(106, "Pekerjaan Anda akan mengisi sebagian besar hidup Anda, dan satu-satunya cara untuk benar-benar puas adalah melakukan apa yang Anda yakini sebagai pekerjaan hebat.", "Steve Jobs"));
        QUOTES.add(new Quote(107, "Tetap lapar, tetap bodoh.", "Steve Jobs"));
        QUOTES.add(new Quote(108, "Inovasi membedakan antara seorang pemimpin dan pengikut.", "Steve Jobs"));
        QUOTES.add(new Quote(109, "Desain bukan sekadar seperti apa bentuk dan rasanya. Desain adalah tentang bagaimana ia bekerja.", "Steve Jobs"));
        QUOTES.add(new Quote(110, "Kesederhanaan adalah kecanggihan tertinggi.", "Leonardo da Vinci"));
        QUOTES.add(new Quote(111, "Belajar tidak pernah menguras pikiran.", "Leonardo da Vinci"));
        QUOTES.add(new Quote(112, "Saya terkesan dengan urgensi bertindak. Mengetahui saja tidak cukup; kita harus menerapkan. Bersedia saja tidak cukup; kita harus melakukan.", "Leonardo da Vinci"));
        QUOTES.add(new Quote(113, "Hambatan tidak bisa menghancurkanku; setiap hambatan menyerah pada tekad yang kuat.", "Leonardo da Vinci"));
        QUOTES.add(new Quote(114, "Ada tiga kelas manusia: mereka yang melihat, mereka yang melihat ketika ditunjukkan, dan mereka yang tidak melihat.", "Leonardo da Vinci"));
        QUOTES.add(new Quote(115, "Seni tidak pernah selesai, hanya ditinggalkan.", "Leonardo da Vinci"));
        QUOTES.add(new Quote(116, "Pengetahuan tanpa amal bagaikan pohon tanpa buah.", "K.H. Ahmad Dahlan"));
        QUOTES.add(new Quote(117, "Dadiyo kyai sing maju, aja kesel anggonmu berjuang.", "K.H. Ahmad Dahlan"));
        QUOTES.add(new Quote(118, "Kasih sayang pada sesama adalah puncak dari ajaran agama.", "K.H. Hasyim Asy'ari"));
        QUOTES.add(new Quote(119, "Persatuan dan kebersamaan adalah kunci utama kejayaan suatu bangsa.", "K.H. Hasyim Asy'ari"));
        QUOTES.add(new Quote(120, "Memaafkan orang lain adalah kemenangan terbesar atas diri sendiri.", "Gus Dur"));
        QUOTES.add(new Quote(121, "Tidak penting apa pun agama atau sukumu, kalau kamu bisa melakukan sesuatu yang baik untuk semua orang, orang tidak pernah tanya apa agamamu.", "Gus Dur"));
        QUOTES.add(new Quote(122, "Gitu aja kok repot!", "Gus Dur"));
        QUOTES.add(new Quote(123, "Yang lebih penting dari politik adalah kemanusiaan.", "Gus Dur"));
        QUOTES.add(new Quote(124, "Kesabaran adalah obat terbaik untuk segala kesulitan.", "Plato"));
        QUOTES.add(new Quote(125, "Awal adalah bagian terpenting dari pekerjaan.", "Plato"));
        QUOTES.add(new Quote(126, "Orang bijak berbicara karena mereka memiliki sesuatu untuk dikatakan; orang bodoh karena mereka harus mengatakan sesuatu.", "Plato"));
        QUOTES.add(new Quote(127, "Arah di mana pendidikan memulai seseorang akan menentukan masa depannya.", "Plato"));
        QUOTES.add(new Quote(128, "Kemenangan pertama dan terbaik adalah menaklukkan diri sendiri.", "Plato"));
        QUOTES.add(new Quote(129, "Kita dapat dengan mudah memaafkan anak kecil yang takut pada kegelapan; tragedi hidup yang sebenarnya adalah ketika orang dewasa takut pada cahaya.", "Plato"));
        QUOTES.add(new Quote(130, "Mengenal diri sendiri adalah awal dari semua kebijaksanaan.", "Aristoteles"));
        QUOTES.add(new Quote(131, "Kita adalah apa yang kita lakukan berulang kali. Keunggulan bukan tindakan, melainkan kebiasaan.", "Aristoteles"));
        QUOTES.add(new Quote(132, "Mendidik pikiran tanpa mendidik hati bukanlah pendidikan sama sekali.", "Aristoteles"));
        QUOTES.add(new Quote(133, "Pikiran yang terdidik mampu mempertimbangkan suatu gagasan tanpa menerimanya.", "Aristoteles"));
        QUOTES.add(new Quote(134, "Kesabaran itu pahit, tetapi buahnya manis.", "Aristoteles"));
        QUOTES.add(new Quote(135, "Bukan hidup yang penting, melainkan hidup yang baik.", "Socrates"));
        QUOTES.add(new Quote(136, "Satu-satunya kebijaksanaan sejati adalah mengetahui bahwa Anda tidak tahu apa-apa.", "Socrates"));
        QUOTES.add(new Quote(137, "Pikiran yang tidak diuji tidak layak untuk dihidupi.", "Socrates"));
        QUOTES.add(new Quote(138, "Dia yang tidak puas dengan apa yang dia miliki, tidak akan puas dengan apa yang dia ingin miliki.", "Socrates"));
        QUOTES.add(new Quote(139, "Biarkan dia yang ingin menggerakkan dunia menggerakkan dirinya sendiri terlebih dahulu.", "Socrates"));
        QUOTES.add(new Quote(140, "Niat tanpa tindakan adalah lamunan. Tindakan tanpa niat adalah mimpi buruk.", "Pepatah Jepang"));
        QUOTES.add(new Quote(141, "Perjalanan seribu mil dimulai dari bawah kaki Anda.", "Lao Tzu"));
        QUOTES.add(new Quote(142, "Kekuatan tidak berasal dari kemampuan fisik, tetapi dari kemauan yang tidak pernah goyah.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(143, "Temukan apa yang Anda cintai dan biarkan itu membunuh Anda.", "Charles Bukowski"));
        QUOTES.add(new Quote(144, "Jika Anda ingin bahagia, jadilah.", "Leo Tolstoy"));
        QUOTES.add(new Quote(145, "Dua prajurit paling kuat adalah kesabaran dan waktu.", "Leo Tolstoy"));
        QUOTES.add(new Quote(146, "Semua keluarga bahagia mirip satu sama lain; setiap keluarga yang tidak bahagia memiliki ketidakbahagiaannya sendiri.", "Leo Tolstoy"));
        QUOTES.add(new Quote(147, "Jika Anda mencari kesempurnaan, Anda tidak akan pernah puas.", "Leo Tolstoy"));
        QUOTES.add(new Quote(148, "Satu-satunya hal yang pasti dalam hidup adalah perubahan.", "Heraclitus"));
        QUOTES.add(new Quote(149, "Tidak ada orang yang melangkah di sungai yang sama dua kali, karena itu bukan sungai yang sama dan dia bukan orang yang sama.", "Heraclitus"));
        QUOTES.add(new Quote(150, "Karakter adalah takdir seseorang.", "Heraclitus"));
        QUOTES.add(new Quote(151, "Cinta adalah keindahan jiwa.", "Saint Augustine"));
        QUOTES.add(new Quote(152, "Dunia adalah sebuah buku, dan mereka yang tidak bepergian hanya membaca satu halaman.", "Saint Augustine"));
        QUOTES.add(new Quote(153, "Kesabaran adalah pendamping kebijaksanaan.", "Saint Augustine"));
        QUOTES.add(new Quote(154, "Tuhan memberi kita ingatan agar kita bisa memiliki mawar di bulan Desember.", "J.M. Barrie"));
        QUOTES.add(new Quote(155, "Hanya mereka yang berisiko pergi terlalu jauh yang mungkin menemukan seberapa jauh seseorang bisa pergi.", "T.S. Eliot"));
        QUOTES.add(new Quote(156, "Setiap saat adalah permulaan baru.", "T.S. Eliot"));
        QUOTES.add(new Quote(157, "Bukan berarti saya sangat pintar, hanya saja saya bertahan dengan masalah lebih lama.", "Albert Einstein"));
        QUOTES.add(new Quote(158, "Ukuran kecerdasan adalah kemampuan untuk berubah.", "Albert Einstein"));
        QUOTES.add(new Quote(159, "Kedamaian tidak dapat dijaga dengan kekuatan; itu hanya dapat dicapai dengan pemahaman.", "Albert Einstein"));
        QUOTES.add(new Quote(160, "Satu-satunya sumber pengetahuan adalah pengalaman.", "Albert Einstein"));
        QUOTES.add(new Quote(161, "Kebijaksanaan bukanlah produk dari sekolah, melainkan dari usaha seumur hidup untuk mendapatkannya.", "Albert Einstein"));
        QUOTES.add(new Quote(162, "Mata diganti mata hanya akan membuat seluruh dunia buta.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(163, "Orang yang lemah tidak akan pernah bisa memaafkan. Memaafkan adalah ciri dari orang yang kuat.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(164, "Kebahagiaan adalah ketika apa yang Anda pikirkan, apa yang Anda katakan, dan apa yang Anda lakukan berada dalam keharmonisan.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(165, "Doa adalah kunci di pagi hari dan kunci di malam hari.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(166, "Seorang pemenang adalah seorang pemimpi yang tidak pernah menyerah.", "Nelson Mandela"));
        QUOTES.add(new Quote(167, "Pikiran yang baik dan hati yang baik selalu merupakan kombinasi yang hebat.", "Nelson Mandela"));
        QUOTES.add(new Quote(168, "Pendidikan adalah paspor kita ke masa depan, karena hari esok milik mereka yang mempersiapkannya hari ini.", "Malcolm X"));
        QUOTES.add(new Quote(169, "Jika Anda tidak membela sesuatu, Anda akan jatuh demi apa pun.", "Malcolm X"));
        QUOTES.add(new Quote(170, "Masa depan milik mereka yang mempersiapkannya hari ini.", "Malcolm X"));
        QUOTES.add(new Quote(171, "Kegelapan tidak bisa mengusir kegelapan; hanya cahaya yang bisa melakukannya. Kebencian tidak bisa mengusir kebencian; hanya cinta yang bisa melakukannya.", "Martin Luther King Jr."));
        QUOTES.add(new Quote(172, "Jika Anda tidak bisa terbang, berlarilah. Jika Anda tidak bisa berlari, berjalanlah. Jika Anda tidak bisa berjalan, merangkaklah. Namun apa pun yang Anda lakukan, Anda harus terus maju.", "Martin Luther King Jr."));
        QUOTES.add(new Quote(173, "Ukuran sejati seorang manusia bukanlah saat berada di kondisi nyaman, melainkan saat berdiri menghadapi tantangan dan ujian.", "Martin Luther King Jr."));
        QUOTES.add(new Quote(174, "Waktu selalu tepat untuk melakukan apa yang benar.", "Martin Luther King Jr."));
        QUOTES.add(new Quote(175, "Pada akhirnya, kita tidak akan mengingat kata-kata musuh kita, melainkan sikap bungkam dari teman-teman kita.", "Martin Luther King Jr."));
        QUOTES.add(new Quote(176, "Impian yang Anda impikan sendiri hanyalah impian. Impian yang Anda impikan bersama adalah kenyataan.", "John Lennon"));
        QUOTES.add(new Quote(177, "Hidup adalah apa yang terjadi pada Anda saat Anda sibuk membuat rencana lain.", "John Lennon"));
        QUOTES.add(new Quote(178, "Semuanya akan baik-baik saja pada akhirnya. Jika tidak baik-baik saja, itu bukan akhir.", "John Lennon"));
        QUOTES.add(new Quote(179, "Tanyakan pada diri Anda sendiri: Apakah yang Anda lakukan hari ini mendekatkan Anda ke tempat yang Anda inginkan besok?", "Paulo Coelho"));
        QUOTES.add(new Quote(180, "Ketika Anda menginginkan sesuatu, seluruh alam semesta bersatu untuk membantu Anda mencapainya.", "Paulo Coelho"));
        QUOTES.add(new Quote(181, "Satu-satunya hal yang membuat impian menjadi tidak mungkin dicapai adalah rasa takut akan kegagalan.", "Paulo Coelho"));
        QUOTES.add(new Quote(182, "Ada satu hal yang membuat impian tidak bisa diwujudkan: rasa takut gagal.", "Paulo Coelho"));
        QUOTES.add(new Quote(183, "Katakan pada hatimu bahwa rasa takut menderita lebih buruk daripada penderitaan itu sendiri.", "Paulo Coelho"));
        QUOTES.add(new Quote(184, "Jangan buang waktu Anda dengan penjelasan: orang hanya mendengar apa yang ingin mereka dengar.", "Paulo Coelho"));
        QUOTES.add(new Quote(185, "Siapa pun yang berjuang melawan monster harus berhati-hati agar dia sendiri tidak menjadi monster.", "Friedrich Nietzsche"));
        QUOTES.add(new Quote(186, "Apa yang tidak membunuh kita membuat kita lebih kuat.", "Friedrich Nietzsche"));
        QUOTES.add(new Quote(187, "Tanpa musik, hidup akan menjadi sebuah kesalahan.", "Friedrich Nietzsche"));
        QUOTES.add(new Quote(188, "Dia yang memiliki 'mengapa' untuk hidup bisa menanggung hampir semua 'bagaimana'.", "Friedrich Nietzsche"));
        QUOTES.add(new Quote(189, "Cara terbaik untuk meramal masa depan Anda adalah dengan menciptakannya.", "Abraham Lincoln"));
        QUOTES.add(new Quote(190, "Hampir semua pria bisa menahan kesulitan, tetapi jika Anda ingin menguji karakter seorang pria, beri dia kekuasaan.", "Abraham Lincoln"));
        QUOTES.add(new Quote(191, "Saya berjalan lambat, tetapi saya tidak pernah berjalan mundur.", "Abraham Lincoln"));
        QUOTES.add(new Quote(192, "Beri saya enam jam untuk menebang pohon dan saya akan menghabiskan empat jam pertama untuk mengasah kapak.", "Abraham Lincoln"));
        QUOTES.add(new Quote(193, "Pada akhirnya, bukan tahun-tahun dalam hidup Anda yang dihitung, melainkan kehidupan dalam tahun-tahun Anda.", "Abraham Lincoln"));
        QUOTES.add(new Quote(194, "Apa pun peran Anda dalam hidup, jadilah yang terbaik.", "Abraham Lincoln"));
        QUOTES.add(new Quote(195, "Saya tidak tahu kunci menuju sukses, tetapi kunci menuju kegagalan adalah mencoba menyenangkan semua orang.", "Bill Cosby"));
        QUOTES.add(new Quote(196, "Jika Anda terlahir miskin itu bukan kesalahan Anda, tetapi jika Anda meninggal miskin itu kesalahan Anda.", "Bill Gates"));
        QUOTES.add(new Quote(197, "Pelanggan Anda yang paling tidak bahagia adalah sumber belajar terbesar Anda.", "Bill Gates"));
        QUOTES.add(new Quote(198, "Merayakan kesuksesan itu baik, tetapi yang lebih penting adalah memperhatikan pelajaran dari kegagalan.", "Bill Gates"));
        QUOTES.add(new Quote(199, "Kita selalu memperkirakan perubahan yang akan terjadi dalam dua tahun ke depan dan meremehkan perubahan yang terjadi dalam sepuluh tahun ke depan.", "Bill Gates"));
        QUOTES.add(new Quote(200, "Jangan membandingkan diri Anda dengan siapa pun di dunia ini... jika Anda melakukannya, Anda menghina diri sendiri.", "Bill Gates"));
        QUOTES.add(new Quote(201, "Risiko terbesar adalah tidak mengambil risiko apa pun.", "Mark Zuckerberg"));
        QUOTES.add(new Quote(202, "Bergeraklah dengan cepat dan buatlah terobosan. Jika tidak pernah membuat terobosan, Anda belum bergerak cukup cepat.", "Mark Zuckerberg"));
        QUOTES.add(new Quote(203, "Pertanyaan yang saya tanyakan pada diri sendiri setiap hari adalah: Apakah saya melakukan hal terpenting yang bisa saya lakukan?", "Mark Zuckerberg"));
        QUOTES.add(new Quote(204, "Orang tidak peduli pada apa yang Anda katakan, mereka peduli pada apa yang Anda hasilkan.", "Mark Zuckerberg"));
        QUOTES.add(new Quote(205, "Ketika kamu berhenti bermimpi, kamu berhenti hidup.", "Malcolm Forbes"));
        QUOTES.add(new Quote(206, "Kegagalan adalah sukses jika kita belajar darinya.", "Malcolm Forbes"));
        QUOTES.add(new Quote(207, "Bekerjalah seolah-olah ada orang lain yang bekerja 24 jam sehari untuk mengambilnya darimu.", "Mark Cuban"));
        QUOTES.add(new Quote(208, "Tidak masalah berapa kali Anda gagal. Anda hanya perlu benar satu kali saja.", "Mark Cuban"));
        QUOTES.add(new Quote(209, "Usaha adalah satu-satunya hal dalam hidup yang dapat Anda kontrol sepenuhnya.", "Mark Cuban"));
        QUOTES.add(new Quote(210, "Kecerdasan tanpa ambisi bagaikan burung tanpa sayap.", "Salvador Dali"));
        QUOTES.add(new Quote(211, "Jangan takut akan kesempurnaan; Anda tidak akan pernah mencapainya.", "Salvador Dali"));
        QUOTES.add(new Quote(212, "Pelajari aturan seperti profesional, sehingga Anda dapat melanggarnya seperti seniman.", "Pablo Picasso"));
        QUOTES.add(new Quote(213, "Tindakan adalah kunci mendasar untuk semua kesuksesan.", "Pablo Picasso"));
        QUOTES.add(new Quote(214, "Segala sesuatu yang dapat Anda bayangkan adalah nyata.", "Pablo Picasso"));
        QUOTES.add(new Quote(215, "Arti hidup adalah menemukan karunia Anda. Tujuan hidup adalah untuk memberikannya.", "Pablo Picasso"));
        QUOTES.add(new Quote(216, "Hanya tunda hingga besok apa yang Anda relakan mati tanpa sempat melakukannya.", "Pablo Picasso"));
        QUOTES.add(new Quote(217, "Seni membasuh dan membersihkan debu kehidupan sehari-hari dari dalam jiwa.", "Pablo Picasso"));
        QUOTES.add(new Quote(218, "Kesuksesan adalah kemampuan melangkah dari satu kegagalan ke kegagalan lain tanpa kehilangan antusiasme.", "Winston Churchill"));
        QUOTES.add(new Quote(219, "Seseorang yang tidak pernah membaca buku yang baik tidak memiliki keunggulan dibanding orang yang tidak bisa membaca.", "Mark Twain"));
        QUOTES.add(new Quote(220, "Hambatan adalah hal-hal menakutkan yang Anda lihat ketika Anda mengalihkan pandangan dari tujuan Anda.", "Henry Ford"));
        QUOTES.add(new Quote(221, "Satu-satunya batasan untuk pencapaian kita hari ini adalah keraguan kita tentang hari esok.", "Franklin D. Roosevelt"));
        QUOTES.add(new Quote(222, "Jika Anda ingin melangkah jauh, berjalanlah bersama-sama.", "Pepatah Afrika"));
        QUOTES.add(new Quote(223, "Tanggul yang kuat berasal dari tumpukan tanah yang kecil.", "Pepatah Cina"));
        QUOTES.add(new Quote(224, "Bahkan pohon yang paling tinggi pun pernah menjadi biji yang sangat kecil.", "Pepatah Jawa"));
        QUOTES.add(new Quote(225, "Urip iku urup (Hidup itu hendaknya memberi manfaat bagi orang lain).", "Sunan Kalijaga"));
        QUOTES.add(new Quote(226, "Aja dumeh, aja nyeleneh, aja ngeluh.", "Sunan Kalijaga"));
        QUOTES.add(new Quote(227, "Sabar iku ingaran mustikaning wasiat (Kesabaran adalah permata kebijaksanaan).", "Pepatah Jawa"));
        QUOTES.add(new Quote(228, "Mikul dhuwur mendhem jero (Menghormati dan menjaga nama baik orang tua/pemimpin).", "Pepatah Jawa"));
        QUOTES.add(new Quote(229, "Sapa tekun bakal tekan (Siapa yang tekun akan sampai pada tujuannya).", "Pepatah Jawa"));
        QUOTES.add(new Quote(230, "Alon-alon waton kelakon (Perlahan-lahan asal terlaksana dengan baik).", "Pepatah Jawa"));
        QUOTES.add(new Quote(231, "Memayu hayuning bawana (Memperindah keindahan dunia).", "Pepatah Jawa"));
        QUOTES.add(new Quote(232, "Orang bijak adalah orang yang mengetahui apa yang tidak diketahuinya.", "Confucius"));
        QUOTES.add(new Quote(233, "Tidak masalah seberapa lambat Anda berjalan selama Anda tidak berhenti.", "Confucius"));
        QUOTES.add(new Quote(234, "Kemuliaan terbesar kita bukan karena tidak pernah jatuh, tetapi bangkit setiap kali kita jatuh.", "Confucius"));
        QUOTES.add(new Quote(235, "Segala sesuatu memiliki keindahan, tetapi tidak semua orang melihatnya.", "Confucius"));
        QUOTES.add(new Quote(236, "Manusia berbudi luhur menuntut segalanya dari dirinya sendiri; manusia kerdil menuntut segalanya dari orang lain.", "Confucius"));
        QUOTES.add(new Quote(237, "Ketika kemarahan timbul, pikirkan konsekuensinya.", "Confucius"));
        QUOTES.add(new Quote(238, "Manusia sejati bertindak sebelum dia berbicara, dan sesudahnya dia berbicara sesuai dengan tindakannya.", "Confucius"));
        QUOTES.add(new Quote(239, "Bila kamu melihat orang baik, pikirlah untuk menyamainya. Bila kamu melihat orang tidak baik, periksalah dirimu sendiri.", "Confucius"));
        QUOTES.add(new Quote(240, "Hidup itu sangat sederhana, tetapi kita bersikeras membuatnya rumit.", "Confucius"));
        QUOTES.add(new Quote(241, "Kekuatan tidak berasal dari kemenangan. Perjuangan Anda yang mengembangkan kekuatan Anda.", "Arnold Schwarzenegger"));
        QUOTES.add(new Quote(242, "Pikiran adalah batasnya. Selama pikiran dapat membayangkan kenyataan bahwa Anda dapat melakukan sesuatu, Anda dapat melakukannya.", "Arnold Schwarzenegger"));
        QUOTES.add(new Quote(243, "Anda tidak bisa memanjat tangga sukses dengan tangan di dalam saku.", "Arnold Schwarzenegger"));
        QUOTES.add(new Quote(244, "Rasa sakit yang Anda rasakan hari ini akan menjadi kekuatan yang Anda rasakan besok.", "Arnold Schwarzenegger"));
        QUOTES.add(new Quote(245, "Saya benci rencana B.", "Arnold Schwarzenegger"));
        QUOTES.add(new Quote(246, "Keberhasilan adalah hasil dari kesempurnaan, kerja keras, belajar dari pengalaman, kesetiaan, dan ketekunan.", "Colin Powell"));
        QUOTES.add(new Quote(247, "Tidak ada rahasia untuk sukses. Sukses adalah hasil dari persiapan, kerja keras, dan belajar dari kegagalan.", "Colin Powell"));
        QUOTES.add(new Quote(248, "Mimpi tidak menjadi kenyataan melalui sihir; itu membutuhkan keringat, tekad, dan kerja keras.", "Colin Powell"));
        QUOTES.add(new Quote(249, "Pemimpin yang baik adalah orang yang melayani orang yang dipimpinnya.", "Colin Powell"));
        QUOTES.add(new Quote(250, "Kesabaran, ketekunan, dan keringat membuat kombinasi yang tidak terkalahkan untuk sukses.", "Napoleon Hill"));
        QUOTES.add(new Quote(251, "Apa pun yang dapat dipikirkan dan diyakini oleh pikiran manusia, itu dapat dicapai.", "Napoleon Hill"));
        QUOTES.add(new Quote(252, "Jangan menunggu; waktunya tidak akan pernah 'pas'. Mulailah di mana Anda berdiri.", "Napoleon Hill"));
        QUOTES.add(new Quote(253, "Setiap kesulitan, setiap kegagalan, membawa serta benih dari keuntungan yang setara atau lebih besar.", "Napoleon Hill"));
        QUOTES.add(new Quote(254, "Tujuan adalah mimpi dengan batas waktu.", "Napoleon Hill"));
        QUOTES.add(new Quote(255, "Jika Anda tidak bisa melakukan hal-hal besar, lakukan hal-hal kecil dengan cara yang besar.", "Napoleon Hill"));
        QUOTES.add(new Quote(256, "Pikiran adalah segalanya. Apa yang Anda pikirkan, itulah wujud diri Anda.", "Buddha"));
        QUOTES.add(new Quote(257, "Tidak ada yang menyelamatkan kita kecuali diri kita sendiri. Tidak ada yang bisa dan tidak ada yang boleh. Kita sendiri harus berjalan di jalan itu.", "Buddha"));
        QUOTES.add(new Quote(258, "Kesehatan adalah hadiah terbesar, kepuasan adalah kekayaan terbesar, kesetiaan adalah hubungan terbaik.", "Buddha"));
        QUOTES.add(new Quote(259, "Satu lilin dapat menyalakan ribuan lilin tanpa memperpendek usianya. Kebahagiaan tidak akan pernah berkurang dengan dibagikan.", "Buddha"));
        QUOTES.add(new Quote(260, "Kamu tidak akan dihukum karena kemarahanmu, kamu akan dihukum oleh kemarahanmu sendiri.", "Buddha"));
        QUOTES.add(new Quote(261, "Pikiran yang tenang membawa kedamaian batin.", "Buddha"));
        QUOTES.add(new Quote(262, "Ubah pikiranmu dan kamu mengubah duniamu.", "Norman Vincent Peale"));
        QUOTES.add(new Quote(263, "Percayalah pada diri sendiri! Milikilah keyakinan teguh pada kemampuan Anda!", "Norman Vincent Peale"));
        QUOTES.add(new Quote(264, "Tembaklah bulan. Bahkan jika Anda meleset, Anda akan mendarat di antara bintang-bintang.", "Norman Vincent Peale"));
        QUOTES.add(new Quote(265, "Sikap yang baik akan menghasilkan tindakan yang baik.", "Norman Vincent Peale"));
        QUOTES.add(new Quote(266, "Berpikirlah positif dan hal-hal baik akan terjadi.", "Norman Vincent Peale"));
        QUOTES.add(new Quote(267, "Hanya ada satu cara untuk menghindari kritik: tidak melakukan apa-apa, tidak mengatakan apa-apa, dan tidak menjadi apa-apa.", "Aristoteles"));
        QUOTES.add(new Quote(268, "Harapan adalah impian bagi jiwa yang terjaga.", "Aristoteles"));
        QUOTES.add(new Quote(269, "Siapa yang menguasai ketakutannya akan benar-benar bebas.", "Aristoteles"));
        QUOTES.add(new Quote(270, "Persahabatan adalah satu jiwa yang tinggal dalam dua tubuh.", "Aristoteles"));
        QUOTES.add(new Quote(271, "Cinta sejati tidak pernah padam.", "Rumi"));
        QUOTES.add(new Quote(272, "Luka adalah tempat di mana cahaya masuk ke dalam dirimu.", "Rumi"));
        QUOTES.add(new Quote(273, "Jangan merasa sepi, seluruh alam semesta ada di dalam dirimu.", "Rumi"));
        QUOTES.add(new Quote(274, "Biarkan keindahan dari apa yang kamu cintai menjadi apa yang kamu lakukan.", "Rumi"));
        QUOTES.add(new Quote(275, "Kamu lahir dengan sayap, mengapa memilih untuk merangkak melalui hidup?", "Rumi"));
        QUOTES.add(new Quote(276, "Tingkatkan katamu, bukan suaramu. Hujanlah yang menumbuhkan bunga, bukan petir.", "Rumi"));
        QUOTES.add(new Quote(277, "Ketika kamu melakukan sesuatu dari jiwamu, kamu merasakan sungai bergerak di dalam dirimu, sebuah kegembiraan.", "Rumi"));
        QUOTES.add(new Quote(278, "Diam adalah bahasa Tuhan, yang lainnya adalah terjemahan yang buruk.", "Rumi"));
        QUOTES.add(new Quote(279, "Kemarin saya pintar, jadi saya ingin mengubah dunia. Hari ini saya bijak, jadi saya mengubah diri saya sendiri.", "Rumi"));
        QUOTES.add(new Quote(280, "Jangan berduka. Apa pun yang hilang darimu akan datang kembali dalam bentuk lain.", "Rumi"));
        QUOTES.add(new Quote(281, "Tindakan membuktikan siapa seseorang; kata-kata hanya membuktikan ingin jadi siapa mereka.", "Anonymous"));
        QUOTES.add(new Quote(282, "Kesuksesan tidak diukur dari posisi yang dicapai seseorang dalam hidup, melainkan dari hambatan yang diatasi.", "Booker T. Washington"));
        QUOTES.add(new Quote(283, "Lakukan yang terbaik dan biarkan Tuhan melakukan sisanya.", "Anonymous"));
        QUOTES.add(new Quote(284, "Kunci menuju kebahagiaan adalah memiliki impian; kunci menuju kesuksesan adalah membuat impian menjadi kenyataan.", "Anonymous"));
        QUOTES.add(new Quote(285, "Senyum adalah jarak terdekat antara dua orang.", "Victor Borge"));
        QUOTES.add(new Quote(286, "Harapan adalah sesuatu yang bersayap yang hinggap di dalam jiwa.", "Emily Dickinson"));
        QUOTES.add(new Quote(287, "Keluarga adalah salah satu mahakarya alam.", "George Santayana"));
        QUOTES.add(new Quote(288, "Mereka yang tidak ingat masa lalu dikutuk untuk mengulangnya.", "George Santayana"));
        QUOTES.add(new Quote(289, "Buku adalah cermin: Anda hanya melihat di dalamnya apa yang sudah Anda miliki di dalam diri Anda.", "Carlos Ruiz Zafón"));
        QUOTES.add(new Quote(290, "Langkah pertama adalah mengatakan bahwa Anda bisa.", "Will Smith"));
        QUOTES.add(new Quote(291, "Tidak ada alasan untuk memiliki Rencana B karena itu mengalihkan perhatian dari Rencana A.", "Will Smith"));
        QUOTES.add(new Quote(292, "Uang dan sukses tidak mengubah orang; mereka hanya memperjelas siapa mereka sebenarnya.", "Will Smith"));
        QUOTES.add(new Quote(293, "Jika Anda tidak bersedia bekerja keras, biarkan orang lain yang melakukannya.", "Will Smith"));
        QUOTES.add(new Quote(294, "Keahlian diciptakan oleh jam-jam kerja keras yang panjang.", "Will Smith"));
        QUOTES.add(new Quote(295, "Jangan pernah membiarkan seseorang memberi tahu Anda bahwa Anda tidak bisa melakukan sesuatu.", "Will Smith"));
        QUOTES.add(new Quote(296, "Masa depan dibentuk oleh apa yang kamu lakukan hari ini, bukan esok hari.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(297, "Orang kaya membeli aset. Orang miskin memiliki pengeluaran. Kelas menengah membeli liabilitas yang mereka kira aset.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(298, "Di dalam dunia nyata, orang yang paling pintar adalah orang yang membuat kesalahan dan belajar darinya.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(299, "Gagal mengalahkan pecundang. Gagal menginspirasi pemenang.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(300, "Satu-satunya aset paling kuat yang kita miliki adalah pikiran kita.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(301, "Ukuran keberhasilan Anda diukur oleh kekuatan keinginan Anda; ukuran impian Anda; dan bagaimana Anda menangani kekecewaan di sepanjang jalan.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(302, "Emosi adalah yang membuat kita manusia. Membuat kita nyata. Kata emosi mengacu pada energi dalam gerakan.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(303, "Orang-orang sukses tidak takut gagal tetapi memahami bahwa itu penting untuk dipelajari dan tumbuh darinya.", "Robert Kiyosaki"));
        QUOTES.add(new Quote(304, "Penghalang terbesar menuju sukses adalah rasa takut gagal.", "Sven-Göran Eriksson"));
        QUOTES.add(new Quote(305, "Keberanian bukanlah ketidakadaan rasa takut, melainkan kemenangan atasnya.", "Nelson Mandela"));
        QUOTES.add(new Quote(306, "Pendidikan adalah senjata paling ampuh yang dapat Anda gunakan untuk mengubah dunia.", "Nelson Mandela"));
        QUOTES.add(new Quote(307, "Bila Anda berbicara dengan bahasa yang dipahami seseorang, itu akan sampai ke kepalanya. Bila Anda berbicara dengan bahasanya, itu akan sampai ke hatinya.", "Nelson Mandela"));
        QUOTES.add(new Quote(308, "Setelah memanjat bukit yang besar, seseorang hanya menemukan bahwa ada lebih banyak bukit untuk dipanjat.", "Nelson Mandela"));
        QUOTES.add(new Quote(309, "Risiko datang dari tidak mengetahui apa yang Anda lakukan.", "Warren Buffett"));
        QUOTES.add(new Quote(310, "Aturan No. 1: Jangan pernah kehilangan uang. Aturan No. 2: Jangan pernah melupakan aturan No. 1.", "Warren Buffett"));
        QUOTES.add(new Quote(311, "Seseorang bisa berteduh hari ini karena ada orang yang menanam pohon di masa lalu.", "Warren Buffett"));
        QUOTES.add(new Quote(312, "Dibutuhkan 20 tahun untuk membangun reputasi dan hanya 5 menit untuk merusaknya. Jika Anda menyadarinya, Anda akan bertindak dengan lebih bijaksana.", "Warren Buffett"));
        QUOTES.add(new Quote(313, "Investasi terbaik yang dapat Anda lakukan adalah pada diri Anda sendiri.", "Warren Buffett"));
        QUOTES.add(new Quote(314, "Harga adalah apa yang Anda bayar. Nilai adalah apa yang Anda dapatkan.", "Warren Buffett"));
        QUOTES.add(new Quote(315, "Jujur adalah hadiah yang sangat mahal, jangan mengharapkannya dari orang murahan.", "Warren Buffett"));
        QUOTES.add(new Quote(316, "Jika Anda tidak menemukan cara untuk menghasilkan uang saat Anda tidur, Anda akan bekerja sampai Anda mati.", "Warren Buffett"));
        QUOTES.add(new Quote(317, "Satu-satunya hal yang berdiri di antara Anda dan tujuan Anda adalah cerita omong kosong yang terus Anda beritahukan pada diri sendiri.", "Jordan Belfort"));
        QUOTES.add(new Quote(318, "Tanpa tujuan yang jelas, hidup hanya menjadi serangkaian kebetulan.", "Jordan Belfort"));
        QUOTES.add(new Quote(319, "Orang-orang sukses 100% yakin bahwa mereka adalah master dari takdir mereka sendiri.", "Jordan Belfort"));
        QUOTES.add(new Quote(320, "Bertindaklah seolah-olah Anda adalah pria kaya, dan Anda akan menjadi kaya.", "Jordan Belfort"));
        QUOTES.add(new Quote(321, "Satu-satunya hal yang menghentikan Anda dari mendapatkan apa yang Anda inginkan adalah cerita yang terus Anda buat.", "Tony Robbins"));
        QUOTES.add(new Quote(322, "Bukan apa yang kita lakukan sekali-sekali yang membentuk hidup kita, tetapi apa yang kita lakukan secara konsisten.", "Tony Robbins"));
        QUOTES.add(new Quote(323, "Ubah ekspektasi Anda dengan rasa syukur dan dunia Anda akan berubah seketika.", "Tony Robbins"));
        QUOTES.add(new Quote(324, "Keputusan Anda, bukan kondisi Anda, yang menentukan takdir Anda.", "Tony Robbins"));
        QUOTES.add(new Quote(325, "Jika Anda melakukan apa yang selalu Anda lakukan, Anda akan mendapatkan apa yang selalu Anda dapatkan.", "Tony Robbins"));
        QUOTES.add(new Quote(326, "Ke mana fokus diarahkan, ke situlah energi akan mengalir.", "Tony Robbins"));
        QUOTES.add(new Quote(327, "Menetapkan tujuan adalah langkah pertama dalam mengubah yang tidak terlihat menjadi terlihat.", "Tony Robbins"));
        QUOTES.add(new Quote(328, "Setiap masalah adalah hadiah—tanpa masalah kita tidak akan tumbuh.", "Tony Robbins"));
        QUOTES.add(new Quote(329, "Pemimpin menghabiskan 5% waktu mereka untuk masalah dan 95% waktu mereka untuk solusi.", "Tony Robbins"));
        QUOTES.add(new Quote(330, "Masa depan milik mereka yang percaya pada keindahan impian mereka.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(331, "Tidak ada seorang pun yang bisa membuat Anda merasa rendah diri tanpa persetujuan Anda.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(332, "Pikiran besar mendiskusikan ide; pikiran rata-rata mendiskusikan peristiwa; pikiran kecil mendiskusikan orang.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(333, "Anda harus melakukan hal yang menurut Anda tidak bisa Anda lakukan.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(334, "Penanganan diri sendiri memerlukan penggunaan kepala; penanganan orang lain memerlukan penggunaan hati.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(335, "Kebahagiaan bukan tujuan... itu adalah produk sampingan dari kehidupan yang dijalani dengan baik.", "Eleanor Roosevelt"));
        QUOTES.add(new Quote(336, "Orang yang paling bahagia tidak memiliki yang terbaik dari segalanya, mereka hanya membuat yang terbaik dari segalanya.", "Unknown"));
        QUOTES.add(new Quote(337, "Tersenyumlah pada prosesnya, nikmati perjalanannya.", "Unknown"));
        QUOTES.add(new Quote(338, "Kebahagiaan adalah pilihan, bukan hasil.", "Unknown"));
        QUOTES.add(new Quote(339, "Satu-satunya batasan dalam hidupmu adalah batasan yang kamu buat dalam pikiranmu sendiri.", "Unknown"));
        QUOTES.add(new Quote(340, "Jangan menunggu kesempatan, ciptakanlah.", "Unknown"));
        QUOTES.add(new Quote(341, "Semakin keras perjuanganmu meraih sesuatu, semakin besar kepuasan saat kamu mencapainya.", "Unknown"));
        QUOTES.add(new Quote(342, "Bermimpilah lebih besar. Lakukan lebih banyak.", "Unknown"));
        QUOTES.add(new Quote(343, "Jangan berhenti saat kamu lelah. Berhentilah saat kamu selesai.", "Unknown"));
        QUOTES.add(new Quote(344, "Bangunlah dengan tekad. Tidurlah dengan kepuasan.", "Unknown"));
        QUOTES.add(new Quote(345, "Lakukan sesuatu hari ini yang akan membuat dirimu di masa depan berterima kasih.", "Unknown"));
        QUOTES.add(new Quote(346, "Hal-hal kecil membuat hari-hari besar.", "Unknown"));
        QUOTES.add(new Quote(347, "Ini akan sulit, tapi sulit tidak berarti tidak mungkin.", "Unknown"));
        QUOTES.add(new Quote(348, "Jangan menunggu esok hari, mulailah sekarang.", "Unknown"));
        QUOTES.add(new Quote(349, "Terkadang kita diuji bukan untuk menunjukkan kelemahan kita, tetapi untuk menemukan kekuatan kita.", "Unknown"));
        QUOTES.add(new Quote(350, "Kunci menuju sukses adalah fokus pada tujuan, bukan hambatan.", "Unknown"));
        QUOTES.add(new Quote(351, "Keyakinan dapat memindahkan gunung.", "Pepatah Bijak"));
        QUOTES.add(new Quote(352, "Air yang tenang menghanyutkan.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(353, "Berakit-rakit ke hulu, berenang-renang ke tepian. Bersakit-sakit dahulu, bersenang-senang kemudian.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(354, "Di mana ada kemauan, di situ ada jalan.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(355, "Sekali merengkuh dayung, dua tiga pulau terlampaui.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(356, "Tong kosong nyaring bunyinya.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(357, "Hemat pangkal kaya, rajin pangkal pandai.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(358, "Sedia payung sebelum hujan.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(359, "Bagai air di daun talas.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(360, "Sambil menyelam minum air.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(361, "Tak ada gading yang tak retak.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(362, "Besar pasak daripada tiang.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(363, "Kecil-kecil cabai rawit.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(364, "Di mana bumi dipijak, di situ langit dijunjung.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(365, "Bersatu kita teguh, bercerai kita runtuh.", "Pepatah Indonesia"));

        // Quotes Tambahan (Iman, Cinta & Kasih, Integritas, Kejujuran)
        QUOTES.add(new Quote(366, "Kejujuran adalah bab pertama dalam buku kebijaksanaan.", "Thomas Jefferson"));
        QUOTES.add(new Quote(367, "Integritas adalah melakukan hal yang benar, bahkan ketika tidak ada orang yang melihat.", "C.S. Lewis"));
        QUOTES.add(new Quote(368, "Iman adalah mengambil langkah pertama bahkan ketika Anda tidak melihat seluruh tangga.", "Martin Luther King Jr."));
        QUOTES.add(new Quote(369, "Cinta dan kasih sayang adalah kebutuhan, bukan kemewahan. Tanpanya, kemanusiaan tidak dapat bertahan.", "Dalai Lama"));
        QUOTES.add(new Quote(370, "Orang yang memiliki integritas tinggi tidak akan pernah mengorbankan prinsip hanya demi keuntungan sesaat.", "B.J. Habibie"));
        QUOTES.add(new Quote(371, "Iman tanpa perbuatan adalah mati; keyakinan sejati selalu membuahkan kebaikan.", "Jalaluddin Rumi"));
        QUOTES.add(new Quote(372, "Di mana ada cinta, di situ ada kehidupan.", "Mahatma Gandhi"));
        QUOTES.add(new Quote(373, "Kejujuran adalah mata uang yang berlaku di seluruh dunia.", "Pepatah Indonesia"));
        QUOTES.add(new Quote(374, "Integritas adalah pilihan untuk memilih keberanian daripada kenyamanan; memilih apa yang benar daripada apa yang mudah.", "Brené Brown"));
        QUOTES.add(new Quote(375, "Kasih sayang adalah bahasa yang membuat orang tuli dapat mendengar dan orang buta dapat melihat.", "Mark Twain"));
        QUOTES.add(new Quote(376, "Kejujuran mungkin tidak membuatmu mendapatkan banyak teman, tetapi selalu membuatmu mendapatkan teman yang tepat.", "John Lennon"));
        QUOTES.add(new Quote(377, "Iman adalah ketenangan di dalam jiwa yang percaya bahwa Tuhan selalu memberikan yang terbaik pada waktunya.", "Imam Al-Ghazali"));
        QUOTES.add(new Quote(378, "Ukuran sejati sebuah bangsa adalah bagaimana ia memperlakukan anggotanya yang paling lemah dengan kasih dan keadilan.", "Nelson Mandela"));
        QUOTES.add(new Quote(379, "Jangan perhatikan apa yang dikatakan orang tentang kejujuranmu, tunjukkanlah melalui setiap perbuatanmu.", "Buya Hamka"));
        QUOTES.add(new Quote(380, "Cinta tidak terlihat dengan mata, melainkan dirasakan dengan hati.", "Helen Keller"));
        QUOTES.add(new Quote(381, "Integritas memberi Anda kebebasan karena Anda tidak perlu mengingat kebohongan yang pernah Anda katakan.", "Warren Buffett"));
        QUOTES.add(new Quote(382, "Iman tidak membuat segalanya mudah, tetapi iman membuat segalanya mungkin.", "Kata Bijak"));
        QUOTES.add(new Quote(383, "Kasih yang tulus tidak pernah menuntut balasan, ia memberi karena itulah sifat alaminya.", "Bunda Teresa"));
        QUOTES.add(new Quote(384, "Orang yang jujur pada dirinya sendiri tidak akan pernah berlaku curang pada orang lain.", "William Shakespeare"));
        QUOTES.add(new Quote(385, "Karakter adalah apa yang Anda lakukan saat gelap; integritas adalah komitmen pada nilai-nilai kebenaran.", "Dwight L. Moody"));
        QUOTES.add(new Quote(386, "Sebarkan cinta ke mana pun Anda pergi. Jangan biarkan siapa pun datang kepada Anda tanpa pulang dengan lebih bahagia.", "Bunda Teresa"));
        QUOTES.add(new Quote(387, "Kejujuran dan integritas adalah dua kualitas terpenting yang menentukan kualitas kepemimpinan seseorang.", "Zig Ziglar"));
        QUOTES.add(new Quote(388, "Iman adalah penerang dalam kegelapan dan kompas saat kita kehilangan arah.", "Albert Einstein"));
        QUOTES.add(new Quote(389, "Cinta sejati adalah ketika kebahagiaan orang lain lebih penting daripada kebahagiaan Anda sendiri.", "H. Jackson Brown Jr."));
        QUOTES.add(new Quote(390, "Jagalah integritasmu bagai menjaga mata air kehidupan; sekali tercemar, butuh waktu lama untuk memulihkannya.", "Ir. Soekarno"));

        // Quotes Tambahan: Nasihat, Kasih, Didikan, dan Pembentukan Karakter (391 - 495)
        // 1. Nasihat dan Kebijaksanaan (391 - 400)
        QUOTES.add(new Quote(391, "Jalan orang bodoh lurus dalam anggapannya sendiri, tetapi siapa mendengarkan nasihat, ia bijak.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(392, "Rancangan gagal kalau tidak ada pertimbangan, tetapi terlaksana kalau penasihat banyak.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(393, "Dengarkanlah nasihat dan terimalah didikan, supaya engkau menjadi bijak di masa depan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(394, "Besi menajamkan besi, orang menajamkan sesamanya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(395, "Setiap orang hendaklah cepat untuk mendengar, tetapi lambat untuk berkata-kata, dan juga lambat untuk marah.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(396, "Perkataan yang menyenangkan adalah seperti sarang madu, manis bagi hati dan obat bagi tulang-tulang.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(397, "Jikalau seseorang memberi jawab sebelum mendengar, itulah kebodohan dan kecelaannya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(398, "Hati orang berpengertian memperoleh pengetahuan, dan telinga orang bijak menuntut pengetahuan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(399, "Rancangan di dalam hati manusia itu seperti air yang dalam, tetapi orang yang pandai tahu menimbanya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(400, "Mendengar hardikan orang berhikmat lebih baik dari pada mendengar nyanyian orang bodoh.", DEFAULT_AUTHOR));

        // 2. Kasih dan Kepedulian (401 - 410)
        QUOTES.add(new Quote(401, "Kasih itu sabar; kasih itu murah hati; ia tidak cemburu. Ia tidak memegahkan diri dan tidak sombong.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(402, "Kasih tidak melakukan yang tidak sopan dan tidak mencari keuntungan diri sendiri. Ia tidak pemarah dan tidak menyimpan kesalahan orang lain.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(403, "Kasih menutupi segala sesuatu, percaya segala sesuatu, mengharapkan segala sesuatu, sabar menanggung segala sesuatu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(404, "Hendaklah kamu saling mengasihi sebagai saudara dan saling mendahului dalam memberi hormat.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(405, "Hendaklah kamu ramah seorang terhadap yang lain, penuh kasih mesra dan saling mengampuni.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(406, "Kebencian menimbulkan pertengkaran, tetapi kasih menutupi segala pelanggaran.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(407, "Tetapi yang terutama: kasihilah sungguh-sungguh seorang akan yang lain, sebab kasih menutupi banyak sekali dosa.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(408, "Bertolong-tolonganlah menanggung bebanmu! Demikianlah kamu memenuhi hukum kasih.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(409, "Marilah kita saling memperhatikan supaya kita saling mendorong dalam kasih dan dalam pekerjaan baik.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(410, "Segala sesuatu yang kamu kehendaki supaya orang perbuat kepadamu, perbuatlah demikian juga kepada mereka.", DEFAULT_AUTHOR));

        // 3. Didikan dan Pendidikan Karakter (411 - 420)
        QUOTES.add(new Quote(411, "Dengarkanlah didikan ayahmu, dan jangan menyia-nyiakan ajaran ibumu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(412, "Janganlah engkau menolak didikan Tuhan, dan janganlah bosan akan peringatan-Nya. Karena Tuhan memberi ajaran kepada yang dikasihi-Nya, seperti seorang ayah kepada anak yang disayangi.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(413, "Berpeganglah pada didikan, janganlah melepaskannya, peliharalah dia, karena dialah hidupmu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(414, "Siapa mengindahkan didikan, menuju jalan kehidupan, tetapi siapa mengabaikan teguran, tersesat.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(415, "Siapa mencintai didikan, mencintai pengetahuan; tetapi siapa membenci teguran, adalah dungu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(416, "Anak yang bijak mendengarkan didikan ayahnya, tetapi seorang pencemooh tidak mendengarkan hardikan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(417, "Orang bodoh menolak didikan ayahnya, tetapi siapa mengindahkan teguran adalah bijak.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(418, "Siapa mengabaikan didikan membuang dirinya sendiri, tetapi siapa mendengarkan teguran, memperoleh akal budi.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(419, "Didiklah orang muda menurut jalan yang patut baginya, maka pada masa tuanya pun ia tidak akan menyimpang dari pada jalan itu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(420, "Memang tiap-tiap ganjaran pada waktu ia diberikan tidak mendatangkan sukacita, tetapi dukacita. Tetapi kemudian ia menghasilkan buah kebenaran yang memberikan damai kepada mereka yang dilatih olehnya.", DEFAULT_AUTHOR));

        // 4. Kerajinan dan Tidak Malas (421 - 430)
        QUOTES.add(new Quote(421, "Pergilah kepada semut, perhatikanlah lakunya dan jadilah bijak: biarpun tidak ada pemimpinnya, pengaturnya atau penguasanya, ia menyediakan rotinya di musim panas, dan mengumpulkan makanannya pada waktu panen.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(422, "Hai pemalas, berapa lama lagi engkau berbaring? Bilakah engkau akan bangun dari tidurmu? Tidur sebentar lagi, mengantuk sebentar lagi, melipat tangan sebentar lagi untuk tinggal berbaring — maka datanglah kemiskinan kepadamu seperti seorang penyerbu, dan kekurangan seperti orang yang bersenjata.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(423, "Tangan yang lamban membuat miskin, tetapi tangan orang rajin menjadikan kaya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(424, "Tangan orang rajin memegang kekuasaan, tetapi kemalasan mengakibatkan kerja paksa.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(425, "Hati si pemalas penuh keinginan, tetapi sia-sia, sedangkan hati orang rajin diberi kelimpahan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(426, "Dalam tiap jerih payah ada keuntungan, tetapi kata-kata belaka mendatangkan kekurangan saja.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(427, "Orang yang bermalas-malas dalam pekerjaannya sudah menjadi saudara dari si perusak.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(428, "Rancangan orang rajin semata-mata mendatangkan kelimpahan, tetapi setiap orang yang tergesa-gesa hanya akan mengalami kekurangan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(429, "Janganlah hendaknya kerajinanmu kendor, biarlah semangatmu menyala-nyala dan layanilah Tuhan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(430, "Apa pun juga yang kamu perbuat, perbuatlah dengan segenap hatimu seperti untuk Tuhan dan bukan untuk manusia.", DEFAULT_AUTHOR));

        // 5. Kejujuran dan Integritas (431 - 438)
        QUOTES.add(new Quote(431, "Siapa bersih kelakuannya, aman jalannya, tetapi siapa berliku-liku jalannya, akan diketahui.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(432, "Orang yang jujur dipimpin oleh ketulusannya, tetapi pengkhianat dirusak oleh kecurangannya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(433, "Orang yang dusta bibirnya adalah kekejian bagi Tuhan, tetapi orang yang berlaku setia dikenan-Nya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(434, "Timbangan dan neraca yang betul adalah kepunyaan Tuhan, segala batu timbangan di dalam pundi-pundi adalah buatan-Nya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(435, "Orang benar yang bersih kelakuannya — berbahagialah keturunannya!", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(436, "Barangsiapa setia dalam perkara-perkara kecil, ia setia juga dalam perkara-perkara besar. Dan barangsiapa tidak benar dalam perkara-perkara kecil, ia tidak benar juga dalam perkara-perkara besar.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(437, "Karena itu buanglah dusta dan berkatalah benar seorang kepada yang lain, karena kita adalah sesama anggota.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(438, "Semua yang benar, semua yang mulia, semua yang adil, semua yang suci, semua yang manis, semua yang sedap didengar, semua yang disebut kebajikan dan patut dipuji, pikirkanlah semuanya itu.", DEFAULT_AUTHOR));

        // 6. Kerendahan Hati dan Menghormati Sesama (439 - 445)
        QUOTES.add(new Quote(439, "Jikalau keangkuhan tiba, tiba juga cemooh, tetapi hikmat ada pada orang yang rendah hati.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(440, "Takut akan Tuhan adalah didikan yang mendatangkan hikmat, dan kerendahan hati mendahului kehormatan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(441, "Kecongkakan mendahului kehancuran, dan tinggi hati mendahului kejatuhan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(442, "Dengan tidak mencari kepentingan sendiri atau puji-pujian yang sia-sia. Sebaliknya hendaklah dengan rendah hati yang seorang menganggap yang lain lebih utama dari pada dirinya sendiri.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(443, "Hendaklah kamu sehati sepikir dalam hidupmu bersama; janganlah kamu memikirkan perkara-perkara yang tinggi, tetapi arahkanlah dirimu kepada perkara-perkara yang sederhana. Janganlah menganggap dirimu pandai!", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(444, "Rendahkanlah dirimu seorang terhadap yang lain, sebab: Allah menentang orang yang congkak, tetapi mengasihani orang yang rendah hati.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(445, "Barangsiapa terbesar di antara kamu, hendaklah ia menjadi pelayanmu.", DEFAULT_AUTHOR));

        // 7. Kesabaran dan Pengendalian Diri (446 - 453)
        QUOTES.add(new Quote(446, "Orang yang sabar besar pengertiannya, tetapi siapa cepat marah membesarkan kebodohan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(447, "Si pemarah membangkitkan pertengkaran, tetapi orang yang sabar memadamkan perbantahan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(448, "Orang yang sabar melebihi seorang pahlawan, orang yang menguasai dirinya, melebihi orang yang merebut kota.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(449, "Akal budi membuat seseorang panjang sabar dan orang itu dipuji karena memaafkan pelanggaran.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(450, "Janganlah lekas-lekas marah dalam hati, karena amarah menetap dalam dada orang bodoh.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(451, "Buah kebaikan sejati ialah: kasih, sukacita, damai sejahtera, kesabaran, kemurahan, kebaikan, kesetiaan, kelemahlembutan, dan penguasaan diri. Tidak ada hukum yang menentang hal-hal itu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(452, "Karena itu, sebagai orang-orang pilihan Allah yang dikuduskan dan dikasihi-Nya, kenakanlah belas kasihan, kemurahan, kerendahan hati, kelemahlembutan dan kesabaran.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(453, "Sebab amarah manusia tidak mengerjakan kebenaran di hadapan Allah.", DEFAULT_AUTHOR));

        // 8. Pengampunan dan Perdamaian (454 - 460)
        QUOTES.add(new Quote(454, "Berbahagialah orang yang membawa damai, karena mereka membawa ketenteraman bagi sesama.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(455, "Sedapat-dapatnya, kalau hal itu bergantung padamu, hiduplah dalam perdamaian dengan semua orang!", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(456, "Janganlah kamu kalah terhadap kejahatan, tetapi kalahkanlah kejahatan dengan kebaikan!", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(457, "Apabila kamu menjadi marah, janganlah kamu berbuat dosa: janganlah matahari terbenam, sebelum padam amarahmu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(458, "Sabarlah kamu seorang terhadap yang lain, dan ampunilah seorang akan yang lain apabila yang seorang menaruh dendam terhadap yang lain, sama seperti Tuhan telah mengampuni kamu, kamu perbuat jugalah demikian.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(459, "Siapa menutupi pelanggaran, mengejar kasih, tetapi siapa membangkit-bangkit perkara, menceraikan sahabat yang karib.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(460, "Karena jikalau kamu mengampuni kesalahan orang, Tuhan Yang Maha Pengampun akan mengampuni kamu juga.", DEFAULT_AUTHOR));

        // 9. Semangat dan Pantang Menyerah (461 - 467)
        QUOTES.add(new Quote(461, "Janganlah kita jemu-jemu berbuat baik, karena apabila sudah datang waktunya, kita akan menuai, jika kita tidak menjadi lemah.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(462, "Segala sesuatu yang dijumpai tanganmu untuk dikerjakan, kerjakanlah itu sekuat tenaga.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(463, "Sebab tujuh kali orang benar jatuh, namun ia bangun kembali.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(464, "Segala perkara dapat kutanggung di dalam Dia yang memberi kekuatan kepadaku.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(465, "Janganlah jemu-jemu berbuat apa yang baik.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(466, "Berbahagialah orang yang bertahan dalam pencobaan, sebab apabila ia sudah tahan uji, ia akan menerima mahkota kehidupan yang dijanjikan Allah kepada barangsiapa yang mengasihi Dia.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(467, "Kesengsaraan itu menimbulkan ketekunan, dan ketekunan menimbulkan tahan uji dan tahan uji menimbulkan pengharapan.", DEFAULT_AUTHOR));

        // 10. Tanggung Jawab dan Perbuatan Baik (468 - 475)
        QUOTES.add(new Quote(468, "Demikianlah hendaknya terangmu bercahaya di depan orang, supaya mereka melihat perbuatanmu yang baik dan memuliakan Tuhan Yang Maha Esa.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(469, "Dan sebagaimana kamu kehendaki supaya orang perbuat kepadamu, perbuatlah juga demikian kepada mereka.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(470, "Karena itu, selama masih ada kesempatan bagi kita, marilah kita berbuat baik kepada semua orang, tetapi terutama kepada kawan-kawan kita seiman.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(471, "Dengan rela jalankanlah pelayananmu seperti orang-orang yang melayani Tuhan dan bukan manusia.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(472, "Dan segala sesuatu yang kamu lakukan dengan perkataan atau perbuatan, lakukanlah semuanya itu dengan tulus hati, sambil mengucap syukur kepada Allah.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(473, "Perhatikanlah, supaya jangan ada orang yang membalas jahat dengan jahat, tetapi usahakanlah senantiasa yang baik, terhadap kamu masing-masing dan terhadap semua orang.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(474, "Janganlah memfitnah, janganlah bertengkar, hendaklah selalu ramah dan bersikap lemah lembut terhadap semua orang.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(475, "Dan janganlah kamu lupa berbuat baik dan memberi bantuan, sebab korban-korban yang demikianlah yang berkenan kepada Allah.", DEFAULT_AUTHOR));

        // 11. Syukur dan Berpikir Positif (476 - 481)
        QUOTES.add(new Quote(476, "Hati yang gembira adalah obat yang manjur, tetapi semangat yang patah mengeringkan tulang.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(477, "Janganlah hendaknya kamu kuatir tentang apa pun juga, tetapi nyatakanlah dalam segala hal keinginanmu kepada Allah dalam doa dan permohonan dengan ucapan syukur.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(478, "Semua yang benar, semua yang mulia, semua yang disebut kebajikan dan patut dipuji, pikirkanlah semuanya itu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(479, "Mengucap syukurlah dalam segala hal, sebab itulah yang dikehendaki Allah bagi kamu.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(480, "Inilah hari yang dijadikan Tuhan, marilah kita bersorak-sorak dan bersukacita karenanya!", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(481, "Hendaklah damai sejahtera memerintah dalam hatimu, karena untuk itulah kamu telah dipanggil menjadi satu kesatuan. Dan bersyukurlah.", DEFAULT_AUTHOR));

        // 12. Kepemimpinan dan Kebijaksanaan dalam Bertindak (482 - 488)
        QUOTES.add(new Quote(482, "Hati manusia memikir-mikirkan jalannya, tetapi Tuhanlah yang menentukan arah langkahnya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(483, "Dengan keadilan seorang raja menegakkan negeri, tetapi siapa memungut pajak terlalu tinggi, meruntuhkannya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(484, "Raja yang menghakimi orang lemah dengan adil, takhtanya tetap kokoh untuk selama-lamanya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(485, "Hai manusia, telah diberitahukan kepadamu apa yang baik. Dan apakah yang dituntut Tuhan dari padamu: selain berlaku adil, mencintai kesetiaan, dan hidup dengan rendah hati di hadapan Allahmu?", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(486, "Barangsiapa ingin menjadi besar di antara kamu, hendaklah ia menjadi pelayanmu, dan barangsiapa ingin menjadi yang terkemuka di antara kamu, hendaklah ia menjadi hamba untuk semuanya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(487, "Setiap orang yang kepadanya banyak diberi, dari padanya akan banyak dituntut, dan kepada siapa yang banyak dipercayakan, dari padanya akan lebih banyak lagi dituntut.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(488, "Tetapi hikmat yang dari atas adalah pertama-tama murni, selanjutnya pendamai, peramah, penurut, penuh belas kasihan dan buah-buah yang baik, tidak memihak dan tidak munafik.", DEFAULT_AUTHOR));

        // 13. Motivasi dan Harapan (489 - 495)
        QUOTES.add(new Quote(489, "Sebab Aku ini mengetahui rancangan-rancangan apa yang ada pada-Ku mengenai kamu, demikianlah firman Tuhan, yaitu rancangan damai sejahtera dan bukan rancangan kecelakaan, untuk memberikan kepadamu hari depan yang penuh harapan.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(490, "Serahkanlah hidupmu kepada Tuhan dan percayalah kepada-Nya, dan Ia akan bertindak.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(491, "Allah itu bagi kita tempat perlindungan dan kekuatan, sebagai penolong dalam kesesakan sangat terbukti.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(492, "Tetapi orang-orang yang menanti-nantikan Tuhan mendapat kekuatan baru: mereka seumpama rajawali yang naik terbang dengan kekuatan sayapnya; mereka berlari dan tidak menjadi lesu, mereka berjalan dan tidak menjadi lelah.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(493, "Kita tahu sekarang, bahwa Allah turut bekerja dalam segala sesuatu untuk mendatangkan kebaikan bagi mereka yang mengasihi Dia.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(494, "Sebab itu kami tidak tawar hati, tetapi meskipun manusia lahiriah kami semakin merosot, namun manusia batiniah kami dibaharui dari sehari ke sehari.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(495, "Akan hal ini aku yakin sepenuhnya, yaitu Ia, yang memulai pekerjaan yang baik di antara kamu, akan meneruskannya sampai pada akhirnya.", DEFAULT_AUTHOR));
        QUOTES.add(new Quote(496, "Takut akan Tuhan adalah permulaan pengetahuan, tetapi orang bodoh menghina hikmat dan didikan.", DEFAULT_AUTHOR));
    }

    public static List<Quote> getAllQuotes() {
        return QUOTES;
    }

    public static Quote getQuoteById(int id) {
        if (id >= 1 && id <= QUOTES.size()) {
            return QUOTES.get(id - 1);
        }
        return QUOTES.get(0);
    }

    /**
     * Dapatkan Quote berdasarkan Jadwal Sesi Hari Ini:
     * - Sesi Pagi (07:00 - 14:29): Quote Sesi Pagi
     * - Sesi Sore/Malam (14:30 ke atas): Quote Sesi Sore
     */
    public static Quote getScheduledQuote() {
        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
        int year = calendar.get(Calendar.YEAR);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int totalQuotes = QUOTES.size();

        int sessionSlot = (dayOfYear - 1) * 2;
        if (hour >= 14 && minute >= 30) {
            sessionSlot += 1;
        } else if (hour > 14 || hour < 7) {
            sessionSlot += 1;
        }

        int yearOffset = (year * 97) % totalQuotes;
        int uniqueIndex = Math.abs((sessionSlot * 137 + yearOffset + 42)) % totalQuotes;
        return QUOTES.get(uniqueIndex);
    }

    public static String getScheduleSessionTitle() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (hour >= 7 && (hour < 14 || (hour == 14 && minute < 30))) {
            return "🌅 MOTIVASI PAGI";
        } else {
            return "🌇 APRESIASI SORE";
        }
    }

    public static Quote getTodayQuote() {
        return getScheduledQuote();
    }

    public static Quote getRandomQuote() {
        int randomIndex = (int) (Math.random() * QUOTES.size());
        return QUOTES.get(randomIndex);
    }

    public static Quote getRandomQuoteExcluding(int excludeId) {
        if (QUOTES.size() <= 1) {
            return QUOTES.get(0);
        }
        int randomIndex = (int) (Math.random() * QUOTES.size());
        Quote candidate = QUOTES.get(randomIndex);
        if (candidate.getId() == excludeId) {
            candidate = QUOTES.get((randomIndex + 1) % QUOTES.size());
        }
        return candidate;
    }
}
