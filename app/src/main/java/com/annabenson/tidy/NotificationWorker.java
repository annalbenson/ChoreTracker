package com.annabenson.tidy;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.ArrayList;
import java.util.List;

public class NotificationWorker extends Worker {

    private static final String CHANNEL_ID = "tidy_chores";

    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        DatabaseHandler db = new DatabaseHandler(getApplicationContext());
        List<Chore> all = db.loadChores();

        List<String> dueNames = new ArrayList<>();
        for (Chore c : all) {
            if (c.isOverdue() || c.isDueToday()) dueNames.add(c.getName());
        }

        if (dueNames.isEmpty()) return Result.success();

        String title = dueNames.size() == 1
                ? dueNames.get(0) + " needs attention"
                : dueNames.size() + " chores need attention";
        String body = dueNames.size() <= 3
                ? TextUtils.join(", ", dueNames)
                : TextUtils.join(", ", dueNames.subList(0, 3)) + " and more";

        createChannel();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, builder.build());

        return Result.success();
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Chore Reminders", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Daily reminders for chores due today or overdue");
            NotificationManager nm = (NotificationManager)
                    getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }
}
