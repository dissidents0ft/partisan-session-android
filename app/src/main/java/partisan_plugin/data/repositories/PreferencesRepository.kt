package partisan_plugin.data.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import partisan_plugin.domain.entities.AppExitAction
import partisan_plugin.domain.entities.AppStartAction
import javax.inject.Inject

/**
 * Class to work with unprotected partisan preferences
 */
class PreferencesRepository @Inject constructor(@ApplicationContext private val context: Context) {


    private fun getIntegerPreference(key: String, defaultValue: Int): Int {
        return context.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE).getInt(key,defaultValue)
    }

    private fun setIntegerPreference(key: String, value: Int) {
        context.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE).edit().putInt(key,value).apply()
    }

    private fun getStringPreference(key: String, defaultValue: String?): String? {
        return context.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE).getString(key,defaultValue)
    }

    private fun setStringPreference(key: String, value: String) {
        context.getSharedPreferences(PREFERENCES_FILE,Context.MODE_PRIVATE).edit().putString(key,value).apply()
    }

    private inline fun <reified T : Enum<T>> getEnumPreference(key: String, defaultValue: T): T {
        val representation = getStringPreference(key, defaultValue.name)
        return enumValueOf<T>(representation!!)
    }

    private inline fun <reified T : Enum<T>> setEnumPreference(key: String, value: T) {
        val representation = value.name
        setStringPreference(key, representation)
    }


    fun getStorageSize(size: Int) {
        setIntegerPreference(DATABASE_SIZE, size)
    }

    fun getStorageSize(): Int {
        return getIntegerPreference(DATABASE_SIZE, 15)
    }

    fun getBackgroundClearTimeout(): Int {
        return getIntegerPreference(BACKGROUND_CLEAR_TIMEOUT, 30)
    }

    fun setBackgroundClearTimeout(timeout: Int) {
        setIntegerPreference(BACKGROUND_CLEAR_TIMEOUT, timeout)
    }

    fun setAppExitAction(value: AppExitAction) {
        setEnumPreference(APP_EXIT_ACTION, value)
    }

    fun getAppExitAction(): AppExitAction {
        return getEnumPreference(APP_EXIT_ACTION, AppExitAction.DO_NOTHING)
    }

    /**
     * Function for setting action that application should perform on start.
     * @param value - start action
     */
    fun setAppStartAction(value: AppStartAction) {
        setEnumPreference(APP_START_ACTION, value)
    }

    /**
     * Function for getting action that application should perform on start.
     * @return acton that application should perform on start. Default - SETUP_DATABASE.
     */
    fun getAppStartAction(): AppStartAction {
        return getEnumPreference(APP_START_ACTION, AppStartAction.SETUP_DATABASE)
    }

    companion object {
        const val PREFERENCES_FILE = "Partisan-Preferences"
        private const val BACKGROUND_CLEAR_TIMEOUT = "background_clear_timeout"
        private const val APP_START_ACTION = "app_start_action"
        private const val APP_EXIT_ACTION = "app_start_action"
        private const val DATABASE_SIZE = "database_length"
    }

}