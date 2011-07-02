package com.piratemedia.lockscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class intentReceiver extends BroadcastReceiver {

    public static final String TAG = "IntentReceiver#ServiceState";
    public static final String ANDROID_INTENT_ACTION_SERVICE_STATE = "android.intent.action.SERVICE_STATE";

    @Override
    public void onReceive(Context aContext, Intent aIntent) {
        LockScreenApp.getInstance().startService(aContext, aIntent);
                handleDefaultKeyguard(aContext, aIntent);
    }

    private void handleDefaultKeyguard(final Context aContext, final Intent aIntent) {
        //exits if no intent involved
        if (aIntent == null || aIntent.getAction() == null) {
            return;
        }
        final String action = aIntent.getAction();
        // we can check service state, if we are not in state ready, we enable default system keyguard
        // to allow user to enter pin lock (managed by android internal lock)
        if (ANDROID_INTENT_ACTION_SERVICE_STATE.equals(action)) {
            TelephonyManager telMgr = (TelephonyManager) aContext.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();

            switch (simState) {
                case (TelephonyManager.SIM_STATE_READY): {
                    if (utils.DEBUG){
                        Log.v(TAG, "Sim is now ready, disabling system keyguard");
                    }
                    ManageKeyguard.disableSystemKeyguard(aContext);
                }
                break;
                default:
                    if (utils.DEBUG){
                        Log.v(TAG, "Sim is not ready, reenabling system keyguard");
                    }
                    ManageKeyguard.reenableSystemKeyguard(aContext);
            }
        }
    }
}
