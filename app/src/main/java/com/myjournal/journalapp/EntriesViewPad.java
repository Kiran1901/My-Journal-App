package com.myjournal.journalapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.myjournal.journalapp.models.Feedbox;

public class EntriesViewPad extends AppCompatActivity {

    FloatingActionButton editFab;

    String USER = FirebaseAuth.getInstance().getCurrentUser().getUid();           //"Kiran1901";

    TextView dateField,timeField,dataField;
    Feedbox feedbox;

    ImageButton deleteEntryButton;

    CollectionReference journalEntriesRef = FirebaseFirestore.getInstance().collection("journal_entries");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_view_pad);

        editFab = findViewById(R.id.editEntryFab);
        dateField = findViewById(R.id.timeline_view_pad_date);
        timeField = findViewById(R.id.timeline_view_pad_time);
        dataField = findViewById(R.id.timeline_view_pad_data);

        dataField.setMovementMethod(new ScrollingMovementMethod());
        dataField.setVerticalScrollBarEnabled(true);

        deleteEntryButton = findViewById(R.id.deleteEntryButton);

        deleteEntryButton.setOnClickListener(v -> deleteEntry());

        Intent intent = getIntent();
        feedbox = ((Feedbox) intent.getSerializableExtra("feedbox"));

        dateField.setText(feedbox.getDate());
        timeField.setText(feedbox.getTime());
        dataField.setText(feedbox.getData());

        editFab.setOnClickListener(v -> editEntry());

    }

    private void editEntry(){
        Intent intent = new Intent(getApplicationContext(), EntriesEditPad.class);
        intent.putExtra("feedbox",feedbox);
        startActivity(intent);
        finish();
    }

    private void deleteEntry(){

        journalEntriesRef.document(USER).collection("entries").document(feedbox.getId()).delete();
        EntriesMap.delete(feedbox.getId(), EntriesMap.EntriesIndex.get(feedbox.getId()));
        finish();
    }
}
