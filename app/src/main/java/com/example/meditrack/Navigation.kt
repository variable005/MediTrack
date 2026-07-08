package com.example.meditrack

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.example.meditrack.navigation.BottomNavItem
import com.example.meditrack.ui.screens.AddMedicineScreen
import com.example.meditrack.ui.screens.HistoryScreen
import com.example.meditrack.ui.screens.HomeScreen
import com.example.meditrack.ui.screens.SettingsScreen
import com.example.meditrack.ui.screens.OnboardingScreen
import com.example.meditrack.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val leftNavItem = BottomNavItem.Home
    val rightNavItem = BottomNavItem.History
    val settingsNavItem = BottomNavItem.Settings

    // Show bottom navigation bar on the three main tabs: Home, History, and Settings
    val showNavElements = currentDestination?.route == leftNavItem.route || 
            currentDestination?.route == rightNavItem.route ||
            currentDestination?.route == settingsNavItem.route

    val hapticFeedback = LocalHapticFeedback.current
    val hapticEnabled by viewModel.hapticEnabled.collectAsState()

    val triggerHaptic = {
        if (hapticEnabled) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Floating Bottom Dock Pill Bar (Left side)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp)
                                .clip(RoundedCornerShape(36.dp))
                                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Home Tab
                            val isHomeSelected = currentDestination?.hierarchy?.any { it.route == leftNavItem.route } == true
                            CustomBottomNavigationItem(
                                screen = leftNavItem,
                                isSelected = isHomeSelected,
                                onClick = {
                                    triggerHaptic()
                                    navController.navigate(leftNavItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // History Tab
                            val isHistorySelected = currentDestination?.hierarchy?.any { it.route == rightNavItem.route } == true
                            CustomBottomNavigationItem(
                                screen = rightNavItem,
                                isSelected = isHistorySelected,
                                onClick = {
                                    triggerHaptic()
                                    navController.navigate(rightNavItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )

                            // Settings Tab
                            val isSettingsSelected = currentDestination?.hierarchy?.any { it.route == settingsNavItem.route } == true
                            CustomBottomNavigationItem(
                                screen = settingsNavItem,
                                isSelected = isSettingsSelected,
                                onClick = {
                                    triggerHaptic()
                                    navController.navigate(settingsNavItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        // 2. Separate Floating Action Button (Right side, matching search circle design)
                        FloatingActionButton(
                            onClick = {
                                triggerHaptic()
                                navController.navigate(BottomNavItem.AddMedicine.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(72.dp), // Height matches dock pill
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add Medicine",
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
        val startDestination = if (onboardingCompleted) BottomNavItem.Home.route else "onboarding"

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350, easing = EaseInOutCubic)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(350, easing = EaseInOutCubic)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350, easing = EaseInOutCubic)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(350, easing = EaseInOutCubic)
                ) + fadeOut(animationSpec = tween(350))
            }
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
            composable(settingsNavItem.route) {
                SettingsScreen(navController = navController, viewModel = viewModel, paddingValues = innerPadding)
            }
            composable("onboarding") {
                OnboardingScreen(navController = navController, viewModel = viewModel, paddingValues = innerPadding)
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
    // Spring-loaded selection bounce scale
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "TabScaleAnimation"
    )

    Box(
        modifier = Modifier
            .weight(1f)
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
            modifier = Modifier
                .size(48.dp)
                .scale(scale)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
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