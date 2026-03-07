package com.annabenson.tidy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Arrays;
import java.util.List;

public class OnboardingActivity extends AppCompatActivity {

    // Onboarding steps
    private static final int STEP_NAME           = 0;
    private static final int STEP_HOME_TYPE      = 1;
    private static final int STEP_BEDROOMS       = 2;
    private static final int STEP_BATHROOMS      = 3;
    private static final int STEP_LAUNDRY        = 4;
    private static final int STEP_HOUSEHOLD      = 5;
    private static final int STEP_CLEANING_STYLE = 6;
    private static final int STEP_PAIN_POINTS    = 7;
    private static final int STEP_DONE           = 8;

    private RecyclerView chatRecycler;
    private TextInputEditText messageInput;
    private ImageButton sendButton;
    private OnboardingAdapter adapter;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private int currentStep = STEP_NAME;
    private final HomeProfile profile = new HomeProfile();
    private DatabaseHandler databaseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

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

        // Kick off with Tilly's opening line
        postTilly("Hi there! I'm Tilly 🌿 I'm here to help you stay on top of your home. " +
                "Let's get to know each other a little. What's your name?");
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
            case STEP_NAME:
                profile.name = extractName(text);
                currentStep = STEP_HOME_TYPE;
                postTilly("Great to meet you, " + profile.name + "! 😊 " +
                        "Tell me about your place — is it an apartment, house, condo, or something else?");
                break;

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
                // Steps handled by chips don't need a text fallback
                break;
        }
    }

    private void handleChipResponse(List<String> selected) {
        adapter.disableChips();
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
        databaseHandler.saveProfile(profile);
        postTilly("Perfect — I've got everything I need! I'll put together a starter chore " +
                "list based on your home. Let's keep things tidy, " + profile.name + " 🌿");
        handler.postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, 2200);
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

    /** Extracts a proper name from phrases like "my name is anna", "I'm Anna", "call me Anna". */
    private String extractName(String raw) {
        String s = raw.trim().toLowerCase();
        String[] prefixes = {
            "my name is ", "i'm ", "im ", "i am ", "it's ", "its ", "call me ", "just "
        };
        for (String prefix : prefixes) {
            if (s.startsWith(prefix)) {
                s = s.substring(prefix.length()).trim();
                break;
            }
        }
        // Take only the first word (first name)
        String firstName = s.split("\\s+")[0];
        return capitalize(firstName);
    }

    /** Normalizes home type from free text: "I live in an apartment" → "Apartment". */
    private String extractHomeType(String raw) {
        String s = raw.toLowerCase();
        if (s.contains("apartment") || s.contains("apt"))  return "Apartment";
        if (s.contains("townhouse") || s.contains("townhome")) return "Townhouse";
        if (s.contains("condo"))                           return "Condo";
        if (s.contains("studio"))                          return "Studio";
        if (s.contains("house"))                           return "House";
        if (s.contains("duplex"))                          return "Duplex";
        // Fall back to capitalizing whatever they typed
        return capitalize(raw.trim());
    }

    /** Parses a number from digits or English words: "two", "3 bedrooms", "a couple" → int. */
    private int parseNumberWords(String s, int fallback) {
        String lower = s.toLowerCase().trim();
        if (lower.matches(".*\\bone\\b.*") || lower.startsWith("a ") || lower.equals("a")) return 1;
        if (lower.contains("two")   || lower.contains("couple"))  return 2;
        if (lower.contains("three"))                               return 3;
        if (lower.contains("four"))                                return 4;
        if (lower.contains("five"))                                return 5;
        if (lower.contains("six"))                                 return 6;
        // Fall back to stripping non-digits
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
