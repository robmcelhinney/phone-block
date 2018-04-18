package com.robmcelhinney.PhoneBlock;

import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class InstalledAppsActivityTest {

    @Rule
    public ActivityTestRule<InstalledAppsActivity> iActivityTestRule = new ActivityTestRule<>(InstalledAppsActivity.class);

    private InstalledAppsActivity iActivity = null;

    @Before
    public void setUp() throws Exception {
        iActivity = iActivityTestRule.getActivity();
    }

    @Test
    public void ensureListViewIsPresent() throws Exception {
        View viewById = iActivityTestRule.getActivity().findViewById(R.id.listViewID);
        assertNotNull(viewById);
        assertThat(viewById, instanceOf(ListView.class));
        ListAdapter adapter = ((ListView) viewById).getAdapter();
        assertThat(adapter, instanceOf(ArrayAdapter.class));
        assertThat(adapter.getCount(), greaterThan(2));
    }

    @After
    public void tearDown() throws Exception {
        iActivity.finish();
    }

}