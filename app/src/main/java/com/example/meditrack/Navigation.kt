package com.example.meditrack

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.meditrack.navigation.BottomNavItem
import com.example.meditrack.ui.screens.AddMedicineScreen
import com.example.meditrack.ui.screens.HistoryScreen
import com.example.meditrack.ui.screens.HomeScreen
import com.example.meditrack.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val leftNavItem = BottomNavItem.Home
    val rightNavItem = BottomNavItem.History

    val showNavElements = currentDestination?.route != BottomNavItem.AddMedicine.route

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showNavElements,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(durationMillis = 300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. LEFT ICON: HOME
                        val isHomeSelected = currentDestination?.hierarchy?.any { it.route == leftNavItem.route } == true
                        CustomBottomNavigationItem(
                            screen = leftNavItem,
                            isSelected = isHomeSelected,
                            onClick = {
                                navController.navigate(leftNavItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )

                        // 2. CENTER EMBEDDED FAB
                        FloatingActionButton(
                            onClick = {
                                navController.navigate(BottomNavItem.AddMedicine.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier
                                .size(56.dp)
                                .offset(y = (-4).dp),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Medicine",
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // 3. RIGHT ICON: HISTORY
                        val isHistorySelected = currentDestination?.hierarchy?.any { it.route == rightNavItem.route } == true
                        CustomBottomNavigationItem(
                            screen = rightNavItem,
                            isSelected = isHistorySelected,
                            onClick = {
                                navController.navigate(rightNavItem.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(navController = navController, viewModel = viewModel, paddingValues = innerPadding)
            }
            composable(BottomNavItem.AddMedicine.route) {
                AddMedicineScreen(navController = navController, viewModel = viewModel, paddingValues = innerPadding)
            }
            composable(BottomNavItem.History.route) {
                HistoryScreen(navController = navController, viewModel = viewModel, paddingValues = innerPadding)
            }
        }
    }
}

@Composable
fun RowScope.CustomBottomNavigationItem(
    screen: BottomNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .weight(1f)
            .height(56.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                    contentDescription = screen.label,
                    tint = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}