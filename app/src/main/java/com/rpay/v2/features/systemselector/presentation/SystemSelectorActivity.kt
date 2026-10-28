package com.rpay.v2.features.systemselector.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.rpay.v2.data.repository.AppPreferencesRepository
import com.rpay.v2.features.systemselector.domain.GeneralSystemType
import com.rpay.v2.ui.theme.Wizar_posTheme
import com.rpay.v2.presentation.home.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SystemSelectorActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: AppPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Wizar_posTheme {
                val viewModel: SystemsViewModel = hiltViewModel()
                val state by viewModel.state.collectAsStateWithLifecycle()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                SystemsMainContent(
                    state = state,
                    stateForUi = uiState,
                    onIntent = viewModel::onIntent,
                    onSystemSelected = { type ->
                        onSystemSelected(type)
                    }
                )
            }
        }
    }

    private fun onSystemSelected(type: GeneralSystemType) {
        lifecycleScope.launch {
            prefs.setGeneralSystemType(type)
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_GENERAL_SYSTEM_TYPE", type.value)
        }

        startActivity(intent)
        finish()
    }
}