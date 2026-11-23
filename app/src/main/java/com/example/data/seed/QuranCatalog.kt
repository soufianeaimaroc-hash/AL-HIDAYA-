package com.example.data.seed

import com.example.data.local.CachedSurahAyahEntity

data class SurahInfo(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val englishTranslation: String,
    val ayahCount: Int,
    val isMeccan: Boolean,
    val startJuz: Int,
    val startPage: Int
)

data class JuzInfo(
    val number: Int,
    val arabicName: String,
    val englishName: String,
    val startSurah: Int,
    val startAyah: Int,
    val startPage: Int
)

data class HizbInfo(
    val number: Int,
    val juzNumber: Int,
    val startSurah: Int,
    val startAyah: Int,
    val titleAr: String,
    val titleEn: String
)

data class DailyAyah(
    val surahNumber: Int,
    val ayahNumber: Int,
    val surahNameAr: String,
    val surahNameEn: String,
    val arabicText: String,
    val englishText: String
)

object QuranCatalog {
    val surahs: List<SurahInfo> = listOf(
        SurahInfo(1, "الفاتحة", "Al-Fatihah", "The Opener", 7, true, 1, 1),
        SurahInfo(2, "البقرة", "Al-Baqarah", "The Cow", 286, false, 1, 2),
        SurahInfo(3, "آل عمران", "Ali 'Imran", "Family of Imran", 200, false, 3, 50),
        SurahInfo(4, "النساء", "An-Nisa", "The Women", 176, false, 4, 77),
        SurahInfo(5, "المائدة", "Al-Ma'idah", "The Table Spread", 120, false, 6, 106),
        SurahInfo(6, "الأنعام", "Al-An'am", "The Cattle", 165, true, 7, 128),
        SurahInfo(7, "الأعراف", "Al-A'raf", "The Heights", 206, true, 8, 151),
        SurahInfo(8, "الأنفال", "Al-Anfal", "The Spoils of War", 75, false, 9, 177),
        SurahInfo(9, "التوبة", "At-Tawbah", "The Repentance", 129, false, 10, 187),
        SurahInfo(10, "يونس", "Yunus", "Jonah", 109, true, 11, 208),
        SurahInfo(11, "هود", "Hud", "Hud", 123, true, 11, 221),
        SurahInfo(12, "يوسف", "Yusuf", "Joseph", 111, true, 12, 235),
        SurahInfo(13, "الرعد", "Ar-Ra'd", "The Thunder", 43, false, 13, 249),
        SurahInfo(14, "إبراهيم", "Ibrahim", "Abraham", 52, true, 13, 255),
        SurahInfo(15, "الحجر", "Al-Hijr", "The Rocky Tract", 99, true, 14, 262),
        SurahInfo(16, "النحل", "An-Nahl", "The Bee", 128, true, 14, 267),
        SurahInfo(17, "الإسراء", "Al-Isra", "The Night Journey", 111, true, 15, 282),
        SurahInfo(18, "الكهف", "Al-Kahf", "The Cave", 110, true, 15, 293),
        SurahInfo(19, "مريم", "Maryam", "Mary", 98, true, 16, 305),
        SurahInfo(20, "طه", "Taha", "Ta-Ha", 135, true, 16, 312),
        SurahInfo(21, "الأنبياء", "Al-Anbya", "The Prophets", 112, true, 17, 322),
        SurahInfo(22, "الحج", "Al-Hajj", "The Pilgrimage", 78, false, 17, 332),
        SurahInfo(23, "المؤمنون", "Al-Mu'minun", "The Believers", 118, true, 18, 342),
        SurahInfo(24, "النور", "An-Nur", "The Light", 64, false, 18, 350),
        SurahInfo(25, "الفرقان", "Al-Furqan", "The Criterion", 77, true, 18, 359),
        SurahInfo(26, "الشعراء", "Ash-Shu'ara", "The Poets", 227, true, 19, 367),
        SurahInfo(27, "النمل", "An-Naml", "The Ant", 93, true, 19, 377),
        SurahInfo(28, "القصص", "Al-Qasas", "The Stories", 88, true, 20, 385),
        SurahInfo(29, "العنكبوت", "Al-'Ankabut", "The Spider", 69, true, 20, 396),
        SurahInfo(30, "الروم", "Ar-Rum", "The Romans", 60, true, 21, 404),
        SurahInfo(31, "لقمان", "Luqman", "Luqman", 34, true, 21, 411),
        SurahInfo(32, "السجدة", "As-Sajdah", "The Prostration", 30, true, 21, 415),
        SurahInfo(33, "الأحزاب", "Al-Ahzab", "The Combined Forces", 73, false, 21, 418),
        SurahInfo(34, "سبأ", "Saba", "Sheba", 54, true, 22, 428),
        SurahInfo(35, "فاطر", "Fatir", "Originator", 45, true, 22, 434),
        SurahInfo(36, "يس", "Ya-Sin", "Ya Sin", 83, true, 22, 440),
        SurahInfo(37, "الصافات", "As-Saffat", "Those who set the Ranks", 182, true, 23, 446),
        SurahInfo(38, "ص", "Sad", "The Letter Sad", 88, true, 23, 453),
        SurahInfo(39, "الزمر", "Az-Zumar", "The Troops", 75, true, 23, 458),
        SurahInfo(40, "غافر", "Ghafir", "The Forgiver", 85, true, 24, 467),
        SurahInfo(41, "فصلت", "Fussilat", "Explained in Detail", 54, true, 24, 477),
        SurahInfo(42, "الشورى", "Ash-Shuraa", "The Consultation", 53, true, 25, 483),
        SurahInfo(43, "الزخرف", "Az-Zukhruf", "The Ornaments of Gold", 89, true, 25, 489),
        SurahInfo(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, true, 25, 496),
        SurahInfo(45, "الجاثية", "Al-Jathiyah", "The Crouching", 37, true, 25, 499),
        SurahInfo(46, "الأحقاف", "Al-Ahqaf", "The Wind-Curved Sandhills", 35, true, 26, 502),
        SurahInfo(47, "محمد", "Muhammad", "Muhammad", 38, false, 26, 507),
        SurahInfo(48, "الفتح", "Al-Fath", "The Victory", 29, false, 26, 511),
        SurahInfo(49, "الحجرات", "Al-Hujurat", "The Rooms", 18, false, 26, 515),
        SurahInfo(50, "ق", "Qaf", "The Letter Qaf", 45, true, 26, 518),
        SurahInfo(51, "الذاريات", "Adh-Dhariyat", "The Winnowing Winds", 60, true, 26, 520),
        SurahInfo(52, "الطور", "At-Tur", "The Mount", 49, true, 27, 523),
        SurahInfo(53, "النجم", "An-Najm", "The Star", 62, true, 27, 526),
        SurahInfo(54, "القمر", "Al-Qamar", "The Moon", 55, true, 27, 528),
        SurahInfo(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, false, 27, 531),
        SurahInfo(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, true, 27, 534),
        SurahInfo(57, "الحديد", "Al-Hadid", "The Iron", 29, false, 27, 537),
        SurahInfo(58, "المجادلة", "Al-Mujadila", "The Pleading Woman", 22, false, 28, 542),
        SurahInfo(59, "الحشر", "Al-Hashr", "The Exile", 24, false, 28, 545),
        SurahInfo(60, "الممتحنة", "Al-Mumtahanah", "She that is to be examined", 13, false, 28, 549),
        SurahInfo(61, "الصف", "As-Saf", "The Ranks", 14, false, 28, 551),
        SurahInfo(62, "الجمعة", "Al-Jumu'ah", "The Congregation, Friday", 11, false, 28, 553),
        SurahInfo(63, "المنافقون", "Al-Munafiqun", "The Hypocrites", 11, false, 28, 554),
        SurahInfo(64, "التغابن", "At-Taghabun", "The Mutual Disillusion", 18, false, 28, 556),
        SurahInfo(65, "الطلاق", "At-Talaq", "The Divorce", 12, false, 28, 558),
        SurahInfo(66, "التحريم", "At-Tahrim", "The Prohibition", 12, false, 28, 560),
        SurahInfo(67, "الملك", "Al-Mulk", "The Sovereignty", 30, true, 29, 562),
        SurahInfo(68, "القلم", "Al-Qalam", "The Pen", 52, true, 29, 564),
        SurahInfo(69, "الحاقة", "Al-Haqqah", "The Reality", 52, true, 29, 566),
        SurahInfo(70, "المعارج", "Al-Ma'arij", "The Ascending Stairways", 44, true, 29, 568),
        SurahInfo(71, "نوح", "Nuh", "Noah", 28, true, 29, 570),
        SurahInfo(72, "الجن", "Al-Jinn", "The Jinn", 28, true, 29, 572),
        SurahInfo(73, "المزمل", "Al-Muzzammil", "The Enshrouded One", 20, true, 29, 574),
        SurahInfo(74, "المدثر", "Al-Muddaththir", "The Cloaked One", 56, true, 29, 575),
        SurahInfo(75, "القيامة", "Al-Qiyamah", "The Resurrection", 40, true, 29, 577),
        SurahInfo(76, "الإنسان", "Al-Insan", "The Man", 31, false, 29, 578),
        SurahInfo(77, "المرسلات", "Al-Mursalat", "The Emissaries", 50, true, 29, 580),
        SurahInfo(78, "النبأ", "An-Naba", "The Tidings", 40, true, 30, 582),
        SurahInfo(79, "النازعات", "An-Nazi'at", "Those who drag forth", 46, true, 30, 583),
        SurahInfo(80, "عبس", "'Abasa", "He Frowned", 42, true, 30, 585),
        SurahInfo(81, "التكوير", "At-Takwir", "The Overthrowing", 29, true, 30, 586),
        SurahInfo(82, "الانفطار", "Al-Infitar", "The Cleaving", 19, true, 30, 587),
        SurahInfo(83, "المطففين", "Al-Mutaffifin", "The Defrauding", 36, true, 30, 587),
        SurahInfo(84, "الانشقاق", "Al-Inshiqaq", "The Sundering", 25, true, 30, 589),
        SurahInfo(85, "البروج", "Al-Buruj", "The Mansions of the Stars", 22, true, 30, 590),
        SurahInfo(86, "الطارق", "At-Tariq", "The Nightcommer", 17, true, 30, 591),
        SurahInfo(87, "الأعلى", "Al-A'la", "The Most High", 19, true, 30, 591),
        SurahInfo(88, "الغاشية", "Al-Ghashiyah", "The Overwhelming", 26, true, 30, 592),
        SurahInfo(89, "الفجر", "Al-Fajr", "The Dawn", 30, true, 30, 593),
        SurahInfo(90, "البلد", "Al-Balad", "The City", 20, true, 30, 594),
        SurahInfo(91, "الشمس", "Ash-Shams", "The Sun", 15, true, 30, 595),
        SurahInfo(92, "الليل", "Al-Layl", "The Night", 21, true, 30, 595),
        SurahInfo(93, "الضحى", "Ad-Duhaa", "The Morning Hours", 11, true, 30, 596),
        SurahInfo(94, "الشرح", "Ash-Sharh", "The Relief", 8, true, 30, 596),
        SurahInfo(95, "التين", "At-Tin", "The Fig", 8, true, 30, 597),
        SurahInfo(96, "العلق", "Al-'Alaq", "The Clot", 19, true, 30, 597),
        SurahInfo(97, "القدر", "Al-Qadr", "The Power", 5, true, 30, 598),
        SurahInfo(98, "البينة", "Al-Bayyinah", "The Clear Proof", 8, false, 30, 598),
        SurahInfo(99, "الزلزلة", "Az-Zalzalah", "The Earthquake", 8, false, 30, 599),
        SurahInfo(100, "العاديات", "Al-'Adiyat", "The Courser", 11, true, 30, 599),
        SurahInfo(101, "القارعة", "Al-Qari'ah", "The Calamity", 11, true, 30, 600),
        SurahInfo(102, "التكاثر", "At-Takathur", "The Rivalry in world increase", 8, true, 30, 600),
        SurahInfo(103, "العصر", "Al-'Asr", "The Declining Day", 3, true, 30, 601),
        SurahInfo(104, "الهمزة", "Al-Humazah", "The Traducer", 9, true, 30, 601),
        SurahInfo(105, "الفيل", "Al-Fil", "The Elephant", 5, true, 30, 601),
        SurahInfo(106, "قريش", "Quraysh", "Quraysh", 4, true, 30, 602),
        SurahInfo(107, "الماعون", "Al-Ma'un", "The Small kindnesses", 7, true, 30, 602),
        SurahInfo(108, "الكوثر", "Al-Kawthar", "The Abundance", 3, true, 30, 602),
        SurahInfo(109, "الكافرون", "Al-Kafirun", "The Disbelievers", 6, true, 30, 603),
        SurahInfo(110, "النصر", "An-Nasr", "The Divine Support", 3, false, 30, 603),
        SurahInfo(111, "المسد", "Al-Masad", "The Palm Fiber", 5, true, 30, 603),
        SurahInfo(112, "الإخلاص", "Al-Ikhlas", "The Sincerity", 4, true, 30, 604),
        SurahInfo(113, "الفلق", "Al-Falaq", "The Daybreak", 5, true, 30, 604),
        SurahInfo(114, "الناس", "An-Nas", "Mankind", 6, true, 30, 604)
    )

    val juzList: List<JuzInfo> = listOf(
        JuzInfo(1, "الم", "Alif Lam Meem", 1, 1, 1),
        JuzInfo(2, "سيقول", "Sayaqool", 2, 142, 22),
        JuzInfo(3, "تلك الرسل", "Tilkal Rusul", 2, 253, 42),
        JuzInfo(4, "لن تنالوا", "Lan Tanaloo", 3, 93, 62),
        JuzInfo(5, "والمحصنات", "Wal Mohsanat", 4, 24, 82),
        JuzInfo(6, "لا يحب الله", "La Yuhibbullah", 4, 148, 102),
        JuzInfo(7, "وإذا سمعوا", "Wa Iza Samiu", 5, 82, 121),
        JuzInfo(8, "ولو أننا", "Wa Lau Annana", 6, 111, 142),
        JuzInfo(9, "قال الملأ", "Qalal Malao", 7, 88, 162),
        JuzInfo(10, "واعلموا", "Wa A'lamu", 8, 41, 182),
        JuzInfo(11, "يعتذرون", "Yatazeroon", 9, 93, 201),
        JuzInfo(12, "وما من دابة", "Wa Mamin Da'abat", 11, 6, 222),
        JuzInfo(13, "وما أبرئ", "Wa Ma Ubrioo", 12, 53, 242),
        JuzInfo(14, "ربما", "Rubama", 15, 1, 262),
        JuzInfo(15, "سبحان الذي", "Subhanallazi", 17, 1, 282),
        JuzInfo(16, "قال ألم", "Qal Alam", 18, 75, 302),
        JuzInfo(17, "اقترب للناس", "Aqtarabo", 21, 1, 322),
        JuzInfo(18, "قد أفلح", "Qadd Aflaha", 23, 1, 342),
        JuzInfo(19, "وقال الذين", "Wa Qalallazina", 25, 21, 362),
        JuzInfo(20, "أمن خلق", "A'man Khalaq", 27, 56, 382),
        JuzInfo(21, "اتل ما أوحي", "Utlu Ma Oohi", 29, 46, 402),
        JuzInfo(22, "ومن يقنت", "Wa Manyaqnut", 33, 31, 422),
        JuzInfo(23, "ومالي", "Wa Mali", 36, 28, 442),
        JuzInfo(24, "فمن أظلم", "Faman Azlam", 39, 32, 462),
        JuzInfo(25, "إليه يرد", "Elahe Yuraddo", 41, 47, 482),
        JuzInfo(26, "حم", "Ha'a Meem", 46, 1, 502),
        JuzInfo(27, "قال فما خطبكم", "Qala Fama Khatbukum", 51, 31, 522),
        JuzInfo(28, "قد سمع الله", "Qadd Sami Allah", 58, 1, 542),
        JuzInfo(29, "تبارك الذي", "Tabarakallazi", 67, 1, 562),
        JuzInfo(30, "عم", "Amma Yatasa'aloon", 78, 1, 582)
    )

    val hizbList: List<HizbInfo> = (1..60).map { hizbNum ->
        val juzNum = ((hizbNum - 1) / 2) + 1
        val juz = juzList[juzNum - 1]
        val isSecondHalf = (hizbNum % 2 == 0)
        val approxSurah = if (!isSecondHalf) juz.startSurah else {
            val nextStart = juzList.getOrNull(juzNum)?.startSurah ?: 114
            ((juz.startSurah + nextStart) / 2).coerceIn(juz.startSurah, 114)
        }
        val surahNameAr = surahs[approxSurah - 1].arabicName
        HizbInfo(
            number = hizbNum,
            juzNumber = juzNum,
            startSurah = approxSurah,
            startAyah = if (!isSecondHalf) juz.startAyah else 1,
            titleAr = "الحزب $hizbNum • الجزء $juzNum (سورة $surahNameAr)",
            titleEn = "Hizb $hizbNum • Juz $juzNum (${surahs[approxSurah - 1].englishName})"
        )
    }

    // Verified offline starter Surahs (100% authentic Uthmani text + Sahih International + Tafsir Al-Muyassar)
    val offlineStarterAyahs: List<CachedSurahAyahEntity> = listOf(
        // Surah 1: Al-Fatihah
        CachedSurahAyahEntity("1_1", 1, 1, 1, 1, 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "أبتدئ قراءة القرآن باسم الله مستعيناً به، (اللهِ) علم على الرب تبارك وتعالى المعبود بحق دون سواه، (الرَّحْمَٰنِ) ذي الرحمة العامة الذي وسعت رحمته جميع الخلق، (الرَّحِيمِ) بالمؤمنين."),
        CachedSurahAyahEntity("1_2", 1, 2, 2, 1, 1, 1, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "[All] praise is [due] to Allah, Lord of the worlds -", "الثناء على الله بصفاته التي كلُّها أوصاف كمال، وبنعمه الظاهرة والباطنة، الدينية والدنيوية، وهو سبحانه المربي جميع العالمين بتدبيره ونعمه."),
        CachedSurahAyahEntity("1_3", 1, 3, 3, 1, 1, 1, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful,", "الرحمن الذي وسعت رحمته جميع الخلق، الرحيم بالمؤمنين."),
        CachedSurahAyahEntity("1_4", 1, 4, 4, 1, 1, 1, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "وهو سبحانه وحده مالك يوم القيامة، وهو يوم الجزاء على الأعمال."),
        CachedSurahAyahEntity("1_5", 1, 5, 5, 1, 1, 1, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "إنا نخصك وحدك بالعبادة، ونستعين بك وحدك في جميع أمورنا."),
        CachedSurahAyahEntity("1_6", 1, 6, 6, 1, 1, 1, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path -", "دلنا وأرشدنا وثبتنا على الطريق المستقيم، وهو الإسلام."),
        CachedSurahAyahEntity("1_7", 1, 7, 7, 1, 1, 1, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked [Your] anger or of those who are astray.", "طريق الذين أنعمت عليهم من النبيين والصديقين والشهداء والصالحين، غير طريق المغضوب عليهم ولا طريق الضالين."),
        // Surah 103: Al-Asr
        CachedSurahAyahEntity("103_1", 103, 1, 6177, 30, 240, 601, "وَالْعَصْرِ", "By time,", "أقسم الله تعالى بالدهر على أن بني آدم في خسارة ونقصان."),
        CachedSurahAyahEntity("103_2", 103, 2, 6178, 30, 240, 601, "إِنَّ الْإِنسَانَ لَفِي خُسْرٍ", "Indeed, mankind is in loss,", "إن الإنسان لفي هلاك وخسران."),
        CachedSurahAyahEntity("103_3", 103, 3, 6179, 30, 240, 601, "إِلَّا الَّذِينَ آمَنُوا وَعَمِلُوا الصَّالِحَاتِ وَتَوَاصَوْا بِالْحَقِّ وَتَوَاصَوْا بِالصَّبْرِ", "Except for those who have believed and done righteous deeds and advised each other to truth and advised each other to patience.", "إلا الذين آمنوا بالله وعملوا الأعمال الصالحة، وأوصى بعضهم بعضاً بالتمسك بالحق والصبر على طاعة الله."),
        // Surah 112: Al-Ikhlas
        CachedSurahAyahEntity("112_1", 112, 1, 6222, 30, 240, 604, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, \"He is Allah, [who is] One,", "قل أيها الرسول: هو الله المتفرد بالألوهية والربوبية والأسماء والصفات."),
        CachedSurahAyahEntity("112_2", 112, 2, 6223, 30, 240, 604, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "الله المقصود في قضاء الحوائج والرغائب على الدوام."),
        CachedSurahAyahEntity("112_3", 112, 3, 6224, 30, 240, 604, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born,", "ليس له ولد ولا والد ولا صاحبة."),
        CachedSurahAyahEntity("112_4", 112, 4, 6225, 30, 240, 604, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.\"", "ولم يكن له شبيه ولا مثيل في أسمائه ولا صفاته ولا أفعاله."),
        // Surah 113: Al-Falaq
        CachedSurahAyahEntity("113_1", 113, 1, 6226, 30, 240, 604, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, \"I seek refuge in the Lord of daybreak", "قل: ألتجئ وأعتصم برب الصبح."),
        CachedSurahAyahEntity("113_2", 113, 2, 6227, 30, 240, 604, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created", "من شر جميع المخلوقات وأذاها."),
        CachedSurahAyahEntity("113_3", 113, 3, 6228, 30, 240, 604, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles", "ومن شر ليل شديد الظلمة إذا دخل وتغلغل."),
        CachedSurahAyahEntity("113_4", 113, 4, 6229, 30, 240, 604, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots", "ومن شر الساحرات اللاتي ينفثن في ما يعقدنه بقصد السحر."),
        CachedSurahAyahEntity("113_5", 113, 5, 6230, 30, 240, 604, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.\"", "ومن شر حاسد مبغض للناس يتمنى زوال النعمة عنهم إذا أظهر حسده."),
        // Surah 114: An-Nas
        CachedSurahAyahEntity("114_1", 114, 1, 6231, 30, 240, 604, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, \"I seek refuge in the Lord of mankind,", "قل: أعتصم وأستجير برب الناس القادر وحده على رد شر الوسواس."),
        CachedSurahAyahEntity("114_2", 114, 2, 6232, 30, 240, 604, "مَلِكِ النَّاسِ", "The Sovereign of mankind.", "ملك الناس المتصرف في كل شؤونهم."),
        CachedSurahAyahEntity("114_3", 114, 3, 6233, 30, 240, 604, "إِلَٰهِ النَّاسِ", "The God of mankind,", "إله الناس الذي لا معبود بحق سواهم غيره."),
        CachedSurahAyahEntity("114_4", 114, 4, 6234, 30, 240, 604, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer -", "من أذى الشيطان الذي يوسوس عند الغفلة ويختفي عند ذكر الله."),
        CachedSurahAyahEntity("114_5", 114, 5, 6235, 30, 240, 604, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers [evil] into the breasts of mankind -", "الذي يبث الشر والشكوك في صدور الناس."),
        CachedSurahAyahEntity("114_6", 114, 6, 6236, 30, 240, 604, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.\"", "من شياطين الجن وشياطين الإنس.")
    )

    val dailyAyahs: List<DailyAyah> = listOf(
        DailyAyah(2, 286, "البقرة", "Al-Baqarah", "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا", "Allah does not charge a soul except [with that within] its capacity."),
        DailyAyah(94, 5, "الشرح", "Ash-Sharh", "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "For indeed, with hardship [will be] ease."),
        DailyAyah(13, 28, "الرعد", "Ar-Ra'd", "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "Unquestionably, by the remembrance of Allah hearts are assured."),
        DailyAyah(2, 152, "البقرة", "Al-Baqarah", "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ", "So remember Me; I will remember you. And be grateful to Me and do not deny Me."),
        DailyAyah(65, 3, "الطلاق", "At-Talaq", "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ", "And whoever relies upon Allah - then He is sufficient for him."),
        DailyAyah(39, 53, "الزمر", "Az-Zumar", "لَا تَقْنَطُوا مِن رَّحْمَةِ اللَّهِ ۚ إِنَّ اللَّهَ يَغْفِرُ الذُّنُوبَ جَمِيعًا", "Do not despair of the mercy of Allah. Indeed, Allah forgives all sins."),
        DailyAyah(3, 139, "آل عمران", "Ali 'Imran", "وَلَا تَهِنُوا وَلَا تَحْزَنُوا وَأَنتُمُ الْأَعْلَوْنَ إِن كُنتُم مُّؤْمِنِينَ", "So do not weaken and do not grieve, and you will be superior if you are [true] believers.")
    )

    fun ayahOfTheDay(dayOfYear: Int): DailyAyah = dailyAyahs[dayOfYear.mod(dailyAyahs.size)]

    fun globalAyahNumber(surahNumber: Int, ayahNumber: Int): Int {
        if (surahNumber !in 1..114) return 1
        val offset = surahs.take(surahNumber - 1).sumOf { it.ayahCount }
        val maxAyah = surahs[surahNumber - 1].ayahCount
        return offset + ayahNumber.coerceIn(1, maxAyah)
    }
}

