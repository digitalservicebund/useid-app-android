package de.digitalService.useID

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import de.digitalService.useID.idCardInterface.EIDInteractionEvent
import de.digitalService.useID.idCardInterface.IDCardManager
import de.digitalService.useID.ui.composables.screens.SetupScanViewModel
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class SetupScanViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun pass() = runTest {
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    @Test
//    fun fail() = runTest {
//        Assertions.fail()
//    }
}
