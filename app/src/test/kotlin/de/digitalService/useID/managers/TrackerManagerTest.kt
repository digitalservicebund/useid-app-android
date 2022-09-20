package de.digitalService.useID.managers

import android.content.Context
import androidx.navigation.NavDestination
import de.digitalService.useID.analytics.TrackerManager
import de.digitalService.useID.util.CurrentTimeProvider
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.matomo.sdk.Matomo
import org.matomo.sdk.Tracker
import org.matomo.sdk.TrackerBuilder
import org.matomo.sdk.extra.TrackHelper

@ExtendWith(MockKExtension::class)
class TrackerManagerTest {

    @MockK
    lateinit var mockCurrentTimeProvider: CurrentTimeProvider

    @MockK(relaxUnitFun = true)
    lateinit var mockContext: Context

    @MockK(relaxUnitFun = true)
    lateinit var mockTracker: Tracker

    @MockK(relaxUnitFun = true)
    lateinit var mockMatomo: Matomo

    private val testUrl = "https://example.com"

    @BeforeEach
    fun setUp() {
        mockkStatic("org.matomo.sdk.Matomo")
        every { Matomo.getInstance(mockContext) } returns mockMatomo

        mockkStatic("org.matomo.sdk.TrackerBuilder")
        every { TrackerBuilder.createDefault(testUrl, 3).build(mockMatomo) } returns mockTracker
    }

    @Test
    fun initialization() {
        every { mockCurrentTimeProvider.currentTime } returns 0

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        verify { TrackerBuilder.createDefault(testUrl, 3).build(mockMatomo) }
    }

    @Test
    fun trackDestination() {
        every { mockCurrentTimeProvider.currentTime } returns 0 andThen 1 andThen 2

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        val route = "testRoute"

        val destination: NavDestination = mockk()
        every { destination.route } returns route

        mockkStatic("org.matomo.sdk.extra.TrackHelper")
        justRun { TrackHelper.track().screen(route).with(mockTracker) }

        trackerManager.trackDestination(destination)

        verify { TrackHelper.track().screen(route).with(mockTracker) }
    }

    @Test
    fun trackDestinationWithoutNewSession() {
        every { mockCurrentTimeProvider.currentTime } returns 0 andThen 1 andThen 2

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        val route = "testRoute"
        val destination: NavDestination = mockk()
        every { destination.route } returns route

        mockkStatic("org.matomo.sdk.extra.TrackHelper")
        justRun { TrackHelper.track().screen(route).with(mockTracker) }

        trackerManager.trackDestination(destination)
        trackerManager.trackDestination(destination)

        verify(exactly = 0) { mockTracker.reset() }
    }

    @Test
    fun trackDestinationWithNewSession() {
        every { mockCurrentTimeProvider.currentTime } returns 0 andThen 1 andThen 12

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        val route = "testRoute"

        val destination: NavDestination = mockk()
        every { destination.route } returns route

        mockkStatic("org.matomo.sdk.extra.TrackHelper")
        justRun { TrackHelper.track().screen(route).with(mockTracker) }

        trackerManager.trackDestination(destination)
        verify(exactly = 0) { mockTracker.reset() }

        trackerManager.trackDestination(destination)
        verify(exactly = 1) { mockTracker.reset() }
    }

    @Test
    fun trackEvent() {
        every { mockCurrentTimeProvider.currentTime } returns 0 andThen 1 andThen 2

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        val category = "category"
        val action = "action"
        val name = "name"

        mockkStatic("org.matomo.sdk.extra.TrackHelper")
        justRun { TrackHelper.track().event(category, action).name(name).with(mockTracker) }

        trackerManager.trackEvent(category, action, name)

        verify { TrackHelper.track().event(category, action).name(name).with(mockTracker) }
    }

    @Test
    fun trackEventWithoutNewSession() {
        every { mockCurrentTimeProvider.currentTime } returns 0 andThen 1 andThen 2

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        val category = "category"
        val action = "action"
        val name = "name"

        mockkStatic("org.matomo.sdk.extra.TrackHelper")
        justRun { TrackHelper.track().event(category, action).name(name).with(mockTracker) }

        trackerManager.trackEvent(category, action, name)
        trackerManager.trackEvent(category, action, name)

        verify(exactly = 0) { mockTracker.reset() }
    }

    @Test
    fun trackEventWithNewSession() {
        every { mockCurrentTimeProvider.currentTime } returns 0 andThen 1 andThen 12

        val trackerManager = TrackerManager(mockCurrentTimeProvider, 10L, testUrl)
        trackerManager.initTracker(mockContext)

        val category = "category"
        val action = "action"
        val name = "name"

        mockkStatic("org.matomo.sdk.extra.TrackHelper")
        justRun { TrackHelper.track().event(category, action).name(name).with(mockTracker) }

        trackerManager.trackEvent(category, action, name)
        verify(exactly = 0) { mockTracker.reset() }

        trackerManager.trackEvent(category, action, name)
        verify(exactly = 1) { mockTracker.reset() }
    }
}
