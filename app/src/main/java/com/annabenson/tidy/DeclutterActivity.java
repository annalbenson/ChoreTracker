package com.annabenson.tidy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeclutterActivity extends AppCompatActivity {

    private static class Task {
        final String emoji, title, desc;
        Task(String emoji, String title, String desc) {
            this.emoji = emoji; this.title = title; this.desc = desc;
        }
    }

    private static final Map<String, List<Task>> ROOM_TASKS = new HashMap<>();
    private static final List<Task> GENERIC_TASKS = new ArrayList<>();

    static {
        List<Task> kitchen = new ArrayList<>();
        kitchen.add(new Task("🫙", "Pantry shelf", "Check expiration dates. Donate unexpired food you won't use. Group by type."));
        kitchen.add(new Task("🍱", "Tupperware cabinet", "Match lids to containers. Anything without a match goes."));
        kitchen.add(new Task("🧲", "Junk drawer", "If you don't know what it is or haven't used it in a year, toss it."));
        kitchen.add(new Task("🫙", "Spice rack", "Smell-test each one. Anything faded or expired gets replaced or tossed."));
        kitchen.add(new Task("🧴", "Under-sink cabinet", "Toss empty or duplicate cleaning products. Consolidate what's left."));
        kitchen.add(new Task("🍳", "Rarely-used appliances", "If it hasn't been on the counter or used in 6 months, store it or donate it."));
        kitchen.add(new Task("📚", "Cookbooks", "Keep only the ones you actually open. Donate the rest."));
        kitchen.add(new Task("🛍️", "Reusable bags", "Sort the pile — keep what folds flat and fits. Recycle the rest."));
        kitchen.add(new Task("🧊", "Fridge door", "Toss expired condiments. Consolidate duplicates. Wipe down shelves."));
        kitchen.add(new Task("🥡", "Cabinet of mystery items", "One shelf at a time — if you can't remember buying it, you don't need it."));
        ROOM_TASKS.put("kitchen", kitchen);

        List<Task> bathroom = new ArrayList<>();
        bathroom.add(new Task("💊", "Medicine cabinet", "Check dates — discard expired meds properly. Group what remains."));
        bathroom.add(new Task("🧴", "Under-sink cabinet", "Toss empty bottles, ancient products, and anything you switched away from."));
        bathroom.add(new Task("💇", "Hair tools and products", "Keep only what you use weekly. Donate or toss the rest."));
        bathroom.add(new Task("💄", "Makeup or skincare drawer", "Anything older than a year or never opened — toss it."));
        bathroom.add(new Task("🪥", "Counter clutter", "Clear everything off. Only the daily-use items come back out."));
        bathroom.add(new Task("🛁", "Old towels", "Donate worn towels to an animal shelter. Only keep what you actually use."));
        bathroom.add(new Task("🧼", "Extra soap and shampoo", "Consolidate duplicates. Move backstock to one spot so you can see what you have."));
        bathroom.add(new Task("🪒", "Razor and grooming supplies", "Toss dull blades and anything you've stopped using."));
        ROOM_TASKS.put("bathroom", bathroom);

        List<Task> bedroom = new ArrayList<>();
        bedroom.add(new Task("👗", "One dresser drawer", "Pull it out, dump it, and only put back what you actually wear."));
        bedroom.add(new Task("🛏️", "Nightstand drawer", "Empty it completely. Toss old receipts, dead pens, and anything random."));
        bedroom.add(new Task("👟", "Closet floor", "Pair up shoes. Donate anything you haven't worn in a year."));
        bedroom.add(new Task("🧳", "Under the bed", "Pull everything out. Donate, toss, or store properly — no orphan items."));
        bedroom.add(new Task("📚", "Books on the nightstand", "Keep your current read. The others can go back to the shelf or be donated."));
        bedroom.add(new Task("💍", "Jewelry or accessories", "Untangle, match, and donate anything you never reach for."));
        bedroom.add(new Task("🧸", "Miscellaneous shelf items", "If it doesn't belong in a bedroom and you're not attached to it, rehome it."));
        bedroom.add(new Task("👔", "Clothes you haven't worn", "If it hasn't been on your body in a year, it goes."));
        ROOM_TASKS.put("bedroom", bedroom);

        List<Task> livingRoom = new ArrayList<>();
        livingRoom.add(new Task("📚", "Bookshelf", "Donate or recycle anything you've already read and won't revisit."));
        livingRoom.add(new Task("🛋️", "Couch cushions", "Pull everything out from behind and under them. Toss pillows you never use."));
        livingRoom.add(new Task("🎮", "Entertainment center", "Wrangle cables, toss dead remotes, and remove anything that drifted here."));
        livingRoom.add(new Task("🪴", "Shelves and surfaces", "Remove everything, dust, and only put back what you love or need."));
        livingRoom.add(new Task("🗞️", "Magazines and catalogs", "Keep the current issue of anything you actually read. Recycle the rest."));
        livingRoom.add(new Task("🎲", "Games and media", "Donate games you never play, DVDs you'll never watch again."));
        livingRoom.add(new Task("🧸", "Decorative items", "If it's just collecting dust and you're not attached, it goes."));
        livingRoom.add(new Task("🔌", "Cables and chargers", "Identify every cable. Toss anything with no matching device."));
        ROOM_TASKS.put("living room", livingRoom);

        List<Task> office = new ArrayList<>();
        office.add(new Task("🖥️", "Desk surface", "Clear everything off. Only essentials come back on."));
        office.add(new Task("🗂️", "Paper pile", "Sort: toss junk mail, shred old bills, file anything actually important."));
        office.add(new Task("🖊️", "Desk drawers", "Empty one drawer. Toss dead pens, mystery items, and old receipts."));
        office.add(new Task("💾", "Old electronics and cables", "Toss anything broken. Properly recycle old devices you're done with."));
        office.add(new Task("📚", "Books and binders", "Donate books you've finished. Toss binders with outdated content."));
        office.add(new Task("📌", "Sticky notes and reminders", "Clear old ones. Anything still relevant goes on a proper to-do list."));
        office.add(new Task("🖨️", "Printer area", "Toss scrap paper, empty cartridges, and anything that accumulated here."));
        office.add(new Task("💼", "Office supplies", "Consolidate duplicates. Toss dried-out pens and anything broken."));
        ROOM_TASKS.put("office", office);

        List<Task> entryway = new ArrayList<>();
        entryway.add(new Task("👟", "Shoes by the door", "Donate anything you haven't worn in a year. Keep only daily-use pairs here."));
        entryway.add(new Task("🧥", "Coats and jackets", "If it's not the right season and you're not attached to it, donate it."));
        entryway.add(new Task("🎒", "Bags and backpacks", "Empty one bag completely. Only put back what actually lives there."));
        entryway.add(new Task("📬", "Mail pile", "Toss junk mail, shred old statements, act on anything overdue."));
        entryway.add(new Task("🗝️", "Keys and hooks area", "Clear anything that isn't a key or daily-carry item."));
        entryway.add(new Task("🧤", "Gloves, hats, scarves", "If they're worn out or it's the wrong season, store or donate."));
        ROOM_TASKS.put("entryway", entryway);

        List<Task> closet = new ArrayList<>();
        closet.add(new Task("👗", "Clothes — tops", "If you haven't worn it in a year or it doesn't fit, it goes."));
        closet.add(new Task("👖", "Clothes — bottoms", "Same rule: unworn or ill-fitting items get donated."));
        closet.add(new Task("👟", "Shoes", "Pair everything up. Donate anything you consistently skip."));
        closet.add(new Task("👜", "Bags and purses", "Keep what you use. Donate the rest — they can go to a good home."));
        closet.add(new Task("🛏️", "Extra linens and bedding", "One set per bed plus one spare is plenty. Donate extras."));
        closet.add(new Task("📦", "Random stored items", "If it's been boxed for over a year without being opened, donate or toss."));
        closet.add(new Task("🧥", "Seasonal clothes", "Store off-season items in vacuum bags or bins so current-season things are easy to find."));
        ROOM_TASKS.put("closet", closet);

        List<Task> laundry = new ArrayList<>();
        laundry.add(new Task("🧴", "Old detergent bottles", "Toss empty or near-empty duplicates. Consolidate to one of each."));
        laundry.add(new Task("🧦", "Mismatched socks", "Give each sock 30 seconds to find its match. Orphans get tossed."));
        laundry.add(new Task("🧺", "Cleaning supplies stash", "Toss empty bottles. Organize what's left by type."));
        laundry.add(new Task("👔", "Clothes without a home", "Sort the pile of items that ended up here — return each to its proper place."));
        laundry.add(new Task("🪣", "Cleaning tools", "Inspect mop heads, scrub brushes, and sponges. Replace anything past its prime."));
        ROOM_TASKS.put("laundry room", laundry);

        List<Task> garage = new ArrayList<>();
        garage.add(new Task("🔧", "Tool corner", "Return borrowed tools. Toss anything broken beyond repair."));
        garage.add(new Task("📦", "Mystery boxes", "Open one. Donate or toss anything you forgot you had."));
        garage.add(new Task("🏋️", "Sports or fitness gear", "Donate gear for sports you no longer play."));
        garage.add(new Task("🧹", "Cleaning supplies", "Consolidate duplicates. Toss empty or drying-out products."));
        garage.add(new Task("🚗", "Car clutter", "Clear out the car — trash, forgotten items, things that live elsewhere."));
        garage.add(new Task("🌱", "Garden tools and supplies", "Toss dead plants, dry soil bags, broken tools."));
        ROOM_TASKS.put("garage", garage);

        // Generic fallback pool
        GENERIC_TASKS.add(new Task("🚪", "Entryway clutter", "Grab everything that doesn't belong near the door and put it in its proper home."));
        GENERIC_TASKS.add(new Task("🛋️", "Couch cushions", "Pull out anything hiding in the cushions. Toss pillows you never use."));
        GENERIC_TASKS.add(new Task("📚", "A bookshelf or stack", "Donate or recycle anything you've already read and won't revisit."));
        GENERIC_TASKS.add(new Task("🗂️", "Paper pile", "Toss junk mail, shred old bills, file anything important."));
        GENERIC_TASKS.add(new Task("👗", "One drawer of clothes", "Pull it out, dump it, and only put back what you actually wear."));
        GENERIC_TASKS.add(new Task("🧴", "Bathroom cabinet or counter", "Toss anything expired, empty, or untouched in 6 months."));
        GENERIC_TASKS.add(new Task("🍱", "Tupperware cabinet", "Match lids to containers. Anything without a match goes."));
        GENERIC_TASKS.add(new Task("🧲", "Junk drawer", "If you don't know what it is or haven't used it in a year, toss it."));
        GENERIC_TASKS.add(new Task("🧳", "Under the bed", "Pull everything out. Donate, toss, or store properly."));
        GENERIC_TASKS.add(new Task("🪴", "A windowsill or shelf", "Remove everything, dust, and only put back what you love or need."));
        GENERIC_TASKS.add(new Task("🖥️", "Desk surface", "Clear everything off. Only essentials come back on."));
        GENERIC_TASKS.add(new Task("🎮", "Entertainment center", "Wrangle cables, toss dead remotes, remove anything that drifted here."));
        GENERIC_TASKS.add(new Task("🧺", "Laundry area", "Put away clean laundry, sort any piles, and clear the floor."));
        GENERIC_TASKS.add(new Task("🫙", "Pantry shelf", "Check expiration dates. Donate unexpired food you won't use."));
        GENERIC_TASKS.add(new Task("👟", "Shoe rack or closet floor", "Donate anything unworn in a year. Pair everything up."));
        GENERIC_TASKS.add(new Task("🎒", "Bags and backpacks", "Empty one bag completely. Only put back what actually lives there."));
        GENERIC_TASKS.add(new Task("💊", "Medicine cabinet", "Check dates — discard expired meds properly."));
        GENERIC_TASKS.add(new Task("🧸", "Decorative items", "If it's just collecting dust and you're not attached, it goes."));
        GENERIC_TASKS.add(new Task("🔌", "Cables and chargers", "Identify every cable. Toss anything with no matching device."));
        GENERIC_TASKS.add(new Task("🧹", "Cleaning supplies", "Toss empty bottles. Organize what's left."));
    }

    private List<Task> tasks;
    private int currentIndex = 0;
    private int doneCount = 0;

    private CardView taskCard;
    private LinearLayout buttonRow;
    private LinearLayout completionView;
    private TextView tvTitle;
    private TextView tvProgress;
    private TextView tvTillyPrompt;
    private TextView tvTaskEmoji;
    private TextView tvTaskTitle;
    private TextView tvTaskDesc;
    private TextView tvCompletionTitle;
    private TextView tvCompletionSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declutter);

        tvTitle        = findViewById(R.id.tvTitle);
        tvProgress     = findViewById(R.id.tvProgress);
        tvTillyPrompt  = findViewById(R.id.tvTillyPrompt);
        taskCard       = findViewById(R.id.taskCard);
        buttonRow      = findViewById(R.id.buttonRow);
        completionView = findViewById(R.id.completionView);
        tvTaskEmoji    = findViewById(R.id.tvTaskEmoji);
        tvTaskTitle    = findViewById(R.id.tvTaskTitle);
        tvTaskDesc     = findViewById(R.id.tvTaskDesc);
        tvCompletionTitle = findViewById(R.id.tvCompletionTitle);
        tvCompletionSub   = findViewById(R.id.tvCompletionSub);

        ImageButton backButton   = findViewById(R.id.backButton);
        MaterialButton btnDone   = findViewById(R.id.btnDone);
        MaterialButton btnSkip   = findViewById(R.id.btnSkip);
        MaterialButton btnFinish = findViewById(R.id.btnFinish);

        backButton.setOnClickListener(v -> finish());
        btnFinish.setOnClickListener(v -> finish());
        btnDone.setOnClickListener(v -> { doneCount++; advance(); });
        btnSkip.setOnClickListener(v -> advance());

        String room = getIntent().getStringExtra("room");
        buildTaskList(room);
        applyRoomHeader(room);
        showTask();
    }

    private void buildTaskList(String room) {
        List<Task> pool = (room != null && ROOM_TASKS.containsKey(room))
                ? ROOM_TASKS.get(room)
                : GENERIC_TASKS;
        tasks = new ArrayList<>(pool);
        Collections.shuffle(tasks);
        tasks = tasks.subList(0, Math.min(5, tasks.size()));
    }

    private void applyRoomHeader(String room) {
        if (room != null) {
            String label = capitalize(room);
            tvTitle.setText("Declutter: " + label);
            tvTillyPrompt.setText("Let's tackle the " + room + " together. No pressure — just decide!");
        }
    }

    private void advance() {
        currentIndex++;
        if (currentIndex >= tasks.size()) showCompletion();
        else showTask();
    }

    private void showTask() {
        Task task = tasks.get(currentIndex);
        tvProgress.setText((currentIndex + 1) + " of " + tasks.size());
        tvTaskEmoji.setText(task.emoji);
        tvTaskTitle.setText(task.title);
        tvTaskDesc.setText(task.desc);
    }

    private void showCompletion() {
        taskCard.setVisibility(View.GONE);
        buttonRow.setVisibility(View.GONE);
        completionView.setVisibility(View.VISIBLE);
        tvProgress.setText("");

        if (doneCount == tasks.size()) {
            tvCompletionTitle.setText("You crushed it! 🌿");
            tvCompletionSub.setText("Every single task — done. Your space is already lighter.");
        } else if (doneCount >= tasks.size() / 2) {
            tvCompletionTitle.setText("Great session! 🌿");
            tvCompletionSub.setText("You tackled " + doneCount + " out of " + tasks.size() + " spots. That's real progress.");
        } else {
            tvCompletionTitle.setText("Good start! 🌿");
            tvCompletionSub.setText("You cleared " + doneCount + " spot" + (doneCount == 1 ? "" : "s") + ". Every bit counts — come back anytime.");
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}
