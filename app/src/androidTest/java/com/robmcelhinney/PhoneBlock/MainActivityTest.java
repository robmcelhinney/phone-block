package com.robmcelhinney.PhoneBlock;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.rule.ActivityTestRule;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    private MainActivity mActivity = null;

    private Instrumentation.ActivityMonitor monitor = getInstrumentation().addMonitor(InstalledAppsActivity.class.getName(), null, false);

    @Before
    public void setUp() throws Exception {
        mActivity = mActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch() {
        View view = mActivity.findViewById(R.id.toggleButtonActive);

        assertNotNull(view);
    }

    @Test
    public void testLaunchInstalledAppsActivityOnButtonClick() {
        assertNotNull(mActivity.findViewById(R.id.appsButton));

        onView(withId(R.id.appsButton)).perform(click());

        Activity installedAppsActivity = getInstrumentation().waitForMonitorWithTimeout(monitor, 5000);

        assertNotNull(installedAppsActivity);

        installedAppsActivity.finish();
    }

    @Test
    public void testStartBlock() throws InterruptedException {
        assertNotNull(mActivity.findViewById(R.id.toggleButtonActive));

        onView(withId(R.id.toggleButtonActive)).perform(click());

        Thread.sleep(2000);

        assertTrue(UtilitiesService.isActive());
    }

    @After
    public void tearDown() throws Exception {
        mActivity = null;
    }

}