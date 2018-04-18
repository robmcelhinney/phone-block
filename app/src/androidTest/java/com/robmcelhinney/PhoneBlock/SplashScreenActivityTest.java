package com.robmcelhinney.PhoneBlock;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.widget.Button;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SplashScreenActivityTest {

    @Rule
    public ActivityTestRule<PermissionsSplashActivity> splashActivityTestRule = new ActivityTestRule<>(PermissionsSplashActivity.class);

    private PermissionsSplashActivity splashActivity = null;

    @Before
    public void setUp() throws Exception {
        splashActivity = splashActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch() throws InterruptedException {
        assertNotNull(splashActivity.findViewById(R.id.permissionsButton));
        Thread.sleep(2000);
    }

    @Test
    @UiThreadTest
    public void testLaunchSettingsOnButtonClick() {
        // Must remove permission before running this test.
        assertNotNull(splashActivity.findViewById(R.id.permissionsButton));
        Button button = splashActivity.findViewById(R.id.permissionsButton);
        button.performClick();
    }

    @After
    public void tearDown() throws Exception {
        splashActivity.finish();
        splashActivity = null;
    }

}
