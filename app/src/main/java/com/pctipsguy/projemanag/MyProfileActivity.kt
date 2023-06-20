package com.pctipsguy.projemanag

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pctipsguy.projemanag.databinding.ActivityMyProfileBinding

class MyProfileActivity : BaseActivity() {


    private lateinit var mUserDetails:User
    private var binding:ActivityMyProfileBinding? = null
    private var imageURI:Uri? = null
    private var mProfileImageURL:String =""

    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()){ result ->
            if(result!=null){
                imageURI = result
                binding?.ivUserImage?.setImageURI(imageURI!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        FirestoreClass().signInUser(this)
        binding?.ivUserImage?.setOnClickListener{
            setUserImage()
        }
        binding?.btnUpdate?.setOnClickListener {
            if(imageURI!=null)
                uploadUserImage()
            else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarMyProfileActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.my_profile)
        }
        binding?.toolbarMyProfileActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    fun setUserDataInUI(user: User) {
        mUserDetails = user
        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(binding?.ivUserImage!!)
        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)
        if (user.mobile != 0L) {
            binding?.etMobile?.setText(user.mobile.toString())
        }
    }

    fun setUserImage(){
        openGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun uploadUserImage(){
        showProgressDialog(resources.getString(R.string.please_wait))
        if(imageURI!=null){
            val pathString = "User_image"+System.currentTimeMillis()
            Log.e("Image saved", "uploadUserImage: $pathString" )
            val sRef: StorageReference =
                FirebaseStorage.getInstance().reference.child(pathString)
            sRef.putFile(imageURI!!).addOnSuccessListener {
                takeSnapshot ->
                Log.i("Firebase Image URL",
                    takeSnapshot.metadata!!.reference!!.downloadUrl.toString())
                takeSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    url ->
                    Log.i("dowloadableImage",url.toString())
                    mProfileImageURL = url.toString()
                    updateUserProfileData()
                }

            }
        }
    }

    fun profileUpdateSuccess(){
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun updateUserProfileData(){
        var isUpdated = false
        val userHashMap = HashMap<String,Any>()
        if(mProfileImageURL.isNotEmpty() && mProfileImageURL!=mUserDetails.image){
            userHashMap[Constants.IMAGE] = mProfileImageURL
            isUpdated = true
        }
        if(binding?.etName?.text.toString()!=mUserDetails.name){
            userHashMap[Constants.Name] = binding?.etName?.text.toString()
            isUpdated = true
        }
        if(binding?.etMobile?.text.toString().isNotEmpty() && (binding?.etMobile?.text.toString()!=mUserDetails.mobile.toString())){
            userHashMap[Constants.MOBILE] = binding?.etMobile?.text.toString().toLong()
            isUpdated = true
        }
        if(isUpdated){
            FirestoreClass().updateUserProfile(this,userHashMap)
        }
    }

}