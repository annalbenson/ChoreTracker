package com.annabenson.tidy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.annabenson.tidy.network.GeminiClient;
import com.annabenson.tidy.network.GeminiRequest;
import com.annabenson.tidy.network.GeminiResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OnboardingActivity extends AppCompatActivity {

    // Name is now captured at sign-up; onboarding starts at home profile
    private static final int STEP_HOME_TYPE      = 0;
    private static final int STEP_BEDROOMS       = 1;
    private static final int STEP_BATHROOMS      = 2;
    private static final int STEP_LAUNDRY        = 3;
    private static final int STEP_HOUSEHOLD      = 4;
    private static final int STEP_CLEANING_STYLE = 5;
    private static final int STEP_PAIN_POINTS    = 6;

    private RecyclerView chatRecycler;
    private TextInputEditText messageInput;
    private ImageButton sendButton;
    private OnboardingAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int currentStep = STEP_HOME_TYPE;
    private final HomeProfile profile = new HomeProfile();
    private DatabaseHandler databaseHandler;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        userId = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
                .getInt(Prefs.KEY_USER_ID, -1);
        if (userId == -1) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return;
        }

        databaseHandler = new DatabaseHandler(this);

        chatRecycler = findViewById(R.id.chatRecycler);
        messageInput = findViewById(R.id.messageInput);
        sendButton   = findViewById(R.id.sendButton);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecycler.setLayoutManager(layoutManager);

        adapter = new OnboardingAdapter();
        adapter.setChipListener(this::handleChipResponse);
        chatRecycler.setAdapter(adapter);

        sendButton.setOnClickListener(v -> sendMessage());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); return true; }
            return false;
        });

        String name = databaseHandler.getUserName(userId);
        String firstName = name != null ? name.split("\\s+")[0] : "there";
        postTilly("Hi " + firstName + "! I'm Tilly 🌿 Let's set up your home so I can build " +
                "you the perfect chore list. What kind of place do you live in — " +
                "apartment, house, condo?");
    }

    private void sendMessage() {
        String text = messageInput.getText() != null
                ? messageInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;
        messageInput.setText("");
        addUserMessage(text);
        handleTextResponse(text);
    }

    private void handleTextResponse(String text) {
        switch (currentStep) {
            case STEP_HOME_TYPE:
                profile.homeType = extractHomeType(text);
                currentStep = STEP_BEDROOMS;
                postTilly("Got it! How many bedrooms does it have?");
                break;

            case STEP_BEDROOMS:
                profile.bedrooms = parseNumberWords(text, 1);
                currentStep = STEP_BATHROOMS;
                postTilly("And bathrooms?");
                break;

            case STEP_BATHROOMS:
                profile.bathrooms = parseNumberWords(text, 1);
                currentStep = STEP_LAUNDRY;
                postTillyWithChips(
                        "Do you have laundry at home, or do you use a shared machine or laundromat?",
                        Arrays.asList("In-unit", "Shared in building", "Laundromat"));
                break;

            case STEP_PAIN_POINTS:
                profile.painPoints = text;
                finishOnboarding();
                break;

            default:
                break;
        }
    }

    private void handleChipResponse(List<String> selected) {
        adapter.disableLastChips();
        String answer = selected.isEmpty() ? "—" : String.join(", ", selected);
        addUserMessage(answer);

        switch (currentStep) {
            case STEP_LAUNDRY:
                profile.laundryType = answer;
                currentStep = STEP_HOUSEHOLD;
                postTillyWithChips(
                        "Who shares the space with you? Pick all that apply.",
                        Arrays.asList("Just me", "Partner", "Kids", "Roommates", "Pets"));
                break;

            case STEP_HOUSEHOLD:
                profile.householdMembers = answer;
                currentStep = STEP_CLEANING_STYLE;
                postTillyWithChips(
                        "How would you describe your current cleaning routine?",
                        Arrays.asList("Pretty on top of it", "Weekly sweep", "As-needed", "Honestly… it's chaos"));
                break;

            case STEP_CLEANING_STYLE:
                profile.cleaningStyle = answer;
                currentStep = STEP_PAIN_POINTS;
                postTilly("Last one! Any cleaning sore spots? Things that pile up, " +
                        "areas you dread, or chores that always get skipped?");
                break;
        }
    }

    private void finishOnboarding() {
        databaseHandler.saveProfile(profile, userId);
        postTilly("Perfect — I've got everything I need! Give me just a moment while I put " +
                "together a chore list for your home… 🌿");
        handler.postDelayed(this::generateStarterChores, 600);
    }

    private static final boolean TEST_MODE = BuildConfig.DEBUG;

    private void generateStarterChores() {
        if (TEST_MODE) {
            Log.d("Tilly", "TEST_MODE: using default chore list");
            saveDefaultChores();
            navigateToMain();
            return;
        }
        String prompt = buildChorePrompt(profile);
        GeminiRequest request = new GeminiRequest(
                "You are Tilly, a helpful home cleaning assistant. " +
                "When asked, generate a practical chore list as plain text — " +
                "one chore per line in the format: \"Chore name | Frequency\" " +
                "where Frequency is one of: Daily, Weekly, Biweekly, Monthly. " +
                "No bullets, no numbers, no extra commentary. Just the list.",
                Collections.singletonList(new GeminiRequest.Content("user",
                        Collections.singletonList(new GeminiRequest.Part(prompt)))));

        GeminiClient.get().generate(BuildConfig.GEMINI_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        String text = response.isSuccessful() && response.body() != null
                                ? response.body().getText() : null;
                        int saved = text != null ? parseAndSaveChores(text) : 0;
                        if (saved == 0) saveDefaultChores();
                        navigateToMain();
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        Log.e("Tilly", "Chore generation failed: " + t.getMessage());
                        saveDefaultChores();
                        navigateToMain();
                    }
                });
    }

    private String buildChorePrompt(HomeProfile p) {
        return "Generate a starter chore list for my home. Here's my situation:\n" +
                "- Home type: " + p.homeType + "\n" +
                "- Bedrooms: " + p.bedrooms + ", Bathrooms: " + p.bathrooms + "\n" +
                "- Laundry: " + p.laundryType + "\n" +
                "- Household: " + p.householdMembers + "\n" +
                "- Cleaning style: " + p.cleaningStyle + "\n" +
                "- Pain points: " + p.painPoints + "\n\n" +
                "Give me 10–14 practical chores suited to this home.";
    }

    private int parseAndSaveChores(String raw) {
        int count = 0;
        for (String line : raw.split("\n")) {
            line = line.trim()
                    .replaceAll("^[\\*\\-\\d\\.]+\\s*", "")
                    .replaceAll("\\*\\*", "");
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\|");
            String name = parts[0].trim();
            String frequency = parts.length > 1 ? normalizeFrequency(parts[1].trim()) : "Weekly";
            if (!name.isEmpty()) {
                databaseHandler.addChore(name, frequency, 0, userId);
                count++;
            }
        }
        return count;
    }

    private String normalizeFrequency(String raw) {
        String s = raw.toLowerCase();
        if (s.contains("daily"))    return "Daily";
        if (s.contains("biweekly")) return "Biweekly";
        if (s.contains("monthly"))  return "Monthly";
        return "Weekly";
    }

    private void saveDefaultChores() {
        String[][] defaults = {
            {"Wash dishes",         "Daily"},
            {"Wipe down kitchen",   "Daily"},
            {"Vacuum floors",       "Weekly"},
            {"Mop floors",          "Weekly"},
            {"Clean bathrooms",     "Weekly"},
            {"Take out trash",      "Weekly"},
            {"Do laundry",          "Weekly"},
            {"Change bed sheets",   "Biweekly"},
            {"Dust surfaces",       "Biweekly"},
            {"Clean mirrors",       "Biweekly"},
            {"Deep clean kitchen",  "Monthly"},
            {"Clean oven",          "Monthly"},
        };
        for (String[] chore : defaults) {
            databaseHandler.addChore(chore[0], chore[1], 0, userId);
        }
    }

    private void navigateToMain() {
        runOnUiThread(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void postTilly(String text) {
        handler.postDelayed(() -> {
            adapter.addMessage(new OnboardingMessage(OnboardingMessage.Type.TILLY, text));
            scrollToBottom();
        }, 400);
    }

    private void postTillyWithChips(String text, List<String> chips) {
        handler.postDelayed(() -> {
            adapter.addMessage(new OnboardingMessage(OnboardingMessage.Type.TILLY, text));
            adapter.addMessage(new OnboardingMessage(chips, text));
            scrollToBottom();
        }, 400);
    }

    private void addUserMessage(String text) {
        adapter.addMessage(new OnboardingMessage(OnboardingMessage.Type.USER, text));
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatRecycler.post(() ->
                chatRecycler.smoothScrollToPosition(adapter.getItemCount() - 1));
    }

    private String extractHomeType(String raw) {
        String s = raw.toLowerCase();
        if (s.contains("apartment") || s.contains("apt"))      return "Apartment";
        if (s.contains("townhouse") || s.contains("townhome")) return "Townhouse";
        if (s.contains("condo"))                               return "Condo";
        if (s.contains("studio"))                              return "Studio";
        if (s.contains("house"))                               return "House";
        if (s.contains("duplex"))                              return "Duplex";
        return capitalize(raw.trim());
    }

    private int parseNumberWords(String s, int fallback) {
        String lower = s.toLowerCase().trim();
        if (lower.matches(".*\\bone\\b.*") || lower.startsWith("a ") || lower.equals("a")) return 1;
        if (lower.contains("two")   || lower.contains("couple")) return 2;
        if (lower.contains("three"))                              return 3;
        if (lower.contains("four"))                               return 4;
        if (lower.contains("five"))                               return 5;
        if (lower.contains("six"))                                return 6;
        String digitsOnly = s.replaceAll("[^0-9]", "");
        if (digitsOnly.isEmpty()) return fallback;
        try { return Integer.parseInt(digitsOnly); }
        catch (NumberFormatException e) { return fallback; }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
