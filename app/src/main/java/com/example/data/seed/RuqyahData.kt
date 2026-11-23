package com.example.data.seed

enum class RuqyahCategory(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String
) {
    COMPREHENSIVE(
        id = "comprehensive",
        titleAr = "الرقية الشاملة",
        titleEn = "Comprehensive",
        subtitleAr = "الرقية الجامعة من القرآن والسنة",
        subtitleEn = "Complete Ruqyah from Quran & Sunnah"
    ),
    QURAN(
        id = "quran",
        titleAr = "من القرآن الكريم",
        titleEn = "From Quran",
        subtitleAr = "سور وآيات التحصين بالرسم العثماني",
        subtitleEn = "Authentic protective Quranic verses"
    ),
    EVIL_EYE(
        id = "evil_eye",
        titleAr = "العين والحسد",
        titleEn = "Evil Eye & Envy",
        subtitleAr = "آيات وأدعية التحصين من العين والحسد",
        subtitleEn = "Protection against envy and the evil eye"
    ),
    MAGIC(
        id = "magic",
        titleAr = "رقية السحر",
        titleEn = "Against Sihr",
        subtitleAr = "آيات إبطال السحر وثبات المؤمن",
        subtitleEn = "Quranic verses affirming truth and nullifying magic"
    ),
    HARM(
        id = "harm",
        titleAr = "رقية المس",
        titleEn = "Harm & Affliction",
        subtitleAr = "آيات الحفظ والطرد والاعتصام بالله",
        subtitleEn = "Verses of divine refuge and protection from harm"
    ),
    WASWAS(
        id = "waswas",
        titleAr = "الوسواس والخوف",
        titleEn = "Waswas & Fear",
        subtitleAr = "آيات السكينة وطمأنينة القلب",
        subtitleEn = "Verses of tranquility, serenity, and peace of heart"
    ),
    CHILDREN(
        id = "children",
        titleAr = "رقية الأطفال",
        titleEn = "For Children",
        subtitleAr = "المعوذات وتعويذ النبي ﷺ للحسن والحسين",
        subtitleEn = "Prophetic protection invocations for children"
    ),
    SUNNAH(
        id = "sunnah",
        titleAr = "من السنة النبوية",
        titleEn = "From Sunnah",
        subtitleAr = "أدعية الرقية الصحيحة من البخاري ومسلم",
        subtitleEn = "Authentic Prophetic Duas from Sahih collections"
    ),
    SHIFA(
        id = "shifa",
        titleAr = "آيات الشفاء",
        titleEn = "Healing Verses",
        subtitleAr = "آيات الشفاء الست من القرآن الكريم",
        subtitleEn = "The six Quranic verses of spiritual & physical healing"
    ),
    BAQARAH(
        id = "baqarah",
        titleAr = "سورة البقرة كاملة",
        titleEn = "Surah Al-Baqarah",
        subtitleAr = "تلاوة سورة البقرة كاملة — أخذها بركة",
        subtitleEn = "Complete Surah Al-Baqarah — protection & blessing"
    )
}

data class RuqyahReciterProfile(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val roleAr: String,
    val roleEn: String,
    val monogram: String,
    val cdnEdition: String?,
    val mp3QuranKeywords: List<String>,
    val attributionAr: String,
    val attributionEn: String
)

data class RuqyahVerseRef(
    val id: String,
    val surahNumber: Int, // 0 for Prophetic Hadith / Dua (uses text / TTS)
    val ayahNumber: Int,
    val arabicText: String,
    val englishText: String,
    val sourceAr: String,
    val sourceEn: String,
    val benefitAr: String = "",
    val benefitEn: String = "",
    val repeatRecommended: Int = 1
) {
    val globalAyahNumber: Int
        get() = if (surahNumber in 1..114 && ayahNumber >= 1) {
            QuranCatalog.globalAyahNumber(surahNumber, ayahNumber)
        } else 0

    val isQuranAyah: Boolean
        get() = surahNumber in 1..114 && ayahNumber >= 1
}

sealed interface RuqyahAudioSource {
    data class ArchiveItem(val identifier: String) : RuqyahAudioSource
    data class SyncedQuranSequence(val ayahRefs: List<RuqyahVerseRef>) : RuqyahAudioSource
    data class FullSurahMp3Quran(val surahNumber: Int = 2) : RuqyahAudioSource
}

data class RuqyahAudioTrack(
    val id: String,
    val category: RuqyahCategory,
    val titleAr: String,
    val titleEn: String,
    val reciterId: String,
    val reciterAr: String,
    val reciterEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val durationLabelAr: String,
    val durationLabelEn: String,
    val audioSource: RuqyahAudioSource,
    val supportsReciterSelection: Boolean,
    val supportsLiveTextSync: Boolean,
    val sourceAttributionAr: String,
    val sourceAttributionEn: String,
    val associatedTexts: List<RuqyahVerseRef>
) {
    val archiveItemId: String
        get() = (audioSource as? RuqyahAudioSource.ArchiveItem)?.identifier ?: ""
}

object RuqyahCatalog {

    val cdnReciters: List<RuqyahReciterProfile> = listOf(
        RuqyahReciterProfile(
            id = "alafasy",
            nameAr = "الشيخ مشاري راشد العفاسي",
            nameEn = "Sheikh Mishary Rashid Alafasy",
            roleAr = "إمام وقارئ من الكويت",
            roleEn = "Imam & Renowned Reciter",
            monogram = "م ع",
            cdnEdition = "ar.alafasy",
            mp3QuranKeywords = listOf("مشاري العفاسي", "مشاري راشد العفاسي", "Alafasy"),
            attributionAr = "شبكة القرآن الإسلامية (ar.alafasy) وMP3Quran",
            attributionEn = "Islamic Network CDN (ar.alafasy) & MP3Quran"
        ),
        RuqyahReciterProfile(
            id = "hudhaify",
            nameAr = "الشيخ علي بن عبدالرحمن الحذيفي",
            nameEn = "Sheikh Ali Al-Hudhaify",
            roleAr = "إمام وخطيب المسجد النبوي الشريف",
            roleEn = "Imam of Al-Masjid an-Nabawi",
            monogram = "ع ح",
            cdnEdition = "ar.hudhaify",
            mp3QuranKeywords = listOf("علي الحذيفي", "الحذيفي", "Hudhaify"),
            attributionAr = "شبكة القرآن الإسلامية (ar.hudhaify) وMP3Quran",
            attributionEn = "Islamic Network CDN (ar.hudhaify) & MP3Quran"
        ),
        RuqyahReciterProfile(
            id = "husary",
            nameAr = "الشيخ محمود خليل الحصري",
            nameEn = "Sheikh Mahmoud Khalil Al-Husary",
            roleAr = "شيخ عموم المقارئ المصرية",
            roleEn = "Grand Reciter (Murattal Hafs)",
            monogram = "م ح",
            cdnEdition = "ar.husary",
            mp3QuranKeywords = listOf("الحصري", "محمود خليل الحصري", "Husary"),
            attributionAr = "شبكة القرآن الإسلامية (ar.husary) وMP3Quran",
            attributionEn = "Islamic Network CDN (ar.husary) & MP3Quran"
        )
    )

    // --- Curated Verified Quranic Verses (Uthmani text + accurate Surah:Ayah citations) ---

    private val verseFatihah1 = RuqyahVerseRef(
        id = "q_1_1", surahNumber = 1, ayahNumber = 1,
        arabicText = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾ الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ ﴿٢﴾ الرَّحْمَٰنِ الرَّحِيمِ ﴿٣﴾ مَالِكِ يَوْمِ الدِّينِ ﴿٤﴾ إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ ﴿٥﴾ اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ ﴿٦﴾ صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ",
        englishText = "Surah Al-Fatihah (1:1-7): In the name of Allah, the Entirely Merciful, the Especially Merciful...",
        sourceAr = "سورة الفاتحة (1-7) • صحيح البخاري (5736)",
        sourceEn = "Quran 1:1-7 • Sahih al-Bukhari (5736)",
        benefitAr = "أعظم سورة في القرآن وهي الشافية والكافية والرقية الشرعية الثابتة في الصحيح.",
        benefitEn = "The Opening Surah; affirmed in Sahih al-Bukhari as an authentic healing Ruqyah."
    )

    private val fatihahAyahList = listOf(
        RuqyahVerseRef("v_1_1", 1, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "In the name of Allah, the Entirely Merciful, the Especially Merciful.", "سورة الفاتحة: 1", "Al-Fatihah 1:1"),
        RuqyahVerseRef("v_1_2", 1, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "All praise is due to Allah, Lord of the worlds.", "سورة الفاتحة: 2", "Al-Fatihah 1:2"),
        RuqyahVerseRef("v_1_3", 1, 3, "الرَّحْمَٰنِ الرَّحِيمِ", "The Entirely Merciful, the Especially Merciful.", "سورة الفاتحة: 3", "Al-Fatihah 1:3"),
        RuqyahVerseRef("v_1_4", 1, 4, "مَالِكِ يَوْمِ الدِّينِ", "Sovereign of the Day of Recompense.", "سورة الفاتحة: 4", "Al-Fatihah 1:4"),
        RuqyahVerseRef("v_1_5", 1, 5, "إِيَّاكَ نَعْبُدُ وَإِيَّاكَ نَسْتَعِينُ", "It is You we worship and You we ask for help.", "سورة الفاتحة: 5", "Al-Fatihah 1:5"),
        RuqyahVerseRef("v_1_6", 1, 6, "اهْدِنَا الصِّرَاطَ الْمُسْتَقِيمَ", "Guide us to the straight path.", "سورة الفاتحة: 6", "Al-Fatihah 1:6"),
        RuqyahVerseRef("v_1_7", 1, 7, "صِرَاطَ الَّذِينَ أَنْعَمْتَ عَلَيْهِمْ غَيْرِ الْمَغْضُوبِ عَلَيْهِمْ وَلَا الضَّالِّينَ", "The path of those upon whom You have bestowed favor, not of those who have evoked Your anger or of those who are astray.", "سورة الفاتحة: 7", "Al-Fatihah 1:7")
    )

    private val ayatKursi = RuqyahVerseRef(
        id = "v_2_255", surahNumber = 2, ayahNumber = 255,
        arabicText = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ ۚ لَا تَأْخُذُهُ سِنَةٌ وَلَا نَوْمٌ ۚ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الْأَرْضِ ۗ مَن ذَا الَّذِي يَشْفَعُ عِندَهُ إِلَّا بِإِذْنِهِ ۚ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ ۖ وَلَا يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلَّا بِمَا شَاءَ ۚ وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالْأَرْضَ ۖ وَلَا يَئُودُهُ حِفْظُهُمَا ۚ وَهُوَ الْعَلِيُّ الْعَظِيمُ",
        englishText = "Ayat al-Kursi (2:255): Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence...",
        sourceAr = "سورة البقرة: 255 (آية الكرسي) • صحيح البخاري (2311)",
        sourceEn = "Quran 2:255 (Ayat al-Kursi) • Sahih al-Bukhari (2311)",
        benefitAr = "أعظم آية في كتاب الله، من قرأها في ليلة لم يزل عليه من الله حافظ ولا يقربه شيطان.",
        benefitEn = "The greatest verse in the Quran; grants continuous divine protection."
    )

    private val baqarah256 = RuqyahVerseRef(
        id = "v_2_256", surahNumber = 2, ayahNumber = 256,
        arabicText = "لَا إِكْرَاهَ فِي الدِّينِ ۖ قَد تَّبَيَّنَ الرُّشْدُ مِنَ الْغَيِّ ۚ فَمَن يَكْفُرْ بِالطَّاغُوتِ وَيُؤْمِن بِاللَّهِ فَقَدِ اسْتَمْسَكَ بِالْعُرْوَةِ الْوُثْقَىٰ لَا انفِصَامَ لَهَا ۗ وَاللَّهُ سَمِيعٌ عَلِيمٌ",
        englishText = "Quran 2:256: Whoever disbelieves in false deities and believes in Allah has grasped the most trustworthy handhold.",
        sourceAr = "سورة البقرة: 256",
        sourceEn = "Quran 2:256"
    )

    private val baqarah257 = RuqyahVerseRef(
        id = "v_2_257", surahNumber = 2, ayahNumber = 257,
        arabicText = "اللَّهُ وَلِيُّ الَّذِينَ آمَنُوا يُخْرِجُهُم مِّنَ الظُّلُمَاتِ إِلَى النُّورِ",
        englishText = "Quran 2:257: Allah is the ally of those who believe. He brings them out from darknesses into the light.",
        sourceAr = "سورة البقرة: 257",
        sourceEn = "Quran 2:257"
    )

    private val baqarahEnd285 = RuqyahVerseRef(
        id = "v_2_285", surahNumber = 2, ayahNumber = 285,
        arabicText = "آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ مِن رَّبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِّن رُّسُلِهِ ۚ وَقَالُوا سَمِعْنَا وَأَطَعْنَا ۖ غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ",
        englishText = "Quran 2:285: The Messenger has believed in what was revealed to him from his Lord, and so have the believers...",
        sourceAr = "سورة البقرة: 285 • صحيح البخاري (5009)",
        sourceEn = "Quran 2:285 • Sahih al-Bukhari (5009)"
    )

    private val baqarahEnd286 = RuqyahVerseRef(
        id = "v_2_286", surahNumber = 2, ayahNumber = 286,
        arabicText = "لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا ۚ لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ ۗ رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا ۚ رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِن قَبْلِنَا ۚ رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ ۖ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا ۚ أَنتَ مَوْلَانَا فَانصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ",
        englishText = "Quran 2:286: Allah does not charge a soul except [with that within] its capacity...",
        sourceAr = "سورة البقرة: 286 • صحيح البخاري (5009)",
        sourceEn = "Quran 2:286 • Sahih al-Bukhari (5009)",
        benefitAr = "الآيتان من آخر سورة البقرة من قرأهما في ليلة كفتاه.",
        benefitEn = "Whoever recites the last two verses of Al-Baqarah at night, they will suffice him."
    )

    private val muawwidhatAyahs = listOf(
        RuqyahVerseRef("v_112_1", 112, 1, "قُلْ هُوَ اللَّهُ أَحَدٌ", "Say, He is Allah, Who is One.", "سورة الإخلاص: 1", "Al-Ikhlas 112:1"),
        RuqyahVerseRef("v_112_2", 112, 2, "اللَّهُ الصَّمَدُ", "Allah, the Eternal Refuge.", "سورة الإخلاص: 2", "Al-Ikhlas 112:2"),
        RuqyahVerseRef("v_112_3", 112, 3, "لَمْ يَلِدْ وَلَمْ يُولَدْ", "He neither begets nor is born.", "سورة الإخلاص: 3", "Al-Ikhlas 112:3"),
        RuqyahVerseRef("v_112_4", 112, 4, "وَلَمْ يَكُن لَّهُ كُفُوًا أَحَدٌ", "Nor is there to Him any equivalent.", "سورة الإخلاص: 4", "Al-Ikhlas 112:4"),
        RuqyahVerseRef("v_113_1", 113, 1, "قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ", "Say, I seek refuge in the Lord of daybreak.", "سورة الفلق: 1", "Al-Falaq 113:1"),
        RuqyahVerseRef("v_113_2", 113, 2, "مِن شَرِّ مَا خَلَقَ", "From the evil of that which He created.", "سورة الفلق: 2", "Al-Falaq 113:2"),
        RuqyahVerseRef("v_113_3", 113, 3, "وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ", "And from the evil of darkness when it settles.", "سورة الفلق: 3", "Al-Falaq 113:3"),
        RuqyahVerseRef("v_113_4", 113, 4, "وَمِن شَرِّ النَّفَّاثَاتِ فِي الْعُقَدِ", "And from the evil of the blowers in knots.", "سورة الفلق: 4", "Al-Falaq 113:4"),
        RuqyahVerseRef("v_113_5", 113, 5, "وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ", "And from the evil of an envier when he envies.", "سورة الفلق: 5", "Al-Falaq 113:5"),
        RuqyahVerseRef("v_114_1", 114, 1, "قُلْ أَعُوذُ بِرَبِّ النَّاسِ", "Say, I seek refuge in the Lord of mankind.", "سورة الناس: 1", "An-Nas 114:1"),
        RuqyahVerseRef("v_114_2", 114, 2, "مَلِكِ النَّاسِ", "The Sovereign of mankind.", "سورة الناس: 2", "An-Nas 114:2"),
        RuqyahVerseRef("v_114_3", 114, 3, "إِلَٰهِ النَّاسِ", "The God of mankind.", "سورة الناس: 3", "An-Nas 114:3"),
        RuqyahVerseRef("v_114_4", 114, 4, "مِن شَرِّ الْوَسْوَاسِ الْخَنَّاسِ", "From the evil of the retreating whisperer.", "سورة الناس: 4", "An-Nas 114:4"),
        RuqyahVerseRef("v_114_5", 114, 5, "الَّذِي يُوَسْوِسُ فِي صُدُورِ النَّاسِ", "Who whispers into the breasts of mankind.", "سورة الناس: 5", "An-Nas 114:5"),
        RuqyahVerseRef("v_114_6", 114, 6, "مِنَ الْجِنَّةِ وَالنَّاسِ", "From among the jinn and mankind.", "سورة الناس: 6", "An-Nas 114:6")
    )

    private val evilEyeVerses = fatihahAyahList + listOf(
        RuqyahVerseRef("v_2_109", 2, 109, "وَدَّ كَثِيرٌ مِّنْ أَهْلِ الْكِتَابِ لَوْ يَرُدُّونَكُم مِّن بَعْدِ إِيمَانِكُمْ كُفَّارًا حَسَدًا مِّنْ عِندِ أَنفُسِهِم مِّن بَعْدِ مَا تَبَيَّنَ لَهُمُ الْحَقُّ ۖ فَاعْفُوا وَاصْفَحُوا حَتَّىٰ يَأْتِيَ اللَّهُ بِأَمْرِهِ ۗ إِنَّ اللَّهَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ", "Out of envy from themselves after the truth has become clear to them; so pardon and overlook until Allah delivers His command.", "سورة البقرة: 109", "Al-Baqarah 2:109"),
        RuqyahVerseRef("v_4_54", 4, 54, "أَمْ يَحْسُدُونَ النَّاسَ عَلَىٰ مَا آتَاهُمُ اللَّهُ مِن فَضْلِهِ ۖ فَقَدْ آتَيْنَا آلَ إِبْرَاهِيمَ الْكِتَابَ وَالْحِكْمَةَ وَآتَيْنَاهُم مُّلْكًا عَظِيمًا", "Or do they envy people for what Allah has given them of His bounty?", "سورة النساء: 54", "An-Nisa 4:54"),
        RuqyahVerseRef("v_18_39", 18, 39, "وَلَوْلَا إِذْ دَخَلْتَ جَنَّتَكَ قُلْتَ مَا شَاءَ اللَّهُ لَا قُوَّةَ إِلَّا بِاللَّهِ ۚ إِن تَرَنِ أَنَا أَقَلَّ مِنكَ مَالًا وَوَلَدًا", "Why did you not say: What Allah willed has occurred; there is no power except in Allah?", "سورة الكهف: 39", "Al-Kahf 18:39"),
        RuqyahVerseRef("v_67_3", 67, 3, "الَّذِي خَلَقَ سَبْعَ سَمَاوَاتٍ طِبَاقًا ۖ مَّا تَرَىٰ فِي خَلْقِ الرَّحْمَٰنِ مِن تَفَاوُتٍ ۖ فَارْجِعِ الْبَصَرَ هَلْ تَرَىٰ مِن فُطُورٍ", "Return your vision to the sky, do you see any breaks?", "سورة الملك: 3", "Al-Mulk 67:3"),
        RuqyahVerseRef("v_67_4", 67, 4, "ثُمَّ ارْجِعِ الْبَصَرَ كَرَّتَيْنِ يَنقَلِبْ إِلَيْكَ الْبَصَرُ خَاسِئًا وَهُوَ حَسِيرٌ", "Then return your vision twice again; your vision will return to you humbled while it is fatigued.", "سورة الملك: 4", "Al-Mulk 67:4"),
        RuqyahVerseRef("v_68_51", 68, 51, "وَإِن يَكَادُ الَّذِينَ كَفَرُوا لَيُزْلِقُونَكَ بِأَبْصَارِهِمْ لَمَّا سَمِعُوا الذِّكْرَ وَيَقُولُونَ إِنَّهُ لَمَجْنُونٌ", "And indeed, those who disbelieve would almost make you slip with their eyes when they hear the message.", "سورة القلم: 51", "Al-Qalam 68:51"),
        RuqyahVerseRef("v_68_52", 68, 52, "وَمَا هُوَ إِلَّا ذِكْرٌ لِّلْعَالَمِينَ", "But it is not except a reminder to the worlds.", "سورة القلم: 52", "Al-Qalam 68:52")
    ) + muawwidhatAyahs.filter { it.surahNumber == 113 }

    private val magicVerses = listOf(
        RuqyahVerseRef("v_7_117", 7, 117, "وَأَوْحَيْنَا إِلَىٰ مُوسَىٰ أَنْ أَلْقِ عَصَاكَ ۖ فَإِذَا هِيَ تَلْقَفُ مَا يَأْفِكُونَ", "Throw your staff, and at once it devoured what they were falsifying.", "سورة الأعراف: 117", "Al-A'raf 7:117"),
        RuqyahVerseRef("v_7_118", 7, 118, "فَوَقَعَ الْحَقُّ وَبَطَلَ مَا كَانُوا يَعْمَلُونَ", "So the truth was established, and abolished was what they were doing.", "سورة الأعراف: 118", "Al-A'raf 7:118"),
        RuqyahVerseRef("v_7_119", 7, 119, "فَغُلِبُوا هُنَالِكَ وَانقَلَبُوا صَاغِرِينَ", "And they were overcome right there and became debased.", "سورة الأعراف: 119", "Al-A'raf 7:119"),
        RuqyahVerseRef("v_10_81", 10, 81, "فَلَمَّا أَلْقَوْا قَالَ مُوسَىٰ مَا جِئْتُم بِهِ السِّحْرُ ۖ إِنَّ اللَّهَ سَيُبْطِلُهُ ۖ إِنَّ اللَّهَ لَا يُصْلِحُ عَمَلَ الْمُفْسِدِينَ", "Moses said: What you have brought is magic. Indeed, Allah will expose its worthlessness.", "سورة يونس: 81", "Yunus 10:81"),
        RuqyahVerseRef("v_10_82", 10, 82, "وَيُحِقُّ اللَّهُ الْحَقَّ بِكَلِمَاتِهِ وَلَوْ كَرِهَ الْمُجْرِمُونَ", "And Allah will establish the truth by His words, even if the criminals dislike it.", "سورة يونس: 82", "Yunus 10:82"),
        RuqyahVerseRef("v_20_69", 20, 69, "وَأَلْقِ مَا فِي يَمِينِكَ تَلْقَفْ مَا صَنَعُوا ۖ إِنَّمَا صَنَعُوا كَيْدُ سَاحِرٍ ۖ وَلَا يُفْلِحُ السَّاحِرُ حَيْثُ أَتَىٰ", "What they have made is but the trick of a magician, and the magician will not succeed wherever he is.", "سورة طه: 69", "Taha 20:69")
    )

    private val harmVerses = listOf(
        ayatKursi,
        baqarah256,
        baqarah257,
        RuqyahVerseRef("v_37_6", 37, 6, "إِنَّا زَيَّنَّا السَّمَاءَ الدُّنْيَا بِزِينَةٍ الْكَوَاكِبِ", "Indeed, We have adorned the nearest heaven with an adornment of stars.", "سورة الصافات: 6", "As-Saffat 37:6"),
        RuqyahVerseRef("v_37_7", 37, 7, "وَحِفْظًا مِّن كُلِّ شَيْطَانٍ مَّارِدٍ", "And as protection against every rebellious devil.", "سورة الصافات: 7", "As-Saffat 37:7"),
        RuqyahVerseRef("v_59_21", 59, 21, "لَوْ أَنزَلْنَا هَٰذَا الْقُرْآنَ عَلَىٰ جَبَلٍ لَّرَأَيْتَهُ خَاشِعًا مُّتَصَدِّعًا مِّنْ خَشْيَةِ اللَّهِ ۚ وَتِلْكَ الْأَمْثَالُ نَضْرِبُهَا لِلنَّاسِ لَعَلَّهُمْ يَتَفَكَّرُونَ", "If We had sent down this Quran upon a mountain, you would have seen it humbled and coming apart from fear of Allah.", "سورة الحشر: 21", "Al-Hashr 59:21"),
        RuqyahVerseRef("v_59_22", 59, 22, "هُوَ اللَّهُ الَّذِي لَا إِلَٰهَ إِلَّا هُوَ ۖ عَالِمُ الْغَيْبِ وَالشَّهَادَةِ ۖ هُوَ الرَّحْمَٰنُ الرَّحِيمُ", "He is Allah, other than whom there is no deity, Knower of the unseen and the witnessed.", "سورة الحشر: 22", "Al-Hashr 59:22")
    ) + muawwidhatAyahs.filter { it.surahNumber != 112 }

    private val waswasVerses = listOf(
        RuqyahVerseRef("v_9_40", 9, 40, "لَا تَحْزَنْ إِنَّ اللَّهَ مَعَنَا ۖ فَأَنزَلَ اللَّهُ سَكِينَتَهُ عَلَيْهِ وَأَيَّدَهُ بِجُنُودٍ لَّمْ تَرَوْهَا", "Do not grieve; indeed Allah is with us. And Allah sent down His tranquillity upon him.", "سورة التوبة: 40", "At-Tawbah 9:40"),
        RuqyahVerseRef("v_13_28", 13, 28, "الَّذِينَ آمَنُوا وَتَطْمَئِنُّ قُلُوبُهُم بِذِكْرِ اللَّهِ ۗ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ", "Unquestionably, by the remembrance of Allah hearts are assured.", "سورة الرعد: 28", "Ar-Ra'd 13:28"),
        RuqyahVerseRef("v_48_4", 48, 4, "هُوَ الَّذِي أَنزَلَ السَّكِينَةَ فِي قُلُوبِ الْمُؤْمِنِينَ لِيَزْدَادُوا إِيمَانًا مَّعَ إِيمَانِهِمْ", "It is He who sent down tranquillity into the hearts of the believers that they would increase in faith.", "سورة الفتح: 4", "Al-Fath 48:4"),
        RuqyahVerseRef("v_94_5", 94, 5, "فَإِنَّ مَعَ الْعُسْرِ يُسْرًا", "For indeed, with hardship will be ease.", "سورة الشرح: 5", "Ash-Sharh 94:5"),
        RuqyahVerseRef("v_94_6", 94, 6, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Indeed, with hardship will be ease.", "سورة الشرح: 6", "Ash-Sharh 94:6"),
        RuqyahVerseRef("v_106_4", 106, 4, "الَّذِي أَطْعَمَهُم مِّن جُوعٍ وَآمَنَهُم مِّنْ خَوْفٍ", "Who has fed them, saving them from hunger, and made them safe, saving them from fear.", "سورة قريش: 4", "Quraysh 106:4")
    ) + muawwidhatAyahs.filter { it.surahNumber == 114 }

    private val shifaSixVerses = listOf(
        RuqyahVerseRef("v_9_14", 9, 14, "وَيَشْفِ صُدُورَ قَوْمٍ مُّؤْمِنِينَ", "And heal the breasts of a believing people.", "سورة التوبة: 14", "At-Tawbah 9:14", "الآية الأولى من آيات الشفاء الست", "1st of the 6 Verses of Healing"),
        RuqyahVerseRef("v_10_57", 10, 57, "يَا أَيُّهَا النَّاسُ قَدْ جَاءَتْكُم مَّوْعِظَةٌ مِّن رَّبِّكُمْ وَشِفَاءٌ لِّمَا فِي الصُّدُورِ وَهُدًى وَرَحْمَةٌ لِّلْمُؤْمِنِينَ", "There has come to you instruction from your Lord and healing for what is in the breasts.", "سورة يونس: 57", "Yunus 10:57", "الآية الثانية من آيات الشفاء الست", "2nd of the 6 Verses of Healing"),
        RuqyahVerseRef("v_16_69", 16, 69, "يَخْرُجُ مِن بُطُونِهَا شَرَابٌ مُّخْتَلِفٌ أَلْوَانُهُ فِيهِ شِفَاءٌ لِّلنَّاسِ ۗ إِنَّ فِي ذَٰلِكَ لَآيَةً لِّقَوْمٍ يَتَفَكَّرُونَ", "There emerges from their bellies a drink, varying in colors, in which there is healing for people.", "سورة النحل: 69", "An-Nahl 16:69", "الآية الثالثة من آيات الشفاء الست", "3rd of the 6 Verses of Healing"),
        RuqyahVerseRef("v_17_82", 17, 82, "وَنُنَزِّلُ مِنَ الْقُرْآنِ مَا هُوَ شِفَاءٌ وَرَحْمَةٌ لِّلْمُؤْمِنِينَ ۙ وَلَا يَزِيدُ الظَّالِمِينَ إِلَّا خَسَارًا", "And We send down of the Quran that which is healing and mercy for the believers.", "سورة الإسراء: 82", "Al-Isra 17:82", "الآية الرابعة من آيات الشفاء الست", "4th of the 6 Verses of Healing"),
        RuqyahVerseRef("v_26_80", 26, 80, "وَإِذَا مَرِضْتُ فَهُوَ يَشْفِينِ", "And when I am ill, it is He who cures me.", "سورة الشعراء: 80", "Ash-Shu'ara 26:80", "الآية الخامسة من آيات الشفاء الست", "5th of the 6 Verses of Healing"),
        RuqyahVerseRef("v_41_44", 41, 44, "قُلْ هُوَ لِلَّذِينَ آمَنُوا هُدًى وَشِفَاءٌ", "Say, It is, for those who believe, a guidance and cure.", "سورة فصلت: 44", "Fussilat 41:44", "الآية السادسة من آيات الشفاء الست", "6th of the 6 Verses of Healing")
    )

    val sunnahPropheticDuas: List<RuqyahVerseRef> = listOf(
        RuqyahVerseRef(
            id = "s_b_5743", surahNumber = 0, ayahNumber = 0,
            arabicText = "أَذْهِبِ الْبَاسَ رَبَّ النَّاسِ، وَاشْفِ أَنْتَ الشَّافِي، لَا شِفَاءَ إِلَّا شِفَاؤُكَ، شِفَاءً لَا يُغَادِرُ سَقَمًا",
            englishText = "Remove the harm, O Lord of mankind, and heal; You are the Healer. There is no healing except Your healing, a healing that leaves behind no ailment.",
            sourceAr = "صحيح البخاري (5743) وصحيح مسلم (2191)",
            sourceEn = "Sahih al-Bukhari (5743) & Sahih Muslim (2191)",
            benefitAr = "كان النبي ﷺ يعوّذ به المريض ويمسح بيده اليمنى على موضع الألم.",
            benefitEn = "Recited while gently passing the right hand over the area of pain.",
            repeatRecommended = 1
        ),
        RuqyahVerseRef(
            id = "s_m_2186", surahNumber = 0, ayahNumber = 0,
            arabicText = "بِسْمِ اللَّهِ أَرْقِيكَ، مِنْ كُلِّ شَيْءٍ يُؤْذِيكَ، مِنْ شَرِّ كُلِّ نَفْسٍ أَوْ عَيْنِ حَاسِدٍ، اللَّهُ يَشْفِيكَ، بِسْمِ اللَّهِ أَرْقِيكَ",
            englishText = "In the name of Allah I perform Ruqyah for you, from everything that harms you, from the evil of every soul or envious eye; may Allah heal you.",
            sourceAr = "صحيح مسلم (2186)",
            sourceEn = "Sahih Muslim (2186)",
            benefitAr = "رقية جبريل عليه السلام للنبي ﷺ للشفاء والتحصين من العين والحسد.",
            benefitEn = "The Ruqyah with which Angel Jibril (AS) prayed for the Prophet ﷺ.",
            repeatRecommended = 3
        ),
        RuqyahVerseRef(
            id = "s_b_3371", surahNumber = 0, ayahNumber = 0,
            arabicText = "أُعِيذُكُمَا بِكَلِمَاتِ اللَّهِ التَّامَّةِ، مِنْ كُلِّ شَيْطَانٍ وَهَامَّةٍ، وَمِنْ كُلِّ عَيْنٍ لَامَّةٍ",
            englishText = "I seek refuge for you both in the perfect words of Allah from every devil and poisonous creature, and from every envious evil eye.",
            sourceAr = "صحيح البخاري (3371)",
            sourceEn = "Sahih al-Bukhari (3371)",
            benefitAr = "كان النبي ﷺ يعوّذ بها الحسن والحسين رضي الله عنهما لتحصين الأطفال.",
            benefitEn = "Authentic prophetic invocation for protecting children (Hasan & Husayn RA).",
            repeatRecommended = 3
        ),
        RuqyahVerseRef(
            id = "s_m_2202", surahNumber = 0, ayahNumber = 0,
            arabicText = "بِسْمِ اللَّهِ، بِسْمِ اللَّهِ، بِسْمِ اللَّهِ — أَعُوذُ بِاللَّهِ وَقُدْرَتِهِ مِنْ شَرِّ مَا أَجِدُ وَأُحَاذِرُ",
            englishText = "In the name of Allah (three times). I seek refuge in Allah and His power from the evil of what I feel and fear (seven times).",
            sourceAr = "صحيح مسلم (2202)",
            sourceEn = "Sahih Muslim (2202)",
            benefitAr = "تُقال مع وضع اليد على موضع الوجع في الجسد.",
            benefitEn = "Recited with the hand placed upon the painful area of the body.",
            repeatRecommended = 7
        ),
        RuqyahVerseRef(
            id = "s_m_2708", surahNumber = 0, ayahNumber = 0,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            englishText = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            sourceAr = "صحيح مسلم (2708)",
            sourceEn = "Sahih Muslim (2708)",
            benefitAr = "من قالها عند نزول منزل لم يضره شيء حتى يرتحل.",
            benefitEn = "Morning, evening, and travel protection.",
            repeatRecommended = 3
        ),
        RuqyahVerseRef(
            id = "s_d_3106", surahNumber = 0, ayahNumber = 0,
            arabicText = "أَسْأَلُ اللَّهَ الْعَظِيمَ رَبَّ الْعَرْشِ الْعَظِيمِ أَنْ يَشْفِيَكَ",
            englishText = "I ask Allah the Almighty, Lord of the Mighty Throne, to heal you.",
            sourceAr = "سنن أبي داود (3106) وسنن الترمذي (2083) - صحيح",
            sourceEn = "Abu Dawud (3106) & At-Tirmidhi (2083) - Sahih",
            benefitAr = "تُقال سبع مرات عند عيادة المريض الذي لم يحضر أجله فيُعافى بإذن الله.",
            benefitEn = "Recited seven times when visiting or praying for the sick.",
            repeatRecommended = 7
        ),
        RuqyahVerseRef(
            id = "s_d_5088", surahNumber = 0, ayahNumber = 0,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            englishText = "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, All-Knowing.",
            sourceAr = "سنن أبي داود (5088) وسنن الترمذي (3388) - صحيح",
            sourceEn = "Abu Dawud (5088) & At-Tirmidhi (3388) - Sahih",
            benefitAr = "من قالها ثلاثاً صباحاً ومساءً لم يضره شيء بإذن الله.",
            benefitEn = "Recited three times in the morning and evening for protection.",
            repeatRecommended = 3
        )
    )

    private val quranSuiteVerses: List<RuqyahVerseRef> =
        fatihahAyahList + listOf(ayatKursi, baqarahEnd285, baqarahEnd286) + muawwidhatAyahs

    private val childrenSuiteVerses: List<RuqyahVerseRef> =
        fatihahAyahList + listOf(ayatKursi) + muawwidhatAyahs + sunnahPropheticDuas.filter { it.id == "s_b_3371" || it.id == "s_m_2186" }

    val audioTracks: List<RuqyahAudioTrack> = listOf(
        RuqyahAudioTrack(
            id = "ruqyah_muaiqly",
            category = RuqyahCategory.COMPREHENSIVE,
            titleAr = "الرقية الشرعية الشاملة",
            titleEn = "Comprehensive Ruqyah Shariah",
            reciterId = "muaiqly",
            reciterAr = "الشيخ ماهر المعيقلي",
            reciterEn = "Sheikh Maher Al-Muaiqly",
            descriptionAr = "التسجيل الكامل والموثوق للرقية الشرعية من القرآن الكريم للتحصين والشفاء بإذن الله",
            descriptionEn = "Full authentic Quranic Ruqyah recitation for healing and protection",
            durationLabelAr = "تسجيل كامل",
            durationLabelEn = "Full Session",
            audioSource = RuqyahAudioSource.ArchiveItem("MuaiqlyRuqyah"),
            supportsReciterSelection = false,
            supportsLiveTextSync = false,
            sourceAttributionAr = "مصدر موثق: أرشيف الإنترنت (MuaiqlyRuqyah)",
            sourceAttributionEn = "Verified Source: Internet Archive (MuaiqlyRuqyah)",
            associatedTexts = listOf(verseFatihah1, ayatKursi, baqarahEnd286) + sunnahPropheticDuas.take(3)
        ),
        RuqyahAudioTrack(
            id = "ruqyah_quran_synced",
            category = RuqyahCategory.QURAN,
            titleAr = "الرقية الشرعية من القرآن الكريم",
            titleEn = "Ruqyah from the Holy Quran",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "الفاتحة وآية الكرسي وخواتيم البقرة والمعوذات مع مزامنة حية لكل آية بالرسم العثماني",
            descriptionEn = "Al-Fatihah, Ayat al-Kursi, End of Al-Baqarah & Al-Muawwidhat with live verse sync",
            durationLabelAr = "${quranSuiteVerses.size} آية متزامنة",
            durationLabelEn = "${quranSuiteVerses.size} Synced Ayahs",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(quranSuiteVerses),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية (AlQuran Cloud Audio CDN)",
            sourceAttributionEn = "Verified: AlQuran Cloud / Islamic Network CDN",
            associatedTexts = quranSuiteVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_evil_eye",
            category = RuqyahCategory.EVIL_EYE,
            titleAr = "رقية العين والحسد",
            titleEn = "Ruqyah for Evil Eye & Envy",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "آيات العين والحسد وسورة الفلق مع متابعة حية للنص القرآني أثناء التلاوة",
            descriptionEn = "Authentic verses against envy and the evil eye with live synchronized text",
            durationLabelAr = "${evilEyeVerses.size} آية متزامنة",
            durationLabelEn = "${evilEyeVerses.size} Synced Ayahs",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(evilEyeVerses),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية + صحيح مسلم (2186)",
            sourceAttributionEn = "Islamic Network CDN + Sahih Muslim (2186)",
            associatedTexts = evilEyeVerses + sunnahPropheticDuas.filter { it.id == "s_m_2186" }
        ),
        RuqyahAudioTrack(
            id = "ruqyah_magic_ajmi",
            category = RuqyahCategory.MAGIC,
            titleAr = "رقية السحر وإبطاله (الشيخ أحمد العجمي)",
            titleEn = "Ruqyah Against Magic (Sheikh Ahmad Al-Ajmi)",
            reciterId = "ajmi",
            reciterAr = "الشيخ أحمد بن علي العجمي",
            reciterEn = "Sheikh Ahmad Al-Ajmi",
            descriptionAr = "تلاوة مؤثرة وموثوقة في الرقية من السحر والمس والعين",
            descriptionEn = "Authentic recitation focused on protection against magic and harm",
            durationLabelAr = "تسجيل كامل",
            durationLabelEn = "Full Session",
            audioSource = RuqyahAudioSource.ArchiveItem("Ar-ruqyahAs-shariahRecitationBySheikhAhmadAlAjmi"),
            supportsReciterSelection = false,
            supportsLiveTextSync = false,
            sourceAttributionAr = "مصدر موثق: أرشيف الإنترنت (Al-Ajmi Ruqyah)",
            sourceAttributionEn = "Verified Source: Internet Archive (Al-Ajmi Ruqyah)",
            associatedTexts = magicVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_magic_verses",
            category = RuqyahCategory.MAGIC,
            titleAr = "آيات السحر من القرآن (مزامنة حية)",
            titleEn = "Quranic Magic Protection Verses (Synced)",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "آيات الأعراف ويونس وطه مع تمييز الآية المقروءة لحظياً بالتشكيل الصحيح",
            descriptionEn = "Verses from Al-A'raf, Yunus, and Taha with live verse highlighting",
            durationLabelAr = "${magicVerses.size} آيات متزامنة",
            durationLabelEn = "${magicVerses.size} Synced Ayahs",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(magicVerses),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية (AlQuran Cloud Audio CDN)",
            sourceAttributionEn = "Verified: AlQuran Cloud / Islamic Network CDN",
            associatedTexts = magicVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_harm_afasy",
            category = RuqyahCategory.HARM,
            titleAr = "رقية المس والتحصين (الشيخ مشاري العفاسي)",
            titleEn = "Ruqyah for Protection & Harm (Alafasy)",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري راشد العفاسي",
            reciterEn = "Sheikh Mishary Rashid Alafasy",
            descriptionAr = "رقية شرعية كاملة للحفظ ودفع الأذى والاعتصام بالله",
            descriptionEn = "Full authentic recitation for spiritual protection and peace",
            durationLabelAr = "تسجيل كامل",
            durationLabelEn = "Full Session",
            audioSource = RuqyahAudioSource.ArchiveItem("Ruqyah-By-Mishary-Al-Afaasy"),
            supportsReciterSelection = false,
            supportsLiveTextSync = false,
            sourceAttributionAr = "مصدر موثق: أرشيف الإنترنت (Ruqyah-By-Mishary-Al-Afaasy)",
            sourceAttributionEn = "Verified Source: Internet Archive (Ruqyah-By-Mishary-Al-Afaasy)",
            associatedTexts = harmVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_harm_verses",
            category = RuqyahCategory.HARM,
            titleAr = "آيات التحصين ودفع المس (مزامنة حية)",
            titleEn = "Verses of Protection & Refuge (Synced)",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "آية الكرسي وخواتيم الحشر والصافات والمعوذات مع تتبع الآيات صوتياً",
            descriptionEn = "Ayat al-Kursi, Al-Hashr, As-Saffat & Muawwidhat with live sync",
            durationLabelAr = "${harmVerses.size} آية متزامنة",
            durationLabelEn = "${harmVerses.size} Synced Ayahs",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(harmVerses),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية (AlQuran Cloud Audio CDN)",
            sourceAttributionEn = "Verified: AlQuran Cloud / Islamic Network CDN",
            associatedTexts = harmVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_waswas",
            category = RuqyahCategory.WASWAS,
            titleAr = "رقية الوسواس والخوف والسكينة",
            titleEn = "Ruqyah for Waswas, Anxiety & Fear",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "آيات السكينة والطمأنينة وسورة الناس لدفع الوساوس والمخاوف",
            descriptionEn = "Verses of divine tranquility and Surah An-Nas against fear & whispers",
            durationLabelAr = "${waswasVerses.size} آية متزامنة",
            durationLabelEn = "${waswasVerses.size} Synced Ayahs",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(waswasVerses),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية (AlQuran Cloud Audio CDN)",
            sourceAttributionEn = "Verified: AlQuran Cloud / Islamic Network CDN",
            associatedTexts = waswasVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_children",
            category = RuqyahCategory.CHILDREN,
            titleAr = "الرقية الشرعية للأطفال",
            titleEn = "Ruqyah Shariah for Children",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "الفاتحة وآية الكرسي والمعوذات مع دعاء تعويذ الحسن والحسين رضي الله عنهما",
            descriptionEn = "Al-Fatihah, Ayat al-Kursi & Al-Muawwidhat paired with the Prophetic kids dua",
            durationLabelAr = "${fatihahAyahList.size + 1 + muawwidhatAyahs.size} آية + دعاء نبوي",
            durationLabelEn = "Synced Quran + Prophetic Dua",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(
                fatihahAyahList + listOf(ayatKursi) + muawwidhatAyahs
            ),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية + صحيح البخاري (3371)",
            sourceAttributionEn = "Islamic Network CDN + Sahih al-Bukhari (3371)",
            associatedTexts = childrenSuiteVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_sunnah",
            category = RuqyahCategory.SUNNAH,
            titleAr = "الرقية الشرعية من السنة النبوية",
            titleEn = "Ruqyah from Authentic Prophetic Sunnah",
            reciterId = "archive_scholars",
            reciterAr = "نخبة من القراء (تلاوة وأدعية نبوية)",
            reciterEn = "Authentic Compilation (Quran & Sunnah)",
            descriptionAr = "تسجيل الرقية بالكتاب والسنة مع موسوعة أدعية الرقية النبوية الموثقة بالتشكيل",
            descriptionEn = "Verified Quran & Sunnah compilation paired with Sahih Hadith invocations",
            durationLabelAr = "${sunnahPropheticDuas.size} أذكار صحيحة",
            durationLabelEn = "${sunnahPropheticDuas.size} Sahih Duas",
            audioSource = RuqyahAudioSource.ArchiveItem("al-ruqyah"),
            supportsReciterSelection = false,
            supportsLiveTextSync = false,
            sourceAttributionAr = "أرشيف الإنترنت (al-ruqyah) + الصحيحين",
            sourceAttributionEn = "Internet Archive (al-ruqyah) + Bukhari & Muslim",
            associatedTexts = sunnahPropheticDuas
        ),
        RuqyahAudioTrack(
            id = "ruqyah_shifa",
            category = RuqyahCategory.SHIFA,
            titleAr = "آيات الشفاء الست",
            titleEn = "The Six Verses of Healing",
            reciterId = "alafasy",
            reciterAr = "الشيخ مشاري العفاسي / اختيار القارئ",
            reciterEn = "Selected Verified Reciter",
            descriptionAr = "آيات الشفاء الست في القرآن الكريم متلوة بالتسلسل مع مزامنة فورية للنص",
            descriptionEn = "All six Quranic healing verses recited sequentially with live verse sync",
            durationLabelAr = "6 آيات شفاء متزامنة",
            durationLabelEn = "6 Synced Healing Ayahs",
            audioSource = RuqyahAudioSource.SyncedQuranSequence(shifaSixVerses),
            supportsReciterSelection = true,
            supportsLiveTextSync = true,
            sourceAttributionAr = "شبكة القرآن الإسلامية (AlQuran Cloud Audio CDN)",
            sourceAttributionEn = "Verified: AlQuran Cloud / Islamic Network CDN",
            associatedTexts = shifaSixVerses
        ),
        RuqyahAudioTrack(
            id = "ruqyah_baqarah_full",
            category = RuqyahCategory.BAQARAH,
            titleAr = "سورة البقرة كاملة",
            titleEn = "Complete Surah Al-Baqarah",
            reciterId = "alafasy",
            reciterAr = "تلاوة كاملة (حسب القارئ المختار / MP3Quran)",
            reciterEn = "Full Recitation (MP3Quran Verified)",
            descriptionAr = "سورة البقرة كاملة (286 آية) — أخذها بركة وتركها حسرة ولا تستطيعها البطلة (صحيح مسلم)",
            descriptionEn = "Complete Surah Al-Baqarah (286 Ayahs) resolved live via MP3Quran API v3",
            durationLabelAr = "286 آية • سورة كاملة",
            durationLabelEn = "286 Ayahs • Complete Surah",
            audioSource = RuqyahAudioSource.FullSurahMp3Quran(2),
            supportsReciterSelection = true,
            supportsLiveTextSync = false,
            sourceAttributionAr = "MP3Quran API v3 + صحيح مسلم (804)",
            sourceAttributionEn = "MP3Quran API v3 + Sahih Muslim (804)",
            associatedTexts = listOf(ayatKursi, baqarahEnd285, baqarahEnd286)
        )
    )

    val allScripturalItems: List<RuqyahVerseRef> =
        (listOf(verseFatihah1, ayatKursi, baqarahEnd285, baqarahEnd286) +
            shifaSixVerses +
            sunnahPropheticDuas).distinctBy { it.id }

    val ruqyahItems: List<RuqyahVerseRef>
        get() = allScripturalItems

    fun findTrackById(id: String): RuqyahAudioTrack? = audioTracks.firstOrNull { it.id == id }

    fun findReciterByEdition(edition: String): RuqyahReciterProfile =
        cdnReciters.firstOrNull { it.cdnEdition == edition } ?: cdnReciters.first()

    fun findBestMoshafForReciter(
        reciters: List<com.example.data.remote.Mp3QuranReciterDto>,
        profile: RuqyahReciterProfile,
        targetSurah: Int = 2
    ): Pair<String, String>? {
        val normalizedKeywords = profile.mp3QuranKeywords.map { it.lowercase() }
        val preferred = reciters.firstOrNull { dto ->
            val n = dto.name?.lowercase() ?: ""
            normalizedKeywords.any { kw -> n.contains(kw) }
        } ?: reciters.firstOrNull { dto ->
            val n = dto.name ?: ""
            n.contains("عبدالله") && n.contains("موسى")
        } ?: reciters.firstOrNull { dto ->
            dto.moshaf?.any { (it.surahTotal ?: 0) >= 114 } == true
        } ?: reciters.firstOrNull() ?: return null

        val moshafs = preferred.moshaf.orEmpty()
        val chosen = moshafs.firstOrNull { m ->
            val surahSet = m.surahList.orEmpty().split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
            !m.server.isNullOrBlank() && (surahSet.isEmpty() || surahSet.contains(targetSurah))
        } ?: moshafs.firstOrNull { !it.server.isNullOrBlank() } ?: return null

        val serverUrl = chosen.server?.trim()?.trimEnd('/') ?: return null
        val displayName = preferred.name?.trim()?.takeIf { it.isNotBlank() } ?: profile.nameAr
        return serverUrl to displayName
    }
}
