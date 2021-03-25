package com.vnptit.videocallsample.db;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.vnptit.videocallsample.model.Contact;

import java.util.List;

public class ContactRepository {
    private ContactDao contactDao;
    private LiveData<List<Contact>> mAllContacts;

    public ContactRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        contactDao = db.contactDao();
        mAllContacts = contactDao.getAll();
    }

    public LiveData<List<Contact>> getAllContacts() {
        return mAllContacts;
    }

    public void insert(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.insert(contact);
        });
    }

    public void update(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.update(contact);
        });
    }

    public void delete(Contact contact) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            contactDao.delete(contact);
        });
    }
}
