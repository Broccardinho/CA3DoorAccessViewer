package com.example.dooraccess

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.dooraccess.data.network.RetrofitInstance
import com.example.dooraccess.data.network.UserResponse
import com.example.dooraccess.ui.theme.CA3DoorAccessViewerTheme
import kotlinx.coroutines.launch

// ========== MAIN ACTIVITY ==========
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //MATERIAL DESIGN THEME
            CA3DoorAccessViewerTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DoorAccessApp()
                }
            }
        }
    }
}

// ========== NAVIGATION ==========
//NAVIGATION BETWEEN SCREENS
@Composable
fun DoorAccessApp() {
    val navController = rememberNavController()
    val usersState = remember { mutableStateOf(emptyList<UserResponse>()) }

    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            UserListScreen(navController = navController, usersState = usersState)
        }
        composable("detail/{userId}") {
            val userId = it.arguments?.getString("userId") ?: "1"
            DetailScreen(userId = userId.toInt(), allUsers = usersState.value)
        }
    }
}

// ========== MAIN LIST SCREEN ==========
@Composable
fun UserListScreen(
    navController: androidx.navigation.NavHostController,
    usersState: MutableState<List<UserResponse>>
) {
    val loading = remember { mutableStateOf(true) }
    val error = remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        //LOGGING
        Log.d("API", "Fetching users...")

        scope.launch {
            try {
                // ✅ REQUIREMENT #6: GETTING DATA FROM INTERNET USING RETROFIT
                val response = RetrofitInstance.apiService.getUsers()
                if (response.isSuccessful) {
                    usersState.value = response.body()?.take(12) ?: emptyList()
                    Log.d("API", "Loaded ${usersState.value.size} users")
                } else {
                    error.value = "Error: ${response.code()}"
                    usersState.value = getMockUsers().take(12)
                }
            } catch (e: Exception) {
                error.value = "Network error"
                usersState.value = getMockUsers().take(12)
                Log.e("API", "Failed: ${e.message}")
            }
            loading.value = false
        }
    }

    if (loading.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text("Loading users...")
        }
    } else if (!error.value.isNullOrEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(error.value!!, color = MaterialTheme.colorScheme.error)
        }
    } else {
        Column {
            //SCROLLABLE LISTS WITH CARD UI COMPONENTS, LAZY COLUMN
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                items(usersState.value) { user ->
                    UserCard(user = user) {
                        navController.navigate("detail/${user.id}")
                    }
                }
            }
        }
    }
}

// ========== USER CARD COMPONENT ==========
@Composable
fun UserCard(user: UserResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(Modifier.padding(16.dp)) {
            //LOAD AND DISPLAY IMAGES USING COIL
            AsyncImage(
                model = "https://i.pravatar.cc/150?img=${user.id}",
                contentDescription = "Profile",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(28.dp))
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (user.id % 2 == 0) Color(0xFF4CAF50)
                                else Color(0xFFF44336)
                            )
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (user.id % 2 == 0) "Access granted" else "Access denied",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (user.id % 2 == 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
            }

            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ========== DETAIL SCREEN ==========
@Composable
fun DetailScreen(userId: Int, allUsers: List<UserResponse>) {
    val user = allUsers.find { it.id == userId } ?: UserResponse(
        id = userId,
        name = "Unknown User",
        email = "unknown@example.com"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text(
            text = "Access Details",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //COIL IMAGE LOADING (Detail Screen)
                AsyncImage(
                    model = "https://i.pravatar.cc/300?img=$userId",
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(50.dp))
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (userId % 2 == 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Text(
                        text = if (userId % 2 == 0) "ACCESS GRANTED" else "ACCESS DENIED",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (userId % 2 == 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "User Information",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow(label = "User ID", value = user.id.toString())
                InfoRow(label = "Email", value = user.email)
                InfoRow(
                    label = "Access Level",
                    value = if (userId % 3 == 0) "Admin" else "Standard"
                )
                InfoRow(label = "Last Access", value = "Today, 10:30 AM")
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

// ========== MOCK DATA FALLBACK ==========
fun getMockUsers(): List<UserResponse> {
    return listOf(
        UserResponse(1, "John Resident", "john@door.com", ""),
        UserResponse(2, "Unknown Person", "unknown@example.com", ""),
        UserResponse(3, "Mary Johnson", "mary@door.com", ""),
        UserResponse(4, "Delivery Person", "delivery@service.com", ""),
        UserResponse(5, "Security Guard", "security@building.com", ""),
        UserResponse(6, "Maintenance", "maintenance@building.com", ""),
        UserResponse(7, "Guest Visitor", "guest@visitor.com", ""),
        UserResponse(8, "Admin User", "admin@system.com", ""),
        UserResponse(9, "Test User", "test@example.com", ""),
        UserResponse(10, "Backup User", "backup@system.com", ""),
        UserResponse(11, "Alex Manager", "alex@office.com", ""),
        UserResponse(12, "Sarah Owner", "sarah@home.com", ""),
        UserResponse(13, "David Guest", "david@guest.com", ""),
        UserResponse(14, "Lisa Cleaner", "lisa@clean.com", ""),
        UserResponse(15, "Mike Engineer", "mike@tech.com", "")
    )
}