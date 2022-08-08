@file:OptIn(ExperimentalMaterial3Api::class)

package de.digitalService.useID.ui.composables.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import dagger.hilt.android.lifecycle.HiltViewModel
import de.digitalService.useID.R
import de.digitalService.useID.ui.AppCoordinator
import de.digitalService.useID.ui.composables.ButtonType
import de.digitalService.useID.ui.composables.RegularBundButton
import de.digitalService.useID.ui.coordinators.SetupCoordinator
import de.digitalService.useID.ui.theme.Black
import de.digitalService.useID.ui.theme.Gray300
import de.digitalService.useID.ui.theme.Gray600
import de.digitalService.useID.ui.theme.UseIDTheme
import javax.inject.Inject

@Composable
@Destination
@RootNavGraph(start = true)
fun HomeScreen(viewModel: HomeScreenViewModelInterface = hiltViewModel<HomeScreenViewModel>()) {
    LaunchedEffect(Unit) {
        viewModel.homeScreenLaunched()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.homeScreen_title),
            style = MaterialTheme.typography.titleLarge
        )
        Text(text = "Platzhalter text")

        Spacer(modifier = Modifier.height(300.dp))

        Text(
            text = stringResource(R.string.homeScreen_more_subtitle),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        SetupUseIdCarBox(viewModel)

        Spacer(modifier = Modifier.height(16.dp))

        MoreSettingsCardBox()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SetupUseIdCarBox(viewModel: HomeScreenViewModelInterface) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Gray600),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.eids),
                contentScale = ContentScale.Inside,
                contentDescription = "",
                modifier = Modifier
                    .offset(y = -20.dp)
                    .padding(bottom = 20.dp)
            )

            RegularBundButton(
                type = ButtonType.SECONDARY,
                onClick = viewModel::setupOnlineID,
                label = stringResource(R.string.homeScreen_setupOnlineID_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun MoreSettingsCardBox() {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Gray600),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        CardButton(text = stringResource(R.string.homeScreen_more_settings_button), onClick = {})
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_askedQuestions_button), onClick = {})
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_privacy_button), onClick = {})
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_accessibilityStatement_button), onClick = {})
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_impressum_button), onClick = {})
        StyledDivider()

        CardButton(text = stringResource(R.string.homeScreen_more_legalNotice_button), onClick = {})
    }
}

@Composable
private fun StyledDivider() {
    Divider(
        color = Gray300,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

@Composable
private fun CardButton(
    text: String,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            color = Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
    }
}

interface HomeScreenViewModelInterface {
    fun setupOnlineID()
    fun homeScreenLaunched()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val appCoordinator: AppCoordinator,
    private val setupCoordinator: SetupCoordinator
) : ViewModel(), HomeScreenViewModelInterface {
    override fun homeScreenLaunched() {
        appCoordinator.homeScreenLaunched(null)
    }

    override fun setupOnlineID() {
        setupCoordinator.startSetupIDCard()
    }
}

private class PreviewViewModel : HomeScreenViewModelInterface {
    override fun setupOnlineID() {}
    override fun homeScreenLaunched() {}
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    UseIDTheme {
        HomeScreen(PreviewViewModel())
    }
}
