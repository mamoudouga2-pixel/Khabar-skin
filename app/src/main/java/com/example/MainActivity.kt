package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.configureAppCheck
import com.example.data.ai.FoodAiService
import com.example.data.preferences.UserPrefsRepository
import com.example.data.repository.LocalNutritionStore
import com.example.data.repository.NutritionRepository
import com.example.ui.navigation.KhabarCalApp
import com.example.ui.theme.KhabarCalTheme
import com.example.ui.viewmodel.KhabarCalViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure App Check with launch intent BEFORE setContent, ViewModels, or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()
        setContent {
            KhabarCalTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
private fun AppNavigation() {
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    val isSignedIn = user != null
    val uid = user?.uid ?: "local_device"
    val displayName = user?.displayName.orEmpty().ifBlank { "KhabarCal" }
    val email = user?.email.orEmpty()

    key(uid) {
        val viewModel: KhabarCalViewModel = viewModel(
            key = uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY]) {
                        "APPLICATION_KEY missing from CreationExtras"
                    }
                    val store = if (isSignedIn) {
                        val databaseId = app.getString(R.string.firestore_database_id)
                        NutritionRepository(FirebaseFirestore.getInstance(databaseId))
                    } else {
                        LocalNutritionStore(app)
                    }
                    KhabarCalViewModel(
                        repository = store,
                        aiService = FoodAiService(),
                        prefsRepo = UserPrefsRepository(app),
                        currentUserId = uid,
                        authDisplayName = displayName
                    )
                }
            }
        )
        KhabarCalApp(
            viewModel = viewModel,
            userEmail = email
        )
    }
}
