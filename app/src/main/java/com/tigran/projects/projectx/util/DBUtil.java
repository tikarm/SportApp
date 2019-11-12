package com.tigran.projects.projectx.util;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class DBUtil {

    private static final String REF_STORAGE = "gs://projectx-c62f5.appspot.com";
    private static final String REF_STORAGE_CHAT = "/chat";
    private static final String REF_STORAGE_AVATARS = "/avatars";
    private static final String REF_STORAGE_IMAGES = "/images";
    private static final String REF_ANDROID = "Android";
    private static final String REF_USERS = "Users";
    private static final String REF_MESSAGES = "Messages";

    private static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance();
    }

    public static StorageReference getStorageReference() {
        return FirebaseStorage.getInstance().getReferenceFromUrl(REF_STORAGE);
    }

    public static StorageReference getRefAvatars(String s) {
        return getStorageReference().child(REF_STORAGE_AVATARS).child(s);
    }

    public static StorageReference getRefImages(String s) {
        return getStorageReference().child(REF_STORAGE_IMAGES).child(s);
    }

    public static StorageReference getRefChatImages() {
        return getStorageReference().child(REF_STORAGE_CHAT).child(REF_STORAGE_CHAT);
    }

    private static DatabaseReference getAndroidReference() {
        return getDatabase().getReference(REF_ANDROID);
    }

    public static DatabaseReference getRefAllUsers() {
        return getAndroidReference().child(REF_USERS);
    }

    public static void addAvatarToFirebase(String path) {
        getAndroidReference().child(REF_STORAGE_AVATARS).push().setValue(path);
    }

    public static void addImageToFirebase(String path) {
        getAndroidReference().child(REF_STORAGE_IMAGES).push().setValue(path);
    }


//    public static DatabaseReference getRefMessages(String friendId) {
//        return getAndroidReference().child(REF_MESSAGES).child(getChatId(friendId));
//    }


//    public static void addUserToFireBase() {
//        getRefAllUsers().child(getCurrentProfile().getId())
//                .setValue(getCurrentProfile().getName());
//    }
//
//
//    public static void addMessageToFirebase(Message message) {
//        getAndroidReference().child(REF_MESSAGES).child(getChatId(message.getReceiver())).push().setValue(message);
//    }
//
//    private static String getChatId(String friendId) {
//        int i = new BigInteger(friendId).compareTo(new BigInteger(getCurrentProfile().getId()));
//        return (i > 0) ? getCurrentProfile().getId() + friendId : friendId + getCurrentProfile().getId();
//    }
}

