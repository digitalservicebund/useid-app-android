package de.digitalService.useID.idCardInterface

import de.governikus.ausweisapp2.sdkwrapper.card.core.AccessRight

// TR-03110 (Part 4), Section 2.2.3
// TR-03127 (Anhang C)
enum class IdCardAttribute {
    DG01,
    DG02,
    DG03,
    DG04,
    DG05,
    DG06,
    DG07,
    DG08,
    DG09,
    DG10,
    DG13,
    DG17,
    DG18,
    DG19,
    DG20,
    PSEUDONYM,
    AGE_VERIFICATION,
    ADDRESS_VERIFICATION,
    WRITE_DG17,
    WRITE_DG18,
    WRITE_DG19,
    WRITE_DG20,
    CAN_ALLOWED,
    PIN_MANAGEMENT;

    companion object {
        fun fromAccessRight(accessRight: AccessRight) = when (accessRight) {
            AccessRight.ADDRESS -> DG17
            AccessRight.BIRTH_NAME -> DG13
            AccessRight.FAMILY_NAME -> DG05
            AccessRight.GIVEN_NAMES -> DG04
            AccessRight.PLACE_OF_BIRTH -> DG09
            AccessRight.DATE_OF_BIRTH -> DG08
            AccessRight.DOCTORAL_DEGREE -> DG07
            AccessRight.ARTISTIC_NAME -> DG06
            AccessRight.PSEUDONYM -> PSEUDONYM
            AccessRight.VALID_UNTIL -> DG03
            AccessRight.NATIONALITY -> DG10
            AccessRight.ISSUING_COUNTRY -> DG02
            AccessRight.DOCUMENT_TYPE -> DG01
            AccessRight.RESIDENCE_PERMIT_I -> DG19
            AccessRight.RESIDENCE_PERMIT_II -> DG20
            AccessRight.COMMUNITY_ID -> DG18
            AccessRight.ADDRESS_VERIFICATION -> ADDRESS_VERIFICATION
            AccessRight.AGE_VERIFICATION -> AGE_VERIFICATION
            AccessRight.WRITE_ADDRESS -> WRITE_DG17
            AccessRight.WRITE_COMMUNITY_ID -> WRITE_DG18
            AccessRight.WRITE_RESIDENCE_PERMIT_I -> WRITE_DG19
            AccessRight.WRITE_RESIDENCE_PERMIT_II -> WRITE_DG20
            AccessRight.CAN_ALLOWED -> CAN_ALLOWED
            AccessRight.PIN_MANAGEMENT -> PIN_MANAGEMENT
        }
    }
}
