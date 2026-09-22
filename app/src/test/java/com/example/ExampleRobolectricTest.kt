package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseSeeder
import com.example.data.model.CheckResult
import com.example.data.model.InspectionTemplate
import com.example.data.model.VehicleAuthStatus
import com.example.data.repository.FleetRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ทองอยู่ ฟลีท", appName)
  }

  @Test
  fun `bangkok date utils returns valid format`() {
    val workDate = DateUtils.getWorkDate()
    assertTrue(workDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
  }

  @Test
  fun `inspection template has no default pass and splits mother and trailer`() {
    val items = InspectionTemplate.createItemsForInspection(
      inspectionId = "test-insp",
      isSemiTrailer = true,
      motherVehicleId = "veh-mother-01",
      trailerVehicleId = "veh-trailer-01"
    )

    // All items must start with result == null (NO pre-checked pass!)
    assertTrue(items.all { it.result == null })

    // Must have mother and trailer split items
    val motherItems = items.filter { it.targetVehicleId == "veh-mother-01" }
    val trailerItems = items.filter { it.targetVehicleId == "veh-trailer-01" }

    assertTrue(motherItems.isNotEmpty())
    assertTrue(trailerItems.isNotEmpty())
  }

  @Test
  fun `gate release blocks when pre-trip incomplete or prohibited`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getInstance(context)
    val dao = db.fleetDao()
    DatabaseSeeder.seedIfEmpty(dao)
    val repo = FleetRepository(dao)

    // Check gate release eligibility before inspection
    val eligibility = repo.checkReleaseEligibility("veh-mother-01", "pair-01")
    assertFalse(eligibility.isEligible)
    assertTrue(eligibility.blockingReasons.any { it.contains("แบบตรวจก่อนออก") })
  }
}
