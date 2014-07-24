package com.example.digigraff.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    /** Button object which is tied to picture taking */
    protected Button _button;

    /** Image field tied to picture taking */
    protected ImageView _image;

    /** Text box in the top left corner of our GUI */
    protected TextView _field;

    /** File path where pictures will be stored after being taken */
    protected String _path;

    /** Boolean value which is true only when a picture has been taken */
    protected boolean _taken;

    /** Used as a key for determining whether or not a photo has been taken */
    protected static final String PHOTO_TAKEN = "photo_taken";

    /** Current Image Object */
    protected Image _currentImage;

    /** Current serialized Image Object */
    protected String _myJSonImage;

    /** Tage reference used for debugging print statements sent to the console */
    private static final String TAG = "MainActivity";


    /** Automatically generated method which was modified to handle android widget assignment */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        _image = (ImageView) findViewById( R.id.image );
        _field = (TextView) findViewById( R.id.field );
        _button = (Button) findViewById( R.id.button );
        _button.setOnClickListener( new ButtonClickHandler() );
        //_path = new File(Environment.getExternalStorageDirectory().getPath() + "/images/");
        //_path = Environment.getExternalStorageDirectory().getPath() + "/images/";
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
                onPhotoTaken();
                break;
        }
    }



    /** Serializes the given JSON image object */
    protected void serializeImage (Image toSerialize) {
        Gson gson = new Gson();
        _myJSonImage = gson.toJson(toSerialize);
    }

    /** Changes the appropriate booleans and constants to their post-picture state
     *  and also down samples image size to save on frivolous space usage. */
    protected void onPhotoTaken()
    {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
        String bitmap_sender = BitMapToString(bitmap);
        Log.i("MakeMachine", "bitmap to string = " + bitmap_sender);
        _image.setImageBitmap(bitmap);
        _currentImage = new Image(_image);


     //   serializeImage(_currentImage);


        _field.setVisibility( View.GONE );
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
            onPhotoTaken();
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


    public class Image {

        /** My username */
        public String username;

        /** My user ID */
        public String user_id;

        /** My physical photo data */
        public ImageView photo;

        /** The date I was captured */
        public String date;

        /** My coordinates as a two element float array */
        public float[] my_coordinates;

        Image(ImageView foto) {
            this.photo = foto;
            this.username = "not specified";
            this.user_id = "not specified";
        }

        Image(ImageView foto, String name) {
            this.photo = foto;
            this.username = name;
            this.user_id = "not specified";
        }

        Image(ImageView foto, String name, String ID) {
            this.photo = foto;
            this.username = name;
            this.user_id = ID;
        }

        public ImageView getPhoto() {
            return photo;
        }

        public String getDate() {
            return date;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getUsername() {
            return username;
        }
    }

}
