package com.myjournal.journalapp.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.myjournal.journalapp.R;
import com.myjournal.journalapp.ExpEntriesMap;
import com.myjournal.journalapp.models.ExpenseBox;
import com.myjournal.journalapp.models.ExpenseBoxDao;
import com.myjournal.journalapp.utils.ExpenseRecyclerViewAdapterView;

import java.util.ArrayList;

import static com.myjournal.journalapp.ExpEntriesMap.ExpEntriesIndex;

public class ExpEntriesTab extends Fragment {
    RecyclerView recyclerView;
    ArrayList<ExpenseBox> expenseEntryList;
    String USER = FirebaseAuth.getInstance().getCurrentUser().getUid();           //"Kiran1901";
    CollectionReference expenseEntriesRef = FirebaseFirestore.getInstance().collection("expense_entries");
    ExpenseRecyclerViewAdapterView adapter;
    ListenerRegistration liveExpenseEntries;

    private boolean isScrolling = false;
    private boolean isLastItemReached = false;
    private int limit = 5;
    private DocumentSnapshot lastVisible;


    public ExpEntriesTab() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_home_expense_entries, container, false);
        recyclerView = rootView.findViewById(R.id.exp_recycler_view);

        expenseEntryList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseRecyclerViewAdapterView(getContext(), expenseEntryList);
        liveExpenseEntries = expenseEntriesRef.document(USER).collection("entries").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.i("ERROR:", "listen:error", e);
                return;
            }
            int i = 0;
            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                String key = null;
                ExpenseBoxDao expenseBoxDao = null;
                switch (dc.getType()) {
                    case ADDED:
                        key = dc.getDocument().getId();
                        expenseBoxDao = dc.getDocument().toObject(ExpenseBoxDao.class);
                        if (expenseEntryList.size() > 0 && expenseEntryList.get(0).getTimestamp().compareTo(expenseBoxDao.getTimestamp().toDate()) < 0) {
                            expenseEntryList.add(0, new ExpenseBox(expenseBoxDao, key));
                            ExpEntriesMap.addFirst(key);
                        } else {
                            expenseEntryList.add(new ExpenseBox(expenseBoxDao, key));
                            ExpEntriesIndex.put(key, expenseEntryList.size() - 1);
                        }
                        break;

                    case MODIFIED:
                        key = dc.getDocument().getId();
                        expenseBoxDao = dc.getDocument().toObject(ExpenseBoxDao.class);
                        int index = ExpEntriesIndex.get(key);
                        expenseEntryList.set(index, new ExpenseBox(expenseBoxDao, key));
                        break;

                    case REMOVED:
                        for (ExpenseBox ex : expenseEntryList) {
                            if (ex.getId().equals(dc.getDocument().getId())) {
                                ExpEntriesMap.delete(ex.getId(), expenseEntryList.indexOf(ex));
                                expenseEntryList.remove(ex);
                                break;
                            }
                        }
                        break;
                }
            }
            adapter.notifyDataSetChanged();
            if (snapshots.size() != 0)
                lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
        });

        recyclerView.setAdapter(adapter);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    isScrolling = true;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
                int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = linearLayoutManager.getChildCount();
                int totalItemCount = linearLayoutManager.getItemCount();

                if (isScrolling && (firstVisibleItemPosition + visibleItemCount == totalItemCount) && !isLastItemReached) {
                    isScrolling = false;

                    expenseEntriesRef.document(USER).collection("entries").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(limit).addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Log.i("ERROR:", "listen:error", e);
                            return;
                        }
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            String key = null;
                            ExpenseBoxDao expenseBoxDao = null;
                            switch (dc.getType()) {
                                case ADDED:
                                    key = dc.getDocument().getId();
                                    expenseBoxDao = dc.getDocument().toObject(ExpenseBoxDao.class);
                                    if (expenseEntryList.size() > 0 && expenseEntryList.get(0).getTimestamp().compareTo(expenseBoxDao.getTimestamp().toDate()) < 0) {
                                        expenseEntryList.add(0, new ExpenseBox(expenseBoxDao, key));
                                        ExpEntriesMap.addFirst(key);
                                    } else {
                                        expenseEntryList.add(new ExpenseBox(expenseBoxDao, key));
                                        ExpEntriesIndex.put(key, expenseEntryList.size() - 1);
                                    }
                                    break;

                                case MODIFIED:
                                    key = dc.getDocument().getId();
                                    expenseBoxDao = dc.getDocument().toObject(ExpenseBoxDao.class);
                                    int index = ExpEntriesIndex.get(key);
                                    expenseEntryList.set(index, new ExpenseBox(expenseBoxDao, key));
                                    break;

                                case REMOVED:
                                    for (ExpenseBox ex : expenseEntryList) {
                                        if (ex.getId().equals(dc.getDocument().getId())) {
                                            ExpEntriesMap.delete(ex.getId(), expenseEntryList.indexOf(ex));
                                            expenseEntryList.remove(ex);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (snapshots.size() != 0) {
                            lastVisible = snapshots.getDocuments().get(snapshots.size() - 1);
                        }

                        if (snapshots.size() < limit) {
                            isLastItemReached = true;
                        }
                    });
                }
            }
        });

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        liveExpenseEntries.remove();
    }
}
