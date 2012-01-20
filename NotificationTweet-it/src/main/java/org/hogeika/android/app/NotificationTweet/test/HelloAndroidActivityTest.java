package org.hogeika.android.app.NotificationTweet.test;

import android.test.ActivityInstrumentationTestCase2;
import org.hogeika.android.app.NotificationTweet.*;

public class HelloAndroidActivityTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {

    public HelloAndroidActivityTest() {
        super("org.hogeika.android.app.NotificationTweet", HelloAndroidActivity.class);
    }

    public void testActivity() {
        HelloAndroidActivity activity = getActivity();
        assertNotNull(activity);
    }
}

