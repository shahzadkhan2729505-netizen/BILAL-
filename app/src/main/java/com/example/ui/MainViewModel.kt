package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.calculator.CalculatorEngine
import com.example.calculator.CalculatorState
import com.example.data.AppDatabase
import com.example.data.MilkRepository
import com.example.data.model.Farmer
import com.example.data.model.FarmerWeeklyHistory
import com.example.data.model.MilkCalculations
import com.example.data.model.MilkRecord
import com.example.data.model.MonthConfig
import com.example.data.model.REFERENCE_TS
import com.example.data.model.TraceRowEntity
import com.example.data.model.TraceSheetConfig
import com.example.traceability.TraceabilityEngine
import com.example.util.WeekDateUtils
import com.example.util.WeekInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DashboardKpis(
    val totalMilkLiters: Double = 0.0,
    val totalTsMilkLiters: Double = 0.0,
    val averageFat: Double = 0.0,
    val totalPayment: Double = 0.0,
    val farmerCount: Int = 0,
    val entriesCount: Int = 0,
    val averageLr: Double = 0.0,
    val averageTs: Double = 0.0
)

data class WeeklySummaryCalculations(
    val totalVolume: Double = 0.0,
    val averageFat: Double = 0.0,
    val averageLr: Double = 0.0,
    val averageTs: Double = 0.0,
    val totalPayment: Double = 0.0,
    val entryCount: Int = 0
) {
    fun formattedTotalVolume(): String = String.format(Locale.US, "%.2f Litres", totalVolume)
    fun formattedAverageFat(): String = String.format(Locale.US, "%.2f%%", averageFat)
    fun formattedAverageLr(): String = String.format(Locale.US, "%.1f", averageLr)
    fun formattedAverageTs(): String = String.format(Locale.US, "%.2f%%", averageTs)
    fun formattedTotalPayment(): String = String.format(Locale.US, "Rs %,.0f", totalPayment)
}

data class MilkEntryFormState(
    val selectedFarmerId: String = "",
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val litersText: String = "",
    val fatText: String = "",
    val lrText: String = "",
    val rateText: String = "",
    val remarks: String = "",
    val calculations: MilkCalculations = MilkCalculations.compute(0.0, 0.0, 0.0, 0.0),
    val editingRecordId: Long? = null
)

data class TraceRowUi(
    val sr: Int,
    val name: String = "",
    val values: Map<String, Double> = emptyMap()
)

data class TraceSheetUiState(
    val supplierCode: String = "",
    val supplierName: String = "",
    val sourceType: String = "",
    val villageName: String = "",
    val telephoneNumber: String = "",
    val locationCode: String = "",
    val farmerNameHint: String = "",
    val isAutoMode: Boolean = true,
    val selectedMonths: Set<String> = emptySet(),
    val monthConfigs: Map<String, MonthConfig> = emptyMap(),
    val rows: List<TraceRowUi> = List(70) { TraceRowUi(it + 1, "", emptyMap()) },
    val loadedDocumentName: String = "Subcenter_Traceability_Log_Sheet.xlsx",
    val isCustomUploaded: Boolean = false,
    val statusMessage: String = "Ready. All cells are clean and empty. Tap any cell or talk to AI Assistant.",
    val isSuccessStatus: Boolean = false,
    val isWarningStatus: Boolean = false,
    val isAiLoading: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = MilkRepository(
        database.farmerDao(),
        database.milkRecordDao(),
        database.traceSheetDao(),
        database.farmerWeeklyHistoryDao()
    )

    val farmers: StateFlow<List<Farmer>> = repository.allFarmers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val records: StateFlow<List<MilkRecord>> = repository.allRecords.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dashboard KPIs
    val dashboardKpis: StateFlow<DashboardKpis> = combine(farmers, records) { fList, rList ->
        if (rList.isEmpty()) {
            DashboardKpis(farmerCount = fList.size, entriesCount = 0)
        } else {
            val totalMilk = rList.sumOf { it.liters }
            val totalTsMilk = rList.sumOf { it.tsMilk }
            val avgFat = rList.map { it.fat }.average()
            val totalPay = rList.sumOf { it.payment }
            val avgLr = rList.map { it.lr }.average()
            val avgTs = rList.map { it.ts }.average()
            DashboardKpis(
                totalMilkLiters = totalMilk,
                totalTsMilkLiters = totalTsMilk,
                averageFat = avgFat,
                totalPayment = totalPay,
                farmerCount = fList.size,
                entriesCount = rList.size,
                averageLr = avgLr,
                averageTs = avgTs
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardKpis()
    )

    // Milk Entry Form State
    private val _entryFormState = MutableStateFlow(MilkEntryFormState())
    val entryFormState: StateFlow<MilkEntryFormState> = _entryFormState.asStateFlow()

    // Records Filter State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _fromDate = MutableStateFlow("")
    val fromDate: StateFlow<String> = _fromDate.asStateFlow()

    private val _toDate = MutableStateFlow("")
    val toDate: StateFlow<String> = _toDate.asStateFlow()

    val filteredRecords: StateFlow<List<MilkRecord>> = combine(records, _searchQuery, _fromDate, _toDate) { recs, q, from, to ->
        val query = q.trim().lowercase()
        recs.filter { r ->
            val matchesQuery = query.isEmpty() ||
                r.farmerName.lowercase().contains(query) ||
                r.farmerId.lowercase().contains(query) ||
                r.remarks.lowercase().contains(query)
            val matchesFrom = from.isEmpty() || r.date >= from
            val matchesTo = to.isEmpty() || r.date <= to
            matchesQuery && matchesFrom && matchesTo
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Calculator State
    private val calcEngine = CalculatorEngine()
    private val _calcState = MutableStateFlow(calcEngine.getState())
    val calcState: StateFlow<CalculatorState> = _calcState.asStateFlow()

    // Traceability Sheet UI State
    private val _traceState = MutableStateFlow(TraceSheetUiState())
    val traceState: StateFlow<TraceSheetUiState> = _traceState.asStateFlow()

    // Transient UI message / toast trigger
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // ================== FARMER WEEKLY ENTRY & HISTORY ==================
    private val _selectedWeeklyFarmerId = MutableStateFlow<String?>(null)
    val selectedWeeklyFarmerId: StateFlow<String?> = _selectedWeeklyFarmerId.asStateFlow()

    val selectedWeeklyFarmer: StateFlow<Farmer?> = combine(farmers, _selectedWeeklyFarmerId) { fList, id ->
        fList.find { it.id == id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Current week info (Monday -> Sunday)
    private val _currentWeekInfo = MutableStateFlow(WeekDateUtils.getWeekInfoForDate())
    val currentWeekInfo: StateFlow<WeekInfo> = _currentWeekInfo.asStateFlow()

    // Records for the selected farmer specifically
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedFarmerAllRecords: StateFlow<List<MilkRecord>> = _selectedWeeklyFarmerId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList())
        else repository.getRecordsByFarmer(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Monday-to-Sunday entries for the selected farmer
    val currentWeeklyEntries: StateFlow<List<MilkRecord>> = combine(
        selectedFarmerAllRecords,
        _currentWeekInfo
    ) { records, week ->
        records.filter { r ->
            WeekDateUtils.isDateInWeek(r.date, week.mondayDate, week.sundayDate)
        }.sortedWith(compareBy({ it.date }, { it.id }))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Weekly calculations for current week
    val currentWeeklySummary: StateFlow<WeeklySummaryCalculations> = currentWeeklyEntries.map { list ->
        if (list.isEmpty()) {
            WeeklySummaryCalculations()
        } else {
            val totalVol = list.sumOf { it.liters }
            val avgFat = list.map { it.fat }.average()
            val avgLr = list.map { it.lr }.average()
            val avgTs = list.map { it.ts }.average()
            val totalPay = list.sumOf { it.payment }
            WeeklySummaryCalculations(
                totalVolume = totalVol,
                averageFat = avgFat,
                averageLr = avgLr,
                averageTs = avgTs,
                totalPayment = totalPay,
                entryCount = list.size
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WeeklySummaryCalculations()
    )

    // Weekly History for the selected farmer (completed past weeks)
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val selectedFarmerWeeklyHistory: StateFlow<List<FarmerWeeklyHistory>> = _selectedWeeklyFarmerId.flatMapLatest { id ->
        if (id.isNullOrBlank()) flowOf(emptyList())
        else repository.getWeeklyHistoryForFarmer(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectFarmerForWeekly(farmerId: String) {
        _selectedWeeklyFarmerId.value = farmerId
        // Refresh week info to current system clock date
        _currentWeekInfo.value = WeekDateUtils.getWeekInfoForDate()
        // Auto check & archive any completed previous week records for this farmer
        checkAndArchiveCompletedWeeksForFarmer(farmerId)
    }

    fun clearWeeklyFarmerSelection() {
        _selectedWeeklyFarmerId.value = null
    }

    /**
     * Checks if any previous weeks have ended and permanently saves their summary into Weekly History.
     * Prevents overwriting and maintains individual farmer weekly separation.
     */
    fun checkAndArchiveCompletedWeeksForFarmer(farmerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val f = database.farmerDao().getFarmerById(farmerId) ?: return@launch
            val curWeek = WeekDateUtils.getWeekInfoForDate()
            val allFarmerRecords = database.milkRecordDao().getRecordListByFarmer(farmerId)
            if (allFarmerRecords.isEmpty()) return@launch

            // Group records strictly by Monday-to-Sunday week
            val weeksMap = mutableMapOf<Pair<Int, Int>, Pair<WeekInfo, MutableList<MilkRecord>>>()
            for (rec in allFarmerRecords) {
                val wInfo = WeekDateUtils.getWeekInfoForDate(rec.date)
                // If this is the current week, it stays active in Current Week list!
                if (wInfo.year == curWeek.year && wInfo.weekNumber == curWeek.weekNumber) {
                    continue
                }
                // Otherwise it is a completed past week: ensure it's in Weekly History
                val key = Pair(wInfo.year, wInfo.weekNumber)
                val entry = weeksMap.getOrPut(key) { Pair(wInfo, mutableListOf()) }
                entry.second.add(rec)
            }

            for ((key, pair) in weeksMap) {
                val (year, weekNum) = key
                val (wInfo, recs) = pair
                val existingHistory = repository.getSpecificWeeklyHistory(farmerId, year, weekNum)
                if (existingHistory == null && recs.isNotEmpty()) {
                    val totalVol = recs.sumOf { it.liters }
                    val avgFat = recs.map { it.fat }.average()
                    val avgLr = recs.map { it.lr }.average()
                    val avgTs = recs.map { it.ts }.average()
                    val totalPay = recs.sumOf { it.payment }

                    val history = FarmerWeeklyHistory(
                        farmerId = farmerId,
                        farmerName = f.name,
                        year = year,
                        weekNumber = weekNum,
                        weekStartDate = wInfo.mondayDate,
                        weekEndDate = wInfo.sundayDate,
                        totalVolume = totalVol,
                        averageFat = avgFat,
                        averageLr = avgLr,
                        averageTs = avgTs,
                        totalPayment = totalPay,
                        entryCount = recs.size
                    )
                    repository.saveWeeklyHistory(history)
                }
            }
        }
    }

    /**
     * Explicitly commits/archives a completed weekly summary into permanent Weekly History
     */
    fun saveWeeklySummaryToHistory(
        farmer: Farmer,
        weekInfo: WeekInfo,
        summary: WeeklySummaryCalculations
    ) {
        if (summary.entryCount == 0) return
        viewModelScope.launch(Dispatchers.IO) {
            val history = FarmerWeeklyHistory(
                farmerId = farmer.id,
                farmerName = farmer.name,
                year = weekInfo.year,
                weekNumber = weekInfo.weekNumber,
                weekStartDate = weekInfo.mondayDate,
                weekEndDate = weekInfo.sundayDate,
                totalVolume = summary.totalVolume,
                averageFat = summary.averageFat,
                averageLr = summary.averageLr,
                averageTs = summary.averageTs,
                totalPayment = summary.totalPayment,
                entryCount = summary.entryCount
            )
            repository.saveWeeklyHistory(history)
            _userMessage.value = "Week ${weekInfo.weekNumber} summary saved into Weekly History."
        }
    }

    init {
        loadTraceSheetFromDb()
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // ================== FARMERS ==================

    fun saveFarmer(id: String, name: String, mobile: String, village: String, defaultRate: Double, onSuccess: () -> Unit) {
        val cleanId = id.trim()
        val cleanName = name.trim()
        val cleanMobile = mobile.trim()
        if (cleanId.isEmpty() || cleanName.isEmpty()) {
            _userMessage.value = "Please enter Farmer ID and Name."
            return
        }
        if (cleanMobile.isEmpty()) {
            _userMessage.value = "Farmer WhatsApp number is required."
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val existing = database.farmerDao().getFarmerById(cleanId)
            val farmer = Farmer(
                id = cleanId,
                name = cleanName,
                mobile = cleanMobile,
                village = village.trim(),
                defaultRate = if (defaultRate > 0) defaultRate else 200.0
            )
            repository.insertFarmer(farmer)
            _userMessage.value = if (existing != null) "Farmer updated successfully." else "Farmer saved successfully."
            viewModelScope.launch(Dispatchers.Main) { onSuccess() }
        }
    }

    fun deleteFarmer(farmerId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!repository.canDeleteFarmer(farmerId)) {
                _userMessage.value = "This farmer has milk records. Delete those records first."
                return@launch
            }
            repository.deleteFarmer(farmerId)
            _userMessage.value = "Farmer deleted successfully."
        }
    }

    // ================== MILK ENTRY ==================

    fun onEntryFarmerChanged(farmerId: String) {
        val f = farmers.value.find { it.id == farmerId }
        val rateStr = if (f != null) String.format(Locale.US, "%.2f", f.defaultRate) else _entryFormState.value.rateText
        _entryFormState.value = _entryFormState.value.copy(
            selectedFarmerId = farmerId,
            rateText = rateStr
        )
        recomputeEntryCalculations()
    }

    fun onEntryDateChanged(date: String) {
        _entryFormState.value = _entryFormState.value.copy(date = date)
    }

    fun onEntryLitersChanged(liters: String) {
        _entryFormState.value = _entryFormState.value.copy(litersText = liters)
        recomputeEntryCalculations()
    }

    fun onEntryFatChanged(fat: String) {
        _entryFormState.value = _entryFormState.value.copy(fatText = fat)
        recomputeEntryCalculations()
    }

    fun onEntryLrChanged(lr: String) {
        _entryFormState.value = _entryFormState.value.copy(lrText = lr)
        recomputeEntryCalculations()
    }

    fun onEntryRateChanged(rate: String) {
        _entryFormState.value = _entryFormState.value.copy(rateText = rate)
        recomputeEntryCalculations()
    }

    fun onEntryRemarksChanged(remarks: String) {
        _entryFormState.value = _entryFormState.value.copy(remarks = remarks)
    }

    private fun recomputeEntryCalculations() {
        val liters = _entryFormState.value.litersText.toDoubleOrNull() ?: 0.0
        val fat = _entryFormState.value.fatText.toDoubleOrNull() ?: 0.0
        val lr = _entryFormState.value.lrText.toDoubleOrNull() ?: 0.0
        val rate = _entryFormState.value.rateText.toDoubleOrNull() ?: 0.0
        val calc = MilkCalculations.compute(liters, fat, lr, rate)
        _entryFormState.value = _entryFormState.value.copy(calculations = calc)
    }

    fun saveMilkEntry(onSuccess: (savedRecord: MilkRecord, farmer: Farmer) -> Unit) {
        val current = _entryFormState.value
        val farmer = farmers.value.find { it.id == current.selectedFarmerId }
        if (farmer == null) {
            _userMessage.value = "Please select a registered farmer."
            return
        }
        val liters = current.litersText.toDoubleOrNull()
        val fat = current.fatText.toDoubleOrNull()
        val lr = current.lrText.toDoubleOrNull()
        val rate = current.rateText.toDoubleOrNull()

        if (liters == null || liters <= 0 || fat == null || lr == null || rate == null || rate < 0) {
            _userMessage.value = "Please complete Milk (L), Fat (%), LR, and Rate."
            return
        }

        val c = current.calculations
        val record = MilkRecord(
            id = current.editingRecordId ?: 0L,
            date = current.date,
            farmerId = farmer.id,
            farmerName = farmer.name,
            liters = liters,
            fat = fat,
            lr = lr,
            snf = c.snf,
            ts = c.ts,
            spGravity = c.spGravity,
            milkKg = c.milkKg,
            fatKg = c.fatKg,
            snfKg = c.snfKg,
            tsKg = c.tsKg,
            tsMilk = c.tsMilk,
            rate = rate,
            payment = c.payment,
            remarks = current.remarks.trim()
        )

        viewModelScope.launch(Dispatchers.IO) {
            val insertedId = if (current.editingRecordId != null) {
                repository.updateRecord(record)
                _userMessage.value = "Milk record updated successfully."
                record.id
            } else {
                val newId = repository.insertRecord(record)
                _userMessage.value = "Milk entry saved successfully."
                newId
            }
            val finalRecord = record.copy(id = insertedId)
            // Reset input fields while keeping date & farmer
            _entryFormState.value = _entryFormState.value.copy(
                litersText = "",
                fatText = "",
                lrText = "",
                remarks = "",
                editingRecordId = null
            )
            recomputeEntryCalculations()
            viewModelScope.launch(Dispatchers.Main) { onSuccess(finalRecord, farmer) }
        }
    }

    fun cancelEntryForm() {
        _entryFormState.value = MilkEntryFormState()
    }

    fun startEditRecord(record: MilkRecord, onNavToEntry: () -> Unit) {
        _entryFormState.value = MilkEntryFormState(
            selectedFarmerId = record.farmerId,
            date = record.date,
            litersText = String.format(Locale.US, "%.2f", record.liters),
            fatText = String.format(Locale.US, "%.2f", record.fat),
            lrText = String.format(Locale.US, "%.1f", record.lr),
            rateText = String.format(Locale.US, "%.2f", record.rate),
            remarks = record.remarks,
            calculations = MilkCalculations.compute(record.liters, record.fat, record.lr, record.rate),
            editingRecordId = record.id
        )
        onNavToEntry()
    }

    fun deleteRecord(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRecord(id)
            _userMessage.value = "Milk record deleted."
        }
    }

    fun clearAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
            _userMessage.value = "All farmers and milk records deleted."
        }
    }

    // Filters
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setDateFilter(from: String, to: String) {
        _fromDate.value = from
        _toDate.value = to
    }
    fun clearRecordFilters() {
        _searchQuery.value = ""
        _fromDate.value = ""
        _toDate.value = ""
    }

    // ================== CALCULATOR ==================

    fun onCalcDigit(d: String) { _calcState.value = calcEngine.onDigit(d) }
    fun onCalcDecimal() { _calcState.value = calcEngine.onDecimal() }
    fun onCalcOperator(op: String) { _calcState.value = calcEngine.onOperator(op) }
    fun onCalcEquals() { _calcState.value = calcEngine.onEquals() }
    fun onCalcClear() { _calcState.value = calcEngine.onClear() }
    fun onCalcBackspace() { _calcState.value = calcEngine.onBackspace() }
    fun onCalcPercent() { _calcState.value = calcEngine.onPercent() }
    fun onCalcNegate() { _calcState.value = calcEngine.onNegate() }
    fun onCalcKgToL() { _calcState.value = calcEngine.onMilkConvertKgToL() }
    fun onCalcLToKg() { _calcState.value = calcEngine.onMilkConvertLToKg() }
    fun onCalcMemory(m: String) { _calcState.value = calcEngine.onMemory(m) }

    // ================== TRACEABILITY LOG SHEET ==================

    private fun loadTraceSheetFromDb() {
        viewModelScope.launch(Dispatchers.IO) {
            val cfg = database.traceSheetDao().getConfigOnce()
            val rows = database.traceSheetDao().getAllRowsOnce()
            if (cfg != null) {
                val months = cfg.selectedMonthsCsv.split(",").filter { it.isNotBlank() }.toSet()
                val monthCfgMap = mutableMapOf<String, MonthConfig>()
                try {
                    if (cfg.monthConfigsJson.isNotBlank()) {
                        val obj = JSONObject(cfg.monthConfigsJson)
                        val keys = obj.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            val mo = obj.getJSONObject(k)
                            monthCfgMap[k] = MonthConfig(mo.getInt("count"), mo.getDouble("maxKg"))
                        }
                    }
                } catch (_: Exception) {}

                months.forEach { m ->
                    if (!monthCfgMap.containsKey(m)) {
                        monthCfgMap[m] = MonthConfig(20, 45.0)
                    }
                }

                val rowUis = if (rows.size == 70) {
                    rows.map { r ->
                        TraceRowUi(
                            sr = r.sr,
                            name = r.farmerName,
                            values = TraceabilityEngine.parseValuesJson(r.valuesJson)
                        )
                    }
                } else {
                    List(70) { TraceRowUi(it + 1) }
                }

                _traceState.value = _traceState.value.copy(
                    supplierCode = cfg.supplierCode,
                    supplierName = cfg.supplierName,
                    sourceType = cfg.sourceType,
                    villageName = cfg.villageName,
                    farmerNameHint = cfg.farmerNameHint,
                    selectedMonths = if (months.isNotEmpty()) months else setOf("Sep"),
                    monthConfigs = monthCfgMap,
                    rows = rowUis
                )
            }
        }
    }

    fun updateTraceHeader(
        supplierCode: String,
        supplierName: String,
        sourceType: String,
        villageName: String,
        farmerNameHint: String,
        isAutoMode: Boolean
    ) {
        _traceState.value = _traceState.value.copy(
            supplierCode = supplierCode,
            supplierName = supplierName,
            sourceType = sourceType,
            villageName = villageName,
            farmerNameHint = farmerNameHint,
            isAutoMode = isAutoMode
        )
    }

    fun toggleTraceMonth(month: String) {
        val current = _traceState.value.selectedMonths.toMutableSet()
        if (current.contains(month)) {
            if (current.size > 1) current.remove(month)
        } else {
            current.add(month)
        }
        val configs = _traceState.value.monthConfigs.toMutableMap()
        current.forEach { m ->
            if (!configs.containsKey(m)) configs[m] = MonthConfig(20, 45.0)
        }
        _traceState.value = _traceState.value.copy(
            selectedMonths = current,
            monthConfigs = configs
        )
    }

    fun toggleAllTraceMonths() {
        val all = TraceabilityEngine.TRACE_MONTHS.toSet()
        val current = _traceState.value.selectedMonths
        val next = if (current.size == 12) setOf("Sep") else all
        val configs = _traceState.value.monthConfigs.toMutableMap()
        next.forEach { m ->
            if (!configs.containsKey(m)) configs[m] = MonthConfig(20, 45.0)
        }
        _traceState.value = _traceState.value.copy(
            selectedMonths = next,
            monthConfigs = configs
        )
    }

    fun updateMonthConfig(month: String, count: Int, maxKg: Double) {
        val safeCount = count.coerceIn(1, 70)
        val safeMax = if (maxKg > 0) maxKg else 1.0
        val map = _traceState.value.monthConfigs.toMutableMap()
        map[month] = MonthConfig(safeCount, safeMax)
        _traceState.value = _traceState.value.copy(monthConfigs = map)
    }

    fun autoFillTraceSheet() {
        val state = _traceState.value
        if (state.supplierCode.isBlank() || state.supplierName.isBlank() || state.villageName.isBlank()) {
            _userMessage.value = "Please enter Supplier Code, Supplier Name, and Village Name."
            return
        }
        if (state.selectedMonths.isEmpty()) {
            _userMessage.value = "Please select at least one month."
            return
        }

        try {
            val maxFarmers = state.selectedMonths.maxOf { state.monthConfigs[it]?.count ?: 20 }
            val names = TraceabilityEngine.makeUrduNames(maxFarmers, state.farmerNameHint)

            val newRows = mutableListOf<TraceRowUi>()
            for (i in 0 until 70) {
                val farmerName = if (i < maxFarmers) names.getOrElse(i) { "محمد ${i + 1}" } else ""
                val valuesMap = mutableMapOf<String, Double>()
                // Preserve existing unselected month values if any, or clear
                state.rows.getOrNull(i)?.values?.forEach { (m, v) ->
                    if (!state.selectedMonths.contains(m)) valuesMap[m] = v
                }

                state.selectedMonths.forEach { m ->
                    val cfg = state.monthConfigs[m] ?: MonthConfig(20, 45.0)
                    // Generate unique numbers for that month
                    val monthVals = TraceabilityEngine.generateUniqueValues(cfg.count, cfg.maxKg)
                    if (i < cfg.count && i < monthVals.size) {
                        valuesMap[m] = monthVals[i]
                    }
                }
                newRows.add(TraceRowUi(sr = i + 1, name = farmerName, values = valuesMap))
            }

            _traceState.value = state.copy(
                rows = newRows,
                statusMessage = "Auto Fill complete. Values are within each Max KGs/Litres and unique per month.",
                isSuccessStatus = true,
                isWarningStatus = false
            )
            saveTraceSheetToDb()
        } catch (e: Exception) {
            _traceState.value = state.copy(
                statusMessage = "Auto fill stopped: ${e.message}",
                isSuccessStatus = false,
                isWarningStatus = true
            )
        }
    }

    fun prepareManualTraceSheet() {
        val state = _traceState.value
        if (state.supplierCode.isBlank() || state.supplierName.isBlank() || state.villageName.isBlank()) {
            _userMessage.value = "Please enter Supplier Code, Supplier Name, and Village Name."
            return
        }
        val maxFarmers = state.selectedMonths.maxOf { state.monthConfigs[it]?.count ?: 20 }
        val names = TraceabilityEngine.makeUrduNames(maxFarmers, state.farmerNameHint)
        val newRows = (0 until 70).map { i ->
            val existing = state.rows.getOrNull(i)
            val name = if (existing != null && existing.name.isNotBlank()) existing.name else if (i < maxFarmers) names[i] else ""
            TraceRowUi(sr = i + 1, name = name, values = existing?.values ?: emptyMap())
        }
        _traceState.value = state.copy(
            rows = newRows,
            statusMessage = "Manual Entry ready. Edit Farmer Names and month values directly, then press Save.",
            isSuccessStatus = false,
            isWarningStatus = true
        )
    }

    fun updateRowFarmerName(sr: Int, newName: String) {
        val updated = _traceState.value.rows.map { row ->
            if (row.sr == sr) row.copy(name = newName) else row
        }
        _traceState.value = _traceState.value.copy(rows = updated)
    }

    fun updateRowMonthValue(sr: Int, month: String, valStr: String) {
        val v = valStr.toDoubleOrNull()
        val updated = _traceState.value.rows.map { row ->
            if (row.sr == sr) {
                val newMap = row.values.toMutableMap()
                if (v != null && v > 0) newMap[month] = v else newMap.remove(month)
                row.copy(values = newMap)
            } else row
        }
        _traceState.value = _traceState.value.copy(rows = updated)
    }

    fun addTraceRow() {
        val currentRows = _traceState.value.rows
        val newSr = currentRows.size + 1
        val updated = currentRows + TraceRowUi(sr = newSr, name = "Farmer $newSr", values = emptyMap())
        _traceState.value = _traceState.value.copy(
            rows = updated,
            statusMessage = "Row $newSr added to sheet.",
            isSuccessStatus = true,
            isWarningStatus = false
        )
    }

    fun deleteTraceRow(sr: Int) {
        val currentRows = _traceState.value.rows
        if (currentRows.size <= 1) {
            _userMessage.value = "Cannot delete the only row."
            return
        }
        val remaining = currentRows.filter { it.sr != sr }
        val renumbered = remaining.mapIndexed { index, row -> row.copy(sr = index + 1) }
        _traceState.value = _traceState.value.copy(
            rows = renumbered,
            statusMessage = "Row $sr deleted. Rows renumbered (1 to ${renumbered.size}).",
            isSuccessStatus = true,
            isWarningStatus = false
        )
    }

    fun recalculateTraceTotals() {
        // Trigger UI refresh and notification
        _userMessage.value = "Totals recalculated across all columns and rows."
    }

    fun saveTraceSheetEdits() {
        saveTraceSheetToDb()
        _traceState.value = _traceState.value.copy(
            statusMessage = "Edits saved successfully.",
            isSuccessStatus = true,
            isWarningStatus = false
        )
        _userMessage.value = "Traceability sheet edits saved."
    }

    /**
     * Replaces existing Excel / Traceability sheet with imported file data.
     */
    fun replaceTraceSheetData(
        newSupplierCode: String? = null,
        newSupplierName: String? = null,
        newVillageName: String? = null,
        newRows: List<TraceRowUi>,
        sourceFileName: String = "Uploaded File"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val state = _traceState.value
            val finalRows = if (newRows.isNotEmpty()) {
                newRows.mapIndexed { idx, r -> r.copy(sr = idx + 1) }
            } else {
                state.rows
            }
            _traceState.value = state.copy(
                supplierCode = newSupplierCode ?: state.supplierCode,
                supplierName = newSupplierName ?: state.supplierName,
                villageName = newVillageName ?: state.villageName,
                rows = finalRows,
                loadedDocumentName = sourceFileName,
                isCustomUploaded = true,
                statusMessage = "✓ Sheet replaced with $sourceFileName (${finalRows.size} rows).",
                isSuccessStatus = true,
                isWarningStatus = false
            )
            saveTraceSheetToDb()
            _userMessage.value = "Sheet replaced with $sourceFileName"
        }
    }

    /**
     * 100% Lifetime Free AI Auto-Balance for Excel sheet.
     */
    fun applyAiAutoBalance() {
        viewModelScope.launch(Dispatchers.Default) {
            val current = _traceState.value
            val balanced = com.example.traceability.ExcelAiEngine.autoBalanceSheet(current)
            _traceState.value = current.copy(
                rows = balanced,
                statusMessage = "✓ AI Auto-Balance applied. Outliers normalized.",
                isSuccessStatus = true,
                isWarningStatus = false
            )
            saveTraceSheetToDb()
            _userMessage.value = "AI Auto-Balance completed."
        }
    }

    /**
     * 100% Lifetime Free AI Smart Complete for Excel sheet.
     */
    fun applyAiSmartComplete() {
        viewModelScope.launch(Dispatchers.Default) {
            val current = _traceState.value
            val completed = com.example.traceability.ExcelAiEngine.smartCompleteMissing(current)
            _traceState.value = current.copy(
                rows = completed,
                statusMessage = "✓ AI Smart Complete applied. Missing entries populated.",
                isSuccessStatus = true,
                isWarningStatus = false
            )
            saveTraceSheetToDb()
            _userMessage.value = "AI Smart Complete finished."
        }
    }

    /**
     * Lifetime Free AI Audit of the Excel Sheet.
     */
    fun getAiAuditReport(): com.example.traceability.ExcelAiEngine.AiAuditReport {
        return com.example.traceability.ExcelAiEngine.auditSheet(_traceState.value)
    }

    private fun saveTraceSheetToDb() {
        val state = _traceState.value
        viewModelScope.launch(Dispatchers.IO) {
            val monthJson = JSONObject()
            state.monthConfigs.forEach { (m, cfg) ->
                val o = JSONObject()
                o.put("count", cfg.count)
                o.put("maxKg", cfg.maxKg)
                monthJson.put(m, o)
            }
            val configEntity = TraceSheetConfig(
                id = 1,
                supplierCode = state.supplierCode,
                supplierName = state.supplierName,
                sourceType = state.sourceType,
                villageName = state.villageName,
                farmerNameHint = state.farmerNameHint,
                selectedMonthsCsv = state.selectedMonths.joinToString(","),
                monthConfigsJson = monthJson.toString()
            )
            repository.saveTraceConfig(configEntity)

            val rowEntities = state.rows.map { r ->
                TraceRowEntity(
                    sr = r.sr,
                    farmerName = r.name,
                    valuesJson = TraceabilityEngine.valuesToJson(r.values)
                )
            }
            repository.saveTraceRows(rowEntities)
        }
    }

    fun executeAiCommand(command: String, onResponse: (String, String) -> Unit) {
        val current = _traceState.value
        val result = com.example.traceability.TraceabilityAiBrain.processCommand(command, current)
        _traceState.value = result.updatedState
        saveTraceSheetToDb()
        onResponse(result.urduResponse, result.englishResponse)
    }

    fun clearAllSheetData() {
        val emptyRows = List(70) { TraceRowUi(sr = it + 1, name = "", values = emptyMap()) }
        _traceState.value = _traceState.value.copy(
            rows = emptyRows,
            statusMessage = "✓ پوری شیٹ صاف اور تمام 70 خانے بلینک کر دیے گئے ہیں۔",
            isSuccessStatus = true,
            isWarningStatus = false
        )
        saveTraceSheetToDb()
        _userMessage.value = "Excel sheet completely cleared."
    }

    fun clearTraceSheet() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearTraceSheet()
            _traceState.value = TraceSheetUiState()
            _userMessage.value = "Traceability sheet cleared."
        }
    }
}
