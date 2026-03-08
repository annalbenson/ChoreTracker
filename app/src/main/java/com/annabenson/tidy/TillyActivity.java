package com.annabenson.tidy;

import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TillyActivity extends AppCompatActivity {

    private static final boolean TEST_MODE = BuildConfig.DEBUG;

    private static final String SYSTEM_PROMPT =
            "You are Tilly, a warm and practical home cleaning assistant. " +
            "Help with cleaning advice, stain removal, routines, and household tips. " +
            "Keep responses friendly, concise, and actionable. " +
            "If asked something unrelated to home care or cleaning, gently redirect.";

    private RecyclerView chatRecycler;
    private TextInputEditText messageInput;
    private OnboardingAdapter adapter; // reuse bubble UI
    private final List<GeminiRequest.Content> history = new ArrayList<>();
    private boolean waiting = false;
    private int userId;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilly);

        userId = getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
                .getInt(Prefs.KEY_USER_ID, -1);
        db = new DatabaseHandler(this);

        chatRecycler = findViewById(R.id.chatRecycler);
        messageInput = findViewById(R.id.messageInput);
        ImageButton sendButton = findViewById(R.id.sendButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        chatRecycler.setLayoutManager(lm);

        adapter = new OnboardingAdapter();
        chatRecycler.setAdapter(adapter);

        String name = userId != -1 ? db.getUserName(userId) : null;
        String greeting = (name == null || name.isEmpty())
                ? "Hi! I'm Tilly 🌿 Ask me anything about cleaning, stains, or routines!"
                : "Hi " + name + "! 🌿 Ask me anything — stain removal, cleaning routines, whatever you need.";
        addTillyMessage(greeting);

        sendButton.setOnClickListener(v -> sendMessage());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) { sendMessage(); return true; }
            return false;
        });
    }

    private void sendMessage() {
        if (waiting) return;
        String text = messageInput.getText() != null
                ? messageInput.getText().toString().trim() : "";
        if (text.isEmpty()) return;
        messageInput.setText("");

        addUserMessage(text);

        if (isReonboardRequest(text)) {
            handleReonboard();
            return;
        }

        if (isQuickChoreRequest(text)) {
            handleQuickChore();
            return;
        }

        if (isDailyPlanRequest(text)) {
            handleDailyPlan();
            return;
        }

        if (isDeclutterRequest(text)) {
            handleDeclutter(text);
            return;
        }

        // Show loading indicator
        addTillyMessage("…");
        int loadingIndex = adapter.getItemCount() - 1;

        waiting = true;

        if (TEST_MODE) {
            String reply = getTestResponse(text);
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                waiting = false;
                adapter.replaceMessage(loadingIndex,
                        new OnboardingMessage(OnboardingMessage.Type.TILLY, reply));
                scrollToBottom();
            }, 800);
            return;
        }

        history.add(new GeminiRequest.Content("user",
                Collections.singletonList(new GeminiRequest.Part(text))));
        GeminiRequest request = new GeminiRequest(SYSTEM_PROMPT, new ArrayList<>(history));

        GeminiClient.get().generate(BuildConfig.GEMINI_KEY, request)
                .enqueue(new Callback<GeminiResponse>() {
                    @Override
                    public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                        waiting = false;
                        String reply = response.isSuccessful() && response.body() != null
                                ? response.body().getText() : null;
                        if (reply == null) reply = "Sorry, I couldn't get a response. Try again?";
                        history.add(new GeminiRequest.Content("model",
                                Collections.singletonList(new GeminiRequest.Part(reply))));
                        final String finalReply = reply;
                        runOnUiThread(() -> {
                            adapter.replaceMessage(loadingIndex,
                                    new OnboardingMessage(OnboardingMessage.Type.TILLY, finalReply));
                            scrollToBottom();
                        });
                    }

                    @Override
                    public void onFailure(Call<GeminiResponse> call, Throwable t) {
                        waiting = false;
                        Log.e("Tilly", "Chat failed", t);
                        runOnUiThread(() -> {
                            adapter.replaceMessage(loadingIndex,
                                    new OnboardingMessage(OnboardingMessage.Type.TILLY,
                                            "Network error — check your connection and try again."));
                            scrollToBottom();
                        });
                    }
                });
    }

    private boolean isReonboardRequest(String input) {
        String s = input.toLowerCase();
        return s.contains("re-onboard") || s.contains("reonboard") ||
               s.contains("start over") || s.contains("start again") ||
               s.contains("redo setup") || s.contains("redo my profile") ||
               s.contains("reset my profile") || s.contains("reset everything") ||
               s.contains("new profile") || s.contains("update my home") ||
               (s.contains("reset") && s.contains("chore"));
    }

    private void handleReonboard() {
        addTillyMessage("Sure! I'll clear your chores and profile and we'll start fresh. See you in a moment 🌿");
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (userId != -1) db.resetAll(userId);
            Intent intent = new Intent(this, OnboardingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }, 1500);
    }

    private static final String[] QUICK_CHORES = {
        "Wipe down the microwave inside and out",
        "Clean the bathroom mirror and sink",
        "Empty and wipe out the trash can",
        "Wipe down the stovetop",
        "Vacuum one room",
        "Sweep the kitchen floor",
        "Clean the toilet bowl",
        "Wipe down light switches and doorknobs",
        "Fold and put away one load of laundry",
        "Wipe down the kitchen counters",
        "Clear and wipe the bathroom counter",
        "Take out all the trash and recycling",
        "Wipe fingerprints off cabinet fronts",
        "Spot-clean the bathroom floor",
        "Dust one shelf or surface",
    };

    private boolean isQuickChoreRequest(String s) {
        s = s.toLowerCase();
        return (s.contains("5 minute") || s.contains("five minute") ||
                s.contains("quick chore") || s.contains("quick task") ||
                s.contains("something quick") || s.contains("fast chore") ||
                s.contains("what can i do") || s.contains("what should i do now"));
    }

    private void handleQuickChore() {
        String chore = QUICK_CHORES[(int)(Math.random() * QUICK_CHORES.length)];
        addTillyMessage("Here's a great 5-minute task:\n\n✅ " + chore +
                "\n\nThat's it! Small wins add up. Want another one?");
    }

    private boolean isDailyPlanRequest(String s) {
        s = s.toLowerCase();
        return s.contains("plan for today") || s.contains("cleaning plan") ||
               s.contains("what should i clean today") || s.contains("today's chores") ||
               s.contains("help me plan") || s.contains("where do i start") ||
               s.contains("cleaning schedule");
    }

    private void handleDailyPlan() {
        HomeProfile profile = userId != -1 ? db.loadProfile(userId) : null;
        String style = profile != null && profile.cleaningStyle != null
                ? profile.cleaningStyle.toLowerCase() : "";

        String quick = "• Wipe down kitchen counters\n• Quick bathroom wipe (sink + toilet)\n• Sweep or vacuum main living area";
        String medium = "• Dishes and kitchen wipe-down\n• Vacuum all rooms\n• Clean one bathroom fully\n• Do a load of laundry";
        String full   = "• Full kitchen clean (counters, stovetop, sink)\n• Mop floors after vacuuming\n• Clean all bathrooms\n• Change bed sheets\n• Do laundry start to finish";

        String plan;
        String opener;
        if (style.contains("top of it") || style.contains("weekly sweep")) {
            opener = "You're already in a good groove — here's a focused plan for today:";
            plan = medium;
        } else if (style.contains("chaos") || style.contains("as-needed")) {
            opener = "No judgment! Let's start manageable and build momentum:";
            plan = quick;
        } else {
            opener = "Here's a solid plan for today:";
            plan = medium;
        }

        addTillyMessage(opener + "\n\n" + plan +
                "\n\nTackle them in order and take a break between each. You've got this 🌿");
    }

    private boolean isDeclutterRequest(String s) {
        s = s.toLowerCase();
        return s.contains("declutter") || s.contains("de-clutter") ||
               s.contains("clutter") || s.contains("clear out") ||
               s.contains("get rid of") || s.contains("purge");
    }

    private void handleDeclutter(String text) {
        String room = extractRoom(text);
        String msg = room != null
                ? "Let's tackle the " + room + "! I've got tasks ready for you 🗂️"
                : "Ooh, declutter mode! I've got a whole activity for that 🗂️";
        addTillyMessage(msg);
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, DeclutterActivity.class);
            if (room != null) intent.putExtra("room", room);
            startActivity(intent);
        }, 800);
    }

    private String extractRoom(String text) {
        String s = text.toLowerCase();
        if (s.contains("kitchen"))                                    return "kitchen";
        if (s.contains("bathroom") || s.contains("bath"))            return "bathroom";
        if (s.contains("bedroom") || s.contains("master"))           return "bedroom";
        if (s.contains("living room") || s.contains("lounge") ||
            s.contains("family room") || s.contains("den"))          return "living room";
        if (s.contains("office") || s.contains("study") ||
            s.contains("home office"))                                return "office";
        if (s.contains("entryway") || s.contains("entry") ||
            s.contains("hallway") || s.contains("foyer") ||
            s.contains("hall"))                                       return "entryway";
        if (s.contains("closet") || s.contains("wardrobe"))          return "closet";
        if (s.contains("laundry"))                                    return "laundry room";
        if (s.contains("garage"))                                     return "garage";
        return null;
    }

    private String getTestResponse(String input) {
        String s = input.toLowerCase();
        if (s.contains("stain") && (s.contains("red wine") || s.contains("wine")))
            return "For red wine: blot immediately — don't rub! Pour cold water or club soda, then apply a mix of dish soap and hydrogen peroxide. Let it sit 5 minutes, then blot clean. Works on carpet and fabric.";
        if (s.contains("stain") && s.contains("grease"))
            return "Grease stains: sprinkle baking soda or cornstarch immediately to absorb the oil. Let sit 10 minutes, brush off, then apply dish soap directly and work it in before washing. Hot water sets grease — always use cold first!";
        if (s.contains("stain"))
            return "For most stains: act fast and blot (don't rub). Cold water first, then a little dish soap or white vinegar. What's the stain and surface? I can give you something more specific!";
        if (s.contains("bathroom") || s.contains("toilet") || s.contains("shower"))
            return "For a quick bathroom refresh: spray surfaces with a vinegar-water mix (50/50), let sit 2 minutes, then wipe down. For the toilet, a little baking soda in the bowl + scrub takes 2 minutes. Doing it weekly keeps it from building up.";
        if (s.contains("kitchen") || s.contains("oven") || s.contains("fridge"))
            return "Kitchen tip: wipe down counters and stovetop after every use — it takes 30 seconds and saves you a big scrub later. For a grimy oven, baking soda paste + white vinegar overnight does the heavy lifting without harsh chemicals.";
        if (s.contains("routine") || s.contains("schedule") || s.contains("habit"))
            return "A solid minimal routine: daily — dishes, wipe kitchen, quick tidy. Weekly — vacuum, mop, bathrooms, laundry. Monthly — deeper clean (oven, fridge, baseboards). Doing a little every day is way easier than one big weekend clean.";
        if (s.contains("smell") || s.contains("odor") || s.contains("musty"))
            return "For mystery odors: baking soda is your best friend — a bowl in the fridge, sprinkled on carpet before vacuuming, or in shoes overnight. White vinegar in a bowl left out for a few hours neutralizes airborne smells. What room are we dealing with?";
        if (s.contains("mold") || s.contains("mildew"))
            return "For mold/mildew: mix 1 part white vinegar with 1 part water in a spray bottle. Spray, let sit 1 hour, then scrub and rinse. For grout, a baking soda paste + vinegar spray works well. Keep the area ventilated to prevent it coming back.";
        return "Great question! In test mode I only have scripted responses for common cleaning topics — stains, bathrooms, kitchens, routines, odors. Once billing is active, I can answer anything. What else can I help with?";
    }

    private void addTillyMessage(String text) {
        adapter.addMessage(new OnboardingMessage(OnboardingMessage.Type.TILLY, text));
        scrollToBottom();
    }

    private void addUserMessage(String text) {
        adapter.addMessage(new OnboardingMessage(OnboardingMessage.Type.USER, text));
        scrollToBottom();
    }

    private void scrollToBottom() {
        chatRecycler.post(() -> chatRecycler.smoothScrollToPosition(adapter.getItemCount() - 1));
    }
}
