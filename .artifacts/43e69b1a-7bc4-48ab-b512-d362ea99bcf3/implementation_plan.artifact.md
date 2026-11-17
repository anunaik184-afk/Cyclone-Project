# CycloneSafe AI Assistance Backend Integration Plan

Connect the CycloneSafe AI Assistance Android app (Java + XML) to a secure Supabase Edge Function (`cyclone-ai-chat`) powered by the Google Gemini API.

## User Review Required

> [!IMPORTANT]
> **Supabase & Gemini Configuration:**
> - You will need to deploy the Supabase Edge Function `cyclone-ai-chat` in your existing Supabase project.
> - Set the `GEMINI_API_KEY` secret in your Supabase project dashboard/CLI.
> - Update your Supabase URL and Anon Key in `MainActivity.java` or `local.properties`.

## Proposed Changes

### Android Frontend (Java + XML)

#### [MODIFY] [libs.versions.toml](file:///C:/Users/DELL/AndroidStudioProjects/Aiassistance/gradle/libs.versions.toml)
- Add OkHttp library dependency for robust HTTP requests.

#### [MODIFY] [app/build.gradle.kts](file:///C:/Users/DELL/AndroidStudioProjects/Aiassistance/app/build.gradle.kts)
- Add OkHttp dependency implementation.

#### [NEW] [ChatMessage.java](file:///C:/Users/DELL/AndroidStudioProjects/Aiassistance/app/src/main/java/com/example/aiassistance/ChatMessage.java)
- Data model representing a chat message (`sender`, `message`, `timestamp`).

#### [NEW] [ChatAdapter.java](file:///C:/Users/DELL/AndroidStudioProjects/Aiassistance/app/src/main/java/com/example/aiassistance/ChatAdapter.java)
- RecyclerView Adapter for rendering user and assistant message bubbles.

#### [NEW] [activity_main.xml](file:///C:/Users/DELL/AndroidStudioProjects/Aiassistance/app/src/main/res/layout/activity_main.xml)
- UI layout with CycloneSafe AI Assistant header, RecyclerView for chat history, thinking animation view/progress bar, message input box, and Send button.
- Also include item layouts for user and assistant chat bubbles (`item_chat_user.xml`, `item_chat_assistant.xml`).

#### [MODIFY] [MainActivity.java](file:///C:/Users/DELL/AndroidStudioProjects/Aiassistance/app/src/main/java/com/example/aiassistance/MainActivity.java)
- Implement chat logic:
  - Initialize RecyclerView and adapter.
  - Handle Send button click.
  - Maintain conversation history (`JSONArray`) for follow-up context.
  - Show thinking animation while waiting for API response.
  - Make asynchronous HTTP requests via OkHttp to Supabase Edge Function (`/functions/v1/cyclone-ai-chat`).
  - Parse Gemini response and update UI on main thread.
  - Handle network errors and API failures gracefully with error messages.

---

### Supabase Backend (Edge Function & Database)

#### [NEW] [index.ts](supabase/functions/cyclone-ai-chat/index.ts) (Supabase Edge Function)
- Deno TypeScript edge function that:
  - Validates POST requests and JSON body.
  - Extracts user message and conversation history.
  - Calls Google Gemini API (`gemini-1.5-flash`) with system prompt for CycloneSafe emergency assistance.
  - Optionally logs messages to `chat_messages` table if authenticated via Bearer token.
  - Returns generated response as JSON.

#### [NEW] [schema.sql](supabase/schema.sql)
- SQL schema for `chat_messages` table and Row Level Security (RLS) policies.

## Verification Plan

### Automated Tests
- Run Gradle build (`app:assembleDebug`) to verify compilation and syntax correctness.

### Manual Verification
- Deploy app to Android emulator/device.
- Send test messages ("What should I do during a category 4 cyclone?") and verify:
  - Thinking animation appears while waiting.
  - Actual Gemini response is displayed in chat.
  - Follow-up messages maintain conversation context.
  - Network errors are handled gracefully.
