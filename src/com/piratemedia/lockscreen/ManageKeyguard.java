package com.piratemedia.lockscreen;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.KeyguardManager.OnKeyguardExitResult;
import android.content.Context;
import android.util.Log;

public class ManageKeyguard {
    private static KeyguardManager myKM = null;
    private static KeyguardLock myKL = null;
    private static final String LOGTAG = "LOCKSCREEN_KEYGUARDMANAGER";
    private static KeyguardLock sKL;

    public static synchronized void initialize(Context context) {
        if (myKM == null) {
            myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }

    public static synchronized void disableKeyguard(Context context) {
        // myKM = (KeyguardManager)
        // context.getSystemService(Context.KEYGUARD_SERVICE);
        initialize(context);

        if (myKM.inKeyguardRestrictedInputMode()) {
            myKL = myKM.newKeyguardLock(LOGTAG);
            myKL.disableKeyguard();
            if (utils.DEBUG) {
                Log.v(LOGTAG, "--Keyguard disabled");
            }
        } else {
            if (utils.DEBUG) {
                Log.v(LOGTAG, "--Keyguard not disabled....");
            }
            myKL = null;
        }
    }

    public static synchronized void disableSystemKeyguard(Context context) {
        initialize(context);
        acquireSystemKeyGuard();
        sKL.disableKeyguard();
        if (utils.DEBUG) {
            Log.v(LOGTAG, "--> System Keyguard disabled");
        }
        disableKeyguard(context);
    }

    private static void acquireSystemKeyGuard() {
        if (sKL == null) {
            sKL = myKM.newKeyguardLock(Context.KEYGUARD_SERVICE);
            if (utils.DEBUG) {
                Log.v(LOGTAG, "--> System keyguard acquired");
            }
        }
    }

    public static synchronized void reenableSystemKeyguard(Context context) {
        initialize(context);

        acquireSystemKeyGuard();
        sKL.reenableKeyguard();
        if (utils.DEBUG) {
            Log.v(LOGTAG, "--> System Keyguard disabled");
        }
        reenableKeyguard();
    }

    public static synchronized boolean inKeyguardRestrictedInputMode() {
        if (myKM != null) {
            if (utils.DEBUG) {
                Log.v(LOGTAG, "--inKeyguardRestrictedInputMode = " + myKM.inKeyguardRestrictedInputMode());
            }
            return myKM.inKeyguardRestrictedInputMode();
        }
        return false;
    }

    public static synchronized void reenableKeyguard() {
        if (myKM != null) {
            if (myKL != null) {
                myKL.reenableKeyguard();
                myKL = null;
                if (utils.DEBUG) {
                    Log.v(LOGTAG, "--Keyguard reenabled");
                }
            }
        }
    }

    public static synchronized void exitKeyguardSecurely(final LaunchOnKeyguardExit callback) {
        if (inKeyguardRestrictedInputMode()) {
            if (utils.DEBUG) {
                Log.v(LOGTAG, "--Trying to exit keyguard securely");
            }
            myKM.exitKeyguardSecurely(new OnKeyguardExitResult() {
                public void onKeyguardExitResult(boolean success) {
                    reenableKeyguard();
                    if (success) {
                        if (utils.DEBUG) {
                            Log.v(LOGTAG, "--Keyguard exited securely");
                        }
                        if (callback != null) {
                            callback.LaunchOnKeyguardExitSuccess();
                        }
                    } else {
                        if (utils.DEBUG) {
                            Log.v(LOGTAG, "--Keyguard exit failed");
                        }
                    }
                }
            });
        } else {
            if (callback != null) {
                callback.LaunchOnKeyguardExitSuccess();
            }
        }
    }

    public interface LaunchOnKeyguardExit {
        public void LaunchOnKeyguardExitSuccess();
    }
}
