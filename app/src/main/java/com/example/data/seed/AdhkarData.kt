package com.example.data.seed

data class AdhkarCategory(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val subtitleAr: String,
    val subtitleEn: String
)

data class DhikrItem(
    val id: String,
    val categoryId: String,
    val arabicText: String,
    val englishTranslation: String,
    val targetCount: Int,
    val virtueAr: String,
    val virtueEn: String,
    val sourceAr: String,
    val sourceEn: String
)

data class DailyDua(
    val arabicText: String,
    val englishText: String,
    val sourceAr: String,
    val sourceEn: String
)

object AdhkarData {
    val categories: List<AdhkarCategory> = listOf(
        AdhkarCategory("morning", "أذكار الصباح", "Morning Adhkar", "تُقال بعد الفجر حتى طلوع الشمس أو الزوال", "Recited after Fajr until sunrise or noon"),
        AdhkarCategory("evening", "أذكار المساء", "Evening Adhkar", "تُقال بعد العصر حتى منتصف الليل", "Recited after Asr until midnight"),
        AdhkarCategory("after_prayer", "أذكار بعد الصلاة", "After Prayer", "السنن المأثورة عقب الصلوات المفروضة", "Authentic supplications after obligatory prayers"),
        AdhkarCategory("sleep_wake", "أذكار النوم والاستيقاظ", "Sleep & Waking", "تحصين المسلم عند النوم وعند الاستيقاظ", "Remembrances when going to sleep and waking up"),
        AdhkarCategory("entry_exit", "أذكار الدخول والخروج", "Entry & Exit", "دخول المنزل والمسجد والخروج منهما", "Entering and leaving the home and mosque"),
        AdhkarCategory("quran_sunnah_duas", "أدعية من القرآن والسنة", "Quranic & Prophetic Duas", "جوامع الدعاء من كتاب الله وسنة نبيه ﷺ", "Comprehensive supplications from Quran and Sunnah")
    )

    val items: List<DhikrItem> = listOf(
        // Morning
        DhikrItem(
            id = "m_1",
            categoryId = "morning",
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            englishTranslation = "We have reached the morning and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah, alone, without partner; to Him belongs all sovereignty and praise and He is over all things omnipotent.",
            targetCount = 1,
            virtueAr = "تُقال في الصباح إقراراً بتوحيد الله وملكه وتفويض الأمر إليه.",
            virtueEn = "Affirms Allah's sole sovereignty and praise at the start of the day.",
            sourceAr = "صحيح مسلم (2723)",
            sourceEn = "Sahih Muslim (2723)"
        ),
        DhikrItem(
            id = "m_2",
            categoryId = "morning",
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لَا يَغْفِرُ الذُّنُوبَ إِلَّا أَنْتَ",
            englishTranslation = "O Allah, You are my Lord, none has the right to be worshipped except You. You created me and I am Your servant, and I abide to Your covenant and promise as best I can. I seek refuge in You from the evil of what I have done. I acknowledge Your favor upon me and I acknowledge my sin, so forgive me, for verily none can forgive sins except You.",
            targetCount = 1,
            virtueAr = "سيد الاستغفار: من قالها موقناً بها فمات من يومه دخل الجنة.",
            virtueEn = "Master Supplication for Forgiveness: whoever recites it with certainty and passes away that day enters Paradise.",
            sourceAr = "صحيح البخاري (6306)",
            sourceEn = "Sahih al-Bukhari (6306)"
        ),
        DhikrItem(
            id = "m_3",
            categoryId = "morning",
            arabicText = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            englishTranslation = "In the name of Allah with whose name nothing is harmed on earth nor in the heavens, and He is The All-Hearing, The All-Knowing.",
            targetCount = 3,
            virtueAr = "من قالها ثلاثاً إذا أصبح لم تصبه فجأة بلاء حتى يمسي.",
            virtueEn = "Whoever recites it three times in the morning will not be afflicted by sudden calamity until evening.",
            sourceAr = "سنن أبي داود (5088) والترمذي (3388) - حسن صحيح",
            sourceEn = "Abu Dawud (5088) & At-Tirmidhi (3388) - Sahih"
        ),
        DhikrItem(
            id = "m_4",
            categoryId = "morning",
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ",
            englishTranslation = "Glory be to Allah and praise Him.",
            targetCount = 100,
            virtueAr = "من قالها مائة مرة حين يصبح وحين يمسي لم يأت أحد يوم القيامة بأفضل مما جاء به إلا أحد قال مثل ما قال أو زاد عليه.",
            virtueEn = "Whoever recites it 100 times in the morning and evening, none shall come on the Day of Resurrection with anything better except one who said the same or more.",
            sourceAr = "صحيح مسلم (2692)",
            sourceEn = "Sahih Muslim (2692)"
        ),
        // Evening
        DhikrItem(
            id = "e_1",
            categoryId = "evening",
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            englishTranslation = "We have reached the evening and at this very time all sovereignty belongs to Allah, and all praise is for Allah. None has the right to be worshipped except Allah, alone, without partner.",
            targetCount = 1,
            virtueAr = "ذكر المساء الجامع للإقرار بملك الله وحمده وسؤال خير الليلة.",
            virtueEn = "Evening remembrance affirming Allah's sovereignty and seeking the goodness of the night.",
            sourceAr = "صحيح مسلم (2723)",
            sourceEn = "Sahih Muslim (2723)"
        ),
        DhikrItem(
            id = "e_2",
            categoryId = "evening",
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            englishTranslation = "I seek refuge in the perfect words of Allah from the evil of that which He has created.",
            targetCount = 3,
            virtueAr = "من قالها حين يمسي ثلاث مرات لم تضره حمة تلك الليلة.",
            virtueEn = "Protection from harm and venomous stings throughout the night.",
            sourceAr = "صحيح مسلم (2709)",
            sourceEn = "Sahih Muslim (2709)"
        ),
        DhikrItem(
            id = "e_3",
            categoryId = "evening",
            arabicText = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
            englishTranslation = "Allah is sufficient for me. There is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
            targetCount = 7,
            virtueAr = "كفاية الله للعبد فيما أهمه من أمر الدنيا والآخرة.",
            virtueEn = "Allah suffices the servant against whatever concerns them in this world and the next.",
            sourceAr = "سورة التوبة (129) وسنن أبي داود (5081)",
            sourceEn = "Quran 9:129 & Abu Dawud (5081)"
        ),
        // After Prayer
        DhikrItem(
            id = "ap_1",
            categoryId = "after_prayer",
            arabicText = "أَسْتَغْفِرُ اللَّهَ (ثلاثاً) اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ، تَبَارَكْتَ يَا ذَا الْجَلَالِ وَالْإِكْرَامِ",
            englishTranslation = "I seek the forgiveness of Allah (3x). O Allah, You are Peace and from You comes peace. Blessed are You, O Possessor of Majesty and Honor.",
            targetCount = 1,
            virtueAr = "أول ما كان النبي ﷺ يقوله إذا انصرف من صلاته.",
            virtueEn = "The first remembrance the Prophet ﷺ would say upon concluding prayer.",
            sourceAr = "صحيح مسلم (591)",
            sourceEn = "Sahih Muslim (591)"
        ),
        DhikrItem(
            id = "ap_2",
            categoryId = "after_prayer",
            arabicText = "سُبْحَانَ اللَّهِ",
            englishTranslation = "Glory be to Allah.",
            targetCount = 33,
            virtueAr = "من سبح الله دبر كل صلاة ثلاثاً وثلاثين وحمد ثلاثاً وثلاثين وكبر ثلاثاً وثلاثين غُفرت خطاياه وإن كانت مثل زبد البحر.",
            virtueEn = "Whoever glorifies Allah 33 times, praises Him 33 times, and magnifies Him 33 times after every prayer has their sins forgiven even if like the foam of the sea.",
            sourceAr = "صحيح مسلم (597)",
            sourceEn = "Sahih Muslim (597)"
        ),
        DhikrItem(
            id = "ap_3",
            categoryId = "after_prayer",
            arabicText = "الْحَمْدُ لِلَّهِ",
            englishTranslation = "All praise is due to Allah.",
            targetCount = 33,
            virtueAr = "تمام التسبيحات المأثورة دبر الصلوات المكتوبات.",
            virtueEn = "Part of the Prophetic post-prayer remembrance.",
            sourceAr = "صحيح مسلم (597)",
            sourceEn = "Sahih Muslim (597)"
        ),
        DhikrItem(
            id = "ap_4",
            categoryId = "after_prayer",
            arabicText = "اللَّهُ أَكْبَرُ",
            englishTranslation = "Allah is the Greatest.",
            targetCount = 33,
            virtueAr = "تُختم بقول: لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير.",
            virtueEn = "Followed by La ilaha illallah wahdahu la sharika lah... to complete 100.",
            sourceAr = "صحيح مسلم (597)",
            sourceEn = "Sahih Muslim (597)"
        ),
        // Sleep & Waking
        DhikrItem(
            id = "sw_1",
            categoryId = "sleep_wake",
            arabicText = "بِاسْمِكَ اللَّهُمَّ أَمُوتُ وَأَحْيَا",
            englishTranslation = "In Your name, O Allah, I die and I live.",
            targetCount = 1,
            virtueAr = "كان النبي ﷺ إذا أخذ مضجعه من الليل قال هذا الذكر.",
            virtueEn = "Recited by the Prophet ﷺ when lying down to sleep at night.",
            sourceAr = "صحيح البخاري (6324)",
            sourceEn = "Sahih al-Bukhari (6324)"
        ),
        DhikrItem(
            id = "sw_2",
            categoryId = "sleep_wake",
            arabicText = "الْحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا وَإِلَيْهِ النُّشُورُ",
            englishTranslation = "All praise is for Allah who gave us life after having taken it from us and unto Him is the resurrection.",
            targetCount = 1,
            virtueAr = "يُقال عند الاستيقاظ من النوم شكراً لله على تجدد الحياة.",
            virtueEn = "Recited upon waking up in gratitude to Allah.",
            sourceAr = "صحيح البخاري (6312)",
            sourceEn = "Sahih al-Bukhari (6312)"
        ),
        // Entry & Exit
        DhikrItem(
            id = "ee_1",
            categoryId = "entry_exit",
            arabicText = "بِسْمِ اللَّهِ، تَوَكَّلْتُ عَلَى اللَّهِ، وَلَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ",
            englishTranslation = "In the name of Allah, I place my trust in Allah, and there is no might nor power except with Allah.",
            targetCount = 1,
            virtueAr = "يُقال له عند الخروج من بيته: هُديت وكُفيت ووُقيت، وتنحّى عنه الشيطان.",
            virtueEn = "Said upon leaving home: one is guided, sufficed, and protected, and Satan turns away.",
            sourceAr = "سنن أبي داود (5095) والترمذي (3426) - صحيح",
            sourceEn = "Abu Dawud (5095) & At-Tirmidhi (3426) - Sahih"
        ),
        DhikrItem(
            id = "ee_2",
            categoryId = "entry_exit",
            arabicText = "اللَّهُمَّ افْتَحْ لِي أَبْوَابَ رَحْمَتِكَ",
            englishTranslation = "O Allah, open for me the doors of Your mercy.",
            targetCount = 1,
            virtueAr = "دعاء دخول المسجد بخشوع ورجاء لرحمة الله.",
            virtueEn = "Supplication upon entering the mosque.",
            sourceAr = "صحيح مسلم (713)",
            sourceEn = "Sahih Muslim (713)"
        ),
        DhikrItem(
            id = "ee_3",
            categoryId = "entry_exit",
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ مِنْ فَضْلِكَ",
            englishTranslation = "O Allah, indeed I ask You of Your bounty.",
            targetCount = 1,
            virtueAr = "دعاء الخروج من المسجد لطلب الرزق الحلال والفضل.",
            virtueEn = "Supplication upon exiting the mosque.",
            sourceAr = "صحيح مسلم (713)",
            sourceEn = "Sahih Muslim (713)"
        ),
        // Quran & Sunnah Duas
        DhikrItem(
            id = "qs_1",
            categoryId = "quran_sunnah_duas",
            arabicText = "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً وَقِنَا عَذَابَ النَّارِ",
            englishTranslation = "Our Lord, give us in this world [that which is] good and in the Hereafter [that which is] good and protect us from the punishment of the Fire.",
            targetCount = 1,
            virtueAr = "كان أكثر دعاء النبي ﷺ، جامعٌ لخيري الدنيا والآخرة.",
            virtueEn = "The most frequent supplication of the Prophet ﷺ, encompassing the good of both worlds.",
            sourceAr = "سورة البقرة (201) ومتفق عليه",
            sourceEn = "Quran 2:201 & Bukhari/Muslim"
        ),
        DhikrItem(
            id = "qs_2",
            categoryId = "quran_sunnah_duas",
            arabicText = "رَبَّنَا لَا تُزِغْ قُلُوبَنَا بَعْدَ إِذْ هَدَيْتَنَا وَهَبْ لَنَا مِن لَّدُنكَ رَحْمَةً ۚ إِنَّكَ أَنتَ الْوَهَّابُ",
            englishTranslation = "Our Lord, let not our hearts deviate after You have guided us and grant us from Yourself mercy. Indeed, You are the Bestower.",
            targetCount = 1,
            virtueAr = "دعاء الثبات على الهداية وسؤال الرحمة من الوهاب.",
            virtueEn = "Quranic supplication for steadfastness upon guidance.",
            sourceAr = "سورة آل عمران (8)",
            sourceEn = "Quran 3:8"
        ),
        DhikrItem(
            id = "qs_3",
            categoryId = "quran_sunnah_duas",
            arabicText = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ، وَشُكْرِكَ، وَحُسْنِ عِبَادَتِكَ",
            englishTranslation = "O Allah, help me to remember You, to give thanks to You, and to worship You in the best of manners.",
            targetCount = 1,
            virtueAr = "وصية النبي ﷺ لمعاذ بن جبل رضي الله عنه ألا يدعها دبر كل صلاة.",
            virtueEn = "The Prophet ﷺ advised Mu'adh ibn Jabal never to omit this after every prayer.",
            sourceAr = "سنن أبي داود (1522) - صحيح",
            sourceEn = "Abu Dawud (1522) - Sahih"
        )
    )

    val dailyDuas: List<DailyDua> = listOf(
        DailyDua(
            arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا",
            englishText = "O Allah, I ask You for beneficial knowledge, goodly provision, and acceptable deeds.",
            sourceAr = "سنن ابن ماجه (925) - صحيح",
            sourceEn = "Sunan Ibn Majah (925) - Sahih"
        ),
        DailyDua(
            arabicText = "يَا حَيُّ يَا قَيُّومُ بِرَحْمَتِكَ أَسْتَغِيثُ، أَصْلِحْ لِي شَأْنِي كُلَّهُ وَلَا تَكِلْنِي إِلَى نَفْسِي طَرْفَةَ عَيْنٍ",
            englishText = "O Ever-Living, O Sustainer, in Your mercy I seek relief; rectify all of my affairs and do not leave me to myself even for the blink of an eye.",
            sourceAr = "المستدرك على الصحيحين (صحيح)",
            sourceEn = "Al-Mustadrak (Sahih)"
        ),
        DailyDua(
            arabicText = "اللَّهُمَّ أَعِنِّي عَلَى ذِكْرِكَ وَشُكْرِكَ وَحُسْنِ عِبَادَتِكَ",
            englishText = "O Allah, assist me in remembering You, thanking You, and worshipping You well.",
            sourceAr = "سنن أبي داود (1522)",
            sourceEn = "Sunan Abi Dawud (1522)"
        ),
        DailyDua(
            arabicText = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
            englishText = "My Lord, expand for me my breast [with assurance] and ease for me my task.",
            sourceAr = "سورة طه (25-26)",
            sourceEn = "Quran 20:25-26"
        )
    )

    fun duaOfTheDay(dayOfYear: Int): DailyDua = dailyDuas[dayOfYear.mod(dailyDuas.size)]
}

