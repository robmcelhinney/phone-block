package com.robmcelhinney.PhoneBlock;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.robmcelhinney.PhoneBlock.MainActivity.MY_PREFS_NAME;

public class InstalledAppsActivity extends AppCompatActivity {
    public static final String PACKAGE_NAME = "com.robmcelhinney.PhoneBlock";
    private List<ApplicationInfo> installedApps;
    private ArrayList<String> installedAppsNames;

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private Set<String> selectedAppsPackageName;

    private View progressOverlay;
    private ListView listView;
    private Context thisContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_installed_apps);

        progressOverlay = findViewById(R.id.progress_loading_overlay);

        settings = getApplicationContext().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();
        thisContext = this;
        selectedAppsPackageName = new HashSet<>();

        listView = findViewById(R.id.listViewID);
        new LoadApplications().execute();
    }

    private void userInstalledApps() {
        List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);

        installedApps = new ArrayList();
        installedAppsNames = new ArrayList();

        for(ApplicationInfo app : apps) {
            if ((app.flags & ApplicationInfo.FLAG_SYSTEM) == 0 && !((String) app.loadLabel(getApplicationContext().getPackageManager())).equalsIgnoreCase(PACKAGE_NAME)) {
                installedApps.add(app);
                installedAppsNames.add((String) app.loadLabel(getApplicationContext().getPackageManager()));
            }
        }

        Collections.sort(installedApps, new Comparator<ApplicationInfo>() {
            public int compare(ApplicationInfo v1, ApplicationInfo v2) {
                return ((String) v1.loadLabel(getApplicationContext().getPackageManager())).compareToIgnoreCase((String)v2.loadLabel(getApplicationContext().getPackageManager()));
            }
        });
        Collections.sort(installedAppsNames);
    }


    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        boolean checkState[];

        public MyListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
            layout = resource;
            checkState = new boolean[objects.size()];
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            selectedAppsPackageName = new HashSet<String>(settings.getStringSet("selectedAppsPackage", new HashSet<String>()));
            if(convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
                viewHolder.thumbnail = convertView.findViewById(R.id.listItemThumbnail);
                viewHolder.title = convertView.findViewById(R.id.listItemText);
                viewHolder.checkBox = convertView.findViewById(R.id.listCheckBox);

                viewHolder.title.setText(installedApps.get(position).loadLabel(getApplicationContext().getPackageManager()));
                viewHolder.thumbnail.setImageDrawable(installedApps.get(position).loadIcon(getApplicationContext().getPackageManager()));

                if(selectedAppsPackageName.contains(installedApps.get(position).packageName)) {
                    viewHolder.checkBox.setChecked(true);
                    checkState[position] = true;
                }
                else{
//                    viewHolder.checkBox.setChecked(checkState[position]);
                    viewHolder.checkBox.setChecked(false);
                    checkState[position] = false;
                }
                convertView.setTag(viewHolder);
            }
            else {
                viewHolder = (ViewHolder) convertView.getTag();
                viewHolder.title.setText(installedApps.get(position).loadLabel(getApplicationContext().getPackageManager()));
                viewHolder.thumbnail.setImageDrawable(installedApps.get(position).loadIcon(getApplicationContext().getPackageManager()));
                if(settings.getStringSet("selectedAppsPackage", new HashSet<String>()).contains(installedApps.get(position).packageName)) {
                    checkState[position] = true;
                    viewHolder.checkBox.setChecked(true);
                }
                else{
                    checkState[position] = false;
                    viewHolder.checkBox.setChecked(false);
                }
            }

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if(checkState[position]) {
                    selectedAppsPackageName.remove(installedApps.get(position).packageName);
                    editor.putStringSet("selectedAppsPackage", selectedAppsPackageName).apply();
//                    Toast.makeText(getContext(), "Button was clicked off for list item " + position, Toast.LENGTH_SHORT).show();
                }
                else {
                    selectedAppsPackageName.add(installedApps.get(position).packageName);
                    editor.putStringSet("selectedAppsPackage", selectedAppsPackageName).apply();
                    Log.d("InstalledClickedOff", installedApps.get(position).packageName + ". Size: " + selectedAppsPackageName.size());
//                    Toast.makeText(getContext(), "Button was clicked on for list item " + position + " : " + installedApps.get(position).packageName, Toast.LENGTH_SHORT).show();
                }
                checkState[position] = !checkState[position];
                notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    public class ViewHolder {
        ImageView thumbnail;
        TextView title;
        CheckBox checkBox;
    }

    @Override
    protected void onPause() {
        Iterator itr = selectedAppsPackageName.iterator();
        while(itr.hasNext()){
            Log.d("installedappsPause", itr.next()+",");
        }
        editor.clear();
        editor.putStringSet("selectedAppsPackage", selectedAppsPackageName);
        editor.commit();
        super.onPause();
    }

    public void animateView(final View view, final int toVisibility, float toAlpha, int duration) {
        boolean show = (toVisibility == View.VISIBLE);
        if (show) {
            view.setAlpha(0);
        }
        view.setVisibility(View.VISIBLE);
        view.animate()
            .setDuration(duration)
            .alpha(show ? toAlpha : 0)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(toVisibility);
                }
            });
    }


    private class LoadApplications extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            animateView(progressOverlay, View.VISIBLE, 1f, 200);

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            animateView(progressOverlay, View.GONE, 0, 200);
        }

        @Override
        protected Void doInBackground(Void... params) {
            userInstalledApps();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listView.setAdapter(new MyListAdapter(thisContext, R.layout.list_item, installedAppsNames));
                }
            });

            return null;
        }
    }
}