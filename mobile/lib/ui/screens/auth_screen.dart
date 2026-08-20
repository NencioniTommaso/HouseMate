import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../state/auth_provider.dart';
import '../../shared/dto/auth/request/register_request_dto.dart';

class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  // State variable to toggle modes
  bool _isLogin = true;

  final TextEditingController _passwordController = TextEditingController();
  final TextEditingController _confirmPasswordController = TextEditingController();
  final TextEditingController _emailController = TextEditingController();
  final TextEditingController _nameController = TextEditingController();
  final TextEditingController _surnameController = TextEditingController();

  @override
  void dispose() {
    _nameController.dispose();
    _surnameController.dispose();
    _emailController.dispose();
    _passwordController.dispose();
    _confirmPasswordController.dispose();
    super.dispose();
  }

  void _submit(AuthProvider authState) async {
    // 1. Clear local UI state immediately
    if (mounted) {
      setState(() {
        authState.errorMessage = null;
      });
    }

    if (_isLogin) {
      final success = await authState.login(
        _emailController.text.trim(),
        _passwordController.text,
      );

      if (!success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(authState.errorMessage ?? "Login failed")),
        );
      }
    } else {
      if (_passwordController.text != _confirmPasswordController.text) {
        setState(() {
          authState.errorMessage = "Passwords do not match";
        });
        return;
      }

      final request = RegisterRequestDTO(
        email: _emailController.text.trim(),
        name: _nameController.text.trim(),
        surname: _surnameController.text.trim(),
        password: _passwordController.text,
      );
      final success = await authState.register(request);

      if (!success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(authState.errorMessage ?? "Registration failed")),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final authState = context.watch<AuthProvider>();

    return Scaffold(
      body: Center(
        child: SingleChildScrollView( // Prevents keyboard overflow errors
          padding: const EdgeInsets.all(24.0),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(_isLogin ? 'Welcome Back' : 'Create Account',
                  style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold)),
              const SizedBox(height: 40),

              if (!_isLogin) ...[
                TextField(
                  controller: _nameController,
                  decoration: const InputDecoration(labelText: 'First Name', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 16),
                TextField(
                  controller: _surnameController,
                  decoration: const InputDecoration(labelText: 'Last Name', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 16),
              ],

              TextField(
                controller: _emailController,
                decoration: const InputDecoration(labelText: 'Email', border: OutlineInputBorder()),
                keyboardType: TextInputType.emailAddress,
              ),
              const SizedBox(height: 24),

              TextField(
                controller: _passwordController,
                obscureText: true,
                decoration: const InputDecoration(labelText: 'Password', border: OutlineInputBorder()),
              ),
              const SizedBox(height: 24),


              if(!_isLogin) ...[
                TextField(
                  controller: _confirmPasswordController,
                  obscureText: true,
                  decoration: const InputDecoration(labelText: 'Confirm Password', border: OutlineInputBorder()),
                ),
                const SizedBox(height: 16),
              ],

              if (authState.errorMessage != null)
                Text(authState.errorMessage!, style: const TextStyle(color: Colors.red)),

              const SizedBox(height: 24),

              authState.isLoading
                  ? const CircularProgressIndicator()
                  : ElevatedButton(
                onPressed: () => _submit(authState),
                child: Text(_isLogin ? 'Login' : 'Register'),
              ),

              // Toggle Button
              TextButton(
                onPressed: () {
                  setState(() {
                    _isLogin = !_isLogin;
                    authState.errorMessage = null; // Clear errors on swap
                  });
                },
                child: Text(_isLogin ? "Don't have an account? Register" : "Already have an account? Login"),
              )
            ],
          ),
        ),
      ),
    );
  }
}