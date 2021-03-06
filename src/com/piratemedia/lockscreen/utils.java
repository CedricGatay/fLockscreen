package com.piratemedia.lockscreen;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class utils {

    private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static Bitmap mCachedBit = null;
    public static final String ACTION_LOCK = "com.piratemedia.lockscreen.ACTION_LOCK";
    public static final String ACTION_UNLOCK = "com.piratemedia.lockscreen.ACTION_UNLOCK";
    public static final String KEYGUARD_KEY = "keyguard";
    public static final String KEYGUARD_KEYWORD = "eliot_is_cool";
    public static final boolean DEBUG = true;

    /** Get album art for specified album. You should not pass in the album id
     * for the "unknown" album here (use -1 instead)
     */
    public static Bitmap getArtwork(Context context, long song_id, long album_id,
                                    boolean allowdefault) {

        if (album_id < 0) {
            // This is something that is not in the database, so get the album art directly
            // from the file.
            if (song_id >= 0) {
                Bitmap bm = getArtworkFromFile(context, song_id, -1);
                if (bm != null) {
                    return bm;
                }
            }
            if (allowdefault) {
                return getDefaultArtwork(context);
            }
            return null;
        }

        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        if (uri != null) {
            InputStream in = null;
            try {
                in = res.openInputStream(uri);
                return BitmapFactory.decodeStream(in, null, sBitmapOptions);
            } catch (FileNotFoundException ex) {
                // The album art thumbnail does not actually exist. Maybe the user deleted it, or
                // maybe it never existed to begin with.
                Bitmap bm = getArtworkFromFile(context, song_id, album_id);
                if (bm != null) {
                    if (bm.getConfig() == null) {
                        bm = bm.copy(Bitmap.Config.RGB_565, false);
                        if (bm == null && allowdefault) {
                            return getDefaultArtwork(context);
                        }
                    }
                } else if (allowdefault) {
                    bm = getDefaultArtwork(context);
                }
                return bm;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException ex) {
                }
            }
        }

        return null;
    }

    // get album art for specified file
    private static final String sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString();

    private static Bitmap getArtworkFromFile(Context context, long songid, long albumid) {
        Bitmap bm = null;
        byte[] art = null;
        String path = null;

        if (albumid < 0 && songid < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }

        try {
            if (albumid < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/" + songid + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            } else {
                Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileDescriptor fd = pfd.getFileDescriptor();
                    bm = BitmapFactory.decodeFileDescriptor(fd);
                }
            }
        } catch (FileNotFoundException ex) {
            //
        }
        if (bm != null) {
            mCachedBit = bm;
        }
        return bm;
    }

    private static Bitmap getDefaultArtwork(Context context) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeStream(
                context.getResources().openRawResource(R.drawable.albumart_mp_unknown), null, opts);
    }

    /**
     * wallpaper in background
     */
    static private class FastBitmapDrawable extends Drawable {
        private Bitmap mBitmap;
        private int mOpacity;

        private FastBitmapDrawable(Bitmap bitmap) {
            mBitmap = bitmap;
            mOpacity = mBitmap.hasAlpha() ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawBitmap(
                    mBitmap,
                    (getBounds().width() - mBitmap.getWidth()) / 2,
                    (getBounds().height() - mBitmap.getHeight()) / 2,
                    null);
        }

        @Override
        public int getOpacity() {
            return mOpacity;
        }

        @Override
        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getIntrinsicWidth() {
            return mBitmap.getWidth();
        }

        @Override
        public int getIntrinsicHeight() {
            return mBitmap.getHeight();
        }

        @Override
        public int getMinimumWidth() {
            return mBitmap.getWidth();
        }

        @Override
        public int getMinimumHeight() {
            return mBitmap.getHeight();
        }
    }

    //Get CheckBox Prefs

    static boolean getCheckBoxPref(Context context, String name, boolean def) {
        SharedPreferences prefs =
                context.getSharedPreferences("com.piratemedia.lockscreen_preferences", Context.MODE_PRIVATE);
        return prefs.getBoolean(name, def);
    }

    //Set CheckBox Prefs

    static void setCheckBoxPref(Context context, String name, boolean value) {
        SharedPreferences prefs =
                context.getSharedPreferences("com.piratemedia.lockscreen_preferences", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }

    //Get Int Prefs

    static int getIntPref(Context context, String name, int def) {
        SharedPreferences prefs =
                context.getSharedPreferences("com.piratemedia.lockscreen_preferences", Context.MODE_PRIVATE);
        return prefs.getInt(name, def);
    }

    //Get float Prefs

    static float getFloatPref(Context context, String name, float def) {
        SharedPreferences prefs =
                context.getSharedPreferences("com.piratemedia.lockscreen_preferences", Context.MODE_PRIVATE);
        return prefs.getFloat(name, def);
    }

    //Get String Prefs (ListPrefs etc.)

    static String getStringPref(Context context, String name, String def) {
        SharedPreferences prefs =
                context.getSharedPreferences("com.piratemedia.lockscreen_preferences", Context.MODE_PRIVATE);
        return prefs.getString(name, def);
    }

    //Store string prefs
    static void setStringPref(Context context, String name, String value) {
        SharedPreferences prefs =
                context.getSharedPreferences("com.piratemedia.lockscreen_preferences", Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(name, value);
        editor.commit();
    }

    //Lock screen intent
    static Intent getLockIntent(Context context) {
        Intent mMainLock = new Intent();
        mMainLock.setClass(context, mainActivity.class);
        mMainLock.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return mMainLock;
    }

}
