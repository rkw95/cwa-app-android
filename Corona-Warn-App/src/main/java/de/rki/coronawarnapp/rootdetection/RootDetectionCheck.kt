package de.rki.coronawarnapp.rootdetection

import com.scottyab.rootbeer.RootBeer
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class RootDetectionCheck @Inject constructor(
    private val rootBeer: RootBeer,
    private val dispatcherProvider: DispatcherProvider
) {

    // Check should run in a background thread cause it uses I/O
    suspend fun isRooted() = withContext(dispatcherProvider.IO) {
        Timber.d("isRooted()")
        isRooted
            .also { Timber.d("Device is rooted: %s", it) }
    }

    private val isRooted: Boolean
        get() = try {
            rootBeer.isRooted
        } catch (e: Exception) {
            Timber.e(e, "Root detection failed. Returning false")
            false
        }
}
