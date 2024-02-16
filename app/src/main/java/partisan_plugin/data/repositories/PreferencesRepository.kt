package partisan_plugin.data.repositories

import android.content.Context
import android.util.Base64
import partisan_plugin.domain.entities.AppExitAction
import partisan_plugin.domain.entities.AppStartAction

/**
 * Class to work with partisan preferences
 */
object PreferencesRepository {
    private const val PREFERENCES = "Partisan-Preferences"
    private const val BACKGROUND_CLEAR_TIMEOUT = "background_clear_timeout"
    private const val APP_START_ACTION = "app_start_action"
    private const val APP_EXIT_ACTION = "app_start_action"
    private const val PARTISAN_PREFIX = "patisan_prefix"
    private const val DATABASE_SIZE = "database_length"
    private fun getIntegerPreference(context: Context, key: String, defaultValue: Int): Int {
        return context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).getInt(key,defaultValue)
    }

    private fun setIntegerPreference(context: Context, key: String, value: Int) {
        context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).edit().putInt(key,value).apply()
    }

    private fun getStringPreference(context: Context, key: String, defaultValue: String?): String? {
        return context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).getString(key,defaultValue)
    }

    private fun setStringPreference(context: Context, key: String, value: String) {
        context.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE).edit().putString(key,value).apply()
    }

    private inline fun <reified T : Enum<T>> getEnumPreference(context: Context, key: String, defaultValue: T): T {
        val representation = getStringPreference(context, key, defaultValue.name)
        return enumValueOf<T>(representation!!)
    }

    private inline fun <reified T : Enum<T>> setEnumPreference(context: Context, key: String, value: T) {
        val representation = value.name
        setStringPreference(context, key, representation)
    }


    @JvmStatic
    fun setDatabaseSize(context: Context, size: Int) {
        setIntegerPreference(context, DATABASE_SIZE, size)
    }

    @JvmStatic
    fun getDatabaseSize(context: Context): Int {
        return getIntegerPreference(context, DATABASE_SIZE, 15)
    }

    /**
     * Function for getting partisan prefix.
     * @return partisan prefix or null, if prefix is absent. Prefix should be set up, if prefix is not set up this may cause crash!
     */
    @JvmStatic
    fun getPartisanPrefix(context: Context): String? {
        return getStringPreference(context, PARTISAN_PREFIX, null)
    }

    /**
     * Function for setting partisan prefix.
     * @param prefix - partisan prefix entered by user
     */
    @JvmStatic
    fun setPartisanPrefix(context: Context, prefix: String) {
        setStringPreference(context, PARTISAN_PREFIX, prefix)
    }

    @JvmStatic
    fun getBackgroundClearTimeout(context: Context): Int {
        return getIntegerPreference(context, BACKGROUND_CLEAR_TIMEOUT, 30)
    }

    @JvmStatic
    fun setBackgroundClearTimeout(context: Context, timeout: Int) {
        setIntegerPreference(context, BACKGROUND_CLEAR_TIMEOUT, timeout)
    }

    @JvmStatic
    fun setAppExitAction(context: Context, value: AppExitAction) {
        setEnumPreference(context, APP_EXIT_ACTION, value)
    }

    @JvmStatic
    fun getAppExitAction(context: Context): AppExitAction {
        return getEnumPreference(context, APP_EXIT_ACTION, AppExitAction.DO_NOTHING)
    }

    /**
     * Function for setting action that application should perform on start.
     * @param value - start action
     */
    @JvmStatic
    fun setAppStartAction(context: Context, value: AppStartAction) {
        setEnumPreference(context, APP_START_ACTION, value)
    }

    /**
     * Function for getting action that application should perform on start.
     * @return acton that application should perform on start. Default - SETUP_DATABASE.
     */
    @JvmStatic
    fun getAppStartAction(context: Context): AppStartAction {
        return getEnumPreference(context, APP_START_ACTION, AppStartAction.SETUP_DATABASE)
    }



}