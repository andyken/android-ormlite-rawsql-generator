# android-ormlite-rawsql-generator

This plugin generates raw insert and update sql of ormlite class for you.

## Installation

- in Android Studio: go to `Preferences → Plugins → Browse repositories` and search for `OrmliteRawSqlGenerator`

_or_

- [download it](https://plugins.jetbrains.com/androidstudio/plugin/9454-ormliterawsqlgenerator) and install via `Preferences → Plugins → Install plugin from disk`

## Usage

![](img/android-ormlite-rawsql-generator.gif)

1. Please ensure using the ormlite annoation @DatabaseTable(tableName=you_table_name) and @DatabaseField on your database field.
2. Right click on the ormlite class, then `Generate` and `Generate Raw Sql of ormlite class`
3. Pick fields you want, you also have an option to include fields from the super class.
4. Click `Confirm`.
