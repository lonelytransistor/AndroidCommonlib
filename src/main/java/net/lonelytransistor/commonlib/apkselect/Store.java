package net.lonelytransistor.commonlib.apkselect;


import android.app.NotificationChannel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.widget.Filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class Store extends Filter {
    private static final String TAG = "Store";
    public enum SortOrder {
        NONE,
        ALPHABETIC,
        ALPHABETIC_INVERSE
    }

    public interface Callback {
        void onStarted(Store apkStore);
        void onProgress(float p);
        void onFinished(Store apkStore);
    }
    public static class NotificationGroup {
        public final String name;
        public final String id;
        boolean monitored;
        final Map<String,Serializable> extra;
        NotificationGroup(String id, String name, boolean monitored, Map<String,Serializable> extra) {
            this.id = id;
            this.name = name;
            this.monitored = monitored;
            this.extra = extra;
        }
    }
    public class ApkInfo {
        public final String pkgName;
        public final Drawable icon;
        public final String label;

        public final List<NotificationGroup> notificationGroups = new ArrayList<>();
        public Map<String,Serializable> extra = new HashMap<>();
        public Map<String,Serializable> catExtra = new HashMap<>();
        ApkInfo(ResolveInfo info) {
            pkgName = info.activityInfo.packageName;
            label = info.activityInfo.loadLabel(packageManager).toString();
            icon = info.activityInfo.loadIcon(packageManager);
        }

        private boolean update(Map<String, Map<String,Serializable>> monitoredChannels, Map<String,Serializable> extras, Map<String,Serializable> catExtras) {
            List<NotificationChannel> allChannels = getNotificationChannels(pkgName);
            notificationGroups.clear();
            if (allChannels != null) {
                for (NotificationChannel channel : allChannels) {
                    String id = channel.getId();
                    notificationGroups.add(new NotificationGroup(
                            channel.getId(),
                            channel.getName().toString(),
                            monitoredChannels.containsKey(id),
                            monitoredChannels.getOrDefault(id, new HashMap<>(catExtras))));
                }
            }
            extra = extras;
            catExtra = catExtras;
            return notificationGroups.size() > 0;
        }
    }

    private final ArrayList<ApkInfo> infos = new ArrayList<>();
    private final ArrayList<String> names = new ArrayList<>();
    private final ArrayList<String> labels = new ArrayList<>();
    private final ArrayList<Integer> filteredIndices = new ArrayList<>();
    private final ArrayList<Integer> enabledIndices = new ArrayList<>();
    private final ArrayList<Integer> disabledIndices = new ArrayList<>();

    private String filterString = "";
    private SortOrder sortOrder = SortOrder.NONE;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final PackageManager packageManager;
    public Store(Context ctx, Callback progressCb) {
        packageManager = ctx.getPackageManager();
        load(progressCb);
    }
    public static class Data implements Serializable {
        public final Map<String, Map<String,Serializable>> categories;
        public final Map<String,Serializable> extra;
        public Map<String, Serializable> catExtra;

        public Data() {
            categories = new HashMap<>();
            extra = new HashMap<>();
            catExtra = new HashMap<>();
        }
        public Data(Map<String, Map<String,Serializable>> categories, Map<String,Serializable> extra, Map<String,Serializable> catExtra) {
            this.categories = categories;
            this.extra = extra;
            this.catExtra = catExtra;
        }
        @Override
        public String toString() {
            return "Data{" +
                    "categories=" + categories +
                    ", extra=" + extra +
                    ", catExtra=" + catExtra +
                    '}';
        }
    }
    public void load(Callback progressCb) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        List<ResolveInfo> allApps = packageManager.queryIntentActivities(intent, 0);

        executor.execute(() -> {
            int progress = 0;
            for (ResolveInfo info : allApps) {
                progressCb.onProgress(((float) progress++) / ((float) allApps.size()));
                ApkInfo app = new ApkInfo(info);
                Data data = load(app);
                if (app.update(data.categories, data.extra, data.catExtra)) {
                    infos.add(app);
                    names.add(app.pkgName);
                    labels.add(app.label);
                }
                progressCb.onProgress(1.0f);
            }
        });
    }
    boolean isEmpty() {
        return infos.isEmpty();
    }
    protected abstract Data load(ApkInfo info);
    public abstract void save(ApkInfo info, Data data);
    public abstract void save(Set<String> monitoredPackages);
    protected abstract List<NotificationChannel> getNotificationChannels(String pkgName);

    public void save() {
        if (isEmpty())
            return;

        Set<String> pkgNames = new HashSet<>();
        for (ApkInfo app : infos) {
            Map<String, Map<String,Serializable>> monitoredGroups = new HashMap<>();
            for (NotificationGroup group : app.notificationGroups) {
                if (group.monitored) {
                    monitoredGroups.put(group.id, group.extra);
                }
            }
            if (!monitoredGroups.isEmpty()) {
                Data data = new Data(monitoredGroups, app.extra, app.catExtra);
                save(app, data);
                pkgNames.add(app.pkgName);
            }
        }
        save(pkgNames);
    }


    public int size() {
        return filteredIndices.size();
    }
    public int size(boolean s) {
        return s ? enabledIndices.size() : disabledIndices.size();
    }
    public int getId(int ix) {
        return filteredIndices.get(ix);
    }
    public ApkInfo getByIndex(int ix) {
        return infos.get(filteredIndices.get(ix));
    }
    public ApkInfo getByLabel(String label) {
        return infos.get(labels.indexOf(label));
    }
    public ApkInfo getByName(String name) {
        return infos.get(names.indexOf(name));
    }
    public boolean hasName(String name) {
        return names.indexOf(name) > 0;
    }
    public void setAllStates(boolean s, FilterListener cb) {
        for (ApkInfo app : infos) {
            for (NotificationGroup notif : app.notificationGroups) {
                notif.monitored = s;
            }
        }
        filter(filterString, cb);
    }
    public void invertAllStates(FilterListener cb) {
        for (ApkInfo app : infos) {
            for (NotificationGroup notif : app.notificationGroups) {
                notif.monitored = !notif.monitored;
            }
        }
        filter(filterString, cb);
    }
    public void sort(SortOrder s, FilterListener cb) {
        sortOrder = s;
        filter(filterString, cb);
    }
    public SortOrder getSort() {
        return sortOrder;
    }


    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        ArrayList<Integer> indices = new ArrayList<>();

        filterString = constraint.toString().toLowerCase();
        enabledIndices.clear();
        disabledIndices.clear();
        for (int ix = 0; ix < labels.size(); ix++) {
            if (labels.get(ix).toLowerCase().contains(filterString)) {
                boolean monitored = false;
                for (NotificationGroup notif : infos.get(ix).notificationGroups) {
                    if (notif.monitored) {
                        monitored = true;
                        break;
                    }
                }
                if (monitored) {
                    enabledIndices.add(ix);
                } else {
                    disabledIndices.add(ix);
                }
                indices.add(ix);
            }
        }
        switch (sortOrder) {
            case ALPHABETIC_INVERSE:
                enabledIndices.sort((l, r) -> -labels.get(l).compareToIgnoreCase(
                        labels.get(r)));
                disabledIndices.sort((l, r) -> -labels.get(l).compareToIgnoreCase(
                        labels.get(r)));
                break;
            case ALPHABETIC:
                enabledIndices.sort((l, r) -> labels.get(l).compareToIgnoreCase(
                        labels.get(r)));
                disabledIndices.sort((l, r) -> labels.get(l).compareToIgnoreCase(
                        labels.get(r)));
                break;
            case NONE:
            default:
                break;
        }
        if (sortOrder != SortOrder.NONE) {
            indices.clear();
            indices.addAll(enabledIndices);
            indices.addAll(disabledIndices);
        }
        results.values = indices;
        results.count = indices.size();
        return results;
    }
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        filteredIndices.clear();
        filteredIndices.addAll((ArrayList<Integer>) results.values);
    }
}
