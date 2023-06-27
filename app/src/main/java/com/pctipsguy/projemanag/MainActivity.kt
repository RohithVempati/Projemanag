package com.pctipsguy.projemanag

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pctipsguy.projemanag.databinding.ActivityMainBinding
import com.pctipsguy.projemanag.databinding.AppBarMainBinding
import com.pctipsguy.projemanag.databinding.ContentMainBinding
import com.pctipsguy.projemanag.databinding.DialogSearchMemberBinding
import com.pctipsguy.projemanag.databinding.NavHeaderMainBinding

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener{

    private var mPosition:Int? = null
    private var adapter:BoardItemsAdapter? = null
    private var binding:ActivityMainBinding? = null
    private lateinit var mUser:User
    private var appBarBinding:AppBarMainBinding? = null
    private var navbinding:NavHeaderMainBinding? = null
    private var contentBinding:ContentMainBinding?=null
    private var boardtest:String? = null
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences
    private var imageURI: Uri? = null
    private var mBoardImageURL:String? = null

    private fun uploadBoardImage(adapter:BoardItemsAdapter,position:Int) {
        showProgressDialog(resources.getString(R.string.please_wait))
        val pathString = "Board_image" + System.currentTimeMillis()
        Log.e("Image saved", "uploadBoardImage: $pathString")
        val sRef: StorageReference = FirebaseStorage.getInstance().reference.child(pathString)
        sRef.putFile(imageURI!!).addOnSuccessListener { takeSnapshot ->
            takeSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { url ->
                Log.i("dowloadableImage", url.toString())
                mBoardImageURL = url.toString()
                adapter.notifyEditPic(mUser, position, mBoardImageURL!!)
                Log.e("kkkk", "uploadBoardImage: $mBoardImageURL")
            }
        }
        mBoardImageURL = null
    }

    private val openGalleryLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { result ->
            if (result != null) {
                imageURI = result
                uploadBoardImage(adapter!!,mPosition!!)
            }
        }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreClass().signInUser(this)
        }
    }

    private var boardActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== Activity.RESULT_OK){
            updateNavUserDetails(mUser,true)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        mSharedPreferences =
            this.getSharedPreferences(Constants.PROGEMANAG_PREFERENCES, Context.MODE_PRIVATE)

        val tokenUpdated = mSharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().signInUser(this@MainActivity, true)
        } else {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                updateFCMToken(token)
            }
        }

        appBarBinding = AppBarMainBinding.bind(binding?.root!!)
        appBarBinding?.toolbarMainActivity?.setNavigationIcon(R.drawable.ic_action_navigation_menu)
        appBarBinding?.toolbarMainActivity?.title = "Welcome"
        setSupportActionBar(appBarBinding?.toolbarMainActivity)
        appBarBinding?.toolbarMainActivity?.setNavigationOnClickListener {
        toggleDrawer()
        }
        binding?.navView?.setNavigationItemSelectedListener(this)
        contentBinding = ContentMainBinding.bind(binding?.root!!)
        contentBinding?.fabCreateBoard?.setOnClickListener {
            boardActivityLauncher.launch(Intent(this,CreateBoardActivity::class.java).putExtra(Constants.Name,mUserName))
        }
        FirestoreClass().signInUser(this,true)


    }
    private fun toggleDrawer(){
        if(binding?.drawerLayout!!.isDrawerOpen(GravityCompat.START)){
            binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        }else{
            binding?.drawerLayout!!.openDrawer(GravityCompat.START)
        }
    }

    fun updateNavUserDetails(user:User,readBoards:Boolean){
        mUser = user
        hideProgressDialog()
        if(readBoards){
            Log.e("test","$readBoards")
            showProgressDialog(resources.getString(R.string.please_wait))
            FirestoreClass().getBoardsList(this)
        }
        mUserName =  user.name
        navbinding = NavHeaderMainBinding.bind(binding?.navView!!.getHeaderView(0))
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(navbinding?.ivUserImage!!)
        navbinding?.tvUsername?.text = user.name
    }

    private fun updateFCMToken(token: String) {

        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        showProgressDialog(resources.getString(R.string.please_wait))
        FirestoreClass().updateUserProfile(this@MainActivity, userHashMap)
    }

    fun tokenUpdateSuccess() {

        hideProgressDialog()

        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        FirestoreClass().signInUser(this@MainActivity, true)
    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.nav_my_profile -> {
                val intent = Intent(this, MyProfileActivity::class.java)
                resultLauncher.launch(intent)
            }
            R.id.nav_sign_out ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
        binding?.drawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun popBoardsinUI(boardsList:ArrayList<Board>){
        hideProgressDialog()
        if(boardsList.size>0){
            contentBinding?.tvNoBoardsAvailable?.visibility = View.GONE
            contentBinding?.rvBoardsList?.visibility = View.VISIBLE
            contentBinding?.rvBoardsList?.layoutManager = LinearLayoutManager(this)
            contentBinding?.rvBoardsList?.setHasFixedSize(true)
            adapter = BoardItemsAdapter(this,boardsList)
            adapter!!.setOnClickListener(object:BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board,edit:Boolean) {
                    mPosition = position
                    if(!edit)
                        startActivity(Intent(this@MainActivity,TaskListActivity::class.java).putExtra(Constants.DOCUMENT_ID,model.documentId))
                    else{
                        openGalleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

                    }

                }

            })
            contentBinding?.rvBoardsList?.adapter = adapter
            val editSwipeHandler = object : SwipeToEditCallback(this@MainActivity) {
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    val dialog = Dialog(this@MainActivity)
                    val dialogSearchMemberBinding: DialogSearchMemberBinding =
                        DialogSearchMemberBinding.inflate(layoutInflater)
                    dialog.setContentView(dialogSearchMemberBinding.root)
                    dialogSearchMemberBinding.tvDialogTitle.text = "Change Board Name:"
                    dialogSearchMemberBinding.tvAdd.text = "Update"
                    dialogSearchMemberBinding.etField.hint = "Board Name"
                    dialogSearchMemberBinding.tvAdd.setOnClickListener(View.OnClickListener {

                        boardtest = dialogSearchMemberBinding.etEmailSearchMember.text.toString()
                        adapter!!.notifyEditItem(
                            mUser, viewHolder.adapterPosition,boardtest!!
                        )
                        dialog.dismiss()

                    })
                    dialogSearchMemberBinding.tvCancel.setOnClickListener(View.OnClickListener {
                        dialog.dismiss()
                        updateNavUserDetails(mUser,true)
                    })
                    dialog.show()
                }
            }
            val deleteSwipeHandler = object : SwipeToDeleteCallback(this@MainActivity) {
                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int
                ) {
                    adapter!!.notifyDeleteItem(mUser, viewHolder.adapterPosition)
                }
            }
            val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
            val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
            editItemTouchHelper.attachToRecyclerView(contentBinding?.rvBoardsList)
            deleteItemTouchHelper.attachToRecyclerView(contentBinding?.rvBoardsList)
        }
        else{
            contentBinding?.tvNoBoardsAvailable?.visibility = View.VISIBLE
            contentBinding?.rvBoardsList?.visibility = View.GONE
        }
    }
}