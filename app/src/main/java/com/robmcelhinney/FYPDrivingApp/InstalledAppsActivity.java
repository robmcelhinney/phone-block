package com.robmcelhinney.FYPDrivingApp;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rob.FYPDrivingApp.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.robmcelhinney.FYPDrivingApp.MainActivity.MY_PREFS_NAME;

public class InstalledAppsActivity extends AppCompatActivity {
    private List<ApplicationInfo> installedApps;
    private ArrayList<String> installedAppsNames;

    private ArrayList<Integer> drawables = new ArrayList<>();

    private SharedPreferences settings;
    private SharedPreferences.Editor editor;

    private Set<String> selectedAppsPackageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_installed_apps);

        settings = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        editor = settings.edit();

        selectedAppsPackageName = new HashSet<>();

        ListView listView = (ListView) findViewById(R.id.listViewID);
        userInstalledApps();
        listView.setAdapter(new MyListAdapter(this, R.layout.list_item, installedAppsNames));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(InstalledAppsActivity.this, "List item was clicked at " + position, Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void userInstalledApps() {
        List<ApplicationInfo> apps = getPackageManager().getInstalledApplications(0);

        installedApps = new ArrayList();
        installedAppsNames = new ArrayList();

        for(ApplicationInfo app : apps) {
            //checks for flags; if flagged, check if updated system app
            if((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                installedApps.add(app);
                installedAppsNames.add((String) app.loadLabel(getApplicationContext().getPackageManager()));
                //it's a system app, not interested
            } else if ((app.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                //Discard this one
                //in this case, it should be a user-installed app
            } else {
                installedApps.add(app);
                installedAppsNames.add((String) app.loadLabel(getApplicationContext().getPackageManager()));
            }
        }
//        Collections.sort(installedAppsNames);
    }



    private class MyListAdapter extends ArrayAdapter<String> {
        private int layout;
        boolean checkState[];

        public MyListAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
            layout = resource;
            checkState = new boolean[objects.size()];
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder = new ViewHolder();
            selectedAppsPackageName = settings.getStringSet("selectedAppsPackage", new HashSet<String>());
            if(convertView == null) {
                LayoutInflater inflator = LayoutInflater.from(getContext());
                convertView = inflator.inflate(layout, parent, false);
                viewHolder.thumbnail = convertView.findViewById(R.id.listItemThumbnail);
                viewHolder.title = convertView.findViewById(R.id.listItemText);
                viewHolder.checkBox = convertView.findViewById(R.id.listCheckBox);

                if(selectedAppsPackageName.contains(installedApps.get(position).packageName)) {
                    viewHolder.checkBox.setChecked(true);
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
                viewHolder.title.setText(installedApps.get(position).loadLabel(getApplicationContext().getPackageManager()) + " " + position);
                viewHolder.thumbnail.setImageDrawable(installedApps.get(position).loadIcon(getApplicationContext().getPackageManager()));
//                if(settings.getStringSet("selectedAppsPackage", new HashSet<String>()).contains(installedApps.get(position).packageName)) {
//                    viewHolder.checkBox.setChecked(true);
//                }
            }

            viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if(checkState[position]) {
                    selectedAppsPackageName.remove(installedApps.get(position).packageName);
                    Toast.makeText(getContext(), "Button was clicked off for list item " + position, Toast.LENGTH_SHORT).show();
                }
                else {
                    selectedAppsPackageName.add(installedApps.get(position).packageName);
                    Toast.makeText(getContext(), "Button was clicked on for list item " + position + " : " + installedApps.get(position).packageName, Toast.LENGTH_SHORT).show();
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
        editor.putStringSet("selectedAppsPackage", selectedAppsPackageName).apply();
        super.onPause();
    }
}