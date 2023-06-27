package com.pctipsguy.projemanag

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.pctipsguy.projemanag.databinding.ActivityMainBinding
import com.pctipsguy.projemanag.databinding.AppBarMainBinding
import com.pctipsguy.projemanag.databinding.ContentMainBinding
import com.pctipsguy.projemanag.databinding.NavHeaderMainBinding

class MainActivity : BaseActivity(),NavigationView.OnNavigationItemSelectedListener{

    private var binding:ActivityMainBinding? = null
    private var appBarBinding:AppBarMainBinding? = null
    private var navbinding:NavHeaderMainBinding? = null
    private var contentBinding:ContentMainBinding?=null
    private lateinit var mUserName:String
    private lateinit var mSharedPreferences: SharedPreferences

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            FirestoreClass().signInUser(this)
        }
    }

    private var boardActivityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode== Activity.RESULT_OK){
            FirestoreClass().getBoardsList(this)
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
            val adapter = BoardItemsAdapter(this,boardsList)
            adapter.setOnClickListener(object:BoardItemsAdapter.OnClickListener{
                override fun onClick(position: Int, model: Board) {
                    startActivity(Intent(this@MainActivity,TaskListActivity::class.java).putExtra(Constants.DOCUMENT_ID,model.documentId))
                }

            })
            contentBinding?.rvBoardsList?.adapter = adapter
        }
        else{
            contentBinding?.tvNoBoardsAvailable?.visibility = View.VISIBLE
            contentBinding?.rvBoardsList?.visibility = View.GONE
        }
    }
}