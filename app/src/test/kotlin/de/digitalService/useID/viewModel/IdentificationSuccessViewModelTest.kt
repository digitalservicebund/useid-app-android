package de.digitalService.useID.viewModel

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.ui.coordinators.IdentificationCoordinator
import de.digitalService.useID.ui.screens.identification.IdentificationSuccessViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class IdentificationSuccessViewModelTest {

    @MockK(relaxUnitFun = true)
    lateinit var mockCoordinator: IdentificationCoordinator

    @MockK(relaxUnitFun = true)
    lateinit var mockSavedStateHandle: SavedStateHandle

    @Test
    fun collectScanEvents_ScanEventCardRequested() = runTest {
        val mockUri: Uri = mockk(relaxUnitFun = true)
        val mockContext: Context = mockk(relaxUnitFun = true)

        val testSavedStateHandle = SavedStateHandle()
        testSavedStateHandle["provider"] = "provider"
        testSavedStateHandle["refreshAddress"] = "refreshAddress"

        mockkStatic(Uri::class)
        every { Uri.decode(any()) } returnsMany listOf("provider", "refreshAddress")
        every { Uri.parse(any()) } returns mockUri

        mockkStatic(ContextCompat::class)
        every { ContextCompat.startActivity(mockContext, any(), null) } returns Unit

        val viewModel = IdentificationSuccessViewModel(
            mockCoordinator,
            testSavedStateHandle
        )

        viewModel.onButtonTapped(mockContext)

        verify(exactly = 1) { ContextCompat.startActivity(mockContext, any(), null) }
        verify(exactly = 1) { mockCoordinator.finishIdentification() }
        verify(exactly = 1) { Uri.parse("refreshAddress") }
    }
}
