package com.example.domain.safety

import com.example.domain.model.RegionalLanguage

enum class SafetyCategory(val title: String, val description: String) {
    UNCONSCIOUS_PERSON(
        title = "Unconscious Person",
        description = "Airway management, spinal protection, and responsiveness protocol."
    ),
    HEAVY_BLEEDING(
        title = "Heavy Bleeding",
        description = "Direct pressure, sterile barriers, and severe hemorrhage control."
    ),
    POSSIBLE_FRACTURE(
        title = "Possible Fracture",
        description = "Limb stabilization, joint immobilization, and pain reduction."
    ),
    VEHICLE_DANGER(
        title = "Vehicle Danger",
        description = "Ignition cutoff, battery safety, and toxic fluid leak precautions."
    ),
    FIRE_SMOKE(
        title = "Fire & Smoke",
        description = "Upwind evacuation, fire extinguisher protocols, and fuel fires."
    ),
    ROADSIDE_COLLISION(
        title = "Roadside Collision",
        description = "Traffic triangle perimeter, high-visibility, and active lane defense."
    ),
    GENERAL_SCENE_SAFETY(
        title = "General Scene Safety",
        description = "Golden Hour hierarchy, 112 delegation, and Good Samaritan precautions."
    )
}

data class SafetyStep(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val caution: String? = null
)

data class LocalizedSafetyContent(
    val category: SafetyCategory,
    val language: RegionalLanguage,
    val title: String,
    val summary: String,
    val steps: List<SafetyStep>,
    val operatorNotice: String = "Follow emergency operator (112 / 108) instructions. Do not provide unsupported medical diagnosis."
)

object SafetyGuidanceRepository {

    fun getGuidance(category: SafetyCategory, language: RegionalLanguage): LocalizedSafetyContent {
        return when (language) {
            RegionalLanguage.HINDI -> getHindiGuidance(category)
            RegionalLanguage.KANNADA -> getKannadaGuidance(category)
            RegionalLanguage.TAMIL -> getTamilGuidance(category)
            RegionalLanguage.ENGLISH -> getEnglishGuidance(category)
        }
    }

    fun getAllCategories(): List<SafetyCategory> = SafetyCategory.entries

    private fun getEnglishGuidance(category: SafetyCategory): LocalizedSafetyContent {
        return when (category) {
            SafetyCategory.UNCONSCIOUS_PERSON -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "Unconscious Person Protocol",
                summary = "Critical airway, breathing, and spinal stabilization.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Verify Scene Safety & Check Response",
                        instruction = "Ensure traffic has stopped. Tap the victim's shoulders firmly and ask loudly: 'Are you okay?'",
                        caution = "Do NOT shake violently if spinal injury is suspected."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Do NOT Remove Helmet Unnecessarily",
                        instruction = "Leave the helmet securely in place. Only trained medics should remove it unless the airway is critically obstructed.",
                        caution = "Removing helmet can cause catastrophic spinal cord severance."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Assess Breathing & Clear Airway",
                        instruction = "Look, listen, and feel for chest movement for 10 seconds. Gently lift chin if neck is neutral to clear airway.",
                        caution = "Do not force head backward."
                    ),
                    SafetyStep(
                        stepNumber = 4,
                        title = "Place in Recovery Position (If Breathing)",
                        instruction = "If breathing normally and no spinal fracture is evident, gently roll onto side to prevent choking on saliva or vomit.",
                        caution = "Maintain spine alignment as a single unit when rolling."
                    )
                )
            )
            SafetyCategory.HEAVY_BLEEDING -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "Severe Bleeding Control",
                summary = "Rapid hemorrhage containment during Golden Hour.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Apply Continuous Direct Pressure",
                        instruction = "Place sterile gauze or the cleanest cloth available directly over the wound. Press down firmly with both hands.",
                        caution = "Do NOT release pressure to check the wound for at least 10 minutes."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Do NOT Remove Soaked Cloths",
                        instruction = "If blood soaks through, place additional cloth layers directly on top and press harder.",
                        caution = "Removing soaked layers pulls away forming blood clots."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Elevate Injured Limb",
                        instruction = "If there is no suspected bone fracture, elevate the bleeding arm or leg above heart level.",
                        caution = "Do not elevate if bone deformity or extreme pain is present."
                    ),
                    SafetyStep(
                        stepNumber = 4,
                        title = "Secure Pressure Bandage",
                        instruction = "Wrap firmly with a bandage or clean cloth. Ensure distal fingers/toes remain pink and warm.",
                        caution = "Do not cut off arterial circulation completely unless trained in tourniquet use."
                    )
                )
            )
            SafetyCategory.POSSIBLE_FRACTURE -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "Bone & Joint Immobilization",
                summary = "Prevent internal tissue and vascular damage from broken bones.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Do NOT Attempt Bone Realignment",
                        instruction = "Support the injured bone or joint in the exact position found. Never attempt to push bones back into place.",
                        caution = "Forcing alignment tears nerves and blood vessels."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Immobilize the Joint Above and Below",
                        instruction = "Use rolled magazines, cardboard, or sticks padded with cloth to create a rigid splint.",
                        caution = "Tie splints gently with cloth strips; do not knot directly over injury."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Apply Cold Pack (Indirect)",
                        instruction = "Apply ice or cold cloth wrapped in a towel for 15 minutes to reduce swelling.",
                        caution = "Never put bare ice directly against skin."
                    )
                )
            )
            SafetyCategory.VEHICLE_DANGER -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "Vehicle Hazard Management",
                summary = "Eliminate secondary fire, electrical, and fuel risks.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Cut Off Engine Ignition",
                        instruction = "Reach in and switch off ignition key or push power button to stop fuel pumps and electrical sparks.",
                        caution = "Ensure vehicle handbrake is pulled to prevent rolling."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Switch On Hazard Lights",
                        instruction = "Turn on emergency 4-way flashers to warn oncoming traffic in low light.",
                        caution = "Do not smoke, use matches, or use metal flares near fuel spills."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Watch For Fuel & Battery Leaks",
                        instruction = "Keep a distance of at least 15 meters if strong petrol/diesel odor or sizzling battery acid is present.",
                        caution = "Disconnect negative 12V battery terminal only if safely accessible."
                    )
                )
            )
            SafetyCategory.FIRE_SMOKE -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "Fire & Smoke Containment",
                summary = "Evacuation and fire suppression guidelines.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Move Upwind Immediately",
                        instruction = "Direct all conscious victims and bystanders upwind and uphill to avoid inhaling toxic vehicle fumes.",
                        caution = "Burning plastic/tires releases lethal carbon monoxide and cyanide gas."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Use Dry-Powder (ABC) Extinguisher",
                        instruction = "Aim at the base of the flame. Sweep side-to-side. Do NOT open the vehicle hood fully if flames are underneath.",
                        caution = "Opening hood rushes oxygen to engine fire, causing flash flare-up."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Call Fire Brigade (101 / 112)",
                        instruction = "Inform operator of vehicle type (petrol, diesel, CNG, or EV/Lithium battery).",
                        caution = "EV battery thermal runaway requires massive continuous water cooling."
                    )
                )
            )
            SafetyCategory.ROADSIDE_COLLISION -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "Roadside Traffic Perimeter",
                summary = "Protect the crash scene from secondary high-speed impacts.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Deploy Warning Triangles",
                        instruction = "Place reflective hazard triangles 50 to 100 meters behind the crash location facing oncoming traffic.",
                        caution = "On high-speed highways, place triangle at least 100m back on road shoulder."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Wear Reflective Wear or Flashlight",
                        instruction = "Use phone flashlights waving downward to warn oncoming drivers. Never stand in the active travel lane.",
                        caution = "Always face oncoming traffic while standing near the shoulder."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Guide Traffic Carefully",
                        instruction = "Assign one bystander to calmly wave vehicles into the open lane at safe distance.",
                        caution = "Never step into the path of approaching heavy trucks."
                    )
                )
            )
            SafetyCategory.GENERAL_SCENE_SAFETY -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.ENGLISH,
                title = "General Scene & Good Samaritan Safety",
                summary = "Protect yourself first, delegate help, and honor the Golden Hour.",
                steps = listOf(
                    SafetyStep(
                        stepNumber = 1,
                        title = "Your Safety Is Priority #1",
                        instruction = "Stop, Look, and Assess before rushing in. A dead or injured helper cannot rescue anyone.",
                        caution = "Never enter live highway lanes or power line hazard areas."
                    ),
                    SafetyStep(
                        stepNumber = 2,
                        title = "Call 112 / 108 in the First 5 Minutes",
                        instruction = "State exact GPS location, number of injured, and conscious state. Clear communication saves Golden Hour lives.",
                        caution = "Stay on line until emergency operator confirms address details."
                    ),
                    SafetyStep(
                        stepNumber = 3,
                        title = "Delegate Specific Tasks to Bystanders",
                        instruction = "Point at individuals: 'You in the blue shirt, call 112. You on the right, wave oncoming traffic around.'",
                        caution = "General calls like 'someone call police' cause bystander inaction."
                    ),
                    SafetyStep(
                        stepNumber = 4,
                        title = "Never Administer Food or Water to Victims",
                        instruction = "Injured or unconscious victims can easily aspirate water into their lungs or require immediate emergency surgery.",
                        caution = "Oral liquids prior to anesthesia can cause lethal pulmonary aspiration."
                    )
                )
            )
        }
    }

    private fun getHindiGuidance(category: SafetyCategory): LocalizedSafetyContent {
        val english = getEnglishGuidance(category)
        return when (category) {
            SafetyCategory.UNCONSCIOUS_PERSON -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.HINDI,
                title = "बेहोश व्यक्ति के लिए प्राथमिक उपचार",
                summary = "श्वसन नली, सांस और रीढ़ की हड्डी की सुरक्षा प्रोटोकॉल।",
                steps = listOf(
                    SafetyStep(1, "घटनास्थल की सुरक्षा जांचें", "पीड़ित के कंधे थपथपाएं और जोर से पूछें: 'क्या आप ठीक हैं?'", "रीढ़ की चोट का संदेह होने पर जोर से न हिलाएं।"),
                    SafetyStep(2, "हेलमेट को जबरदस्ती न हटाएं", "हेलमेट लगा रहने दें। केवल सांस रुकने पर ही प्रशिक्षित व्यक्ति हटाए।", "हेलमेट हटाने से रीढ़ की हड्डी को गंभीर नुकसान हो सकता है।"),
                    SafetyStep(3, "सांस की जांच करें", "10 सेकंड तक सीने की हलचल देखें और सांस महसूस करें।", "सिर को अधिक पीछे न झुकाएं।"),
                    SafetyStep(4, "रिकवरी पोजीशन (करवट) में लिटाएं", "यदि सांस चल रही है तो उल्टी से बचाव के लिए धीरे से करवट दिलाएं।", "रीढ़ को सीधा रखते हुए मोड़ें।")
                ),
                operatorNotice = "आपातकालीन ऑपरेटर (112/108) के निर्देशों का पालन करें। अप्रमाणित चिकित्सा निदान न दें।"
            )
            SafetyCategory.HEAVY_BLEEDING -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.HINDI,
                title = "गंभीर रक्तस्राव नियंत्रण",
                summary = "गोल्डन ऑवर में तेजी से खून का बहाव रोकना।",
                steps = listOf(
                    SafetyStep(1, "लगातार सीधा दबाव बनाएं", "साफ कपड़े या पट्टी से घाव पर दोनों हाथों से लगातार दबाव डालें।", "कम से कम 10 मिनट तक कपड़ा न हटाएं।"),
                    SafetyStep(2, "भीगे हुए कपड़े को न हटाएं", "यदि खून बहता रहे तो ऊपर से और साफ कपड़ा रखकर दबाएं।", "कपड़ा हटाने से खून के थक्के टूट जाते हैं।"),
                    SafetyStep(3, "अंग को ऊपर उठाएं", "यदि हड्डी नहीं टूटी है तो चोटिल हाथ या पैर को दिल के स्तर से ऊपर उठाएं।", "टूटी हड्डी को न हिलाएं।")
                ),
                operatorNotice = "आपातकालीन ऑपरेटर (112/108) के निर्देशों का पालन करें।"
            )
            else -> english.copy(language = RegionalLanguage.HINDI)
        }
    }

    private fun getKannadaGuidance(category: SafetyCategory): LocalizedSafetyContent {
        val english = getEnglishGuidance(category)
        return when (category) {
            SafetyCategory.UNCONSCIOUS_PERSON -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.KANNADA,
                title = "ಪ್ರಜ್ಞಾಹೀನ ವ್ಯಕ್ತಿಗೆ ಪ್ರಥಮ ಚಿಕಿತ್ಸೆ",
                summary = "ಉಸಿರಾಟ, ಕುತ್ತಿಗೆ ಮತ್ತು ಬೆನ್ನುಮೂಳೆಯ ರಕ್ಷಣೆಯ ವಿಧಾನ.",
                steps = listOf(
                    SafetyStep(1, "ಸುರಕ್ಷತೆ ಪರಿಶೀಲಿಸಿ & ಪ್ರತಿಕ್ರಿಯೆ ನೋಡಿ", "ಭುಜ ತಟ್ಟಿ ಜೋರಾಗಿ ಕೇಳಿ: 'ನೀವು ಆರಾಮವಾಗಿದ್ದೀರಾ?'", "ಬೆನ್ನುಮೂಳೆ ಗಾಯವಾಗಿದ್ದರೆ ಅತಿಯಾಗಿ ಅಲುಗಾಡಿಸಬೇಡಿ."),
                    SafetyStep(2, "ಹೆಲ್ಮೆಟ್ ಬಲವಂತವಾಗಿ ತೆಗೆಯಬೇಡಿ", "ಹೆಲ್ಮೆಟ್ ಹಾಗೆಯೇ ಇರಲಿ. ತೀವ್ರ ಉಸಿರಾಟದ ಸಮಸ್ಯೆಯಿಲ್ಲದಿದ್ದರೆ ತೆಗೆಯಬೇಡಿ.", "ಹೆಲ್ಮೆಟ್ ತೆಗೆದರೆ ನರಗಳಿಗೆ ಅಪಾಯವಾಗಬಹುದು."),
                    SafetyStep(3, "ಉಸಿರಾಟ ಪರಿಶೀಲಿಸಿ", "ಎದೆಯ ಏರಿಳಿತವನ್ನು 10 ಸೆಕೆಂಡುಗಳ ಕಾಲ ಸೂಕ್ಷ್ಮವಾಗಿ ಗಮನಿಸಿ.", "ತಲೆಯನ್ನು ಅತಿಯಾಗಿ ಹಿಂದಕ್ಕೆ ಬಗ್ಗಿಸಬೇಡಿ.")
                ),
                operatorNotice = "ತುರ್ತು ಸೇವಾ ಆಪರೇಟರ್ (112/108) ಸೂಚನೆಗಳನ್ನು ಪಾಲಿಸಿ."
            )
            else -> english.copy(language = RegionalLanguage.KANNADA)
        }
    }

    private fun getTamilGuidance(category: SafetyCategory): LocalizedSafetyContent {
        val english = getEnglishGuidance(category)
        return when (category) {
            SafetyCategory.UNCONSCIOUS_PERSON -> LocalizedSafetyContent(
                category = category,
                language = RegionalLanguage.TAMIL,
                title = "மயக்கமடைந்த நபருக்கான முதலுதவி",
                summary = "சுவாசப்பாதை மற்றும் முதுகெலும்பு பாதுகாப்பு நெறிமுறை.",
                steps = listOf(
                    SafetyStep(1, "பாதுகாப்பை உறுதி செய்து சோதிக்கவும்", "தோள்பட்டையைத் தட்டி சத்தமாகக் கேளுங்கள்: 'நீங்கள் நலமாக உள்ளீர்களா?'", "முதுகெலும்பு காயம் இருந்தால் அதிகமாக அசைக்க வேண்டாம்."),
                    SafetyStep(2, "ஹெல்மெட்டை அகற்ற வேண்டாம்", "ஹெல்மெட்டை அகற்றாமல் அப்படியே வைக்கவும்.", "ஹெல்மெட்டை கழற்றினால் கழுத்து எலும்பு முறிய வாய்ப்புள்ளது.")
                ),
                operatorNotice = "அவசர சேவை (112/108) வழிமுறைகளைப் பின்பற்றவும்."
            )
            else -> english.copy(language = RegionalLanguage.TAMIL)
        }
    }
}
