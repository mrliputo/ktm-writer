package id.ac.unja.si.ktmwriter.com;

import android.util.Log;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by norman on 2/19/18.
 */

public class EncryptionLibrary {
    public static String encrypt(String input, String key){
        byte[] crypted;
        try{
            SecretKeySpec skey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skey);
            crypted = cipher.doFinal(input.getBytes());
        }catch(Exception e){
            return e.toString();
        }
        return clean(new String(Base64.encodeBase64(crypted)));
    }

    private static String clean(String str) {
        String from = "+/";
        String to = "-_";
        for (int i = 0; i < from.length(); i++) {
            str = str.replace(from.charAt(i), to.charAt(i));
        }
        return str.replace("=", "");
    }

}
