package id.ac.unja.si.ktmwriter.act;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import id.ac.unja.si.ktmwriter.com.EncryptionLibrary;
import id.ac.unja.si.ktmwriter.R;

public class MainActivity extends AppCompatActivity {
    NfcAdapter nfcAdapter;
    EditText nimInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writer);

        nimInput = findViewById(R.id.nimInput);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    // This method is executed every time a new card is detected
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String nim = nimInput.getText().toString().toUpperCase();
            NdefMessage ndefMessage = createNdefMessage(EncryptionLibrary.encrypt(nim, "1234567890123456"));
            writeNdefMessage(tag, ndefMessage);
        }
    }


    /* NFC HANDLING METHODS */

    // Listener
    private void enableFDS() {
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters,null);
    }

    private void disableFDS() {
        nfcAdapter.disableForegroundDispatch(this);
    }

    // Content management
    private void formatTag(Tag tag, NdefMessage ndefMessage) {
        try {
            NdefFormatable ndefFormatable = NdefFormatable.get(tag);
            if(ndefFormatable == null) {
                Toast.makeText(this, "KTM tidak didukung!", Toast.LENGTH_SHORT).show();
                ndefFormatable.connect();
                ndefFormatable.format(ndefMessage);
                ndefFormatable.close();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    private NdefMessage createNdefMessage(String token) {
        NdefRecord ndefRecord = createTextRecord(token);
        return new NdefMessage(new NdefRecord[]{
                ndefRecord
        });
    }

    private void writeNdefMessage(Tag tag, NdefMessage ndefMessage) {
        try {
            if(tag == null) {
                Toast.makeText(this, "Data cannot be NULL", Toast.LENGTH_SHORT).show();
                return;
            }

            Ndef ndef = Ndef.get(tag);

            if(ndef == null) formatTag(tag, ndefMessage);
            else{
                ndef.connect();

                if(!ndef.isWritable()) {
                    Toast.makeText(this, "KTM tidak dapat ditulis", Toast.LENGTH_SHORT).show();
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(ndefMessage);
                ndef.close();
                Toast.makeText(this, "KTM berhasil ditulis", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private NdefRecord createTextRecord(String content) {
        try {
            byte[] language;
            language = Locale.getDefault().getLanguage().getBytes("UTF-8");

            final byte[] text = content.getBytes("UTF-8");
            final int languageSize = language.length;
            final int textLength = text.length;
            final ByteArrayOutputStream payload = new ByteArrayOutputStream(1 + languageSize + textLength);

            payload.write((byte) (languageSize & 0x1F));
            payload.write(language, 0, languageSize);
            payload.write(text, 0, textLength);

            return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload.toByteArray());
        }catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableFDS();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableFDS();
    }

}
