package org.hogeika.android.app.NotificationTweet.test;

import android.test.ActivityInstrumentationTestCase2;
import org.hogeika.android.app.NotificationTweet.*;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super("org.hogeika.android.app.NotificationTweet", MainActivity.class);
    }

    public void testActivity() {
        MainActivity activity = getActivity();
        assertNotNull(activity);
    }
}

