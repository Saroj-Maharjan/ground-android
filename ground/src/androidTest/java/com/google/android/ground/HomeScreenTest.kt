package com.google.android.ground

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isEnabled
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.google.android.ground.model.Survey
import com.google.android.ground.model.basemap.BaseMap
import com.google.android.ground.repository.SurveyRepository
import com.sharedtest.FakeData
import dagger.hilt.android.testing.HiltAndroidTest
import java.net.URL
import javax.inject.Inject
import org.hamcrest.CoreMatchers.not
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest : BaseMainActivityTest() {

  @Inject lateinit var surveyRepository: SurveyRepository

  private val survey: Survey =
    Survey(
      "SURVEY",
      "Survey title",
      "Test survey description",
      mapOf(FakeData.JOB.id to FakeData.JOB),
      listOf(),
      mapOf(Pair(FakeData.USER.email, "data-collector"))
    )

  private val surveyBasemaps: Survey =
    survey.copy(
      baseMaps =
        listOf(
          BaseMap(URL("http://google.com"), BaseMap.BaseMapType.MBTILES_FOOTPRINTS),
        ),
      id = "BASEMAPS"
    )

  @Test
  fun onViewCreated_offlineBasemapMenuIsDisabledWhenActiveSurveyHasNoBasemaps() {
    surveyRepository.activeSurvey = survey
    dataBindingIdlingResource.monitorActivity(scenarioRule.scenario)

    onView(withId(R.id.hamburger_btn)).check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
    onView(withId(R.id.nav_offline_areas)).check(matches(not(isEnabled())))
  }

  @Test
  fun onViewCreated_offlineBasemapMenuIsEnabledWhenActiveSurveyHasBasemaps() {
    surveyRepository.activeSurvey = surveyBasemaps
    dataBindingIdlingResource.monitorActivity(scenarioRule.scenario)

    onView(withId(R.id.hamburger_btn)).check(matches(isDisplayed())).perform(click())
    onView(withId(R.id.nav_view)).check(matches(isDisplayed()))
    onView(withId(R.id.nav_offline_areas)).check(matches(isEnabled()))
  }
}
