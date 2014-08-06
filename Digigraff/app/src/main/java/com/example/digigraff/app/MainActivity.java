package com.example.digigraff.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    /** Button object which is tied to picture taking */
    protected Button _button;

    /** Image field tied to picture taking */
    protected ImageView _image;

    /** Text box in the top left corner of our GUI */
    protected TextView _field;

    /** File path where pictures will be stored after being taken */
    protected String _path;

    /** File path where pictures will be stored after being taken */
    public String _username;

    /** Boolean value which is true only when a picture has been taken */
    protected boolean _taken;

    /** Used as a key for determining whether or not a photo has been taken */
    protected static final String PHOTO_TAKEN = "photo_taken";

    /** Current Image Object */
    protected Image _currentImage;

    /** Most Recent Longitude Value */
    protected Double _longitude;

    /** Most Recent Latitude Value */
    protected Double _latitude;

    /** Pseudo-boolean which is 0 before the username is changed and 1 after */
    protected int _alreadyset = 0;


    /** Automatically generated method which was modified to handle android widget assignment */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _image = (ImageView) findViewById( R.id.image );
        _field = (TextView) findViewById( R.id.field );

        _button = (Button) findViewById( R.id.button );
        _button.setOnClickListener(new ButtonClickHandler());
        if (_alreadyset == 0) {
            usernameRequest();
        }
    }

    /** Request Username from user and change internal field */
    public void usernameRequest() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Welcome to DigiGraff!");
        alert.setMessage("Please Enter Your Username Below");

// Set an EditText view to get user input
        final EditText input = new EditText(this);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (input.getText().toString() != null) {
                    _alreadyset = 1;
                    _username = input.getText().toString();
                } else {
                    _username = "Eugene Wang";
                }

                // Do something with value!
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    /** Method which prepares intent to actually take a picture */
    public class ButtonClickHandler implements View.OnClickListener
    {
        public void onClick( View view ){
            try {
                startCameraActivity();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /** Method which creates an intent object to start the camera functionality.
     *  (Should apparently circumvent the need to declare camera use permissions.) */

      protected void startCameraActivity() throws IOException {
        File file = createImageFile();
        file.mkdirs();
        Uri outputFileUri = Uri.fromFile( file );

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
        startActivityForResult( intent, 11 );
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        _path = image.getAbsolutePath();
        return image;
    }

    /** Handles user camera interaction, such as returning after user is done. */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.i("MakeMachine", "resultCode: " + resultCode);

        switch( resultCode )
        {
            case 0:
                Log.i( "MakeMachine", "User cancelled" );
                break;

            case -1:
                Log.i( "MakeMachine", "User took photo" );
                try {
                    onPhotoTaken();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    /** Changes the appropriate booleans and constants to their post-picture state
     *  and also down samples image size to save on frivolous space usage. */
    protected void onPhotoTaken() throws IOException, JSONException {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
        String bitmap_sender = BitMapToString(bitmap);
        Log.i("MakeMachine", "bitmap to string = " + bitmap_sender);
        _image.setImageBitmap(bitmap);

    //    locationSnapUp();

        FetchTask poster = new FetchTask();
        poster.execute();
        _field.setVisibility(View.GONE);
    }

    /** Updates the GPS latitude and longitude */
    public void locationSnapUp() {


        final LocationListener locationListener = new LocationListener() {
            Double longitude;
            Double latitude;
            public void onLocationChanged(Location location) {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        _longitude = location.getLongitude();
        _latitude = location.getLatitude();
    }


    public class FetchTask extends AsyncTask<Void, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(Void... params) {
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://sagebears-datastore.appspot.com/store");
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4;
                Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
                String bitmap_sender = BitMapToString(bitmap);


                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
                nameValuePairs.add(new BasicNameValuePair("username", _username));

                Log.i("MakeMachine", "This is what was passed in for a username: " + _username);
                Calendar c = Calendar.getInstance();
                int seconds = c.get(Calendar.MILLISECOND);
                nameValuePairs.add(new BasicNameValuePair("user_id", _username.hashCode() + seconds + ""));
                nameValuePairs.add(new BasicNameValuePair("date", "08/06/14"));
         //       nameValuePairs.add(new BasicNameValuePair("lat", _latitude + ""));
         //       nameValuePairs.add(new BasicNameValuePair("lon", _longitude + ""));
                nameValuePairs.add(new BasicNameValuePair("lon", "-122.0840"));
                nameValuePairs.add(new BasicNameValuePair("lat", "37.4220"));
                nameValuePairs.add(new BasicNameValuePair("photo", bitmap_sender));
        //        Log.i("MakeMachine", "Latitude: " + _latitude);
        //        Log.i("MakeMachine", "Longitude: " + _longitude);

                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                sb.append(reader.readLine() + "\n");
                String line = "0";
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                reader.close();
                String result11 = sb.toString();
            //    Log.i("MakeMachine", "HTTP Response made into string: " + result11);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            if (result != null) {
                // do something
            } else {
                // error occured
            }
        }
    }


    /** Method from stack overflow which converts a bitmap into a string */
    public String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream ByteStream=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, ByteStream);
        byte [] b=ByteStream.toByteArray();
        String temp=Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    /** Prevents camera rotation error which causes pictures to disappear */
    @Override
    protected void onSaveInstanceState( Bundle outState ) {
        outState.putBoolean( MainActivity.PHOTO_TAKEN, _taken );
    }
    @Override
    protected void onRestoreInstanceState( Bundle savedInstanceState)
    {
        Log.i( "MakeMachine", "onRestoreInstanceState()");
        if( savedInstanceState.getBoolean( MainActivity.PHOTO_TAKEN ) ) {
            try {
                onPhotoTaken();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
