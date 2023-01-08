package com.example.notes.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notes.R;
import com.example.notes.database.NotesDatabase;
import com.example.notes.entites.Note;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

public class CreateNoteActivity extends AppCompatActivity {
    private EditText inputNoteTitle, inputNoteSubTitle, inputNoteText;
    private TextView textDateTime;
    private String selectedNoteColor;
    private ImageView imageNote;
    private String selectedImagePath;
    private View viewSubtitle;
    private TextView textWebURL;
    private LinearLayout layoutWebURL;
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private AlertDialog dialogAddURL;
    private AlertDialog dialogDeleteNote;
    private Note alreadyAvailableNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_create_note );
        ImageView imageAddNoteMain = findViewById( R.id.imageBack );
        imageAddNoteMain.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        } );
        inputNoteTitle = findViewById( R.id.inputNoteTitle);
        inputNoteSubTitle = findViewById( R.id.inputNoteSubtitle );
        inputNoteText = findViewById( R.id.inputNote );
        textDateTime = findViewById( R.id.textDateTime);
        imageNote = findViewById( R.id.imageNote );
        viewSubtitle = findViewById( R.id.viewSubtitle );
        imageNote = findViewById( R.id.imageNote );
        textWebURL = findViewById( R.id.textWebURL );
        layoutWebURL = findViewById( R.id.layoutWebURL );
        textDateTime.setText(
                new SimpleDateFormat( "EEE,dd MMMM yyyy HH:mm a", Locale.getDefault() )
                        .format( new Date() )
        );
        ImageView imageSave = findViewById( R.id.imageSave );
        imageSave.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        } );

        selectedNoteColor = "#333333";
        selectedImagePath = "";

        if(getIntent().getBooleanExtra( "isViewOrUpdate", false)){
           alreadyAvailableNote = (Note)getIntent().getSerializableExtra( "note" );
           setViewOrUpdateNote();
        }

        initMiscellaneous();
        setSubtitleColor();
    }
    private void setViewOrUpdateNote(){
        inputNoteTitle.setText(alreadyAvailableNote.getTitle());
        inputNoteSubTitle.setText( alreadyAvailableNote.getSubtitle() );
        inputNoteText.setText( alreadyAvailableNote.getNotetext());
        textDateTime.setText( alreadyAvailableNote.getDatetime());
        if(alreadyAvailableNote.getImagepath()!=null && !alreadyAvailableNote.getImagepath().trim().isEmpty()){
            imageNote.setImageBitmap( BitmapFactory.decodeFile(alreadyAvailableNote.getImagepath()));
            imageNote.setVisibility((View.VISIBLE));
            selectedImagePath = alreadyAvailableNote.getImagepath();
        }
        if(alreadyAvailableNote.getWeblink() != null && !alreadyAvailableNote.getWeblink().trim().isEmpty()){
            textWebURL.setText( alreadyAvailableNote.getWeblink());
            layoutWebURL.setVisibility( View.VISIBLE);
        }
    }

    private void saveNote() {
        if (inputNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText( this, "Note title can't be empty!", Toast.LENGTH_SHORT ).show();
            return;
        } else if (inputNoteSubTitle.getText().toString().trim().isEmpty()
                && inputNoteText.getText().toString().trim().isEmpty()) {
            Toast.makeText( this, "Note can't be empty", Toast.LENGTH_SHORT ).show();
            return;
        }
        final Note note = new Note();
        note.setTitle( inputNoteTitle.getText().toString() );
        note.setSubtitle( inputNoteSubTitle.getText().toString() );
        note.setNotetext( inputNoteText.getText().toString() );
        note.setDatetime( textDateTime.getText().toString() );
        note.setColor( selectedNoteColor );
        note.setImagepath( selectedImagePath );
        if(layoutWebURL.getVisibility()==View.VISIBLE){
            note.setWeblink( textWebURL.getText().toString() );
        }

        if(alreadyAvailableNote != null){
            note.setId( alreadyAvailableNote.getId() );
        }
        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase( getApplicationContext() ).noteDao().insertNote( note );
                return null;
            }

            @Override
            protected void onPostExecute(Void avoid) {
                super.onPostExecute( avoid );
                Intent intent = new Intent(CreateNoteActivity.this, MainActivity.class );
                startActivity( intent );
                setResult( RESULT_OK, intent );
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById( R.id.layoutMiscellaneous );
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from( layoutMiscellaneous );
        layoutMiscellaneous.findViewById( R.id.textMiscellaneous ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState( BottomSheetBehavior.STATE_EXPANDED );
                } else {
                    bottomSheetBehavior.setState( BottomSheetBehavior.STATE_COLLAPSED );
                }
            }
        } );

        final ImageView imagecolor1 = layoutMiscellaneous.findViewById( R.id.imageColor1 );
        final ImageView imagecolor2 = layoutMiscellaneous.findViewById( R.id.imageColor2 );
        final ImageView imagecolor3 = layoutMiscellaneous.findViewById( R.id.imageColor3 );
        final ImageView imagecolor4 = layoutMiscellaneous.findViewById( R.id.imageColor4 );
        final ImageView imagecolor5 = layoutMiscellaneous.findViewById( R.id.imageColor5 );

        layoutMiscellaneous.findViewById( R.id.viewColor1 ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#333333";
                imagecolor1.setImageResource( R.drawable.done );
                imagecolor2.setImageResource( 0 );
                imagecolor3.setImageResource( 0 );
                imagecolor4.setImageResource( 0 );
                imagecolor5.setImageResource( 0 );
                setSubtitleColor();
            }
        } );

        layoutMiscellaneous.findViewById( R.id.viewColor2 ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#ECD405";
                imagecolor1.setImageResource( 0 );
                imagecolor2.setImageResource( R.drawable.done );
                imagecolor3.setImageResource( 0 );
                imagecolor4.setImageResource( 0 );
                imagecolor5.setImageResource( 0 );
                setSubtitleColor();
            }
        } );
        layoutMiscellaneous.findViewById( R.id.viewColor3 ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#F8266D";
                imagecolor1.setImageResource( 0 );
                imagecolor2.setImageResource( 0 );
                imagecolor3.setImageResource( R.drawable.done );
                imagecolor4.setImageResource( 0 );
                imagecolor5.setImageResource( 0 );
                setSubtitleColor();
            }
        } );
        layoutMiscellaneous.findViewById( R.id.viewColor4 ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#1C36ED";
                imagecolor1.setImageResource( 0 );
                imagecolor2.setImageResource( 0 );
                imagecolor3.setImageResource( 0 );
                imagecolor4.setImageResource( R.drawable.done );
                imagecolor5.setImageResource( 0 );
                setSubtitleColor();
            }
        } );
        layoutMiscellaneous.findViewById( R.id.viewColor5 ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedNoteColor = "#000000";
                imagecolor1.setImageResource( 0 );
                imagecolor2.setImageResource( 0 );
                imagecolor3.setImageResource( 0 );
                imagecolor4.setImageResource( 0 );
                imagecolor5.setImageResource(R.drawable.done);
                setSubtitleColor();
            }
        } );

        if(alreadyAvailableNote != null && alreadyAvailableNote.getColor() != null && !alreadyAvailableNote.getColor().trim().isEmpty()){
            switch (alreadyAvailableNote.getColor()){
                case "#ECD405":
                    layoutMiscellaneous.findViewById( R.id.viewColor2 ).performClick();
                    break;
                case "#F8266D":
                    layoutMiscellaneous.findViewById( R.id.viewColor3 ).performClick();
                    break;
                case "#1C36ED":
                    layoutMiscellaneous.findViewById( R.id.viewColor4 ).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById( R.id.viewColor5 ).performClick();
                    break;


            }
        }

        layoutMiscellaneous.findViewById( R.id.layoutAddImage ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState( BottomSheetBehavior.STATE_COLLAPSED );
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            CreateNoteActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                } else {
                    selectImage();
                }
            }
        } );
        layoutMiscellaneous.findViewById( R.id.layoutAddUrl ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState( BottomSheetBehavior.STATE_COLLAPSED );
                showAddURLDialog();
            }
        } );

        if(alreadyAvailableNote != null){
            layoutMiscellaneous.findViewById( R.id.layoutDeleteNote ).setVisibility( View.VISIBLE );
            layoutMiscellaneous.findViewById( R.id.layoutDeleteNote ).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState( BottomSheetBehavior.STATE_COLLAPSED );
                    showDeleteNoteDialog();
                }
            } );
        }
    }
    private void showDeleteNoteDialog(){
        if(dialogDeleteNote == null){
            AlertDialog.Builder builder = new AlertDialog.Builder( CreateNoteActivity.this );
            View view = LayoutInflater.from( this ).inflate(
                    R.layout.layout_delete_note,
                    (ViewGroup) findViewById( R.id.layoutDeleteNoteContainer )
            );
            builder.setView( view );
            dialogDeleteNote = builder.create();
            if(dialogDeleteNote.getWindow()!=null){
                dialogDeleteNote.getWindow().setBackgroundDrawable( new ColorDrawable(0));
            }

            view.findViewById(R.id.textDeleteNote ).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint( "StaticFieldLeak" )
                    class DeleteNoteTask extends AsyncTask<Void,Void,Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getNotesDatabase( getApplicationContext()).noteDao()
                                    .deleteNote(alreadyAvailableNote );
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute( aVoid );
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDelete",true);
                            setResult( RESULT_OK,intent );
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            } );

            view.findViewById(R.id.textCancel ).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogDeleteNote.dismiss();
                }
            } );
        }
        dialogDeleteNote.show();
    }
    private void setSubtitleColor(){
        GradientDrawable gradientDrawable = (GradientDrawable) viewSubtitle.getBackground();
        gradientDrawable.setColor( Color.parseColor(selectedNoteColor) );
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
        if(intent.resolveActivity( getPackageManager()) != null){
            startActivityForResult( intent,REQUEST_CODE_SELECT_IMAGE );
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult( requestCode, permissions, grantResults );
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText( this, "Permission Denied!", Toast.LENGTH_SHORT ).show();
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                Uri selectedImageUri = data.getData();
                if(selectedImageUri != null){
                    try{
                        InputStream inputStream = getContentResolver().openInputStream( selectedImageUri );
                        Bitmap bitmap = BitmapFactory.decodeStream( inputStream );
                        imageNote.setImageBitmap( bitmap );
                        imageNote.setVisibility( View.VISIBLE );

                    }catch (Exception exception){
                        Toast.makeText( this,exception.getMessage(),Toast.LENGTH_SHORT ).show();
                    }
                }
            }
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private String setImageUri(Uri contentUri){
        String filePath;
        Cursor cursor = getContentResolver().query( contentUri,null,null,null );
        if(cursor == null){
            filePath = contentUri.getPath();
        }else{
            cursor.moveToFirst();
            int index = cursor.getColumnIndex( "_data" );
            filePath = cursor.getString( index );
            cursor.close();
        }
        return filePath;
    }

    private void showAddURLDialog(){
        if(dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder( CreateNoteActivity.this );
            View view = LayoutInflater.from( this ).inflate(
                    R.layout.layout_add_url,
                    (ViewGroup) findViewById( R.id.layoutAddUrlContainer )
            );
            builder.setView( view );
            dialogAddURL = builder.create();
            if(dialogAddURL.getWindow()!=null){
                dialogAddURL.getWindow().setBackgroundDrawable( new ColorDrawable( 0 ));
            }
            final EditText inputURL = view.findViewById( R.id.inputURL );
            inputURL.requestFocus();
            view.findViewById( R.id.textAdd ).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (inputURL.getText().toString().trim().isEmpty()) {
                        Toast.makeText( CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT ).show();
                    } else if (!Patterns.WEB_URL.matcher( inputURL.getText().toString() ).matches()) {
                        Toast.makeText( CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT ).show();
                    } else {
                        textWebURL.setText( inputURL.getText().toString() );
                        layoutWebURL.setVisibility( View.VISIBLE );
                        dialogAddURL.dismiss();
                    }
                }
            } );
            view.findViewById( R.id.textCancel).setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogAddURL.dismiss();
                }
            } );
        }
        dialogAddURL.show();
    }
}
