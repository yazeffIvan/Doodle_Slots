package com.example.doodleslots.data

import android.content.Context


class GameRepository(context: Context) {
    private val prefs = context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    // --- Деньги и покупки ---
    fun getMoney(): Int = prefs.getInt("money", 100)
    fun saveMoney(money: Int) = prefs.edit().putInt("money", money).apply()

    fun getCurrentSkin(): String = prefs.getString("current_skin", "default") ?: "default"
    fun saveCurrentSkin(skin: String) = prefs.edit().putString("current_skin", skin).apply()

    fun getCurrentTrail(): String = prefs.getString("current_trail", "green") ?: "green"
    fun saveCurrentTrail(trail: String) = prefs.edit().putString("current_trail", trail).apply()

    fun getOwnedSkins(): Set<String> = prefs.getStringSet("owned_skins", setOf("default")) ?: setOf("default")
    fun saveOwnedSkins(skins: Set<String>) = prefs.edit().putStringSet("owned_skins", skins).apply()

    fun getOwnedTrails(): Set<String> = prefs.getStringSet("owned_trails", setOf("green")) ?: setOf("green")
    fun saveOwnedTrails(trails: Set<String>) = prefs.edit().putStringSet("owned_trails", trails).apply()

    // --- Настройки ---
    /** Множитель скорости персонажа: 1.0 по умолчанию. Варианты: 1.0 / 1.4 / 1.8 / 2.4 / 3.0 */
    fun getSpeedMultiplier(): Float = prefs.getFloat("speed_multiplier", 1.4f)
    fun saveSpeedMultiplier(value: Float) = prefs.edit().putFloat("speed_multiplier", value).apply()

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun saveSoundEnabled(v: Boolean) = prefs.edit().putBoolean("sound_enabled", v).apply()

    fun isVibrationEnabled(): Boolean = prefs.getBoolean("vibration_enabled", true)
    fun saveVibrationEnabled(v: Boolean) = prefs.edit().putBoolean("vibration_enabled", v).apply()

    /** Тема интерфейса: "neon" / "matrix" / "sunset" / "winter" */
    fun getThemeId(): String = prefs.getString("theme_id", "neon") ?: "neon"
    fun saveThemeId(id: String) = prefs.edit().putString("theme_id", id).apply()

    fun isDepoSlamMode(): Boolean = prefs.getBoolean("depo_slam", false)
    fun saveDepoSlamMode(v: Boolean) = prefs.edit().putBoolean("depo_slam", v).apply()

    /** Сброс всего прогресса к дефолту */
    fun resetProgress() {
        prefs.edit()
            .putInt("money", 100)
            .putString("current_skin", "default")
            .putString("current_trail", "green")
            .putStringSet("owned_skins", setOf("default"))
            .putStringSet("owned_trails", setOf("green"))
            .apply()
    }
}