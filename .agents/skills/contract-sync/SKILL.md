---
name: contract-sync
description: Syncs Java DTOs in backend/shared with Dart models in mobile, updating JSON serialization. Use when backend API contracts or DTOs change.
---

# Contract Sync Skill

## Summary

Syncs Java DTOs (`backend` / `shared`) to Dart models (`mobile`), ensuring 1:1 API contract parity across fields, nullability, and JSON serialization.

## When to Use

- You added or modified a Java DTO or Enum in `shared` or `backend`.
- You modified a backend `@RestController` endpoint signature or payload structure.
- You need to update the Flutter app to handle new backend request/response structures.

## Steps

1. **Analyze the Java Source:**
   Identify the target Java DTO or Enum. Pay close attention to:
   - Field types and collections
   - Nullability constraints (e.g., `Optional`, `@Nullable`, `@NotNull`)
   - Jackson annotations (e.g., `@JsonProperty`, `@JsonFormat`, `@JsonValue`)

2. **Locate the Target Dart Model:**
   Find or create the matching Dart file in `mobile/lib/.../models/`.

3. **Map the Types:**
   Apply standard Java-to-Dart type translations:
   - `String` -> `String`
   - `int`, `long`, `Integer`, `Long` -> `int`
   - `double`, `float`, `Double`, `Float`, `BigDecimal` -> `double`
   - `boolean`, `Boolean` -> `bool`
   - `List<T>` -> `List<T>`
   - `Set<T>` -> `Set<T>`
   - `Map<K, V>` -> `Map<K, V>`
   - Java enums -> Dart enums

4. **Update the Dart Model:**
   Update class fields to match the Java DTO, strictly preserving nullability (`T?` vs `T`).

5. **Implement Serialization:**
   - Implement or update `fromJson(Map<String, dynamic> json)` factory constructor.
   - Implement or update `Map<String, dynamic> toJson()` method.
   - If using code generation (e.g., `json_serializable`), ensure `@JsonSerializable()` annotations and part directives are updated, then run build runner:
     ```bash
     dart run build_runner build --delete-conflicting-outputs
     ```

6. **Update API Client Services (If Applicable):**
   If endpoint paths, query parameters, or request/response types changed, locate the corresponding service classes in `mobile/lib/.../services/` and update HTTP calls accordingly.

## Gotchas

- **Enum Values:** Ensure Dart enum strings match the Java enum constants serialized by Jackson (watch for case changes or custom `@JsonValue` mappings).
- **Date/Time Formats:** Convert Java date/time objects (e.g., `LocalDate`, `Instant`, `LocalDateTime`) to ISO-8601 strings in Dart's `toJson()` and parse them via `DateTime.parse()` in `fromJson()`.
- **Numbers & Decimals:** Jackson may serialize numbers as integers or floating-point decimals. In Dart, ensure decimal fields use `(json['fieldName'] as num?)?.toDouble()` to prevent runtime cast exceptions when parsing integers as doubles.