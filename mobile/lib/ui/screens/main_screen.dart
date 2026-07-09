import 'package:flutter/material.dart';

import 'tabs/user_screen.dart';
import 'tabs/household_screen.dart';
import 'tabs/expenses_screen.dart';
import 'tabs/assignments_screen.dart';

class MainScreen extends StatefulWidget {
  const MainScreen({super.key});

  @override
  State<MainScreen> createState() => _MainScreenState();
}

class _MainScreenState extends State<MainScreen> {
  // This integer tracks which tab is currently active
  int _currentIndex = 0;

  // The actual screens that correspond to your desktop tabs
  final List<Widget> _screens = const [
    HouseholdScreen(),
    AssignmentsScreen(),
    ExpensesScreen(),
    UserScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(

      appBar: AppBar(
        title: Text(
          'HouseMate',
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        centerTitle: false,
        elevation: 1,       // Adds a tiny drop shadow

        // 'actions' is a list of widgets aligned to the right side of the AppBar
        actions: [
          IconButton(
            icon: const Icon(Icons.notifications_none),
            onPressed: () {
              // TODO: Open the Notifications Modal/Screen later
              print("Notifications clicked!");
            },
          ),
          // You could easily add a profile picture avatar here later too!
        ],
      ),

      // The IndexedStack keeps all screens alive but only shows the _currentIndex one
      body: IndexedStack(
        index: _currentIndex,
        children: _screens,
      ),

      // The classic Mobile Bottom Navigation
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (int index) {
          setState(() {
            _currentIndex = index; // Tap a button, swap the screen!
          });
        },
        destinations: const [
          NavigationDestination(
            icon: Icon(Icons.home_outlined),
            selectedIcon: Icon(Icons.home),
            label: 'Household',
          ),
          NavigationDestination(
              icon: Icon(Icons.check_box_outlined),
              selectedIcon: Icon(Icons.check_box),
              label: 'Assignments',
          ),
          NavigationDestination(
            icon: Icon(Icons.attach_money_outlined),
            selectedIcon: Icon(Icons.attach_money),
            label: 'Expenses',
          ),
          NavigationDestination(
            icon: Icon(Icons.person_outline),
            selectedIcon: Icon(Icons.person),
            label: 'Profile',
          ),
        ],
      ),
    );
  }
}