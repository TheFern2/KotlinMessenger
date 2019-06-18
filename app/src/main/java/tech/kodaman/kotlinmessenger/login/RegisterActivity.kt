package tech.kodaman.kotlinmessenger.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_register.*
import android.app.Activity
import android.net.Uri
import android.provider.MediaStore
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import tech.kodaman.kotlinmessenger.R
import tech.kodaman.kotlinmessenger.messages.LatestMessagesActivity
import tech.kodaman.kotlinmessenger.models.User
import java.util.*

const val TAG = "RegisterActivity"

class RegisterActivity : AppCompatActivity() {

    private val PICK_PHOTO_FOR_AVATAR = 0
    var email = ""
    var password = ""
    var username = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        registerButton.setOnClickListener {
            performRegister()
        }

        existingAccountTextView.setOnClickListener {
            Log.d(TAG, "Launching login activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }

        photoButton.setOnClickListener {
            Log.d(TAG, "Selecting photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
            //intent.putExtra("selectedPhotoUri",selectedPhotoUri);
            //setResult(RESULT_OK,intent);
            //finish();
        }
    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            // check what image was selected
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
            profileCircleImageView.setImageBitmap(bitmap) // Set Circle View
            photoButton.alpha = 0f // Hide Button
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            photoButton.setBackgroundDrawable(bitmapDrawable)
            Log.d(TAG, "selectedPhotoUri $selectedPhotoUri")

        }
    }

    private fun performRegister(){
        email = emailEditText.text.toString()
        password = passwordEditText.text.toString()
        username = userNameEditText.text.toString()

        if(email.isEmpty() || password.isEmpty() || username.isEmpty()){
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "email is $email")
        Log.d(TAG, "password is $password")
        Log.d(TAG, "username is $username")

        // Firebase authentication
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful){
                        //Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
                        return@addOnCompleteListener
                    }

                    Log.d(TAG, "Successfully created user with ${it.result?.user?.uid}")
                    Toast.makeText(this, "Successfully created user", Toast.LENGTH_SHORT).show()

                    // here we need a sanity check to ensure a user can still register
                    // without a photo
                    if(selectedPhotoUri == null){
                        saveUserToFirebaseDatabase("null");
                    } else{
                        uploadImagetoFirebaseStorage()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Failed to create user! ${it.message}")
                }
    }

    private fun uploadImagetoFirebaseStorage(){

        if(selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully uploaded photo ${it.metadata?.path}")

                    ref.downloadUrl.addOnSuccessListener {
                        Log.d(TAG, "$it")

                        saveUserToFirebaseDatabase(it.toString());
                    }
                }
                .addOnFailureListener{
                    Log.d(TAG, "Failed to upload photo ${it.message}")
                }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl:String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username, profileImageUrl)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully created user")

                    // Launch messaging activity
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d(TAG, "Failed to create user")
                }
    }

}