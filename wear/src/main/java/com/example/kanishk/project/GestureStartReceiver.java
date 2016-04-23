package com.example.kanishk.project;

/**
 * Created by kanishk on 4/23/16.
 */

import watch.nudge.gesturelibrary.AppControllerReceiverService;

public class GestureLaunchReceiver extends AppControllerReceiverService {
    @Override
    protected Class getWatchActivityClass() {
        return MainActivity.class;
    }
}
