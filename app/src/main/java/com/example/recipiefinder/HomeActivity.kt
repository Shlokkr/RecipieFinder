package com.example.recipiefinder

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.recipiefinder.ui.theme.RecipieFInderTheme
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecipieFInderTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }
    var profileMenuExpanded by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val allCategories = mapOf(
        "Main Dishes" to listOf(
            "Spaghetti Bolognese",
            "Chicken Curry",
            "Grilled Cheese Sandwich",
            "Vegetable Stir Fry",
            "Avocado Toast"
        ),
        "Desserts" to listOf(
            "Chocolate Cake",
            "Strawberry Cheesecake",
            "Ice Cream Sundae",
            "Pancakes",
            "Brownies"
        ),
        "Drinks" to listOf(
            "Mango Smoothie",
            "Iced Coffee",
            "Lemonade",
            "Hot Chocolate",
            "Green Tea"
        )
    )

    Column(modifier = Modifier.padding(16.dp)) {

        // Top bar with profile icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Box {
                IconButton(onClick = { profileMenuExpanded = true }) {
                    Icon(Icons.Default.Person, contentDescription = "Profile")
                }

                DropdownMenu(
                    expanded = profileMenuExpanded,
                    onDismissRequest = { profileMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Privacy Policy") },
                        onClick = {
                            profileMenuExpanded = false
                            showPrivacyDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            profileMenuExpanded = false
                            FirebaseAuth.getInstance().signOut()
                            context.startActivity(Intent(context, LoginActivity::class.java))
                        }
                    )
                }
            }
        }

        // Privacy Policy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("OK")
                    }
                },
                title = { Text("Privacy Policy") },
                text = {
                    Text("This is a demo app. We do not collect or share any user data.")
                }
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search Recipes") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Recipe List
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val filteredCategories = allCategories.mapValues { (_, recipes) ->
                recipes.filter { it.contains(searchQuery, ignoreCase = true) }
            }.filterValues { it.isNotEmpty() || searchQuery.isBlank() }

            if (filteredCategories.isEmpty() && searchQuery.isNotBlank()) {
                item {
                    Text(
                        text = "No recipes found.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            }

            filteredCategories.forEach { (category, recipes) ->
                item {
                    val expanded = expandedStates[category] == true
                    CategoryCard(
                        category = category,
                        expanded = expanded,
                        onToggle = {
                            expandedStates[category] = !expanded
                        }
                    )

                    AnimatedVisibility(
                        visible = expanded,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column(modifier = Modifier.padding(start = 8.dp, top = 4.dp)) {
                            recipes.forEach { recipe ->
                                RecipeCard(recipeName = recipe)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: String, expanded: Boolean, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (expanded) "▼ $category" else "▶ $category",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun RecipeCard(recipeName: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable {
                val intent = Intent(context, RecipeDetailActivity::class.java).apply {
                    putExtra("RECIPE_NAME", recipeName)
                }
                context.startActivity(intent)
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = recipeName, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "A delicious recipe for $recipeName.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
