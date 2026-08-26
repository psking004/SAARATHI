package com.example.domain.safety

data class GoodSamaritanRight(
    val title: String,
    val description: String,
    val legalReference: String,
    val iconName: String
)

object GoodSamaritanProtection {
    val legalRights = listOf(
        GoodSamaritanRight(
            title = "Zero Civil or Criminal Liability",
            description = "A Good Samaritan who assists a road crash victim shall not be liable for any civil or criminal action for any injury or death resulting from any assistance rendered in good faith.",
            legalReference = "Section 134A(1), Motor Vehicles (Amendment) Act & Supreme Court Guidelines",
            iconName = "shield"
        ),
        GoodSamaritanRight(
            title = "No Forced Police Interrogation",
            description = "You cannot be forced to reveal your name, personal identity, or phone number to the police or hospital staff upon transporting a victim. Disclosure is entirely voluntary.",
            legalReference = "Ministry of Road Transport & Highways (MoRTH) Standard Operating Procedures",
            iconName = "security"
        ),
        GoodSamaritanRight(
            title = "Hospitals Cannot Demand Advance Payment",
            description = "All registered public and private hospitals are legally mandated to admit and provide immediate emergency first-aid and trauma resuscitation without demanding payment or registration deposits from the bystander.",
            legalReference = "Supreme Court Judgment in Pt. Parmanand Katara vs Union of India",
            iconName = "medical_services"
        ),
        GoodSamaritanRight(
            title = "Voluntary Eyewitness Protection",
            description = "If you voluntarily choose to become a witness, examination can only occur at a time and place of your convenience, or via single-session video conference, without detention or repeated court summons.",
            legalReference = "Supreme Court Guidelines for Good Samaritan Examination",
            iconName = "gavel"
        ),
        GoodSamaritanRight(
            title = "Cash Reward & Certificate",
            description = "Under the MoRTH Good Samaritan Scheme, citizens who rush trauma victims to hospital during the Golden Hour are eligible for government cash recognition awards and commendation certificates.",
            legalReference = "National Road Safety Council Guidelines",
            iconName = "military_tech"
        )
    )

    val coreSafetyRules = listOf(
        "1. Never put yourself in lethal danger (e.g. oncoming high-speed trucks or live power lines).",
        "2. Call National Emergency 112 or Ambulance 108 immediately.",
        "3. Follow the guidance of official 112 emergency service operators.",
        "4. Professional responders and paramedics remain the top priority."
    )
}
