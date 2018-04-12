package com.robmcelhinney.PhoneBlock;

import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.assertNotNull;

public class InstalledAppsActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Rule
    public ActivityTestRule<InstalledAppsActivity> iActivityTestRule = new ActivityTestRule<>(InstalledAppsActivity.class);

    private MainActivity mActivity = null;

    private InstalledAppsActivity iActivity = null;

    Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(InstalledAppsActivity.class.getName(), null, false);

    @Before
    public void setUp() throws Exception {
        iActivity = iActivityTestRule.getActivity();
    }

    @Test
    public void testRememberSelections() {
        assertNotNull(iActivity.findViewById(R.id.listCheckBox));

    }

    @After
    public void tearDown() throws Exception {
        iActivity.finish();
    }

}