package com.microsoft.azure.adal;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import com.microsoft.aad.adal.AuthenticationSettings;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by durgaprasad on 7/3/17.
 */

public class AdalUtils {


    /**
     * For API version lower than 18, you have to provide the secret key. The secret key
     * needs to be 256 bits. You can use the following way to generate the secret key. And
     * use AuthenticationSettings.Instance.setSecretKey(secretKeyBytes) to supply us the key.
     * For API version 18 and above, we use android keystore to generate keypair, and persist
     * the keypair in AnroidKeyStore. Current investigation shows 1)Keystore may be locked with
     * a lock screen, if calling app has a lot of background activity, keystore cannot be
     * accessed when locked, we'll be unable to decrypt the cache items 2) AndroidKeystore could
     * be reset when gesture to unlock the device is changed.
     * We do recommend the calling app the supply us the key with the above two limitations.
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     * @throws UnsupportedEncodingException
     */
    public static void SetupKey() throws NoSuchAlgorithmException,
            InvalidKeySpecException, UnsupportedEncodingException {
        if (AuthenticationSettings.INSTANCE.getSecretKeyData() == null) {
            // use same key for tests
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(Constants.SECRET_FACTORY_INSTANCE_ID);
            SecretKey tempkey = keyFactory.generateSecret(new PBEKeySpec(Constants.SECRET_KEY.toCharArray(), Constants.SALT.getBytes(Constants.UTF8_ENCODING), 100, 256));
            SecretKey secretKey = new SecretKeySpec(tempkey.getEncoded(), Constants.ALGORITHM_TYPE);
            AuthenticationSettings.INSTANCE.setSecretKey(secretKey.getEncoded());
        }
    }

    /**
     *
     * @param context application context
     */
    @SuppressWarnings("deprecation")
    public static void clearCookies(Context context)
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            Log.d(Constants.TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else
        {
            Log.d(Constants.TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
            CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(context);
            cookieSyncMngr.startSync();
            CookieManager cookieManager=CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
    }


}
