# Fix CI build: Generate GEMINI_API_KEY BuildConfig and add credentials/auth deps

This branch makes two changes to address Kotlin compilation errors observed in CI:

1. Generate a BuildConfig field `GEMINI_API_KEY` in `app/build.gradle.kts`, reading the value from the environment (falling back to a placeholder). This resolves `Unresolved reference: GEMINI_API_KEY` during compilation.

2. Add AndroidX Credentials (`androidx.credentials:credentials`) and Google Play `play-services-auth` dependencies to resolve missing types such as `CredentialManager`, `GetCredentialRequest`, `GoogleIdTokenCredential`, and `GetCredentialException`.

Additionally, the workflow has been updated (on this branch) so the Gradle build step receives `GEMINI_API_KEY` from repo secrets.

After merging, CI will still need the secret `GEMINI_API_KEY` set in repository Secrets for the correct key to be included at build time.
