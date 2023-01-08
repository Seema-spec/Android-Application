package com.example.notes.activities;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.notes.R;
import com.example.notes.adapters.NotesAdapter;
import com.example.notes.database.NotesDatabase;
import com.example.notes.entites.Note;
import com.example.notes.listeners.NotesListener;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {
    public static final int REQUEST_CODE_ADD_NOTE = 1;
    public static final int REQUEST_CODE_UPDATE_NOTE = 2;
    public static final int REQUEST_CODE_SHOW_NOTE = 3;

    private RecyclerView notesRecyclerView;
    private List<Note> noteList;
    private NotesAdapter notesAdapter;
    private AlertDialog dialogAddURL;
    private int noteClickedPosition = -1;

    NavigationView navigationView;
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        ImageView imageAddNoteMain = findViewById( R.id.imageAddNoteMain );
        imageAddNoteMain.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult( new Intent(getApplicationContext(), CreateNoteActivity.class),
                REQUEST_CODE_ADD_NOTE
                );
            }
        } );
        notesRecyclerView = findViewById( R.id.recyclerView );
        notesRecyclerView.setLayoutManager(
                new StaggeredGridLayoutManager( 2,StaggeredGridLayoutManager.VERTICAL )
        );
        noteList = new ArrayList<>();
        notesAdapter = new NotesAdapter(noteList, this );
        notesRecyclerView.setAdapter( notesAdapter );

        getNotes(REQUEST_CODE_SHOW_NOTE , false);

        EditText inputSearch = findViewById( R.id.inputSearch );
        inputSearch.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                notesAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(noteList.size() != 0){
                    notesAdapter.searchNotes(s.toString());
                }
            }
        } );

        findViewById( R.id.imageAddNote ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(
                        new Intent(getApplicationContext(), CreateNoteActivity.class),
                        REQUEST_CODE_ADD_NOTE
                );
            }
        } );

        findViewById( R.id.imageAddWebLink ).setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddURLDialog();
            }
        } );
    }

    @Override
    public void onNoteClicked(Note note, int position) {
        noteClickedPosition= position;
        Intent intent = new Intent( getApplicationContext(), CreateNoteActivity.class );
        intent.putExtra( "isViewOrUpdate", true );
        intent.putExtra( "note", note );
        startActivityForResult( intent, REQUEST_CODE_UPDATE_NOTE );
    }

    private void getNotes(final int requestCode,final boolean isNoteDeleted){
        @SuppressLint( "StaticFieldLeak" )
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {

            @Override
            protected List<Note> doInBackground(Void...voids){
                return NotesDatabase
                        .getNotesDatabase( getApplicationContext())
                        .noteDao().getAllNotes();
            }
            @Override
            protected void onPostExecute(List<Note> notes){
                super.onPostExecute( notes );
               if(requestCode == REQUEST_CODE_SHOW_NOTE){
                   noteList.addAll( notes );
                   notesAdapter.notifyDataSetChanged();
               }else if(requestCode == REQUEST_CODE_ADD_NOTE){
                   noteList.add(0,notes.get(0));
                   notesAdapter.notifyItemInserted( 0 );
                   notesRecyclerView.smoothScrollToPosition( 0 );
               }else if(requestCode == REQUEST_CODE_UPDATE_NOTE){
                   noteList.remove( noteClickedPosition );
                   if(isNoteDeleted){
                       notesAdapter.notifyItemRemoved( noteClickedPosition );
                   }else {
                       noteList.add(noteClickedPosition, notes.get(noteClickedPosition));
                       notesAdapter.notifyItemChanged( noteClickedPosition );
                   }
               }
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if(requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK){
            getNotes( REQUEST_CODE_ADD_NOTE ,false);
        }else if(requestCode == REQUEST_CODE_UPDATE_NOTE && resultCode ==RESULT_OK){
            if(data !=null){
                getNotes( REQUEST_CODE_UPDATE_NOTE,data.getBooleanExtra( "isNoteDeleted",false) );
            }
        }

    }
    private void showAddURLDialog(){
        if(dialogAddURL == null){
            AlertDialog.Builder builder = new AlertDialog.Builder( MainActivity.this );
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
                        Toast.makeText( MainActivity.this, "Enter URL", Toast.LENGTH_SHORT ).show();
                    } else if (!Patterns.WEB_URL.matcher( inputURL.getText().toString() ).matches()) {
                        Toast.makeText( MainActivity.this, "Enter valid URL", Toast.LENGTH_SHORT ).show();
                    } else {
                         dialogAddURL.dismiss();
                         Intent intent = new Intent( getApplicationContext(), CreateNoteActivity.class );
                         intent.putExtra( "isFromQuickActions",true );
                         intent.putExtra(  "quickAcyionType","URL");
                         intent.putExtra( "URL", inputURL.getText().toString() );
                         startActivityForResult( intent,REQUEST_CODE_ADD_NOTE );
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