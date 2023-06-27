package com.pctipsguy.projemanag

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pctipsguy.projemanag.databinding.ActivityCreateBoardBinding

class CreateBoardActivity : BaseActivity() {
    private var binding: ActivityCreateBoardBinding? = null
    private var imageURI: Uri? = null
    private lateinit var mUserName:String
    private var mBoardImageURL: String? = null

    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
            if (result != null) {
                imageURI = result
                binding?.ivBoardImage?.setImageURI(imageURI!!)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setupActionBar()
        if(intent.hasExtra(Constants.Name)){
            mUserName = intent.getStringExtra(Constants.Name)!!
        }
        binding?.ivBoardImage?.setOnClickListener {
            openGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        binding?.btnCreate?.setOnClickListener {
            showProgressDialog(resources.getString(R.string.please_wait))
            if(imageURI!=null)
                uploadBoardImage()
            else
                createBoard()
        }
    }

    private fun uploadBoardImage() {
        val pathString = "Board_image" + System.currentTimeMillis()
        Log.e("Image saved", "uploadBoardImage: $pathString")
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(pathString)
        sRef.putFile(imageURI!!).addOnSuccessListener { takeSnapshot ->
                takeSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { url ->
                    Log.i("dowloadableImage", url.toString())
                    mBoardImageURL = url.toString()
                    Log.e("kkkk", "uploadBoardImage: $mBoardImageURL")
                    createBoard()
                }
            }
    }

    private fun createBoard() {
        val assignedUsersArrayList:ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())
        if(mBoardImageURL==null)
            mBoardImageURL=""
        val board = Board(binding?.etBoardName?.text.toString(),
            mBoardImageURL!!,mUserName,assignedUsersArrayList)
        FirestoreClass().registerBoard(this, board)
    }

    fun boardCreationSuccess() {
        hideProgressDialog()
        startActivity(Intent(this,MainActivity::class.java))
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarCreateBoardActivity)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_white_color_back_24dp)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}