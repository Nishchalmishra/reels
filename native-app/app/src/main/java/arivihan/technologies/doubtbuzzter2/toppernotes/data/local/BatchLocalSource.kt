package arivihan.technologies.doubtbuzzter2.toppernotes.data.local

import arivihan.technologies.doubtbuzzter2.MyApplication
import arivihan.technologies.doubtbuzzter2.enums.preference.PreferenceKeys
import javax.inject.Inject
import javax.inject.Singleton

/** Reads the active batch the rest of the app already stores in prefs. */
@Singleton
class BatchLocalSource @Inject constructor() {

    fun batchId(): Int = MyApplication.getIntData(PreferenceKeys.BATCH_ID.getKey())
}
