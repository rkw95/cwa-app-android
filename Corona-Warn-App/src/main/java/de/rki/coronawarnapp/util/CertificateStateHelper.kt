package de.rki.coronawarnapp.util

import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isGone
import androidx.core.view.isVisible
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.covidcertificate.common.certificate.CwaCovidCertificate
import de.rki.coronawarnapp.covidcertificate.recovery.core.RecoveryCertificate
import de.rki.coronawarnapp.covidcertificate.test.core.TestCertificate
import de.rki.coronawarnapp.covidcertificate.vaccination.core.VaccinationCertificate
import de.rki.coronawarnapp.databinding.IncludeCertificateQrcodeCardBinding
import de.rki.coronawarnapp.util.ContextExtensions.getDrawableCompat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toLocalDateTimeUserTz
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortDayFormat
import de.rki.coronawarnapp.util.TimeAndDateExtensions.toShortTimeFormat

@Suppress("LongParameterList", "ComplexMethod")
fun IncludeCertificateQrcodeCardBinding.bindValidityViews(
    certificate: CwaCovidCertificate,
    isPersonOverview: Boolean = false,
    isPersonDetails: Boolean = false,
    isCertificateDetails: Boolean = false,
    badgeCount: Int = 0,
    onCovPassInfoAction: () -> Unit
) {
    val valid = certificate.isValid
    val context = root.context
    covpassInfoTitle.isVisible = valid
    covpassInfoButton.isVisible = valid
    covpassInfoButton.setOnClickListener { onCovPassInfoAction() }

    invalidOverlay.isGone = valid || (isCertificateDetails && !certificate.isNotBlocked)
    image.isEnabled = isCertificateDetails && (valid || !certificate.isNotBlocked) // Disable Qr-Code full-screen mode

    val isNewTestCertificate = certificate is TestCertificate && certificate.isNew
    notificationBadge.isVisible = if (isNewTestCertificate) {
        false
    } else {
        isCertificateDetails &&
            certificate.hasNotificationBadge &&
            certificate.getState() !is CwaCovidCertificate.State.Valid
    }

    qrTitle.isVisible = !isPersonOverview
    qrSubtitle.isVisible = !isPersonOverview

    when (certificate) {
        is TestCertificate -> {
            val dateTime = certificate.sampleCollectedAt.toLocalDateTimeUserTz().run {
                "${toShortDayFormat()}, ${toShortTimeFormat()}"
            }

            qrTitle.text = context.getString(R.string.test_certificate_name)
            qrSubtitle.text = context.getString(R.string.test_certificate_qrcode_card_sampled_on, dateTime)
        }
        is VaccinationCertificate -> {
            qrTitle.text = context.getString(R.string.vaccination_details_subtitle)
            qrSubtitle.text = context.getString(
                R.string.vaccination_certificate_vaccinated_on,
                certificate.vaccinatedOn.toShortDayFormat()
            )
        }
        is RecoveryCertificate -> {
            qrTitle.text = context.getString(R.string.recovery_certificate_name)
            qrSubtitle.text = context.getString(
                R.string.recovery_certificate_valid_until,
                certificate.validUntil.toShortDayFormat()
            )
        }
    }
    when (certificate.displayedState()) {
        is CwaCovidCertificate.State.ExpiringSoon -> {
            expirationStatusIcon.isVisible = isCertificateDetails
            (expirationStatusIcon.layoutParams as ConstraintLayout.LayoutParams).verticalBias = 0f
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_av_timer))
            expirationStatusText.isVisible = isCertificateDetails
            expirationStatusText.text = context.getString(
                R.string.certificate_qr_expiration,
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortDayFormat(),
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortTimeFormat()
            )
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.expiration_info)
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Expired -> {
            expirationStatusIcon.isVisible = badgeCount == 0
            (expirationStatusIcon.layoutParams as ConstraintLayout.LayoutParams).verticalBias = 1.0f
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            expirationStatusText.isVisible = badgeCount == 0
            expirationStatusText.text = context.getText(R.string.certificate_qr_expired)
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.expired_certificate_info)
            qrSubtitle.isVisible = false
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Invalid -> {
            expirationStatusIcon.isVisible = badgeCount == 0
            (expirationStatusIcon.layoutParams as ConstraintLayout.LayoutParams).verticalBias = 0f
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            expirationStatusText.isVisible = badgeCount == 0
            expirationStatusText.text = context.getText(R.string.certificate_qr_invalid_signature)
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.invalid_certificate_signature_info)
            qrSubtitle.isVisible = false
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Blocked -> {
            expirationStatusIcon.isVisible = badgeCount == 0
            (expirationStatusIcon.layoutParams as ConstraintLayout.LayoutParams).verticalBias = 0f
            expirationStatusIcon.setImageDrawable(context.getDrawableCompat(R.drawable.ic_error_outline))
            expirationStatusText.isVisible = badgeCount == 0
            expirationStatusText.text = context.getText(R.string.error_dcc_in_blocklist_title)
            expirationStatusBody.isVisible = isCertificateDetails
            expirationStatusBody.text = context.getText(R.string.error_dcc_in_blocklist_message)
            qrSubtitle.isVisible = false
            startValidationCheckButton.isVisible = isPersonDetails
        }

        is CwaCovidCertificate.State.Valid -> {
            expirationStatusIcon.isVisible = false
            expirationStatusText.isVisible = false
            expirationStatusBody.isVisible = false
            qrTitle.isVisible = isPersonDetails
            qrSubtitle.isVisible = isPersonDetails
            startValidationCheckButton.isVisible = isPersonDetails
        }
    }
    certificateBadgeCount.isVisible = badgeCount != 0
    certificateBadgeCount.text = badgeCount.toString()
    certificateBadgeText.isVisible = badgeCount != 0
}

fun TextView.displayExpirationState(certificate: CwaCovidCertificate) {
    when (certificate.displayedState()) {
        is CwaCovidCertificate.State.ExpiringSoon -> {
            isVisible = true
            text = context.getString(
                R.string.certificate_person_details_card_expiration,
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortDayFormat(),
                certificate.headerExpiresAt.toLocalDateTimeUserTz().toShortTimeFormat()
            )
        }

        is CwaCovidCertificate.State.Expired -> {
            isVisible = true
            text = context.getText(R.string.certificate_qr_expired)
        }

        is CwaCovidCertificate.State.Invalid -> {
            isVisible = true
            text = context.getText(R.string.certificate_qr_invalid_signature)
        }

        is CwaCovidCertificate.State.Valid -> {
            isVisible = false
            if (certificate.isNew) {
                isVisible = true
                text = context.getText(R.string.test_certificate_qr_new)
            }
        }
        CwaCovidCertificate.State.Blocked -> {
            isVisible = true
            text = context.getText(R.string.error_dcc_in_blocklist_title)
        }
    }
}

val CwaCovidCertificate.europaStarsResource
    get() = when {
        isValid -> R.drawable.ic_eu_stars_blue
        else -> R.drawable.ic_eu_stars_grey
    }

val CwaCovidCertificate.expendedImageResource
    get() = when {
        isValid -> R.drawable.certificate_complete_gradient
        else -> R.drawable.vaccination_incomplete
    }

/**
 * Display state is just for UI purpose only and does change the state for Test Certificate only
 */
private fun CwaCovidCertificate.displayedState(): CwaCovidCertificate.State = when (this) {
    is TestCertificate -> if (isValid) {
        CwaCovidCertificate.State.Valid(headerExpiresAt)
    } else {
        CwaCovidCertificate.State.Invalid()
    }
    else -> getState()
}
