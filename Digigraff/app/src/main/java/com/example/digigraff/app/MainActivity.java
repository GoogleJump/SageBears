package com.example.digigraff.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;


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

        _path = Environment.getExternalStorageDirectory() + "/images/make_machine_example.jpg";
    }

    /** Method which prepares intent to actually take a picture */
    public class ButtonClickHandler implements View.OnClickListener
    {
        public void onClick( View view ){
            startCameraActivity();
        }
    }


    /** Method which creates an intent object to start the camera functionality. */
    protected void startCameraActivity()
    {
        File file = new File( _path );
        Uri outputFileUri = Uri.fromFile( file );

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
        intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );

        startActivityForResult( intent, 0 );
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
                onPhotoTaken();
                break;
        }
    }

    /** Changes the appropriate booleans and constants to their post-picture state
     *  and also down samples image size to save on frivolous space usage. */
    protected void onPhotoTaken()
    {
        _taken = true;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;

        Bitmap bitmap = BitmapFactory.decodeFile( _path, options );
        _image.setImageBitmap(bitmap);

        _field.setVisibility( View.GONE );
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

}
