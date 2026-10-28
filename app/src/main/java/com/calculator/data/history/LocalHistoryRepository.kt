package com.calculator.data.history

import android.content.Context
import android.content.SharedPreferences
import com.calculator.ui.CalculatorMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(
    val id: Long,
    val expression: String,
    val result: String,
    val mode: CalculatorMode,
    val timestampMs: Long = System.currentTimeMillis()
)

interface HistoryRepository {
    val historyTape: Flow<List<HistoryItem>>
    val currentHistory: List<HistoryItem>
    suspend fun addRecord(expression: String, result: String, mode: CalculatorMode)
    suspend fun deleteRecord(id: Long)
    suspend fun clearHistory()
}

/**
 * Local persistent calculation history repository.
 * Stores up to 100 calculation entries strictly on-device in private app storage.
 */
class LocalHistoryRepository(context: Context? = null) : HistoryRepository {

    private val prefs: SharedPreferences? = context?.getSharedPreferences(HISTORY_PREFS_NAME, Context.MODE_PRIVATE)
    private var nextId = 1L

    private val _history = MutableStateFlow(loadInitialHistory())
    override val historyTape: Flow<List<HistoryItem>> = _history.asStateFlow()
    override val currentHistory: List<HistoryItem> get() = _history.value

    private fun loadInitialHistory(): List<HistoryItem> {
        val raw = prefs?.getString(KEY_HISTORY_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<HistoryItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optLong("id", (i + 1).toLong())
                if (id >= nextId) nextId = id + 1
                val expr = obj.optString("expression", "")
                val res = obj.optString("result", "")
                val modeStr = obj.optString("mode", CalculatorMode.NORMAL.name)
                val ts = obj.optLong("timestampMs", 0L)
                val mode = runCatching { CalculatorMode.valueOf(modeStr) }.getOrDefault(CalculatorMode.NORMAL)
                if (expr.isNotEmpty() && res.isNotEmpty()) {
                    list.add(HistoryItem(id, expr, res, mode, ts))
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveToDisk(items: List<HistoryItem>) {
        val p = prefs ?: return
        try {
            val array = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("expression", item.expression)
                    put("result", item.result)
                    put("mode", item.mode.name)
                    put("timestampMs", item.timestampMs)
                }
                array.put(obj)
            }
            p.edit().putString(KEY_HISTORY_JSON, array.toString()).apply()
        } catch (ignored: Exception) {
        }
    }

    override suspend fun addRecord(expression: String, result: String, mode: CalculatorMode) {
        val item = HistoryItem(
            id = nextId++,
            expression = expression.trim(),
            result = result.trim(),
            mode = mode,
            timestampMs = System.currentTimeMillis()
        )
        val updated = (listOf(item) + _history.value).take(MAX_HISTORY_ITEMS)
        _history.value = updated
        saveToDisk(updated)
    }

    override suspend fun deleteRecord(id: Long) {
        val updated = _history.value.filterNot { it.id == id }
        _history.value = updated
        saveToDisk(updated)
    }

    override suspend fun clearHistory() {
        _history.value = emptyList()
        prefs?.edit()?.remove(KEY_HISTORY_JSON)?.apply()
    }

    companion object {
        const val HISTORY_PREFS_NAME = "calculator_local_history"
        private const val KEY_HISTORY_JSON = "history_entries_json"
        private const val MAX_HISTORY_ITEMS = 100
    }
}
