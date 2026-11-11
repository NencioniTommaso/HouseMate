import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mobile/core/theme/app_colors.dart';
import 'package:mobile/ui/widgets/shared/app_button.dart';
import 'package:mobile/ui/widgets/shared/app_card.dart';
import 'package:mobile/ui/widgets/shared/app_empty_state.dart';
import 'package:mobile/ui/widgets/shared/app_header.dart';
import 'package:mobile/ui/widgets/shared/app_text_field.dart';

Widget wrapWidget(Widget child) {
  return MaterialApp(
    home: Scaffold(
      body: child,
    ),
  );
}

void main() {
  group('AppButton', () {
    testWidgets('renders label and triggers onPressed when tapped', (tester) async {
      var pressed = false;
      await tester.pumpWidget(
        wrapWidget(
          AppButton(
            label: 'Submit',
            onPressed: () => pressed = true,
          ),
        ),
      );

      expect(find.text('Submit'), findsOneWidget);
      await tester.tap(find.text('Submit'));
      await tester.pump();

      expect(pressed, isTrue);
    });

    testWidgets('renders icon when provided', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppButton(
            label: 'Add',
            icon: Icons.add,
          ),
        ),
      );

      expect(find.byIcon(Icons.add), findsOneWidget);
      expect(find.text('Add'), findsOneWidget);
    });

    testWidgets('shows loading indicator and disables onTap when isLoading is true', (tester) async {
      var pressed = false;
      await tester.pumpWidget(
        wrapWidget(
          AppButton(
            label: 'Loading Button',
            isLoading: true,
            onPressed: () => pressed = true,
          ),
        ),
      );

      expect(find.byType(CircularProgressIndicator), findsOneWidget);
      expect(find.text('Loading Button'), findsNothing);

      // Verify button is disabled
      final elevatedButton = tester.widget<ElevatedButton>(find.byType(ElevatedButton));
      expect(elevatedButton.onPressed, isNull);
    });

    testWidgets('renders fullWidth correctly', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const Center(
            child: SizedBox(
              width: 300,
              child: AppButton(
                label: 'Full Width',
                fullWidth: true,
              ),
            ),
          ),
        ),
      );

      expect(find.text('Full Width'), findsOneWidget);
      final sizedBoxes = tester.widgetList<SizedBox>(find.byType(SizedBox));
      expect(sizedBoxes.any((sb) => sb.width == double.infinity), isTrue);
    });

    testWidgets('renders with custom padding and fontSize', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppButton(
            label: 'Custom Style',
            padding: EdgeInsets.all(24),
            fontSize: 20,
          ),
        ),
      );

      final textWidget = tester.widget<Text>(find.text('Custom Style'));
      expect(textWidget.style?.fontSize, 20);
    });

    testWidgets('supports all button variants', (tester) async {
      for (final variant in AppButtonVariant.values) {
        await tester.pumpWidget(
          wrapWidget(
            AppButton(
              label: variant.name,
              variant: variant,
            ),
          ),
        );
        expect(find.text(variant.name), findsOneWidget);
      }
    });

    testWidgets('can be rendered with null onPressed', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppButton(label: 'Disabled'),
        ),
      );

      final elevatedButton = tester.widget<ElevatedButton>(find.byType(ElevatedButton));
      expect(elevatedButton.onPressed, isNull);
    });
  });

  group('AppCard', () {
    testWidgets('renders child widget correctly', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppCard(
            child: Text('Card Content'),
          ),
        ),
      );

      expect(find.text('Card Content'), findsOneWidget);
    });

    testWidgets('handles onTap callback', (tester) async {
      var tapped = false;
      await tester.pumpWidget(
        wrapWidget(
          AppCard(
            onTap: () => tapped = true,
            child: const Text('Tappable Card'),
          ),
        ),
      );

      await tester.tap(find.text('Tappable Card'));
      await tester.pump();

      expect(tapped, isTrue);
    });

    testWidgets('applies custom padding, margin, and backgroundColor', (tester) async {
      const customPadding = EdgeInsets.all(32);
      const customMargin = EdgeInsets.symmetric(horizontal: 16);
      const customBg = Colors.purple;

      await tester.pumpWidget(
        wrapWidget(
          const AppCard(
            padding: customPadding,
            margin: customMargin,
            backgroundColor: customBg,
            child: Text('Custom Card'),
          ),
        ),
      );

      final container = tester.widget<Container>(find.byType(Container).first);
      expect(container.margin, customMargin);

      final decoration = container.decoration as BoxDecoration;
      expect(decoration.color, customBg);

      final paddingWidget = tester.widget<Padding>(
        find.descendant(of: find.byType(InkWell), matching: find.byType(Padding)),
      );
      expect(paddingWidget.padding, customPadding);
    });
  });

  group('AppEmptyState', () {
    testWidgets('renders message and default icon', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppEmptyState(message: 'No items found'),
        ),
      );

      expect(find.text('No items found'), findsOneWidget);
      expect(find.byIcon(Icons.info_outline), findsOneWidget);
    });

    testWidgets('renders custom icon', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppEmptyState(
            message: 'No chores yet',
            icon: Icons.cleaning_services,
          ),
        ),
      );

      expect(find.text('No chores yet'), findsOneWidget);
      expect(find.byIcon(Icons.cleaning_services), findsOneWidget);
    });
  });

  group('AppHeader', () {
    testWidgets('renders title only', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppHeader(title: 'Household Overview'),
        ),
      );

      expect(find.text('Household Overview'), findsOneWidget);
    });

    testWidgets('renders title, subtitle, and action widget', (tester) async {
      var actionTapped = false;
      await tester.pumpWidget(
        wrapWidget(
          AppHeader(
            title: 'Chores',
            subtitle: 'Manage daily tasks',
            action: IconButton(
              icon: const Icon(Icons.add),
              onPressed: () => actionTapped = true,
            ),
          ),
        ),
      );

      expect(find.text('Chores'), findsOneWidget);
      expect(find.text('Manage daily tasks'), findsOneWidget);
      expect(find.byIcon(Icons.add), findsOneWidget);

      await tester.tap(find.byIcon(Icons.add));
      await tester.pump();
      expect(actionTapped, isTrue);
    });

    testWidgets('aligns content properly when centered is true vs false', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppHeader(
            title: 'Centered Header',
            subtitle: 'Subtitle text',
            centered: true,
          ),
        ),
      );

      final titleText = tester.widget<Text>(find.text('Centered Header'));
      expect(titleText.textAlign, TextAlign.center);

      final subText = tester.widget<Text>(find.text('Subtitle text'));
      expect(subText.textAlign, TextAlign.center);
    });
  });

  group('AppTextField', () {
    testWidgets('renders label when provided and hides it when null', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppTextField(label: 'Email Address'),
        ),
      );
      expect(find.text('Email Address'), findsOneWidget);

      await tester.pumpWidget(
        wrapWidget(
          const AppTextField(label: null),
        ),
      );
      expect(find.text('Email Address'), findsNothing);
    });

    testWidgets('renders hint text and prefix', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppTextField(
            hintText: 'Enter your name',
            prefixText: '\$',
            prefixIcon: Icons.attach_money,
          ),
        ),
      );

      expect(find.text('Enter your name'), findsOneWidget);
      expect(find.text('\$'), findsOneWidget);
      expect(find.byIcon(Icons.attach_money), findsOneWidget);
    });

    testWidgets('handles text input and onChanged callback', (tester) async {
      var changedValue = '';
      await tester.pumpWidget(
        wrapWidget(
          AppTextField(
            hintText: 'Type here',
            onChanged: (val) => changedValue = val,
          ),
        ),
      );

      await tester.enterText(find.byType(TextField), 'Hello World');
      await tester.pump();

      expect(changedValue, 'Hello World');
    });

    testWidgets('obscures text when isPassword is true', (tester) async {
      await tester.pumpWidget(
        wrapWidget(
          const AppTextField(
            isPassword: true,
          ),
        ),
      );

      final textField = tester.widget<TextField>(find.byType(TextField));
      expect(textField.obscureText, isTrue);
    });

    testWidgets('uses provided controller', (tester) async {
      final controller = TextEditingController(text: 'Initial');
      await tester.pumpWidget(
        wrapWidget(
          AppTextField(
            controller: controller,
          ),
        ),
      );

      expect(find.text('Initial'), findsOneWidget);

      await tester.enterText(find.byType(TextField), 'Updated');
      expect(controller.text, 'Updated');
    });
  });
}
