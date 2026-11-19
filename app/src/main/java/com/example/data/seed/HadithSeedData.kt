package com.example.data.seed

data class HadithBookInfo(
    val id: String,
    val arabicEdition: String,
    val englishEdition: String,
    val titleAr: String,
    val titleEn: String,
    val authorAr: String,
    val authorEn: String,
    val totalChapters: Int
)

data class HadithChapterInfo(
    val number: Int,
    val titleAr: String,
    val titleEn: String
)

data class HadithItem(
    val id: String,
    val bookId: String,
    val bookNameAr: String,
    val bookNameEn: String,
    val chapterNumber: Int,
    val chapterNameAr: String,
    val chapterNameEn: String,
    val hadithNumber: Int,
    val narratorAr: String,
    val narratorEn: String,
    val arabicText: String,
    val englishText: String,
    val gradeAr: String,
    val gradeEn: String
)

object HadithSeedData {
    val books: List<HadithBookInfo> = listOf(
        HadithBookInfo("bukhari", "ara-bukhari", "eng-bukhari", "صحيح البخاري", "Sahih al-Bukhari", "الإمام محمد بن إسماعيل البخاري", "Imam Muhammad al-Bukhari", 97),
        HadithBookInfo("muslim", "ara-muslim", "eng-muslim", "صحيح مسلم", "Sahih Muslim", "الإمام مسلم بن الحجاج", "Imam Muslim ibn al-Hajjaj", 56),
        HadithBookInfo("abudawud", "ara-abudawud", "eng-abudawud", "سنن أبي داود", "Sunan Abi Dawud", "الإمام سليمان بن الأشعث", "Imam Abu Dawud", 43),
        HadithBookInfo("tirmidhi", "ara-tirmidhi", "eng-tirmidhi", "جامع الترمذي", "Jami' at-Tirmidhi", "الإمام محمد بن عيسى الترمذي", "Imam At-Tirmidhi", 49),
        HadithBookInfo("nasai", "ara-nasai", "eng-nasai", "سنن النسائي", "Sunan an-Nasa'i", "الإمام أحمد بن شعيب النسائي", "Imam An-Nasa'i", 51),
        HadithBookInfo("ibnmajah", "ara-ibnmajah", "eng-ibnmajah", "سنن ابن ماجه", "Sunan Ibn Majah", "الإمام محمد بن يزيد ابن ماجه", "Imam Ibn Majah", 37)
    )

    val chapters: List<HadithChapterInfo> = listOf(
        HadithChapterInfo(1, "كتاب بدء الوحي والإيمان", "Revelation & Faith"),
        HadithChapterInfo(2, "كتاب العلم والأخلاق", "Knowledge & Manners"),
        HadithChapterInfo(3, "كتاب الطهارة والصلاة", "Purification & Prayer"),
        HadithChapterInfo(4, "كتاب الذكر والدعاء", "Remembrance & Supplication"),
        HadithChapterInfo(5, "كتاب البر والصلة والزهد", "Righteousness & Piety")
    )

    val starterHadiths: List<HadithItem> = listOf(
        // Sahih al-Bukhari
        HadithItem(
            id = "bukhari_1",
            bookId = "bukhari",
            bookNameAr = "صحيح البخاري",
            bookNameEn = "Sahih al-Bukhari",
            chapterNumber = 1,
            chapterNameAr = "بدء الوحي",
            chapterNameEn = "Revelation",
            hadithNumber = 1,
            narratorAr = "عمر بن الخطاب رضي الله عنه",
            narratorEn = "Umar ibn al-Khattab (RA)",
            arabicText = "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ، وَإِنَّمَا لِكُلِّ امْرِئٍ مَا نَوَى، فَمَنْ كَانَتْ هِجْرَتُهُ إِلَى دُنْيَا يُصِيبُهَا أَوْ إِلَى امْرَأَةٍ يَنْكِحُهَا فَهِجْرَتُهُ إِلَى مَا هَاجَرَ إِلَيْهِ.",
            englishText = "The reward of deeds depends upon the intentions and every person will get the reward according to what he has intended.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        HadithItem(
            id = "bukhari_5027",
            bookId = "bukhari",
            bookNameAr = "صحيح البخاري",
            bookNameEn = "Sahih al-Bukhari",
            chapterNumber = 2,
            chapterNameAr = "فضائل القرآن",
            chapterNameEn = "Virtues of the Quran",
            hadithNumber = 5027,
            narratorAr = "عثمان بن عفان رضي الله عنه",
            narratorEn = "Uthman ibn Affan (RA)",
            arabicText = "خَيْرُكُمْ مَنْ تَعَلَّمَ القُرْآنَ وَعَلَّمَهُ.",
            englishText = "The best among you (Muslims) are those who learn the Quran and teach it.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        HadithItem(
            id = "bukhari_6407",
            bookId = "bukhari",
            bookNameAr = "صحيح البخاري",
            bookNameEn = "Sahih al-Bukhari",
            chapterNumber = 4,
            chapterNameAr = "الدعوات",
            chapterNameEn = "Invocations",
            hadithNumber = 6407,
            narratorAr = "أبو موسى الأشعري رضي الله عنه",
            narratorEn = "Abu Musa al-Ash'ari (RA)",
            arabicText = "مَثَلُ الَّذِي يَذْكُرُ رَبَّهُ وَالَّذِي لَا يَذْكُرُ رَبَّهُ مَثَلُ الحَيِّ وَالمَيِّتِ.",
            englishText = "The example of the one who remembers his Lord and the one who does not remember his Lord is like that of the living and the dead.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        // Sahih Muslim
        HadithItem(
            id = "muslim_2564",
            bookId = "muslim",
            bookNameAr = "صحيح مسلم",
            bookNameEn = "Sahih Muslim",
            chapterNumber = 5,
            chapterNameAr = "البر والصلة",
            chapterNameEn = "Virtue & Good Manners",
            hadithNumber = 2564,
            narratorAr = "أبو هريرة رضي الله عنه",
            narratorEn = "Abu Hurairah (RA)",
            arabicText = "إِنَّ اللَّهَ لَا يَنْظُرُ إِلَى صُوَرِكُمْ وَأَمْوَالِكُمْ، وَلَكِنْ يَنْظُرُ إِلَى قُلُوبِكُمْ وَأَعْمَالِكُمْ.",
            englishText = "Verily Allah does not look to your faces and your wealth, but He looks to your hearts and to your deeds.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        HadithItem(
            id = "muslim_2699",
            bookId = "muslim",
            bookNameAr = "صحيح مسلم",
            bookNameEn = "Sahih Muslim",
            chapterNumber = 2,
            chapterNameAr = "الذكر والدعاء",
            chapterNameEn = "Remembrance & Supplication",
            hadithNumber = 2699,
            narratorAr = "أبو هريرة رضي الله عنه",
            narratorEn = "Abu Hurairah (RA)",
            arabicText = "وَمَنْ سَلَكَ طَرِيقًا يَلْتَمِسُ فِيهِ عِلْمًا، سَهَّلَ اللَّهُ لَهُ بِهِ طَرِيقًا إِلَى الْجَنَّةِ.",
            englishText = "Whoever treads a path in search of knowledge, Allah will make easy for him the path to Paradise.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        // Sunan Abi Dawud
        HadithItem(
            id = "abudawud_4941",
            bookId = "abudawud",
            bookNameAr = "سنن أبي داود",
            bookNameEn = "Sunan Abi Dawud",
            chapterNumber = 5,
            chapterNameAr = "الأدب",
            chapterNameEn = "General Behavior",
            hadithNumber = 4941,
            narratorAr = "عبدالله بن عمرو رضي الله عنهما",
            narratorEn = "Abdullah ibn Amr (RA)",
            arabicText = "الرَّاحِمُونَ يَرْحَمُهُمُ الرَّحْمَنُ، ارْحَمُوا مَنْ فِي الْأَرْضِ يَرْحَمْكُمْ مَنْ فِي السَّمَاءِ.",
            englishText = "The merciful will be shown mercy by the Most Merciful. Be merciful to those on the earth and the One in the heavens will have mercy upon you.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Al-Albani)"
        ),
        HadithItem(
            id = "abudawud_1522",
            bookId = "abudawud",
            bookNameAr = "سنن أبي داود",
            bookNameEn = "Sunan Abi Dawud",
            chapterNumber = 3,
            chapterNameAr = "الصلاة",
            chapterNameEn = "Prayer",
            hadithNumber = 1522,
            narratorAr = "معاذ بن جبل رضي الله عنه",
            narratorEn = "Mu'adh ibn Jabal (RA)",
            arabicText = "يَا مُعَاذُ، وَاللَّهِ إِنِّي لَأُحِبُّكَ، أُوصِيكَ يَا مُعَاذُ لَا تَدَعَنَّ فِي دُبُرِ كُلِّ صَلَاةٍ تَقُولُ: اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ.",
            englishText = "O Mu'adh, by Allah I love you. I advise you, O Mu'adh, do not ever leave saying after every prayer: O Allah, help me to remember You, thank You, and worship You well.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        // Jami' at-Tirmidhi
        HadithItem(
            id = "tirmidhi_1987",
            bookId = "tirmidhi",
            bookNameAr = "جامع الترمذي",
            bookNameEn = "Jami' at-Tirmidhi",
            chapterNumber = 5,
            chapterNameAr = "البر والصلة",
            chapterNameEn = "Righteousness",
            hadithNumber = 1987,
            narratorAr = "أبو ذر الغفاري رضي الله عنه",
            narratorEn = "Abu Dharr al-Ghifari (RA)",
            arabicText = "اتَّقِ اللَّهِ حَيْثُمَا كُنْتَ، وَأَتْبِعِ السَّيِّئَةَ الْحَسَنَةَ تَمْحُهَا، وَخَالِقِ النَّاسَ بِخُلُقٍ حَسَنٍ.",
            englishText = "Have Taqwa (fear) of Allah wherever you may be, follow up a bad deed with a good deed which will wipe it out, and behave well towards the people.",
            gradeAr = "حسن صحيح",
            gradeEn = "Hasan Sahih"
        ),
        HadithItem(
            id = "tirmidhi_2317",
            bookId = "tirmidhi",
            bookNameAr = "جامع الترمذي",
            bookNameEn = "Jami' at-Tirmidhi",
            chapterNumber = 5,
            chapterNameAr = "الزهد",
            chapterNameEn = "Asceticism",
            hadithNumber = 2317,
            narratorAr = "أبو هريرة رضي الله عنه",
            narratorEn = "Abu Hurairah (RA)",
            arabicText = "مِنْ حُسْنِ إِسْلَامِ الْمَرْءِ تَرْكُهُ مَا لَا يَعْنِيهِ.",
            englishText = "Indeed, among the excellence of a person's Islam is leaving that which does not concern him.",
            gradeAr = "حسن",
            gradeEn = "Hasan"
        ),
        // Sunan an-Nasa'i
        HadithItem(
            id = "nasai_1299",
            bookId = "nasai",
            bookNameAr = "سنن النسائي",
            bookNameEn = "Sunan an-Nasa'i",
            chapterNumber = 3,
            chapterNameAr = "السهو والذكر",
            chapterNameEn = "Prayer & Remembrance",
            hadithNumber = 1299,
            narratorAr = "أنس بن مالك رضي الله عنه",
            narratorEn = "Anas ibn Malik (RA)",
            arabicText = "مَنْ صَلَّى عَلَيَّ صَلَاةً وَاحِدَةً صَلَّى اللَّهُ عَلَيْهِ عَشْرَ صَلَوَاتٍ، وَحُطَّتْ عَنْهُ عَشْرُ خَطِيئَاتٍ، وَرُفِعَتْ لَهُ عَشْرُ دَرَجَاتٍ.",
            englishText = "Whoever sends blessings upon me once, Allah will send blessings upon him tenfold, erase ten sins from him, and raise him ten degrees.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        ),
        // Sunan Ibn Majah
        HadithItem(
            id = "ibnmajah_224",
            bookId = "ibnmajah",
            bookNameAr = "سنن ابن ماجه",
            bookNameEn = "Sunan Ibn Majah",
            chapterNumber = 2,
            chapterNameAr = "المقدمة في العلم",
            chapterNameEn = "The Book of Sunnah",
            hadithNumber = 224,
            narratorAr = "أنس بن مالك رضي الله عنه",
            narratorEn = "Anas ibn Malik (RA)",
            arabicText = "طَلَبُ الْعِلْمِ فَرِيضَةٌ عَلَى كُلِّ مُسْلِمٍ.",
            englishText = "Seeking knowledge is a duty upon every Muslim.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Al-Albani)"
        ),
        HadithItem(
            id = "ibnmajah_4141",
            bookId = "ibnmajah",
            bookNameAr = "سنن ابن ماجه",
            bookNameEn = "Sunan Ibn Majah",
            chapterNumber = 5,
            chapterNameAr = "الزهد",
            chapterNameEn = "Asceticism",
            hadithNumber = 4141,
            narratorAr = "عبدالله بن عمر رضي الله عنهما",
            narratorEn = "Abdullah ibn Umar (RA)",
            arabicText = "كُنْ فِي الدُّنْيَا كَأَنَّكَ غَرِيبٌ أَوْ عَابِرُ سَبِيلٍ.",
            englishText = "Be in this world as if you were a stranger or a traveler along a path.",
            gradeAr = "صحيح",
            gradeEn = "Sahih (Authentic)"
        )
    )
}
