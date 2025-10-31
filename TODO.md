# TODO

This document outlines the remaining work required to get the project building successfully.

## Current Blocker: `R` Class Not Generated

The primary blocker is the failure of the Android Gradle Plugin to generate the `R` class, which contains IDs for all application resources. This results in numerous "cannot find symbol" errors during compilation, preventing the build from completing.

### Symptoms

- The build fails during the `:app:compileDebugJavaWithJavac` task.
- The error log shows multiple `cannot find symbol` errors for variables within the `R` class (e.g., `R.attr.colorPrimary`, `R.layout.support_simple_spinner_dropdown_item`).
- All other Java compilation errors (e.g., those related to the Dagger migration) have been resolved.

### What Has Been Tried

1.  **Resolved all other compilation errors:** Systematically fixed all compilation errors unrelated to the `R` class.
2.  **Clean Builds:** Multiple attempts have been made to clean the project using `./gradlew clean` and by manually deleting the `app/build` directory.
3.  **Permission Fixes:** Resolved file ownership issues for the `.gradle` and `.kotlin` directories.
4.  **Dependency Review:** Briefly inspected the `app/build.gradle` file for obvious issues.

## Next Steps / What Remains to be Tried

The root cause is almost certainly a configuration issue within the `app/build.gradle` file that is preventing the Android Asset Packaging Tool (AAPT) from running correctly.

### 1. In-depth Dependency Analysis

A thorough review of all dependencies in `app/build.gradle` is the next logical step.
- **Check for version conflicts:** Ensure there are no conflicting versions between libraries, especially within the AndroidX ecosystem.
- **Check for missing dependencies:** The error `R.layout.support_simple_spinner_dropdown_item` suggests that a core AndroidX library (like `androidx.appcompat`) may be missing or improperly configured.
- **Simplify dependencies:** Temporarily comment out non-essential dependencies to see if the `R` class generates. This can help isolate the problematic library.

### 2. Gradle Configuration Review

- **`compileOptions` and `kotlinOptions`:** Double-check that the Java and Kotlin versions are correctly set to 21 in the `build.gradle` file, as this was a source of previous issues.
- **Namespace declaration:** Verify the `namespace 'io.github.hidroh.materialistic'` declaration is correct.

### 3. Resource File Inspection

- **Check for errors in XML:** Manually inspect recent changes to XML layout or resource files. An error in one of these files can silently cause AAPT to fail.
- **Isolate resource files:** Temporarily remove all resource files and add them back in batches to see if a specific file is causing the problem.

### 4. Consult Gradle Build Scans

- **Generate and analyze a build scan:** Run the build with the `--scan` option (`./gradlew assembleDebug --scan`) to get a detailed report of the build process. This may provide more insight into why the resource processing task is failing.
